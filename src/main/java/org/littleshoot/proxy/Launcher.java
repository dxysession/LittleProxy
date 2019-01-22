package org.littleshoot.proxy;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.littleshoot.proxy.impl.ThreadPoolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Properties;

/**
 * Launches a new HTTP proxy.
 */
public class Launcher {

    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);

    private static final String OPTION_DNSSEC = "dnssec";

    private static final String OPTION_PORT = "port";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_MITM = "mitm";

    private static final String OPTION_NIC = "nic";

    private static int myDefaultPort;

    private static int withAcceptorThreads;

    private static int withClientToProxyWorkerThreads;

    private static int withProxyToServerWorkerThreads;

    private static int withConnectTimeout;

    private static int withIdleConnectionTimeout;

    public static String ZDY_URI;

    /**
     * Starts the proxy from the command line.
     *
     * @param args Any command line arguments.
     */
    public static void main(final String... args) {
        pollLog4JConfigurationFileIfAvailable();
        initParam();
        LOG.info("Running LittleProxy with args: {}", Arrays.asList(args));
        final Options options = new Options();
        options.addOption(null, OPTION_DNSSEC, true, "Request and verify DNSSEC signatures.");
        options.addOption(null, OPTION_PORT, true, "Run on the specified port.");
        options.addOption(null, OPTION_NIC, true, "Run on a specified Nic");
        options.addOption(null, OPTION_HELP, false, "Display command line help.");
        options.addOption(null, OPTION_MITM, false, "Run as man in the middle.");

        final CommandLineParser parser = new PosixParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.getArgs().length > 0) {
                throw new UnrecognizedOptionException("Extra arguments were provided in " + Arrays.asList(args));
            }
        } catch (final ParseException e) {
            printHelp(options, "Could not parse command line: " + Arrays.asList(args));
            return;
        }
        if (cmd.hasOption(OPTION_HELP)) {
            printHelp(options, null);
            return;
        }
        final int defaultPort = myDefaultPort;
        int port;
        if (cmd.hasOption(OPTION_PORT)) {
            final String val = cmd.getOptionValue(OPTION_PORT);
            try {
                port = Integer.parseInt(val);
            } catch (final NumberFormatException e) {
                printHelp(options, "Unexpected port " + val);
                return;
            }
        } else {
            port = defaultPort;
        }

        System.out.println("About to start server on port: " + port);
        HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer.bootstrapFromFile("./littleproxy.properties");

        if (cmd.hasOption(OPTION_NIC)) {
            final String val = cmd.getOptionValue(OPTION_NIC);
            bootstrap.withNetworkInterface(new InetSocketAddress(val, 0));
        }

        if (cmd.hasOption(OPTION_DNSSEC)) {
            final String val = cmd.getOptionValue(OPTION_DNSSEC);
            if (ProxyUtils.isTrue(val)) {
                LOG.info("Using DNSSEC");
                bootstrap.withUseDnsSec(true);
            } else if (ProxyUtils.isFalse(val)) {
                LOG.info("Not using DNSSEC");
                bootstrap.withUseDnsSec(false);
            } else {
                printHelp(options, "Unexpected value for " + OPTION_DNSSEC + "=:" + val);
                return;
            }
        }

        bootstrap.withName("TongDunProxy");
        bootstrap.withPort(port);
        bootstrap.withAllowLocalOnly(false);
        bootstrap.withTransparent(false);//Transparent
        bootstrap.withAllowRequestToOriginServer(true);

        ThreadPoolConfiguration threadPoolConfiguration = new ThreadPoolConfiguration();
        threadPoolConfiguration.withAcceptorThreads(withAcceptorThreads);
        threadPoolConfiguration.withClientToProxyWorkerThreads(withClientToProxyWorkerThreads);
        threadPoolConfiguration.withProxyToServerWorkerThreads(withProxyToServerWorkerThreads);
        LOG.info("添加线程池配置");
        bootstrap.withThreadPoolConfiguration(threadPoolConfiguration);
        LOG.info("添加请求超时限制");
        bootstrap.withConnectTimeout(withConnectTimeout);
        bootstrap.withIdleConnectionTimeout(withIdleConnectionTimeout);
        LOG.info("添加MITM中间人");
        bootstrap.withManInTheMiddle(new SelfSignedMitmManager());
        LOG.info("添加代理链");
        bootstrap.withChainProxyManager(new ChainedProxyManagerTongDun());
        System.out.println("About to start...");
        bootstrap.start();
        LOG.info("启动成功");
    }

    private static void printHelp(final Options options, final String errorMessage) {
        if (!StringUtils.isBlank(errorMessage)) {
            LOG.error(errorMessage);
            System.err.println(errorMessage);
        }

        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("littleproxy", options);
    }

    private static void pollLog4JConfigurationFileIfAvailable() {
//        File log4jConfigurationFile = new File("src/test/resources/log4j.xml");
        File log4jConfigurationFile = new File("log4j.xml");
        if (log4jConfigurationFile.exists()) {
            DOMConfigurator.configureAndWatch(log4jConfigurationFile.getAbsolutePath(), 15);
        }
    }

    private static void initParam() {
        try {
            LOG.info("从文件初始化参数");
            InputStream inStream = new FileInputStream(new File("setting.properties"));
            Properties prop = new Properties();
            prop.load(inStream);
            myDefaultPort = Integer.parseInt(prop.getProperty("port"));
            withAcceptorThreads = Integer.parseInt(prop.getProperty("withAcceptorThreads"));
            withClientToProxyWorkerThreads = Integer.parseInt(prop.getProperty("withClientToProxyWorkerThreads"));
            withProxyToServerWorkerThreads = Integer.parseInt(prop.getProperty("withProxyToServerWorkerThreads"));
            withConnectTimeout = Integer.parseInt(prop.getProperty("withConnectTimeout"));
            withIdleConnectionTimeout = Integer.parseInt(prop.getProperty("withIdleConnectionTimeout"));
            ZDY_URI = prop.getProperty("ZDY_URI");
        }
        catch (Exception e){
            LOG.error(e.toString());
        }
    }
}
