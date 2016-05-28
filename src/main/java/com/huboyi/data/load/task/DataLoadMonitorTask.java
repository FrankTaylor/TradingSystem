package com.huboyi.data.load.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.data.load.DataLoadEngine;

/**
 * 监控载入股票行情数据的工作类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @see DataLoadEngine
 * @since 1.5
 */
public class DataLoadMonitorTask implements Runnable {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(DataLoadMonitorTask.class);
	
	/** 需要载入行情数据的总个数。*/
	private final Integer marketDataFilepathNum;
	/** 当前已经载入的行情数据的个数。*/
	private final AtomicInteger currentReadMarketDataNum;
	/** 监控间隔时间（单位毫秒）。*/
	private final long monitoringInterval;
	
	/** 记录监控线程的运行状态。*/
	private volatile boolean isRun = false;
	
	/**
	 * 构造函数。
	 * 
	 * @param marketDataFilepathMap 需要载入行情数据的总个数
	 * @param currentReadMarketDataNum 当前已经载入的行情数据的个数
	 * @param monitoringInterval 监控间隔时间（单位毫秒）
	 */
	public DataLoadMonitorTask (Integer marketDataFilepathNum, AtomicInteger currentReadMarketDataNum, long monitoringInterval) {
		this.marketDataFilepathNum = marketDataFilepathNum;
		this.currentReadMarketDataNum = currentReadMarketDataNum;
		this.monitoringInterval = monitoringInterval;
	}
	
	@Override
	public void run() {
		log.info("准备开启行情数据载入监控线程。");
		Thread current = Thread.currentThread();
		String name = current.getName();
		isRun = true;
		
		try {
			while (true) {
				
				BigDecimal fileNum = BigDecimal.valueOf(marketDataFilepathNum.intValue());
				BigDecimal readNum = BigDecimal.valueOf(currentReadMarketDataNum.get());
				
				BigDecimal rate = null;
				if (readNum.intValue() == 0) {
					rate = BigDecimal.valueOf(0);
				} else {
					rate = readNum.divide(fileNum, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));					
				}
				
				// 如果读取比率达到 100%，则设置 “中断标志位” 退出监控。
				if (rate.floatValue() >= 100) {
					Thread.currentThread().interrupt();
				}
				
				log.info("当前载入行情数据的进度为：" + rate.floatValue() + "%");
				
				if (Thread.currentThread().isInterrupted() || !isRun) {
					break;
				}
				
				TimeUnit.MILLISECONDS.sleep(monitoringInterval);
			}
		} catch (Exception e) {
			log.error("[name = " + name + "]在监控行情数据载入的过程中出现错误！");
			log.error(e);
		}
	}
	
	/**
	 * 设置监控的运行状态为 false，以停止监控线程的运行（该方法更多是作为一个停止保险而已）。
	 */
	public void stop() {
		isRun = false;
	}
}