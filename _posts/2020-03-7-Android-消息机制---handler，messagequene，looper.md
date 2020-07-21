---
ayout:     post
title:     Android 消息机制---handler，messagequene，looper
subtitle: 
date:       2020-03-6
author:     taoke
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:

- 面试
- android
---

# MessqgeQueue

MessqgeQuene主要负责消息的插入和读取，读取伴随着删除操作，分别对应enqueueMessage方法和next方法

先看下MessqgeQueue的实现

```java
public final class MessageQueue {
    private static final String TAG = "MessageQueue";
    private static final boolean DEBUG = false;

    // True if the message queue can be quit.
    private final boolean mQuitAllowed;

    @SuppressWarnings("unused")
    private long mPtr; // used by native code

    //链表的头结点
    Message mMessages;
```

里面有一个Message属性，查看Message类

```java
public final class Message implements Parcelable {
    	...
        Handler target;//目标，最终处理的是handler
        Message next;
```

发现有一个next属性。证实了**MessqgeQueue确实是通过单链表实现的**



## **enqueueMessage**

插入消息

```java
boolean enqueueMessage(Message msg, long when) {
    if (msg.target == null) {
        throw new IllegalArgumentException("Message must have a target.");
    }
    if (msg.isInUse()) {
        throw new IllegalStateException(msg + " This message is already in use.");
    }
```

Message的target（handler）不能为空，否则会抛出异常

参数when：发送的时间，**实际上是该消息执行的时间**

依据when属性从小到到的顺序排序单链表。when小的消息在前面，先被处理。

```java
synchronized (this) {
    if (mQuitting) {
        IllegalStateException e = new IllegalStateException(
                msg.target + " sending message to a Handler on a dead thread");
        Log.w(TAG, e.getMessage(), e);
        msg.recycle();
        return false;
    }

    msg.markInUse();
    msg.when = when;
    Message p = mMessages;
```

给要发送的msg的when属性赋值

mMessages是消息头结点

```java
if (p == null || when == 0 || when < p.when) {
    // New head, wake up the event queue if blocked.
    msg.next = p;
    mMessages = msg;
    needWake = mBlocked;
} 
```

如果当前头结点为null，那么msg就是第一个节点头结点。

如果msg的when属性值小于头结点的when或等于0，它将成为新的头结点

**只和头结点相比是因为，此时的链表已经是按when递增排序的**



否则的话，要在链表中找出第一个when比msg大的节点。并把msg插入到它前面。

如果一直没找到，就把msg插入到队尾了

```java
else {
    // Inserted within the middle of the queue.  Usually we don't have to wake
    // up the event queue unless there is a barrier at the head of the queue
    // and the message is the earliest asynchronous message in the queue.
    needWake = mBlocked && p.target == null && msg.isAsynchronous();
    Message prev;
    for (;;) {
        prev = p;
        p = p.next;
        if (p == null || when < p.when) {
            break;
        }
        if (needWake && p.isAsynchronous()) {
            needWake = false;
        }
    }
    msg.next = p; // invariant: p == prev.next
    prev.next = msg;
}
```

enQueueMessqge就分析到这里。返回**是否插入成功**

**总结，将插入的消息按照when从小到大的顺序。使整个消息队列when递增**



## next方法

```java
Message next() {
    // Return here if the message loop has already quit and been disposed.
    // This can happen if the application tries to restart a looper after quit
    // which is not supported.
    final long ptr = mPtr;
    if (ptr == 0) {
        return null;
    }

    int pendingIdleHandlerCount = -1; // -1 only during first iteration
    int nextPollTimeoutMillis = 0;
```



next方法会开启无线循环，**当有新消息来到的时候，就返回这条消息并将它从链表中删除**

```java
for (;;) {
    if (nextPollTimeoutMillis != 0) {
        Binder.flushPendingCommands();
    }

    nativePollOnce(ptr, nextPollTimeoutMillis);
```

```java
synchronized (this) {
    // Try to retrieve the next message.  Return if found.
    final long now = SystemClock.uptimeMillis();
    Message prevMsg = null;
    Message msg = mMessages;
    if (msg != null && msg.target == null) {
        // Stalled by a barrier.  Find the next asynchronous message in the queue.
        do {
            prevMsg = msg;
            msg = msg.next;
        } while (msg != null && !msg.isAsynchronous());
    }
```

**msg指向头结点**



```java

if (msg != null) {
    if (now < msg.when) {
        // Next message is not ready.  Set a timeout to wake up when it is ready.
        nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
    } else {
        // Got a message.
        mBlocked = false;
        if (prevMsg != null) {
            prevMsg.next = msg.next;
        } else {
            mMessages = msg.next;
        }
        msg.next = null;
        if (DEBUG) Log.v(TAG, "Returning message: " + msg);
        msg.markInUse();
        return msg;
    }
}
```

**如果头结点不为null，且它的target也不为null**

当当前时间now小于msg的执行时间**，now是现在到开机的时间差**，记录下时间的差值。(我也不知道记下来有什么屌用)

然后开始下一轮的循环

随着时间的推移，now会逼近when。当now >= when的时候。是时候处理这条消息了。

返回当前头结点msg，同时头结点向后移动一个mMessages = msg.next;最后还要删除从链表中这个消息msg.next = null;结束next方法

**总结：**

next方法会开启无线循环等待当前时间达到链表头结点的when。然后返回头结点并删除。



# Looper

## 构造方法

```java
private Looper(boolean quitAllowed) {
    mQueue = new MessageQueue(quitAllowed);
    mThread = Thread.currentThread();
}
```

可见Looper无法在外部实例化的

而handler必须要Looper才能工作，所以Looper提供了prepare方法

```java
public static void prepare() {
    prepare(true);
}
```

```java
private static void prepare(boolean quitAllowed) {
    if (sThreadLocal.get() != null) {
        throw new RuntimeException("Only one Looper may be created per thread");
    }
    sThreadLocal.set(new Looper(quitAllowed));
}
```

先看一下sThreadLocal

```java
static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>();
```

sThreadLocal的泛型是Looper。**hander是如何获取到对应线程的呢Looper**。这里我们就可以回答了

当一个线程第一次调用Looper.prepare,就会在自己的ThreadLocalMap中存储键值对。

下一次在以sThreadLocal为为key就可以取出Looper了。

**我们还看到，一个线程只能实例化一个Looper**，当第一次prepare的时候，存进了键值对，第二次取出的时候一定不是null；



## getMainLooper

```java
public static Looper getMainLooper() {
    synchronized (Looper.class) {
        return sMainLooper;
    }
}
```

可以在任意位置得到主线程的Looper

Looper默认会初始化主线程的Looper，同样第二次调用会报错

```java
public static void prepareMainLooper() {
    prepare(false);
    synchronized (Looper.class) {
        if (sMainLooper != null) {
            throw new IllegalStateException("The main Looper has already been prepared.");
        }
        sMainLooper = myLooper();
    }
}
```



## looper退出

looper是可以退出的，quit和quitsafely

```java
public void quit() {
    mQueue.quit(false);
}
```

```java
public void quitSafely() {
    mQueue.quit(true);
}
```

quit和quitsafely只是标志位不同，都是调用messqgeQueue的quit方法。

```java
void quit(boolean safe) {
    if (!mQuitAllowed) {
        throw new IllegalStateException("Main thread not allowed to quit.");
    }

    synchronized (this) {
        if (mQuitting) {
            return;
        }
        mQuitting = true;

        if (safe) {
            removeAllFutureMessagesLocked();
        } else {
            removeAllMessagesLocked();
        }

        // We can assume mPtr != 0 because mQuitting was previously false.
        nativeWake(mPtr);
    }
}
```

首先将退出quitting标志位设为true；

直接退出调用**removeAllMessagesLocked()**

```java
private void removeAllMessagesLocked() {
    Message p = mMessages;
    while (p != null) {
        Message n = p.next;
        p.recycleUnchecked();
        p = n;
    }
    //将整个链表删除
    mMessages = null;
}
```

对所有节点调用recycleUnchecked()方法，**同时将整个链表删除**

```java
void recycleUnchecked() {
    // Mark the message as in use while it remains in the recycled object pool.
    // Clear out all other details.
    flags = FLAG_IN_USE;
    what = 0;
    arg1 = 0;
    arg2 = 0;
    obj = null;
    replyTo = null;
    sendingUid = -1;
    when = 0;
    target = null;
    callback = null;
    data = null;

    synchronized (sPoolSync) {
        if (sPoolSize < MAX_POOL_SIZE) {
            next = sPool;
            sPool = this;
            sPoolSize++;
        }
    }
}
```



安全退出调用

**removeAllFutureMessagesLocked();**

```java
private void removeAllFutureMessagesLocked() {
    final long now = SystemClock.uptimeMillis();
    Message p = mMessages;
    if (p != null) {
        if (p.when > now) {
            removeAllMessagesLocked();
        } else {
            Message n;
            for (;;) {
                n = p.next;
                if (n == null) {
                    return;
                }
                if (n.when > now) {
                    break;
                }
                p = n;
            }
            p.next = null;
            do {
                p = n;
                n = p.next;
                p.recycleUnchecked();
            } while (n != null);
        }
    }
}
```

如果头结点的执行时间都比now大，那等同于直接quit

反之，从头结点向后遍历，找到第一个when大于当前时间now的节点。删除这个节点和以后的节点。并对这些节点调用recycleUnchecked();



**总结**

quit会清除消息队列中的所有消息，quitsafely只清除延迟消息

Looper退出后，通过handler发送的消息都会失败

```java
boolean enqueueMessage(Message msg, long when) {
    if (msg.target == null) {
        throw new IllegalArgumentException("Message must have a target.");
    }
    if (msg.isInUse()) {
        throw new IllegalStateException(msg + " This message is already in use.");
    }

    synchronized (this) {
        if (mQuitting) {
            IllegalStateException e = new IllegalStateException(
                    msg.target + " sending message to a Handler on a dead thread");
            Log.w(TAG, e.getMessage(), e);
            msg.recycle();
            //直接return false
            return false;
        }
```

在子线程中，当事情做完以后，应该显示地调用quit/quitsafely;否则这个looper会一直处于等待的状态



## Looper.loop(开启循环)

```java
public static void loop() {
    final Looper me = myLooper();
    if (me == null) {
        throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
    }
    final MessageQueue queue = me.mQueue;

    // Make sure the identity of this thread is that of the local process,
    // and keep track of what that identity token actually is.
    Binder.clearCallingIdentity();
    final long ident = Binder.clearCallingIdentity();

    // Allow overriding a threshold with a system prop. e.g.
    // adb shell 'setprop log.looper.1000.main.slow 1 && stop && start'
    final int thresholdOverride =
            SystemProperties.getInt("log.looper."
                    + Process.myUid() + "."
                    + Thread.currentThread().getName()
                    + ".slow", 0);

    boolean slowDeliveryDetected = false;
```



首先是要拿到**当前线程的looper和messqgeQuene**，myLooper方法，通过threadlocal拿到

```java
public static @Nullable Looper myLooper() {
    return sThreadLocal.get();
}
```

同时也看到了。如果没有looper会抛出异常



接下来开启无线循环，从messqgeQueue里取出消息

```java
for (;;) {
    Message msg = queue.next(); // might block
    if (msg == null) {
        // No message indicates that the message queue is quitting.
        return;
    }
```

一旦messageQueue的next方法返回null了，就中止looper方法

**那么next方法在什么情况下会返回null呢？**

```java
Message next() {
    // Return here if the message loop has already quit and been disposed.
    // This can happen if the application tries to restart a looper after quit
    // which is not supported.
    final long ptr = mPtr;
    if (ptr == 0) {
        return null;
    }
```

关键在于**Ptr**什么时候会等于0？

还有一处也会返回null

```java
if (mQuitting) {
    dispose();
    return null;
}
```

```java
private void dispose() {
    if (mPtr != 0) {
        nativeDestroy(mPtr);
        mPtr = 0;
    }
}
```

这下大致清楚了，next方法只有在looper调用quit/quitsafely的情况下，mQuitting= true

进而设置ptr = 0；looper方法中止

哪怕消息队列中暂时没有消息了，next也不会返回null。looper就不会结束



**拿到消息以后当然就是处理了**

```java
try {
    msg.target.dispatchMessage(msg);
    dispatchEnd = needEndTime ? SystemClock.uptimeMillis() : 0;
} finally {
    if (traceTag != 0) {
        Trace.traceEnd(traceTag);
    }
}
```

调用**msg.target.**dispatchMessage(msg);也就是**handler**的dispatchMessage方法

最后就将任务交回给handler处理。由于dispatchMessage方法是在创建handler时所使用的looper中执行的，这样就成功地将任务切换到指定线程中执行了



# handler

消息发送和处理

```java
public class Handler {
    
	public interface Callback {
        /**
         * @param msg A {@link android.os.Message Message} object
         * @return True if no further handling is desired
         */
        public boolean handleMessage(Message msg);
    }
    final Looper mLooper;
    final MessageQueue mQueue;
    final Callback mCallback;
```

handler中的主要属性，looper，MessageQueue，以及CallBack



## 构造方法

hander的构造方法众多，这里选取两个终点构造方法，其他构造方法都是中间调用

**不指定Looper的方式**

```java
public Handler(Callback callback, boolean async) {
    if (FIND_POTENTIAL_LEAKS) {
        final Class<? extends Handler> klass = getClass();
        if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass()) &&
                (klass.getModifiers() & Modifier.STATIC) == 0) {
            Log.w(TAG, "The following Handler class should be static or leaks might occur: " +
                klass.getCanonicalName());
        }
    }

    mLooper = Looper.myLooper();
    if (mLooper == null) {
        throw new RuntimeException(
            "Can't create handler inside thread " + Thread.currentThread()
                    + " that has not called Looper.prepare()");
    }
    mQueue = mLooper.mQueue;
    mCallback = callback;
    mAsynchronous = async;
}
```

Handler**会使用当前线程的looer**，messqgeQueue则是指向loope的messageQueue



**指定Looper的方式**

```java
public Handler(Looper looper, Callback callback, boolean async) {
    mLooper = looper;
    mQueue = looper.mQueue;
    mCallback = callback;
    mAsynchronous = async;
}
```

直接使用指定的looper

从这里我们可以发现。**最终任务在哪个线程，执行取决于looper而不是handler**



## **dispatchMessage**

分发消息处理。

在Looper的looper方法中，最终拿到消息调用了handler的dispatchMessage（msg）

```java
public void dispatchMessage(Message msg) {
    if (msg.callback != null) {
        handleCallback(msg);
    } else {
        if (mCallback != null) {
            if (mCallback.handleMessage(msg)) {
                return;
            }
        }
        handleMessage(msg);
    }
}
```

如果message自己有callBack，就调用 handleCallback(msg)方法

```java
private static void handleCallback(Message message) {
    message.callback.run();
}
```

很简单，直接运行run方法就可以了；

**msg的callback是一个Runnable**，**其实就是handler的post方法传递的**

```java
public final boolean post(Runnable r)
{
   return  sendMessageDelayed(getPostMessage(r), 0);
}
```

```java
private static Message getPostMessage(Runnable r) {
    Message m = Message.obtain();
    m.callback = r;
    return m;
}
```

post方法最终也是通过send方法实现的，原因就在这里；通过post方式发送的消息是没有延迟的



回到dispatchMessage方法

如果msg的callback为null，就看handler自己的callBack是否为null，不为null则调用mCallback.handleMessage(msg)。

如果handlr的callback也为null或者mCallback.handleMessage(msg)执行后返回false。最后才轮到halder的handleMessage方法处理



## send方法

```java
public final boolean sendMessage(Message msg)
{
    return sendMessageDelayed(msg, 0);
}
```

```java
public final boolean sendMessageDelayed(Message msg, long delayMillis)
{
    if (delayMillis < 0) {
        delayMillis = 0;
    }
    return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
}
```

处理时间等于开机时间加上延迟时间

```java
public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
    MessageQueue queue = mQueue;
    if (queue == null) {
        RuntimeException e = new RuntimeException(
                this + " sendMessageAtTime() called with no mQueue");
        Log.w("Looper", e.getMessage(), e);
        return false;
    }
    return enqueueMessage(queue, msg, uptimeMillis);
}
```

```java
private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
    msg.target = this;
    if (mAsynchronous) {
        msg.setAsynchronous(true);
    }
    return queue.enqueueMessage(msg, uptimeMillis);
}
```

最终调用了messageQueue的enqueueMessage方法，设置msg.target = 发送的hander

