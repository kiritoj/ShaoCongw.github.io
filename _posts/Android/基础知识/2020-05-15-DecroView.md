---
layout:     post
title:      DecroView
subtitle:   setContentView做了什么？
date:       2020-05-15
author:     taoke
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - 蓝桥杯
---

### activity.setContentView

```java
 public void setContentView(@LayoutRes int layoutResID) {
      getWindow().setContentView(layoutResID);
      initWindowDecorActionBar();
  }
```



#getWindow

```java
 mWindow = new PhoneWindow(this, window);

 public Window getWindow() {
    return mWindow;
 }
```



#phoneWindow.setContentView

![](http://ww1.sinaimg.cn/large/006nB4gFgy1geszorm0s7j30kq0lp0u8.jpg)



首先初始化mContentParent以及移除它的view

初始化mContentView

```java
mContentParent = generateLayout(mDecor); 
```

#generateLayout(mDecor)

![](http://ww1.sinaimg.cn/large/006nB4gFgy1gesyycwik2j30ks02v74d.jpg)

加载R.layout.simple，并加入到DecorView

#R.layout.simple

![](http://ww1.sinaimg.cn/large/006nB4gFgy1gesz0t4ygyj30kp09j0tl.jpg)



mContentParent就是一个FrameLyout，上面的标题栏

初始化mContentParent后的操作，将我们传入的布局加入到mContentParent中

总结：DecorView包含一个LinearLayout，上面一部分是标题栏，下边是内容区域（android.R.id.content）,我们在activity中的布局加入到content中