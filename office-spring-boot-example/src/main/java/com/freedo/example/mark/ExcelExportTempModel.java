package com.freedo.example.mark;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.freedo.office.validate.inter.MarkExcelModel;
import lombok.Data;

import java.util.Set;

@Data
public class ExcelExportTempModel implements MarkExcelModel {

    @Excel(name = "id")
    private Integer id;

    @Excel(name = "姓名")
    private String name;

    private Set<String> markProperties;

    @Excel(name = "异常信息")
    private String errorMsg;

}
