package com.huboyi.run.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.data.entity.MarketDataBean;
import com.huboyi.data.entity.StockDataBean;
import com.huboyi.run.entity.StarategyRunResultBean;
import com.huboyi.strategy.BaseStrategy;

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
	
	/** 策略抽象类。*/
	private final BaseStrategy starategy;
	
	/** 股东代码。*/
    private String stockholder;
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
	 * @param starategy BaseStrategy
	 * @param stockholder 股东代码
	 * @param initMoney 交易初始资金
	 * @param startMonitorTask 是否启动监听线程
	 * @param completeStrategyNums 已完成策略的数量
	 */
	public RunStrategyTask (
			MarketDataBean marketData, BaseStrategy starategy, String stockholder, 
			double initMoney, boolean startMonitorTask, AtomicInteger completeStrategyNums) {
		
		this.marketData = marketData;
		this.starategy = starategy;
		
		this.stockholder = stockholder;
		this.initMoney = new BigDecimal(initMoney).setScale(3, RoundingMode.HALF_UP);
		
		this.startMonitorTask = startMonitorTask;
		this.completeStrategyNums = completeStrategyNums;	
	}
	
	@Override
	public StarategyRunResultBean call() throws Exception {
		
		/* --- 从 MarketDataBean 类中得到 “股票代码”、“股票名称” 和 “股票数据集合” --- */
		final String stockCode = marketData.getStockCode();
		final String stockName = marketData.getStockName();
		final List<StockDataBean> stockDataList = marketData.getStockDataList();
		
		starategy.setStockCode(stockCode);
		starategy.setStockName(stockName);
		
		Thread current = Thread.currentThread();
		log.info("当前线程[name = " + current.getName() + "]正在对[证券代码：" + stockCode + "]执行顶底分型交易系统测试任务。");
		
		// 开始时间。
		long startTime = System.nanoTime();
		
		// 策略运行结果。
		StarategyRunResultBean runResultBean = new StarategyRunResultBean();
		
		try {
			
			// 股票数据迭代器。
			Iterator<StockDataBean> stockDataIterator = stockDataList.iterator();
			
			// 策略运行过程中的当前行情数据。
			StockDataBean runStockDataBean = null;
			// 策略运行过程中的行情序列数据。
			List<StockDataBean> runStockDataList = new ArrayList<StockDataBean>();
			
			while (stockDataIterator.hasNext()) {
				
				runStockDataBean = stockDataIterator.next();
				runStockDataList.add(runStockDataBean);
				
				starategy.setStockData(runStockDataBean);
				starategy.setStockDataList(runStockDataList);
				
				/*
				 * +-----------------------------------------------------------+
				 * + 一般而言股票的价格不能小于0，但是经过前复权的计算股票价格会小于0（阳泉煤业），这导致    +
				 * + 了我测试总是报错。因此，一旦股票价格小于0，就不再进行买卖和刷新仓位。                                   +
				 * +-----------------------------------------------------------+
				 */
				if (runStockDataBean.getOpen().doubleValue() <= 0) {
					continue;
				}
				
				if (starategy.preProcessStockData(runStockDataBean, runStockDataList)) {
					starategy.processStockData(runStockDataBean, runStockDataList);
					starategy.postProcessStockData(runStockDataBean, runStockDataList);
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