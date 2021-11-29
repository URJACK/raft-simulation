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

/**
 * Excel输出的 增量总结器
 */
public class IncrementalSummarizerWithExcel extends IncrementalSummarizer {
    private String outputPath;

    /**
     * @param simulator 网络模拟器对象引用
     */
    public IncrementalSummarizerWithExcel(NetSimulator simulator, String outputPath) {
        super(simulator);
        this.outputPath = outputPath;
    }

    /**
     * 重定向文件输出路径
     *
     * @param filePath 文件输出路径
     */
    public void redirectFilePath(String filePath) {
        this.outputPath = filePath;
    }

    @Override
    protected void processOutput() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        String[] titles = {"节点id", "数据<能耗增量>"};
        transplantTitle(sheet, 0, titles);
        transplantValue(sheet, incrementalMap, 1);
        try {
            workbook.write(new FileOutputStream(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建标题
     * 把标题数组塞入对应行中 （从第0列开始塞）
     *
     * @param sheet    Excel的sheet表
     * @param rowIndex 标题所属行号
     * @param titles   标题数组
     */
    private void transplantTitle(XSSFSheet sheet, int rowIndex, String[] titles) {
        XSSFRow row = sheet.createRow(rowIndex);
        for (int i = 0; i < titles.length; i++) {
            row.createCell(i).setCellValue(titles[i]);
        }
    }

    /**
     * 移植数据 从 [beginOffset, beginOffset + n)
     * n 代表节点个数
     *
     * @param sheet       Excel 的 sheet表
     * @param map         被移植的数据结构
     * @param beginOffset 开始的偏移量（有第二类的数据的时候，我们可以使用这个偏移量）
     */
    private void transplantValue(XSSFSheet sheet, HashMap<Integer, List<Float>> map, int beginOffset) {
        for (Map.Entry<Integer, List<Float>> entry : map.entrySet()) {
            Integer moteId = entry.getKey();
            List<Float> values = entry.getValue();
            XSSFRow row = sheet.getRow(moteId - 1 + beginOffset);
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
