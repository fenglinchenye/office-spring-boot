package com.freedo.office.validate.inter;

import cn.afterturn.easypoi.handler.inter.IExcelModel;

import java.util.Set;

/**
 * 校验excelModel 接口
 * @author freedo
 */
public interface MarkExcelModel extends IExcelModel {

    /**
     * 不合法的属性
     * @return
     */
    Set<String> getMarkProperties();

    /**
     * 设置不合法的属性
     * @param properties 不合法属性
     */
    void setMarkProperties(Set<String> properties);

}
