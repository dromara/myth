/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.github.myth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * myth分布式事务框架注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Myth {


    /**
     * The destination name for this listener, resolved through the container-wide
     */
    String destination();


    /**
     * 目标接口类
     * 如果是springcloud用户，需要指定目标的接口服务
     * （因为springcloud是http的请求，通过反射序列化方式没办法调用，所有加了这个属性）
     * 如果是dubbo用户 则不需要指定
     * 如果是motan用户 则不需要指定
     *
     * @return Class
     */
    Class target() default Object.class;


    /**
     * 目标接口方法名称
     * 如果是springcloud用户，需要指定目标的方法名称
     * （因为springcloud是http的请求，通过反射序列化方式没办法调用，所有加了这个属性）
     * 如果是dubbo用户 则不需要指定
     * 如果是motan用户 则不需要指定
     *
     * @return String
     */
    String targetMethod() default "";


    /**
     * 是否有事务 这里具体指的是发起方是否有进行数据库的操作（是否有事务操作）
     *
     * @return PropagationEnum
     */
    PropagationEnum propagation() default PropagationEnum.PROPAGATION_REQUIRED;


    /**
     * mq 消息模式
     *
     * @return MessageTypeEnum
     */
    MessageTypeEnum pattern() default MessageTypeEnum.P2P;


}