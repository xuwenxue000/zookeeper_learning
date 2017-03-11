import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 节点和子节点监控
 * Created by william on 2017/3/6.
 */
public class WatcherTest {

    RetryPolicy retryPolicy = new ExponentialBackoffRetry(100,2,2000);
    CuratorFramework curatorFramework = CuratorFrameworkFactory
            .builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(2000)
            .retryPolicy(retryPolicy)
            .namespace("test")
            .build()
            ;
    String path="/william1";
    /**
     * 节点数据变化监控就,删除监控不到
     */
    @Test
    public void test(){
        try {

            curatorFramework.start();
            curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,"ddd".getBytes());
            final  NodeCache nodeCache = new NodeCache(curatorFramework,path,false);
            nodeCache.start();
            nodeCache.getListenable().addListener(new NodeCacheListener() {
                public void nodeChanged() throws Exception {
                    System.out.println("1:"+new String(nodeCache.getCurrentData().getData()));
                }
            });

            curatorFramework.setData().forPath(path,"zzz".getBytes());
            curatorFramework.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 监控子节点,可以监控到删除
     */
    @Test
    public void test2(){
        try {
            String subPath=path+"/xxx";
            curatorFramework.start();

            PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework,path,true);
            pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                    System.out.println(pathChildrenCacheEvent.getType().toString());
                    ChildData data = pathChildrenCacheEvent.getData();
                    if(data!=null){
                        System.out.println(new String(data.getData()));
                    }
                }
            });


            curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(subPath,"ddd".getBytes());
            Thread.sleep(1000);
            curatorFramework.setData().forPath(subPath,"xxxx".getBytes());
            Thread.sleep(1000);
            curatorFramework.delete().forPath(subPath);




        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
