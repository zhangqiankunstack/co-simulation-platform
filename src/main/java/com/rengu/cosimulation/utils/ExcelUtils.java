    package com.rengu.cosimulation.utils;

    import lombok.extern.slf4j.Slf4j;
    import org.apache.poi.hssf.usermodel.HSSFWorkbook;
    import org.apache.poi.ss.usermodel.Cell;
    import org.apache.poi.ss.usermodel.Row;
    import org.apache.poi.ss.usermodel.Sheet;
    import org.apache.poi.ss.usermodel.Workbook;
    import org.apache.poi.xssf.usermodel.XSSFWorkbook;

    import java.io.File;
    import java.io.FileInputStream;
    import java.io.InputStream;
    import java.text.DecimalFormat;
    import java.text.SimpleDateFormat;
    import java.util.*;

    /**
     * Author: XYmar
     * Date: 2019/11/6 15:59
     * 解析文件
     */

    @Slf4j
    public class ExcelUtils {


        private final static String excel2003L = ".xls"; // 2003- 版本的excel
        private final static String excel2007U = ".xlsx"; // 2007+ 版本的excel

        /**
         * 将流中的Excel数据转成List<Map>
         *
         * @param file 输入流
         * @return
         * @throws Exception
         */
        public static List<Map<String, Object>> parseExcel(File file, Map<String, String> mapping) throws Exception {
            //传过来一个文件，导入
            // 根据文件名来创建Excel工作薄
            Workbook work = getWorkbook(file);                              //调用了getWorkbook进行对比传进来的报文类型
            if (null == work) {
                throw new Exception("创建Excel工作薄为空！");
            }
            Sheet sheet = null;    //sheet页
            Row row = null;        //行
            Cell cell = null;     //列
            // 返回数据
            List<Map<String, Object>> ls = new ArrayList<>();
            // 遍历Excel中所有的sheet
            for(int i = 0; i <work.getNumberOfSheets(); i++) {        //遍历有多少
                sheet = work.getSheetAt(0);
                if (sheet == null) {
                    continue;
                }else {

                    // 取第一行标题
                    row = sheet.getRow(0);
                    String title[] = null;
                    if (row != null) {                              //如果第一行数不为空的话，证明ECAEL中有数据。
                        title = new String[row.getLastCellNum()];     //得到最后一列的列数，即等于一条数据的总列数

                        for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {        //遍历这一条数据的第一列到最后一列
                            cell = row.getCell(y);                    //遍历后把值赋给这一行，就拿到了这一行的数据
                            title[y] = (String) getCellValue(cell);   //并把这一行赋值给这张表

                        }

                    } else
                        continue;                                   //如果第一页sheet为空的话就结束此次循环，执行下一次循环，当进入sheet第二页有值的时候，遍历。

                    // 遍历当前sheet中的所有行
                    for (int j = 1; j < sheet.getLastRowNum() + 1; j++) {
                        row = sheet.getRow(j);

                        Map<String, Object> m = new HashMap<String, Object>();                  //有值的话就存到一个map里面
                        // 遍历所有的列
                        for (int y = row.getFirstCellNum(); y < row.getLastCellNum(); y++) {
                            cell = row.getCell(y);
                            String key = title[y];
                            // log.info(JSON.toJSONString(key));
                            m.put(mapping.get(key), getCellValue(cell));            //先格式化，在赋值
                        }
                        ls.add(m);
                    }
                }

            }
            work.close();
            return ls;
        }

        /**
         * 描述：根据文件后缀，自适应上传文件的版本
         *
         * @param ,file
         * @return
         * @throws Exception
         */
        public static Workbook getWorkbook(File file) throws Exception {
            InputStream inStr = new FileInputStream(file);//文件传过来
            Workbook wb = null;
            String fileName = file.getPath();                   //得到文件路径
            String fileType = fileName.substring(fileName.lastIndexOf("."));        //从左边开始获取.的位置，并转为字符串
            if (excel2003L.equals(fileType)) {
                wb = new HSSFWorkbook(inStr); // 2003-
            } else if (excel2007U.equals(fileType)) {
                wb = new XSSFWorkbook(inStr); // 2007+
            } else {
                throw new Exception("解析的文件格式有误！");
            }
            return wb;                                                          //返回报文类型
        }

        /**
         *
         * 描述：对表格中数值进行格式化
         * @param cell
         * @return
         */
        public static Object getCellValue(Cell cell) {
            Object value = null;
            DecimalFormat df = new DecimalFormat("0"); // 格式化number String字符
            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd"); // 日期格式化
            DecimalFormat df2 = new DecimalFormat("0"); // 格式化数字

            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_STRING:
                    value = cell.getRichStringCellValue().getString();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    if ("General".equals(cell.getCellStyle().getDataFormatString())) {
                        value = df.format(cell.getNumericCellValue());
                    } else if ("m/d/yy".equals(cell.getCellStyle().getDataFormatString())) {
                        value = sdf.format(cell.getDateCellValue());
                    } else {
                        value = df2.format(cell.getNumericCellValue());
                    }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case Cell.CELL_TYPE_BLANK:
                    value = "";
                    break;
                default:
                    break;
            }
            return value;
        }
    }
