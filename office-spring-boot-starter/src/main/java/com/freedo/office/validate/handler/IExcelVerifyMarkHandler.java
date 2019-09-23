package com.freedo.office.validate.handler;

import com.freedo.office.validate.result.ExcelVerifyMarkHandlerResult;

public interface IExcelVerifyMarkHandler<T> {

    /**
     * 导入校验方法
     * @param obj 当前对象
     * @param verifyGroup 检验分组，默认是{@link javax.validation.groups.Default}
     * @return
     */
    ExcelVerifyMarkHandlerResult verifyHandler(T obj, Class<?>... verifyGroup);

}