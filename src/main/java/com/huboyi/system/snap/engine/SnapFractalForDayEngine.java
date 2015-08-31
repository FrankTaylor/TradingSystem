package com.huboyi.system.snap.engine;

import java.util.ArrayList;
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

import com.huboyi.engine.load.LoadEngine;
import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.constant.OrderInfoTradeFlagEnum;
import com.huboyi.system.module.fractal.signal.calc.FractalDataCalculator;
import com.huboyi.system.module.fractal.signal.rule.FractalDealRuleForDay;
import com.huboyi.system.snap.bean.SnapResultBean;
import com.huboyi.system.snap.service.SpanEverySumPositionInfoService;
import com.huboyi.system.snap.worker.SnapFractalForDayWorker;
import com.huboyi.system.test.worker.TestMonitor;
import com.huboyi.util.IOHelper;
import com.huboyi.util.ThreadHelper;

/**
 * 执行顶底分型交易系统捕捉信号的引擎类。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/1/13
 * @version 1.0
 */
public class SnapFractalForDayEngine {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(SnapFractalForDayEngine.class);
	
	/** 装载股票数据的引擎类。*/
	@Resource(name="winLoadEngine")
	private LoadEngine loadEngine;
	
	/** 计算顶底分型交易系统中所需数据的计算类。*/
	@Resource 
	private FractalDataCalculator fractalDataCalculator;
	/** 顶底分型交易系统进出场规则。*/
	@Resource 
	private FractalDealRuleForDay fractalDealRuleForDay;
	
	/** 每一笔持仓信息Service。*/
	@Resource
	private SpanEverySumPositionInfoService spanEverySumPositionInfoService;

	/**
	 * 执行捕捉交易信号。
	 * 
	 * @param textOutputPath 交易信号文件路径
	 */
	public void executeSnap (String textOutputPath) {
		try {
			List<SnapResultBean> resultBeanList = execTradingSystem();                                                     // 用于保存执行结果。
			List<SnapResultBean> openSignalList = new ArrayList<SnapResultBean>();                                         // 用于保存建仓信号。
			List<SnapResultBean> closeSignalList = new ArrayList<SnapResultBean>();                                        // 用于保存平仓信号。
			
			for (SnapResultBean result : resultBeanList) {                                                                 // 把建仓和平仓交易信号保存到相应的集合中。
				if (result.getOrderInfoTradeFlagEnum() == OrderInfoTradeFlagEnum.STOCK_BUY) {
					openSignalList.add(result);
				} else {
					closeSignalList.add(result);
				}
			}
			
			// --- 把建仓交易信号放入集合中。 
			List<String> openInfoList = new ArrayList<String>();                                                           // 用于保存建仓信息。
			openInfoList.add("####################### 建仓信号 #######################");
			for (SnapResultBean result : openSignalList) {
				
				StringBuilder builder = new StringBuilder();
				builder
				.append("股票代码：").append(result.getStockCode()).append(", ")
				.append("信号日期：").append(result.getSignalDate()).append(", ")
				.append("信号类型").append(result.getSignalType()).append(", ")
				.append("信号名称").append(result.getSignalName()).append(", ")
				.append("建仓数量：").append(result.getTradeNumber());
				
				openInfoList.add(builder.toString());
			}
			
			// --- 把平仓交易信号放入集合中。
			List<String> closeInfoList = new ArrayList<String>();                                                          // 用于保存平仓信息。
			closeInfoList.add("\n####################### 平仓信号 #######################\n");
			for (SnapResultBean result : closeSignalList) {
				
				StringBuilder builder = new StringBuilder();
				builder
				.append("股票代码：").append(result.getStockCode()).append(", ")
				.append("信号日期：").append(result.getSignalDate()).append(", ")
				.append("信号类型").append(result.getSignalType()).append(", ")
				.append("信号名称").append(result.getSignalName()).append(", ")
				.append("建仓数量：").append(result.getTradeNumber());
				
				closeInfoList.add(builder.toString());
			}

			// --- 保存建仓和平仓交易信号。
			List<String> outputList = new ArrayList<String>();
			outputList.addAll(openInfoList);
			outputList.addAll(closeInfoList);
			IOHelper.saveFileToHardDisk(outputList, textOutputPath);
			
			System.out.println("执行完毕");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// --- private method ---
	
	/**
	 * 执行交易系统。
	 * 
	 * @return List<SnapResultBean>
	 */
	private List<SnapResultBean> execTradingSystem () {
		log.info("执行交易系统。");
		
		// 得到股票数据集合。
		Map<String, List<StockDataBean>> stockDataBeanMap = loadEngine.getStockData();
		
		// 开始时间。
		long startTime = System.nanoTime();
		
		List<SnapResultBean> resultBeanList = new ArrayList<SnapResultBean>();
		ExecutorService moniterExec = ThreadHelper.getMonitorThreadPool();                     // 得到监控测试顶底分型交易系统的线程池。
		ExecutorService workerExec = ThreadHelper.getExecThreadPool(3);                        // 得到测试顶底分型交易系统的线程池。
		
		try {
			// 1、启用一根线程对测试进度进行监控。
			AtomicInteger completeTaskNums = new AtomicInteger(0);
			moniterExec.execute(new TestMonitor(stockDataBeanMap.size(), completeTaskNums));
			moniterExec.shutdown();
			
			// 2、多线程测试顶底分型交易系统，同时把测试结果放到Bean中。
			resultBeanList = getSnapResultToBean(moniterExec, workerExec, stockDataBeanMap, completeTaskNums, 10000);
			
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
	 * @return List<SnapResultBean> 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private List<SnapResultBean> 
	getSnapResultToBean (
			ExecutorService moniter, ExecutorService worker,
			Map<String, List<StockDataBean>> stockDataBeanMap,
			AtomicInteger completeTaskNums, int readTimeout) 
			throws InterruptedException, ExecutionException, TimeoutException {
		
		List<SnapResultBean> resultBeanList = new ArrayList<SnapResultBean>();
		
		// 构造捕捉顶底分型交易系统信号的工作类。
		List<SnapFractalForDayWorker> workerList = getSnapFractalForDayWorkerList(stockDataBeanMap, completeTaskNums);
		
		// 使用线程池驱动任务。
		List<Future<SnapResultBean[]>> futureList = worker.invokeAll(workerList);
		worker.shutdown();
		
		if (null != futureList && !futureList.isEmpty()) {
			for (Future<SnapResultBean[]> future : futureList) {
				SnapResultBean[] resultBean = future.get(readTimeout, TimeUnit.MINUTES);
				if (resultBean != null && resultBean.length > 0) {
					for (SnapResultBean r : resultBean) {						
						resultBeanList.add(r);
					}
				}
			}
		}
		
		// 关闭监控线程池。
		moniter.shutdownNow();
		
		return resultBeanList;
	}
	
	// --- construct worker ---
	
	/**
	 * 构造捕捉顶底分型交易系统信号的工作类。
	 * 
	 * @param stockDataBeanMap 行情数据集合
	 * @param completeTaskNums 当前完成的任务数量
	 * @return List<SnapFractalForDayWorker> 
	 */
	private List<SnapFractalForDayWorker> 
	getSnapFractalForDayWorkerList (Map<String, List<StockDataBean>> stockDataBeanMap, AtomicInteger completeTaskNums) {
		log.info("构造捕捉顶底分型交易系统信号的工作类。");
		
		List<SnapFractalForDayWorker> workerList = new ArrayList<SnapFractalForDayWorker>();
		if (null == stockDataBeanMap || stockDataBeanMap.isEmpty()) {
			log.warn("装载股票行情数据文件路径的集合类中没有任何信息！");
			return workerList;
		}
		
		for (Map.Entry<String, List<StockDataBean>> entry : stockDataBeanMap.entrySet()) {
			
			String code = entry.getKey();                                                   // 证券代码。
			List<StockDataBean> stockDataBeanList = entry.getValue();                       // 股票行情数据。
			
			
			SnapFractalForDayWorker worker = new SnapFractalForDayWorker(                   // 构造捕捉交易信号线程。
					code,
					stockDataBeanList, 
					fractalDataCalculator, 
					fractalDealRuleForDay, 
					spanEverySumPositionInfoService,
					completeTaskNums);
			workerList.add(worker);
		}
		
		return workerList;
	}
}