myth  
================

#####  采用消息队列解决分布式事务的开源框架, 基于java语言来开发（JDK1.8），支持dubbo，springcloud,motan等rpc框架进行分布式事务。

#  Features

  * ##### RPC框架支持 : dubbo,motan,springcloud。

  * ##### 消息中间件支持 : jms(activimq),amqp(rabbitmq),kafka,roceketmq。

  * ##### 本地事务存储支持 : redis,mogondb,zookeeper,file,mysql。

  * ##### 事务日志序列化支持 ：java，hessian，kryo，protostuff。

  * ##### 采用Aspect AOP 切面思想与Spring无缝集成，天然支持集群,高可用,高并发。

  * #####  配置简单，集成简单，源码简洁，稳定性高，已在生产环境使用。

  * ##### 内置经典的分布式事务场景demo工程，并有swagger-ui可视化界面可以快速体验。

# Prerequisite

  *   #### JDK 1.8+

  *   #### Maven 3.2.x

  *   #### Git

  *   ####  RPC framework dubbo or motan or springcloud。

  *   #### Message Oriented Middleware


# Quick Start

* #### Clone & Build
   ```
   > git clone https://github.com/yu199195/myth.git

   > cd myth

   > mvn -DskipTests clean install -U
   ```

* #### execute this sql       
 https://github.com/yu199195/myth/tree/master/myth-demo/sql/myth-mysql-demo.sql

* #### Find the RPC framework that works for you
 https://github.com/yu199195/myth/tree/master/myth-demo


* ## [Dubbo Quick Start](https://github.com/yu199195/myth/wiki/Dubbo-Quick-Start)

* ##  [SpringCloud Quick Start](https://github.com/yu199195/myth/wiki/SpringCloud--Quick-Start)

* ##  [Motan Quick Start](https://github.com/yu199195/myth/wiki/Motan-Quick-Start)

# Configuration

* ####  [配置详解](https://github.com/yu199195/myth/wiki/Configuration)

# User Guide

* #### 关于jar包引用问题，现在jar包还未上传到maven的中央仓库，所以使用者需要自行获取代码，然后打包上传到自己maven私服

   ```
   > git clone https://github.com/yu199195/myth.git

   > mvn -DskipTests clean deploy -U
   ```
* #### 关于jar包版本问题 ，现在因为没有传到中央仓库，如果引用的话，请自行设置相应的版本。


*  ## [Dubbo User Guide](https://github.com/yu199195/myth/wiki/Dubbo-User-Guide)

*  ## [Motan User Guide](https://github.com/yu199195/myth/wiki/Motan-User-Guide)

*  ## [SpringCloud User Guide](https://github.com/yu199195/myth/wiki/SpringCloud-User-Guide)

# FAQ

* ### 为什么我下载的代码后，用idea打开没有相应的get set 方法呢？
   ##### 答：因为框架使用了Lombok包，它是在编译的时期，自动生成get set方法，并不影响运行，如果觉得提示错误难受，请自行下载lombok包插件，[lombok官网](http://projectlombok.org/)

* ### 为什么我运行demo工程，找不到applicationContent.xml呢？
  ##### 答：请设置项目的资源文件夹。

# Support

  ### 如有任何问题欢迎加入QQ群：162614487 进行讨论 
  ![](https://yu199195.github.io/images/weixin.jpg)


# Contribution
