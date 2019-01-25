package org.littleshoot.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by dxy on 2018/8/1.
 */

public class ChainedProxyAdapterTongDun extends ChainedProxyAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(ChainedProxyAdapterTongDun.class);
    private InetSocketAddress remoteAddress;

    ChainedProxyAdapterTongDun() {
        this.remoteAddress = null;
    }

    @Override
    public InetSocketAddress getChainedProxyAddress() {
        if (this.remoteAddress != null) {
            return this.remoteAddress;
        }
        String result = ZdyProxyPool.getSingleton().getProxy();
        if (!"".equals(result)) {
            result = result.replace("http://", "");
        }
        String[] splitAddress = result.split(":");
        String hostname = splitAddress[0];
        int port = Integer.parseInt(splitAddress[1]);
        this.remoteAddress = new InetSocketAddress(hostname, port);
        LOG.info("获取代理 --> " + result);
        return this.remoteAddress;
    }
}
