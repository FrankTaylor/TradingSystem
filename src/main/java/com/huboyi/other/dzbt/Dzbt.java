package com.huboyi.other.dzbt;

import com.huboyi.data.entity.MarketDataBean;
import com.huboyi.data.entity.StockDataBean;
import com.huboyi.data.load.DataLoadEngine;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by admin on 2016/8/6.
 */
public class Dzbt {

    /** 日志。*/
    private static final Logger log = LogManager.getLogger(Dzbt.class);

    // --- 注入变量 ---
    /** 输入文件的相对路径。*/
    private String inputFilePath;
    /** 输出文件的绝对路径。*/
    private String outputFilePath;

    @Autowired
    @Qualifier("dataLoadEngine")
    private DataLoadEngine dataLoadEngine;

    // --- 初始变量 ---
    /** 输入文件的绝对路径。*/
    private String fileAbsolutePath;
    /** 行情数据集合。*/
    private List<MarketDataBean> marketDataList;
    /** 装载文件的字符。*/
    private ByteArrayInputStream bais;

    @PostConstruct
    public void init() {

        // --- 设置输入文件的绝对路径 ---
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        fileAbsolutePath = cl.getResource("").getFile().concat(inputFilePath);
        // --- 设置行情数据集合 ---
        marketDataList = dataLoadEngine.loadMarketData();

        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileAbsolutePath))
        ) {
            byte[] b = new byte[bis.available()];
            bis.read(b);

            bais = new ByteArrayInputStream(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        try (
                XSSFWorkbook workbook = new XSSFWorkbook(bais);
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outputFilePath))
        ) {

            XSSFSheet sheet = workbook.getSheetAt(0);
            sheet.setDisplayGridlines(false);                              // 不显示网格线。
            sheet.createFreezePane(1, 1);                                  // 冻结窗口。（冻结第 1 列、第 1 行）
            int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();    // 得到物理行的数量。

            // --- 设置单元格风格 ---
            XSSFCellStyle defaultCellStyle = createDefaultResultCellStyle(workbook);                         // 创建输出测试结果的默认结果单元的风格。

            // --- 设置字体风格 ---
            XSSFFont normalFont = createDefaultXSSFFont(workbook, false, false, new int[] {0, 112, 192});    // 普通内容专用的字体风格。
            XSSFFont codeFont = createDefaultXSSFFont(workbook, true, false, new int[] {112, 50, 160});      // 证券代码专用的字体风格。
            XSSFFont redFont = createDefaultXSSFFont(workbook, false, false, new int[] {192, 0, 0});         // 红色字体风格。
            XSSFFont greenFont = createDefaultXSSFFont(workbook, false, false, new int[] {0, 101, 65});      // 绿色字体风格。

            // --- 设置显示风格 ---
            XSSFDataFormat fmt = workbook.createDataFormat();
            short stringDataFromat = fmt.getFormat("@");                                                     // 字符显示风格。
            short dateDataFromat = fmt.getFormat("yyyy\"年\"mm\"月\"dd\"日\"");                                            // 日期显示风格。
            short intDataFromat = fmt.getFormat("0");                                                    // 整数显示风格。
            short floatDataFormat = fmt.getFormat("0.0000");                                               // 浮点显示风格。
            short moneyDataFormat = fmt.getFormat("¥#,##0.00;¥-#,##0.00");                                   // 金钱显示风格。
            short rateDataFormat = fmt.getFormat("0.00%");                                                   // 比例显示风格。

            // --- 字符单元格风格 ---
            XSSFCellStyle stringCellStyle = (XSSFCellStyle)defaultCellStyle.clone();                         // 普通字符单元格风格。
            stringCellStyle.setAlignment(CellStyle.ALIGN_LEFT);
            stringCellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
            stringCellStyle.setDataFormat(stringDataFromat);

            // --- 代码单元格风格 ---
            XSSFCellStyle codeCellStyle = (XSSFCellStyle)defaultCellStyle.clone();                           // 证券代码单元格风格。
            codeCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            codeCellStyle.setFont(codeFont);
            codeCellStyle.setDataFormat(stringDataFromat);

            // --- 日期单元格风格 ---
            XSSFCellStyle dateCellStyle = (XSSFCellStyle)defaultCellStyle.clone();                           // 日期单元格风格。
            dateCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            dateCellStyle.setFont(normalFont);
            dateCellStyle.setDataFormat(dateDataFromat);

            // --- 金钱单元格风格 ---
            XSSFCellStyle normalMoneyCellStyle = (XSSFCellStyle)defaultCellStyle.clone();                    // 普通金钱单元格风格。
            normalMoneyCellStyle.setFont(normalFont);
            normalMoneyCellStyle.setDataFormat(moneyDataFormat);

            // --- 浮点单元格风格 ---
            XSSFCellStyle floatCellStyle = (XSSFCellStyle)defaultCellStyle.clone();                          // 日期单元格风格。
            floatCellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            floatCellStyle.setFont(normalFont);
            floatCellStyle.setDataFormat(floatDataFormat);

            // --- 比例单元格风格 ---
            XSSFCellStyle redRateCellStyle = (XSSFCellStyle)defaultCellStyle.clone();                        // 红色比例单元格风格。
            redRateCellStyle.setFont(redFont);
            redRateCellStyle.setDataFormat(rateDataFormat);

            XSSFCellStyle greenRateCellStyle = (XSSFCellStyle)defaultCellStyle.clone();                      // 绿色比例单元格风格。
            greenRateCellStyle.setFont(greenFont);
            greenRateCellStyle.setDataFormat(rateDataFormat);

            for (int i = 1; i < physicalNumberOfRows; i++) {
                XSSFRow row = sheet.getRow(i);



                String stockCode = row.getCell(0).getStringCellValue();                          // 股票代码。

                DecimalFormat decimalFormat = new DecimalFormat();
                decimalFormat.setGroupingUsed(false);
                String dzDate = decimalFormat.format(row.getCell(1).getNumericCellValue());      // 定增日期。
                dzDate = dzDate.substring(0, 4) + "-"
                        + dzDate.substring(4, 6) + "-"
                        + dzDate.substring(6, dzDate.length());

                double zfStockNums = row.getCell(2).getNumericCellValue();                       // 增发股数。
                double ltStockNums = row.getCell(3).getNumericCellValue();                       // 总流通股数。
                String dzdx = row.getCell(5).getStringCellValue();                               // 定增对象。
                double zfPrice = row.getCell(6).getNumericCellValue();                           // 增发价格。

                // --- 设置单元格风格 ----
                row.getCell(0).setCellStyle(codeCellStyle);                                      // 设置 “股票代码” 风格。
                row.getCell(1).setCellStyle(dateCellStyle);                                      // 设置 “定增日期” 风格。
                row.getCell(2).setCellStyle(floatCellStyle);                                     // 设置 “增发股数” 风格。
                row.getCell(3).setCellStyle(floatCellStyle);                                     // 设置 “总流通股数” 风格。
                row.getCell(4).setCellStyle(redRateCellStyle);                                   // 设置 “增发与流通占比” 风格。
                row.getCell(5).setCellStyle(stringCellStyle);                                    // 设置 “定增对象” 风格。
                row.getCell(6).setCellStyle(floatCellStyle);                                     // 设置 “增发价格” 风格。
                row.getCell(7).setCellStyle(floatCellStyle);                                     // 设置 “当前价格” 风格。
                row.getCell(8).setCellStyle(redRateCellStyle);                                   // 设置 “折、溢价幅度” 风格。


                if (StringUtils.isBlank(stockCode) || zfPrice <= 0) {
                    continue;
                }

                row.getCell(1).setCellValue(dzDate);

                double zfRate = BigDecimal.valueOf(zfStockNums)
                        .divide(BigDecimal.valueOf(ltStockNums), 4, RoundingMode.HALF_UP)
                        .doubleValue();
                row.getCell(4).setCellValue(zfRate);

                double cpPrice = findCloseByStockCode(stockCode, marketDataList);                // 得到该股最新的收盘价。
                if (cpPrice == -1) { continue; }
                double zyRate = BigDecimal.valueOf(zfPrice)                                      // 得到增发价相对于最新收盘价的折/溢比率。
                        .subtract(BigDecimal.valueOf(cpPrice))
                        .divide(BigDecimal.valueOf(cpPrice), 4, RoundingMode.HALF_UP)
                        .doubleValue();
                if (zyRate < 0) {
                    row.getCell(8).setCellStyle(greenRateCellStyle);
                }

                // --- 把 “收盘价” 和 “折/溢比率” 设置到 Excel 中 ---
                row.getCell(7).setCellValue(cpPrice);
                row.getCell(8).setCellValue(zyRate);
            }

            workbook.write(os);
            log.info("完成定增配套的数据分析工作");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据股票代码找出其最新的收盘价。
     *
     * @param stockCode 股票代码
     * @param marketDataList 行情数据集合
     * @return double 返回 -1D 则说明没有找到
     */
    private double findCloseByStockCode(String stockCode, List<MarketDataBean> marketDataList) {
        if (StringUtils.isBlank(stockCode) || marketDataList == null || marketDataList.size() == 0) {
            return -1D;
        }
        for (MarketDataBean marketData : marketDataList) {
            String sc = marketData.getStockCode();
            if (StringUtils.isBlank(sc)) { continue; }
            sc = sc.substring(2, sc.length()).concat(".").concat(sc.substring(0, 2));

            if (sc.equals(stockCode)) {
                List<StockDataBean> stockDataList = marketData.getStockDataList();
                if (stockDataList == null || stockDataList.size() == 0) { continue; }

                return stockDataList.get(stockDataList.size() - 1).getClose().doubleValue();
            }
        }

        return -1D;
    }

    // --- 修饰 Excel 风格的方法 ---

    /**
     * 创建输出测试结果的默认结果单元的风格。
     *
     * @param workbook XSSFWorkbook
     * @return XSSFCellStyle
     */
    private XSSFCellStyle createDefaultResultCellStyle (XSSFWorkbook workbook) {
        XSSFFont font = createDefaultXSSFFont(workbook, false, false, new int[] {255, 255, 255});        // 设置内容单元的字体。
        XSSFCellStyle cellStyle = createDefaultXSSFCellStyle(workbook, font, new int[] {255, 255, 255}); // 设置内容单元的风格。
        cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
        return cellStyle;
    }

    /**
     * 创建Excel默认风格。
     *
     * @param workbook XSSFWorkbook
     * @param font XSSFFont
     * @param rgb 字体颜色
     * @return XSSFCellStyle
     */
    private XSSFCellStyle
    createDefaultXSSFCellStyle (XSSFWorkbook workbook, XSSFFont font, int[] rgb) {

        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(rgb[0], rgb[1], rgb[2]))); // 设置单元格前景色。
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);                                        // 设置填充模式，有半透明的效果。（如果不设置此项，则单元格前景色不成显示）

        cellStyle.setWrapText(true);                                                                 // 内容自动换行。
        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);                                              // 字体居中。
        cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);                                   // 字体垂直居中。
        cellStyle.setFont(font);                                                                     // 设置字体风格。

        cellStyle.setBorderTop(BorderStyle.THIN);                                                    // 单元格顶部线框的类型。（细黑线）
        cellStyle.setBorderBottom(BorderStyle.THIN);                                                 // 单元格底部线框的类型。（细黑线）
        cellStyle.setBorderLeft(BorderStyle.THIN);                                                   // 单元格左边线框的类型。（细黑线）
        cellStyle.setBorderRight(BorderStyle.THIN);                                                  // 单元格右边线框的类型。（细黑线）

        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.TOP, new XSSFColor(new java.awt.Color(0, 0, 0)));        // 单元格顶部线框的颜色。（黑色）
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.BOTTOM, new XSSFColor(new java.awt.Color(0, 0, 0)));     // 单元格底部线框的颜色。（黑色）
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.LEFT, new XSSFColor(new java.awt.Color(0, 0, 0)));       // 单元格左边线框的颜色。（黑色）
        cellStyle.setBorderColor(XSSFCellBorder.BorderSide.RIGHT, new XSSFColor(new java.awt.Color(0, 0, 0)));      // 单元格右边线框的颜色。（黑色）

        return cellStyle;
    }

    /**
     * 创建Excel默认字体。
     *
     * @param workbook XSSFWorkbook
     * @param isBold 是否是粗体
     * @param isItalic 是否是斜体
     * @param rgb 字体颜色
     * @return XSSFFont
     */
    private XSSFFont
    createDefaultXSSFFont (XSSFWorkbook workbook, boolean isBold, boolean isItalic, int[] rgb) {
        XSSFFont font = workbook.createFont();
        font.setBold(isBold);                                                       // 显示为粗体。
        font.setItalic(isItalic);                                                   // 显示为斜体。
        font.setColor(new XSSFColor(new java.awt.Color(rgb[0], rgb[1], rgb[2])));   // 字体颜色。
        font.setFontName("微软雅黑");                                                // 字体风格。
        font.setFontHeight(8);                                                      // 字体大小。

        return font;
    }

    // --- get and set method ---

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }
}
