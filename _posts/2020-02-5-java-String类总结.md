---
layout:     post
title:      java String类总结
subtitle:   
date:       2020-02-5
author:     taoke
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - java
    - string
    - 字符串
---

栈（stack）：主要保存**基本类型**（或者叫内置类型）（char、byte、short、int、long、float、double、boolean）和**对象的引用**，数据可以共享，速度仅次于寄存器（register），快于堆。 

堆（heap）：用于存储**对象**

# String概览



**java_8**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gblmg2bhpyj30rl07saak.jpg)

String类**不可继承，且不可变**

其内部由**char数组**存储字符串，因为被final修饰，value不能再引用其他数组，且String类内部也没有改变value数组值的方法，因而String是不可变的

String类计算hashCode的方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gblmliz7aej30mv0cjq3f.jpg)

value数组不可变，所以哈希值也是不可变的

**java_9之后**

改用**byte数组**存储字符串，用**coder**标识使用了那种编码

```java
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
  
    private final byte[] value;

    private final byte coder;
}
```

# 不可变的好处

**1. 可以缓存 hash 值**

因为 String 的 hash 值经常被使用，例如 String 用做 HashMap 的 key。不可变的特性可以使得 hash 值也不可变，因此只需要进行一次计算。

**2. String Pool 的需要**

如果一个 String 对象已经被创建过了，那么就会从 String Pool 中取得引用。只有 String 是不可变的，才可能使用 String Pool。

**3. 安全性**

String 经常作为参数，String 不可变性可以保证参数不可变。例如在作为网络连接参数的情况下如果 String 是可变的，那么在网络连接过程中，String 被改变，改变 String 的那一方以为现在连接的是其它主机，而实际情况却不一定是。

**4. 线程安全**

String 不可变性天生具备线程安全，同一个字符串实例可以被多个线程共享，可以在多个线程中安全地使用。

# String, StringBuffer and StringBuilder

**1. 可变性**

- String 不可变
- StringBuffer 和 StringBuilder 可变

**2. 线程安全**

- String 不可变，因此是线程安全的
- StringBuilder 不是线程安全的，效率高
- StringBuffer 是线程安全的，内部使用 synchronized 进行同步

# 字符串常量池

JVM会专门维护一片内存空间，叫做字符串常量池，用于存储String对象的**引用**

## String对象的两种创建方式

- 字面量赋值

```java
String  s1  = “abc”;
```

- new关键字

```java
String s2 = new String("abc");
```

## 经典题型：创建了几个对象

假如前面**没有进行任何字符串操作**

```java
String s1 = "abc" //方式1
```

答案：**创建了1个对象**

首先去String pool查找是否存在**某个引用的所指向的对象**的值等于abc（eaual）

如果存在，直接返回该引用给s1

如果不存在，在堆内存中创建一个值为abc的String 对象，将它的引用存储在String pool，再返回引用给s1

```java
String s2 = new String("abc");//方式2
```

答案：**创建了2个对象**

先看看String的构造方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gblnsk4rkdj30kp047t8u.jpg)

前面的过程和方式一一样，将值为abc的string对象的引用作为构造函数的参数，**一定会**再次在堆内存中创建了一个新对象，所以一共是两个对象。最终**s2指向的是后创建的string对象，不是存储在string pool中的引用**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gblo1r9kswj30i303p3ym.jpg)



当然，如果string pool中本来就已经存在值相同的对象引用了，就少创建一个对象

![](http://ww1.sinaimg.cn/large/006nwaiFly1gblo61ybjxj30ni063mxt.jpg)

## 其他情况

![](http://ww1.sinaimg.cn/large/006nwaiFly1gblokn96x1j30la07ndgh.jpg)

当**加号两边都是字符串常量值**的时候，如s2，编译时会被优化为s2 = “hello”,而s1创建以后，string pool中已经有一个hello引用了，所以直接返回该引用。如图s5创建了一个值为“hello”的对象，与常量池中的不同。

**而对于所有包含new方式新建对象（包括null）或变量的“+”连接表达式，会在堆内存中新创建一个对象，且新对象的引用都不会被加入字符串池中**



**更复杂的**

```java
String s1 = new String("a") + new String("b")
```

一共是5个

new String("a")创建2个

new String("b")创建2个

由上一条结论：“+”两端不是字符串常量，会在创建一个对象，引用不会存储在string pool中



## **String.intern方法**

intern方法的作用是手动将对对象的引用添加到string pool。

如果string pool中已经存在具有相同值的对象的引用，则直接返回该引用，反之，添加引用到pool中

![](http://ww1.sinaimg.cn/large/006nwaiFly1gblpo1jre4j30l7070mxq.jpg)

s1 = "abc",所以string pool中会有一个值为“abc”的引用，s1.intern()直接返回该引用

s2.intern()返回的同样是这个引用，故相等。

再看另外一个例子

```java
       String s1 = "1";
       String s2 = "1" + s1;
       //s2.intern();
       String s3 = "11";
       System.out.println(s2 == s3);
```

s2创建以后，string pool中并没有“11”这个对象引用。s2指向堆内存中的对象

如果此时直接创建s3，会发现pool中没有，按照规则，会创建一个对象并将其引用存储到pool中，s2和s3地址就不同，**False**

如果去掉注释，s2创建完成后调用intern方法，将“11”对象的引用添加进pool，s3创建是就会发现pool已经存在“11”的引用，直接返回该引用，所以s2和s3地址相同，**True**

