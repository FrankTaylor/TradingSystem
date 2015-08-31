package com.huboyi.system.snap.worker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 监控交易系统捕捉信号的工作类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/11/04
 * @version 1.0
 */
public class SnapMonitor implements Runnable {

	/** 日志。*/
	private static final Logger log = LogManager.getLogger(SnapMonitor.class);
	
	/** 需要监控的任务数量。*/
	private final Integer taskNums;
	/** 当前完成的任务数量。*/
	private final AtomicInteger completeTaskNums;
	
	/**
	 * 构造函数。
	 * 
	 * @param marketDataFilepathMap 需要载入行情数据的总个数
	 * @param currentReadMarketDataNum 当前已经载入的行情数据的个数
	 */
	public SnapMonitor (Integer taskNums, AtomicInteger completeTaskNums) {
		this.taskNums = taskNums;
		this.completeTaskNums = completeTaskNums;
	}

	@Override
	public void run() {
		log.info("准备开启交易系统捕捉信号监控线程。");
		Thread current = Thread.currentThread();
		String name = current.getName();
		try {
			while (!current.isInterrupted()) {
				BigDecimal fileNum = BigDecimal.valueOf(taskNums.intValue());
				BigDecimal readNum = BigDecimal.valueOf(completeTaskNums.get());
				
				BigDecimal rate = null;
				if (readNum.intValue() == 0) {
					rate = BigDecimal.valueOf(0);
				} else {
					rate = readNum.divide(fileNum, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));					
				}
				
				log.info("当前交易系统捕捉信号的进度为：" + rate.floatValue() + "%");
				TimeUnit.MILLISECONDS.sleep(10000);
			}
		} catch (InterruptedException e) {
			log.warn("[name = " + name + "]被挂起，可能已经完成了交易系统捕捉信号任务！");
		} catch (Exception e) {
			log.error("[name = " + name + "]在监控交易系统捕捉信号的过程中出现错误！");
			log.error(e);
		}
	}
	
}