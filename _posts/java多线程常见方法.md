### java多线程

- #### wait，notify，notifyAll

  **wait**

  Wait方法属于Object类，不可覆写。必须在同步代码块中调用，否则会抛异常

  wait方法的作用是使调用该方法的线程立即等待，直到被唤醒。或者在wait方法中传入一个等待时间，超时会自动唤醒。**调用wait方法后会立即释放掉对象锁**

  

  **notify & notifyAll**

  调用对象的notify方法唤醒等待的线程，如果有多个，则随机唤醒一个。

  notifyAll唤醒所有等待的线程，调用notify/notifyAll不会立即释放对象锁，要等到同步代码块的程序执行完才释放

  等待中的线程被唤醒后并不能立即运行，还要与其他线程竞争对象锁，获得对象锁后才可以继续运行。

   

  **demo**

  线程类

  ```java
  class MyThread extends Thread {
      private Object mLock;
  
      public MyThread(Object lock) {
          this.mLock = lock;
      }
  
      @Override
      public void run() {
          String name = Thread.currentThread().getName();
          synchronized (mLock) {
              try {
                  System.out.println(name + "wait前");
                  mLock.wait();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
  
              System.out.println(name+"被唤醒");
          }
      }
  }
  ```

  main方法

  ```java
  public class Main {
      public static void main(String[] args) throws InterruptedException {
         Object lock = new Object();
         Thread thread1 = new MyThread(lock);
         Thread thread2 = new MyThread(lock);
         thread1.start();
         thread2.start();
         Thread.sleep(2000);
         synchronized (lock){
             System.out.println("主线程随机唤醒一个线程");
             //Thread.sleep(8000);
             lock.notify();
         }
      }
  }
  ```

  运行结果

  ![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge501j7ttxj31be0cu75r.jpg)

   

  程序没有执行结束，因为在主线程调用notify只会随机唤醒一个线程，Thread-1还在无限期等待

  可以通过调用notifyAll唤醒所有线程

  ![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge506osddxj31q20fqtax.jpg)

​      也可以在调用wait的时候传入一个时间，超时自动唤醒

- #### sleep

  sleep有两个重载方法

  ```java
  sleep(long millis)//毫秒
  ```

  ```Java
  sleep(long millis, int nanos)//毫秒，纳秒
  ```

  sleep的作用是使当前线程暂停执行一段时间，其他线程执行，继续执行的时间不一定等于休眠的时间，取决于系统的线程调度

  ```java
  public static void main(String[] args) throws InterruptedException {
      long time = System.currentTimeMillis();
      Thread.sleep(2000);
      System.out.println(System.currentTimeMillis()-time);
  }
  ```

  **Result:2002**

  **在休眠期间，当前线程不会丢失获得的锁或监视器**

   

  Thread.sleep和具体的线程对象thread.sleep的效果是一样的，**都是作用于当前线程**，而不是thread线程

  因为sleep方法是一个静态方法

- #### join方法

  JDK1.8源码

  无参

  ![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge568sjo2ej31is07mgmi.jpg)



​		有参![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge56an2nooj321q1botgj.jpg)



无参方法会调用有参方法

join（0）的时候只要线程还活着，就无限调用wait（0）。有参的时候，调用wait（timeout），直到超时，跳出循环。而join方法又是一个同步方法，即在调用wait方法的时候，已经取得对象锁。

在当前线程调用其他线程的join方法，当前线程会因为间接调用了wait方法而等待，直到其他线程结束或超时会调用notifyAll方法唤醒当前线程



**demo**

MyThread

```java
class MyThread extends Thread {
    private String name;
    public MyThread(String name){
        this.name=name;
    }
    public void run(){
        for(int i=1;i<=5;i++){
            System.out.println(name+"-"+i);
        }
    }
}
```

Main

```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        MyThread thread1 = new MyThread("thread1");
        MyThread thread2 = new MyThread("thread2");
        thread1.start();
        thread2.start();
        //thread1.join();
        //thread2.join();
        for (int i = 0; i < 5; i++) {
            System.out.println("main-"+i);
        }
    }
}
```

注释掉上面两行，先在普通情况下运行，结果肯定是thread1，thread2，main3个线程交替执行

![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge56mtvd6sj311g0ts40o.jpg)



**加入thread1.join（）**，运行结果如下

![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge56p84vohj31bi0uqgo1.jpg)

分析一下，在thread1，thread2先后执行start后，轮到main线程了。在main线程里调用了thread1.join，main线程等待，直到thread1结束。main线程被唤起，然后和thread2交替执行。



**再加入thread2.join**，运行结果如下

![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge56tx32uhj317m0u6di6.jpg)

前面的步骤和相似，但是当thread1结束后，main被唤起，紧接着又执行thread2.join，main线程再次被等待，直到thread2结束。所以main最后执行



- #### interrupt，interrupted，isInterrued

线程中断相关方法

thread.interrupt（）只是给thread线程设置一个中断标志，**实际上thread还会继续执行**

```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        final Thread t1 = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    System.out.println("t1-" + i);
                }

            }
        };
        t1.start();
        t1.interrupt();
        System.out.println("t1执行了interrupt方法");
        System.out.println("t1线程是否存活："+t1.isAlive());
        Thread.sleep(2000);
        System.out.println("主线程执行完毕");
    }

}
```

运行结果

![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge5tlsa0cfj30o50f43yw.jpg)



Thread.interrued：当前线程是否被中断，并且一次调用以后会清除中断状态。也就是说，如果当前想成被中断了，那么第一次调用interrupted返回true，并清除中断状态，第二次调用interrupt就返回false，除非又被中断了一次

```java
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().interrupt();
        System.out.println("第一次调用interrupted："+Thread.interrupted());
        System.out.println("第二次调用interrupted："+Thread.interrupted());
        System.out.println("再中断一次");
        Thread.currentThread().interrupt();
        System.out.println("第三次调用interrupted："+Thread.interrupted());
    }

}
```

运行结果

![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge5u1xhwwtj30r4086glx.jpg)



**thread.isInterrupted**，判断thread有没有被中断，不会清除中断状态

```java
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread.currentThread().interrupt();
        System.out.println("第一次调用interrupted："+Thread.currentThread().isInterrupted());
        System.out.println("第二次调用interrupted："+Thread.currentThread().isInterrupted());
        
    }

}
```

运行结果

![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge5u5x7fecj30qk06bwen.jpg)



**如何真正中断线程的执行？**

线程会在run方法执行完毕后结束，根据这点，在thread.interrupt调用以后，isinterrupted会返回true，在这里结束run方法就可以了



```java
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        final Thread thread = new Thread(){
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    if (isInterrupted()){
                        System.out.println("thread监测到中断，结束run方法");
                        return;
                    }
                    System.out.println("thread-"+i);
                }
            }
        };
        thread.start();
        for (int i = 0; i < 10; i++) {
            System.out.println("main-"+i);
        }
        thread.interrupt();
        System.out.println("thread调用interrupt");

    }

}
```

![](http://ww1.sinaimg.cn/large/006nB4gFgy1ge5ufwabuij30q40g3aak.jpg)

