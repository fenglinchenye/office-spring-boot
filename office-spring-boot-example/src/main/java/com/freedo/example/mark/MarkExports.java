package com.freedo.example.mark;

import com.freedo.office.exports.ExcelMarkExportUtil;
import com.freedo.office.exports.param.MarkExportParams;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 标记导出
 */
public class MarkExports {

    public static void main(String[] args) throws IOException {
        MarkExportParams markExportParams = new MarkExportParams();
        // 开启标记 (默认关闭)
        markExportParams.setNeedMark(true);
        List<ExcelExportTempModel> list = new ArrayList<>();
        // 组装数据
        ExcelExportTempModel model = new ExcelExportTempModel();
        model.setId(1);
        model.setName("JACK");
        model.setErrorMsg("非法字符");
        // 设置标记属性
        Set<String> set = new HashSet<>();
        set.add("name");
        model.setMarkProperties(set);
        list.add(model);
        // 标记导出工具类
        Workbook workbook = ExcelMarkExportUtil.exportExcel(markExportParams, ExcelExportTempModel.class, list);
        OutputStream out = null;
        try {
            out = new FileOutputStream(new File("d:\\c.xlsx"));
            workbook.write(out);
        }finally {
            out.close();
        }
    }

}
