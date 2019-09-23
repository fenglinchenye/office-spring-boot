package com.freedo.office.exports;

import lombok.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

/**
 * 标记单元格样式
 * @author freedo
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class MarkCellStyle {

    /**
     * 字体颜色 默认是红色
     */
    private short fontColor = HSSFColor.HSSFColorPredefined.RED.getIndex();

    /**
     * 背景色 默认采用白色
     */
    private short backgroundColor = HSSFColor.HSSFColorPredefined.WHITE.getIndex();

    /**
     * 字体是否加粗
     */
    private boolean fontBold = false;

    /**
     * 是否使用斜体
     */
    private boolean fontItalic = false;

    /**
     * 字体下划线格式 默认无下划线
     * {@link FontUnderlineStyle}
     */
    private FontUnderlineStyle fontUnderlineStyle = FontUnderlineStyle.NONE;

    /**
     * 下划线格式
     */
    @AllArgsConstructor
    @Getter
    enum FontUnderlineStyle{

        NONE((byte)0,"无下划线"),
        NORMAL((byte)1,"正常下划线"),
        DOUBLE((byte)2,"双下划线"),
        NORMAL_ACCOUNTING((byte)0x21,"会计风格单一下划线"),
        DOUBLE_ACCOUNTING((byte)0x22,"会计风格双下划线"),
        ;
        private byte code;
        private String message;
    }

    /**
     * 转换到CellStyle属性
     * @return
     */
    public final CellStyle toCellStyle(Workbook workbook){
        // 通过WorkBook 创建CellStyle
        CellStyle markCellStyle = workbook.createCellStyle();
        // 设置背景色
        markCellStyle.setFillBackgroundColor(backgroundColor);
        //普通数据单元格字体
        Font markDataFont = workbook.createFont();
        // 设置字体加粗
        markDataFont.setBold(fontBold);
        // 设置字体颜色
        markDataFont.setColor(fontColor);
        // 设置字体倾斜
        markDataFont.setItalic(fontItalic);
        // 设置字体下划线
        markDataFont.setUnderline(fontUnderlineStyle.code);
        //把字体应用到当前的样式
        markCellStyle.setFont(markDataFont);
        // 设置居中
        markCellStyle.setAlignment(HorizontalAlignment.CENTER);
        markCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return markCellStyle;
    }

}
