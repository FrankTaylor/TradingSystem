package com.huboyi.system.test.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;

import com.huboyi.engine.DealFeeCalculator;
import com.huboyi.system.bean.PositionInfoBean;
import com.huboyi.system.constant.DealSignal;
import com.huboyi.system.constant.FundsFlowBusiness;
import com.huboyi.system.constant.OrderInfoTradeFlag;
import com.huboyi.system.po.EverySumPositionInfoPO;
import com.huboyi.system.po.FundsFlowPO;
import com.huboyi.system.po.OrderInfoPO;
import com.huboyi.system.test.db.TestEverySumPositionInfoRepository;
import com.huboyi.system.test.db.TestFundsFlowRepository;
import com.huboyi.system.test.db.TestOrderInfoRepository;

/**
 * 测试顶底分型交易系统的仓位控制规则类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/1/10
 * @version 1.0
 */
public class TestFractalPositionInfoRule {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(TestFractalPositionInfoRule.class);
	
	/*
	// 处理日期和时间的格式类。（YYYY是国际标准ISO 8601所指定的以周来纪日的历法。yyyy是格里高利历，它以400年为一个周期，在这个周期中，一共有97个闰日，在这种历法的设计中，闰日尽可能均匀地分布在各个年份中，所以一年的长度有两种可能：365天或366天。）
	private final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	// 处理日期格式类。
	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	// 处理时间格式类。（HH是24小时制，hh是12小时制
	private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss");
	*/
	
	/** 交易费用计算类。*/
	@Resource
	private DealFeeCalculator dealFeeCalculator;
	
	/** 资金流水DAO。*/
	@Resource(name = "testFundsFlowRepositoryWithRedis")
	private TestFundsFlowRepository testFundsFlowRepository;
	/** 订单信息DAO。*/
	@Resource(name = "testOrderInfoRepositoryWithRedis")
	private TestOrderInfoRepository testOrderInfoRepository;
	/** 每一笔持仓信息DAO。*/
	@Resource(name = "testEverySumPositionInfoRepositoryWithRedis")
	private TestEverySumPositionInfoRepository testEverySumPositionInfoRepository;
	
	/**
	 * 查询每一笔仓位记录（按照open_date + open_time 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<PositionInfoBean>
	 */
	public List<PositionInfoBean> findAllPositionInfoList (String stockCode) {
		List<PositionInfoBean> positionInfoList = new ArrayList<PositionInfoBean>();                      // 装载每一笔仓位信息的集合。
		
		List<EverySumPositionInfoPO> everySumPositionInfoList =                                           // 查询出每一笔未平仓的仓位信息。
			testEverySumPositionInfoRepository
			.findEverySumPositionInfoList(stockCode, null, null, null, null, null, null);
		
		for (EverySumPositionInfoPO source : everySumPositionInfoList) {
			PositionInfoBean target = new PositionInfoBean();
			BeanUtils.copyProperties(source, target);
			positionInfoList.add(target);
		}
		
		return positionInfoList;
	}
			
	/**
	 * 删除测试结果。
	 * @param stockCode 证券代码
	 */
	public void deleteTestResult (String stockCode) {
		testFundsFlowRepository.dropCollection(stockCode);
		testOrderInfoRepository.dropCollection(stockCode);
		testEverySumPositionInfoRepository.dropCollection(stockCode);
	}
	
	/**
	 * 为测试结果增加索引。
	 * @param stockCode 证券代码
	 */
	public void ensureIndexForTestResult (String stockCode) {
		testFundsFlowRepository.createIndex(stockCode);
		testOrderInfoRepository.createIndex(stockCode);
		testEverySumPositionInfoRepository.createIndex(stockCode);
	}
	
	/**
	 * 保存银行转存记录。
	 * 
	 * @param businessType 业务类型
	 * @param stockCode 证券代码
	 * @param tradeDate 转账日期 （格式：yyyyMMddhhmmssSSS）
	 * @param transferMoney 转存金额
	 * @return boolean true：转存成功；false：转存失败
	 */
	public boolean 
	insertBankTransfer (
			FundsFlowBusiness businessType, String stockCode, 
			Long tradeDate, BigDecimal transferMoney) {
		
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用  [保存银行转存记录] 方法").append("\n");
		logMsg.append("@param [businessType = " + businessType + "]\n");
		logMsg.append("@param [stockCode = " + stockCode + "]\n");
		logMsg.append("@param [tradeDate = " + tradeDate + "]\n");
		logMsg.append("@param [transferMoney = " + transferMoney + "]\n");
		log.info(logMsg.toString());
		
		// --- 参数完整性验证 ---
		// #TODO 略
		
		// --- 业务逻辑正确性验证 ---
		if (businessType != FundsFlowBusiness.ROLL_IN && businessType != FundsFlowBusiness.ROLL_OUT) {
			log.error("在保存银行转存记录时出现错误，业务类型只能是银行转入或资金转出！[businessType = " + businessType + "]");
			return false;
		}
		
		FundsFlowPO oldFundsFlowPo = testFundsFlowRepository.findNewOne(stockCode);         // 为了找到剩余资金，需要找到最新的一条资金流水。
		BigDecimal oldFundsBalance = (oldFundsFlowPo != null)                               // 得到数据库中最新的资金余额。
		                             ? oldFundsFlowPo.getFundsBalance() 
		                             : new BigDecimal(0);
		                             
		if (oldFundsBalance == null) {
	    	log.error("在保存银行转存记录时出现错误，数据库记录中的资金余额不能为0，请检查程序，完善数据的完整性！");
	    	return false;
	    }
		
	    if (oldFundsFlowPo != null) {
	    	Long oldTradeDate = oldFundsFlowPo.getTradeDate();
	    	if (oldTradeDate == null) {
	    		log.error("在保存银行转存记录时出现错误，数据库记录中的交易日期不能为null，请检查程序，完善数据的完整性！");
	    		return false;
	    	}

	    	if (tradeDate < oldTradeDate) {
	    		log.error("在保存银行转存记录时出现错误，新插入记录的日期不能小于数据库中其他记录的日期！" +
	    				"[tradeDate = " + tradeDate + "] | [oldTradeDate = " + oldTradeDate + "]");
	    		return false;
	    	}
	    }
	    
	    if (transferMoney.doubleValue() < 0) {
	    	log.error("在保存银行转存记录时出现错误，转入转出的金额不能为负数！ [transferMoney = " + transferMoney + "]");
    		return false;
	    }
	    
	    if (businessType == FundsFlowBusiness.ROLL_OUT) {
	    	if (transferMoney.compareTo(oldFundsBalance) == 1) {
	    		log.error("在保存银行转存记录时出现错误，转出的金额不能超过资金余额！" +
	    				"[oldFundsBalance = " + oldFundsBalance + "] | [transferMoney = " + transferMoney + "]");
	    		return false;
	    	}
	    }
	    
	    // --- 计算新的资金余额 ---
	    BigDecimal fundsBalance = null;
	    if (businessType == FundsFlowBusiness.ROLL_IN) {
	    	fundsBalance = oldFundsBalance.add(transferMoney);
	    } else {
	    	fundsBalance = oldFundsBalance.subtract(transferMoney);
	    }
	    
	    // --- 构造新的资金流水记录 ---
		FundsFlowPO po = new FundsFlowPO();
		/* 合同编号。 */
		po.setContractCode(UUID.randomUUID().toString());
		/* 证券代码。 */
		po.setStockCode(stockCode);
		/* 证券名称。 */
		po.setStockName(null);
		/* 交易日期 （格式：yyyyMMddhhmmssSSS）。 */
		po.setTradeDate(tradeDate);
		/* 成交价格。 */
		po.setTradePrice(null);
		/* 成交数量。 */
		po.setTradeNumber(null);
		/* 成交金额。 */
		po.setTradeMoney(transferMoney.setScale(2, RoundingMode.HALF_UP));
		/* 资金余额。 */
		po.setFundsBalance(fundsBalance.setScale(2, RoundingMode.HALF_UP));
		/* 业务名称。 */
		po.setBusinessName(businessType.getType());
		/* 手续费。 */
		po.setCharges(null);
		/* 印花税。 */
		po.setStampDuty(null);
		/* 过户费。 */
		po.setTransferFee(null);
		/* 结算费。 */
		po.setClearingFee(null);
		/* 股东代码。*/
		po.setStockholder(null);
		
		testFundsFlowRepository.insert(po);
		
		return true;
	}
	
	/**
	 * 保存买入证券时的资金流水记录和仓位信息。
	 * 执行顺序： 1、增加一条资金流水记录；
	 *        2、增加一条买入订单信息；
	 *        3、增加每一笔持仓信息;
	 *        4、修改之前持仓记录的止损价格。
	 *        
	 * @param systemOpenPoint 系统建仓点
	 * @param stockCode 证券代码
	 * @param openSignalDate 建仓信号发出时间 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeDate 交易日期 （格式：yyyyMMddhhmmssSSS）
	 * @param tradePrice 成交价格
	 * @param stopPrice 止损价格
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 */
	public void 
	insertBuyInfo (
			DealSignal systemOpenPoint, String stockCode, Long openSignalDate, 
			Long tradeDate, BigDecimal tradePrice, BigDecimal stopPrice) 
	throws NumberFormatException, ParseException {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用  [保存买入证券时的资金流水记录和仓位信息] 方法").append("\n");
		logMsg.append("@param [systemOpenPoint = " + systemOpenPoint + "]\n");
		logMsg.append("@param [stockCode = " + stockCode + "]\n");
		logMsg.append("@param [openSignalDate = " + openSignalDate + "]\n");
		logMsg.append("@param [tradeDate = " + tradeDate + "]\n");
		logMsg.append("@param [tradePrice = " + tradePrice + "]\n");
		logMsg.append("@param [stopPrice = " + stopPrice + "]\n");
		log.info(logMsg.toString());
		
		if (tradePrice == null || tradePrice.doubleValue() <= 0) {
			log.warn("购买价格出现小于等于0的情况[ tradePrice = "+ tradePrice +" ]，程序将退出本次购买。");
    		return;
		}
		
		// --- 执行仓位控制策略 ---
		if (!positionControlForBuy(systemOpenPoint, stockCode, openSignalDate, tradeDate, tradePrice, stopPrice)) {
    		return;
		}
		
		
		// --- 参数完整性验证 ---
		// #TODO 略
		
	    // --- 验证即将保存的资金流水记录中的业务数据的正确性 ---
		FundsFlowPO oldFundsFlowPo = testFundsFlowRepository.findNewOne(stockCode);                                    // 为了找到剩余资金，需要找到最新的一条资金流水。
		if (oldFundsFlowPo == null) {
			log.error("在数据库中没有查出该证券的任何资金流水记录！[stockCode = " + stockCode +"]");
	    	return;
		}
		
		BigDecimal oldFundsBalance = oldFundsFlowPo.getFundsBalance();                                                 // 得到已存在的资金余额。
		if (oldFundsBalance == null || oldFundsBalance.doubleValue() == 0) {
	    	log.error("资金余额不足，不能再继续购买证券！[oldFundsBalance = " + oldFundsBalance +"]");
	    	return;
	    }
		
		Long oldTradeDateOfFundsFlow = oldFundsFlowPo.getTradeDate();                                                  // 得到已存在的成交日期（格式：yyyyMMddhhmmssSSS）。
    	if (oldTradeDateOfFundsFlow == null) {
    		log.error("数据库记录中资金流水的交易日期不能为null，请检查程序，完善数据的完整性！");
    		return;
    	}
    	
    	Long newTradeDateOfFundsFlow = tradeDate;                                                                      // 得到最新的资金流水的成交日期（格式：yyyyMMddhhmmssSSS）。
    	if (newTradeDateOfFundsFlow < oldTradeDateOfFundsFlow) {
    		log.error(
    				"新插入资金流水记录中的交易日期不能小于已存在资金流水记录中的交易日期！" +
    				"[newTradeDateOfFundsFlow = " + newTradeDateOfFundsFlow + "] | " +
    				"[oldTradeDateOfFundsFlow = " + oldTradeDateOfFundsFlow + "]");
    		return;
    	}

    	// --- 验证即将保存的订单记录中的业务数据的正确性 ---
    	OrderInfoPO oldOrderInfoPO = testOrderInfoRepository.findNewOne(stockCode);                                    // 得到最新的一条订单记录。
    	Long newTradeDateOfOrderInfo = tradeDate;                                                                      // 得到最新的订单记录的成交日期（格式：yyyyMMddhhmmssSSS）。
    	
    	if (oldOrderInfoPO != null) {
    		Long oldTradeDateOfOrderInfo = oldOrderInfoPO.getTradeDate();                                              // 得到已存在的成交日期（格式：yyyyMMddhhmmssSSS）。
        	if (oldTradeDateOfOrderInfo == null) {
        		log.error("数据库记录中订单记录的交易日期不能为null，请检查程序，完善数据的完整性！");
        		return;
        	}
        	
        	if (newTradeDateOfOrderInfo < oldTradeDateOfOrderInfo) {
        		log.error(
        				"新插入订单记录中的交易日期不能小于已存在订单记录中的交易日期！" +
        				"[newTradeDateOfOrderInfo = " + newTradeDateOfOrderInfo + "] | " +
        				"[oldTradeDateOfOrderInfo = " + oldTradeDateOfOrderInfo + "]");
        		return;
        	}
    	}
		
    	
    	// --- 验证即将保存的仓位记录中的业务数据的正确性 ---
    	EverySumPositionInfoPO oldEverySumPositionInfoPO = testEverySumPositionInfoRepository.findNewOne(stockCode);   // 得到最新的一条建仓记录。
    	Long newOpenDateOfEverySumPositionInfo = tradeDate;                                                            // 得到最新的建仓记录的建仓日期（格式：yyyyMMddhhmmssSSS）。
    	
    	if (oldEverySumPositionInfoPO != null) {
    		Long oldOpenDateOfEverySumPositionInfo = oldEverySumPositionInfoPO.getOpenDate();                          // 得到已存在的建仓日期（格式：yyyyMMddhhmmssSSS）。
        	if (oldOpenDateOfEverySumPositionInfo == null) {
        		log.error("数据库记录中建仓记录的建仓日期不能为null，请检查程序，完善数据的完整性！");
        		return;
        	}
        	
        	if (newOpenDateOfEverySumPositionInfo < oldOpenDateOfEverySumPositionInfo) {
        		log.error(
        				"新插入建仓记录中的建仓日期不能小于已存在建仓记录中的建仓日期！" +
        				"[newOpenDateOfEverySumPositionInfo = " + newOpenDateOfEverySumPositionInfo + "] | " +
        				"[oldOpenDateOfEverySumPositionInfo = " + oldOpenDateOfEverySumPositionInfo + "]");
        		return;
        	}
    	}
    	
    	
    	// --- 构造在买入证券过程中都需要用到的数据 ---
    	String contractCode = UUID.randomUUID().toString();                                                            // 合同ID。
		String systemName = "顶底分型交易系统";                                                                             // 系统名称。
		
		
		/*
		 * +-----------------------------------------------------------+
		 * + 仓风控制：使用2%，一次最少购买200股。                                                                                               +
		 * +-----------------------------------------------------------+
		 * 
		 * 目的：减少一买试错成本，提高斐波那契盈利。
		 */
		BigDecimal buyMoneyRate = new BigDecimal(0.02);                                                                // 买入资金比率（使用2%原则）。
		
		BigDecimal budgetMoney = oldFundsBalance.multiply(buyMoneyRate);                                               // 计算预算资金。
    	Long tradeNumber =                                                                                             // 计算成交数量。
    		budgetMoney.divide(tradePrice, 0, RoundingMode.HALF_UP).longValue();
    	while (tradeNumber == null || tradeNumber < 200) {
    		if (buyMoneyRate.doubleValue() >= 0.2) {
    			log.error("买入证券的数量必须大于等于200股，且必须是100的倍数，但是即使用了剩余资金的20%也不能购买，所以放弃本次购买！" +
    					"[oldFundsBalance = " + oldFundsBalance + "] | [buyMoneyRate = " + buyMoneyRate + "] | " +
    							"[budgetMoney = " + budgetMoney + "] | [tradeNumber = " + tradeNumber + "]");
    			return;
    		}
    		
    		/*
			 * +-----------------------------------------------------------+
			 * + 重新计算买入资金比率、预算资金和成交数量。                                                                                          +
			 * +-----------------------------------------------------------+
			 */
    		buyMoneyRate = buyMoneyRate.add(new BigDecimal(0.01)).setScale(2, RoundingMode.HALF_UP);
    		budgetMoney = oldFundsBalance.multiply(buyMoneyRate).setScale(3, RoundingMode.HALF_UP);
    		tradeNumber = budgetMoney.divide(tradePrice, 0, RoundingMode.HALF_UP).longValue();
    	}
    	tradeNumber -= (tradeNumber % 100);                                                                            // 对成交数量进行取整操作。（股市中一手100股）
		
		BigDecimal amountMoney = tradePrice.multiply(new BigDecimal(tradeNumber));                                     // 计算买入或卖出的发生金额。
		
		BigDecimal charges = dealFeeCalculator.calcCharges(amountMoney);                                               // 计算手续费（目前手续费双向收取）。
		BigDecimal stampDuty = new BigDecimal(0);                                                                      // 计算印花税（目前只有在卖出时收取）。
		BigDecimal transferFee = dealFeeCalculator.calcTransferFee(stockCode, tradeNumber);                            // 计算过户费（目前过户费只有在上交所双向收取，深交所不收）。
		BigDecimal clearingFee = new BigDecimal(0);                                                                    // 结算费（不购买B股不用计算）。
		BigDecimal totleFee = charges.add(stampDuty).add(transferFee).add(clearingFee);                                // 计算总共的费用。
		
		BigDecimal tradeMoney = amountMoney.add(totleFee);                                                             // 计算成交金额。买入时：成交金额 ==（交易总金额 + 手续费 + 印花税 + 过户费 + 结算费）。
		if (oldFundsBalance.compareTo(tradeMoney) == -1) {
    		log.error("保存证券买卖资金流水记录时出现错误，购买金额不能超过资金余额！" +
    				"[oldFundsBalance = " + oldFundsBalance + "] | [tradeMoney = " + tradeMoney + "]");
    		return;
    	}
		BigDecimal fundsBalance = oldFundsBalance.subtract(tradeMoney);                                                // 计算资金余额。
		BigDecimal openCost = tradeMoney;                                                                              // 成本金额（此时成本金额就是成交金额）。
		
		/*
		 * +-----------------------------------------------------------+
		 * + 构造资金流水 。                                                                                                                                                 +
		 * +-----------------------------------------------------------+
		 */   
		FundsFlowPO fundsFlowPO = new FundsFlowPO();
		/* 合同编号。 */
		fundsFlowPO.setContractCode(contractCode);
		
		// --- 
		/* 证券代码。 */
		fundsFlowPO.setStockCode(stockCode);
		/* 证券名称。 */
		fundsFlowPO.setStockName(null);
		/* 交易日期（格式：yyyyMMddhhmmssSSS）。 */
		fundsFlowPO.setTradeDate(newTradeDateOfFundsFlow);
		/* 成交价格。 */
		fundsFlowPO.setTradePrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
		/* 成交数量。 */
		fundsFlowPO.setTradeNumber(tradeNumber);
		/* 成交金额。 */
		fundsFlowPO.setTradeMoney(tradeMoney.setScale(3, RoundingMode.HALF_UP));
		/* 资金余额。 */
		fundsFlowPO.setFundsBalance(fundsBalance.setScale(3, RoundingMode.HALF_UP));
		
		// --- 
		/* 业务名称。 */
		fundsFlowPO.setBusinessName(FundsFlowBusiness.STOCK_BUY.getType());
		
		// --- 
		/* 手续费。 */
		fundsFlowPO.setCharges(charges);
		/* 印花税。 */
		fundsFlowPO.setStampDuty(stampDuty);
		/* 过户费。 */
		fundsFlowPO.setTransferFee(transferFee);
		/* 结算费。 */
		fundsFlowPO.setClearingFee(clearingFee);
		
		/*
		 * +-----------------------------------------------------------+
		 * + 构造订单信息 。                                                                                                                                                 +
		 * +-----------------------------------------------------------+
		 */
		OrderInfoPO orderInfoPO = new OrderInfoPO();
		/* 合同编号。 */
		orderInfoPO.setContractCode(contractCode);
		
		// --- 
		/* 系统名称。 */
		orderInfoPO.setSystemName(systemName);
		/* 证券代码。 */
		orderInfoPO.setStockCode(stockCode);
		/* 证券名称。 */
		orderInfoPO.setStockName(null);
		/* 成交日期（格式：yyyyMMddhhmmssSSS）。 */
		orderInfoPO.setTradeDate(newTradeDateOfOrderInfo);
		/* 买卖标志。*/
		orderInfoPO.setTradeFlag(OrderInfoTradeFlag.STOCK_BUY.getType());
		/* 成交价格。 */
		orderInfoPO.setTradePrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
		/* 成交数量。 */
		orderInfoPO.setTradeNumber(tradeNumber);
		/* 成交金额。 */
		orderInfoPO.setTradeMoney(amountMoney.setScale(3, RoundingMode.HALF_UP));
		/*
		 * +-----------------------------------------------------------+
		 * + 每一笔持仓信息 。                                                                                                                                             +
		 * +-----------------------------------------------------------+
		 */
		EverySumPositionInfoPO everySumPositionInfoPO = new EverySumPositionInfoPO();
		
		// ---
		/* 系统名称。*/
		everySumPositionInfoPO.setSystemName(systemName);
		/* 证券代码。 */
		everySumPositionInfoPO.setStockCode(stockCode);
		/* 证券名称。 */
		everySumPositionInfoPO.setStockName(null);
		
		// ---
		/* 建仓合同编号。 */
		everySumPositionInfoPO.setOpenContractCode(contractCode);
		/* 系统建仓点。*/
		everySumPositionInfoPO.setSystemOpenPoint(systemOpenPoint.getType());
		/* 系统建仓点名称。*/
		everySumPositionInfoPO.setSystemOpenName(systemOpenPoint.getName());
		/* 建仓信号发出时间（格式：yyyyMMddhhmmssSSS）。*/
		everySumPositionInfoPO.setOpenSignalDate(openSignalDate);
		/* 建仓日期（格式：yyyyMMddhhmmssSSS）。 */
		everySumPositionInfoPO.setOpenDate(newOpenDateOfEverySumPositionInfo);
		/* 建仓价格。 */
		everySumPositionInfoPO.setOpenPrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
		/* 建仓数量。 */
		everySumPositionInfoPO.setOpenNumber(tradeNumber);
		/* 建仓成本。 */
		everySumPositionInfoPO.setOpenCost(openCost.setScale(3, RoundingMode.HALF_UP));
		
		// ---
		/* 可平仓数量。*/
		everySumPositionInfoPO.setCanCloseNumber(null);
		/* 止损价格。*/
		everySumPositionInfoPO.setStopPrice(stopPrice.setScale(3, RoundingMode.HALF_UP));
		
		// ---
		
		BigDecimal newMarketValue = amountMoney;                                                                       // 最新市值。公式 = 证券价格 * 证券数量 
		BigDecimal floatProfitAndLoss = newMarketValue.subtract(openCost);                                             // 浮动盈亏。公式 = 最新市值 - 建仓成本 
		BigDecimal profitAndLossRatio =                                                                                // 盈亏比例。公式 = 浮动盈亏 / 建仓成本
			floatProfitAndLoss.divide(openCost, 3, RoundingMode.HALF_UP);
		
		/* 当前价。 */
		everySumPositionInfoPO.setNewPrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
		/* 最新市值。 */
		everySumPositionInfoPO.setNewMarketValue(newMarketValue.setScale(3, RoundingMode.HALF_UP));
		/* 浮动盈亏。 */
		everySumPositionInfoPO.setFloatProfitAndLoss(floatProfitAndLoss.setScale(3, RoundingMode.HALF_UP));
		/* 盈亏比例。 */
		everySumPositionInfoPO.setProfitAndLossRatio(profitAndLossRatio.setScale(3, RoundingMode.HALF_UP));
		
		/*
		 * +-----------------------------------------------------------+
		 * + 把数据记录保存到数据库中 。                                                                                                                         +
		 * +-----------------------------------------------------------+
		 */
		testFundsFlowRepository.insert(fundsFlowPO);                                                                   // 保存资金流水记录。
		testOrderInfoRepository.insert(orderInfoPO);                                                                   // 保存订单信息记录。
		testEverySumPositionInfoRepository.insert(everySumPositionInfoPO);                                             // 保存每一笔持仓信息记录。
		
	}

	/**
	 * 保存卖出证券时的资金流水记录和仓位信息。
	 * 执行顺序： 1、修改每一笔持仓记录；
	 *        2、增加一条卖出订单信息；
	 *        3、增加一条资金流水记录。
	 * 
	 * @param systemClosePoint 系统平仓点
	 * @param stockCode 证券代码
	 * @param openContractCode 建仓合同编号
	 * @param closeSignalDate 平仓信号发出时间 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeDate 交易日期 （格式：yyyyMMddhhmmssSSS）
	 * @param tradePrice 成交价格
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 */
	public void 
	insertSellInfo (
			DealSignal systemClosePoint, String stockCode, String openContractCode, 
			Long closeSignalDate, Long tradeDate, BigDecimal tradePrice) 
	throws NumberFormatException, ParseException {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用  [保存买入证券时的资金流水记录和仓位信息] 方法").append("\n");
		logMsg.append("@param [systemClosePoint = " + systemClosePoint + "]\n");
		logMsg.append("@param [stockCode = " + stockCode + "]\n");
		logMsg.append("@param [openContractCode = " + openContractCode + "]\n");
		logMsg.append("@param [closeSignalDate = " + closeSignalDate + "]\n");
		logMsg.append("@param [tradeDate = " + tradeDate + "]\n");
		logMsg.append("@param [tradePrice = " + tradePrice + "]\n");
		log.info(logMsg.toString());
		
		// --- 参数完整性验证 ---
		// #TODO 略
		
	    // --- 业务逻辑正确性验证 ---
		if (
				systemClosePoint != DealSignal.SELL_ONE_TENTH &&
				systemClosePoint != DealSignal.SELL_TWO_TENTH &&
				systemClosePoint != DealSignal.SELL_THREE_TENTH &&
				systemClosePoint != DealSignal.SELL_FOUR_TENTH &&
				systemClosePoint != DealSignal.SELL_FIVE_TENTH &&
				systemClosePoint != DealSignal.SELL_SIX_TENTH &&
				systemClosePoint != DealSignal.SELL_SEVEN_TENTH &&
				systemClosePoint != DealSignal.SELL_EIGHT_TENTH &&
				systemClosePoint != DealSignal.SELL_NINE_TENTH &&
				systemClosePoint != DealSignal.SELL_ALL
			) {
			log.error("传入的平仓点不合法！[systemClosePoint = " + systemClosePoint + "]");
			return;
		}
		
	    // --- 验证即将保存的资金流水记录中的业务数据的正确性 ---
		FundsFlowPO oldFundsFlowPo = testFundsFlowRepository.findNewOne(stockCode);                                    // 为了找到剩余资金，需要找到最新的一条资金流水。
		if (oldFundsFlowPo == null) {
			log.error("在数据库中没有查出该证券的任何资金流水记录！[stockCode = " + stockCode +"]");
	    	return;
		}
		
		BigDecimal oldFundsBalance = oldFundsFlowPo.getFundsBalance();                                                 // 得到已存在的资金余额。
		if (oldFundsBalance == null) {
	    	log.error("资金余额数据错误！[oldFundsBalance = " + oldFundsBalance +"]");
	    	return;
	    }
		
		Long oldTradeDateOfFundsFlow = oldFundsFlowPo.getTradeDate();                                                  // 得到已存在的成交日期（格式：yyyyMMddhhmmssSSS）。
    	if (oldTradeDateOfFundsFlow == null) {
    		log.error("数据库记录中资金流水的交易日期不能为null，请检查程序，完善数据的完整性！");
    		return;
    	}
    	
    	Long newTradeDateOfFundsFlow = tradeDate;                                                                      // 得到最新的资金流水的成交日期（格式：yyyyMMddhhmmssSSS）。
    	if (newTradeDateOfFundsFlow < oldTradeDateOfFundsFlow) {
    		log.error(
    				"新插入资金流水记录中的交易日期不能小于已存在资金流水记录中的交易日期！" +
    				"[newTradeDateOfFundsFlow = " + newTradeDateOfFundsFlow + "] | " +
    				"[oldTradeDateOfFundsFlow = " + oldTradeDateOfFundsFlow + "]");
    		return;
    	}
    	
    	// --- 验证即将保存的订单记录中的业务数据的正确性 ---
    	OrderInfoPO oldOrderInfoPO = testOrderInfoRepository.findNewOne(stockCode);                                    // 得到最新的一条订单记录。
    	if (oldOrderInfoPO == null) {
			log.error("在数据库中没有查出该证券的任何订单记录！[stockCode = " + stockCode +"]");
	    	return;
		}
    	
    	Long newTradeDateOfOrderInfo = tradeDate;                                                                      // 得到最新的订单记录的成交日期（格式：yyyyMMddhhmmssSSS）。
    	Long oldTradeDateOfOrderInfo = oldOrderInfoPO.getTradeDate();                                                  // 得到已存在的成交日期（格式：yyyyMMddhhmmssSSS）。
    	if (oldTradeDateOfOrderInfo == null) {
    		log.error("数据库记录中订单记录的交易日期不能为null，请检查程序，完善数据的完整性！");
    		return;
    	}
    	
    	if (newTradeDateOfOrderInfo < oldTradeDateOfOrderInfo) {
    		log.error(
    				"新插入订单记录中的交易日期不能小于已存在订单记录中的交易日期！" +
    				"[newTradeDateOfOrderInfo = " + newTradeDateOfOrderInfo + "] | " +
    				"[oldTradeDateOfOrderInfo = " + oldTradeDateOfOrderInfo + "]");
    		return;
    	}
    	
		/*
		 * +-----------------------------------------------------------+
		 * + 每一笔持仓信息 。                                                                                                                                             +
		 * +-----------------------------------------------------------+
		 */
		List<EverySumPositionInfoPO> everySumPositionInfoList =                                                        // 查询出每一笔未平仓的仓位信息。
			testEverySumPositionInfoRepository
			.findEverySumPositionInfoList(stockCode, openContractCode, null, null, "0", null, null);
		if (everySumPositionInfoList == null || everySumPositionInfoList.isEmpty()) {
			log.error("在卖出时未找到任何该证券的持仓记录！[stockCode = " + stockCode + "]！");
    		return;
		}
		
		/*
		 * 在测试交易系统的过程中出现卖出信号时，以“盈利超过50%，平掉5/10的仓位”为例，由于每笔仓位的盈利情况不同，会导致某笔仓位盈利
		 * 超过50%，甚至100%，而某些仓位则小于50%，甚至为亏损。如果不对仓位以盈利幅度进行排序，而仅以建仓时间排序，那就可能会出现，
		 * 本次仅平掉了部分亏损的仓位，而在下一天又满足了“盈利超过50%，平掉5/10的仓位”，这就会导致：
		 * 1、降低测试报告中的胜率；
		 * 2、短时间内的频繁平仓。
		 */
		Collections.sort(everySumPositionInfoList, new Comparator<EverySumPositionInfoPO>() {
			@Override
			public int compare(EverySumPositionInfoPO o1, EverySumPositionInfoPO o2) {
				if (o1.getProfitAndLossRatio().compareTo(o2.getProfitAndLossRatio()) < 0) {
					return 1;
				}
				
				return -1;
			}
		});
		
		// --- 计算本次的平仓数量 ---
		BigDecimal allCanCloseNumber = new BigDecimal(0);                                                              // 记录所有未平仓的仓位数量。
		for (EverySumPositionInfoPO everySumPositionInfoPO : everySumPositionInfoList) {
			allCanCloseNumber = allCanCloseNumber
			.add(new BigDecimal(everySumPositionInfoPO.getCanCloseNumber()));
		}
		
		BigDecimal sellPercent = new BigDecimal(0);                                                                   // 计算需要卖掉仓位的百分比。
		if (systemClosePoint == DealSignal.SELL_ONE_TENTH) {
			sellPercent = new BigDecimal(0.1);
		} else if (systemClosePoint == DealSignal.SELL_TWO_TENTH) {
			sellPercent = new BigDecimal(0.2);
		} else if (systemClosePoint == DealSignal.SELL_THREE_TENTH) {
			sellPercent = new BigDecimal(0.3);
		} else if (systemClosePoint == DealSignal.SELL_FOUR_TENTH) {
			sellPercent = new BigDecimal(0.4);
		} else if (systemClosePoint == DealSignal.SELL_FIVE_TENTH) {
			sellPercent = new BigDecimal(0.5);
		} else if (systemClosePoint == DealSignal.SELL_SIX_TENTH) {
			sellPercent = new BigDecimal(0.6);
		} else if (systemClosePoint == DealSignal.SELL_SEVEN_TENTH) {
			sellPercent = new BigDecimal(0.7);
		} else if (systemClosePoint == DealSignal.SELL_EIGHT_TENTH) {
			sellPercent = new BigDecimal(0.8);
		} else if (systemClosePoint == DealSignal.SELL_NINE_TENTH) {
			sellPercent = new BigDecimal(0.9);
		} else if (systemClosePoint == DealSignal.SELL_ALL) {
			sellPercent = new BigDecimal(1);
		}
		
		BigDecimal needSellPositionNums =                                                                             // 计算需要卖掉的仓位数量。
			allCanCloseNumber.multiply(sellPercent).setScale(0, RoundingMode.HALF_UP);
		needSellPositionNums =                                                                                        // 最小的平仓数量为100股。
			(needSellPositionNums.longValue() < 100) ? new BigDecimal(100) : needSellPositionNums;
			
		BigDecimal positionRemainder = new BigDecimal(needSellPositionNums.longValue() % 100);                        // 对不符合平仓规则的仓位数量进行整理。
		if (positionRemainder.longValue() != 0) {
			needSellPositionNums = needSellPositionNums
			.add(new BigDecimal(100).subtract(positionRemainder))
			.setScale(0, RoundingMode.HALF_UP);
		}
		if (needSellPositionNums.longValue() > allCanCloseNumber.longValue()) {
			needSellPositionNums = new BigDecimal(allCanCloseNumber.longValue());
		}
		
		// --- 构造在卖出证券过程中都需要用到的数据 ---
    	String contractCode = UUID.randomUUID().toString();                                                           // 合同ID。
		String systemName = "顶底分型交易系统";                                                                            // 系统名称。
		
		List<EverySumPositionInfoPO> needUpdateEverySumPositionInfoList =                                             // 装载需要修改的每一笔未平仓的仓位信息。
			new LinkedList<EverySumPositionInfoPO>();
		for (EverySumPositionInfoPO everySumPositionInfoPO : everySumPositionInfoList) {
			
			if (needSellPositionNums.longValue() <= 0) {
				break;
			}
			
			if (
					everySumPositionInfoPO.getCanCloseNumber() == null || 
					everySumPositionInfoPO.getCanCloseNumber() == 0) {
				continue;
			}
			
			// 构造一个专门用于修改的对象。
			EverySumPositionInfoPO updatePO = new EverySumPositionInfoPO();
			/* id */
			updatePO.setId(everySumPositionInfoPO.getId());
			/* 证券代码。*/
			updatePO.setStockCode(everySumPositionInfoPO.getStockCode());
			/* 平仓合同编号。*/
			updatePO.setCloseContractCode(null);
			/* 股东代码。*/
			updatePO.setStockholder(null);
			
			// ---
			Long oldCanCloseNumber = everySumPositionInfoPO.getCanCloseNumber();                                      // 可平仓数量。
			Long oldCloseNumber = everySumPositionInfoPO.getCloseNumber();                                            // 平仓数量。
			oldCloseNumber = (oldCloseNumber == null) ? 0 : oldCloseNumber;

			// 如果需要平仓的数量 >= 该笔仓位的数量，则把该笔仓位全部平掉。如果 < 该笔仓位的数量，则仅修改该笔仓位的可平仓数量和平仓数量。
			if (needSellPositionNums.longValue() >= oldCanCloseNumber.longValue()) {
				
				needSellPositionNums =                                                                                // 重新计算需要平仓的数量。
					needSellPositionNums.subtract(new BigDecimal(oldCanCloseNumber));
				
				/* 可平仓数量。*/
				updatePO.setCanCloseNumber(0L);
				
				/* 平仓合同编号。*/
				updatePO.setCloseContractCode(contractCode);
				/* 系统平仓点。*/
				updatePO.setSystemClosePoint(systemClosePoint.getType());
				/* 系统平仓点名称。*/
				updatePO.setSystemCloseName(systemClosePoint.getName());
				/* 平仓信号发出时间（格式：yyyyMMddhhmmssSSS）。*/
				updatePO.setCloseSignalDate(closeSignalDate);
				/* 平仓日期（格式：yyyyMMddhhmmssSSS）。 */
				updatePO.setCloseDate(tradeDate);
				/* 平仓价格。 */
				updatePO.setClosePrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
				/* 平仓数量。 */
				updatePO.setCloseNumber(oldCanCloseNumber + oldCloseNumber);

				// ---
				BigDecimal openCost = everySumPositionInfoPO.getOpenCost();                                           // 建仓成本。
				Long closeNumber = oldCanCloseNumber;                                                                 // 本次平仓数量。
				
				BigDecimal newMarketValue = tradePrice.multiply(new BigDecimal(closeNumber));                         // 最新市值。公式 = 证券价格 * 证券数量    
				BigDecimal floatProfitAndLoss = newMarketValue.subtract(openCost);                                    // 浮动盈亏。公式 = 最新市值 * 建仓成本 
				BigDecimal profitAndLossRatio = floatProfitAndLoss.divide(                                            // 盈亏比例。公式 = 浮动盈亏 / 建仓成本
						openCost, 3, RoundingMode.HALF_UP);
				
				/* 当前价。 */
				updatePO.setNewPrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
				/* 最新市值。 */
				updatePO.setNewMarketValue(newMarketValue.setScale(3, RoundingMode.HALF_UP));
				/* 浮动盈亏。 */
				updatePO.setFloatProfitAndLoss(floatProfitAndLoss.setScale(3, RoundingMode.HALF_UP));
				/* 盈亏比例。 */
				updatePO.setProfitAndLossRatio(profitAndLossRatio.setScale(3, RoundingMode.HALF_UP));
				
			} else {
				
				Long newCanCloseNumber =                                                                              // 计算该笔仓位的新的可平仓数量。
				new BigDecimal(oldCanCloseNumber)
				.subtract(needSellPositionNums).longValue();
				
				// --- 这里就不计算盈亏比这些信息了，因为每天刷盘后会自动计算。
				
				/* 可平仓数量。*/
				updatePO.setCanCloseNumber(newCanCloseNumber);
				/* 平仓数量。 */
				updatePO.setCloseNumber(needSellPositionNums.longValue());
				
				needSellPositionNums = new BigDecimal(0);                                                             // 重新计算需要平仓的数量。
			}
			
			needUpdateEverySumPositionInfoList.add(updatePO);                                                         // 向集合中加入需要修改的仓位信息。
		}
		
		Long tradeNumber = 0L;                                                                                        // 卖出总数量。
		for (EverySumPositionInfoPO everySumPositionInfoPO : needUpdateEverySumPositionInfoList) {
			tradeNumber += everySumPositionInfoPO.getCloseNumber();
		}

		if (tradeNumber == null || tradeNumber < 100) {
			log.error("卖出证券的数量必须大于等于100股，且必须是100的倍数！[tradeNumber = " + tradeNumber + "]");
			return;
		}

		// --- 计算使用资金与剩余资金 ---
	    
		BigDecimal amountMoney = tradePrice.multiply(new BigDecimal(tradeNumber));                                    // 计算买入或卖出的发生金额。
		
		BigDecimal charges = dealFeeCalculator.calcCharges(amountMoney);                                              // 计算手续费（目前手续费双向收取）。
		BigDecimal stampDuty = dealFeeCalculator.calStampDuty(amountMoney);                                           // 计算印花税（目前只有在卖出时收取）。
		BigDecimal transferFee = dealFeeCalculator.calcTransferFee(stockCode, tradeNumber);                           // 计算过户费（目前过户费只有在上交所双向收取，深交所不收）。
		BigDecimal clearingFee = new BigDecimal(0);                                                                   // 结算费（不购买B股不用计算）。
		BigDecimal totleFee = charges.add(stampDuty).add(transferFee).add(clearingFee);                               // 计算总共的费用。
		
		BigDecimal tradeMoney = amountMoney.subtract(totleFee);                                                       // 计算成交金额。卖出时：成交金额 ==（交易总金额 - 手续费 - 印花税 - 过户费 - 结算费）。
		BigDecimal fundsBalance = oldFundsBalance.add(tradeMoney);                                                    // 计算资金余额。
		
		/*
		 * +-----------------------------------------------------------+
		 * + 构造订单信息 。                                                                                                                                                 +
		 * +-----------------------------------------------------------+
		 */
		OrderInfoPO orderInfoPO = new OrderInfoPO();
		/* 合同编号。 */
		orderInfoPO.setContractCode(contractCode);
		
		// --- 
		/* 系统名称。 */
		orderInfoPO.setSystemName(systemName);
		/* 证券代码。 */
		orderInfoPO.setStockCode(stockCode);
		/* 证券名称。 */
		orderInfoPO.setStockName(null);
		/* 成交日期。 */
		orderInfoPO.setTradeDate(newTradeDateOfOrderInfo);
		/* 买卖标志。*/
		orderInfoPO.setTradeFlag(OrderInfoTradeFlag.STOCK_SELL.getType());
		/* 成交价格。 */
		orderInfoPO.setTradePrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
		/* 成交数量。 */
		orderInfoPO.setTradeNumber(tradeNumber);
		/* 成交金额。 */
		orderInfoPO.setTradeMoney(amountMoney.setScale(3, RoundingMode.HALF_UP));
		
		/*
		 * +-----------------------------------------------------------+
		 * + 构造资金流水 。                                                                                                                                                 +
		 * +-----------------------------------------------------------+
		 */   
		FundsFlowPO fundsFlowPO = new FundsFlowPO();
		/* 合同编号。 */
		fundsFlowPO.setContractCode(contractCode);
		
		// --- 
		/* 证券代码。 */
		fundsFlowPO.setStockCode(stockCode);
		/* 证券名称。 */
		fundsFlowPO.setStockName(null);
		/* 交易日期。 */
		fundsFlowPO.setTradeDate(newTradeDateOfFundsFlow);
		/* 成交价格。 */
		fundsFlowPO.setTradePrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
		/* 成交数量。 */
		fundsFlowPO.setTradeNumber(tradeNumber);
		/* 成交金额。 */
		fundsFlowPO.setTradeMoney(tradeMoney.setScale(3, RoundingMode.HALF_UP));
		/* 资金余额。 */
		fundsFlowPO.setFundsBalance(fundsBalance.setScale(3, RoundingMode.HALF_UP));
		
		// --- 
		/* 业务名称。 */
		fundsFlowPO.setBusinessName(FundsFlowBusiness.STOCK_SELL.getType());
		
		// --- 
		/* 手续费。 */
		fundsFlowPO.setCharges(charges);
		/* 印花税。 */
		fundsFlowPO.setStampDuty(stampDuty);
		/* 过户费。 */
		fundsFlowPO.setTransferFee(transferFee);
		/* 结算费。 */
		fundsFlowPO.setClearingFee(clearingFee);
		
		/*
		 * +-----------------------------------------------------------+
		 * + 把数据记录保存到数据库中 。                                                                                                                         +
		 * +-----------------------------------------------------------+
		 */
		
		for (EverySumPositionInfoPO everySumPositionInfoPO : needUpdateEverySumPositionInfoList) {                    // 修改每一笔持仓信息记录。
			testEverySumPositionInfoRepository.update(everySumPositionInfoPO);
		}
		testOrderInfoRepository.insert(orderInfoPO);                                                                  // 保存订单信息记录。                     
		testFundsFlowRepository.insert(fundsFlowPO);                                                                  // 保存资金流水记录。

	}
	
	/**
	 * 每天收盘时刷新每笔仓位信息。
	 * 
	 * @param stockCode 证券代码
	 * @param date 日期 （格式：yyyyMMddhhmmssSSS）
	 * @param newPrice 当前价格
	 */
	public void flushEverySumPositionInfoInClosing (String stockCode, Long date, BigDecimal newPrice) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用  [每天收盘时刷新每笔仓位信息] 方法").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]\n");
		logMsg.append("@param [date = " + date + "]\n");
		logMsg.append("@param [newPrice = " + newPrice + "]\n");
		log.info(logMsg.toString());
		
		List<EverySumPositionInfoPO> everySumPositionInfoList =                                      // 查询出每一笔未平仓的仓位信息。
			testEverySumPositionInfoRepository
			.findEverySumPositionInfoList(stockCode, null, null, null, "0", null, null);
		
		// 如果没有查询到每笔仓位信息就直接返回。
		if (everySumPositionInfoList == null || everySumPositionInfoList.isEmpty()) {
			return;
		}
		
		for (EverySumPositionInfoPO po : everySumPositionInfoList) {
			
			/*
			 * +-----------------------------------------------------------+
			 * + 目的：1、同步可平仓数量和当前价；2、计算最新市值；2、计算浮动盈亏和盈亏比。                     +
			 * +-----------------------------------------------------------+
			 */   
			
			// 构造一个专门用于修改的对象。
			EverySumPositionInfoPO updatePo = new EverySumPositionInfoPO();
			/* id。*/
			updatePo.setId(po.getId());
			/* 证券代码。*/
			updatePo.setStockCode(po.getStockCode());
			/* 平仓合同编号。*/
			updatePo.setCloseContractCode(null);
			/* 股东代码。*/
			updatePo.setStockholder(null);
			
			// --- 实际需要修改的部分 ---
			
			/* 可平仓数量。*/
			Long canCloseNumber = po.getCanCloseNumber();
			if (canCloseNumber == null && 
					Integer.valueOf(String.valueOf(date).substring(0, 8)).equals(Integer.valueOf(String.valueOf(po.getOpenSignalDate()).substring(0, 8)))) {
				canCloseNumber = po.getOpenNumber();
				updatePo.setCanCloseNumber(canCloseNumber);
			}

			/* 当前价。 */
			updatePo.setNewPrice(newPrice);
			
			BigDecimal newMarketValue = newPrice.multiply(new BigDecimal(canCloseNumber));           // 最新市值。公式 = 证券价格 * 证券数量 
			BigDecimal floatProfitAndLoss = newMarketValue.subtract(po.getOpenCost());               // 浮动盈亏。公式 = 最新市值 * 建仓成本 
			BigDecimal profitAndLossRatio = 
				floatProfitAndLoss.divide(po.getOpenCost(), 3, RoundingMode.HALF_UP);                // 盈亏比例。公式 = 浮动盈亏 / 建仓成本
			
			/* 最新市值。 */
			updatePo.setNewMarketValue(newMarketValue.setScale(3, RoundingMode.HALF_UP));
			/* 浮动盈亏。 */
			updatePo.setFloatProfitAndLoss(floatProfitAndLoss.setScale(3, RoundingMode.HALF_UP));
			/* 盈亏比例。 */
			updatePo.setProfitAndLossRatio(profitAndLossRatio.setScale(3, RoundingMode.HALF_UP));
			
			testEverySumPositionInfoRepository.update(updatePo);
		}
	}
	
	/**
	 * 查询某证券全部的资金流水信息（按照trade_date 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<EverySumPositionInfoPO>
	 */
	public List<FundsFlowPO> findAllFundsFlow (String stockCode) {
		return testFundsFlowRepository.findFundsFlowList(stockCode, null, null, null, null);
	}
	
	/**
	 * 查询某证券全部的订单信息（按照trade_date 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<EverySumPositionInfoPO>
	 */
	public List<OrderInfoPO> findAllOrderInfo (String stockCode) {
		return  testOrderInfoRepository.findOrderInfoList(stockCode, null, null, null, null);
	}
	
	/**
	 * 查询某证券每一笔持仓信息（按照trade_date 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<EverySumPositionInfoPO>
	 */
	public List<EverySumPositionInfoPO> findAllEverySumPositionInfo (String stockCode) {
		return testEverySumPositionInfoRepository.findEverySumPositionInfoList(stockCode, null, null, null, null, null, null);
	}
	
	// ------------- private method ---------------
	
	
	/**
	 * 在买入时的建仓策略。
	 * 
	 * @param systemOpenPoint 系统建仓点
	 * @param stockCode 证券代码
	 * @param openSignalDate 建仓信号发出时间 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeDate 交易日期 （格式：yyyyMMddhhmmssSSS）
	 * @param tradePrice 成交价格
	 * @param stopPrice 止损价格
	 * @return boolean true：可以建仓；false：不可以建仓
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
	private boolean 
	positionControlForBuy (
			DealSignal systemOpenPoint, String stockCode, Long openSignalDate, 
			Long tradeDate, BigDecimal tradePrice, BigDecimal stopPrice) 
	throws NumberFormatException, ParseException {
		
		List<EverySumPositionInfoPO> everySumPositionInfoList =                                 // 查询出每一笔未平仓的仓位信息。
			testEverySumPositionInfoRepository
			.findEverySumPositionInfoList(stockCode, null, null, null, "0", null, null);
		
		/*
		 * +-----------------------------------------------------------+
		 * + 未平仓信息查询                                                                                                                                                  +
		 * +-----------------------------------------------------------+
		 */
		EverySumPositionInfoPO firstOneBuy = null;                                              // 记录最早一笔未平仓的一买仓位。
		int oneBuyNums = 0;                                                                     // 记录未平仓一买仓位的数量。
		@SuppressWarnings("unused")
		EverySumPositionInfoPO firstFiboBuy = null;                                             // 记录最早一笔未平仓的斐波那契仓位。		
		int oneFiboNums = 0;                                                                    // 记录未平仓斐波那契仓位的数量。
		
		for (int i = 0; i < everySumPositionInfoList.size(); i++) {
			EverySumPositionInfoPO po = everySumPositionInfoList.get(i);
			
			if (po.getSystemOpenPoint().equalsIgnoreCase(systemOpenPoint.getType())) {
				// 记录尝试性仓位信息。
				if (systemOpenPoint == DealSignal.ONE_B) {
					if (oneBuyNums == 0) { firstOneBuy = po; }
					oneBuyNums++;
				}
				// 记录斐波那契仓位信息
				if (systemOpenPoint == DealSignal.FIBO_B) {
					if (oneFiboNums == 0) { firstFiboBuy = po; }
					oneFiboNums++;
				}		
			}
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 仓风控制1：在已持有三笔尝试性仓位情况下，如还需要再次建立尝试性仓位时，就平掉最早的那   +
		 * + 一笔尝试性仓位。斐波那契仓位采取最多只能建5笔仓位的策略。                                                           +
		 * +-----------------------------------------------------------+
		 * 
		 * 目的：截短尝试性建仓的亏损，提高资金利用率，让系统有一定的容错性；
		 */
		if (systemOpenPoint == DealSignal.ONE_B && oneBuyNums > 3) {
			insertSellInfo (
					DealSignal.SELL_FIVE_TENTH,
					stockCode,
					firstOneBuy.getOpenContractCode(), 
					openSignalDate,
					tradeDate,
					tradePrice);
			return true;
		}
		
		/* 由于我已经开发了斐波那契阶段性兑现的策略，这个部分就暂时可以不需要了。 
		if (systemOpenPoint == FractalDealSignalEnum.FIBO_B && oneFiboNums > 3) {
			tradeTime = new SimpleDateFormat("yyyyMMddHHmmss").parse(tradeDate + "093200").getTime();
			insertSellInfo (
					FractalDealSignalEnum.FIBO_STOP,
					stockCode,
					firstFiboBuy.getOpenContractCode(), 
					openSignalTime,
					tradeDate,
					tradeTime,
					tradePrice);
			return true;
		}
		*/
		if (systemOpenPoint == DealSignal.FIBO_B) {
			return true;
		}
		
		return false;
	}
}