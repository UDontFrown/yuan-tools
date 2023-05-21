package com.aqiu.yuantools.partition.aspect;

import com.aqiu.yuantools.partition.annotations.BatchPartition;
import com.google.common.collect.Lists;
import com.aqiu.yuantools.partition.enums.PartitionExceptionStrategyEnum;
import com.aqiu.yuantools.partition.enums.ResultTypeEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.aqiu.yuantools.partition.enums.ResultTypeEnum.LIST;


/**
 * @author: yuanyang
 * @date: 2022-11-07 10:13
 * @desc:
 */
@Aspect
@Order
@Component
public class BatchPartitionAspect {

    private final Logger logger = LoggerFactory.getLogger(BatchPartitionAspect.class);

    private static final String INFO_MSG = "分片操作执行, index={}, onceCount={}, resultTypeEnum={}";
    private static final String ERROR_MSG = "分片操作执行异常, index={}, onceCount={}, resultTypeEnum={}";

    /**
     * 暂时只支持批量List操作
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("@annotation(batchPartition)")
    public Object interceptor(ProceedingJoinPoint pjp, BatchPartition batchPartition) throws Throwable {
        // 获取被拦截的方法
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        //获取被拦截的方法参数
        Object[] args = pjp.getArgs();
        // 获取被拦截的方法返回值类型
        Class<?> resultType = method.getReturnType();
        ResultTypeEnum resultTypeEnum = ResultTypeEnum.getByType(resultType);
        // 检查入参类型
        if (!this.preCheckArgs(args)) {
            return pjp.proceed();
        }
        // 如果指定了分片字段
        if (StringUtils.hasText(batchPartition.partitionField())) {
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.getName().equals(batchPartition.partitionField())) {
                    return this.foreachProceedOfMultiArgs(pjp, args, i, batchPartition, resultTypeEnum);
                }
            }
        }
        // 如果没指定分片字段
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof List) {
                return this.foreachProceedOfMultiArgs(pjp, args, i, batchPartition, resultTypeEnum);
            }
        }
        return pjp.proceed();
    }

    /**
     * 检查出入参是否符合要求
     *
     * @param args
     * @return
     * @throws Throwable
     */
    private boolean preCheckArgs(Object[] args) {
        // 入参列表长度必须大于0(必须要有参数)
        if (args == null || args.length == 0) {
            return false;
        }
        // 必须有一个参数为List
        return Stream.of(args).anyMatch(List.class::isInstance);
    }

    /**
     * 针对单有效List类型入参的关键性批量处理
     *
     * @param pjp
     * @param args
     * @param index
     * @param batchPartition
     * @param resultTypeEnum
     * @return
     * @throws Throwable
     */
    private Object foreachProceedOfMultiArgs(ProceedingJoinPoint pjp, Object[] args, int index, BatchPartition batchPartition, ResultTypeEnum resultTypeEnum) throws Throwable {
        List<Object> data = (List<Object>) args[index];
        int onceCount = batchPartition.onceCount();
        PartitionExceptionStrategyEnum strategy = batchPartition.strategy();
        // 去重
        if (batchPartition.needDistinct()) {
            data = data.stream().distinct().collect(Collectors.toList());
        }

        // 数据长度小于分批长度，不分批处理
        if (data.size() <= onceCount) {
            return pjp.proceed();
        }

        List<List<Object>> partition = Lists.partition(data, onceCount);
        switch (resultTypeEnum) {
            case LIST:
            case SET:
                // 收集数据
                Collection<Object> resultCollection = resultTypeEnum == LIST ? new ArrayList<>() : new HashSet<>();
                Collection<Object> emptyCollection = resultTypeEnum == LIST ? Collections.emptyList() : Collections.emptySet();
                for (int i = 0; i < partition.size(); i++) {
                    // 替换参数
                    args[index] = partition.get(i);
                    // 执行被代理方法
                    try {
                        logger.info(INFO_MSG, i, onceCount, resultTypeEnum);
                        Object proceed = pjp.proceed(args);
                        if (Objects.nonNull(proceed)) {
                            Collection<Object> proceedColl = resultTypeEnum == LIST ? (List<Object>) proceed : (Set<Object>) proceed;
                            resultCollection.addAll(proceedColl);
                        }
                    } catch (Exception e) {
                        logger.error(ERROR_MSG, i, onceCount, resultTypeEnum, e);
                        switch (strategy) {
                            case FINISH_WITH_DATA:
                                return resultCollection;
                            case FINISH_WITHOUT_DATA:
                                return emptyCollection;
                            case THROW_EXCEPTION:
                                throw e;
                            case IGNORE:
                            default:
                                break;
                        }
                    }
                }
                return resultCollection;
            case MAP:
                // 收集数据
                Map<String, Object> resultMap = new HashMap<>();
                for (int i = 0; i < partition.size(); i++) {
                    // 替换参数
                    args[index] = partition.get(i);
                    // 执行被代理方法
                    try {
                        logger.info(INFO_MSG, i, onceCount, resultTypeEnum);
                        Object proceed = pjp.proceed(args);
                        if (Objects.nonNull(proceed)) {
                            resultMap.putAll((Map<String, Object>) proceed);
                        }
                    } catch (Exception e) {
                        logger.error(ERROR_MSG, i, onceCount, resultTypeEnum, e);
                        switch (strategy) {
                            case FINISH_WITH_DATA:
                                return resultMap;
                            case FINISH_WITHOUT_DATA:
                                return Collections.emptyMap();
                            case THROW_EXCEPTION:
                                throw e;
                            case IGNORE:
                            default:
                                break;
                        }
                    }
                }
                return resultMap;
            case NUMBER:
                Number number = 0;
                for (int i = 0; i < partition.size(); i++) {
                    // 替换参数
                    args[index] = partition.get(i);
                    // 执行被代理方法
                    try {
                        logger.info(INFO_MSG, i, onceCount, resultTypeEnum);
                        logger.info(INFO_MSG, i, onceCount, resultTypeEnum);
                        Object proceed = pjp.proceed(args);
                        number = addNumbers(number, proceed);
                    } catch (Exception e) {
                        logger.error(ERROR_MSG, i, onceCount, resultTypeEnum, e);
                        switch (strategy) {
                            case FINISH_WITH_DATA:
                                return number;
                            case FINISH_WITHOUT_DATA:
                                return 0;
                            case THROW_EXCEPTION:
                                throw e;
                            case IGNORE:
                            default:
                                break;
                        }
                    }
                }
                return number;
            default:
                for (int i = 0; i < partition.size(); i++) {
                    // 替换参数
                    args[index] = partition.get(i);
                    // 执行被代理方法
                    try {
                        logger.info(INFO_MSG, i, onceCount, resultTypeEnum);
                        pjp.proceed(args);
                    } catch (Exception e) {
                        logger.error(ERROR_MSG, i, onceCount, resultTypeEnum, e);
                        switch (strategy) {
                            case FINISH_WITH_DATA:
                            case FINISH_WITHOUT_DATA:
                                return null;
                            case THROW_EXCEPTION:
                                throw e;
                            case IGNORE:
                            default:
                                break;
                        }
                    }
                }
                return null;
        }
    }

    /**
     * 数字相加
     *
     * @param number
     * @param result
     * @return
     */
    private Number addNumbers(Number number, Object result) {
        if (Objects.isNull(result) || Objects.isNull(number)) {
            return null;
        }
        if (result instanceof Byte) {
            return number.byteValue() + (Byte) result;
        } else if (result instanceof Short) {
            return number.shortValue() + (Short) result;
        } else if (result instanceof Integer) {
            return number.intValue() + (Integer) result;
        } else if(number instanceof Long) {
            return number.longValue() + (Long) result;
        } else if (number instanceof Float) {
            return number.floatValue() + (Float) result;
        } else if (number instanceof Double) {
            return number.doubleValue() + (Double) result;
        }
        return number;
    }
}
