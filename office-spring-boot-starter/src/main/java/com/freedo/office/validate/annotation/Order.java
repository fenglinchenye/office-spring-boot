package com.freedo.office.validate.annotation;

import java.lang.annotation.*;

/**
 * 排序值(用于校验器的先后顺序)
 * @author freedo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Order {

    /**
     * The order value.
     */
    int value() default  Integer.MAX_VALUE;

}