package utils;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * &#064;描述:  <解析指定excel表单的数据，封装到对象中：使用泛型可以处理不同类型的对象，返回泛型集合>
 */

public class ExcelUtil {
    public static Logger logger = Logger.getLogger(ExcelUtil.class);

    // 解析指定excel表单的数据，封装到对象中【对象类型使用泛型】
    public static <T> List<T> loadExcel(String excelPath, String sheetName, Class<T> clazz) {
        logger.info("===================开始读取sheet: " + sheetName);

        List<T> list = new ArrayList<T>();
        InputStream in = null;
        // 创建WorkBook对象
        try {
            String CaseFilePath =
                    new File(ExcelUtil.class.getResource("").toString()).getParent().substring(6)
                            + "\\resources\\" + excelPath;
            File file = new File(CaseFilePath);
            in = Files.newInputStream(file.toPath());
            // 获取workbook对象
            Workbook workbook = WorkbookFactory.create(in);
            // 获取sheet对象
            Sheet sheet = workbook.getSheet(sheetName);
            // 获取第一行，Row是行对象类型，通过行对象可以操作列
            Row firstRow = sheet.getRow(0);
            // 获取最后一列的列号
            int lastCellNum = firstRow.getLastCellNum();

            // 定义存放表头的数组
            String[] titles = new String[lastCellNum];
            // 将表头放入数组
            for (int i = 0; i < lastCellNum; i++) {
                // 通过行对象和列索引，获取单元格对象
                Cell cell = firstRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                //设置单元格类型
                cell.setCellType(CellType.STRING);
                // 获取单元格的值
                String title = cell.getStringCellValue();
                // 值保存到数组
                titles[i] = title;
            }

            // 获取sheet最后一行的行号
            int lastRowNum = sheet.getLastRowNum();
            // 循环处理每一行数据，从2行开始是数据行
            for (int i = 1; i <= lastRowNum  ; i++) {
                // 每行数据一个对象
                T obj = clazz.newInstance();
                // 获取一行数据
                Row rowData = sheet.getRow(i);
                if (rowData==null || rowDataIsEmpty(rowData)){
                    continue;
                }
                // 获取此行的列数据，封装到caseObject对象中
                for (int j = 0; j < lastCellNum ; j++) {
                    Cell cell = rowData.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    //设置单元格类型
                    cell.setCellType(CellType.STRING);
                    String cellValue = cell.getStringCellValue();
                    // 获取要反射的方法名
                    String methodName = "set" + titles[j];
                    // 获取要反射的方法对象
                    Method method = clazz.getMethod(methodName, String.class);
                    // 反射调用
                    method.invoke(obj, cellValue);
                }
                list.add(obj);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        } finally {
            if (in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error(e.toString());
                }
            }
        }
        logger.info("===================读取sheet完成: " + sheetName);
        return list;
    }


    // 判断行的单元格数据是否都是空
    public static boolean rowDataIsEmpty(Row rowData) {
        int lastCellNum = rowData.getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            Cell cell = rowData.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellType(CellType.STRING);
            String cellValue = cell.getStringCellValue();
            if (cellValue!=null && !cellValue.trim().isEmpty()){
                return false;
            }
        }
        return true;
    }
}