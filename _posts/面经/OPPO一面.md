# OPPO一面

## java引用和C++指针的区别？

指针和引用都是指向一块内存地址

* 指针本身是一个无符号长整型的数，而引用是封装后的类型。
* 引用的初始值是null，指针不是固定的（危险）
* 引用不可以计算，指针可以（比如指针加减访问数组的元素）
* 指针要手动delete

## C++引用和指针的区别



## java为啥比C/C++慢？

* 编译过程

  java首先要将源码(.java文件)编译成字节码（.class文件），再编译成机器码（计算机可以直接执行的二进制代码）

  C++的g++编译器会将源码直接编译为二进制代码

* java的对象都在堆中，栈里仅仅是对象引用，而C++局部变量的对象是直接在栈中的（不通过new关键字）

* 检查机制：java会在运行时做很多检查（运行时异常）以及GC都会影响效率

## 进程间通信（Bundle）

## ANR问题（响应时间）

**原因**：UI线程没有在规定时间内相应事件

Button点击事件（5s）

前台广播（10s）,后台广播（60s）不指名都是后台

服务：前台20s, 后台200s

内容提供器：10s

**实现原理**

在操作开始的时候，handler发送一个延时消息，在操作结束后remove掉message，如果操作没有在规定时间内完成，就会被取出来执行ANR

**避免ANR的注意事项**

* 避免在主线程从数据库中读取数据（主线程网络请求Android本身就不允许，NetWorkONMainThreadException）
* sharedpreference的commit是同步操作，apply是异步操作。并且写是全量的，因此尽量全部修改完之后再统一提交
* 不要在广播的onReceive中做耗时操作，以及Service的onCreate
* 避免主线程被锁

## 进程级别（Service被回收）

优先级一次递减

* 前台进程（ForeGround Process）

  Activity的onResume生命周期之后

  与活动绑定的服务

  **只有内存太低导致他们不能继续运行的时候才会杀掉他们**

  BroadcastReceiver的onReceive（） 

* 可视进程（Visible Process）

  比如一个Activity上弹出一个对话框。这个activity的进程级别是可视进程

  **可视进程一般也不会被回收，除非是为了保证前台进程的继续运行**

* 服务进程（Service Process）

  通过startService启动的进程，onStartConmmond（）只有，普通的音乐服务就会被杀掉

  **没有足够的内存保证前台进程和可视进程就会被回收**

* 后台进程（BackGround Process）

  不可见的活动（onStop，但没有onDestory的活动）
  
  **可以在任何时间回收内存供服务进程、可视进程、前台进程**
  
* 空进程（empty）

  没有任何内容进程，为了缓存的需要

## viewmodel为什么不会发生内存泄漏

## 设计模式大分类（创建型，结构型，行为型）

## wait方法没有在同步代码块里会抛什么异常？

IllegalMonitorStateException

