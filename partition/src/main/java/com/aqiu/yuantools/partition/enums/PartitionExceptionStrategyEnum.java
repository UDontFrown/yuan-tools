package com.aqiu.yuantools.partition.enums;

import lombok.Getter;

/**
 * @author: yuanyang
 * @date: 2023-04-26 10:07
 * @desc:
 */
@Getter
public enum PartitionExceptionStrategyEnum {

    IGNORE("ignore", "忽略"),
    FINISH_WITH_DATA("finishWithData", "携带数据结束"),
    FINISH_WITHOUT_DATA("finishWithoutData", "不携带数据结束"),
    THROW_EXCEPTION("throwException", "抛出异常"),
    ;

    private final String code;
    private final String desc;

    PartitionExceptionStrategyEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PartitionExceptionStrategyEnum getByCode(String code) {
        for (PartitionExceptionStrategyEnum value : PartitionExceptionStrategyEnum.values()) {
            if (value.getCode().equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
