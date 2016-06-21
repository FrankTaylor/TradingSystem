package com.huboyi.engine.merge;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.huboyi.data.entity.MarketDataBean;
import com.huboyi.data.entity.StockDataBean;
import com.huboyi.data.load.DataLoadEngine;
import com.huboyi.data.merge.MergeByTime;
import com.huboyi.data.merge.constant.MergeTimeType;
/**
 * 对{@link MergeByTime}的测试。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/09/22
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/config/engine/engine-spring.xml"})
public class TestMergeByTime {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestMergeByTime.class);
	
	@Autowired
	@Qualifier("dataLoadEngine")
	private DataLoadEngine dataLoadEngine;
	
	@Autowired
	@Qualifier("mergeByTime")
	private MergeByTime mergeByTime;
	
	@Test
	public void testMerge() {
		
		List<MarketDataBean> marketDataList = dataLoadEngine.loadMarketData();
		
		for (MarketDataBean marketData : marketDataList) {
			String stockCode = marketData.getStockCode();
			List<StockDataBean> originalStockDataList = marketData.getStockDataList();
			
			List<StockDataBean> mergeStockDataList = mergeByTime.merge(originalStockDataList, MergeTimeType.MINUTE_30);
			
			for (StockDataBean stockData : mergeStockDataList) {
				StringBuilder builder = new StringBuilder();
				builder
				.append("[")
				.append("stockCode = ").append(stockCode).append(",")
				.append("date = ").append(stockData.getDate()).append(",")
				.append("open = ").append(stockData.getOpen()).append(",")
				.append("high = ").append(stockData.getHigh()).append(",")
				.append("low = ").append(stockData.getLow()).append(",")
				.append("close = ").append(stockData.getClose()).append(",")
				.append("volume = ").append(stockData.getVolume()).append(",")
				.append("amount = ").append(stockData.getAmount())
				.append("]");
				
				log.info(builder.toString());
			}
		}
	}
}