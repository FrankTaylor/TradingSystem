package com.huboyi.system.snap.service;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.huboyi.system.module.fractal.signal.bean.FractalPositionInfoBean;
import com.huboyi.system.module.fractal.signal.constant.FractalDealSignalEnum;
import com.huboyi.system.snap.service.impl.SpanEverySumPositionInfoServiceImpl;
/**
 * 对{@link EverySumPositionInfoRepository}的测试。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/1/3
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/config/system/snap/db/mysql/mysql-spring.xml", "/config/system/snap/db/mysql/repository-spring.xml", "/config/system/snap/service-spring.xml", "/config/engine/engine-spring.xml"})
public class TestSpanEverySumPositionInfoServiceImpl {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestSpanEverySumPositionInfoServiceImpl.class);

	/** 每一笔持仓信息Service。*/
	@Resource
	private SpanEverySumPositionInfoService spanEverySumPositionInfoService;

	/**
	 * test {@link SpanEverySumPositionInfoServiceImpl#insert} method.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInsert () throws Exception {
		// 开始时间。
		long startTime = System.nanoTime();

		spanEverySumPositionInfoService.insert(
				"顶底分型交易系统", "SZ300144", "宋城演艺", 
				FractalDealSignalEnum.FIBO_B.getType(), FractalDealSignalEnum.FIBO_B.getName(), 20150420L, 
				20150421, 20150421093000L, new BigDecimal(55.00), 
				100L, new BigDecimal(120), "672288");
		
		// 结束时间。
		long endTime = System.nanoTime();
		
		System.out.println("此次测试共花费：" + (endTime - startTime) / 1000000000 + "秒");
	}
	
	/**
	 * test {@link SpanEverySumPositionInfoServiceImpl#findAllPositionInfoByStockCode} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testFindAllPositionInfoByStockCode () throws Exception {
		// 开始时间。
		long startTime = System.nanoTime();

		List<FractalPositionInfoBean> positionInfoList = spanEverySumPositionInfoService.findAllPositionInfoByStockCode("SZ000158");
		System.out.println(positionInfoList);
		
		// 结束时间。
		long endTime = System.nanoTime();
		
		System.out.println("此次测试共花费：" + (endTime - startTime) / 1000000000 + "秒");
	}
	
}