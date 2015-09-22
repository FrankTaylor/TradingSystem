package com.huboyi.system.test.output;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.springframework.util.StringUtils;

import com.huboyi.system.test.bean.TestResultBean;

/**
 * 把测试结果输出到Excel中。生成Excel的版本为2007及以上，使用的框架是POI-3.10.1，
 * 在该框架中已经不建议使用CellRangeAddress类了，但是我还没有找到合适的代替类。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/1/13
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class TestResultOutputExcel {
	
	public static void main(String[] args) {
		
		List<Integer> resultBeanList = new ArrayList<Integer>();
		for (int i = 0; i < 1; i++) {
			resultBeanList.add((i + 1));
		}
		
		int begin = 1;
		int end = resultBeanList.size() / 100;
		end = (end == 0) ? 1 : ((end * 100) < resultBeanList.size()) ? (end + 1) : end;
		
		for (int i = begin; i <= end; i++) {
			
			int rowNums = 2;                                                                // 从第2行开始才是结果内容。
			for (int j = 0; j < 100; j++) {
				int index = (i - 1) * 100 + j;
				if (index >= resultBeanList.size()) {
					break;
				}
				Integer result = resultBeanList.get(index);
				
				System.out.println("结果 = " + result);
				rowNums++;
			}
			
		}	
			
	}
	
	/**
	 * 输出stanWeinstein交易系统的测试结果。
	 * 
	 * @param resultBeanList 装载测试结果的集合
	 * @param outputFileput 输出的文件路径
	 */
	public void 
	outputTestResult (List<TestResultBean> resultBeanList, String outputFileput) {
		try {
			
			BufferedOutputStream os = null;
			XSSFWorkbook workbook = new XSSFWorkbook();
			
			try {
				
				if (resultBeanList != null && !resultBeanList.isEmpty() && !StringUtils.isEmpty(outputFileput)) {
					
					int begin = 1;
					int end = resultBeanList.size() / 100;
					end = (end == 0) ? 1 : ((end * 100) < resultBeanList.size()) ? (end + 1) : end;
					
					for (int i = begin; i <= end; i++) {
						XSSFSheet sheet = workbook.createSheet("系统测试结果" + i);      // 创建Excel中的sheet页。
						setDefaultSheetStyle(sheet);                                 // 给sheet设置默认的风格。
						sheet.createFreezePane(1, 2);                                // 冻结窗口。（冻结第1列、第2行）
						
						outputTitle(workbook, sheet);                                // 输出标题内容。
						
						int rowNums = 2;                                             // 从第2行开始才是结果内容。
						for (int j = 0; j < 100; j++) {
							int index = (i - 1) * 100 + j;
							if (index >= resultBeanList.size()) {
								break;
							}
							TestResultBean resultBean = resultBeanList.get(index);   // 输出交易系统测试的结果内容。
							outputResult(rowNums, workbook, sheet, resultBean);                         
							rowNums++;
						}
					}
				}
				
				// 输出测试结果。
			    os = new BufferedOutputStream(new FileOutputStream(outputFileput));
				workbook.write(os);
			} finally {
				if (null != os) {
					os.flush();
					os.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*----------------------------Excel操作的private方法-----------------------------------*/
	
	/**
	 * 输出交易系统测试的结果内容。
	 * 
	 * @param workbook XSSFWorkbook
	 * @param sheet XSSFSheet
	 * @param resultBean TestResultBean
	 * @throws ParseException
	 */
	private void 
	outputResult (int rowNums, XSSFWorkbook workbook, XSSFSheet sheet, TestResultBean resultBean) 
	throws ParseException {
		
		if (null == resultBean) {
			return;
		}
		
		XSSFRow row = sheet.createRow(rowNums);                                                          // 创建Excel文件的第n行，用于显示结果。
		XSSFCellStyle defaultCellStyle = createDefaultResultCellStyle(workbook);                         // 创建输出测试结果的默认结果单元的风格。
		
		// --- 设置字体风格 --- 
		XSSFFont normalFont = createDefaultXSSFFont(workbook, false, false, new int[] {0, 112, 192});    // 普通内容专用的字体风格。 
		XSSFFont codeFont = createDefaultXSSFFont(workbook, true, false, new int[] {112, 50, 160});      // 证券代码专用的字体风格。 
		XSSFFont profitFont = createDefaultXSSFFont(workbook, false, false, new int[] {192, 0, 0});      // 盈利情况下的字体风格。
		XSSFFont lossFont = createDefaultXSSFFont(workbook, false, false, new int[] {0, 101, 65});       // 亏损情况下的字体风格。
		// --- 设置字体风格 ---
		
		// --- 设置显示风格 ---
		XSSFDataFormat fmt = workbook.createDataFormat(); 
		short stringDataFromat = fmt.getFormat("@");                                  // 字符显示风格。
		short intDataFromat = fmt.getFormat("#,##0");                                 // 整数显示风格。
		short floatDataFormat = fmt.getFormat("#,##0.00");                            // 浮点显示风格。
		short moneyDataFormat = fmt.getFormat("¥#,##0.00;¥-#,##0.00");                // 金钱显示风格。
		short rateDataFormat = fmt.getFormat("0.00%");                                // 比例显示风格。
		// --- 设置显示风格 ---
		
		// ----------- 整型单元格风格 ----------
		XSSFCellStyle normalIntCellStyle = (XSSFCellStyle)defaultCellStyle.clone();   // 普通整型单元格风格。
		normalIntCellStyle.setFont(normalFont);
		normalIntCellStyle.setDataFormat(intDataFromat);
		
		XSSFCellStyle profitIntCellStyle = (XSSFCellStyle)defaultCellStyle.clone();   // 盈利整型单元格风格。
		profitIntCellStyle.setFont(profitFont);
		profitIntCellStyle.setDataFormat(intDataFromat);
		
		XSSFCellStyle lossIntCellStyle = (XSSFCellStyle)defaultCellStyle.clone();     // 亏损整型单元格风格。
		lossIntCellStyle.setFont(lossFont);
		lossIntCellStyle.setDataFormat(intDataFromat);
		
		// ----------- 浮点单元格风格 ----------
		XSSFCellStyle normalFloatCellStyle = (XSSFCellStyle)defaultCellStyle.clone(); // 普通浮点单元格风格。
		normalFloatCellStyle.setFont(normalFont);
		normalFloatCellStyle.setDataFormat(floatDataFormat);
		
		XSSFCellStyle profitFloatCellStyle = (XSSFCellStyle)defaultCellStyle.clone(); // 盈利浮点单元格风格。
		profitFloatCellStyle.setFont(profitFont);
		profitFloatCellStyle.setDataFormat(floatDataFormat);
		
		XSSFCellStyle lossFloatCellStyle = (XSSFCellStyle)defaultCellStyle.clone();   // 亏损浮点单元格风格。
		lossFloatCellStyle.setFont(lossFont);
		lossFloatCellStyle.setDataFormat(floatDataFormat);
		
		// ----------- 金钱单元格风格 ----------
		XSSFCellStyle normalMoneyCellStyle = (XSSFCellStyle)defaultCellStyle.clone(); // 普通金钱单元格风格。
		normalMoneyCellStyle.setFont(normalFont);
		normalMoneyCellStyle.setDataFormat(moneyDataFormat);
		
		XSSFCellStyle profitMoneyCellStyle = (XSSFCellStyle)defaultCellStyle.clone(); // 盈利金钱单元格风格。
		profitMoneyCellStyle.setFont(profitFont);
		profitMoneyCellStyle.setDataFormat(moneyDataFormat);
		
		XSSFCellStyle lossMoneyCellStyle = (XSSFCellStyle)defaultCellStyle.clone();   // 亏损金钱单元格风格。
		lossMoneyCellStyle.setFont(lossFont);
		lossMoneyCellStyle.setDataFormat(moneyDataFormat);
		
		// ----------- 比例单元格风格 ----------
		XSSFCellStyle profitRateCellStyle = (XSSFCellStyle)defaultCellStyle.clone();  // 盈利比例单元格风格。
		profitRateCellStyle.setFont(profitFont);
		profitRateCellStyle.setDataFormat(rateDataFormat);
		
		XSSFCellStyle lossRateCellStyle = (XSSFCellStyle)defaultCellStyle.clone();    // 亏损比例单元格风格。
		lossRateCellStyle.setFont(lossFont);
		lossRateCellStyle.setDataFormat(rateDataFormat);
		
		// ----------- 代码单元格风格 ----------
		XSSFCellStyle codeCellStyle = (XSSFCellStyle)defaultCellStyle.clone();        // 证券代码单元格风格。
		codeCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		codeCellStyle.setFont(codeFont);
		codeCellStyle.setDataFormat(stringDataFromat);
		
		// ----------- 字符单元格风格 ----------
		XSSFCellStyle stringCellStyle = (XSSFCellStyle)defaultCellStyle.clone();      // 买卖详情、盈亏详情单元格风格。
		stringCellStyle.setAlignment(CellStyle.ALIGN_LEFT);
		stringCellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		stringCellStyle.setDataFormat(stringDataFromat);
		
		// --- 单元格赋值 ---
		                                                                                                                       // ----------------------- 交易盈亏 ------------------------
		createCellAndOutput(workbook, row, 0, codeCellStyle, resultBean.getStockCode());                                       // 证券代码。
		createCellAndOutput(workbook, row, 1, profitMoneyCellStyle, resultBean.getTotalAsset().doubleValue());                 // 总资产。
		if (resultBean.getFloatProfitAndLoss().doubleValue() > 0) {			                                                   // 浮动盈亏。
			createCellAndOutput(workbook, row, 2, profitMoneyCellStyle, resultBean.getFloatProfitAndLoss().doubleValue());
		} else {
			createCellAndOutput(workbook, row, 2, lossMoneyCellStyle, resultBean.getFloatProfitAndLoss().doubleValue());
		}
		if (resultBean.getProfitAndLossRatio().doubleValue() > 0) {                                                            // 盈亏比例。
			createCellAndOutput(workbook, row, 3, profitRateCellStyle, resultBean.getProfitAndLossRatio().doubleValue());
		} else {
			createCellAndOutput(workbook, row, 3, lossRateCellStyle, resultBean.getProfitAndLossRatio().doubleValue());		
		}
		if (resultBean.getWinRate().doubleValue() > 0.5) {                                                                     // 胜率。
			createCellAndOutput(workbook, row, 4, profitRateCellStyle, resultBean.getWinRate().doubleValue());
			
		} else {
			createCellAndOutput(workbook, row, 4, lossRateCellStyle, resultBean.getWinRate().doubleValue());
		}
		
		                                                                                                                       // ----------------------- 资产分布 ------------------------
		createCellAndOutput(workbook, row, 5, normalMoneyCellStyle, resultBean.getInitMoney().doubleValue());                  // 初始资金。
		createCellAndOutput(workbook, row, 6, normalMoneyCellStyle, resultBean.getFundsBalance().doubleValue());               // 剩余资金。
		createCellAndOutput(workbook, row, 7, normalMoneyCellStyle, resultBean.getMarketValue().doubleValue());                // 股票市值。
		createCellAndOutput(workbook, row, 8, normalIntCellStyle, resultBean.getStockNumber().doubleValue());                  // 证券数量。
		createCellAndOutput(workbook, row, 9, profitFloatCellStyle, resultBean.getCostPrice().doubleValue());                  // 成本价格。
		createCellAndOutput(workbook, row, 10, profitFloatCellStyle, resultBean.getNewPrice().doubleValue());                  // 当前价格。
		
		                                                                                                                       // ----------------------- 交易频率 ------------------------
		createCellAndOutput(workbook, row, 11, stringCellStyle, resultBean.getDealDetailList());                               // 买卖详情。
		createCellAndOutput(workbook, row, 12, normalIntCellStyle, resultBean.getDealNumber().doubleValue());                  // 交易次数。
		createCellAndOutput(workbook, row, 13, normalIntCellStyle, resultBean.getBuyNumber().doubleValue());                                 // 买入次数。
		createCellAndOutput(workbook, row, 14, normalIntCellStyle, resultBean.getSellNumber().doubleValue());                                // 卖出次数。
		createCellAndOutput(workbook, row, 15, normalIntCellStyle, resultBean.getAvgBuyAndSellInterval().doubleValue());       // 平均间隔。
		createCellAndOutput(workbook, row, 16, profitIntCellStyle, resultBean.getMaxBuyAndSellInterval().doubleValue());       // 最大间隔。
		createCellAndOutput(workbook, row, 17, lossIntCellStyle, resultBean.getMinBuyAndSellInterval().doubleValue());         // 最小间隔。

		                                                                                                                       // ----------------------- 交易周期 ------------------------
		createCellAndOutput(workbook, row, 18, stringCellStyle, resultBean.getCyclePLDetailList());                            // 盈亏详情。
		createCellAndOutput(workbook, row, 19, normalIntCellStyle, resultBean.getCycleNumber().doubleValue());                               // 交易周期。
		createCellAndOutput(workbook, row, 20, profitIntCellStyle, resultBean.getWinNumber().doubleValue());                                 // 赢利周期。
		createCellAndOutput(workbook, row, 21, lossIntCellStyle, resultBean.getLossNumber().doubleValue());                                  // 亏损周期。
		createCellAndOutput(workbook, row, 22, normalIntCellStyle, resultBean.getAvgCycleInterval().doubleValue());            // 平均间隔。
		createCellAndOutput(workbook, row, 23, lossIntCellStyle, resultBean.getMaxCycleInterval().doubleValue());              // 最大间隔。
		createCellAndOutput(workbook, row, 24, lossIntCellStyle, resultBean.getMinCycleInterval().doubleValue());              // 最小间隔。
		
		                                                                                                                       // ----------------------- 周期盈利 ------------------------
		createCellAndOutput(workbook, row, 25, profitMoneyCellStyle, resultBean.getAvgProfit().doubleValue());                 // 平均盈利。
		createCellAndOutput(workbook, row, 26, lossMoneyCellStyle, resultBean.getAvgLoss().doubleValue());                     // 平均亏损。
		createCellAndOutput(workbook, row, 27, profitMoneyCellStyle, resultBean.getMaxProfit().doubleValue());                 // 最大盈利。
		createCellAndOutput(workbook, row, 28, lossMoneyCellStyle, resultBean.getMaxLoss().doubleValue());                     // 最大亏损。
		createCellAndOutput(workbook, row, 29, profitMoneyCellStyle, resultBean.getMinProfit().doubleValue());                 // 最小盈利。
		createCellAndOutput(workbook, row, 30, lossMoneyCellStyle, resultBean.getMinLoss().doubleValue());                     // 最小亏损。
		// --- 单元格赋值 ---
	}

	/**
	 * 输出交易系统测试标题内容。
	 * 
	 * @param workbook XSSFWorkbook
	 * @param sheet XSSFSheet
	 */
	private void outputTitle (XSSFWorkbook workbook, XSSFSheet sheet) {
		
		// --- 输出标题分类 ---
		
		/*
		 * CellRangeAddress这个类框架已经不主张使用了，可是我没有找到他的代替类。
		 */
		// 进行单元格合并。
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));                                                                // 交易盈亏。
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 10));                                                               // 资产分布。
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 11, 17));                                                              // 交易频率。
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 18, 24));                                                              // 交易周期。
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 25, 30));                                                              // 周期盈亏。	
		
		XSSFRow rowCategory = sheet.createRow(0);                                                                               // 创建Excel文件的第0行，用于显示标题分类。
		
		// 设置字体和标题分类的单元格式。
		XSSFFont fontCategory = createDefaultXSSFFont(workbook, false, false, new int[] {0, 0, 0});                             // 标题分类字体风格。
		fontCategory.setFontHeight(16);
		
		XSSFCellStyle ccDealProfitAndLossStyle = createDefaultXSSFCellStyle(workbook, fontCategory, new int[] {192, 80, 77});   // 交易盈亏单元风格。
		XSSFCellStyle ccFundStyle = createDefaultXSSFCellStyle(workbook, fontCategory, new int[] {79, 129, 189});               // 资产分布单元风格。
		XSSFCellStyle ccFrequencyStyle = createDefaultXSSFCellStyle(workbook, fontCategory, new int[] {155, 187, 89});          // 交易频率单元风格。
		XSSFCellStyle ccDealCycleStyle = createDefaultXSSFCellStyle(workbook, fontCategory, new int[] {128, 100, 162});         // 交易周期单元风格。
		XSSFCellStyle ccCycleProfitAndLossStyle = createDefaultXSSFCellStyle(workbook, fontCategory, new int[] {247, 150, 70}); // 周期盈亏单元风格。
		
		// 输出标题分类。
		XSSFCell ccDealProfitAndLoss = rowCategory.createCell(0); 
		ccDealProfitAndLoss.setCellStyle(ccDealProfitAndLossStyle);
		ccDealProfitAndLoss.setCellValue("交易盈亏");
		
		XSSFCell ccFund = rowCategory.createCell(5);
		ccFund.setCellStyle(ccFundStyle);
		ccFund.setCellValue("资产分布");
		
		XSSFCell ccFrequency = rowCategory.createCell(11);
		ccFrequency.setCellStyle(ccFrequencyStyle);
		ccFrequency.setCellValue("交易频率");
		
		XSSFCell ccDealCycle = rowCategory.createCell(18);
		ccDealCycle.setCellStyle(ccDealCycleStyle);
		ccDealCycle.setCellValue("交易周期");
		
		XSSFCell ccCycleProfitAndLoss = rowCategory.createCell(25);
		ccCycleProfitAndLoss.setCellStyle(ccCycleProfitAndLossStyle);
		ccCycleProfitAndLoss.setCellValue("周期盈亏");
		
		// --- 输出标题 ---
		
		XSSFRow rowTitle = sheet.createRow(1);                                                           // 创建Excel文件的第1行，用于显示标题。
		XSSFCellStyle cellTitleStyle = createDefaultTitleCellStyle(workbook);                            // 创建输出测试结果的默认标题单元的风格。
		
		String[] titleNameArray = {
				"证券代码", "总资产", "浮动盈亏", "盈亏比例", "胜率",                                               // 交易盈亏。
				"初始资金", "剩余资金", "股票市值", "证券数量", "成本价格", "当前价格",                                  // 资产分布。
				"买卖详情", "交易次数", "买入次数", "卖出次数", "平均间隔", "最大间隔", "最小间隔",                         // 交易频率。
				"盈亏详情", "交易周期", "盈利周期", "亏损周期", "平均间隔", "最大间隔", "最小间隔",                         // 交易周期。
				"平均赢利", "平均亏损", "最大盈利", "最大亏损", "最小盈利", "最小亏损"                                   // 周期盈亏。
		};
		
		for (int i = 0; i < titleNameArray.length; i++) {
			createCellAndOutput(workbook, rowTitle, i, cellTitleStyle, titleNameArray[i]);
		}
	}
	
	/**
	 * 创建输出测试结果的默认标题单元的风格。
	 * 
	 * @param workbook XSSFWorkbook
	 * @return XSSFCellStyle
	 */
	private XSSFCellStyle createDefaultTitleCellStyle (XSSFWorkbook workbook) {
		XSSFFont font = createDefaultXSSFFont(workbook, true, true, new int[] {0, 0, 0});                // 设置标题头单元的字体。
		XSSFCellStyle cellStyle = createDefaultXSSFCellStyle(workbook, font, new int[] {192, 203, 216}); // 设置标题头单元的风格。
		return cellStyle;
	}
	
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
		
		cellStyle.setBorderColor(BorderSide.TOP, new XSSFColor(new java.awt.Color(0, 0, 0)));        // 单元格顶部线框的颜色。（黑色）
		cellStyle.setBorderColor(BorderSide.BOTTOM, new XSSFColor(new java.awt.Color(0, 0, 0)));     // 单元格底部线框的颜色。（黑色）
		cellStyle.setBorderColor(BorderSide.LEFT, new XSSFColor(new java.awt.Color(0, 0, 0)));       // 单元格左边线框的颜色。（黑色）
		cellStyle.setBorderColor(BorderSide.RIGHT, new XSSFColor(new java.awt.Color(0, 0, 0)));      // 单元格右边线框的颜色。（黑色）
		
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
		font.setFontName("微软雅黑");                                                  // 字体风格。
		font.setFontHeight(8);                                                      // 字体大小。
		
		return font;
	}
	
	/**
	 * 给sheet设置默认的风格。
	 * 
	 * @param sheet XSSFSheet
	 */
	private void setDefaultSheetStyle (XSSFSheet sheet) {
		sheet.setDefaultRowHeight((short)500); // 设置默认行高（实际行高为25，为什么会这样，应该有一套换算规则，但是我不清楚，这个值是试出来的）。
		sheet.setDefaultColumnWidth(12);       // 设置默认列宽。
		sheet.setColumnWidth(11, 15000);       // 设置买卖详细单元的列宽。（实际列宽为58，为什么会这样，应该有一套换算规则，但是我不清楚，这个值是试出来的）
		sheet.setColumnWidth(18, 15000);       // 设置盈亏详细单元的列宽。（实际列宽为58，为什么会这样，应该有一套换算规则，但是我不清楚，这个值是试出来的）
		sheet.setDisplayGridlines(true);       // 显示网格线。
	}
	
	/**
	 * 创建Excel列单元并输出内容。
	 * 
	 * @param workbook XSSFWorkbook
	 * @param row XSSFRow
	 * @param columnIndex 列单元序列号
	 * @param cellStyle 列单元风格
	 * @param value 要输出的内容
	 */
	private void 
	createCellAndOutput (XSSFWorkbook workbook, XSSFRow row, int columnIndex, XSSFCellStyle cellStyle, Object value) {
		
		if (value == null) {
			return;
		}
		
		XSSFCell cell = row.createCell(columnIndex);
		cell.setCellStyle(cellStyle);
		
		if (value.getClass().isAssignableFrom(String.class)) {			
			cell.setCellValue(String.class.cast(value));
		} else if (value.getClass().isAssignableFrom(Double.class)) {
			cell.setCellValue(Double.class.cast(value));
		} else if (value.getClass().isAssignableFrom(Boolean.class)) {
			cell.setCellValue(Boolean.class.cast(value));
		} else if (value.getClass().isAssignableFrom(Date.class)) {
			cell.setCellValue(Date.class.cast(value));
		} else if (value.getClass().isAssignableFrom(Calendar.class)) {
			cell.setCellValue(Calendar.class.cast(value));
		} else if (value.getClass().isAssignableFrom(ArrayList.class) || value.getClass().isAssignableFrom(LinkedList.class)) {
			
			/*
			 * 处理单元格是买卖详情时的情况，要求买字是红色，卖是绿色。
			 */
			if (columnIndex == 11) {
				StringBuilder builder = new StringBuilder();
				@SuppressWarnings("unchecked")
				List<String> buyDetailList = (List<String>)value;
				for (int i = 0; i < buyDetailList.size(); i++) {
					builder.append(buyDetailList.get(i));
					if (i != buyDetailList.size() - 1) {
						builder.append("|");
					}
				}
				
				if (builder.length() == 0) {
					cell.getCellStyle().setFont(createDefaultXSSFFont(workbook, false, false, new int[] {0, 112, 192}));
					cell.setCellValue("无相关数据");
					return;
				}
				
				RichTextString rts = workbook.getCreationHelper().createRichTextString(builder.toString());
				
				for (int i = 0; i < builder.length(); i++) {
					if (builder.charAt(i) == '买') {
						rts.applyFont(i, (i + 1), createDefaultXSSFFont(workbook, false, false, new int[] {192, 0, 0}));
					}
					if (builder.charAt(i) == '卖') {
						rts.applyFont(i, (i + 1), createDefaultXSSFFont(workbook, false, false, new int[] {0, 101, 65}));
					}
					if (builder.charAt(i) == '|') {
						rts.applyFont(i, (i + 1), createDefaultXSSFFont(workbook, false, false, new int[] {0, 112, 192}));
					}
					
				}

				cell.setCellValue(rts);
			}
			
			/*
			 * 处理单元格是盈亏详情时的情况，要求盈利是红色，亏损是绿色。
			 */
			if (columnIndex == 18) {
				StringBuilder builder = new StringBuilder();
				@SuppressWarnings("unchecked")
				List<BigDecimal> cyclePLDetailList = (List<BigDecimal>)value;
				for (int i = 0; i < cyclePLDetailList.size(); i++) {
					builder.append(cyclePLDetailList.get(i).toString());
					if (i != cyclePLDetailList.size() - 1) {
						builder.append("|");
					}
				}
				
				if (builder.length() == 0) {
					cell.getCellStyle().setFont(createDefaultXSSFFont(workbook, false, false, new int[] {0, 112, 192}));
					cell.setCellValue("无相关数据");
					return;
				}
				
				RichTextString rts = workbook.getCreationHelper().createRichTextString(builder.toString());
				
				int fromIndex = 0;
				int position = 0;
				while (position != -1) {
					position = builder.indexOf("|", fromIndex);
					if (position == -1) {
						if (builder.charAt(fromIndex) != '-') {
							rts.applyFont(fromIndex, builder.length(), createDefaultXSSFFont(workbook, false, false, new int[] {192, 0, 0}));
						} else {						
							rts.applyFont(fromIndex, builder.length(), createDefaultXSSFFont(workbook, false, false, new int[] {0, 101, 65}));
						}
						
						break;
					} 
					
					rts.applyFont(position, (position + 1), createDefaultXSSFFont(workbook, false, false, new int[] {0, 112, 192}));
					
					if (builder.charAt(fromIndex) != '-') {
						rts.applyFont(fromIndex, position, createDefaultXSSFFont(workbook, false, false, new int[] {192, 0, 0}));
					} else {						
						rts.applyFont(fromIndex, position, createDefaultXSSFFont(workbook, false, false, new int[] {0, 101, 65}));
					}
					
					
					fromIndex = (position + 1);
				}
				
				cell.setCellValue(rts);
			}
		}
	}
}