package com.aqiu.yuantools.partition.annotations;

import com.aqiu.yuantools.partition.enums.PartitionExceptionStrategyEnum;

import java.lang.annotation.*;

/**
 * @author: yuanyang
 * @date: 2023-04-26 10:04
 * @desc:
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BatchPartition {

    int ONCE_COUNT = 200;

    /**
     * 自动分批操作每批次的数量
     * @return 自动分批操作每批次的数量,默认一次处理200条
     */
    int onceCount() default ONCE_COUNT;

    /**
     * 批量操作的异常策略
     * 1、忽略本次异常，并进行下一次操作
     * 2、返回已有数据
     * 3、返回空数据
     * 4、抛出异常
     * @return 批量操作的异常策略,默认抛出异常
     */
    PartitionExceptionStrategyEnum strategy() default PartitionExceptionStrategyEnum.THROW_EXCEPTION;

    /**
     * List里的数据是否需要去重
     * @return List里的数据是否需要去重,默认需要去重
     */
    boolean needDistinct() default true;

    /**
     * 当存在多个List类型参数时可以指定参数中分片字段名称
     * @return 当存在多个List类型参数时可以指定参数中分片字段名称,如果没有指定,默认找第一个List类型的参数
     */
    String partitionField() default "";
}
