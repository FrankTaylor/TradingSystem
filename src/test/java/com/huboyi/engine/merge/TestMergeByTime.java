package com.huboyi.engine.merge;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.huboyi.data.load.bean.StockDataBean;
import com.huboyi.engine.constant.MergeTimeType;
import com.huboyi.engine.load.LoadEngine;
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
	
	@Resource(name = "winLoadEngine")
	private LoadEngine load;
	@Resource
	private MergeByTime mergeByTime;
	
	@Test
	public void testMerge() {
		Map<String, List<StockDataBean>> mergeStockDataListMap = mergeByTime.merge(load.getStockData(), MergeTimeType.MINUTE_30);
		
		for (Map.Entry<String, List<StockDataBean>> entrySet : mergeStockDataListMap.entrySet()) {
			String stockCode = entrySet.getKey();
			List<StockDataBean> stockDataList = entrySet.getValue();
			
			System.out.println("stockDataList.size() = " + stockDataList.size());
			for (StockDataBean stockData : stockDataList) {
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
				
				System.out.println(builder.toString());
			}
		}
	}
}