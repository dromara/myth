myth  
================
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d0dd634df7854d27add47fcfaea0e9d5)](https://www.codacy.com/app/yu199195/myth?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yu199195/myth&amp;utm_campaign=Badge_Grade)
[![Total lines](https://tokei.rs/b1/github/yu199195/myth?category=lines)](https://github.com/yu199195/myth)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?label=license)](https://github.com/yu199195/myth/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.dromara/myth.svg?label=maven%20central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.dromara%22%20AND%20myth)
[![Build Status](https://travis-ci.org/yu199195/myth.svg?branch=master)](https://travis-ci.org/yu199195/myth)
[![QQ群](https://img.shields.io/badge/chat-on%20QQ-ff69b4.svg?style=flat-square)](https://shang.qq.com/wpa/qunwpa?idkey=2e9e353fa10924812bc58c10ab46de0ca6bef80e34168bccde275f7ca0cafd85)
###  Reliable messages resolve distributed transactions


# Modules
  
  * myth-admin: Transaction log management background
  
  * myth-annotation : Framework common annotations
  
  * myth-common :  Framework common class
    
  * myth-core : Framework core package (annotation processing, log storage...)              
    
  * myth-dashboard : Management background front-end
    
  * myth-dubbo : Support for the dubbo framework Less than 2.7 version
    
  * myth-motan : Support for the motan rpc framework
    
  * myth-springcloud : Support for the spring cloud rpc framework
    
  * myth-spring-boot-starter : Support for the spring boot starter
  
  * myth-aliyunmq: Support for aliyunmq
  
  * myth-jms : support for Mq for the JMS protocol(amq...)
  
  * myth-kafka : support for kafka
  
  * myth-rabbitmq : support for rabbitmq
  
  * myth-rocketmq : support for rocketmq
    
  * hmily-demo : Examples using the hmily framework
 
#  Features
   
   *  All spring versions are supported and Seamless integration
   
   *  Provides support for the springcloud dubbo motan RPC framework
   
   *  Provides integration of the spring boot starter approach
   
   * Support for a lot of messaging middleware (rabbitmq jms kafka rabbitmq rocketmq)  
   
   *  Local transaction storage support :  redis mongodb zookeeper file mysql
   
   *  Transaction log serialization support : java hessian kryo protostuff
   
   *  Spi extension : Users can customize the storage of serialization and transaction logs

# Prerequisite 

  * You must use jdk1.8 +
  
  * You must be a user of the spring framework
  
  * You must use  messaging middleware
  
  * You must use one of the dubbo, motan, and springcloud RPC frameworks
  
# About 

   Myth is a Reliable messages solution for distributed transactions, Its rapid integration, zero penetration high performance has been run by a number of companies  in the production environment.
   
   Myth is not a framework for exception rollbacks .
   
   Myth To ensure that your RPC interface can be executed, use mq to execute the RPC you need to call when your RPC interface is down
  
   If you want to use it or get a quick look at it. [Quick Start](http://dromara.org/website/zh-cn/docs/myth/index.html)
  
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

# Stargazers

[![Stargazers over time](https://starchart.cc/yu199195/myth.svg)](https://starchart.cc/yu199195/myth)
 
# Support

  ![](https://yu199195.github.io/images/qq.png)    ![](https://yu199195.github.io/images/public.jpg)

