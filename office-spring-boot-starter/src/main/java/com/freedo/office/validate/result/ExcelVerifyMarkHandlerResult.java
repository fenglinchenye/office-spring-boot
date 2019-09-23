package com.freedo.office.validate.result;

import cn.afterturn.easypoi.excel.entity.result.ExcelVerifyHandlerResult;
import lombok.Data;

import java.util.Set;

/**
 * 校验标记结果
 * @author freedo
 */
@Data
public class ExcelVerifyMarkHandlerResult extends ExcelVerifyHandlerResult {

    /**
     * 不合法字段属性集合
     */
    private Set<String> markPropertySet;

    public ExcelVerifyMarkHandlerResult(boolean success, Set<String> markPropertySet) {
        super(success);
        this.markPropertySet = markPropertySet;
    }

    public ExcelVerifyMarkHandlerResult(boolean success, String msg, Set<String> markPropertySet) {
        super(success, msg);
        this.markPropertySet = markPropertySet;
    }
}
