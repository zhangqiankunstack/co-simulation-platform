package com.rengu.cosimulation.test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rengu.cosimulation.utils.ExcelUtils.parseExcel;

/**
 * Author: XYmar
 * Date: 2019/11/6 20:42
 */
public class Test {
    public static void main(String[] args) throws Exception {
//        File file = new File("C:\\Users\\XY\\Desktop\\getExcel(1).java");
//        String str = file.getPath();
//        System.out.println(str);
        File file = new File("C:\\Users\\XY\\Desktop\\111.xlsx");
        //InputStream fis = new FileInputStream(file);
        Map<String, String> m = new HashMap<>();
        m.put("ID", "id");
        m.put("EMPID", "empId");
        m.put("USERID", "userId");
        m.put("EMPNAME", "empName");
        m.put("CARDNO", "cardNo");
        m.put("SECRETRANK", "secretRank");
        m.put("ORGNAME", "orgName");
        m.put("EMPID1", "enpId1");
        m.put("POSINAME", "posiName");
        List<Map<String, Object>> ls = parseExcel(file, m);

        for(Map<String, Object> map : ls) {
            System.out.println(map.get("id"));
            System.out.println(map.get("userId"));
        }
    }
    /*//导出xls表
    public void exportXLS() {
        //新建一个ArrayList<Student>
        ArrayList<Student> stuList = new ArrayList<Student>();
        Student stu1 = new Student(1,"fancy","男","a");
        Student stu2 = new Student(1,"leeen","男","b");
        Student stu3 = new Student(1,"zerozero1","女","c");
        Student stu4 = new Student(1,"tot","男","d");
        stuList.add(stu1);
        stuList.add(stu2);
        stuList.add(stu3);
        stuList.add(stu4);
//        System.out.println(stuList);
        // 1、在内存中创建一个excel文件
        XSSFWorkbook workbook = new XSSFWorkbook();
        // 2、创建工作簿
        XSSFSheet sheet = workbook.createSheet();
        // 3、创建标题行
        XSSFRow titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("编号");
        titleRow.createCell(1).setCellValue("姓名");
        titleRow.createCell(2).setCellValue("性别");
        titleRow.createCell(3).setCellValue("地址");
        // 4、遍历数据，创建数据行
        for (Student stu:
                stuList) {
            // 1、获取当前最后一行的行号
            int lastRowNumber = sheet.getLastRowNum();
            // 2、添加新行
            XSSFRow dataRow = sheet.createRow(lastRowNumber + 1);
            dataRow.createCell(0).setCellValue(stu.getId());
            dataRow.createCell(1).setCellValue(stu.getName());
            dataRow.createCell(2).setCellValue(stu.getSex());
            dataRow.createCell(3).setCellValue(stu.getAddress());
        }
        // 5、创建文件名
        String fileName = "student.xls";
        // 6、获取输出流对象
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream("C:/Users/fancyyyy/Desktop/poi/" + fileName);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}