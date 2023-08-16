package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//声明自定义注解要放在方法上
@Target(ElementType.METHOD)
//声明自定义注解在运行时有效
@Retention(RetentionPolicy.RUNTIME)
//创建自定义注解接口
public @interface LoginRequired {

}
