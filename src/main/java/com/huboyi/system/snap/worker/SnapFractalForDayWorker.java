package com.huboyi.system.snap.worker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.data.load.bean.StockDataBean;
import com.huboyi.system.SnapDealSignal;
import com.huboyi.system.bean.DealSignalBean;
import com.huboyi.system.bean.PositionInfoBean;
import com.huboyi.system.constant.DealSignal;
import com.huboyi.system.constant.OrderInfoTradeFlag;
import com.huboyi.system.module.fractal.signal.bean.FractalIndicatorsInfoBean;
import com.huboyi.system.module.fractal.signal.calc.FractalDataCalculator;
import com.huboyi.system.snap.bean.SnapResultBean;
import com.huboyi.system.snap.service.SpanEverySumPositionInfoService;

/**
 * 捕捉顶底分型交易系统信号的工作线程类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/4/17
 * @version 1.0
 */
public class SnapFractalForDayWorker implements Callable<SnapResultBean[]> {

	/** 日志。*/
	private static final Logger log = LogManager.getLogger(SnapFractalForDayWorker.class);

	/** 证券代码。*/
	private final String stockCode;
	
	/** 原始股票行情数据。*/
	private final List<StockDataBean> stockDataBeanList;
	
	/** 计算顶底分型交易系统中所需数据的计算类。*/
	private final FractalDataCalculator calculator;
	
	/** 捕捉交易信号接口类。*/
	private final SnapDealSignal snapDealSignal;
	
	/** 每一笔持仓信息Service。*/
	private final SpanEverySumPositionInfoService spanEverySumPositionInfoService;
	
	/** 当前完成的任务数量。*/
	private final AtomicInteger completeTaskNums;
	
	/**
	 * 构造函数。
	 * 
	 * @param stockCode
	 * @param stockDataBeanList 原始股票行情数据
	 * @param calculator 计算交易系统所需数据的计算类
	 * @param snapDealSignal 交易规则
	 * @param spanEverySumPositionInfoService 每一笔持仓信息Service
	 * @param completeTaskNums 当前完成的任务数量
	 */
	public SnapFractalForDayWorker (
			String stockCode, List<StockDataBean> stockDataBeanList, 
			FractalDataCalculator calculator, SnapDealSignal snapDealSignal, 
			SpanEverySumPositionInfoService spanEverySumPositionInfoService, AtomicInteger completeTaskNums) {
		this.stockCode = stockCode;
		this.stockDataBeanList = stockDataBeanList;
		this.calculator = calculator;
		this.snapDealSignal = snapDealSignal;
		this.spanEverySumPositionInfoService = spanEverySumPositionInfoService;
		this.completeTaskNums = completeTaskNums;
	}
	
	@Override
	public SnapResultBean[] call() throws Exception {
		Thread current = Thread.currentThread();
		log.info("当前线程[name = " + current.getName() + "]正在对[证券代码：" + stockCode + "]执行顶底分型交易系统捕捉信号任务。");
		
		// 开始时间。
		long startTime = System.nanoTime();
		
		try {
			
			// 查询出该证券的每一笔持仓信息。
			List<PositionInfoBean> positionInfoList = spanEverySumPositionInfoService.findAllPositionInfoByStockCode(stockCode);
			
			// 用于顺序的装载测试数据。
			List<StockDataBean> testSdBeanList = new ArrayList<StockDataBean>();
			
			// 为了减少计算量，对行情数据进行截取。
			int begin = 0;
			if (stockDataBeanList.size() > 720) {
				begin = stockDataBeanList.size() - 720;
			}
			
			for (int i = begin; i < stockDataBeanList.size(); i++) {
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
				 * # 1、根据交易规则来判断买卖点。                                                                                  #
				 * ##################################################
				 */
				FractalIndicatorsInfoBean fractalIndicatorsInfo = calculator.calc(testSdBeanList);                        // 顶底分型交易系统所需数据的计算结果。
				DealSignalBean buyToOpenSignal = snapDealSignal.snapBuyToOpenSignal(
						stockCode, testSdBeanList, fractalIndicatorsInfo, positionInfoList);                              // 捕捉买卖信号。
				DealSignalBean sellToCloseSignal = snapDealSignal.snapSellToCloseSignal(
						stockCode, testSdBeanList, fractalIndicatorsInfo, positionInfoList);                              // 捕捉卖出信号。
				
				/*
				 * ##################################################
				 * # 2、过滤掉空信号，把卖出信号排在买入信号的前面。                                              #
				 * ##################################################
				 */
				List<DealSignalBean> dealSignalList = new ArrayList<DealSignalBean>();
				if (sellToCloseSignal != null) { dealSignalList.add(sellToCloseSignal); }
				if (buyToOpenSignal != null) { dealSignalList.add(buyToOpenSignal); }
				
				/*
				 * ##################################################
				 * # 3、记录捕捉到的最后一个交易信号。                                                                          #
				 * ##################################################
				 */
				if (i == stockDataBeanList.size() - 1) {
					List<SnapResultBean> resultList = new ArrayList<SnapResultBean>();                                    // 用于记录捕捉到的信息。
					
					for (DealSignalBean dealSignal : dealSignalList) {
						SnapResultBean result = constructSnapResultBean(stockCode, dealSignal);
						
						if (result != null) {
							resultList.add(result);
						}
					}
					
					/*
					 * ##################################################
					 * # 4、根据最后一根K线更新仓位信息。                                                                           #
					 * ##################################################
					 */
					spanEverySumPositionInfoService.flushEverySumPositionInfoInClosing(stockCode, testSdBean.getClose());
					
					return resultList.toArray(new SnapResultBean[0]);
				}
			}
			
			// 把当前完成交易系统测试的股票数量加一。
			completeTaskNums.addAndGet(1);
			log.info("当前线程[name = " + current.getName() + "]完成对[证券代码：" + stockCode + "]的顶底分型交易系统的信号捕捉");
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("当前线程[name = " + current.getName() + "]在对[证券代码：" + stockCode + "]执行顶底分型交易系统信号捕捉的过程中出现错误！", e);
		}
		
		// 结束时间。
		long endTime = System.nanoTime();
		System.out.println("此次捕捉交易信号共花费：" + (endTime - startTime) / 1000000000 + "秒");
		
		return null;
	}
	
	/**
	 * 构造一个捕捉结果对象。
	 * 
	 * @param stockCode 证券代码
	 * @param dealSignal 捕捉到的交易信号
	 * @return SnapResultBean
	 */
	private SnapResultBean constructSnapResultBean (String stockCode, DealSignalBean dealSignal) {
		
		StockDataBean dealPoint =  (dealSignal != null) ? dealSignal.getStockDataBean() : null;                               // 计算信号发出点。
		
		if (dealPoint == null) {
			return null;
		}
		
		SnapResultBean result = new SnapResultBean();
		
		/* 订单信息中的买卖标志枚举类。*/
		if (dealSignal.getType() == DealSignal.ONE_B || dealSignal.getType() == DealSignal.FIBO_B) {
			result.setOrderInfoTradeFlag(OrderInfoTradeFlag.STOCK_BUY);
		} else {
			result.setOrderInfoTradeFlag(OrderInfoTradeFlag.STOCK_SELL);
		}
		
		/* 证券代码。*/
		result.setStockCode(stockCode);
		/* 信号日期。*/
		result.setSignalDate(dealPoint.getDate());
		/* 信号类型。*/
		result.setSignalType(dealSignal.getType().getType());
		/* 信号名称。*/
		result.setSignalName(dealSignal.getType().getName());
		
		/* 交易数量。*/
		if (dealSignal.getType() == DealSignal.ONE_B || dealSignal.getType() == DealSignal.FIBO_B) {
			
			BigDecimal totalFundsBalance = new BigDecimal(100000);                                                            // 假设总资金。
			BigDecimal buyMoneyRate = new BigDecimal(0.02);                                                                   // 2%原则。
			BigDecimal budgetMoney = totalFundsBalance.multiply(buyMoneyRate);                                                // 计算预算资金。
			Long tradeNumber = budgetMoney.divide(dealPoint.getClose(), 0, RoundingMode.HALF_UP).longValue();                 // 计算成交数量。
			
			tradeNumber -= (tradeNumber % 100);                                                                               // 当购买数量不够1手时，就按1手购买。
	    	if (tradeNumber == null || tradeNumber < 100 || (tradeNumber % 100) != 0) {
	    		tradeNumber = 100L;
	    	}
	    	
	    	result.setTradeNumber(tradeNumber);
	    	
		} else {
			result.setTradeNumber(999999999999L);
		}
		
		return result;
	}
}