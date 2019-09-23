package com.freedo.office.validate.handler.impl;

import com.freedo.office.basic.SystemConstant;
import com.freedo.office.util.BeanUtils;
import com.freedo.office.validate.annotation.Order;
import com.freedo.office.validate.annotation.PrimaryIndex;
import com.freedo.office.validate.annotation.PrimaryIndexes;
import com.freedo.office.validate.handler.IExcelVerifyMarkHandler;
import com.freedo.office.validate.result.ExcelVerifyMarkHandlerResult;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带标记的唯一索引校验
 */
@Order
public class PrimaryIndexVerifyMarkHandler implements IExcelVerifyMarkHandler<Object> {

    private final Map<String,Set<String>> INDEX_MAP = new ConcurrentHashMap<>();

    @Override
    public ExcelVerifyMarkHandlerResult verifyHandler(Object obj, Class<?>... verifyGroup) {
        PrimaryIndexes primaryIndexes = obj.getClass().getAnnotation(PrimaryIndexes.class);
        PrimaryIndex[] primaryIndexArray;
        if (Objects.isNull(primaryIndexes)||primaryIndexes.value().length==0){
            PrimaryIndex primaryIndex = obj.getClass().getAnnotation(PrimaryIndex.class);
            // 如果两者注解都不存在设定时，默认不进行唯一索引校验。直接返回
            if (Objects.isNull(primaryIndex)){
                return new ExcelVerifyMarkHandlerResult(true, Collections.EMPTY_SET);
            }
            primaryIndexArray = new PrimaryIndex[]{primaryIndex};
        }else{
            primaryIndexArray = primaryIndexes.value();
        }
        // 获取对应属性值
        StringJoiner errorMessageJoiner = new StringJoiner(SystemConstant.COMMA);
        Set<String> markPropertySet = new HashSet<>();
        for (PrimaryIndex primaryIndex : primaryIndexArray) {
            String[] fields = primaryIndex.fields();
            boolean isBreak = false;
            StringJoiner valueJoiner = new StringJoiner(SystemConstant.UNDERLINE);
            StringJoiner indexJoiner = new StringJoiner(SystemConstant.UNDERLINE);
            for (String field : fields) {
                // 获取属性值
                Object value = BeanUtils.getValueByProperty(field, obj);
                // 如果此索引存在null 对象值直接跳过。因此此类建议加上@NotNull 配合校验
                if (Objects.isNull(value)){
                    isBreak = true;
                }else{
                    valueJoiner.add(value.toString());
                    indexJoiner.add(field);
                }
            }
            // 如果中断此条验证，直接继续下一个联合索引校验
            if (isBreak){
                continue;
            }else{
                String indexValue = valueJoiner.toString();
                String index = indexJoiner.toString();
                //判别是否正确
                // 单一数据情况
                Set<String> indexValueSet = INDEX_MAP.getOrDefault(index, new HashSet<>());
                if(primaryIndex.isSingle()){
                    if (!indexValueSet.contains(indexValue)&&indexValueSet.size()!=0){
                        errorMessageJoiner.add(primaryIndex.message());
                        // 添加标记字段
                        markPropertySet.addAll(Sets.newHashSet(primaryIndex.fields()));
                    }else{
                        indexValueSet.add(indexValue);
                        INDEX_MAP.put(index,indexValueSet);
                    }
                }else{
                    if (indexValueSet.contains(indexValue)){
                        errorMessageJoiner.add(primaryIndex.message());
                        // 添加标记字段
                        markPropertySet.addAll(Sets.newHashSet(primaryIndex.fields()));
                    } else {
                        indexValueSet.add(indexValue);
                        INDEX_MAP.put(index,indexValueSet);
                    }
                }
            }
        }
        // 返回错误信息
        if (errorMessageJoiner.length()!=0){
            return new ExcelVerifyMarkHandlerResult(false,errorMessageJoiner.toString(),markPropertySet);
        }
        return new ExcelVerifyMarkHandlerResult(true,Collections.EMPTY_SET);
    }
}
