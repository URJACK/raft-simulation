package com.sicnu.netsimu.ui.summary;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.sicnu.netsimu.core.NetSimulator;
import lombok.Data;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncrementalSummarizerWithExcel extends IncrementalSummarizer {
    private String outputPath;

    /**
     * @param simulator 网络模拟器对象引用
     */
    public IncrementalSummarizerWithExcel(NetSimulator simulator, String outputPath) {
        super(simulator);
        this.outputPath = outputPath;
    }

    public void redirectFilePath(String filePath) {
        this.outputPath = filePath;
    }

    @Override
    protected void processOutput() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        transplantTitle(sheet);
        transplantValue(sheet, incrementalMap, 0);
        try {
            workbook.write(new FileOutputStream(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transplantTitle(XSSFSheet sheet) {
        XSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("节点Id");
        row.createCell(1).setCellValue("数据");
    }

    private void transplantValue(XSSFSheet sheet, HashMap<Integer, List<Float>> map, int beginOffset) {
        for (Map.Entry<Integer, List<Float>> entry : map.entrySet()) {
            Integer moteId = entry.getKey();
            List<Float> values = entry.getValue();
            XSSFRow row = sheet.getRow(moteId + beginOffset);
            if (row == null) {
                row = sheet.createRow(moteId);
            }
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(moteId);
            int cursor = 1;
            for (Float val : values) {
                cell = row.createCell(cursor++);
                cell.setCellValue(val);
            }
        }
    }
}
