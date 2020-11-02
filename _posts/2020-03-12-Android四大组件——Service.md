---
layout:     post
title:      Android四大组件——Service
subtitle:   总结服务的用法及两种启动的方式区别
date:       2020-03-12
author:     taoke
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - 四大组件
---

# 生命周期

![](https://picb.zhimg.com/v2-4bccc6d2b4b2459a2b386e6e7f417aab_r.jpg)

可以看到，两种服务的启动方式，生命周期不一样：

startService：

onCreat（只执行一次），onStartConmoned，onDestory

bingService：

onCreate->onBind ->onUnbind ->onDestory

onUnBind要等到所有绑定者解除绑定才会执行

# startService启动服务

首先定义一个最简单的服务

```java
public class MyService extends Service {
    public MyService() {
    }
  
    @Override
    public void onCreate() {
        Log.d("xiaojin","onCreat");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("xiaojin","onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("xiaojin","onDestory");
    }
    
}
```



在**SecondActivity**中启动它

开启服务

```java
btService.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        startService(new Intent(SecondActivity.this,MyService.class));
       
    }
});
```

关闭服务

```java
btStop.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      
        stopService(new Intent(SecondActivity.this,MyService.class));
    }
});
```



查看日志

**第一次**点击开启服务

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcsbvfxma4j310c03ddiz.jpg)

关闭服务

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcsbwiaj6dj30w60173zy.jpg)



**多次开启服务再关闭**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcsby5ox2lj30zp05pgs1.jpg)

多次开启服务，由于第一次服务已经创建，所以不会重复创建，只会执行onStartCommand方法，由于**每个服务只有一个实例**，所以不管调用了多少次startService，**只需要一次stopServiece就可以停止服务**



**结论：通过startService启动的服务，启动后服务和调用者就没什么关系了。就算调用者死亡，服务还是会继续运行**

**且只要服务还没有结束，就不会执行onCreat创建服务**



**证明：首先在SecondActivity中通过start方式开启服务，然后返回到MainActivity，此时虽然SecondActivity死亡了，但是服务还在后台运行。**

**再次进入SecondActivity，点击开启服务，会发现只有onstartCommand执行了**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcsceogpjsg30a50kkn25.gif)

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcsc7easfyj311a04pwjb.jpg)

从结果中可以看到，开启服务后，退出SecondActivity的时候，Service的onDestory并没有执行

**start方式的缺点也显而易见，调用者无法与服务进行通信**

# bindService启动服务

首先改变一下Service，代码如下

```java
public class MyService extends Service {
    public MyService() {
    }
    class MyBinder extends Binder {

    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("xiaojin","onBind");
       return new MyBinder();
    }

    @Override
    public void onCreate() {
        Log.d("xiaojin","onCreat");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("xiaojin","onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("xiaojin","onDestory");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("xiaojin","onUnBind");
        return super.onUnbind(intent);
    }
}
```

创建一个MyBinder类继承Binder类，并在onBind方法中返回它，其余不变

SecondActivity也要改变一下

定义一个ServiceConnecttion对象，用于调用者与服务连接

```java
private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MyService.MyBinder binder = (MyService.MyBinder)service;
        Log.d("xiaojin","服务与SecondActivity连接成功");
        Log.d("xiaojin",binder.toString());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("xiaojin","服务与SecondActivity连接断开");
    }
};
```

在onServiceConnected方法中，与服务连接建立成功的时候吧service强转为MyBinder对象，打印出它的地址值



**建立连接**

```java
btService.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
       
       bindService(new Intent(SecondActivity.this,MyService.class),serviceConnection,BIND_AUTO_CREATE);
    }
});
```

**取消连接**

```java
btStop.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        unbindService(serviceConnection);

    }
});
```



**先点击连接，在取消连接**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcscpniymrj31az073dpo.jpg)

可以看到先后执行了onCreat，onBind，onUnBind，onDestory方法



**先点击连接，然后退出SecondActivity**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcscrw9a7fj31av06eaja.jpg)

SecondActivity退出，服务就结束了。这一点和start方式是完全不同的



**连续多次点击连接**	

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcsctg8l6mj31ak043gry.jpg)

除了第一次有反应，后面的都没有反应



**先在MainActivity连接，再去SecondActivity连接**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcscvqyejmj31ee06j7e9.jpg)

由于在MainActivity已经创建了服务了，所以在SecondActivity中不会再次创建，service的实例之中只有一个。**更重要的是，两次打印的binder的地址是一致的，也就是说是同一个binder。**

**这时候如果SecondActivity如果退出了，服务会退出吗？**

答案是不会的，因为service还和MainActivity绑定了。除非MainActivity也退出服务才会退出。



# 同时使用

**这种情况下一般用于想要一个服务长时间存在又要和其保持通信**

这时候想要退出必须满足

- 该服务的所有调用者都解除连接

- stopService



# 前台服务

如果想要尽可能保证服务不被杀掉，使用前台服务，需要权限

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

前台服务需要用到通知**Notification**

但是自Android 8.0开始，Google引入了通知渠道

**所谓渠道就是将通知进行分类，比如有好友消息通知，推荐通知等。是用户能够自由选择接受哪些类的通知**

渠道一旦设置就不能更改了！！！

首先需要创建一个渠道，需要**渠道id，渠道名，重要性**

```java
//创建渠道
@TargetApi(Build.VERSION_CODES.O)
private void ceartNotificationChannel(String id, String name, int importance){
    NotificationChannel channel = new NotificationChannel(id,name,importance);
    
    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    
    manager.createNotificationChannel(channel);
}
```



notification的实例化也要比之前**多传入一个渠道id**，以区别是哪个渠道的id

```java
notification = new Notification.Builder(this,"chat")
        .setContentTitle("我是标题")
        .setContentText("我是内容")
        .setWhen(System.currentTimeMillis())
        .setSmallIcon(R.mipmap.ic_launcher)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
        .setContentIntent(pi)
        .build();
```

**发送方式不变**

manger.notify(int i, notification)



**适配Android8.0的前台服务**

```java
@Override
public void onCreate() {
    Log.d("xiaojin","onCreat");
    Intent intent = new Intent(this,MainActivity.class);
    PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
    Notification notification = null;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        creatNotificationChannel("chat","聊天信息",NotificationManager.IMPORTANCE_HIGH);
        notification = new Notification.Builder(this,"chat")
                .setContentTitle("我是标题")
                .setContentText("我是内容")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
    }else{
        notification = new Notification.Builder(this)
                .setContentTitle("我是标题")
                .setContentText("我是内容")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                .setContentIntent(pi)
                .build();
    }
    startForeground(1,notification);
}
```



## 自定义前台服务的样式

需要用到**RemoteView**

这里以云音的前台服务为例

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcseue57lzj309d02uglh.jpg)

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcseweeg3rj31a20fztxq.jpg)

用写好的布局文件实例化remoteview

如果需要点击整个通知跳转，和notification一样，需要intent和pendingIntent。然后在notification中设置ContentIntent（pi）

我们自定义的布局中有3个按钮，分别是播放上一曲，下一曲和暂停

定义三个Intent（String action），是用广播实现的

```java
class PlayControlReceiver: BroadcastReceiver() {
        companion object {
            val ACTION1 = "PLAY_PREV"//播放上一曲
            val ACTION2 = "PLAY_NEXT"//播放下一曲
            val ACTION3 = "PLAY_OR_PAUSE"//继续播放或暂停
        }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("testbro", "收到广播")

        when (intent?.action) {
                ACTION1 -> PlayManger.playPreVious()
                ACTION2 -> PlayManger.playNext()
                ACTION3 -> {
                    if (PlayManger.player.isPlaying) {
                        PlayManger.pause()
                    } else {
                        PlayManger.resume()
                    }
                }
            }

    }
}
```

然后定义3个pendingIntent

最后给remoteview设置点击事件

将remoteview添加到notification中

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcsfoq7i1hj31d70jc1kx.jpg)



服务的两种启动方式的区别：

先说生命周期

**srart**方式：

通过stopservice关闭服务

一旦服务开启，服务就跟开启者没有关系了。

开启者退出了服务也不会跟着退出

开启者和服务不能通信



**bind**方式

通过unbindservice方式关闭服务

服务的存活和绑定者有关，一但绑定者退出，服务就结束。

如果有多个绑定者，需要全部解除绑定或者退出

绑定者可以和服务通信





