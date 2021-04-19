---
layout:     post
title:     Android 消息机制---ThreadLocal
subtitle: 
date:       2020-03-6
author:     taoke
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:

- 面试
 - android
---



# Handler概述

handler可以将一个任务切换到指定的线程中执行，常常被用来更新UI。

常用做法是，在主线程创建handler实例。在子线程完成复杂操作后发送消息，位于主线程的handler收到消息更新UI

以上只是消息传递机制的一个最常见的例子。



# MessageQuene和Looper概述

Messqge和Looper都是Handler的属性，在其内部

```java
public class Handler {
    final Looper mLooper;
    final MessageQueue mQueue;
    final Callback mCallback;
    final boolean mAsynchronous;  
```

**MessageQuene：消息队列**

用单链表的形式实现消息队列，对外提供插入和删除功能。主要就是存储消息

**Looper：循环**

MessageQuene并不能处理消息，Looper填补这个功能。会以无限循环的方式去查找有无新消息。如果有就处理，没有就一直循环等待。一个线程中只能有一个Looper

**ThreadLocal**

可以在在不同的线程互不干扰地存储数据和提供数据



handle创建的时候**默认**会采用当前线程的Looper构造消息循环系统，**一个Looper对应一个MessageQuene**。handler正是通过ThreadLocal获取到当前线程的Looper。



可见handler的工作必须要有looper，而主线程被创建的时候会初始化Looper，所以主线程中默认可以使用looper；**其他线程中使用Handler必须先初始化looper**，调用**Looper.prepare();**



**Handler出现的原因**

解决无法在子线程访问UI的矛盾

ViewRootImpl有这么一段代码

```java
void checkThread() {
    if (mThread != Thread.currentThread()) {
        throw new CalledFromWrongThreadException(
                "Only the original thread that created a view hierarchy can touch its views.");
    }
}
```

mThread是UI线程，当访问UI的时候，就会运行该方法，如果不是在主线程访问UI就会抛出异常

因为Android中的UI空间不是线程安全的。如果**多线程并发访问**会使UI空间处于不可预期的状态。

那为什么不加锁呢？

- 加锁机制会让UI的访问变得复杂
- 降低UI访问的效率
- 阻塞了某些线程的执行



**Hander有两种方式发送消息**

handler.post(Runnable)

handler.send(Message)

post最终也是通过send方法完成的。每发送一条消息就会在MessqgeQuene中插入，等待Looper处理



# ThreadLocal

threadLocal可以在指定线程存储数据，当数据存储后。只有在当前线程运行再能取出这个数据。在其他线程是取不出这个数据的，只能取出它们自己存储的数据，没存那就是null;

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final ThreadLocal<Boolean> tl = new ThreadLocal<>();
    tl.set(true);
    Log.d("thread_local",tl.get()+"");

    new Thread("thread_1"){
        @Override
        public void run() {
            tl.set(false);
            Log.d("thread_local",tl.get()+"");
        }
    }.start();

    new Thread(){
        @Override
        public void run() {
            Log.d("thread_local",tl.get()+"");
        }
    }.start();


}
```

在主线程中threadlocal设值为true，在子线程1中设值为false。在子线程2中不设置。在三个线程中分别获取值

结果如下

main:true
thread_1：false
Thread-2：null

和预期的结果相符



## ThreadLocal源码分析

ThreadLocalMap作为ThreadLocal的重要组成部分，是ThreadLocal里面的一个静态内部类

```java
static class ThreadLocalMap {

    /**
     * The entries in this hash map extend WeakReference, using
     * its main ref field as the key (which is always a
     * ThreadLocal object).  Note that null keys (i.e. entry.get()
     * == null) mean that the key is no longer referenced, so the
     * entry can be expunged from table.  Such entries are referred to
     * as "stale entries" in the code that follows.
```

既然是Map，那么一定有一个Entry吧，在ThreadLocal中又有一个静态内部类Entry如下

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
    /** The value associated with this ThreadLocal. */
    Object value;

    Entry(ThreadLocal<?> k, Object v) {
        super(k);
        value = v;
    }
}
```

继承与泛型为ThreadLocal<?>的弱引用，Key为ThreadLocal，至于为什么后面就能知道了。ThreadLocal本身也是泛型类



然而，ThreadLocal没有持有ThreadLocalMap的引用。反而是**Thread类持有ThreadLocalMap的引用**

```java
class Thread implements Runnable {
	.....
	 ThreadLocal.ThreadLocalMap threadLocals = null;
```

现在可以大概猜测实现原理了。

每个线程在ThreadLocal里面塞东西的时候，其实是向该自己持有的ThreadLocalMap存东西，以ThreadLocal为键，value为值。

**以上面的演示代码为例**

主线程设置threadlocal的值为true，实际上是在主线程的ThreadLocalMap存入了一个Entry（threadlocal，map）。取的时候也是从自己的ThreadLocalMap里面取，根据threadLocal作为值取出value。

一个线程可以有多个threadLocal，每一个都作为一个key。和该threadLocal创建在哪个线程是无关的。



### **set方法**

```java
public void set(T value) {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}
```

首先获取当前线程的ThreadLocalMap，看一下getMap的实现

```java
ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}
```

嗯，直接返回的是该线程的ThreadLocalMap

接**下来就是向map中put数据了**

如果map不为空直接插入，可以看到，是以当前的threadLocal为key插入到map中的。put方法的实现待会再看

如果map是null，则初始化并插入

```java
void createMap(Thread t, T firstValue) {
    t.threadLocals = new ThreadLocalMap(this, firstValue);
}
```



### ThreadLocalMap源码分析

各个成员变量

初始容量16，和hashMap一样

```java
private static final int INITIAL_CAPACITY = 16;
```

```java
private Entry[] table;
```

```java
private int size = 0;
```

```java
private int threshold;
```

设置阈值

```java
private void setThreshold(int len) {
    threshold = len * 2 / 3;
}
```

和hashmap不同的是，threadLocalMap并没有扩容因子，阈值是自己设置的，2/3



Entry继承自弱引用 WeakReference<ThreadLocal<?>>可以使ThreadLocal的对象的生命周期和Thread的生命周期解绑。换言之就是，当ThreadLocal没有强引用的时候（也就是没用了的时候）可以被GC回收，**避免线程得不到销毁的时候ThreadLocal对象无法被回收**



#### **构造函数**

```java
ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
    table = new Entry[INITIAL_CAPACITY];
    int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
    table[i] = new Entry(firstKey, firstValue);
    size = 1;
    setThreshold(INITIAL_CAPACITY);
}
```

可以看到，定位在哪个数组的哪个Index仍然和hashMap一样，**采用hashcode与运算数组长度减1**



#### **set函数**

```java
private void set(ThreadLocal<?> key, Object value) {

    // We don't use a fast path as with get() because it is at
    // least as common to use set() to create new entries as
    // it is to replace existing ones, in which case, a fast
    // path would fail more often than not.

    Entry[] tab = table;
    int len = tab.length;
    int i = key.threadLocalHashCode & (len-1);
```

仍然是先计算要在数组插入的下标i

```java
for (Entry e = tab[i];
     e != null;
     e = tab[i = nextIndex(i, len)]) {
    ThreadLocal<?> k = e.get();

    if (k == key) {
        e.value = value;
        return;
    }
    //k为什么会等于null？
    //由于Entry继承的弱引用，说明threadlocal被回收了。
    //那么该位置可以重新利用了。
    if (k == null) {
        replaceStaleEntry(key, value, i);
        //关于replae的详细过程：https://www.jianshu.com/p/80866ca6c424
        return;
    }
}
```

for循环的作用

回想hashmap中for循环的作用：更新key相同的value值，这里也是一样的。

但是这里的for循环还有一个作用：**解决hash冲突**

hashmap是用数组+链表实现的，如果出现hash冲突则把Entry插入到链表的头部。

**ThreadLocalMap没有使用链表**，**是用纯数组实现的**。当tabel【i】的位置已经有元素了，且key值也不相等。那就使用**线性探测法再哈希**，看nextIndex方法

```java
private static int nextIndex(int i, int len) {
    return ((i + 1 < len) ? i + 1 : 0);
}
```



for循环后面

```java
tab[i] = new Entry(key, value);
    int sz = ++size;
    if (!cleanSomeSlots(i, sz) && sz >= threshold)
        rehash();
}
```

找到一个为还没有元素的位置，就在table[i]的位置插入了，更新长度；

**cleanSomeSlots**的作用是将那些key已经被回收的Entry（table【index】）置为null，腾出位置给后来的使用，如果没有清除任何一个Entry返回false，且map长度已经到达阈值，就要扩容了。

```java
private boolean cleanSomeSlots(int i, int n) {
    boolean removed = false;
    Entry[] tab = table;
    int len = tab.length;
    do {
        i = nextIndex(i, len);
        Entry e = tab[i];
        if (e != null && e.get() == null) {
            n = len;
            removed = true;
            i = expungeStaleEntry(i);
            // expungeStaleEntry方法，从i开始，向后扫描一段连续的Entry，即中间没有null，清空key == null的Entry，key ！= null的Entry要重新计算下标值。然后返回下一个table【i】 == null 的i。继续while循环
        }
    } while ( (n >>>= 1) != 0);
    return removed;
}
```



#### **rehash扩容**

```java
private void rehash() {
    expungeStaleEntries();

    // Use lower threshold for doubling to avoid hysteresis
    if (size >= threshold - threshold / 4)
        resize();
}
```

仍然先要清理key被回收的Entry。为什么要在清理一次呢。

我们注意到，**在cleanSomeSlots方法中只清除了i后面的，前面并没有清除**

因为又做了一次全清理，所以map的size变小了，**超过阈值的3/4就扩容**，如果清理过后

没有超过阈值的3/4，就不用扩容了



**resize扩容**

```java
private void resize() {
    Entry[] oldTab = table;
    int oldLen = oldTab.length;
    //扩容为原来的2倍，和hashmap一样。ArrayList是扩容为原来的1.5倍
    int newLen = oldLen * 2;
    Entry[] newTab = new Entry[newLen];
    int count = 0;

    for (int j = 0; j < oldLen; ++j) {
        Entry e = oldTab[j];
        if (e != null) {
            ThreadLocal<?> k = e.get();
            //key == null 的继续清理
            if (k == null) {
                e.value = null; // Help the GC
            } else {
                //key不等于null的重新计算下标值
                int h = k.threadLocalHashCode & (newLen - 1);
                //仍然使用线性探测解决hash冲突
                while (newTab[h] != null)
                    h = nextIndex(h, newLen);
                newTab[h] = e;
                count++;
            }
        }
    }

    setThreshold(newLen);
    size = count;
    table = newTab;
}
```



#### getEntry方法

```java
private Entry getEntry(ThreadLocal<?> key) {
    int i = key.threadLocalHashCode & (table.length - 1);
    Entry e = table[i];
    if (e != null && e.get() == key)
        return e;
    else
        return getEntryAfterMiss(key, i, e);
}
```

首先根据key值算出index。如果该位置不为null且key值相等

直接返回Entry；

else分之有

否则调用getEntryAfterMiss(key, i, e);从第i个位置向后找key相等的值，找不到就返回null

```java
private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
    Entry[] tab = table;
    int len = tab.length;

    while (e != null) {
        ThreadLocal<?> k = e.get();
        if (k == key)
            return e;
        if (k == null)
            expungeStaleEntry(i);
        else
            i = nextIndex(i, len);
        e = tab[i];
    }
    return null;
}
```



### ThreadLocalget方法

```java
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}
```

理解了ThreadLocalMap在来看ThreadLocal就简单许多了

