package com.freedo.office.validate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import org.apache.commons.lang3.StringUtils;


/**
 * @desc 校验枚举值有效性
 * @author freedo
 */
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumRange.Validator.class)
public @interface EnumRange {

	String message() default "not belong to this enum code";

	/**
     * group 分组默认为Hibernate 分组
	 * @return
     */
	Class<?>[] groups() default {};

	/**
     *
	 * @return
     */
	Class<? extends Payload>[] payload() default {};

	/**
	 * 针对字符串类型的值，是否开启空串为有效验证
	 * 默认为false 积空串作为正常值匹配处理
	 * 如果为 true 则讲空串作为 null 值等同处理
	 */
	boolean isSelectiveBlank() default false;

	/***
	 * 枚举类
	 * 必须实现接口{@link EnumInterface}
	 */
	Class<? extends Enum<? extends EnumInterface<?>>> enumClass();

	class Validator implements ConstraintValidator<EnumRange, Object> {

		private Class<? extends Enum<? extends EnumInterface<?>>> enumClass;

		private boolean selectiveBlank;

		@Override
		public void initialize(EnumRange enumRange) {
			enumClass = enumRange.enumClass();
			selectiveBlank = enumRange.isSelectiveBlank();
		}

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
			if (value == null) {
				return true;
			}
			if (enumClass == null) {
				return true;
			}
			// 枚举必须实现EnumInterface
			if (!EnumInterface.class.isAssignableFrom(enumClass)) {
				throw new RuntimeException(
					String.format("%s class must implement %s class", enumClass, EnumInterface.class));
			}
			Enum<?>[] enumConstants = enumClass.getEnumConstants();
			if (enumConstants==null||enumConstants.length==0){
				return false;
			}

			boolean occur = false;
			if (enumConstants.length==0){
				return false;
			}
			EnumInterface firstEnumInterface = (EnumInterface) enumConstants[0];
			if (firstEnumInterface.getCode() instanceof String){
				if (selectiveBlank&& StringUtils.isBlank((String) value)){
					return true;
				}
			}
			for (Enum<?> constant : enumConstants) {
				EnumInterface enumInterface = (EnumInterface) constant;
				if(enumInterface.getCode().equals(value)){
					occur = true;
				}
			}
			return occur;
		}
	}
}