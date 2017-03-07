import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Test;

/**
 * 节点和子节点监控
 * Created by william on 2017/3/6.
 */
public class MasterTest {

    RetryPolicy retryPolicy = new ExponentialBackoffRetry(100,2,2000);
    CuratorFramework curatorFramework = CuratorFrameworkFactory
            .builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(2000)
            .retryPolicy(retryPolicy)
            .namespace("test")
            .build()
            ;
    String path="/lock";
    /**
     * 节点数据变化监控就,删除监控不到
     */
    @Test
    public void test(){
        try {
            curatorFramework.start();
            final String subpath=path+"/test1";

            LeaderSelector selector1 = new LeaderSelector(curatorFramework, subpath, new LeaderSelectorListenerAdapter() {
                public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                    System.out.println(Thread.currentThread().getName()+" is master");
                    Thread.sleep(5000);
                    System.out.println(Thread.currentThread().getName()+"  master over");
                }
            });

            selector1.start();
            LeaderSelector selector = new LeaderSelector(curatorFramework, subpath, new LeaderSelectorListenerAdapter() {
                public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                    System.out.println(Thread.currentThread().getName()+" is master");
                    Thread.sleep(5000);
                    System.out.println(Thread.currentThread().getName()+"  master over");
                }
            });
            selector.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
