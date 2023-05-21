package com.aqiu.yuantools.partition.enums;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.*;

/**
 * @author: yuanyang
 * @date: 2023-04-26 10:07
 * @desc:
 */
@Getter
public enum ResultTypeEnum {

    LIST(List.class, "List"),
    SET(Set.class, "Set"),
    MAP(Map.class, "Map"),
    NUMBER(Number.class, "Number"),
    OTHER(OtherClass.class, "Other"),
    ;

    private final Class clazz;
    private final String desc;

    ResultTypeEnum(Class clazz, String desc) {
        this.clazz = clazz;
        this.desc = desc;
    }

    public static ResultTypeEnum getByType(Class<?> classType) {
        // 如果是基本类型数字就返回数字
        if (classType.isPrimitive() && !Lists.newArrayList("void", "boolean", "char").contains(classType.getName())) {
            return NUMBER;
        }
        for (ResultTypeEnum value : ResultTypeEnum.values()) {
            if (value.getClazz().isAssignableFrom(classType)) {
                return value;
            }
        }
        return OTHER;
    }

    /**
     * final 防止其他类继承
     */
    private static final class OtherClass {

    }
}
