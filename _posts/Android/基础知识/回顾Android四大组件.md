#### 回顾Android四大组件

##### Activity

**生命周期**

验证了两种特殊情况的下的生命周期的变化情况

* 屏幕旋转

  前提：当前activity在栈顶，旋转屏幕

  onResume-->onPause-->onStop-->onDestory--->onCreate--->onStart--->onResume

  旋转屏幕会销毁activity并重建。这里就涉及到旋转屏幕数据的保存

  ```
  onSaveInstanceState //将在onDestory结束前保存
  onRestoreInstanceState //在onResume之前恢复数据
  ```

* 熄屏及亮屏

  熄屏生命周期：

  onResume-->onPause-->onStop

  紧接着亮屏

  onRestart-->onStart-->onResume

**启动模式**

standard：标准模式，没有什么好说的

singleTop：栈顶复用，适用于启动一个栈顶活动，不希望在创建一个；比如新闻详情页在     栈顶，推送一条消息，同样打开详情页activity。这时候就适用于该模式，回调onNewIntent刷新视图

singleTask：返回栈复用，清除目标activity之上的其他activity。适用于某些页面需要提供给第三方app打开，这时候点击返回，可以更少次数地回到调用app，提升用户体验。

将该属性指定一个不同于本应用的包名，会重新开启一个返回栈

```xml
android:taskAffinity = "xxx"
```

singleInstance：全局唯一性，一个新的返回栈。适用于系统来电显示这种活动



**scheme协议跳转**

指定目标activity的scheme，host，path进行跳转。方便从H5页面跳转到native页面。以及路由的实现



##### 广播接收器

这部分内容比较熟悉，但也发现了以前理解错误的地方以及新的部分

以前理解错误的地方：Android 8.0以上不是不能静态注册了，是可以静态注册的，但是要指明intent的packaName，即广播的接收应用

新的部分：

黏性广播，即使接收器暂时没有注册或被销毁掉了，会保留最新的消息。待条件满足，依然可以接收到广播，和普通的广播使用方式，只是发送方式改变了

##### 内容提供器

自己写了两个demo，一个demo提供自己的内容提供器，实现增删改查功能。另一个程序读取。提供方主要通过UriMatcher去匹配ContentResolver传入的Uri，判断调用方想要的数据

##### Service

这部分主要回顾了两种启动方式的区别

startService：service启动后和调用方无关，也不能完成交互

bindService：只要没有绑定者了，sevice就会销毁，由Binder提供调用方与Service的交互

两种方式一起启动的服务要销毁需满足：所有的绑定者取消绑定，stopservice



#### 动画

##### 视图动画

帧动画

逐帧显示图片形成动画，适用于图片较小，动画效果不要好实现的情况下。否则容易OOM

View动画

只能实现scala，alpha，rotate，translate四种动画，不能满足复杂的需求。动画的执行只是视觉上的，只能作用于view，view的真实位置和大小都没有改变

##### 属性动画

不直接作用于view，提供的是数据的变化，添加监听器，实时获取当前数值，可以去真实地改变view的属性。

以上的动画都实现了一个demo

关于interpolator和evaluator这两部分明天细看