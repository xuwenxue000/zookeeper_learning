package com.william.xu;

import com.alibaba.fastjson.JSON;
import com.william.xu.bean.EnumConstServerNodeStatus;
import com.william.xu.bean.ServerNode;
import com.william.xu.config.ZookeeperPathConstants;
import com.william.xu.utils.CuratorClientUtils;
import com.william.xu.utils.ServerUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerRegister{

    Logger logger = Logger.getLogger(this.getClass());
    private boolean online=false;
    private boolean master=false;

    PathChildrenCache pathChildrenCache=null;
    /**
     * 注册服务
     *
     * 首先检查是否持久话的数据库了,没有的话将自己持久化到数据库内,否则读取信息
     * 然后注册到zookeeper上,这个时候没有设置masterid
     * 最后再获取所有的服务器节点,检查是否包含自己,如果包含说明注册成功,否则注册失败
     * 注册完成之后,
     *      首先查找当前的主节点,如果找到了.设置为自己的masterid,如果没找到,说明没有主,就进行竞争主节点
     *      然后再检查是否添加了服务器列表监听,如果加了就跳过,如果没加,就添加监听逻辑
     *      监听逻辑内监听了
     *          节点消失逻辑:如果消失的是当前节点,再次调用regServer,如果不是则判断消失的是否为主节点,如果是,则开始竞争主节点,其他待补充
     *
     *
     *
     * @return
     */
    public boolean regServer() {
        CuratorFramework client = CuratorClientUtils.getDefaultSessionClient();
        try {
            final String mac = ServerUtils.getLocalMac();
            String ip = ServerUtils.getLocalIP();
            String port=ServerUtils.getLocalPort();
            ServerNode serverNode = new ServerNode();
            serverNode.setMac(mac);
            serverNode.setIp(ip);
            serverNode.setPort(port);
            serverNode.setRegTime(new Date());
            serverNode.setServerNodeStatus(EnumConstServerNodeStatus.ONLINE);
            String path = ZookeeperPathConstants.SERVERS_PATH+ZookeeperPathConstants.SEP+mac;
            byte[] data = JSON.toJSONString(serverNode).getBytes();
            try{
                client.delete().forPath(path);
                logger.info("delete old session data");
            }catch (Exception e){
                logger.info(" ignore");
            }

            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,data);
            Map<String,ServerNode> servers= getServerList();
            if(servers!=null){
                if(servers.containsKey(mac)){
                    online=true;
                    logger.info("reg success!");
                    String masterMac = getMasterMac(servers);
                    if(masterMac==null){
                        grabMaster();
                    }else{
                        serverNode.setMasterId(masterMac);
                        data = JSON.toJSONString(serverNode).getBytes();
                        client.setData().forPath(path,data);
                    }
                    if(pathChildrenCache==null){
                        pathChildrenCache = new PathChildrenCache(client,ZookeeperPathConstants.SERVERS_PATH,true);
                        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
                        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                                ChildData childData =  pathChildrenCacheEvent.getData();
                                ServerNode watchServer=null;
                                if(childData!=null){
                                    byte[] data = childData.getData();
                                    if(data!=null){
                                        watchServer = JSON.parseObject(new String(data),ServerNode.class);
                                    }
                                }

                                switch (pathChildrenCacheEvent.getType()){
                                    case CHILD_REMOVED:{
                                        if(watchServer!=null){
                                            String watchMac = watchServer.getMac();
                                            String masterId = watchServer.getMasterId();
                                            //如果是自己
                                            if(watchMac.equals(mac)){
                                                regServer();
                                            }else{
                                                Thread.sleep(5000);
                                                if(watchMac.equals(masterId)) {//如果不是自己,是其他的某个主机,但这个主机是master
                                                    grabMaster();
                                                }
                                                if(master){
                                                    //做节点宕机的处理
                                                }
                                            }


                                        }
                                    }break;
                                    case CHILD_ADDED:{
                                        //节点新增处理

                                    }break;
                                    case CHILD_UPDATED:{
                                        //节点修改处理
                                    }break;
                                    default:{

                                    }
                                }
                            }
                        });
                    }
                }else{
                    logger.error("注册失败了");
                }
            }

        } catch (Exception e) {
            logger.error("注册失败了");
            logger.error(e.getMessage(),e);
        }

        return false;
    }

    /**
     * 获取主节点mac
     * 取节点之中存在masterId中数据最多的的mac
     * @param map
     * @return
     */
    public String  getMasterMac(Map<String, ServerNode> map){
        String result = null;
        if(map==null){
            map = this.getServerList();
            if(map!=null){
                result = getMasterMac(map);
            }
        }else{
            Integer maxCount =0;
            Map<String,Integer> masterCount=new HashMap<String,Integer>();
            for(Map.Entry<String,ServerNode> entry : map.entrySet()){
                ServerNode server = entry.getValue();
                String serverMaster = server.getMasterId();
                Integer count = masterCount.get(serverMaster);
                if(count==null){
                    count=0;
                }
                masterCount.put(serverMaster,++count);
                if(count>maxCount){
                    maxCount=count;
                    result = serverMaster;
                }
            }
        }
        return result;
    }

    /**
     * 竞争主节点
     *
     * 首先获取一次主节点,以免冲突
     * 如果主节点已存在,不再竞争,直接更新自己节点数据的主节点信息
     * 如果主节点mac与自己mac一直,说明自己是主,否则自己不是主
     */
    private void  grabMaster(){
        final CuratorFramework client = CuratorClientUtils.getDefaultSessionClient();
        String masterPath = ZookeeperPathConstants.MASTER_PATH;
        LeaderSelector selector = new LeaderSelector(client, masterPath, new LeaderSelectorListenerAdapter() {
            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                logger.info("begin grabMaster");
                String masterMac = getMasterMac(null);
                if(masterMac==null){
                    master=true;
                    logger.info("grab master success");
                    masterOptions();
                }else{
                    String mac = ServerUtils.getLocalMac();
                    if(mac.equals(masterMac)){
                        master =true;
                    }else{
                        master=false;
                    }
                    logger.info("set masterId:"+masterMac+";mymac:"+mac);
                    String path = ZookeeperPathConstants.SERVERS_PATH+ZookeeperPathConstants.SEP+mac;
                    byte[] data = client.getData().forPath(path);
                    ServerNode server = JSON.parseObject(new String(data),ServerNode.class);
                    String masterId = server.getMasterId();
                    if(masterId==null||!masterId.equals(masterMac)){
                        server.setMasterId(masterMac);
                        data = JSON.toJSONString(server).getBytes();
                        try {
                            client.setData().forPath(path,data);
                        } catch (Exception e) {
                            logger.error(e.getMessage(),e);
                        }
                    }
                }
                logger.info("finish grabMaster");
            }
        });
        selector.start();
    }

    /**
     * 竞争注解滴成功后的操作
     *
     * 将服务器上所有服务器节点的masterid设置为自己
     */
    private void masterOptions(){
        if(master){
            logger.info("set all node masterId");
            String mac = ServerUtils.getLocalMac();
            Map<String,ServerNode> serfers = getServerList();
            for(Map.Entry<String,ServerNode> entry : serfers.entrySet()){
                String serverMac = entry.getKey();
                ServerNode serverNodeVo = entry.getValue();
                String masterId = serverNodeVo.getMasterId();
                if(masterId==null||!masterId.equals(mac)){
                    serverNodeVo.setMasterId(mac);
                    CuratorFramework client = CuratorClientUtils.getDefaultSessionClient();
                    String path = ZookeeperPathConstants.SERVERS_PATH+ZookeeperPathConstants.SEP+serverMac;
                    byte[] data = JSON.toJSONString(serverNodeVo).getBytes();
                    try {
                        client.setData().forPath(path,data);
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                    }
                }
            }
        }
    }


    public boolean isMaster() {
        return master;
    }

    /**
     * 获取服务器所有节点
     * @return
     */
    public Map<String,ServerNode> getServerList() {
        Map<String,ServerNode> result = null;
        CuratorFramework client = CuratorClientUtils.getDefaultSessionClient();
        String path = ZookeeperPathConstants.SERVERS_PATH;
        try {
            List<String> children = client.getChildren().forPath(path);

            for(String childPath : children){
                byte[] data = client.getData().forPath(ZookeeperPathConstants.SERVERS_PATH+ZookeeperPathConstants.SEP+childPath);
                if(data!=null){
                    String serverString = new String(data);
                    ServerNode serverNodeVo = JSON.parseObject(serverString,ServerNode.class);
                    if(result==null){
                        result=new HashMap<String, ServerNode>();
                    }
                    result.put(childPath,serverNodeVo);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isMaster(Runnable callback) {
        return false;
    }

    public boolean readyOfflineServer() {
        return false;
    }

    public boolean offlineServer() {
        return false;
    }
}
