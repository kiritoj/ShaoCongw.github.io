## ProtoBuf在Android中的使用

* **编写.proto文件，以微盘模块为例。ww_qydisk.proto**

![](http://ww1.sinaimg.cn/large/006nwaiFgy1gho2ppun84j317m0pktf0.jpg)



* **编译成java类或，C++类**

![](http://ww1.sinaimg.cn/large/006nwaiFgy1gho2v8tqzej31im0je0zk.jpg)

* java生成类QyDiskProto.java
* c++生成ww_qydisk.pb.cc 和 ww_qydisk.pb.h



### 以容量提示需求为例

SpaceItemList在ww_qydisk中的定义：

![](http://ww1.sinaimg.cn/large/006nwaiFgy1gho4k4paz7j310e0b0n16.jpg)

**Java**

![](http://ww1.sinaimg.cn/large/006nwaiFgy1gho4lxef6yj3158166dmx.jpg)



**QyDiskService 获取实例**

![](http://ww1.sinaimg.cn/large/006nwaiFgy1gho4o42g51j31qy0iggpz.jpg)

生成的Java解析native成返回的byte[], 生成对象

![](http://ww1.sinaimg.cn/large/006nwaiFgy1gho4pv7l3qj3192064wfu.jpg)



JNI调用Cpp代码

![](http://ww1.sinaimg.cn/large/006nwaiFgy1gho4tggskpj320y18qaju.jpg)



Cpp：QydiskService.getSpaceList

![](http://ww1.sinaimg.cn/large/006nwaiFgy1gho4ws6u35j313a0b4gpj.jpg)