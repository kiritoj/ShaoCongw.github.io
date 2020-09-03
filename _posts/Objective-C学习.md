自动释放池

```objective-c
@autoreleasepool{
	//code
}
```



NSLog打印值对应的占位符

int ：%i

# 面向对象

## 定义类

@interface

定义未实现的方法

-（return type）pram1：(type)p1 pram2:(type)p2

实现类

@implementation

实现方法

其中，实例变量定义在interface中还是inplementation都可以，在内部都可以访问到，在外部不能访问。而且实例变量要在一对大括号中

**分清实例变量和属性的区别**

**实例方法可以访问实例变量，但类方法不行**

## 实例变量的getter和setter

所以，给实例变量设置get，set方法

```objective-c
@interface Fraction : NSObject
  
-(void) setFenzi:(int)n; //set方法
-(void) setFenmu:(int)d; //set方法
-(int) fenzi; //get方法
-(int) fenmu; //get方法

@end
```

```objective-c
@implementation Fraction
{
    
    int fenzi;
    int fenmu;
}


-(void)setFenzi:(int)n{
    fenzi = n;
}
-(void) setFenmu:(int)d{
    fenmu = d;
}

-(int) fenzi{
    return fenzi;
}

-(int) fenmu{
    return fenmu;
}

```

## 基本数据类型

* int (%i)
* float (%f)
* double 
* char（字符）%c
* id类型，存储任意类型的对象。同样的，id类型声明时也不用写*号

![](http://ww1.sinaimg.cn/large/006nwaiFgy1ghcgeowvy0j30wq0ik46e.jpg)

类型转换和java类似

## 导入头文件

import “xxx.h”，用**双引号**，代表本地文件而不是系统文件

在类中如果要使用其他类，需要引入相应的.h文件



## 属性

属性和是实例变量是一个东西。

@property 属性名

只不过自动实现了getter和setter方法

```objective-c
#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface Person : NSObject

@property int age; //属性

-(void) toString;

@end

NS_ASSUME_NONNULL_END
```

```objective-c
#import "Person.h"

@implementation Person

-(void) toString{
    NSLog(@"age:%i",_age);
}

@end
```

在实现中，通过**_属性名**来访问，也可以通过**self.age**访问

在外部中，可直接通过**对象.属性名**访问，或者【对象 属性名】getter方式访问

设值方法为**对象.属性名 = x**,或者【对象 set属性名】

在实现类中，加上

```objective-c
@synthesize age
```

在内部就可以直接通过属性名访问，前面不用加   **_**



如果是手动实现的getter/setter方法，也是可以通过**对象.属性**直接访问的，但getter和setter命名必须规范

## Static 变量

一次初始化，保存多次操作的结果

## 继承

父类，只有在接口部分声明的实例变量，子类才可以直接访问

父类

```objective-c
@interface Father : NSObject
{
    int val;
}

-(void)initVal;

@end

@implementation Father

-(void)initVal{
    val = 100;
}
```

子类

```objective-c
@interface Kid : Father

-(void)printVal;

@end

@implementation Kid

-(void)printVal{
    NSLog(@"val=%i",val);
}

@end
```

可以看到，直接使用了父类实例变量val

### 方法覆写

子类要在



# Block

