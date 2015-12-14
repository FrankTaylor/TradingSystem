package com.huboyi.position.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.huboyi.position.DealFeeCalculator;
import com.huboyi.position.entity.po.FundsFlowPO;
import com.huboyi.position.entity.po.FundsFlowPO.Business;
import com.huboyi.position.entity.po.OrderInfoPO;
import com.huboyi.position.entity.po.OrderInfoPO.Trade;
import com.huboyi.position.entity.po.PositionInfoPO;
import com.huboyi.position.repository.FundsFlowRepository;
import com.huboyi.position.repository.OrderInfoRepository;
import com.huboyi.position.repository.PositionInfoRepository;
import com.huboyi.position.service.PositionInfoService;

/**
 * 仓位信息Service实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
public class PositionInfoServiceImpl implements PositionInfoService {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(PositionInfoServiceImpl.class);
	
	/** 日期格式处理类。*/
	private final DateFormat dataFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	
	@Autowired
	@Qualifier("fundsFlowRepository")
	private FundsFlowRepository fundsFlowRepository;
	
	@Autowired
	@Qualifier("orderInfoRepository")
	private OrderInfoRepository orderInfoRepository;
	
	@Autowired
	@Qualifier("positionInfoRepository")
	private PositionInfoRepository positionInfoRepository;
	
	@Autowired
	@Qualifier("dealFeeCalculator")
	private DealFeeCalculator dealFeeCalculator;
	
	@Override
	public void buyToOpen(String stockCode, Long tradeDate, Long tradeNumber, BigDecimal tradePrice, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用  [买入开仓] 方法").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]\n");
		logMsg.append("@param [tradeDate = " + tradeDate + "]\n");
		logMsg.append("@param [tradeNumber = " + tradeNumber + "]\n");
		logMsg.append("@param [tradePrice = " + tradePrice + "]\n");
		logMsg.append("@param [stockholder = " + stockholder + "]\n");
		log.info(logMsg.toString());
		
		FundsFlowPO lastFundsFlow = fundsFlowRepository.findLastOne(stockholder);               // 为了找到剩余资金，需要找到最新的一条资金流水。
		if (lastFundsFlow == null) {
			log.error("该证券用户没有任何资金流水记录！[股东代码 = " + stockholder +"]");
	    	return;
		}
		
		BigDecimal lastFundsBalance = lastFundsFlow.getFundsBalance();                                                 // 得到最新的资金余额。
		if (lastFundsBalance == null || lastFundsBalance.doubleValue() == 0) {
	    	log.error("资金余额不足，不能再继续购买证券！[资金余额 = " + lastFundsBalance +"]");
	    	return;
	    }
		
		
		BigDecimal amountMoney = tradePrice.multiply(new BigDecimal(tradeNumber));                                     // 计算买入证券的理论金额。
		
		BigDecimal charges = dealFeeCalculator.calcCharges(amountMoney);                                               // 计算手续费（目前手续费双向收取）。
		BigDecimal stampDuty = new BigDecimal(0);                                                                      // 计算印花税（目前只有在卖出时收取）。
		BigDecimal transferFee = dealFeeCalculator.calcTransferFee(stockCode, tradeNumber);                            // 计算过户费（目前过户费只有在上交所双向收取，深交所不收）。
		BigDecimal clearingFee = new BigDecimal(0);                                                                    // 结算费（不购买B股不用计算）。
		BigDecimal totleFee = charges.add(stampDuty).add(transferFee).add(clearingFee);                                // 计算总共的费用。
		
		BigDecimal tradeMoney = amountMoney.add(totleFee);                                                             // 计算成交金额。买入时：成交金额 ==（交易总金额 + 手续费 + 印花税 + 过户费 + 结算费）。
		if (lastFundsBalance.compareTo(tradeMoney) == -1) {
			log.error("资金余额不足，不能再继续购买证券！[资金余额 = "+ lastFundsBalance +"] | [成交金额 = "+ tradeMoney +"]");
    		return;
    	}
		BigDecimal fundsBalance = lastFundsBalance.subtract(tradeMoney);                                               // 计算购买后的资金余额。
		
		// --- 构造在买入证券过程中都需要用到的数据 ---
    	String contractCode = UUID.randomUUID().toString();                                                            // 合同ID。
    	
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
		fundsFlowPO.setTradeDate(tradeDate);
		/* 成交价格。 */
		fundsFlowPO.setTradePrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
		/* 成交数量。 */
		fundsFlowPO.setTradeNumber(tradeNumber);
		/* 成交金额。 */
		fundsFlowPO.setTradeMoney(tradeMoney.setScale(3, RoundingMode.HALF_UP));
		/* 资金余额。 */
		fundsFlowPO.setFundsBalance(fundsBalance);
		
		// --- 
		/* 业务类型（在数据库中实际记录的值，主要用于查询）。 */
		fundsFlowPO.setBusinessType(Business.STOCK_BUY.getType());
		
		// --- 
		/* 手续费。 */
		fundsFlowPO.setCharges(charges);
		/* 印花税。 */
		fundsFlowPO.setStampDuty(stampDuty);
		/* 过户费。 */
		fundsFlowPO.setTransferFee(transferFee);
		/* 结算费。 */
		fundsFlowPO.setClearingFee(clearingFee);
		
		// --- 
		/* 股东代码。*/
		fundsFlowPO.setStockholder(stockholder);
		
		/*
		 * +-----------------------------------------------------------+
		 * + 构造订单信息 。                                                                                                                                                 +
		 * +-----------------------------------------------------------+
		 */
		OrderInfoPO orderInfoPO = new OrderInfoPO();
		/* 合同编号。 */
		orderInfoPO.setContractCode(contractCode);
		
		// --- 
		/* 证券代码。 */
		orderInfoPO.setStockCode(stockCode);
		/* 证券名称。 */
		orderInfoPO.setStockName(null);
		/* 成交日期（格式：yyyyMMddhhmmssSSS）。 */
		orderInfoPO.setTradeDate(tradeDate);
		/* 买卖类型（在数据库中实际记录的值，主要用于查询）。*/
		orderInfoPO.setTradeType(Trade.STOCK_BUY.getType());
		/* 成交价格。 */
		orderInfoPO.setTradePrice(tradePrice.setScale(3, RoundingMode.HALF_UP));
		/* 成交数量。 */
		orderInfoPO.setTradeNumber(tradeNumber);
		/* 成交金额。 */
		orderInfoPO.setTradeMoney(amountMoney.setScale(3, RoundingMode.HALF_UP));
		
		// --- 
		/* 股东代码。*/
		orderInfoPO.setStockholder(stockholder);
		
		/*
		 * +-----------------------------------------------------------+
		 * + 构造持仓信息 。                                                                                                                                             +
		 * +-----------------------------------------------------------+
		 */
	}

	@Override
	public void sellToClose(String stockCode, Long tradeDate, Long tradeNumber, BigDecimal tradePrice, String stockholder) {
		
	}

	@Override
	public void updateProfitAndLoss(String stockCode, Long date, BigDecimal close) {
		
	}

	@Override
	public void deleteAllRecords() {
		positionInfoRepository.truncate();
	}

	@Override
	public void deleteRecords(String stockholder) {
		positionInfoRepository.delete(stockholder);
	}
	
	@Override
	public PositionInfoPO findRecord(String stockholder, String stockCode) {
		return positionInfoRepository.findOne(stockholder, stockCode);
	}
	
	@Override
	public List<PositionInfoPO> findRecords(String stockholder) {
		return findRecords(stockholder, SortType.PROFIT_AND_LOSS_RATIO_DESC);
	}
	
	@Override
	public List<PositionInfoPO> findRecords(String stockholder, SortType sortType) {
		if (sortType == null) {
			sortType = SortType.PROFIT_AND_LOSS_RATIO_DESC;
		}
		return positionInfoRepository.findAll(stockholder, sortType);
	}
	
}