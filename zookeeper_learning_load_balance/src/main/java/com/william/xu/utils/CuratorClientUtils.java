package com.william.xu.utils;

import com.william.xu.config.ZookeeperConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

/**
 * Created by william on 2017/3/8.
 */
public class CuratorClientUtils {

    private static volatile  CuratorFramework defaultClient = null;

    public static CuratorFramework  getNewSessionClient(String host,String port,Integer sessionTimeoutMs,String namespace){
        RetryPolicy retryPolicy = new RetryNTimes(10,3000);
        CuratorFramework curatorFramework = CuratorFrameworkFactory
                .builder()
                .connectString(host+":"+port)
                .sessionTimeoutMs(sessionTimeoutMs)
                .maxCloseWaitMs(5)
                .retryPolicy(retryPolicy)
                .namespace(namespace)
                .build()
                ;
        curatorFramework.start();
        return curatorFramework;
    }

    private CuratorClientUtils(){

    }


    public static void  initDefaultSessionClient(String host,String port,Integer sessionTimeoutMs,String namespace){
        if(defaultClient==null){
            synchronized (CuratorClientUtils.class){
                if(defaultClient==null){
                    defaultClient= getNewSessionClient(host,port,sessionTimeoutMs,namespace);
                }
            }
        }
    }
    public static CuratorFramework getDefaultSessionClient(){
        if(defaultClient==null){
            ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
            String host = zookeeperConfig.getHost();
            String port =zookeeperConfig.getPort();
            Integer sessionTimeoutMs=60000;
            String namespace=zookeeperConfig.getNamespace();
            initDefaultSessionClient(host,port,sessionTimeoutMs,namespace);
        }
        return defaultClient;
    }
}
