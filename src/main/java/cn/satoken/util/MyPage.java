package cn.satoken.util;

//用于切面增强

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)   // 用在方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface MyPage {
}
