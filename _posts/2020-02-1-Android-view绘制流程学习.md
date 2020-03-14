---
layout:     post
title:      Android MeasureSpec
subtitle: 
date:       2020-03-6
author:     taoke
header-img: img/post-bg-ios9-web.jpg
catalog: true
tags:
    - 面试
    - android
---

# activity，window，viewroot

一个activity有一个window，其中window是一个抽象类

```JAVA
public abstract class Window {
    /** Flag for the "options panel" feature.  This is enabled by default. */
    public static final int FEATURE_OPTIONS_PANEL = 0;
    /** Flag for the "no title" feature, turning off the title at the top
     *  of the screen. */
    public static final int FEATURE_NO_TITLE = 1;

    /**
     * Flag for the progress indicator feature.
     *
     * @deprecated No longer supported starting in API 21.
     */
    @Deprecated
    public static final int FEATURE_PROGRESS = 2;
```

window类有一个实现类**PhoneWindow**，PhoneWindow提供了一系列窗口的方法，比如设置背景，标题等。每一个PhoneWindow对应一个**DecorView**

DecorView是顶级View，包含一个LinerLayout，上面是标题栏（title），下面是内容栏（content），内容部分是一个ViewGroup，通过

```java
ViewGroup vg = findViewById(android.R.id.content);
```

可以获得它

而在onCreat方法中setContentView方法正是给它设置布局，如何得到布局中的最大父元素呢

```java
View content = vg.getChildAt(0);
```



**ViewRoot**有一个实现类ViewRootImpl，它的作用将DecorView和Window建立关联，将DecorView添加到Window中



------

# MesureSpec



先说一个LayoutParams，我们在布局文件中给view设置的宽高就是LayoutParams，有一下三种情况

```java
LayoutParams.MATCH_PARENT
```

```
LayoutParams.WRAP_CONTENT
```

```
固定数值如，10dp
```

但上述设置的宽高并不是view的宽高，而是作为测量view宽高的一部分数据，即构造view的MesureSpec，**MesureSpec决定了view的尺寸**，**但还要受父元素的影响**

**总之，MesureSpec受自身LayoutParams和父元素所施加的规则共同构造**



**MeasureSpec的组成**

MeasureSpec是一个32位二进制数，前两位代表**SpecMode**，后30位才是**SpecSize**

```java
public static class MeasureSpec {
    private static final int MODE_SHIFT = 30;
    private static final int MODE_MASK  = 0x3 << MODE_SHIFT;
    public static final int UNSPECIFIED = 0 << MODE_SHIFT;
    public static final int EXACTLY     = 1 << MODE_SHIFT;
    public static final int AT_MOST     = 2 << MODE_SHIFT;
```

从源码中可以看到一共有3种Mode：UNSPECIFIED，EXACTLY，AT_MOST

它们的高两位分别是00,01,10，后面全是30个0，方便和size相加构成最后的32位数

```
public static int makeMeasureSpec(int size, int mode) {
    if (sUseBrokenMakeMeasureSpec) {
        return size + mode;
    } else {
        return (size & ~MODE_MASK) | (mode & MODE_MASK);
    }
}
```

## 计算32位数

从源码可以看出，**有两种计算32位measurespec方法**

一是直接将mode和size相加，这种比较好理解。假如mode是AT_MOST,size是0000...110

mode = **10**...........0000

size = **00**(size的高两位肯定是0).......110

mode+size就可以把mode和size拼接到一起了

二是else语句块中**或运算**的方法

MODE_MASK  = 0x3 << MODE_SHIFT;

MODE_MASK = **11**000000....0000;

size只需要后面30位，所以将mask取反再与操作

mode只需要前面两位，所以将mode直接与mask与操作，最后再和size或运算



## getMode和getSize

```java
public static int getMode(int measureSpec) {
    //noinspection ResourceType
    return (measureSpec & MODE_MASK);
}
```

```java
public static int getSize(int measureSpec) {
    return (measureSpec & ~MODE_MASK);
}
```

getMode和getSize就比较简单了，上面已经分析过了



以上可知，MeasureSpec将mode和size打包成一个32位int值，**减少了对象内存分配**。

同时提供了解包的方法。**注意**：上述的MeasureSpec指代的是MeasureSpec的int值，而不是MeasureSpec本身，MeasureSpec是一个类，要实例化才能使用



## SpecMode

**UNSPECIFIED**

表示父容器对view没有任何限制，这个一般不管它

**EXACTLY**

父容器已经检测出View所需要的**精确大小**，View的最终大小就是SpecSize的值。对应于**LayoutParamas.MATCH_PARENT**和**具体数值**这两种情况

**AT_MOST**

父容器指定了一个**可用大小**，即SpecSize。View的大小**不能大于这个值**，具体大小取决于子view的实现。对应于**LayoutParams.WRAP_CONTENT**



## MeasureSpec和LayoutParams的对应关系

LayoutParams 的作用是：**子控件告诉父控件，自己要如何布局**

LayoutParams是一个ViewGroup中的一个静态类。FrameLayout。LinearLayout等都有自己的LayoutParams继承它

所以设置一个view的LayoutParams的时候使用的是它父容器的LayoutParams。

比如现在有一个LinnerLayout如下

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center_vertical"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/tv_hello"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        android:layout_gravity="center_horizontal"
        android:background="@color/colorAccent"
       />
    <Button
        android:id="@+id/bt_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="设置params"

        />
```

现在要实现点击button设置text的宽度为**MATCH_PARENT**，在button的点击事件中如下写

```java
button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        textView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT
                        ,ViewGroup.LayoutParams.WRAP_CONTENT));

    }
});
```

可以看到，textView设置的LayoutParams是它的父容器LinearLayout的LayoutParams



当我们为view设置LayoutParams的时候，系统会将LayoutParams**在父容器的约束下**转换成对应的MeasureSpec，**所以最终的从测量结果还是由MeasureSpec得来的**



------

对于顶级View（DecorView）和普通view，MeasureSpec的转换过程略有不同。

DecorView的MeasureSpec由自身LayoutParams和**窗口尺寸**确定的，因为顶级View没有父容器了

普通view的MeasureSpec有自身LayoutParams和**父容器的MeasureSpec确定的**

**一旦MeasureSpec确定以后，onMeasure（）中就可以确定view的测量宽高**



------

## **DecorView的MeasureSpec的确定过程**

获取宽的MeasureSpec

```ajva
getRootMeasureSpec(desiredWindowWidth, lp.width)
```

desiredWindowWidth是屏幕的宽度，lp.width是自身LayoutParams的宽度

```java
private static int getRootMeasureSpec(int windowSize, int rootDimension) {
    int measureSpec;
    switch (rootDimension) {

    case ViewGroup.LayoutParams.MATCH_PARENT:
        // Window can't resize. Force root view to be windowSize.
        measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.EXACTLY);
        break;
    case ViewGroup.LayoutParams.WRAP_CONTENT:
        // Window can resize. Set max size for root view.
        measureSpec = MeasureSpec.makeMeasureSpec(windowSize, MeasureSpec.AT_MOST);
        break;
    default:
        // Window wants to be an exact size. Force root view to be that size.
        measureSpec = MeasureSpec.makeMeasureSpec(rootDimension, MeasureSpec.EXACTLY);
        break;
    }
    return measureSpec;
}
```

可见，根据LayoutParams的不同，DecorView的MeasureSpec创建有以下三种情况

- **LayoutParams.MATCH_PARENT**

  精确模式（Exactly），大小为窗口大小

- **LayoutParams.WRAP_CONTENT**

  最大模式（AT_MOST）,大小最大不能超过窗口尺寸

- **固定数值大小（100dp）**

  精确模式（Exactly），大小为传入的具体数值



## 子元素的measureSpec的确定过程

首先必须明确，子元素的measureSpec是在父容器的中确定的

在ViewGroup中有以下两个方法

```java
protected void measureChild(View child, int parentWidthMeasureSpec,
        int parentHeightMeasureSpec) {
    final LayoutParams lp = child.getLayoutParams();

    final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
            mPaddingLeft + mPaddingRight, lp.width);
    final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
            mPaddingTop + mPaddingBottom, lp.height);

    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
}
```

```java
    protected void measureChildWithMargins(View child,
        int parentWidthMeasureSpec, int widthUsed,
        int parentHeightMeasureSpec, int heightUsed) {
    final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

    final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
            mPaddingLeft + mPaddingRight + lp.leftMargin + lp.rightMargin
                    + widthUsed, lp.width);
    final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
            mPaddingTop + mPaddingBottom + lp.topMargin + lp.bottomMargin
                    + heightUsed, lp.height);

    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
}
```

过程都是一样的，不过measureChildWithMargins考虑了子元素margin值和赋容器已经使用了的值



在这两个方法中都通过**getChildMeasureSpec**方法获取到子view的measureSpec，然后再去调用子元素的measure方法



```java
public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
    int specMode = MeasureSpec.getMode(spec);
    int specSize = MeasureSpec.getSize(spec);

    int size = Math.max(0, specSize - padding);

    int resultSize = 0;
    int resultMode = 0;
    switch (specMode) {
```

方法开头分别取出父容器的SpecMode和SpecSize。定义了一个父容器**当前可用size**

以及最后子元素的size和mode，然后针对父容器的mode进行分类

**父容器的mode为EXACTLY**

```java

// Parent has imposed an exact size on us
case MeasureSpec.EXACTLY:
    if (childDimension >= 0) {
        resultSize = childDimension;
        resultMode = MeasureSpec.EXACTLY;
    } else if (childDimension == LayoutParams.MATCH_PARENT) {
        // Child wants to be our size. So be it.
        resultSize = size;
        resultMode = MeasureSpec.EXACTLY;
    } else if (childDimension == LayoutParams.WRAP_CONTENT) {
        // Child wants to determine its own size. It can't be
        // bigger than us.
        resultSize = size;
        resultMode = MeasureSpec.AT_MOST;
    }
    break;
```

这时候就取决于子view的**LayoutParams**了

- 固定数值（比如我们在xml中设置30dp，但最终的值并不是30，涉及到单位不同的问题，但一定是一个正整数）

  specSize就等于lp的值

  specMode为Exactly

- MATCH_PARENT（-1）

  specSize为**父容器的可用大小size**，子view的大小就是可用大小

  specMode为Exactly

- WRAP_CONTENT（-2）

  specSize为父容器的可用大小size，子元素的大小不会超过可用大小

  specMode为AT_MOST



**父容器的mode为ATMOST**

```java
case MeasureSpec.AT_MOST:
    if (childDimension >= 0) {
        // Child wants a specific size... so be it
        resultSize = childDimension;
        resultMode = MeasureSpec.EXACTLY;
    } else if (childDimension == LayoutParams.MATCH_PARENT) {
        // Child wants to be our size, but our size is not fixed.
        // Constrain child to not be bigger than us.
        resultSize = size;
        resultMode = MeasureSpec.AT_MOST;
    } else if (childDimension == LayoutParams.WRAP_CONTENT) {
        // Child wants to determine its own size. It can't be
        // bigger than us.
        resultSize = size;
        resultMode = MeasureSpec.AT_MOST;
    }
    break;
```

子viewLayoutParams的值

- 固定数值

  specSize为lp的值

  specMode为Exactly

- MATCH_PARENT（-1）

  specSize为当前父元素的可用大小，view的大小不会超过可用大小

  specMode为AT_MOST

- WRAP_CONTENT(-2)

  specSize为当前父元素的可用大小，view的大小不会超过可用大小

  specMode为AT_MOST



至于UNSPECIFIED这种情况不去管

**总结一下**

当view采用固定宽高的时候，view的MeasureSpec都是精准模式（Exactly），大小遵循自己的大小

当view是Match_parent的时候，size都是父容器的可用大小，当父元素的mode是exactly，则子元素的模式也是exactly，大小为size；   当父容器的mode是at_most，子view的模式也是at_most。子view的大小不会超过可用大小

当view是WRAP_CONTENT的时候，不论父容器的模式，size都为父容器的可用大小，子元素的模式都是at_most,大小不会超过可用大小



最后看一下ViewGroup的

```java
protected void measureChildren(int widthMeasureSpec, int heightMeasureSpec) {
    final int size = mChildrenCount;
    final View[] children = mChildren;
    for (int i = 0; i < size; ++i) {
        final View child = children[i];
        if ((child.mViewFlags & VISIBILITY_MASK) != GONE) {
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
    }
}
```

可见，在viewgroup中，会遍历所有子元素，获取他们的measureSpec，然后每个子元素调用自己的measure（）方法