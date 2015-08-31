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

import com.huboyi.system.po.FundsFlowPO;
import com.huboyi.system.test.db.TestFundsFlowRepository;
/**
 * 对{@link FundsFlowRepository}的测试。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/1/2
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/config/system/test/db/mongodb/mongodb-spring.xml", "/config/system/test/db/mongodb/repository-spring.xml"})
public class TestTestFundsFlowRepository {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestTestFundsFlowRepository.class);

	/** 证券代码。*/
	private static final String STOCK_CODE = "000518";
	/** 证券名称。*/
	private static final String STOCK_NAME = "常山股份";
	
	private static final Random random = new Random();
	
	/** 资金流水DAO。*/
	@Resource(name = "testFundsFlowRepositoryWithMongoDB")
	private TestFundsFlowRepository testFundsFlowRepository;
	
	/**
	 * test {@link FundsFlowRepository#createIndex} method.
	 * 
	 * @throws Exception
	 */
//	@Test 
	public void testCreateIndex () throws Exception {
		testFundsFlowRepository.createIndex(STOCK_CODE);
	}
	
	/**
	 * test {@link FundsFlowRepository#insert} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testInsert () throws Exception {
		for (int i = 0; i < 50000; i++) {
			FundsFlowPO po = new FundsFlowPO();
			/* 合同编号。 */
			po.setContractCode(UUID.randomUUID().toString());
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
			po.setTradeDate(Integer.valueOf((tradeDateYear + tradeDateMonth + tradeDateDay)));
			/* 交易时间。 */
			String tradeTimeHour = "" + (random.nextInt(24));
			tradeTimeHour = tradeTimeHour.length() == 1 ? "0" + tradeTimeHour : tradeTimeHour;
			String tradeTimeMinute = "" + (random.nextInt(60));
			tradeTimeMinute = tradeTimeMinute.length() == 1 ? "0" + tradeTimeMinute : tradeTimeMinute;
			String tradeTimeSecond = "" + (random.nextInt(60));
			tradeTimeSecond = tradeTimeSecond.length() == 1 ? "0" + tradeTimeSecond : tradeTimeSecond;
			po.setTradeTime(Long.valueOf((tradeTimeHour + tradeTimeMinute + tradeTimeSecond)));
			/* 成交价格。 */
			po.setTradePrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
			/* 成交数量。 */
			po.setTradeNumber(100L);
			/* 成交金额。 */
			po.setTradeMoney(new BigDecimal(random.nextInt(900000)).setScale(3, RoundingMode.HALF_UP));
			/* 资金余额。 */
			po.setFundsBalance(new BigDecimal(random.nextInt(50000)).setScale(3, RoundingMode.HALF_UP));
			/* 业务名称。 */
			po.setBusinessName(random.nextInt(1) == 0 ? "买入" : "卖出");
			/* 手续费。 */
			po.setCharges(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
			/* 印花税。 */
			po.setStampDuty(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
			/* 过户费。 */
			po.setTransferFee(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
			/* 结算费。 */
			po.setClearingFee(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
			
			testFundsFlowRepository.insert(po);
		}
	}
	
	/**
	 * test {@link FundsFlowRepository#findNewOne} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testFindNewOne () throws Exception {
		log.info(testFundsFlowRepository.findNewOne(STOCK_CODE));
	}
	
	/**
	 * test {@link FundsFlowRepository#findFundsFlowList} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testFindFundsFlowList () throws Exception {
		log.info(testFundsFlowRepository.findFundsFlowList(STOCK_CODE, 20190101, 20191230, null, null).size());
	}
	
	/**
	 * test {@link FundsFlowRepository#dropCollection} method.
	 * 
	 * @throws Exception
	 */
//	@Test
	public void testDropCollection () throws Exception {
		testFundsFlowRepository.dropCollection(STOCK_CODE);
	}
}