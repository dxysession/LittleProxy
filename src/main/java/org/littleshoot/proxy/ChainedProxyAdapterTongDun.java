package org.littleshoot.proxy;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
/**
 * Created by dxy on 2018/8/1.
 */

public class ChainedProxyAdapterTongDun extends ChainedProxyAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ChainedProxyAdapterTongDun.class);
    private InetSocketAddress remoteAddress;
    private String ZDY_URI;

    ChainedProxyAdapterTongDun() {
        this.remoteAddress = null;
        this.ZDY_URI = Launcher.ZDY_URI;
//        this.ZDY_URI = "http://10.9.123.200:8088/get?task_name=littleproxy";
//        this.ZDY_URI = "http://123.59.136.87:6800/get?task_name=littleproxy_local";
    }

    @Override
    public InetSocketAddress getChainedProxyAddress() {
        if (this.remoteAddress != null) {
            return this.remoteAddress;
        }
        try {
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;
            try {
                HttpGet httpGet = new HttpGet(this.ZDY_URI);
                client = HttpClients.createDefault();
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
//                LOG.info(result);
                if (!"".equals(result)) {
                    result = result.replace("http://", "");
                }
                String[] splitAddress = result.split(":");
                String hostname = splitAddress[0];
                int port = Integer.parseInt(splitAddress[1]);
                this.remoteAddress = new InetSocketAddress(hostname, port);
            } finally {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            }
        } catch (Exception e) {
        }
        return this.remoteAddress;
    }
}
