package com.huboyi.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 提供对线程的操作。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/4/20
 * @version 1.0
 */
public class ThreadHelper {
	
	/**
	 * 得到监控执行顶底分型交易系统的线程池。
	 * 
	 * @return ExecutorService
	 */
	public static ExecutorService getMonitorThreadPool () {
		
		ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactory () {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("监控执行顶底分型交易系统的线程");
				t.setPriority(Thread.MAX_PRIORITY);
				return t;
			}
		});
		return es;
	}
	
	/**
	 * 得到执行顶底分型交易系统的线程池。
	 * 
	 * @param num 池中线程的数量
	 * @return ExecutorService
	 */
	public static ExecutorService getExecThreadPool (int num) {
		
		ExecutorService es = Executors.newFixedThreadPool(num, new ThreadFactory () {
			
			int threadNum = 1;
			
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("执行顶底分型交易系统的线程" + threadNum++);
				
				t.setUncaughtExceptionHandler(new UncaughtExceptionHandler () {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						throw new RuntimeException("执行顶底分型交易系统的线程在执行过程中出现错误！[线程ID = " + t.getId() + "、线程名称 = " + t.getName() + "]" + e);
					}
				});
				return t;
			}
		});
		return es;
	}
	
}