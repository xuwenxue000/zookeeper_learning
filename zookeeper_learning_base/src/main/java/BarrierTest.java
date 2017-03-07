import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程同步,还是有所不同,
 * CyclicBarrier的是线程等待,
 * DistributedBarrier,如果移除的早就不行了,至少要在本地判断本地的锁已全部锁了.然后在remove,否则可能造成锁没释放
 * DistributedDoubleBarrier.应该是利用会话创建临时节点处理的(如果使用一个会话会报错),这个需要每个线程新创建一个会话,enter锁,leave,释放,这个是线程等待,与jdk实现的效果比较一致
 * Created by william on 2017/3/6.
 */
public class BarrierTest {

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

    CyclicBarrier cyclicBarrier = new CyclicBarrier(3);
    DistributedBarrier distributedBarrier = new DistributedBarrier(curatorFramework,path+"/barrier");


    class Runner implements Runnable{

        private  String name;

        public Runner(String name){
            this.name = name;

        }

        public void run() {
            try {
                System.out.println(name +" init");
                //jdk自带
                //cyclicBarrier.await();

                //第一种分布式同步
                //distributedBarrier.setBarrier();
                //distributedBarrier.waitOnBarrier();

                //第二种同步
                System.out.println(name+" running");
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 线程同步
     */
    @Test
    public void test(){
        try {
            curatorFramework.start();
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            executorService.submit(new Runner("1"));
            executorService.submit(new Runner("2"));
            executorService.submit(new Runner("3"));
            Thread.sleep(1000);
            distributedBarrier.removeBarrier();
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    class Runner2 implements Runnable{

        private  String name;

        public Runner2(String name){
            this.name = name;

        }

        public void run() {
            try {
                System.out.println(name +" init");
                CuratorFramework curatorFramework = CuratorFrameworkFactory
                        .builder()
                        .connectString("localhost:2181")
                        .sessionTimeoutMs(2000)
                        .retryPolicy(retryPolicy)
                        .namespace("test")
                        .build()
                        ;
                curatorFramework.start();
                DistributedDoubleBarrier distributedDoubleBarrier = new DistributedDoubleBarrier(curatorFramework,path+"/barrier2",3);
                distributedDoubleBarrier.enter();
                System.out.println(name+" running");
                distributedDoubleBarrier.leave();
                System.out.println(name+" leaving");
                curatorFramework.close();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 第二种
     */
    @Test
    public void test2(){
        try {

            ExecutorService executorService = Executors.newFixedThreadPool(3);
            executorService.submit(new Runner2("1"));
            executorService.submit(new Runner2("2"));
            executorService.submit(new Runner2("3"));
            executorService.shutdown();

            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



}
