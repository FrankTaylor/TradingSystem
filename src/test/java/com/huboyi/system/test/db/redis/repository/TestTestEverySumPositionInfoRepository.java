//package com.huboyi.system.test.db.redis.repository;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
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
//import com.huboyi.system.constant.DealSignal;
//import com.huboyi.system.po.EverySumPositionInfoPO;
//import com.huboyi.system.test.db.TestEverySumPositionInfoRepository;
///**
// * 对{@link EverySumPositionInfoRepositoryWithRedis}的测试。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 2015/03/25
// * @version 1.0
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"/config/system/test/db/redis/redis-spring.xml", "/config/system/test/db/redis/repository-spring.xml"})
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
//	@Resource(name = "testEverySumPositionInfoRepositoryWithRedis")
//	private TestEverySumPositionInfoRepository testEverySumPositionInfoRepository;
//	
////	@Before
//	public void dropEverySumPositionInfo () {
//		System.out.println("删除集合中的测试数据");
//		testEverySumPositionInfoRepository.dropCollection(STOCK_CODE);
//	}
//	
//	/**
//	 * test {@link TestTestEverySumPositionInfoRepository#insert} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test
//	public void testInsert () throws Exception {
//		// 开始时间。
//		long startTime = System.nanoTime();
//		
//		for (int i = 0; i < 50; i++) {
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
//			po.setSystemOpenPoint(DealSignal.FIBO_B.getType());			
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
//				po.setSystemClosePoint(DealSignal.SELL_ALL.getType());
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
//	@Test 
//	public void a () {
//		List<EverySumPositionInfoPO> allPositionList = getAllClosePositionList ();                   // 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
//		for (EverySumPositionInfoPO po : allPositionList) {
//			
//			System.out.println("po.getOpenContractCode() = " + po.getOpenContractCode() + ", po.getCloseContractCode() = " + po.getCloseContractCode() + ", po.getOpenDate() = " + po.getOpenDate() + ", po.getCloseDate() = " + po.getCloseDate());
//		}
//	}
//	
//	/**
//	 * 查询出某一买点，尚未平仓的全部仓位信息（按照open_date + open_time 倒序）。
//	 * 
//	 * @param positionInfoList 分型战法仓位集合
//	 * @param dealSignalType 分型战法交易信号类型枚举
//	 * @return List<FractalPositionInfoBean>
//	 */
//	@SuppressWarnings("unused")
//	private List<EverySumPositionInfoPO> getAllNoClosePositionList () {
//		
//		// --- 查询到所有的仓位信息。
//		List<EverySumPositionInfoPO> allPositionList = getAllPositionList();                   // 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
//		if (allPositionList == null || allPositionList.isEmpty()) {
//			return new ArrayList<EverySumPositionInfoPO>();
//		}
//	
//		// --- 找出未平仓的仓位信息。
//		List<EverySumPositionInfoPO> allNoClosePositionList = new ArrayList<EverySumPositionInfoPO>();                        // 装载某一买点，尚未平仓的全部仓位信息。
//		for (EverySumPositionInfoPO position : allPositionList) {
//			if (
//					position.getCloseContractCode() == null ||
//					position.getCloseContractCode().trim().equals("") ||
//					position.getCloseContractCode().equalsIgnoreCase("no")) {
//				allNoClosePositionList.add(position);
//			}
//		}
//		
//		// --- 按照open_date + open_time 降序。
//		Collections.sort(allNoClosePositionList, new Comparator<EverySumPositionInfoPO>() {
//			@Override
//			public int compare(EverySumPositionInfoPO o1, EverySumPositionInfoPO o2) {
//				return (o1.getOpenDate() > o2.getOpenDate()) ? -1  :
//					   (o1.getOpenDate() < o2.getOpenDate()) ? 1   :
//			           0;
//			}
//		});
//		
//		return allNoClosePositionList;
//	}
//	
//	/**
//	 * 查询出某一买点，已平仓的全部仓位信息（按照close_date + close_time 倒序）。
//	 * 
//	 * @param positionInfoList 分型战法仓位集合
//	 * @param dealSignalType 分型战法交易信号类型枚举
//	 * @return List<FractalPositionInfoBean>
//	 */
//	private List<EverySumPositionInfoPO> getAllClosePositionList () {
//		
//		// --- 查询到所有的仓位信息。
//		List<EverySumPositionInfoPO> allPositionList = getAllPositionList();                   // 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
//		if (allPositionList == null || allPositionList.isEmpty()) {
//			return new ArrayList<EverySumPositionInfoPO>();
//		}
//		
//		// --- 找出已平仓的仓位信息。
//		List<EverySumPositionInfoPO> allClosePositionList = new ArrayList<EverySumPositionInfoPO>();                          // 装载某一买点，已平仓的全部仓位信息。
//		for (EverySumPositionInfoPO position : allPositionList) {
//			if (
//					position.getCloseContractCode() != null &&
//					!position.getCloseContractCode().trim().equals("") &&
//					!position.getCloseContractCode().equalsIgnoreCase("no")) {
//				allClosePositionList.add(position);
//			}
//		}
//		
//		// --- 按照close_date + close_time 降序。
//		Collections.sort(allClosePositionList, new Comparator<EverySumPositionInfoPO>() {
//			@Override
//			public int compare(EverySumPositionInfoPO o1, EverySumPositionInfoPO o2) {
//				return (o1.getCloseDate() > o2.getCloseDate()) ? -1  :
//					   (o1.getCloseDate() < o2.getCloseDate()) ? 1   :
//			           0;
//			}
//		});
//		return allClosePositionList;
//	}
//	
//	
//	private List<EverySumPositionInfoPO> getAllPositionList () {
//		// 把符合买点的仓位信息装载到集合中。
//		List<EverySumPositionInfoPO> poList = 
//			testEverySumPositionInfoRepository.findEverySumPositionInfoList(STOCK_CODE, null, null, null, null, null, null);
//		
//		List<EverySumPositionInfoPO> tempPositionInfoList = new ArrayList<EverySumPositionInfoPO>();
//		if (null != poList && !poList.isEmpty()) {
//			for (EverySumPositionInfoPO positionInfo : poList) {
//				if (positionInfo.getSystemOpenPoint().equalsIgnoreCase(DealSignal.FIBO_B.getType())) {
//					tempPositionInfoList.add(positionInfo);
//				}
//			}
//		}
//		
//		return tempPositionInfoList;
//	}
//	
//	/**
//	 * test {@link TestTestEverySumPositionInfoRepository#findEverySumPositionInfoList} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test
//	public void testFindEverySumPositionInfoList () throws Exception {
//		log.info(testEverySumPositionInfoRepository.findEverySumPositionInfoList(STOCK_CODE, null, 20180101, 20191230, "1", null, null));
//	}
//	
//	/**
//	 * test {@link TestTestEverySumPositionInfoRepository#update} method.
//	 * 
//	 * @throws Exception
//	 */
////	@Test
//	public void testUpdate () throws Exception {
//		EverySumPositionInfoPO po = testEverySumPositionInfoRepository.findEverySumPositionInfoList(STOCK_CODE, null, null, null, "0", null, null).get(25);
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
//		// 开始时间。
//		long startTime = System.nanoTime();
//		
//		testEverySumPositionInfoRepository.update(po);
//		
//		// 结束时间。
//		long endTime = System.nanoTime();
//		
//		System.out.println("此次测试共花费：" + (endTime - startTime) / 1000000000 + "秒");
//		
//		log.info("修改后查询的PO");
//		log.info(testEverySumPositionInfoRepository.findEverySumPositionInfoList(STOCK_CODE, po.getOpenContractCode(), null, null, "1", null, null).get(0));
//		
//	}
//
//}