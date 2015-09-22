package com.huboyi.engine.merge;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.huboyi.engine.constant.MergeTimeType;
import com.huboyi.engine.load.LoadEngine;
import com.huboyi.engine.load.bean.StockDataBean;
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
		Map<String, List<StockDataBean>> stockDataListMap = load.getStockData();
		Map<String, List<StockDataBean>> mergeMap = mergeByTime.merge(load.getStockData(), MergeTimeType.MINUTE, 30);
		
	}
}