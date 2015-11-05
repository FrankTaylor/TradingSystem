package com.huboyi.system.test.module;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.huboyi.system.snap.engine.SnapFractalForDayEngine;

/**
 * 执行顶底分型日线交易系统捕捉交易信号。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/4/20
 * @version 1.0
 */
public class SnapFractalForDay {
	
	/** 执行顶底分型交易系统捕捉信号的引擎类。*/
	private static SnapFractalForDayEngine engine;
	
	static {
		ApplicationContext atx = 
			new ClassPathXmlApplicationContext(
					new String[] {
							"classpath:/config/engine/**/*-spring.xml", 
							"classpath:/config/system/**/*-spring.xml"});
		engine = SnapFractalForDayEngine.class.cast(atx.getBean("snapFractalForDayEngine"));
		
	}
	
	public static void main(String[] args) {
		execute();
	}
	
	// --- private method ---
	
	/**
	 * 执行系统测试。
	 * 
	 */
	private static void execute () {
		engine.executeSnap("D:\\日线顶底分型交易系统捕捉结果.txt");
	}
}