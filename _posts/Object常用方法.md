Java面试复习



### 1、Object类的常用方法

**equals** 等价 ，注意与“==”的区别。Object的equals是“==”

子类重写实现

```java
class A{
    private String s1;
    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        A that = (A) o;
        return s1.equals(that.s1);
    }
}
```

**hashcode**

equals和hashcode不能双向推导相等

即使equals也不能推出hashcode相等，如果equals没有被重写（==），那hashcode肯定是一样的



**toString**



**clone**

Protect ,只限子类及同一个包中调用

子类必须要重写clone(public/protected)才能在其他类中调用，因为Object和我们的类不在同一个包中

还要实现Cloneable接口（仅仅只是个标记）

实现

```java
class A implements Cloneable {
    public String s1;

    public A(String s) {
        s1 = s;
    }

    @Override
    protected A clone() throws CloneNotSupportedException {
        return (A) super.clone();
    }
}
```

默认是浅拷贝，需要自己实现深拷贝

```java
@Override
protected A clone() throws CloneNotSupportedException {
    A copy = (A) super.clone();
    String s = copy.s1;
    copy.s1 = new String(s);
    return copy;
}
```



**wait 、notify、notifyAll（多线程相关）**

只能在同步代码块中调用，保证调用时,**当前线程取得了该对象的对象锁**

wait：当前线程释放该对象的对象锁，等待notify/notifyAll

notify：唤醒1个正在wait对象锁的线程，由虚拟机决定唤醒哪一个

notifyAll：唤醒所有等待线程，它们再竞争对象锁

用于线程间通信（可参考操作系统进程间通信）



生产者，消费者模型

信号量实现

![](http://ww1.sinaimg.cn/large/006nwaiFgy1giciulpmt1j30u00tswzv.jpg)

java实现

​	

```java
public class HelloWorld {
    String lock = "lock";
    static final int FULL = 10;
    int content = 0;
    public static void main(String[] args) {
        HelloWorld helloWorld = new HelloWorld();
        Produce produce = helloWorld.new Produce();
        Comsumer comsumer = helloWorld.new Comsumer();
        new Thread(produce).start();
        new Thread(comsumer).start();
    }
    class Produce implements Runnable{

        @Override
        public void run() {
            while (true){
                //一直生产
                synchronized (lock){
                    if (content == FULL){
                        //缓冲池已满，wait线程
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    content++;//生产产品+1
                    System.out.println("当前缓冲区有："+content+"个产品");
                    lock.notify();
                }
            }
        }
    }

    class Comsumer implements  Runnable{

        @Override
        public void run() {
            while (true){
                synchronized (lock){
                    if (content == 0){
                        //缓冲区已经没有产品了，开始等待
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    content--;
                    System.out.println("当前缓冲区有："+content+"个产品");
                    lock.notify();
                }
            }
        }
    }

}
```



## 适配器模式

三个角色：

目标接口：抽象类or接口

适配者Adaptee：已有的组件接口

适配器Adapter：将Adapter转化为Target

**目标接口**

```java
interface Target{
    void request();
}
```

**Adaptee**

```java
class Adaptee{
    public void speciRequest(){
        System.out.println("Adaptee的方法");
    }
}
```



类适配器模式：

adapter继承Adaptee

```java
class Adapter extends Adaptee implements Target{

    @Override
    public void request() {
        speciRequest();
    }
}
```



对象适配器模式

Adapter持有Adaptee

```java
class Adapter implements Target{
    Adaptee mAdaptee;
    
    public Adapter(Adaptee adaptee){
        mAdaptee = adaptee;
    }
    
    @Override
    public void request() {
        mAdaptee.speciRequest();
    }
}
```



## HashMap相关

数组+链表实现

Entry<K,V>[]

**基本属性**

数组初始容量16，最大容量2的30次方，默认负载因子0.75

可以自定义初始容量和负载因子。



刚传入的初始容量不会立马去构建数组，在put的时候如果是空数组才会去构建数组，如果传入的数字不是2的整数次幂，则会用一个最近的大的2的整数次幂代替



**put**

Key可以为null，直接在数组的0位置去更新Entry。从数组第0个位置的Entry作为头节点，向下找key = null的Entry，找到了就更新value值，返回旧value值。找不到就头插法加入到数组第0个位置

Key不为null

* 根据key计算hash，会用到key.hashcode，但还会做一些移位和异或操作，使得hash值尽可能地分散

* 根据hash && （length - 1）得到在数组中对应的位置

* 以数组该位置的Entry为头节点，向下搜索。直到满足一下条件 

  ```java
  entry.hash = hash && (entry.key == key ||key.equals(entry.key))
  ```

  总之就是hash值一定要相同（这也是为什么尽量重写equals也要重写hashcode），key完全相同或等价

  更新entry的value，返回旧value

  找不到满足条件的，头插法插入（包含扩容操作）



**插入节点**

java 7 头插，java8尾插

**在插入之前要扩容**

检查扩容：size >= threshold && table[index]!= null

就是当前size大于等于阈值（容量*负载因子），而且要插入的位置已经有元素了（没有就可以不用扩容，直接插）



扩容详情

* 大小为原来的2倍
* 如果数组容量已经是最大（2的30次方），不扩容，调整阈值为Integer.MAX_VALUE
* 遍历数组，再以每一个元素为头节点，遍历链表。重新计算hash值，以及index值，然后头插法直接插入到新数组的index位置上
* 扩容完成，插入节点。hash和index经过重新计算的



**get**

key为null，直接去数组0的位置找，key == null的节点，返回value。找不到返回null

key ！= null, 计算hash，计算index，找key相同的（条件同put），返回value，找不到返回null



**JDK8的变化**

数组+链表+红黑树

当链表长度大于8的时候，链表将被改造会红黑树，后面的数据也会插入到红黑树中，加快检索速度

put的变化，基本步骤没变化

* 计算hash， 计算index，如果数组该位置为null，直接插入
* 否则，判断数组**第一个**元素key是否相同，相同则更新，结束
* 判断第一个节点是不是树节点，如果是，执行红黑树的插入方法
* 如果不是，遍历链表更新，不更新插入。插入前链表长度阈值为8，小于8就在尾部插入。大于8就**可能**会被插入红黑树，或者扩容

插入节点后扩容；size > threshold

不重新计算hash和index，直接在新数组的j+oldSize处插入，j为在原数组的位置，比如原来数组长度16，index = 11.。现在直接吧11的node移动到27，整个链表也就移动过来了。



## GC相关

判定回收：

**引用计数法**

无法解决循环引用的问题，不使用

**可达性算法**

GC Roots如果可以通过引用链到达对象，证明此对象是可用的，不能回收。否则是不可用的，可以回收

可作为GC Root的

* 方法区中静态变量、常量引用的对象
* 活着的线程，包含处于等待或阻塞的线程
* 当前被调用的方法（Java方法、native方法）的一些参数/局部变量



**垃圾回收算法**

* 标记-清除

  1.标记需要被回收的对象

  2.回收被标记对象所占的空间

  容易产生内存碎片，碎片太多为大对象分配空间时无法找到一块足够大的空间而提前出发一次垃圾收集动作

  ![](https://user-gold-cdn.xitu.io/2019/12/13/16efe0b7a3b9bf63?imageslim)



* 复制算法

  为了解决标记-清除算法的缺陷。将可用内存分为大小相同的两块，每次只使用其中的一块。当这一块用完了就将还存活的对象复制到另一块，然后一次性全部清除这一块，这样就不容易出现内存碎片。代价是可用空间缩减为原来的一半了。

  ![](http://ww1.sinaimg.cn/large/006nwaiFgy1gicnc08dqmj30zs0lmafp.jpg)

* 标记-整理

  1.标记可回收对象

  2.整理，将存活对象都向其中一端整理。在清除内存。

  解决碎片的问题，引入了移动对象的成本

  ![](http://ww1.sinaimg.cn/large/006nwaiFgy1gicnic7t60j310a0nodl8.jpg)

* 分代回收（主流）

  ![](http://ww1.sinaimg.cn/large/006nwaiFgy1gicnr6fgz5j31201je16y.jpg)