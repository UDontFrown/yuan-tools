### 背景
对外提供接口或者调用外部接口时，通常会提供批量操作接口，如果一次处理的数据量太大会对数据库造成较大压力，并且处理时间超长也会导致接口调用失败，所以使用分片方法对接口进多次调用，最后整合结果。
### 使用
#### 1、引入依赖
```xml
<dependency>
    <groupId>com.aqiu</groupId>
    <artifactId>partition</artifactId>
    <version>1.0.0</version>
</dependency>
```
#### 2、添加配置类
```java
@Configuration
public class AspectConfiguration {

    @Bean
    public BatchPartitionAspect batchPartitionAspect() {
        return new BatchPartitionAspect();
    }
}
```
#### 3、添加注解
在方法上添加@BatchPartition注解，示例：
```java
@BatchPartition(strategy = IGNORE)
public List<EmpVO> getEmpListByEmpIds(String corpId, List<String> empIds) {
    return empRepository.getByCorpIdAndEmpIds(corpId, empIds);
}
```
#### 4、参数介绍
 
```java
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
```
#### 5、支持的场景
1、方法至少有一个参数并且为类型List
2、支持的返回类型

   - List：将每次分片的结果汇总成一个List
   - Set：将每次分片的结果汇总成一个Set
   - Map：将每次分片的结果汇总成一个Map
   - Number：支持数字类型的基本类型和包装类型（byte、Byte、short、Short、int、Integer、long、Long、float、Float、double、Double），将每次分片的结果汇总成一个总数
   - Void：返回值为null
