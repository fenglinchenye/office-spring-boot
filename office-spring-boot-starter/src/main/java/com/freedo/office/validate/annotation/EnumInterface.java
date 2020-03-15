package com.freedo.office.validate.annotation;

/**
 * 枚举接口
 * @author freedo
 */
public interface EnumInterface<T> {

    /**
     * 获取对应的code
     * @return
     */
    T getCode();

    /**
     * 获取对应的解释
     * @return
     */
    String getDesc();

}
