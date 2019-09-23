package com.freedo.office.exports.param;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.freedo.office.exports.MarkCellStyle;
import lombok.Data;

@Data
public class MarkExportParams extends ExportParams {

    /**
     * 是否需要标记导出 默认不需要
     */
    private boolean needMark = false;

    /**
     * 标记导出的单元格样式
     */
    private MarkCellStyle markCellStyle = new MarkCellStyle();

}
