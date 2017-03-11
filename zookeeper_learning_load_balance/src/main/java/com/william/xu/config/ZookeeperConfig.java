package com.william.xu.config;


/*

 */
//@Component
public class ZookeeperConfig {
    //@Value("${zookeeper.host}")
    private  String host="127.0.0.1";

    //@Value("${zookeeper.port}")
    private  String port="2181";

    //@Value("${zookeeper.namespace}")
    private  String namespace="zookeeper_learning";

    //@Value("${zookeeper.acl}")
    private  String acl;


    public  String getHost() {
        return host;
    }

    public  String getPort() {
        return port;
    }

    public  String getNamespace() {
        return namespace;
    }

    public  String getAcl() {
        return acl;
    }
}
