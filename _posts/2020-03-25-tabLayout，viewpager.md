---
layout:     post
title:      tabLayout，viewpager
subtitle:   开发中遇到的问题
date:       2020-03-25
author:     taoke
header-img: img/post-bg-re-vs-ng2.jpg
catalog: true
tags:
    - 开发
---

迁移到androidx以后，tablayout不再是原来的 android.support.design.widget.TabLayout，变为**com.google.android.material.tabs.TabLayout**

使用方法和之前一样，不过新增另一种方式添加tabItem，即直接写在xml文件中

```xml
<com.google.android.material.tabs.TabLayout
   >

    <com.google.android.material.tabs.TabItem
    	android：icon = "@drawable/xxxxx"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="快捷"
        />

    <com.google.android.material.tabs.TabItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="好友" />
</com.google.android.material.tabs.TabLayout>
```

利用这种方式可以轻松地实现**icon+文字**的tab选项

当然也可以手动addTab



如果不想要选中tabitem时的阴影扩散效果可以在xml添加：

```xml
app:tabRippleColor="@android:color/transparent"
```



取消下标占满item

```xml
app:tabIndicatorFullWidth="false"
```



**本文主要是记录tablayout和viewpager联动即setupWithViewPager出现tab的内容消失的情况**

首先看setupWithViewPager方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gd68ewfg32j310203idje.jpg)

调用了重载的一个方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gd68frtiabj30zy033gon.jpg)

又调用了重载的方法...

在这个方法里有这样一段代码

![](http://ww1.sinaimg.cn/large/006nwaiFly1gd68hte86nj31030e7nen.jpg)



继续点进去，**重点来了**

![](http://ww1.sinaimg.cn/large/006nwaiFly1gd68jiqgooj30zx0gfh54.jpg)

进入箭头指向的方法

![](http://ww1.sinaimg.cn/large/006nwaiFly1gd68kihsrbj310e0fj7mf.jpg)



答案出现，**setupWithViewPager最终把tabLayout的所有tab清空了**，然后重新添加了adapterCount个tab，这个adapterCount是什么呢，**调用的PagerAdapter的getCount方法**，我们平时实现viewpager适配器继承PagerAdapter的时候都会覆写这个方法。每一个tabItem的文字是pagerAdapter的getPageTitle方法得到的，而这个方法默认是返回null的



**所以！！！**并不是tabItem都不见了，事实上tabItem还存在，且数量为adapter.get返回的数字，但是getPageTitle在不覆写的情况下返回null，自然就没有文字了。

既然反正都要移除，在xml中就不必写tabItem了



**解决方案**

- 在pagerAdapter的子类中重写getPageTitle，返回每一个page的title

- 不重写该方法，在相应处调用tabLayout.setupWithViewPager的后面给每一个tabItem调用setText。同时还可以设置icon等



**部分代码**

```xml
<com.google.android.material.tabs.TabLayout
    android:id="@+id/tab_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:fitsSystemWindows="true"
    <!-- 去除点击的阴影效果-->
    app:tabRippleColor="@android:color/transparent"
    <!-- 下划线不沾满tabItem -->
    app:tabIndicatorFullWidth="false"
	<!-- 下划线高度-->
    app:tabIndicatorHeight="3dp"
	<!-- 文字颜色（用selector）-->
    app:tabTextColor="@drawable/tab_text_color_selector"
	<!-- 下划线颜色-->
    app:tabIndicatorColor="@color/blue">
```



**tab_text_color_selector**

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color = "@color/black" android:state_selected="false"/>
    <item android:color = "@color/blue" android:state_selected="true"/>
</selector>
```



因为需要设置icon，所以没有重写adpter的getPageTitle方法

```java
private void setTabItem(){
    int tabCount = tabLayout.getTabCount();
    String[] titles = new String[tabCount];
    int[] icons = new int[tabCount];
    titles[0] = "快捷";
    titles[1] = "好友";
    titles[2] = "日志";
    titles[3] = "相册";
    icons[0] = R.drawable.tab_icon_quick_selector;
    icons[1] = R.drawable.tab_icon_friend_selector;
    icons[2] = R.drawable.tab_icon_log_selector;
    icons[3] = R.drawable.tab_icon_photo_selector;
    for (int i = 0; i < tabCount; i++) {
        if (tabLayout.getTabAt(i) != null) {
            tabLayout.getTabAt(i).setText(titles[i]);
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }
    }
}
```

所有icon均使用**svg**，通过**svgToDrawable**插件转化为drawable（xml形式），然后用selector形式

**R.drawable.tab_icon_quick_selector**

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/vector_drawable_quick_black" android:state_selected="false"/>
    <item android:drawable="@drawable/vector_drawable_quick_blue" android:state_selected="true"/>
</selector>
```



但是有时候icon加文字并不能满足我们的需求，**需要实现一些更加花里胡哨的tabItem**，每一个Tab是可以设置自定义布局的

先加载tabItem的布局view

```java
tabLayout.getTabAt(i).setCustomView(view)
```

直接传入布局id也是可以的

