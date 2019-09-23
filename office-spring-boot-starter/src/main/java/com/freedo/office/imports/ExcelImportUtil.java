package com.freedo.office.imports;

import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.afterturn.easypoi.exception.excel.ExcelImportException;
import com.freedo.office.imports.param.ImportParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@Slf4j
public class ExcelImportUtil {

    private ExcelImportUtil() {
    }

    /**
     * Excel 导入 数据源本地文件,不返回校验结果 导入 字 段类型 Integer,Long,Double,Date,String,Boolean
     * 
     * @param file
     * @param pojoClass
     * @param params
     * @return
     */
    public static <T> List<T> importExcel(File file, Class<?> pojoClass, ImportParams params) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return new ExcelImportService().importExcelByIs(in, pojoClass, params, false).getList();
        } catch (ExcelImportException e) {
            throw new ExcelImportException(e.getType(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExcelImportException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Excel 导入 数据源IO流,不返回校验结果 导入 字段类型 Integer,Long,Double,Date,String,Boolean
     * 
     * @param inputstream
     * @param pojoClass
     * @param params
     * @return
     * @throws Exception
     */
    public static <T> List<T> importExcel(InputStream inputstream, Class<?> pojoClass,
                                          ImportParams params) throws Exception {
        return new ExcelImportService().importExcelByIs(inputstream, pojoClass, params, false).getList();
    }

    /**
     * Excel 导入 数据源IO流 字段类型 Integer,Long,Double,Date,String,Boolean
     * 支持校验,支持Key-Value
     * 
     * @param inputstream
     * @param pojoClass
     * @param params
     * @return
     * @throws Exception
     */
    public static <T> ExcelImportResult<T> importExcelMore(InputStream inputstream,
                                                           Class<?> pojoClass,
                                                           ImportParams params) throws Exception {
        return new ExcelImportService().importExcelByIs(inputstream, pojoClass, params, true);
    }

    /**
     * Excel 导入 数据源本地文件 字段类型 Integer,Long,Double,Date,String,Boolean
     * 支持校验,支持Key-Value
     * @param file
     * @param pojoClass
     * @param params
     * @return
     */
    public static <T> ExcelImportResult<T> importExcelMore(File file, Class<?> pojoClass,
                                                             ImportParams params) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return new ExcelImportService().importExcelByIs(in, pojoClass, params, true);
        } catch (ExcelImportException e) {
            throw new ExcelImportException(e.getType(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ExcelImportException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
