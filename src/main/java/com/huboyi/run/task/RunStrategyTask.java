package com.huboyi.run.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.data.entity.MarketDataBean;
import com.huboyi.data.entity.StockDataBean;
import com.huboyi.run.entity.StarategyRunResultBean;
import com.huboyi.strategy.IBaseStrategy;

/**
 * 该任务主要用于，执行各用策略。</p>
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class RunStrategyTask implements Callable<StarategyRunResultBean> {

	/** 日志。*/
	private static final Logger log = LogManager.getLogger(RunStrategyTask.class);
	
	/** 市场行情数据。*/
	private final MarketDataBean marketData;
	
	/** 策略接口。*/
	private final IBaseStrategy starategy;
	
	/** 交易初始资金。*/
	private final BigDecimal initMoney;
	
	/** 是否启动监听线程。*/
	private final boolean startMonitorTask;
	/** 已完成策略的数量。*/
	private final AtomicInteger completeStrategyNums;
	
	/**
	 * 构造函数。
	 * 
	 * @param marketData MarketDataBean
	 * @param starategy IBaseStrategy
	 * @param initMoney 交易初始资金
	 * @param startMonitorTask 是否启动监听线程
	 * @param completeStrategyNums 已完成策略的数量
	 */
	public RunStrategyTask (
			MarketDataBean marketData, IBaseStrategy starategy, double initMoney,
			boolean startMonitorTask, AtomicInteger completeStrategyNums) {
		
		this.marketData = marketData;
		this.starategy = starategy;
		
		this.initMoney = new BigDecimal(initMoney).setScale(3, RoundingMode.HALF_UP);
		
		this.startMonitorTask = startMonitorTask;
		this.completeStrategyNums = completeStrategyNums;
	}
	
	@Override
	public StarategyRunResultBean call() throws Exception {
		
		final String stockCode = marketData.getStockCode();
		final String stockName = marketData.getStockName();
		final List<StockDataBean> stockDataList = marketData.getStockDataList();
		
		Thread current = Thread.currentThread();
		log.info("当前线程[name = " + current.getName() + "]正在对[证券代码：" + stockCode + "]执行顶底分型交易系统测试任务。");
		
		// 开始时间。
		long startTime = System.nanoTime();
		
		// 策略运行结果。
		StarategyRunResultBean runResultBean = new StarategyRunResultBean();
		
		try {
			
			// 装载用于运行策略的行情数据。
			List<StockDataBean> curStockDataList = new ArrayList<StockDataBean>();

			for (int i = 0; i < stockDataList.size(); i++) {
				
				StockDataBean curStockDataBean = stockDataList.get(i);
				curStockDataList.add(curStockDataBean);
				
				/*
				 * +-----------------------------------------------------------+
				 * + 一般而言股票的价格不能小于0，但是经过前复权的计算股票价格会小于0（阳泉煤业），这导致    +
				 * + 了我测试总是报错。因此，一旦股票价格小于0，就不再进行买卖和刷新仓位。                                   +
				 * +-----------------------------------------------------------+
				 */
				if (curStockDataBean.getOpen().doubleValue() <= 0) {
					continue;
				}
				
				if (starategy.preProcessStockData(curStockDataBean, curStockDataList)) {
					starategy.processStockData(curStockDataBean, curStockDataList);
					starategy.postProcessStockData(curStockDataBean, curStockDataList);
				}
			}
			
			// 由于 CAS 在多线程竞争时有性能消耗，如果不需要监控，则可以避免消耗，从而加快读取速度。
			if (startMonitorTask) {
				completeStrategyNums.addAndGet(1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("当前线程 [name = " + current.getName() + "] 在对 [证券代码：" + stockCode + "] 运行策略的过程中出现错误！", e);
		}
		
		// 结束时间。
		long endTime = System.nanoTime();
		
		// 耗时。
		long elapsedTime = TimeUnit.NANOSECONDS.toSeconds((endTime - startTime));
		
		log.info("当前线程 [name = " + current.getName() + "] 完成对 [证券代码：" + stockCode + "] 的策略，耗时 = " + elapsedTime +" 秒");
		
		return runResultBean;
	}
	
}