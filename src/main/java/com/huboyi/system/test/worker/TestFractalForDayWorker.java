
package com.huboyi.system.test.worker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.SnapDealSignal;
import com.huboyi.system.bean.DealSignalBean;
import com.huboyi.system.bean.PositionInfoBean;
import com.huboyi.system.constant.DealSignal;
import com.huboyi.system.constant.FundsFlowBusiness;
import com.huboyi.system.constant.OrderInfoTradeFlag;
import com.huboyi.system.function.BandFunction;
import com.huboyi.system.module.fractal.signal.bean.FractalIndicatorsInfoBean;
import com.huboyi.system.module.fractal.signal.calc.FractalDataCalculator;
import com.huboyi.system.po.EverySumPositionInfoPO;
import com.huboyi.system.po.FundsFlowPO;
import com.huboyi.system.po.OrderInfoPO;
import com.huboyi.system.test.bean.TestResultBean;
import com.huboyi.system.test.rule.TestFractalPositionInfoRule;

/**
 * 测试顶底分型交易系统的工作线程类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/1/12
 * @version 1.0
 */
public class TestFractalForDayWorker implements Callable<TestResultBean> {

	/** 日志。*/
	private static final Logger log = LogManager.getLogger(TestFractalForDayWorker.class);
	
	/** 证券代码。*/
	private final String stockCode;
	
	/** 交易初始资金。*/
	private final BigDecimal initMoney;
	
	/** 原始股票行情数据。*/
	private final List<StockDataBean> stockDataBeanList;
	
	/** 计算顶底分型交易系统中所需数据的计算类。*/
	private final FractalDataCalculator calculator;
	/** 捕捉交易信号接口类。*/
	private final SnapDealSignal snapDealSignal;
	
	/** 顶底分型交易系统仓位控制规则。*/
	private final TestFractalPositionInfoRule positionInfoRule;
	
	/** 当前完成的任务数量。*/
	private final AtomicInteger completeTaskNums;
	
	/**
	 * 构造函数。
	 * 
	 * @param stockCode
	 * @param initMoney 初始资金
	 * @param stockDataBeanList 原始股票行情数据
	 * @param calculator 计算交易系统所需数据的计算类
	 * @param snapDealSignal 交易规则
	 * @param positionInfoRule 仓位控制规则
	 * @param completeTaskNums 当前完成的任务数量
	 */
	public TestFractalForDayWorker (
			String stockCode, BigDecimal initMoney, List<StockDataBean> stockDataBeanList, 
			FractalDataCalculator calculator, SnapDealSignal snapDealSignal, 
			TestFractalPositionInfoRule positionInfoRule, AtomicInteger completeTaskNums) {
		this.stockCode = stockCode;
		this.initMoney = initMoney;
		this.stockDataBeanList = stockDataBeanList;
		this.calculator = calculator;
		this.snapDealSignal = snapDealSignal;
		this.positionInfoRule = positionInfoRule;
		this.completeTaskNums = completeTaskNums;
	}
	
	@Override
	public TestResultBean call() throws Exception {
		Thread current = Thread.currentThread();
		log.info("当前线程[name = " + current.getName() + "]正在对[证券代码：" + stockCode + "]执行顶底分型交易系统测试任务。");
		
		// 开始时间。
		long startTime = System.nanoTime();
		
		// 用于保存测试结果。
		TestResultBean resultBean = null;
		
		// 用于保存最后一个交易信号。
		DealSignalBean lastDealSignal = null;
		
		try {
			
			/*
			 * ##################################################
			 * # 1、在测试之前 1、删除之前的测试结果；2、做索引；3、转入一笔资金。        #
			 * ##################################################
			 */
			positionInfoRule.deleteTestResult(stockCode);
			positionInfoRule.ensureIndexForTestResult(stockCode);
			positionInfoRule.insertBankTransfer(
					FundsFlowBusiness.ROLL_IN, 
					stockCode, 
					19800101000000000L, 
					initMoney);
			
			// 用于顺序的装载测试数据。
			List<StockDataBean> testSdBeanList = new ArrayList<StockDataBean>();

			for (int i = 0; i < stockDataBeanList.size(); i++) {
				// 装在测试用数据。
				StockDataBean testSdBean = stockDataBeanList.get(i);
				testSdBeanList.add(testSdBean);
				
				/*
				 * +-----------------------------------------------------------+
				 * + 一般而言股票的价格不能小于0，但是经过前复权的计算股票价格会小于0（阳泉煤业），这导致    +
				 * + 了我测试总是报错。因此，一旦股票价格小于0，就不再进行买卖和刷新仓位。                                   +
				 * +-----------------------------------------------------------+
				 */
				if (testSdBean.getOpen().doubleValue() <= 0) {
					continue;
				}
				
				/*
				 * ##################################################
				 * # 2、根据交易规则来判断买卖点。                                                                                  #
				 * ##################################################
				 */
				FractalIndicatorsInfoBean fractalIndicatorsInfo = calculator.calc(testSdBeanList);                        // 顶底分型交易系统所需数据的计算结果。
				List<PositionInfoBean> positionInfoList =  positionInfoRule.findAllPositionInfoList(stockCode);           // 查询每一笔仓位记录（按照open_date + open_time 升序）。
				
				DealSignalBean buyToOpenSignal = snapDealSignal.snapBuyToOpenSignal(
						stockCode, testSdBeanList, fractalIndicatorsInfo, positionInfoList);                              // 捕捉买卖信号。
				DealSignalBean sellToCloseSignal = snapDealSignal.snapSellToCloseSignal(
						stockCode, testSdBeanList, fractalIndicatorsInfo, positionInfoList);                              // 捕捉卖出信号。
				
				/*
				 * ##################################################
				 * # 3、过滤掉空信号，把卖出信号排在买入信号的前面。                                              #
				 * ##################################################
				 */
				List<DealSignalBean> dealSignalList = new ArrayList<DealSignalBean>();
				if (sellToCloseSignal != null) { dealSignalList.add(sellToCloseSignal); }
				if (buyToOpenSignal != null) { dealSignalList.add(buyToOpenSignal); }

				/*
				 * ##################################################
				 * # 4、插入资金流水、仓位信息、每一笔仓位等信息。                                                  #
				 * ##################################################
				 */
				for (DealSignalBean dealSignal : dealSignalList) {

					StockDataBean dealPoint =  (dealSignal != null) ? dealSignal.getStockDataBean() : null;               // 计算信号发出点。
					StockDataBean point = (dealPoint != null) ? dealPoint.getNext() : null;                               // 计算实际的买卖点。

					// 如果交易信号中的K线没有下一根K线，且该K线是行情集合中的最后一个K线，就说明这个信号是最后一个交易信号。
					if (dealPoint != null && point == null) {
						if (stockDataBeanList.get(stockDataBeanList.size() - 1).getDate().equals(dealPoint.getDate())) {							
							lastDealSignal = dealSignal;
						}
					}

					if (point != null) {
						if (
								dealSignal.getType() == DealSignal.ONE_B ||
								dealSignal.getType() == DealSignal.FIBO_B) {

							/*
							 * +-----------------------------------------------------------+
							 * + 注意：1、建仓价为：产生交易信号隔天的开盘价；                                                                                   +
							 *       2、止损价为：买入价是最近底分型的最低价                                                                                   +
							 * +-----------------------------------------------------------+
							 */
							
							BigDecimal stopPrice = BandFunction
							.getLastBand(fractalIndicatorsInfo.getBandBeanList())
							.getBottom().getCenter().getLow();
							
							positionInfoRule.insertBuyInfo(
									dealSignal.getType(), 
									stockCode, 
									dealPoint.getDate(),
									point.getDate(),
									point.getOpen(), 
									
									/*
									 * +-----------------------------------------------------------+
									 * + 注意：止损范围可看做是赌博的筹码。                                                                                                          +
									 * +-----------------------------------------------------------+
									 */
									stopPrice);
						} else {
							/*
							 * +-----------------------------------------------------------+
							 * + 注意：1、平仓价为：产生交易信号隔天的开盘价；                                                                                   +
							 * +-----------------------------------------------------------+
							 */
							positionInfoRule.insertSellInfo(
									dealSignal.getType(), 
									stockCode, 
									null,
									dealPoint.getDate(),
									point.getDate(),
									point.getOpen());
						}
					}
				}
				
				/*
				 * ##################################################
				 * # 5、每天收盘时刷新每笔仓位信息。                                                                              #
				 * ##################################################
				 */
				positionInfoRule.flushEverySumPositionInfoInClosing(stockCode, testSdBean.getDate(), testSdBean.getClose());
			}
			
			
			/*
			 * ##################################################
			 * # 6、构造一个测试结果对象。                                                                                          #
			 * ##################################################
			 */
			resultBean = constructNewTestResultBean();
			if (resultBean != null && lastDealSignal != null) {				
				resultBean.setLastDealSignal(lastDealSignal);
			}
			
			// 把当前完成交易系统测试的股票数量加一。
			completeTaskNums.addAndGet(1);
			log.info("当前线程[name = " + current.getName() + "]完成对[证券代码：" + stockCode + "]的顶底分型交易系统的测试");
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("当前线程[name = " + current.getName() + "]在对[证券代码：" + stockCode + "]执行顶底分型交易系统测试的过程中出现错误！", e);
		}
		
		// 结束时间。
		long endTime = System.nanoTime();
		System.out.println("此次测试共花费：" + (endTime - startTime) / 1000000000 + "秒");
		
		return resultBean;
	}
	
	public static void main(String[] args) throws ParseException {
		System.out.println(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(870658200000L)));
		System.out.println(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(870658500000L)));
	}
	
	/**
	 * 构造一个测试结果对象。
	 */
	private TestResultBean constructNewTestResultBean () {
		
		List<FundsFlowPO> fundsFlowList = positionInfoRule.findAllFundsFlow(stockCode);                                     // 查询某证券全部的资金流水信息（按照trade_date升序）。
		List<OrderInfoPO> orderInfoList = positionInfoRule.findAllOrderInfo(stockCode);                                     // 查询某证券全部的订单信息（按照trade_date升序）。
		List<EverySumPositionInfoPO> everySumPositionInfoList = positionInfoRule.findAllEverySumPositionInfo(stockCode);    // 查询某证券每一笔持仓信息（按照open_date升序）。

		if (
				(fundsFlowList == null || fundsFlowList.isEmpty()) || 
				(orderInfoList == null || orderInfoList.isEmpty()) || 
				(everySumPositionInfoList == null || everySumPositionInfoList.isEmpty())) {
			return null;
		}
		
		FundsFlowPO lastFundsFlow = fundsFlowList.get(fundsFlowList.size() - 1);                                            // 得到最后一条资金流水记录。
		EverySumPositionInfoPO lastPositionInfo = everySumPositionInfoList.get(everySumPositionInfoList.size() - 1);        // 得到最后一笔持仓信息。
		
		/*
		 * ##################################################
		 * # 1、计算各种交易测试结果。                                                                                          #
		 * ##################################################
		 */
		
		/*
		 * +-----------------------------------------------------------+
		 * + 1、计算交易盈亏和资产分布中的各项数据。                                                                                               +
		 * + 注意：胜率放到了计算交易周期各项数据的过程中。                                                                                  +
		 * +-----------------------------------------------------------+
		 */
		/* 资金余额（最后一笔资金流水的资金余额）。*/
		BigDecimal fundsBalance = lastFundsFlow.getFundsBalance();
		/* 股票市值（所有未平仓记录中股票市值的总和）。*/
		BigDecimal marketValue = new BigDecimal(0);
		Integer noClosePositionNumber = 0;                                                                                  // 所有未平仓记录的数量。
		BigDecimal totalCostPrice = new BigDecimal(0);                                                                      // 所有未平仓记录中建仓价格的总和。
		/* 证券数量（所有未平仓记录中建仓数量的总和）。*/
		Long stockNumber = 0L;
		
		for (EverySumPositionInfoPO po : everySumPositionInfoList) {
			if (po.getCloseContractCode().equalsIgnoreCase("no")) {
				marketValue = marketValue.add(po.getNewMarketValue());
				noClosePositionNumber++;
				totalCostPrice = totalCostPrice.add(po.getOpenPrice());
				stockNumber += po.getOpenNumber();
			}
		}
		
		/* 成本价格（所有未平仓记录中建仓价格的总和 / 所有未平仓记录的数量）。*/
		BigDecimal costPrice = (totalCostPrice.doubleValue() > 0 && noClosePositionNumber > 0) 
		? totalCostPrice.divide(new BigDecimal(noClosePositionNumber), 2, RoundingMode.HALF_UP) : new BigDecimal(0);
		/* 当前价格（最后一笔持仓信息中股票的最新价格）。*/
		BigDecimal newPrice = lastPositionInfo.getNewPrice();
		/* 总资产（剩余资金 + 股票市值）。*/
		BigDecimal totalAsset = fundsBalance.add(marketValue);
		/* 浮动盈亏（总资产 - 初始资金）。*/
		BigDecimal floatProfitAndLoss = totalAsset.subtract(initMoney);
		/* 盈亏比例（浮动盈亏 / 初始资金）。*/
		BigDecimal profitAndLossRatio = (floatProfitAndLoss.doubleValue() != 0 && initMoney.doubleValue() > 0) 
		? floatProfitAndLoss.divide(initMoney, 2, RoundingMode.HALF_UP) : new BigDecimal(0);
                                        
		/*
		 * +-----------------------------------------------------------+
		 * + 2、计算交易频率中的各项数据。                                                                                                                   +
		 * +-----------------------------------------------------------+
		 */
		/* 买卖详情（订单集合中具体的买卖事项）。*/
		List<String> dealDetailList = new ArrayList<String>();
		/* 交易次数（订单集合中所有订单数量）。*/
		Integer dealNumber = 0;
		/* 买入次数（订单集合中所有买入订单数量）。*/
		Integer buyNumber = 0;
		/* 卖出次数（订单集合中所有卖出订单数量）。*/
		Integer sellNumber = 0;
		List<Long> buyAndSellIntervalList = new ArrayList<Long>();                                                         // 记录每次交易间隔时间的集合。
		
		for (int i = 0; i < orderInfoList.size(); i++) {
			dealNumber++;
			OrderInfoPO current = orderInfoList.get(i);
			if (current.getTradeFlag().equals(OrderInfoTradeFlag.STOCK_BUY.getType())) {
				dealDetailList.add("买");
				buyNumber++;
			} else {
				dealDetailList.add("卖");
				sellNumber++;
			}
			if (i < orderInfoList.size() - 2) {
				OrderInfoPO next = orderInfoList.get(i + 1);
				buyAndSellIntervalList.add(
						(new Date(next.getTradeDate()).getTime() - new Date(current.getTradeDate()).getTime()) 
						/ 
						(24 * 60 * 60 * 1000));
			}
		}
		Collections.sort(buyAndSellIntervalList);                                                                           // 对每次交易间隔时间的集合进行升序排序。
		
		/* 平均间隔（各订单间成交时间的平均间隔）。*/
		BigDecimal avgBuyAndSellInterval = new BigDecimal(0);
		for (Long interval : buyAndSellIntervalList) {
			avgBuyAndSellInterval = avgBuyAndSellInterval.add(new BigDecimal(interval));
		}
		avgBuyAndSellInterval = (avgBuyAndSellInterval.doubleValue() > 0 && buyAndSellIntervalList.size() > 0) 
		? avgBuyAndSellInterval.divide(new BigDecimal(buyAndSellIntervalList.size()), 2, RoundingMode.HALF_UP) : new BigDecimal(0);
		/* 最大间隔。*/
		BigDecimal maxBuyAndSellInterval = (!buyAndSellIntervalList.isEmpty()) 
		? new BigDecimal(buyAndSellIntervalList.get(buyAndSellIntervalList.size() - 1)) : new BigDecimal(0);
		/* 最小间隔。*/
		BigDecimal minBuyAndSellInterval = (!buyAndSellIntervalList.isEmpty()) 
		? new BigDecimal(buyAndSellIntervalList.get(0)) : new BigDecimal(0);
		/*
		 * +-----------------------------------------------------------+
		 * + 3、计算交易周期中的各项数据。                                                                                                                   +
		 * +-----------------------------------------------------------+
		 */
		
		/* 盈亏详情（每笔持仓记录中的浮动盈亏）。*/
		List<BigDecimal> cyclePLDetailList = new ArrayList<BigDecimal>();
		/* 交易周期（已平仓的每笔持仓记录的数量）。*/
		Integer cycleNumber = 0;
		/* 盈利周期（已平仓的每笔持仓记录中浮动盈亏大于0的数量）。*/
		Integer winNumber = 0;
		/* 亏损周期（已平仓的每笔持仓记录中浮动盈亏小于0的数量）。*/
		Integer lossNumber = 0;
		List<Long> cycleIntervalList = new ArrayList<Long>();                                                              // 记录每笔已平仓记录中建仓时间与平仓时间的间隔的集合。
		List<BigDecimal> floatProfitList = new ArrayList<BigDecimal>();                                                    // 记录浮动盈利的集合。
		List<BigDecimal> floatLossList = new ArrayList<BigDecimal>();                                                      // 记录浮动亏损的集合。
		for (EverySumPositionInfoPO po : everySumPositionInfoList) {
			if (!po.getCloseContractCode().equalsIgnoreCase("no")) {
				cyclePLDetailList.add(po.getFloatProfitAndLoss());
				cycleNumber++;
				
				if (po.getFloatProfitAndLoss().doubleValue() > 0) {
					winNumber++;
					floatProfitList.add(po.getFloatProfitAndLoss());
				} else {
					lossNumber++;
					floatLossList.add(po.getFloatProfitAndLoss());
				}
				cycleIntervalList.add(
						(new Date(po.getCloseDate()).getTime() - new Date(po.getOpenDate()).getTime()) 
						/ 
						(24 * 60 * 60 * 1000));
			}
		}
		Collections.sort(cycleIntervalList);                                                                                // 对每笔已平仓记录中建仓时间与平仓时间的间隔的集合进行升序排序。
		Collections.sort(floatProfitList);                                                                                  // 对浮动盈利的集合进行升序排序。
		Collections.sort(floatLossList);                                                                                    // 对浮动亏损的集合进行升序排序（注意：排序之后数据是这样的“-50.540, -13.120, -5.540, -3.120, -3.120”）。
		
		/* 平均间隔（每笔已平仓记录中建仓时间与平仓时间的平均间隔）。*/
		BigDecimal avgCycleInterval = new BigDecimal(0);
		for (Long interval : cycleIntervalList) {
			avgCycleInterval = avgCycleInterval.add(new BigDecimal(interval));
		}
		avgCycleInterval = (avgCycleInterval.doubleValue() > 0 && cycleIntervalList.size() > 0)
		? avgCycleInterval.divide(new BigDecimal(cycleIntervalList.size()), 2, RoundingMode.HALF_UP) : new BigDecimal(0);
		/* 最大间隔。*/
		BigDecimal maxCycleInterval = (!cycleIntervalList.isEmpty()) 
		? new BigDecimal(cycleIntervalList.get(cycleIntervalList.size() - 1)) : new BigDecimal(0);
		/* 最小间隔。*/
		BigDecimal minCycleInterval = (!cycleIntervalList.isEmpty()) 
		? new BigDecimal(cycleIntervalList.get(0)) : new BigDecimal(0);
		
		/* 胜率（盈利周期数 / 总周期数）。*/
		BigDecimal winRate = (winNumber > 0 && cycleNumber > 0)
		? new BigDecimal(winNumber).divide(new BigDecimal(cycleNumber), 2, RoundingMode.HALF_UP) : new BigDecimal(0);
		/*
		 * +-----------------------------------------------------------+
		 * + 4、计算周期盈亏中的各项数据。                                                                                                                   +
		 * +-----------------------------------------------------------+
		 */
		/* 平均赢利。*/
		BigDecimal avgProfit = new BigDecimal(0);
		for (BigDecimal avg : floatProfitList) {
			avgProfit = avgProfit.add(avg);
		}
		avgProfit = (avgProfit.doubleValue() > 0 && floatProfitList.size() > 0) 
		? avgProfit.divide(new BigDecimal(floatProfitList.size()), 2, RoundingMode.HALF_UP) : new BigDecimal(0);
		/* 平均亏损。*/
		BigDecimal avgLoss = new BigDecimal(0);
		for (BigDecimal avg : floatLossList) {
			avgLoss = avgLoss.add(avg);
		}
		avgLoss = (avgLoss.doubleValue() != 0 && floatLossList.size() > 0) 
		? avgLoss.divide(new BigDecimal(floatLossList.size()), 2, RoundingMode.HALF_UP) : new BigDecimal(0);
		/* 最大盈利。*/
		BigDecimal maxProfit = (!floatProfitList.isEmpty()) ? floatProfitList.get(floatProfitList.size() - 1) : new BigDecimal(0);
		/* 最大亏损。*/
		BigDecimal maxLoss = (!floatLossList.isEmpty()) ? floatLossList.get(0) : new BigDecimal(0);
		/* 最小盈利。*/
		BigDecimal minProfit = (!floatProfitList.isEmpty()) ? floatProfitList.get(0) : new BigDecimal(0);
		/* 最小亏损。*/
		BigDecimal minLoss = (!floatLossList.isEmpty()) ? floatLossList.get(floatLossList.size() - 1) : new BigDecimal(0);
		
		/*
		 * ##################################################
		 * # 2、构造测试结果对象。                                                                                                  #
		 * ##################################################
		 */
		TestResultBean result = new TestResultBean();
		
		// --- 交易盈亏 ---
		/* 证券代码。*/
		result.setStockCode(stockCode);
		/* 总资产（剩余资金 + 股票市值）。*/
		result.setTotalAsset(totalAsset.setScale(2, RoundingMode.HALF_UP));
		/* 浮动盈亏。*/
		result.setFloatProfitAndLoss(floatProfitAndLoss.setScale(2, RoundingMode.HALF_UP));
		/* 盈亏比例。*/
		result.setProfitAndLossRatio(profitAndLossRatio.setScale(2, RoundingMode.HALF_UP));
		/* 胜率。*/
		result.setWinRate(winRate.setScale(2, RoundingMode.HALF_UP));
		
		// --- 资产分布 ---
		/* 初始资金。*/
		result.setInitMoney(initMoney.setScale(2, RoundingMode.HALF_UP));
		/* 资金余额。*/
		result.setFundsBalance(fundsBalance.setScale(2, RoundingMode.HALF_UP));
		/* 股票市值。*/
		result.setMarketValue(marketValue.setScale(2, RoundingMode.HALF_UP));
		/* 证券数量。*/
		result.setStockNumber(stockNumber);
		/* 成本价格。*/
		result.setCostPrice(costPrice.setScale(2, RoundingMode.HALF_UP));
		/* 当前价格。*/
		result.setNewPrice(newPrice.setScale(2, RoundingMode.HALF_UP));
		
		// --- 交易频率 ---
		/* 买卖详情。*/
		result.setDealDetailList(dealDetailList);
		/* 交易次数。*/
		result.setDealNumber(dealNumber);
		/* 买入次数。*/
		result.setBuyNumber(buyNumber);
		/* 卖出次数。*/
		result.setSellNumber(sellNumber);
		/* 平均间隔。*/
		result.setAvgBuyAndSellInterval(avgBuyAndSellInterval.setScale(2, RoundingMode.HALF_UP));
		/* 最大间隔。*/
		result.setMaxBuyAndSellInterval(maxBuyAndSellInterval.setScale(2, RoundingMode.HALF_UP));
		/* 最小间隔。*/
		result.setMinBuyAndSellInterval(minBuyAndSellInterval.setScale(2, RoundingMode.HALF_UP));
		
		// --- 交易周期 ---
		/* 盈亏详情。*/
		result.setCyclePLDetailList(cyclePLDetailList);
		/* 交易周期。*/
		result.setCycleNumber(cycleNumber);
		/* 盈利周期。*/
		result.setWinNumber(winNumber);
		/* 亏损周期。*/
		result.setLossNumber(lossNumber);
		/* 平均间隔。*/
		result.setAvgCycleInterval(avgCycleInterval.setScale(2, RoundingMode.HALF_UP));
		/* 最大间隔。*/
		result.setMaxCycleInterval(maxCycleInterval.setScale(2, RoundingMode.HALF_UP));
		/* 最小间隔。*/
		result.setMinCycleInterval(minCycleInterval.setScale(2, RoundingMode.HALF_UP));
		
		// --- 周期盈亏 ---
		/* 平均赢利。*/
		result.setAvgProfit(avgProfit.setScale(2, RoundingMode.HALF_UP));
		/* 平均亏损。*/
		result.setAvgLoss(avgLoss.setScale(2, RoundingMode.HALF_UP));
		/* 最大盈利。*/
		result.setMaxProfit(maxProfit.setScale(2, RoundingMode.HALF_UP));
		/* 最大亏损。*/
		result.setMaxLoss(maxLoss.setScale(2, RoundingMode.HALF_UP));
		/* 最小盈利。*/
		result.setMinProfit(minProfit.setScale(2, RoundingMode.HALF_UP));
		/* 最小亏损。*/
		result.setMinLoss(minLoss.setScale(2, RoundingMode.HALF_UP));
		
		return result;
	}
}