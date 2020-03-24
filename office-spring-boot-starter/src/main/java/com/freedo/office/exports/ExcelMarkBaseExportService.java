package com.freedo.office.exports;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.vo.BaseEntityTypeConstants;
import cn.afterturn.easypoi.excel.entity.vo.PoiBaseConstants;
import cn.afterturn.easypoi.excel.export.base.BaseExportService;
import cn.afterturn.easypoi.exception.excel.ExcelExportException;
import cn.afterturn.easypoi.exception.excel.enums.ExcelExportEnum;
import cn.afterturn.easypoi.util.PoiMergeCellUtil;
import com.freedo.office.basic.SystemConstant;
import com.freedo.office.util.BeanUtils;
import com.freedo.office.validate.inter.MarkExcelModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.poi.ss.usermodel.*;

import java.util.*;

/**
 * 标记导出ExcelBaseService
 * @author freedo
 */
@Slf4j
public abstract class ExcelMarkBaseExportService extends BaseExportService {

    private int currentIndex = 0;

    /**
     * 创建单元格
     * @param patriarch {@link Drawing}Sheet页的画布
     * @param index 下标
     * @param t 数据类
     * @param excelParams {@link List<ExcelExportEntity>} 导出参数
     * @param sheet {@link Sheet} Sheet页
     * @param workbook {@link Workbook} 工作簿
     * @param rowHeight 行高
     * @param cellNum 单元格数目
     * @param markCellStyle {@link MarkCellStyle} 标记单元格的样式
     * @return
     */
    public int[] createCells(Drawing patriarch,
                             Integer index,
                             Object t,
                             List<ExcelExportEntity> excelParams,
                             Sheet sheet,
                             Workbook workbook,
                             short rowHeight,
                             Integer cellNum,
                             MarkCellStyle markCellStyle) {
        try {
            // 获取被标记的属性
            Set<String> markPropertySet = Collections.EMPTY_SET;
            if (t instanceof MarkExcelModel){
                MarkExcelModel model = (MarkExcelModel) t;
                markPropertySet = model.getMarkProperties();
            }
            ExcelExportEntity entity;
            Row row = sheet.getRow(index) == null ? sheet.createRow(index) : sheet.getRow(index);
            if (rowHeight != -1) {
                row.setHeight(rowHeight);
            }
            int maxHeight = 1;
            // 合并需要合并的单元格
            int margeCellNum = cellNum;
            // 创建下标单元格 (包括标记单元格样式)
            Integer indexKey = createIndexCell(row, index, excelParams.get(0),workbook,markPropertySet,markCellStyle);
            cellNum += indexKey;
            for (int k = indexKey, paramSize = excelParams.size(); k < paramSize; k++) {
                entity = excelParams.get(k);
                if (entity.getList() != null) {
                    Collection<?> list = getListCellValue(entity, t);
                    if (list != null && list.size() > 0) {
                        int tempCellNum = 0;
                        for (Object obj : list) {
                            int[] temp = createCells(patriarch, index + maxHeight - 1, obj, entity.getList(), sheet, workbook, rowHeight, cellNum,markCellStyle);
                            tempCellNum = temp[1];
                            maxHeight += temp[0];
                        }
                        cellNum = tempCellNum;
                        maxHeight--;
                    }
                } else {
                    Object value = getCellValue(entity, t);
                    // 字符串类型
                    if (entity.getType() == BaseEntityTypeConstants.STRING_TYPE) {
                        createStringCell(row, index,entity,value,cellNum,markPropertySet,markCellStyle,workbook);
                        cellNum++;
                        if (entity.isHyperlink()) {
                            row.getCell(cellNum - 1)
                                    .setHyperlink(dataHandler.getHyperlink(
                                            row.getSheet().getWorkbook().getCreationHelper(), t,
                                            entity.getName(), value));
                        }
                        // 创建double 类型的单元格
                    } else if (entity.getType() == BaseEntityTypeConstants.DOUBLE_TYPE) {
                        createDoubleCell(row, index,entity,value,cellNum,markPropertySet,markCellStyle,workbook);
                        cellNum++;
                        if (entity.isHyperlink()) {
                            row.getCell(cellNum - 1).setHyperlink(dataHandler.getHyperlink(
                                    row.getSheet().getWorkbook().getCreationHelper(), t,
                                    entity.getName(), value));
                        }
                    } else {
                        createImageCell(patriarch, entity, row, cellNum,
                                value == null ? "" : value.toString(), t);
                        cellNum++;
                    }
                }
            }
            for (int k = indexKey, paramSize = excelParams.size(); k < paramSize; k++) {
                entity = excelParams.get(k);
                if (entity.getList() != null) {
                    margeCellNum += entity.getList().size();
                } else if (entity.isNeedMerge() && maxHeight > 1) {
                    for (int i = index + 1; i < index + maxHeight; i++) {
                        sheet.getRow(i).createCell(margeCellNum);
                        sheet.getRow(i).getCell(margeCellNum).setCellStyle(getStyles(false, entity));
                    }
                    PoiMergeCellUtil.addMergedRegion(sheet, index, index + maxHeight - 1, margeCellNum, margeCellNum);
                    margeCellNum++;
                }
            }
            return new int[]{maxHeight, cellNum};
        } catch (Exception e) {
            log.error("excel cell export error ,data is :{}", ReflectionToStringBuilder.toString(t));
            log.error(e.getMessage(), e);
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, e);
        }
    }

    /**
     * 创建序号单元格
     * @param row
     * @param index
     * @param excelExportEntity
     * @return
     */
    private int createIndexCell(Row row, int index, ExcelExportEntity excelExportEntity,Workbook workbook,Set<String> markPropertySet, MarkCellStyle markCellStyle) {
        boolean flag = Objects.isNull(excelExportEntity.getName()) &&
                SystemConstant.SEQUENCE_NO_CHINESE.equals(excelExportEntity.getName()) &&
                excelExportEntity.getFormat() != null &&
                excelExportEntity.getFormat().equals(PoiBaseConstants.IS_ADD_INDEX);
        if (flag) {

            // 字段是否被标记
            if (!markPropertySet.contains(BeanUtils.getPropertyByGetterMethod(excelExportEntity.getMethod()))){
                createStringCell(row, 0, currentIndex + StringUtils.EMPTY,
                        index % 2 == 0 ? getStyles(false, null) : getStyles(true, null), null);
            }else{
                createStringCell(row, 0, currentIndex + StringUtils.EMPTY,
                        markCellStyle.toCellStyle(workbook), null);
            }
            currentIndex = currentIndex + 1;
            return 1;
        }
        return 0;
    }

    /**
     * 创建字符串类型单元格
     * @param row {@link Row}行标
     * @param index 下标
     * @param entity 导出实体
     * @param cellNum
     * @param markPropertySet 标记属性集合
     * @param value 单元格值
     * @param markCellStyle {@link MarkCellStyle} 标记单元格的样式
     * @param workbook {@link Workbook} 工作簿
     */
    public void createStringCell(Row row, Integer index,
                                 ExcelExportEntity entity,
                                 Object value,
                                 Integer cellNum,
                                 Set<String> markPropertySet,
                                 MarkCellStyle markCellStyle,
                                 Workbook workbook) {
        // 标记字段包含方法
        if (!markPropertySet.contains(BeanUtils.getPropertyByGetterMethod(entity.getMethod()))) {
            createStringCell(row, cellNum, Objects.isNull(value) ? StringUtils.EMPTY : value.toString(),
                    index % 2 == 0 ? getStyles(false, entity) : getStyles(true, entity),
                    entity);

        } else {
            createStringCell(row, cellNum, Objects.isNull(value) ? StringUtils.EMPTY : value.toString(),
                    markCellStyle.toCellStyle(workbook),
                    entity);
        }
    }

    /**
     * 创建Double 类型的单元格
     * @param row {@link Row}行标
     * @param index 下标
     * @param entity 导出实体
     * @param cellNum
     * @param markPropertySet 标记属性集合
     * @param value 单元格值
     * @param markCellStyle {@link MarkCellStyle} 标记单元格的样式
     * @param workbook {@link Workbook} 工作簿
     */
    public void createDoubleCell(Row row, Integer index,
                                 ExcelExportEntity entity,
                                 Object value,
                                 Integer cellNum,
                                 Set<String> markPropertySet,
                                 MarkCellStyle markCellStyle,
                                 Workbook workbook) {
        // 字段是否被标记
        if (!markPropertySet.contains(BeanUtils.getPropertyByGetterMethod(entity.getMethod()))) {
            createDoubleCell(row, cellNum, Objects.isNull(value) ? StringUtils.EMPTY : value.toString(),
                    index % 2 == 0 ? getStyles(false, entity) : getStyles(true, entity),
                    entity);
        } else {
            createDoubleCell(row, cellNum, Objects.isNull(value) ? StringUtils.EMPTY : value.toString(),
                    markCellStyle.toCellStyle(workbook),
                    entity);
        }
    }

}
