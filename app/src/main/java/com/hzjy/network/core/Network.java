package com.hzjy.network.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * pj567
 * 2019/9/9
 */

@Target(ElementType.METHOD)//作用在方法之上
@Retention(RetentionPolicy.RUNTIME)//jvm运行时，通过反射获取该注解的值
public @interface Network {
    NetType netType() default NetType.AUTO;
}
