package com.freedo.office.util;

import com.freedo.office.basic.SystemConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Objects;

/**
 * 类操作工具 扩展
 * @author freedo
 */
public class BeanUtils extends org.springframework.beans.BeanUtils {

    /**
     * 获取对象的某个字段的属性值
     * @param propertyName
     * @param source
     * @return
     */
    public static Object getValueByProperty(String propertyName,Object source){
        // 获取对象的值必须不能为空
        Assert.notNull(source,"source must not be null");
        // 获取对应字段的特征描述
        PropertyDescriptor descriptor = getPropertyDescriptor(source.getClass(), propertyName);
        if (Objects.nonNull(descriptor)) {
            Method readMethod = descriptor.getReadMethod();
            if (Objects.nonNull(readMethod)) {
                try {
                    return readMethod.invoke(source);
                } catch (Throwable e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        throw new RuntimeException(MessageFormat.format("this class :{1} not have property :{2} or not have public get method for this {3} property",
                source.getClass().getName(),
                propertyName,
                propertyName));
    }

    /**
     * 获得属性根据getter方法
     * @param method
     * @return
     */
    public static String getPropertyByGetterMethod(Method method){
        String name = method.getName();
        if (name.startsWith(SystemConstant.GET_PREFIX)) {
            return firstCharLowCase(name.replace(SystemConstant.GET_PREFIX, StringUtils.EMPTY));
        }
        if (name.startsWith(SystemConstant.IS_PREFIX)){
            return firstCharLowCase(name.replace(SystemConstant.IS_PREFIX,StringUtils.EMPTY));
        }
        throw new RuntimeException("method must be getter method!");
    }

    /**
     * 首字母小写
     * @param str
     * @return
     */
    public static String firstCharLowCase(String str){
        if (StringUtils.isBlank(str)){
            return str;
        }
        if (str.length()==1) {
            return str.toLowerCase();
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
}
