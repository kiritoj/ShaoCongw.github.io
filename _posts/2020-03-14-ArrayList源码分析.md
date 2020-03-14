---
layout:     post
title:      ArrayList源码分析
subtitle:   
date:       2020-03-14
author:     taoke
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - java
    - 集合
---

# 概述

通过动态数组实现，支持随机访问，不是线程安全的。和它类似的vector是线程安全的，使用了同步关键字synchronized

**数组初始容量为10**

```java
private static final int DEFAULT_CAPACITY = 10;
```



# 构造方法

默认的构造方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gctsvlprhij30ma03t3yi.jpg)

elementData是一个Object数组，默认的构造方法给它设置为空数组



指定初始容量的构造方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gctsx9cp3bj317h0bdq3r.jpg)

如果初始容量大于0，则初始化elementdata数组，大小为传入的数值
等于0，设置elementdata数组为空数组

小于0，抛出异常



# add方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gctszgi2eej316g05qglu.jpg)



ensureCapacityInternal其实是一个检验并扩容的方法，传入的是**size+1**

当这个方法执行过以后，直接在数组当前size的位置插入元素e，并返回true，可见add方法一定能够添加成功的



追踪ensureCapacityInternal方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gctt8549o0j316d07paai.jpg)

首先判断了当前数组是否等于**DEFAULTCAPACITY_EMPTY_ELEMENTDATA**

而只有我们**调用无参构造方法**的时候elementdata才等于它

取最小容量为默认容量（10）和minCapacity（size+1）中的较大值

也就是说，当我们第一次调用add方法的时候，size = 0，size+1 = 1，默认容量为10.

较大值是10

所以通过无参构造初始化的数组容量还是10



然后调用**ensureExplicitCapacity(minCapacity)**方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcttfo6e4kj316m0810t3.jpg)

判断minCapacity的与当前数组容量的大小，minCapacity通常是size+1

意思就是加1后的容量大于数组的容量了，必须扩容

**grow方法**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcttig0gqij316m0bmwfl.jpg)

新的数组容量是旧数组容量的1.5倍。如果乘以1.5倍后比minCapacity还小，**我猜测是int越界了，**就直接设置新数组容量为minCapacity



然后直接copy原数组，新数组的长度是newCapacity



# **remove（index）**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gcttxldjl2j31380f4t9l.jpg)

计算需要移动的个数

然后将elementdata的index+1往后的数组，copy到index出，移动的个数为numMoved。就是把index后面的元素向前移动



# remove（Object）

![](http://ww1.sinaimg.cn/large/006nwaiFly1gctu0doxtuj317i0hamy3.jpg)

如果object是null，则遍历数组，找到第一个为null的元素，找到了就执行fastRemove方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gctu2c2dzyj316z08xt9b.jpg)

这个fastRemove方法还是靠copy实现的，和reomve（INdex）是一样的，



如果obj不是null

则从前向后找到第一个**equals方法返回true的，不是要地址相同**

然后又执行fastRemove方法