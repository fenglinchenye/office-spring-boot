package com.freedo.office.exports;

import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.export.ExcelExportService;
import com.freedo.office.basic.SystemConstant;
import com.freedo.office.exports.param.MarkExportParams;
import com.freedo.office.validate.inter.MarkExcelModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * 标记导出excel
 * @author freedo
 */
@Slf4j
public class ExcelMarkExportUtil {

    /**
     * 导出Excel (标记/非标记)
     * @param entity {@link MarkExportParams} 标记参数实体
     * @param pojoClass 数据集类
     * @param dataSet {@link Collection}数据集
     * @return {@link Workbook} 工作簿
     */
    public static Workbook exportExcel(MarkExportParams entity, Class<?> pojoClass,
                                       Collection<?> dataSet) {
        boolean notNeedMark = !entity.isNeedMark()|| CollectionUtils.isEmpty(dataSet)||(!(CollectionUtils.isEmpty(dataSet))&&!(dataSet.stream().findFirst().get() instanceof MarkExcelModel));
        // 获取workshop 工作簿
        Workbook workbook = getWorkbook(entity.getType(),dataSet.size());
        if (notNeedMark){
            new ExcelExportService().createSheet(workbook, entity, pojoClass, dataSet);
            return workbook;
        }else{
            //  执行标记导出
            new ExcelMarkExportService().createSheet(workbook,entity,pojoClass,dataSet);
            return workbook;
        }
    }

    /**
     * 获取工作簿
     * @param type
     * @param size
     * @return
     */
    private static Workbook getWorkbook(ExcelType type, int size) {
        if (ExcelType.HSSF.equals(type)) {
            return new HSSFWorkbook();
        } else if (size < SystemConstant.EXCEL_LIMIT_NUM) {
            return new XSSFWorkbook();
        } else {
            return new SXSSFWorkbook();
        }
    }

}
