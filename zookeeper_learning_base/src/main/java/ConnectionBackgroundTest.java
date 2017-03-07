import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

/**
 * 增删改查
 * Created by william on 2017/3/6.
 */
public class ConnectionBackgroundTest {

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

        final String  path="/william/background";
        try {
            curatorFramework.start();
            //创建节点/test/xu  数据xxx

            curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).inBackground(new BackgroundCallback() {
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                    String result = new String(curatorFramework.getData().forPath(path));
                    System.out.println(result);
                    //删除数据

                }
            }).forPath(path,"ttt".getBytes());;
                    //
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("rrr");

        String result = null;
        try {
            result = new String(curatorFramework.getData().forPath(path));
            System.out.println("1:"+result);
            curatorFramework.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
