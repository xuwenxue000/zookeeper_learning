import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;


/**
 * 分布式锁
 * Created by william on 2017/3/6.
 */
public class LockTest {

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
    private  int i=0;
    /**
     * 注释掉锁之后会小于100,加锁等于100
     */
    @Test
    public void test(){
        final LockTest test = new LockTest();
        try {

            curatorFramework.start();
            final String subpath=path+"/test2";
           // final CountDownLatch countDownLatch = new CountDownLatch(100);
            final InterProcessMutex lock = new InterProcessMutex(curatorFramework,subpath);

            for(int i=0;i<10;i++){
                new Thread(new Runnable() {
                    public void run() {
                        try{
                           //
                            Thread.sleep(1);
                            lock.acquire();
                            for(int j=0;j<10;j++){
                                test.i=test.i+1;
                            }


                        }catch (Exception e){
                            e.printStackTrace();
                        }finally {
                            try{
                               lock.release();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        //countDownLatch.countDown();
                    }
                }).start();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(10000);
            System.out.println(test.i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
