//package com.huboyi.system.test.db.mongodb.repository;
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
//import com.huboyi.system.po.EverySumPositionInfoPO;
//import com.huboyi.system.test.db.TestEverySumPositionInfoRepository;
///**
// * 对{@link EverySumPositionInfoRepository}的测试。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 2015/1/3
// * @version 1.0
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"/config/system/test/db/mongodb/mongodb-spring.xml", "/config/system/test/db/mongodb/repository-spring.xml"})
//public class TestTestEverySumPositionInfoRepository {
//	
//	/** 日志。*/
//	private final Logger log = Logger.getLogger(TestTestEverySumPositionInfoRepository.class);
//
//	/** 证券代码。*/
//	private static final String STOCK_CODE = "SZ000158";
//	/** 证券名称。*/
//	private static final String STOCK_NAME = "常山股份";
//	
//	private static final Random random = new Random();
//	
//	/** 每一笔持仓信息DAO。*/
//	@Resource(name = "testEverySumPositionInfoRepositoryWithMongoDB")
//	private TestEverySumPositionInfoRepository testEverySumPositionInfoRepository;
//	
//	/**
//	 * test {@link EverySumPositionInfoRepository#createIndex} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test 
//	public void testCreateIndex () throws Exception {
//		testEverySumPositionInfoRepository.createIndex(STOCK_CODE);
//	}
//	
//	/**
//	 * test {@link EverySumPositionInfoRepository#insert} method.
//	 * 
//	 * @throws Exception
//	 */
//	@Test 
//	public void testInsert () throws Exception {
//		// 开始时间。
//		long startTime = System.nanoTime();
//		
//		for (int i = 0; i < 500; i++) {
//			EverySumPositionInfoPO po = new EverySumPositionInfoPO();
//			
//			// ---
//			/* 系统名称。*/
//			po.setSystemName("顶底分型交易系统");
//			/* 证券代码。 */
//			po.setStockCode(STOCK_CODE);
//			/* 证券名称。 */
//			po.setStockName(STOCK_NAME);
//			
//			// ---
//			/* 建仓合同编号。 */
//			po.setOpenContractCode(UUID.randomUUID().toString());
//			/* 系统建仓点。*/
//			po.setSystemOpenPoint(random.nextInt(3) == 2 ? "3B" : random.nextInt(2) == 1 ? "2B" : "1B");			
//			/* 成交日期（格式：%Y%m%d）。 */
//			String openDateYear = "" + (2010 + random.nextInt(10));
//			String openDateMonth = "" + (random.nextInt(12));
//			openDateMonth = openDateMonth.length() == 1 ? "0" + openDateMonth : openDateMonth;
//			String openDateDay = "" + (random.nextInt(31));
//			openDateDay = openDateDay.length() == 1 ? "0" + openDateDay : openDateDay;
//			po.setOpenDate(Long.valueOf((openDateYear + openDateMonth + openDateDay + "000000000")));
//			/* 开仓价格。 */
//			po.setOpenPrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			/* 开仓数量。 */
//			po.setOpenNumber(random.nextLong());
//			/* 建仓成本。 */
//			po.setOpenCost(new BigDecimal(random.nextInt(100000)).setScale(3, RoundingMode.HALF_UP));
//			
//			// --- 
//			/* 可平仓数量。*/
//			po.setCanCloseNumber(random.nextLong());
//			/* 止损价格。*/
//			po.setStopPrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			
//			// --- 
//			if (random.nextInt(2) > 0) {
//				/* 平仓合同编号。*/
//				po.setCloseContractCode(UUID.randomUUID().toString());
//				/* 系统平仓点。*/
//				po.setSystemClosePoint(random.nextInt(3) == 2 ? "3S" : random.nextInt(2) == 1 ? "2S" : "1S");
//				/* 平仓日期（格式：%Y%m%d）。 */
//				String closeDateYear = "" + (2010 + random.nextInt(10));
//				String closeDateMonth = "" + (random.nextInt(12));
//				closeDateMonth = closeDateMonth.length() == 1 ? "0" + closeDateMonth : closeDateMonth;
//				String closeDateDay = "" + (random.nextInt(31));
//				closeDateDay = closeDateDay.length() == 1 ? "0" + closeDateDay : closeDateDay;
//				po.setCloseDate(Long.valueOf((closeDateYear + closeDateMonth + closeDateDay + "000000000")));
//				/* 平仓价格。 */
//				po.setClosePrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//				/* 平仓数量。 */
//				po.setCloseNumber(random.nextLong());
//			}
//
//			/* 当前价。 */
//			po.setNewPrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			/* 最新市值。 */
//			po.setNewMarketValue(new BigDecimal(random.nextInt(80000)).setScale(3, RoundingMode.HALF_UP));
//			/* 浮动盈亏。 */
//			po.setFloatProfitAndLoss(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			/* 盈亏比例。 */
//			po.setProfitAndLossRatio(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//			
//			testEverySumPositionInfoRepository.insert(po);
//		}
//		
//		// 结束时间。
//		long endTime = System.nanoTime();
//		
//		System.out.println("此次测试共花费：" + (endTime - startTime) / 1000000000 + "秒");
//	}
//	
//	/**
//	 * test {@link EverySumPositionInfoRepository#findEverySumPositionInfoList} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test 
//	public void testFindEverySumPositionInfoList () throws Exception {
//		log.info(testEverySumPositionInfoRepository.findEverySumPositionInfoList(STOCK_CODE, null, 20190101, 20191230, "0", null, null).size());
//	}
//	
//	/**
//	 * test {@link EverySumPositionInfoRepository#update} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test
//	public void testUpdate () throws Exception {
//		EverySumPositionInfoPO po = testEverySumPositionInfoRepository.findEverySumPositionInfoList(STOCK_CODE, null, null, null, "0", null, null).get(0);
//		
//		log.info("刚开始查询的PO");
//		log.info(po);
//		
//		// --- 
//		/* 可平仓数量。*/
//		po.setCanCloseNumber(random.nextLong());
//		/* 止损价格。*/
//		po.setStopPrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//		
//		// --- 
//		/* 平仓合同编号。*/
//		po.setCloseContractCode(UUID.randomUUID().toString());
//		/* 系统平仓点。*/
//		po.setSystemClosePoint(random.nextInt(3) == 2 ? "3S" : random.nextInt(2) == 1 ? "2S" : "1S");
//		/* 平仓日期（格式：%Y%m%d）。 */
//		String closeDateYear = "" + (2010 + random.nextInt(10));
//		String closeDateMonth = "" + (random.nextInt(12));
//		closeDateMonth = closeDateMonth.length() == 1 ? "0" + closeDateMonth : closeDateMonth;
//		String closeDateDay = "" + (random.nextInt(31));
//		closeDateDay = closeDateDay.length() == 1 ? "0" + closeDateDay : closeDateDay;
//		po.setCloseDate(Long.valueOf((closeDateYear + closeDateMonth + closeDateDay + "000000000")));
//		/* 平仓价格。 */
//		po.setClosePrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//		/* 平仓数量。 */
//		po.setCloseNumber(random.nextLong());
//		
//		// ---
//		/* 当前价。 */
//		po.setNewPrice(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//		/* 最新市值。 */
//		po.setNewMarketValue(new BigDecimal(random.nextInt(80000)).setScale(3, RoundingMode.HALF_UP));
//		/* 浮动盈亏。 */
//		po.setFloatProfitAndLoss(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//		/* 盈亏比例。 */
//		po.setProfitAndLossRatio(new BigDecimal(random.nextInt(100)).setScale(3, RoundingMode.HALF_UP));
//		
//		testEverySumPositionInfoRepository.update(po);
//		
//		log.info("修改后查询的PO");
//		log.info(testEverySumPositionInfoRepository.findEverySumPositionInfoList(STOCK_CODE, po.getOpenContractCode(), null, null, "1", null, null).get(0));
//		
//	}
//	
//	/**
//	 * test {@link EverySumPositionInfoRepository#dropCollection} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test
//	public void testDropCollection () throws Exception {
//		testEverySumPositionInfoRepository.dropCollection(STOCK_CODE);
//	}
//}