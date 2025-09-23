package cn.satoken.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

//为了避免每个分页类都要写：用了切面
//已经废除了使用了！！！XXXXX 因为业务逻辑没这么简单
//        if (pageNo.equals(-1L)) {
//            pageNo = 1L;
//            pageSize = count();
//        }


@Aspect
@Component
public class PageAspect {

    @Around("@annotation(cn.satoken.util.MyPage)")
//    @Around("execution(public cn.satoken.util.Result *(Long,Long))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取原始参数
        Object[] args = joinPoint.getArgs();
        // 获取目标类实例
        Object target = joinPoint.getTarget();
        Method count = target.getClass().getMethod("count");
        if (args[0].equals(-1L)) {
            args[0] = 1L;
            args[1] = count.invoke(target);
        }
        return joinPoint.proceed(args);
    }
}
