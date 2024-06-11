package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/*
自定义切面类，实现公共字段自动填充处理逻辑
 */
@Aspect//切面类的标识
@Component//这个类是bean，交给容器管理
@Slf4j
public class AutoFillAspect {
    //切面类 = 通知 + 切入点
    /*
    切入点，对哪些类的方法进行拦截
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")//切点表达式，对哪些类哪些方法进行拦截
    public void autoFillPointCut(){}

    /*
    前置通知,功能是：在公共字段中赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段进行通知");
        //1.获取当前被拦截到的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //获取方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上注解对象
        OperationType operationType = autoFill.value(); //获得操作类型（operationType里面的常量值）

        //2.获取到当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();    //获得所有的参数;
        // joinPoint.getArgs() 是一个方法，用于获取连接点处方法的参数数组。
        // 这里获取了自定义注解AutoFill下所有的参数
        // 例如void update(Employee employee)中的employee等

        if (args == null || args.length == 0) return;
        Object entity = args[0];    //我们是【约定俗成】void update(Employee employee xxx)中第一个对象为所需的实体对象

        //3.准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //4.根据当前不同的操作类型，为对应的属性通过【反射】来赋值
        if (operationType == OperationType.INSERT) {
            //if (name.contains("insert")) {
            try {
                //为4个公共字段赋值
                //赋值：调用set方法来进行赋值
                //entity.getClass().getDeclaredMethod()
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //情况1字段填充
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            //else if (name.contains("update")){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //情况2字段填充
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

}
