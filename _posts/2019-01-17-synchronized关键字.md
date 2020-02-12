---
layout:     post
title:      synchronized关键字
subtitle:   java同步关键字synchronized用法总结
date:       2019-01-17
author:     Taoke
header-img: img/post_20_1_17_1.jpg
catalog: true
tags:
    - java
    - 并发
---

synchronized关键字作用于方法和代码块，对应为**同步方法**和**同步代码块**，其中同步方法只需在方法中加入关键字，同步代码块需要设置具体给哪个对象加锁

**同步方法例**：

```java
public synchronized void funcName(){
	do something
}
```

**同步代码块**

```java
synchronized(Object object){	
	do something;
}
```

**6种用法**

- 同步代码块

  - 加锁对象为本地变量
  - 类静态变量
  - 共享变量
  - 类对象

- 同步方法

  - 修饰普通方法
  - 修饰类静态方法

  #

# 同步代码块

## 加锁本地变量

测试类

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazlwnqxa9j30sf0cbdfy.jpg)

测试方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazlyuipclj30nf099wf5.jpg)

结果

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazm0keqbuj30h804pwet.jpg)

可知，在多线程环境下，**单个对象**的测试结果正确。

**测试多个对象**，将测试方法改为如下

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazm7un0s8j30qo0fx0tp.jpg)

结果

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazm9s7piuj30i7044t91.jpg)

可知，当同时有两个对象，只对测试类的本地变量加锁，无法保证同步

## 加锁类静态变量

更改测试类

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazmg6e4ebj30sj0aggm5.jpg)

测试结果

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazmhjqgz6j30f5027q2x.jpg)

多线程，多个变量调用也能保证同步性

## 加锁共享变量

修改测试类

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazmp3bnxcj30se0c7gmd.jpg)

测试结果

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazmhjqgz6j30f5027q2x.jpg)

**多线程多对象调用**可以保证同步性

## 加锁类对象

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazmwjo806j30rh09d0t6.jpg)

**多线程多对象调用最后结果仍然正确**



**最后，同步代码块的方式加锁，只同步了加锁的这部分代码，其他地方同步性无法得到保证**

测试类

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazn48ka0rj30rn0ggdgx.jpg)

测试代码

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazn58gi1hj30ng0dmgme.jpg)

结果

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazn6odwfwj30gw03aq32.jpg)

结果错误

修正测试类

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazz15syvij30rd0glwfl.jpg)

运行结果正确

# 同步方法

## 普通同步方法

![](C:\Users\MIFANS\AppData\Roaming\Typora\typora-user-images\1579274859472.png)

![](http://ww1.sinaimg.cn/large/006nwaiFly1gazn58gi1hj30ng0dmgme.jpg)

多线程多个对象调用，运行结果有误

## 类静态同步方法

将上图synchronized修饰的方法加上static关键字，再次运行，运行结果正确

**注：和同步代码块一样，如果有几个方法都需要同步，则都需要加上synchronized关键字。**

无论是同步方法还是同步代码块，都看成是检查锁，加锁，释放锁的过程，便于理解。

**同步类静态方法等效于同步代码块中的加锁类对象**