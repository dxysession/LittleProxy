package org.littleshoot.proxy;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by dxy on 2019/1/22.
 */
public class ZdyProxyPool implements Runnable {
    private volatile static ZdyProxyPool singleton;
    private static final Logger LOG = LoggerFactory.getLogger(ZdyProxyPool.class);

    private List<String> zdyProxyPool;
    private String ZDY_ALL_URI = Launcher.ZDY_ALL_URI;

    private ZdyProxyPool() {
    }

    static ZdyProxyPool getSingleton() {
        if (singleton == null) {
            synchronized (ZdyProxyPool.class) {
                if (singleton == null) {
                    singleton = new ZdyProxyPool();
                    singleton.refreshProxyPool();
                    new Thread(singleton).start();
                }
            }
        }
        return singleton;
    }

    public String getProxy() {
        String proxy;
        int index = (int) (Math.random() * this.zdyProxyPool.size());
        proxy = this.zdyProxyPool.get(index);
        return proxy;

    }

    private void refreshProxyPool() {
        try {
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;
            try {
                HttpGet httpGet = new HttpGet(this.ZDY_ALL_URI);
                client = HttpClients.createDefault();
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                if (!"[]".equals(result)) {
                    this.zdyProxyPool = JSON.parseArray(result, String.class);
                    LOG.info("代理数量 --> " + Integer.toString(this.zdyProxyPool.size()));
                }
            } finally {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                refreshProxyPool();
                Thread.sleep(5000);
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }
    }

//    public static void main(String[] args) {
//        ZdyProxyPool zdyProxyPool = ZdyProxyPool.getSingleton();
//        for (int i = 0; i < 100; i++) {
//            System.out.println(zdyProxyPool.getProxy());
//        }
//    }
}
