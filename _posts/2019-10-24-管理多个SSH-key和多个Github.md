---
layout:     post
title:      管理多个SSH key和多个Github
subtitle:   在本地创建多个ssh key关联Github
date:       2019-10-24
author:     Taoke
header-img: img/post_19_10_24_ssh.png
catalog: true
tags:
    - SSH Key
    - Git
---

参考资料：[github设置添加SSH](https://blog.csdn.net/binyao02123202/article/details/20130891)，[多个github帐号的SSH key切换](http://ju.outofmemory.cn/entry/143690)   ,    [多个github帐号的SSH key切换](https://blog.csdn.net/itmyhome1990/article/details/42643233?utm_source=tuicool&utm_medium=referral)

## 前言：

我们都知道，要想push本地仓库的代码到Github需要将本地的**ssh key**的公钥添加到Github上，否则无权push。在本地只有一个ssh key情况下，我们通常先通过

```
git clone git@github.com:用户名/仓库名.git
```

克隆远程仓库

## https 和 SSH 的区别：

1、前者可以随意克隆github上的项目，而不管是谁的；而后者则是你必须是你要克隆的项目的拥有者或管理员，且需要先添加 SSH key ，否则无法克隆。

2、https url 在push的时候是需要验证用户名和密码的；而 SSH 在push的时候，是不需要输入用户名的，如果配置SSH key的时候设置了密码，则需要输入密码的，否则直接是不需要输入密码的。

然后更新本地仓库的代码，最后push回去

## 背景：

我最近需要将一些不方便让别人知道东西托管到github（不是开车😀，别瞎想），私有仓库又要money，我需要重新创建一个朋友们不知道的github账号（也就是当前博客所在的github）。github不允许同一个ssh公钥添加到不同的账号，那就需要在创建一个ssh key了ok，下面是正文，第一次认真的写东西，太过啰嗦还请见谅(*^_^*)

## 添加单一的ssh key

假定一切从零开始，我们本地没有ssh key

###  创建SSH

**cd到用户目录下git bash here** 输入

```shell
$ ssh-keygen -t rsa -C "youremail@example.com"
//-t 指定密钥类型，默认是 rsa ，可以省略。
//-C 设置注释文字，比如邮箱。
//-f 指定密钥文件存储文件名。默认是id_rsa
```

一路回车，完成后检查用户目录下是会出现**.ssh**文件夹以及**id_rsa**[私钥]和**id_rsa.pub**[公钥]。

![](http://ww1.sinaimg.cn/large/006nB4gFly1g89fyzu8aaj30uu0ah0ti.jpg)

### 添加到Github

用文本编辑器打开id_rsa.pub,建议不要用记事本，会出现意向不到的问题。复制内容，登录Github

![](http://ww1.sinaimg.cn/large/006nB4gFly1g89fdj9voaj30g30g23zl.jpg)

![](http://ww1.sinaimg.cn/mw690/006nB4gFly1g89fgkii5dj30yw0aowf6.jpg)

![](http://ww1.sinaimg.cn/large/006nB4gFly1g89fjbhnmoj30tp0esq3f.jpg)

ok，这样一个简单的ssh就添加好了

### 测试

```shell
$ ssh -T git@github.com
```

当你输入以上代码时，会有一段警告代码

```shell
The authenticity of host 'github.com (207.97.227.239)' can't be established.
# RSA key fingerprint is 16:27:ac:a5:76:28:2d:36:63:1b:56:4d:eb:df:a6:48.
# Are you sure you want to continue connecting (yes/no)?
```

直接yes通过，如果创建ssh的时候设置了密码，还要再输入一次密码

```
Hi username! You've successfully authenticated, but GitHub does not
```

测试成功！美滋滋😄🐒

## 添加多个SSH Key

照着上面的步骤，再创建一个ssh，注意名字不要重复了

在新的github账号上按同样的方法添加SSH Key

默认的SSH只会读取id_rsa,需要添加新增的ssh key

```shell
$ ssh-agent bash
$ ssh-add ~/.ssh/id_rsa_second
```

在.ssh目录下新建配置文件config

![](http://ww1.sinaimg.cn/large/006nB4gFly1g89gi8c3tjj30gt00qdfm.jpg)

写入

```
Host first.github.com  //随意,后面clone，push等会用到
HostName github.com
User git
IdentityFile ~/.ssh/id_rsa    

Host sencond.github.com //
HostName github.com
User git
IdentityFile ~/.ssh/id_rsa_sencond

```

到这里就ok了

可以按上面的方法测试一下

```shell
$ ssh -T git@senond.github.com //@后面跟Host
```

## 非默认账号需要做的改变

当连接非默认账号时，无论时**clone**，**remote**

```
@github.com:用户名/仓库名.git
```

需要更改为

```
@对应的Host:用户名/仓库名.git
```

这样当我们在Git中配置的时第一个用户，只要clone的时候使用另一个HOST就可以对另一个Github上的仓库进行操纵了

文末留下pljj的图片😀

![](http://ww1.sinaimg.cn/large/006nB4gFly1g8behnkz7nj31z4140dmp.jpg)
