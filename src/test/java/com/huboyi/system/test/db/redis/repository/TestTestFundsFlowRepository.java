//package com.huboyi.system.test.db.redis.repository;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.Random;
//import java.util.UUID;
//
//import javax.annotation.Resource;
//
//import org.apache.log4j.Logger;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.huboyi.system.po.FundsFlowPO;
//import com.huboyi.system.test.db.TestFundsFlowRepository;
///**
// * 对{@link FundsFlowRepositoryWithRedis}的测试。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 2015/03/25
// * @version 1.0
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"/config/system/test/db/redis/redis-spring.xml", "/config/system/test/db/redis/repository-spring.xml"})
//public class TestTestFundsFlowRepository {
//	
//	/** 日志。*/
//	private final Logger log = Logger.getLogger(TestTestFundsFlowRepository.class);
//
//	/** 证券代码。*/
//	private static final String STOCK_CODE = "000518";
//	/** 证券名称。*/
//	private static final String STOCK_NAME = "常山股份";
//	
//	private static final Random random = new Random();
//	
//	/** 资金流水DAO。*/
//	@Resource(name = "testFundsFlowRepositoryWithRedis")
//	private TestFundsFlowRepository testFundsFlowRepository;
//	
//	/**
//	 * test {@link TestTestFundsFlowRepository#insert} method.
//	 * 
//	 * @throws Exception
//	 */
//	@Test
//	public void testInsert () throws Exception {
//		for (int i = 0; i < 5000; i++) {
//			FundsFlowPO po = new FundsFlowPO();
//			/* 合同编号。 */
//			po.setContractCode(UUID.randomUUID().toString() + "-----------------------" + i);
//			/* 证券代码。 */
//			po.setStockCode(STOCK_CODE);
//			/* 证券名称。 */
//			po.setStockName(STOCK_NAME);
//			
//			po.setCurrency("");
//			
//			/* 交易日期。 */
//			String tradeDateYear = "" + (2010 + random.nextInt(10));
//			String tradeDateMonth = "" + (random.nextInt(12));
//			tradeDateMonth = tradeDateMonth.length() == 1 ? "0" + tradeDateMonth : tradeDateMonth;
//			String tradeDateDay = "" + (random.nextInt(31));
//			tradeDateDay = tradeDateDay.length() == 1 ? "0" + tradeDateDay : tradeDateDay;
//			po.setTradeDate(Long.valueOf((tradeDateYear + tradeDateMonth + tradeDateDay + "000000000")));
//			/* 成交价格。 */
//			po.setTradePrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			/* 成交数量。 */
//			po.setTradeNumber(100L);
//			/* 成交金额。 */
//			po.setTradeMoney(new BigDecimal(random.nextInt(900000)).setScale(3, RoundingMode.HALF_UP));
//			/* 资金余额。 */
//			po.setFundsBalance(new BigDecimal(random.nextInt(50000)).setScale(3, RoundingMode.HALF_UP));
//			/* 业务名称。 */
//			po.setBusinessName(random.nextInt(1) == 0 ? "买入" : "卖出");
//			/* 手续费。 */
//			po.setCharges(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			/* 印花税。 */
//			po.setStampDuty(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			/* 过户费。 */
//			po.setTransferFee(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			/* 结算费。 */
//			po.setClearingFee(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			
//			testFundsFlowRepository.insert(po);
//		}
//	}
//	
//	/**
//	 * test {@link TestTestFundsFlowRepository#findNewOne} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test
//	public void testFindNewOne () throws Exception {
//		log.info(testFundsFlowRepository.findNewOne(STOCK_CODE));
//	}
//	
//	/**
//	 * test {@link TestTestFundsFlowRepository#findFundsFlowList} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test
//	public void testFindFundsFlowList () throws Exception {
//		for (int i = 0; i < 1000; i++) {			
//			log.info(testFundsFlowRepository.findFundsFlowList(STOCK_CODE, null, null, 4900, 30).size());
//		}
//	}
//	
//	/**
//	 * test {@link TestTestFundsFlowRepository#dropCollection} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test
//	public void testDropCollection () throws Exception {
//		testFundsFlowRepository.dropCollection(STOCK_CODE);
//	}
//}