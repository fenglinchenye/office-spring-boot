package com.freedo.office.validate.annotation;

import javax.validation.groups.Default;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 唯一索引（组合索引）
 * @author freedo
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryIndexes {

	/**
	 * 唯一索引
	 */
	PrimaryIndex[] value();

	/**
	 * 分组 默认是Default 分组
	 * @return
	 */
	Class<?>[] group() default Default.class;

}
