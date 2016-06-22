package com.huboyi.run;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.huboyi.data.entity.MarketDataBean;
import com.huboyi.data.load.DataLoadEngine;
import com.huboyi.run.entity.StarategyRunResultBean;
import com.huboyi.run.task.RunStrategyTask;
import com.huboyi.strategy.impl.ChanLunStrategy;

public class RunStrategyEngine {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(RunStrategyEngine.class);

	public static void main(String[] args) {
		
		ApplicationContext atx = 
				new ClassPathXmlApplicationContext(
						new String[] {
								"classpath:/config/data/spring-data-load.xml"});
		
		DataLoadEngine dataLoadEngine = DataLoadEngine.class.cast(atx.getBean("dataLoadEngine"));
		List<MarketDataBean> marketDataList = dataLoadEngine.loadMarketData();
		
		ExecutorService es = Executors.newFixedThreadPool(36, new ThreadFactory() {
			
			int threadNum = 1;
			
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("运行策略的线程 " + threadNum++);
				
				t.setUncaughtExceptionHandler(new UncaughtExceptionHandler () {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						log.error("运行策略的线程在执行过程中出现错误！[线程ID = " + t.getId() + "、线程名称 = " + t.getName() + "]" + e);
					}
				});
				return t;
			}
		});
		
		List<Callable<StarategyRunResultBean>> taskList = new ArrayList<>();
		for (MarketDataBean marketData : marketDataList) {
			taskList.add(new RunStrategyTask(marketData, new ChanLunStrategy(), "123456", 10000, false, null));
		}
		
		try {
			es.invokeAll(taskList);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}