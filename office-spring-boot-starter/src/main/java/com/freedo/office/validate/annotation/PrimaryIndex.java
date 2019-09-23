package com.freedo.office.validate.annotation;

import javax.validation.groups.Default;
import java.lang.annotation.*;

/**
 * 唯一索引
 * @author freedo
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryIndex {

    /**
     * 索引字段名
     * @return
     */
    String[] fields();

    /**
     * 是否是单一数据
     */
    boolean isSingle() default false;

    /**
     * 提示信息校验失败
     */
    String message() default "verify indexes is illegal";

    /**
     * 分组
     */
    Class<?>[] group() default Default.class;

}
