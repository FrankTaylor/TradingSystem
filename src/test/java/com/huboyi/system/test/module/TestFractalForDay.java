package com.huboyi.system.test.module;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.huboyi.deal.auxiliary.graphical.ZhaoShangZhengQuan;
import com.huboyi.engine.indicators.technology.PatternAlogrithm;
import com.huboyi.engine.indicators.technology.pattern.bean.BandBean;
import com.huboyi.engine.indicators.technology.pattern.bean.FractalBean;
import com.huboyi.engine.indicators.technology.pattern.bean.PowerBean;
import com.huboyi.engine.load.LoadEngine;
import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.test.db.TestEverySumPositionInfoRepository;
import com.huboyi.system.test.engine.TestFractalForDayEngine;
import com.huboyi.util.IOHelper;

/**
 * 测试顶底分型日线交易系统。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/1/3
 * @version 1.0
 */
public class TestFractalForDay {
	
	/** 装载股票数据的引擎类。*/
	private static LoadEngine loadEngine;
	/** 测试顶底分型交易系统的引擎类。*/
	private static TestFractalForDayEngine engine;
	/** 每一笔持仓信息DAO。*/
	private static TestEverySumPositionInfoRepository testEverySumPositionInfoRepository;
	
	static {
		ApplicationContext atx = 
			new ClassPathXmlApplicationContext(
					new String[] {
							"classpath:/config/engine/**/*-spring.xml", 
							"classpath:/config/system/**/*-spring.xml"});
		
		loadEngine = LoadEngine.class.cast(atx.getBean("winLoadEngine"));
		engine = TestFractalForDayEngine.class.cast(atx.getBean("testFractalForDayEngine"));
		testEverySumPositionInfoRepository = TestEverySumPositionInfoRepository.class.cast(atx.getBean("testEverySumPositionInfoRepositoryWithRedis"));
		
	}
	
	public static void main(String[] args) {
		execute();
//		outputTestResult();
		
//		Map<String, List<StockDataBean>> map = loadEngine.getStockData();
//		List<StockDataBean> stockDataBeanList = map.get("SZ000049");
//		
//		// 得到没有包含关系的K线集合。
//		List<StockDataBean> noContainKLineList = PatternAlogrithm.getNoContainKLineList(stockDataBeanList);
//
//		// 有效的顶底分型集合。
//		List<FractalBean> fractalBeanList = (PatternAlogrithm.getFractalBeanList(noContainKLineList));
//		List<FractalBean> validFractalBeanList = PatternAlogrithm.getValidFractalBeanList(fractalBeanList);
//		
//		for (FractalBean f : fractalBeanList) {
//			if (f.getCenter().getDate().equals(19961212) || f.getLeft().getDate().equals(19961212) || f.getRight().getDate().equals(19961212)) {
//				
//				System.out.println("--------------------- = " + f);
//			}
//		}
		
		
		// 波段集合。
//		bean.setBandBeanList(PatternAlogrithm.getBandBeanList(bean.getValidFractalBeanList(), stockDataBeanList));
	}
	
	// --- private method ---
	
	/**
	 * 执行系统测试。
	 * 
	 */
	private static void execute () {
		engine.executeTest("D:\\日线顶底分型交易系统测试结果.xlsx");
	}
	
	/**
	 * 输出测试的结果。
	 * 
	 */
	private static void outputTestResult () {
		// 转入股票的行情数据。
		Map<String, List<StockDataBean>> stockDataBeanMap = loadEngine.getStockData();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
			try {
				String s;
				while ((s = br.readLine()) != null) {
					
					String stockCode = "";
					Integer date = 0;
					
					String[] arrays = s.split(" ");
					if (arrays.length == 2) {
						stockCode = arrays[0];
						date = Integer.valueOf(arrays[1]);
					} else {
						stockCode = s;
					}
					
					// 读取某一行情数据。
					List<StockDataBean> sdBeanList = stockDataBeanMap.get(stockCode);
					
					if (date > 0) {
						List<StockDataBean> temp = sdBeanList;
						sdBeanList = new ArrayList<StockDataBean>();
						for (StockDataBean ss : temp) {
							if (ss.getDate() < date) {
								sdBeanList.add(ss);
							}
						}
					}
					
					if (null != sdBeanList && !sdBeanList.isEmpty()) {
						// 得到没有包含关系的K线集合。
						List<StockDataBean> noContainKLineList = PatternAlogrithm.getNoContainKLineList(sdBeanList);
						// 找出顶底分型集合。
						List<FractalBean> fractalBeanList = PatternAlogrithm.getFractalBeanList(noContainKLineList);
						// 过滤掉无效的顶底分型，返回有效的顶底分型集合。
						List<FractalBean> validFractalBeanList = PatternAlogrithm.getValidFractalBeanList(fractalBeanList);
						// 得到行情波段集合。
						List<BandBean> bandBeanList = PatternAlogrithm.getBandBeanList(validFractalBeanList, sdBeanList);
						// 得到中枢信息集合。
						List<PowerBean> powerBeanList = PatternAlogrithm.getPowerBeanList(bandBeanList.get(0));
						// 得到没有包含关系的中枢集合。
						List<PowerBean> noContainPowerBeanList = PatternAlogrithm.getNoContainPowerBeanList(powerBeanList);
						
						// --- 计算信息 ---
						// 得到展示波段的代码。
						List<String> bandCodeList = ZhaoShangZhengQuan.getShowBandCode(bandBeanList);
						// 得到展示中枢的代码。
						List<String> powerCodeList = ZhaoShangZhengQuan.getShowPowerCode(noContainPowerBeanList);
						
						// 得到展示波段和中枢的代码。
						List<String> bandAndpowerCodeList = new ArrayList<String>();
						bandAndpowerCodeList.addAll(bandCodeList);
						bandAndpowerCodeList.addAll(powerCodeList);
						bandAndpowerCodeList.addAll(ZhaoShangZhengQuan.getShowBuyAndSellCode(testEverySumPositionInfoRepository.findEverySumPositionInfoList(s, null, null, null, null, null, null)));
						
						// --- 计算信息 ---
						IOHelper.saveFileToHardDisk(bandAndpowerCodeList, "D:\\" + s + "波段中枢买卖点集成.txt");

						System.out.println("输出完毕");

					}
				}

			} finally {
				if (br != null) {
					br.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}