# 广播

定义一个类继承BroadcastReceiver，实现onReceive方法

**动态注册**

intentfilter（action），只有intent的adtion符合过滤器的，广播才接受

receiver

registerReceiver

**静态注册** 

在AndroidManifest.xml定义receiver标签，同样要指明intent的action

例子：监听开机自启，弹toast。

证明了广播是一种跨进程通信手段

**发送广播**

（标准）sendBroadcast（intent），intent指明action 

（有序）sendOrderBroadcast（intent）

**标准广播**

只要intent的action对得上，多个广播接收器都可以接受到，没有顺序可言，**异步的**，包括在不同的应用

**有序广播**

通过设置receiver的的优先级，xml OR 代码，达到谁先接受

先接受的可以截断广播：在onReceive中调用abortBroadcast，实际应用：垃圾短信拦截

**本地广播**

只能接受本应用发出的广播



**优先级顺序**

有序广播

* 先按优先级顺序接受
* 优先级相同：先动态注册接收，再静态注册的
* 都是动态/静态，按照注册的先后顺序

无序广播

* 先动态注册的，再静态注册的
* 注册方式一样，按优先级
* 优先级也一样，按注册顺序