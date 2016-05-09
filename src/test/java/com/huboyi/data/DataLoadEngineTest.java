package com.huboyi.data;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.huboyi.data.entity.StockDataBean;
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
public class DataLoadEngineTest {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(DataLoadEngineTest.class);
	
	@Autowired
	@Qualifier("dataLoadEngine")
	private DataLoadEngine dataLoadEngine;
	
	@Test
	public void getStockData() {
		Map<String, List<StockDataBean>> map = dataLoadEngine.getStockData();
		
		log.info("共读取证券数据文件 = " + map.size() + " 个 \n\n\n");
		
		for (Map.Entry<String, List<StockDataBean>> entry : map.entrySet()) {
			String key = entry.getKey();
			List<StockDataBean> stockDataList = entry.getValue();
			
			log.info("证券代码 = " + key + ", 行情数据量 = " + stockDataList.size());
		}
	}
}