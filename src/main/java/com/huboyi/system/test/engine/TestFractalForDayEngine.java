package com.huboyi.system.test.engine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.data.load.bean.StockDataBean;
import com.huboyi.engine.load.LoadEngine;
import com.huboyi.system.bean.DealSignalBean;
import com.huboyi.system.constant.DealSignal;
import com.huboyi.system.module.fractal.signal.calc.FractalDataCalculator;
import com.huboyi.system.module.fractal.signal.rule.FractalDealRuleForDay;
import com.huboyi.system.test.bean.TestResultBean;
import com.huboyi.system.test.output.TestResultOutputExcel;
import com.huboyi.system.test.rule.TestFractalPositionInfoRule;
import com.huboyi.system.test.worker.TestFractalForDayWorker;
import com.huboyi.system.test.worker.TestMonitor;
import com.huboyi.util.IOHelper;
import com.huboyi.util.ThreadHelper;

/**
 * 测试顶底分型交易系统的引擎类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/1/13
 * @version 1.0
 */
public class TestFractalForDayEngine {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(TestFractalForDayEngine.class);
	
	/** 装载股票数据的引擎类。*/
	@Resource(name="winLoadEngine")
	private LoadEngine loadEngine;

	/** 顶底分型交易系统仓位控制规则。*/
	@Resource 
	private TestFractalPositionInfoRule testFractalPositionInfoRule;
	/** 把测试结果输出到Excel中。*/
	@Resource 
	private TestResultOutputExcel testResultOutputExcel; 
	
	/**
	 * 执行测试并输出结果。
	 */
	public void executeTest (String excelOutputPath) {
		try {
			List<TestResultBean> resultBeanList = execTradingSystem();
			testResultOutputExcel.outputTestResult(resultBeanList, excelOutputPath);
			System.out.println("执行完毕");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 执行捕捉交易信号。
	 * 
	 * @param dealSignalOutputPath 交易信号文件路径
	 */
	public void executeSnapDealSignal (String dealSignalOutputPath) {
		try {
			List<TestResultBean> resultBeanList = execTradingSystem();                                       // 用于保存执行结果。
			Map<String, DealSignalBean> lastOpenDealSignalMap = new HashMap<String, DealSignalBean>();       // 用于保存最后一个交易信号是建仓信号。
			Map<String, DealSignalBean> lastCloseDealSignalMap = new HashMap<String, DealSignalBean>();      // 用于保存最后一个交易信号是平仓信号。
			
			for (TestResultBean result : resultBeanList) {                                                   // 把建仓和平仓交易信号保存到相应的集合中。
				if (result != null) {					
					DealSignalBean lastDealSignal = result.getLastDealSignal();
					if (lastDealSignal != null) {
						if (lastDealSignal.getType() == DealSignal.ONE_B || lastDealSignal.getType() == DealSignal.FIBO_B) {
							lastOpenDealSignalMap.put(result.getStockCode(), lastDealSignal);
						} else {
							lastCloseDealSignalMap.put(result.getStockCode(), lastDealSignal);
						}
					}
				}
			}
			
			// --- 把建仓交易信号放入集合中。 
			List<String> lastOpenInfoList = new ArrayList<String>();
			lastOpenInfoList.add("####################### 建仓信号 #######################");
			for (Map.Entry<String, DealSignalBean> entry : lastOpenDealSignalMap.entrySet()) {
				String stockCode = entry.getKey();                                   // 证券代码。
				StockDataBean stockDataBean = entry.getValue().getStockDataBean();   // 发出交易信号的行情信息。
				DealSignal type = entry.getValue().getType();                        // 信号类别。
				
				BigDecimal totalFundsBalance = new BigDecimal(100000);               // 假设总资金。
				BigDecimal buyMoneyRate = new BigDecimal(0.02);                      // 2%原则。
				BigDecimal budgetMoney = totalFundsBalance.multiply(buyMoneyRate);   // 计算预算资金。
				Long tradeNumber =                                                   // 计算成交数量。
		    		budgetMoney.divide(stockDataBean.getClose(), 0, RoundingMode.HALF_UP).longValue();
				tradeNumber -= (tradeNumber % 100);
		    	if (tradeNumber == null || tradeNumber < 100 || (tradeNumber % 100) != 0) {
		    		tradeNumber = 100L;
		    		budgetMoney = 
		    			new BigDecimal(tradeNumber).multiply(stockDataBean.getClose()).setScale(2, RoundingMode.HALF_UP);
		    	}
				
				
				StringBuilder builder = new StringBuilder();
				builder
				.append("股票代码：").append(stockCode).append(", ")
				.append("信号日期：").append(stockDataBean.getDate()).append(", ")
				.append("信号类型").append(type.getType()).append(", ")
				.append("信号名称").append(type.getName()).append(", ")
				.append(" | 假设总资金为" + totalFundsBalance + "元 |，建议开仓资金：").append(budgetMoney.doubleValue()).append(", ")
				.append("建议开仓数量：").append(tradeNumber);
				
				lastOpenInfoList.add(builder.toString());
			}
			
			// --- 把平仓交易信号放入集合中。
			List<String> lastCloseInfoList = new ArrayList<String>();
			lastCloseInfoList.add("\n####################### 平仓信号 #######################\n");
			for (Map.Entry<String, DealSignalBean> entry : lastCloseDealSignalMap.entrySet()) {
				String stockCode = entry.getKey();                                   // 证券代码。
				StockDataBean stockDataBean = entry.getValue().getStockDataBean();   // 发出交易信号的行情信息。
				DealSignal type = entry.getValue().getType();                        // 信号类别。
				
				
				StringBuilder builder = new StringBuilder();
				builder
				.append("股票代码：").append(stockCode).append(", ")
				.append("信号日期：").append(stockDataBean.getDate()).append(", ")
				.append("信号类型：").append(type.getType()).append(", ")
				.append("信号名称：").append(type.getName()).append(", ")
				.append("建议平仓数量：").append(type.getName());
				
				lastCloseInfoList.add(builder.toString());
			}
			
			// --- 保存建仓和平仓交易信号。
			List<String> outputList = new ArrayList<String>();
			outputList.addAll(lastOpenInfoList);
			outputList.addAll(lastCloseInfoList);
			IOHelper.saveFileToHardDisk(outputList, dealSignalOutputPath);
			
			System.out.println("执行完毕");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// --- private method ---
	
	/**
	 * 执行交易系统。
	 * 
	 * @return List<TestResultBean>
	 */
	private List<TestResultBean> execTradingSystem () {
		log.info("执行交易系统。");
		
		// 得到股票数据集合。
		Map<String, List<StockDataBean>> stockDataBeanMap = loadEngine.getStockData();
		
		// 开始时间。
		long startTime = System.nanoTime();
		
		List<TestResultBean> resultBeanList = new ArrayList<TestResultBean>();
		ExecutorService moniterExec = ThreadHelper.getMonitorThreadPool();                     // 得到监控测试顶底分型交易系统的线程池。
		ExecutorService workerExec = ThreadHelper.getExecThreadPool(60);                       // 得到测试顶底分型交易系统的线程池。
		
		try {
			// 1、启用一根线程对测试进度进行监控。
			AtomicInteger completeTaskNums = new AtomicInteger(0);
			moniterExec.execute(new TestMonitor(stockDataBeanMap.size(), completeTaskNums));
			moniterExec.shutdown();
			
			// 2、多线程测试顶底分型交易系统，同时把测试结果放到Bean中。
			resultBeanList = getTestResultToBean(moniterExec, workerExec, stockDataBeanMap, completeTaskNums, 10000);
			
			// 3、对测试结果进行排序（这里是倒排，总资产越大越靠前）。
			Collections.sort(resultBeanList);
			
		} catch (Exception e) {
			log.error(e);
		}
		
		// 结束时间。
		long endTime = System.nanoTime();
		
		log.info("此次测试顶底分型交易系统共花费：" + (endTime - startTime) / 1000000000 + "秒");
		
		return resultBeanList;
	}
	
	
	
	/**
	 * 读取测试数据到Bean中。
	 * 
	 * @param moniter 驱动监控任务的线程池
	 * @param worker 驱动工作任务的线程池
	 * @param stockDataBeanMap 装载股票行情数据的集合
	 * @param completeTaskNums 当前完成的任务数量
	 * @param readTimeout 读取超时
	 * @return List<NewTestResultBean> 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private List<TestResultBean> 
	getTestResultToBean (
			ExecutorService moniter, ExecutorService worker,
			Map<String, List<StockDataBean>> stockDataBeanMap,
			AtomicInteger completeTaskNums, int readTimeout) 
			throws InterruptedException, ExecutionException, TimeoutException {
		
		List<TestResultBean> resultBeanList = new ArrayList<TestResultBean>();
		
		// 构造执行顶底分型交易系统测试任务的工作类。
		List<TestFractalForDayWorker> workerList = getTestFractalForDayWorkerList(stockDataBeanMap, completeTaskNums);
		
		// 使用线程池驱动任务。
		List<Future<TestResultBean>> futureList = worker.invokeAll(workerList);
		worker.shutdown();
		
		if (null != futureList && !futureList.isEmpty()) {
			for (Future<TestResultBean> future : futureList) {
				TestResultBean resultBean = future.get(readTimeout, TimeUnit.MINUTES);
				if (resultBean != null) {					
					resultBeanList.add(resultBean);
				}
			}
		}
		
		// 关闭监控线程池。
		moniter.shutdownNow();
		
		return resultBeanList;
	}
	
	// --- construct worker ---
	
	/**
	 * 构造测试顶底分型交易系统任务的工作类。
	 * 
	 * @param stockDataBeanMap 行情数据集合
	 * @param completeTaskNums 当前完成的任务数量
	 * @return List<TestFractalForDayWorker> 
	 */
	private List<TestFractalForDayWorker> 
	getTestFractalForDayWorkerList (Map<String, List<StockDataBean>> stockDataBeanMap, AtomicInteger completeTaskNums) {
		log.info("构造测试顶底分型交易系统任务的工作类。");
		
		List<TestFractalForDayWorker> workerList = new ArrayList<TestFractalForDayWorker>();
		if (null == stockDataBeanMap || stockDataBeanMap.isEmpty()) {
			log.warn("装载股票行情数据文件路径的集合类中没有任何信息！");
			return workerList;
		}
		
		for (Map.Entry<String, List<StockDataBean>> entry : stockDataBeanMap.entrySet()) {
			
			String code = entry.getKey();                                                   // 证券代码。
			BigDecimal initMoney = BigDecimal.valueOf(100000);                              // 初始资金。
			List<StockDataBean> stockDataBeanList = entry.getValue();                       // 股票行情数据。
			
			
			TestFractalForDayWorker worker = new TestFractalForDayWorker(
					code, 
					initMoney,
					stockDataBeanList, 
					new FractalDataCalculator(), 
					new FractalDealRuleForDay(), 
					testFractalPositionInfoRule, 
					completeTaskNums);
			workerList.add(worker);
		}
		
		return workerList;
	}
}