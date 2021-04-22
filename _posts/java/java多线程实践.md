# java多线程实践

## 1、生产者，消费者实现

```java
class Producer implements Runnable{

    Queue<Integer> queue = new LinkedList<>();
    int maxSize;

    public Producer(Queue<Integer> queue, int maxSize){
        this.queue = queue;
        this.maxSize = maxSize;
    }

    @Override
    public void run() {
        while (true){
            synchronized (queue){
                //用while应对多个生产者、消费者
                //if只能适用单一生产者，消费者
                while (queue.size() >= maxSize){
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                queue.add(1);
                System.out.println("生产："+queue.size());
                queue.notifyAll();
            }
        }
    }
}

class Consumer implements Runnable{

    Queue<Integer> queue = new LinkedList<>();
    int maxSize;

    public Consumer(Queue<Integer> queue){
        this.queue = queue;
        this.maxSize = maxSize;
    }

    @Override
    public void run() {
        while (true){
            synchronized (queue){
                while (queue.isEmpty()){
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                queue.poll();
                System.out.println("消费："+queue.size());
                queue.notifyAll();
            }
        }
    }
}
```

测试代码

```java
 public static void main(String[] args) {
        Queue<Integer> queue = new LinkedList<>();
        Thread product = new Thread(new Producer(queue,5));
        Thread product2 = new Thread(new Producer(queue,5));
        Thread consume = new Thread(new Consumer(queue));
        Thread consume2 = new Thread(new Consumer(queue));
        product.start();
        product2.start();
        consume.start();
        consume2.start();
    }
```



**信号量实现方法**

```java
public class Test {
    static Semaphore empty = new Semaphore(5);
    static Semaphore full = new Semaphore(0);
    static Semaphore mutex = new Semaphore(1);
    public static void main(String[] args) {
        Queue<Integer> queue = new LinkedList<>();
        Thread product = new Thread(new Producer(queue));
        Thread product2 = new Thread(new Producer(queue));
        Thread consume = new Thread(new Consumer(queue));
        Thread consume2 = new Thread(new Consumer(queue));
        product.start();
        product2.start();
        consume.start();
        consume2.start();

    }
     static class Producer implements Runnable{
        Queue<Integer> queue = new LinkedList<>();

        public Producer(Queue<Integer> queue){
            this.queue = queue;;
        }

        @Override
        public void run() {
            while (true){
                try {
                    empty.acquire();
                    mutex.acquire();
                    queue.add(1);
                    System.out.println("生产:"+queue.size());
                    mutex.release();
                    full.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Consumer implements Runnable{

        Queue<Integer> queue = new LinkedList<>();
        int maxSize;

        public Consumer(Queue<Integer> queue){
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true){
                try {
                    full.acquire();
                    mutex.acquire();
                    queue.poll();
                    System.out.println("消费:"+queue.size());
                    mutex.release();
                    empty.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```



## 2、两个线程交替打印0，100

```java
class MyRunnable implements Runnable{
    int num = 0;
    @Override
    public void run() {
        while (true){
            synchronized (this){
                if (num > 100){
                    break;
                }
                notify();
                System.out.println(Thread.currentThread().getName()+":"+num);
                num++;
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

测试代码

```java
public static void main(String[] args) {
        MyRunnable runnable = new MyRunnable();
        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        t1.start();
        t2.start();
    }
```

