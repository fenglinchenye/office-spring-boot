package com.freedo.office.exports;

import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.export.styler.IExcelExportStyler;
import cn.afterturn.easypoi.exception.excel.ExcelExportException;
import cn.afterturn.easypoi.exception.excel.enums.ExcelExportEnum;
import cn.afterturn.easypoi.util.PoiExcelGraphDataUtil;
import cn.afterturn.easypoi.util.PoiMergeCellUtil;
import cn.afterturn.easypoi.util.PoiPublicUtil;
import com.freedo.office.exports.param.MarkExportParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 标记导出Excel 服务
 * @author fredo
 */
@Slf4j
public class ExcelMarkExportService extends ExcelMarkBaseExportService {

    /**
     * 最大行数,超过自动多Sheet
     */
    private static int MAX_NUM = 60000;

    protected int createHeaderAndTitle(MarkExportParams entity, Sheet sheet, Workbook workbook,
                                       List<ExcelExportEntity> excelParams) {
        int rows = 0, fieldLength = getFieldLength(excelParams);
        if (entity.getTitle() != null) {
            rows += createTitle2Row(entity, sheet, workbook, fieldLength);
        }
        createHeaderRow(entity, sheet, workbook, rows, excelParams, 0);
        rows += getRowNums(excelParams, true);
        if (entity.isFixedTitle()) {
            sheet.createFreezePane(0, rows, 0, rows);
        }
        return rows;
    }

    /**
     * 创建表头
     */
    private int createHeaderRow(MarkExportParams title, Sheet sheet, Workbook workbook, int index,
                                List<ExcelExportEntity> excelParams, int cellIndex) {
        Row row = sheet.getRow(index) == null ? sheet.createRow(index) : sheet.getRow(index);
        int rows = getRowNums(excelParams, true);
        row.setHeight(title.getHeaderHeight());
        Row listRow = null;
        if (rows >= 2) {
            listRow = sheet.createRow(index + 1);
            listRow.setHeight(title.getHeaderHeight());
        }
        int groupCellLength = 0;
        CellStyle titleStyle = getExcelExportStyler().getTitleStyle(title.getColor());
        for (int i = 0, exportFieldTitleSize = excelParams.size(); i < exportFieldTitleSize; i++) {
            ExcelExportEntity entity = excelParams.get(i);
            // 加入换了groupName或者结束就，就把之前的那个换行
            if (StringUtils.isBlank(entity.getGroupName()) || !entity.getGroupName().equals(excelParams.get(i - 1).getGroupName())) {
                if (groupCellLength > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(index, index, cellIndex - groupCellLength, cellIndex - 1));
                }
                groupCellLength = 0;
            }
            if (StringUtils.isNotBlank(entity.getGroupName())) {
                createStringCell(row, cellIndex, entity.getGroupName(), titleStyle, entity);
                createStringCell(listRow, cellIndex, entity.getName(), titleStyle, entity);
                groupCellLength++;
            } else if (StringUtils.isNotBlank(entity.getName())) {
                createStringCell(row, cellIndex, entity.getName(), titleStyle, entity);
            }
            if (entity.getList() != null) {
                // 保持原来的
                int tempCellIndex = cellIndex;
                cellIndex = createHeaderRow(title, sheet, workbook, rows == 1 ? index : index + 1, entity.getList(), cellIndex);
                List<ExcelExportEntity> sTitel = entity.getList();
                if (StringUtils.isNotBlank(entity.getName()) && sTitel.size() > 1) {
                    PoiMergeCellUtil.addMergedRegion(sheet, index, index, tempCellIndex, tempCellIndex + sTitel.size() - 1);
                }
                cellIndex--;
            } else if (rows > 1 && StringUtils.isBlank(entity.getGroupName())) {
                createStringCell(listRow, cellIndex, "", titleStyle, entity);
                PoiMergeCellUtil.addMergedRegion(sheet, index, index + rows - 1, cellIndex, cellIndex);
            }
            cellIndex++;
        }
        if (groupCellLength > 1) {
            PoiMergeCellUtil.addMergedRegion(sheet, index, index, cellIndex - groupCellLength, cellIndex - 1);
        }
        return cellIndex;
    }

    /**
     * 创建 表头改变
     */
    public int createTitle2Row(MarkExportParams entity, Sheet sheet, Workbook workbook,
                               int fieldWidth) {

        Row row = sheet.createRow(0);
        row.setHeight(entity.getTitleHeight());
        createStringCell(row, 0, entity.getTitle(),
                getExcelExportStyler().getHeaderStyle(entity.getHeaderColor()), null);
        for (int i = 1; i <= fieldWidth; i++) {
            createStringCell(row, i, "",
                    getExcelExportStyler().getHeaderStyle(entity.getHeaderColor()), null);
        }
        PoiMergeCellUtil.addMergedRegion(sheet, 0, 0, 0, fieldWidth);
        if (entity.getSecondTitle() != null) {
            row = sheet.createRow(1);
            row.setHeight(entity.getSecondTitleHeight());
            CellStyle style = workbook.createCellStyle();
            style.setAlignment(HorizontalAlignment.RIGHT);
            createStringCell(row, 0, entity.getSecondTitle(), style, null);
            for (int i = 1; i <= fieldWidth; i++) {
                createStringCell(row, i, "",
                        getExcelExportStyler().getHeaderStyle(entity.getHeaderColor()), null);
            }
            PoiMergeCellUtil.addMergedRegion(sheet, 1, 1, 0, fieldWidth);
            return 2;
        }
        return 1;
    }

    /**
     * 创建Sheet页
     * @param workbook {@link Workbook} 工作簿
     * @param entity {@link MarkExportParams} 标记参数
     * @param pojoClass
     * @param dataSet {@link Collection} 数据集
     */
    public void createSheet(Workbook workbook, MarkExportParams entity, Class<?> pojoClass,
                            Collection<?> dataSet) {
        if (log.isDebugEnabled()) {
            log.debug("Excel export start ,class is {}", pojoClass);
            log.debug("Excel version is {}",
                    entity.getType().equals(ExcelType.HSSF) ? "03" : "07");
        }
        if (workbook == null || entity == null || pojoClass == null || dataSet == null) {
            throw new ExcelExportException(ExcelExportEnum.PARAMETER_ERROR);
        }
        try {
            List<ExcelExportEntity> excelParams = new ArrayList<>();
            // 得到所有字段 (包括父类)
            Field[] fileds = PoiPublicUtil.getClassFields(pojoClass);
            ExcelTarget etarget = pojoClass.getAnnotation(ExcelTarget.class);
            String targetId = etarget == null ? null : etarget.value();
            getAllExcelField(entity.getExclusions(), targetId, fileds, excelParams, pojoClass,
                    null, null);
            //获取所有参数后,后面的逻辑判断就一致了
            createSheetForMap(workbook, entity, excelParams, dataSet);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, e.getCause());
        }
    }

    public void createSheetForMap(Workbook workbook, MarkExportParams entity,
                                  List<ExcelExportEntity> entityList, Collection<?> dataSet) {
        if (log.isDebugEnabled()) {
            log.debug("Excel version is {}",
                    entity.getType().equals(ExcelType.HSSF) ? "03" : "07");
        }
        if (workbook == null || entity == null || entityList == null || dataSet == null) {
            throw new ExcelExportException(ExcelExportEnum.PARAMETER_ERROR);
        }
        super.type = entity.getType();
        if (type.equals(ExcelType.XSSF)) {
            MAX_NUM = 1000000;
        }
        if (entity.getMaxNum() > 0) {
            MAX_NUM = entity.getMaxNum();
        }
        Sheet sheet;
        try {
            sheet = workbook.createSheet(entity.getSheetName());
        } catch (Exception e) {
            // 重复遍历,出现了重名现象,创建非指定的名称Sheet
            sheet = workbook.createSheet();
        }
        // 填充Sheet 页面数据
        insertDataToSheet(workbook, entity, entityList, dataSet, sheet);
    }

    /**
     * 插入数据到指定Sheet 页
     * @param workbook {@link Workbook}工作簿
     * @param entity {@link MarkExportParams} 标记导出参数
     * @param entityList {@link List<ExcelExportEntity>} 导出实体类
     * @param dataSet {@link Collection} 数据集
     * @param sheet {@link Sheet} 当前Sheet 页
     */
    protected void insertDataToSheet(Workbook workbook, MarkExportParams entity,
                                     List<ExcelExportEntity> entityList, Collection<?> dataSet,
                                     Sheet sheet) {
        try {
            dataHandler = entity.getDataHandler();
            if (dataHandler != null && dataHandler.getNeedHandlerFields() != null) {
                needHandlerList = Arrays.asList(dataHandler.getNeedHandlerFields());
            }
            dictHandler = entity.getDictHandler();
            i18nHandler = entity.getI18nHandler();
            // 创建表格样式(表格基本样式)
            setExcelExportStyler((IExcelExportStyler) entity.getStyle()
                    .getConstructor(Workbook.class).newInstance(workbook));
            //获取画布
            Drawing patriarch = PoiExcelGraphDataUtil.getDrawingPatriarch(sheet);
            List<ExcelExportEntity> excelParams = new ArrayList<>();
            if (entity.isAddIndex()) {
                excelParams.add(indexExcelEntity(entity));
            }
            excelParams.addAll(entityList);
            sortAllParams(excelParams);
            // 用于标识已经写入excel 的数据下标;
            int index = entity.isCreateHeadRows()
                    ? createHeaderAndTitle(entity, sheet, workbook, excelParams) : 0;
            int titleHeight = index;
            setCellWith(excelParams, sheet);
            setColumnHidden(excelParams, sheet);
            short rowHeight = entity.getHeight() != 0 ? entity.getHeight() : getRowHeight(excelParams);
            setCurrentIndex(1);
            Iterator<?> its = dataSet.iterator();
            List<Object> tempList = new ArrayList<>();
            while (its.hasNext()) {
                Object t = its.next();
                // 创建一行数据的单元格
                index += createCells(patriarch, index, t, excelParams, sheet, workbook, rowHeight, 0,entity.getMarkCellStyle())[0];
                tempList.add(t);
                // 数据行数超出最大限制时进行跳出
                if (index >= MAX_NUM) {
                    break;
                }
            }
            if (entity.getFreezeCol() != 0) {
                sheet.createFreezePane(entity.getFreezeCol(), 0, entity.getFreezeCol(), 0);
            }
            // 合并单元格
            mergeCells(sheet, excelParams, titleHeight);

            its = dataSet.iterator();
            for (int i = 0, le = tempList.size(); i < le; i++) {
                its.next();
                its.remove();
            }
            if (log.isDebugEnabled()) {
                log.debug("List data more than max ,data size is {}",
                        dataSet.size());
            }
            // 发现还有剩余list 继续循环创建Sheet
            if (dataSet.size() > 0) {
                createSheetForMap(workbook, entity, entityList, dataSet);
            } else {
                // 创建合计信息
                addStatisticsRow(getExcelExportStyler().getStyles(true, null), sheet);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExcelExportException(ExcelExportEnum.EXPORT_ERROR, e.getCause());
        }
    }

}
