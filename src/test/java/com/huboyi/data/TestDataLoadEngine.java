package com.huboyi.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.huboyi.data.entity.MarketDataBean;
import com.huboyi.data.load.DataLoadEngine;

/**
 * 对{@link com.huboyi.data.load.DataLoadEngine}的测试。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/09/22
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/config/data/spring-data-load.xml"})
public class TestDataLoadEngine {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestDataLoadEngine.class);
	
	@Autowired
	@Qualifier("dataLoadEngine")
	private DataLoadEngine dataLoadEngine;
	
	@Test
	public void getStockData() {
		
		// 测试次数。
		final int TEST_NUMBER = 1;
		// 采样率。
		final double SAMPLING_RATE = 0.02;
		
		BigDecimal sub_total = BigDecimal.valueOf(0);
		// 执行 50 次的随机抽取 20%  已读数据的验证。
		for (int a = 0; a < TEST_NUMBER; a++) {
			long start = System.nanoTime();
			
			List<MarketDataBean> marketDataList = dataLoadEngine.loadMarketData();
			Random r = new Random();
			for (int b = 0; b < (marketDataList.size() * SAMPLING_RATE); b++) {				
				MarketDataBean marketData = marketDataList.get(r.nextInt(marketDataList.size()));

				BufferedReader reader = null;
				
				int num = 0;
				String s;
				try {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(marketData.getDataPath())));
					while ((s = reader.readLine()) != null) {
						if (s.matches("[0-9]+.*")) {						
							num++;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {}
					}
				}
				
				if (num != marketData.getStockDataList().size()) {
					throw new RuntimeException("[读取的行情数据量 = " + marketData.getStockDataList().size() + "] 和 [数据源中的数据量 =  " + num + "]不匹配！" +
							"code = " + marketData.getCode() + 
							", 数据源路径" + marketData.getDataPath());
				}
				
				log.info("" +
						"[第 " + b + "条比对数据]" +
								"[读取的行情数据量 = " + marketData.getStockDataList().size() + "]" +
										"[数据源中的数据量 = " + num + "]" +
												"[编码 = " + marketData.getCode() + "]" +
														"[数据源路径 = " + marketData.getDataPath() + "]");
			}
			
			long end = System.nanoTime();
			sub_total = sub_total.add(BigDecimal.valueOf(end - start));
		}
		
		// 求出平均耗时。
		long sub_avg = sub_total.divide(BigDecimal.valueOf(TEST_NUMBER), 0, RoundingMode.HALF_UP).longValue();
		log.error("行情数据读取无误，本次测试耗时 = " + TimeUnit.NANOSECONDS.toSeconds((sub_avg)) + " 秒");
	}
}