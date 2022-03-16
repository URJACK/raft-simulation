package com.sicnu.netsimu.ui.summary;

import com.sicnu.netsimu.core.NetSimulator;
import com.sicnu.netsimu.core.node.Node;
import com.sicnu.netsimu.core.node.NodeManager;
import com.sicnu.netsimu.core.net.TransmissionManager;
import com.sicnu.netsimu.core.statis.EnergyStatistician;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Excel输出的 增量总结器
 */
public class IncrementalSummarizerWithExcel extends IncrementalSummarizer {
    HashMap<Integer, List<Float>> energyCalcMap;
    private String outputPath;

    /**
     * @param simulator 网络模拟器对象引用
     */
    public IncrementalSummarizerWithExcel(NetSimulator simulator, String outputPath) {
        super(simulator);
        this.outputPath = outputPath;
        this.energyCalcMap = new HashMap<>();
    }

    /**
     * 重定向文件输出路径
     *
     * @param filePath 文件输出路径
     */
    public void redirectFilePath(String filePath) {
        this.outputPath = filePath;
    }


    /**
     * 当传入的param是 OUTPUT的时候，summarize() 会调用该方法。
     */
    @Override
    protected void processOutput() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        //给第0行，添加标题内容
        String[] titles_one = {"节点id", "数据<能耗增量>"};
        int rowIndex = 0;
        transplantTitle(sheet, rowIndex, titles_one);
        rowIndex++;
        //添加若干行（节点数那么多行），从第1行开始
        transplantValue(sheet, energyCalcMap, rowIndex);
        rowIndex += energyCalcMap.size();
        //添加新的标题栏
        String[] titles_two = {"节点id", "平均送达率", "平均发送丢包率", "平均接受率", "平均接收丢包率"};
        transplantTitle(sheet, rowIndex, titles_two);
        rowIndex++;
        //添加新的数据 添加每个节点的4项 传输指标
        TransmissionManager transmissionManager = simulator.getTransmissionManager();
        for (Map.Entry<Integer, List<Float>> entry : energyCalcMap.entrySet()) {
            Integer moteId = entry.getKey();
            TransmissionManager.StatisticInfo info = transmissionManager.getStatisticInformationWithId(moteId);

            float sendSuccessRate = info.getSendSuccessRate();
            float sendFailedRate = info.getSendFailedRate();
            float receiveSuccessRate = info.getReceiveSuccessRate();
            float receiveFailedRate = info.getReceiveFailedRate();

            XSSFRow row = sheet.createRow(rowIndex);
            XSSFCell moteIdCell = row.createCell(0);
            moteIdCell.setCellValue(moteId);
            transplantTransmissionValue(row, sendSuccessRate, 1);
            transplantTransmissionValue(row, sendFailedRate, 2);
            transplantTransmissionValue(row, receiveSuccessRate, 3);
            transplantTransmissionValue(row, receiveFailedRate, 4);
            rowIndex++;
        }

        try {
            workbook.write(new FileOutputStream(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transplantTransmissionValue(XSSFRow row, float rate, int colIndex) {
        XSSFCell cell = row.createCell(colIndex);
        cell.setCellValue(rate);
    }

    /**
     * 基础增量计算
     * 所有的IncrementalSummarizer都应当调用该方法计算各个数据的增量
     */
    @Override
    protected void processBasicCalc() {
        NodeManager nodeManager = simulator.getMoteManager();
        ArrayList<Node> allNodes = nodeManager.getAllMotes();
        for (Node node : allNodes) {
            EnergyStatistician energyStatistician = node.getSingleMoteEnergyStatistician();
            //获得每个节点的能耗
            Float statisticianAllSummary = energyStatistician.getAllSummary();
            //清空这个时间点的能耗记录
            energyStatistician.clear();
            //将这次到上次调用之间时段 “时段能耗数据” 进行统计
            List<Float> list = energyCalcMap.computeIfAbsent(node.getMoteId(), k -> new LinkedList<>());
            //将“时段能耗数据”塞入对应节点的列表中
            list.add(statisticianAllSummary);
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
