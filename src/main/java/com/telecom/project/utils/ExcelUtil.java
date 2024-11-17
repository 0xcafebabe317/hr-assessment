package com.telecom.project.utils;

import com.telecom.project.model.entity.ContractsScore;
import com.telecom.project.model.entity.PerformanceContracts;
import com.telecom.project.model.vo.ExcelVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExcelUtil {
    public static void safeMerge(Sheet sheet, int startRow, int endRow, int col, CellStyle style) {
        if (startRow != endRow) {  // 确保开始行和结束行不同，才进行合并
            sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, col, col));

            // 为所有合并的单元格应用样式
            for (int j = startRow; j <= endRow; j++) {
                Row mergedRow = sheet.getRow(j);
                if (mergedRow != null) {
                    Cell cell = mergedRow.getCell(col);
                    if (cell != null) {
                        cell.setCellStyle(style); // 设置边框样式
                    }
                }
            }
        }
    }

    public static ByteArrayOutputStream exportUsersToExcel(List<ExcelVO> excelData) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // 根据 assessed_unit 进行分组
        Map<String, List<ExcelVO>> groupedData = excelData.stream()
                .collect(Collectors.groupingBy(ExcelVO::getAssessed_unit));

        // 表头
        String[] headers = {
                "大类名称", "小类名称", "指标", "考核部门", "权重", "记分方法", "考核周期",
                "被考核单位", "被考核中心", "被考核人", "其他", "得分", "考核时间", "评分人"
        };

        // 创建样式用于换行+边框
        CellStyle wrapTextWithBorderStyle = workbook.createCellStyle();
        wrapTextWithBorderStyle.setWrapText(true);  // 启用换行
        wrapTextWithBorderStyle.setBorderTop(BorderStyle.THIN);
        wrapTextWithBorderStyle.setBorderRight(BorderStyle.THIN);
        wrapTextWithBorderStyle.setBorderBottom(BorderStyle.THIN);
        wrapTextWithBorderStyle.setBorderLeft(BorderStyle.THIN);

        // 创建样式用于表头
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER); // 中心对齐
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        headerStyle.setWrapText(false); // 禁用换行

        // 创建样式用于左对齐（用于其他列）
        CellStyle leftAlignStyle = workbook.createCellStyle();
        leftAlignStyle.setAlignment(HorizontalAlignment.LEFT); // 左对齐
        leftAlignStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
        leftAlignStyle.setBorderTop(BorderStyle.THIN);
        leftAlignStyle.setBorderRight(BorderStyle.THIN);
        leftAlignStyle.setBorderBottom(BorderStyle.THIN);
        leftAlignStyle.setBorderLeft(BorderStyle.THIN);

        // 创建样式用于表头边框
        CellStyle headerBorderStyle = workbook.createCellStyle();
        headerBorderStyle.setAlignment(HorizontalAlignment.CENTER);
        headerBorderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerBorderStyle.setBorderTop(BorderStyle.THIN);
        headerBorderStyle.setBorderRight(BorderStyle.THIN);
        headerBorderStyle.setBorderBottom(BorderStyle.THIN);
        headerBorderStyle.setBorderLeft(BorderStyle.THIN);

        // 为每个 assessed_unit 创建一个 sheet
        for (Map.Entry<String, List<ExcelVO>> entry : groupedData.entrySet()) {
            String sheetName = entry.getKey();
            List<ExcelVO> unitData = entry.getValue();

            // 创建新表单
            Sheet sheet = workbook.createSheet(sheetName);
            sheet.protectSheet("xzlzdx.telecom");  // 设置密码保护，防止修改

            // 填充表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerBorderStyle); // 应用表头样式
            }

            // 设置表头行高
            headerRow.setHeightInPoints(20); // 设置行高为20个点

            // 设置表头列宽
            int[] columnWidths = {22, 22, 25, 15, 8, 30, 10, 15, 15, 12, 10, 10, 15, 13}; // 示例宽度
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i] * 256); // 设置列宽
            }

            // 填充数据并合并单元格
            String lastCategory = null;
            String lastSubCategory = null;
            int categoryRowStart = -1;
            int subCategoryRowStart = -1;

            // 填充数据并应用样式
            for (int i = 0; i < unitData.size(); i++) {
                Row row = sheet.createRow(i + 1);
                ExcelVO data = unitData.get(i);

                // 检查类别
                if (lastCategory == null || !lastCategory.equals(data.getCategories())) {
                    lastCategory = data.getCategories();
                    row.createCell(0).setCellValue(lastCategory);
                    if (categoryRowStart >= 0) {
                        // 合并上一个类别单元格
                        safeMerge(sheet, categoryRowStart, i, 0, headerBorderStyle); // 使用安全的合并方法
                    }
                    categoryRowStart = i + 1; // 下一行开始
                } else {
                    row.createCell(0); // 空白单元格
                }

                // 检查子类别
                if (lastSubCategory == null || !lastSubCategory.equals(data.getSub_categories())) {
                    lastSubCategory = data.getSub_categories();
                    row.createCell(1).setCellValue(lastSubCategory);
                    if (subCategoryRowStart >= 0) {
                        // 合并上一个子类别单元格
                        safeMerge(sheet, subCategoryRowStart, i, 1, headerBorderStyle); // 使用安全的合并方法
                    }
                    subCategoryRowStart = i + 1; // 下一行开始
                } else {
                    row.createCell(1); // 空白单元格
                }

                // 填充其他数据并应用样式
                for (int j = 2; j < headers.length; j++) {
                    Cell cell = row.createCell(j);
                    String value = "";
                    switch (j) {
                        case 2:
                            value = data.getIndicators();
                            break;
                        case 3:
                            value = data.getAssessment_dept();
                            break;
                        case 4:
                            value = data.getWeight() != null ? String.valueOf(data.getWeight()) : "0";
                            break;
                        case 5:
                            value = data.getScoring_method(); // 记分方法
                            cell.setCellStyle(wrapTextWithBorderStyle); // 记分方法应用换行和边框样式
                            break;
                        case 6:
                            value = data.getAssessment_cycle();
                            break;
                        case 7:
                            value = data.getAssessed_unit();
                            break;
                        case 8:
                            value = data.getAssessed_center();
                            break;
                        case 9:
                            value = data.getAssessed_people();
                            break;
                        case 10:
                            value = data.getOther();
                            break;
                        case 11:
                            value = data.getScore() != null ? String.valueOf(data.getScore()) : "0.0";
                            break;
                        case 12:
                            value = data.getAssessment_time();
                            break;
                        case 13:
                            value = data.getAssessment_people();
                            break;
                    }
                    cell.setCellValue(value);

                    // 对于非记分方法的其他列应用左对齐样式
                    if (j != 5) {
                        cell.setCellStyle(leftAlignStyle);
                    }

                    // 为每个数据单元格添加边框
                    if (j != 5) {
                        cell.setCellStyle(leftAlignStyle); // 确保每个单元格都有边框
                    }
                }
            }

            // 合并最后一组合并并添加边框
            if (categoryRowStart >= 0 && categoryRowStart < unitData.size()) {
                safeMerge(sheet, categoryRowStart, unitData.size(), 0, headerBorderStyle); // 使用安全的合并方法
            }
            if (subCategoryRowStart >= 0 && subCategoryRowStart < unitData.size()) {
                safeMerge(sheet, subCategoryRowStart, unitData.size(), 1, headerBorderStyle); // 使用安全的合并方法
            }
        }

        // 将数据写入输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    public static List<ExcelVO> convertToExcelVO(List<ContractsScore> contractsScores, List<PerformanceContracts> performanceContracts) {
        // 将 PerformanceContracts 按 categories 和 sub_categories 排序
        List<PerformanceContracts> sortedContracts = performanceContracts.stream()
                .sorted(Comparator.comparing(PerformanceContracts::getCategories)
                        .thenComparing(PerformanceContracts::getSub_categories))
                .collect(Collectors.toList());

        // 将排序后的 PerformanceContracts 转换为 Map，方便根据 contract_id 获取数据
        Map<Long, PerformanceContracts> contractsMap = sortedContracts.stream()
                .collect(Collectors.toMap(PerformanceContracts::getId, contract -> contract, (existing, replacement) -> existing));

        // 将 ContractsScore 和排序后的 PerformanceContracts 合并到 ExcelVO
        return contractsScores.stream()
                .map(score -> {
                    PerformanceContracts contract = contractsMap.get(score.getContract_id());

                    if (contract == null) {
                        return null; // 如果没有匹配到对应的合同，跳过
                    }

                    ExcelVO excelVO = new ExcelVO();
                    excelVO.setCategories(contract.getCategories());
                    excelVO.setSub_categories(contract.getSub_categories());
                    excelVO.setIndicators(contract.getIndicators());
                    excelVO.setAssessment_dept(contract.getAssessment_dept());
                    excelVO.setWeight(contract.getWeight());
                    excelVO.setScoring_method(contract.getScoring_method());
                    excelVO.setAssessment_cycle(contract.getAssessment_cycle());
                    excelVO.setAssessed_unit(contract.getAssessed_unit());
                    excelVO.setAssessed_center(contract.getAssessed_center());
                    excelVO.setAssessed_people(contract.getAssessed_people());
                    excelVO.setOther(contract.getOther());

                    // 从 ContractsScore 中设置分数、考核时间和评分人
                    excelVO.setScore(score.getScore());
                    excelVO.setAssessment_time(score.getAssessment_time());
                    excelVO.setAssessment_people(score.getAssessment_people());

                    return excelVO;
                })
                .filter(Objects::nonNull) // 去除没有匹配的合同
                .sorted(Comparator.comparing(ExcelVO::getCategories)
                        .thenComparing(ExcelVO::getSub_categories)) // 最终按 categories 和 sub_categories 排序
                .collect(Collectors.toList());
    }


}