package org.littleshoot.proxy;

import io.netty.handler.codec.http.HttpRequest;

import java.util.Queue;

/**
 * Created by dxy on 2018/8/1.
 *
 */
public class ChainedProxyManagerTongDun implements ChainedProxyManager {
    @Override
    public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
        ChainedProxy chainedProxy = new ChainedProxyAdapterTongDun();
        chainedProxies.add(chainedProxy);
    }
}
