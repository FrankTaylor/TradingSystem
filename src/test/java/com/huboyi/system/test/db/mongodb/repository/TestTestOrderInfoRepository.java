package com.huboyi.system.test.db.mongodb.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.huboyi.system.constant.OrderInfoTradeFlag;
import com.huboyi.system.po.OrderInfoPO;
import com.huboyi.system.test.db.TestOrderInfoRepository;
/**
 * 对{@link OrderInfoRepository}的测试。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/1/2
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/config/system/test/db/mongodb/mongodb-spring.xml", "/config/system/test/db/mongodb/repository-spring.xml"})
public class TestTestOrderInfoRepository {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestTestOrderInfoRepository.class);

	/** 证券代码。*/
	private static final String STOCK_CODE = "000518";
	/** 证券名称。*/
	private static final String STOCK_NAME = "常山股份";
	
	private static final Random random = new Random();
	
	/** 订单信息DAO。*/
	@Resource(name = "testOrderInfoRepositoryWithMongoDB")
	private TestOrderInfoRepository testOrderInfoRepository;
	
	/**
	 * test {@link OrderInfoRepository#createIndex} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testCreateIndex () throws Exception {
		testOrderInfoRepository.createIndex(STOCK_CODE);
	}
	
	/**
	 * test {@link OrderInfoRepository#insert} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testInsert () throws Exception {
		for (int i = 0; i < 50000; i++) {
			OrderInfoPO po = new OrderInfoPO();
			/* 合同编号。 */
			po.setContractCode(UUID.randomUUID().toString());
			/* 系统名称。 */
			po.setSystemName("顶底分型交易系统");
			/* 证券代码。 */
			po.setStockCode(STOCK_CODE);
			/* 证券名称。 */
			po.setStockName(STOCK_NAME);
			
			/* 交易日期。 */
			String tradeDateYear = "" + (2010 + random.nextInt(10));
			String tradeDateMonth = "" + (random.nextInt(12));
			tradeDateMonth = tradeDateMonth.length() == 1 ? "0" + tradeDateMonth : tradeDateMonth;
			String tradeDateDay = "" + (random.nextInt(31));
			tradeDateDay = tradeDateDay.length() == 1 ? "0" + tradeDateDay : tradeDateDay;
			po.setTradeDate(Long.valueOf((tradeDateYear + tradeDateMonth + tradeDateDay + "000000000")));
			/* 买卖标志。*/
			po.setTradeFlag(random.nextInt(1) == 0 ? OrderInfoTradeFlag.STOCK_BUY.getType() : OrderInfoTradeFlag.STOCK_SELL.getType());
			
			/* 成交价格。 */
			po.setTradePrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
			/* 成交数量。 */
			po.setTradeNumber(100L);
			/* 成交金额。 */
			po.setTradeMoney(new BigDecimal(random.nextInt(900000)).setScale(3, RoundingMode.HALF_UP));
			
			testOrderInfoRepository.insert(po);
		}
	}

	/**
	 * test {@link OrderInfoRepository#findOrderInfoList} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testFindOrderInfoList () throws Exception {
		log.info(testOrderInfoRepository.findOrderInfoList(STOCK_CODE, 20190101, 20191230, null, null).size());
	}
	
	/**
	 * test {@link OrderInfoRepository#dropCollection} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testDropCollection () throws Exception {
		testOrderInfoRepository.dropCollection(STOCK_CODE);
	}
}