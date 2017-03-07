import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

/**
 * 增删改查
 * Created by william on 2017/3/6.
 */
public class ConnectionTest {

    @Test
    public void test(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(100,2,2000);
        CuratorFramework curatorFramework = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(2000)
                .retryPolicy(retryPolicy)
                .namespace("test")
                .build()
        ;

        String path="/william";
        try {
            curatorFramework.start();
            //创建节点/test/xu  数据xxx
            curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,"xxx".getBytes());
            //查询数据
            String result = new String(curatorFramework.getData().forPath(path));
            System.out.println(result);
            //更新数据
            result=curatorFramework.setData().forPath(path,"yyy".getBytes()).toString();
            System.out.println(result);

            //再次读取数据
            result = new String(curatorFramework.getData().forPath(path));
            System.out.println(result);
            //删除数据
            curatorFramework.delete().forPath(path);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
