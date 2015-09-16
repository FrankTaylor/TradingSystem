package com.huboyi.system.snap.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.huboyi.engine.DealFeeCalculator;
import com.huboyi.system.bean.PositionInfoBean;
import com.huboyi.system.po.EverySumPositionInfoPO;
import com.huboyi.system.snap.db.SpanEverySumPositionInfoRepository;
import com.huboyi.system.snap.db.mysql.repository.impl.SpanEverySumPositionInfoRepositoryImpl;
import com.huboyi.system.snap.service.SpanEverySumPositionInfoService;

/**
 * 每一笔持仓信息Service实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/04/17
 * @version 1.0
 */
@Service
public class SpanEverySumPositionInfoServiceImpl implements SpanEverySumPositionInfoService {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(SpanEverySumPositionInfoRepositoryImpl.class);
	
	/** 交易费用计算类。*/
	@Resource
	private DealFeeCalculator dealFeeCalculator;
	
	@Resource
	/** 每一笔持仓信息DAO。*/
	private SpanEverySumPositionInfoRepository spanEverySumPositionInfoRepository;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = {RuntimeException.class})
	public void insert (String systemName, String stockCode, String stockName,
			String systemOpenPoint, String systemOpenName, Long openSignalTime,
			Integer openDate, Long openTime, BigDecimal openPrice,
			Long openNumber, BigDecimal stopPrice, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke insert method").append("\n");
		logMsg.append("@param [systemName = " + systemName + "]");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		logMsg.append("@param [stockName = " + stockName + "]");
		logMsg.append("@param [systemOpenPoint = " + systemOpenPoint + "]");
		logMsg.append("@param [systemOpenName = " + systemOpenName + "]");
		logMsg.append("@param [openSignalTime = " + openSignalTime + "]");
		logMsg.append("@param [openDate = " + openDate + "]");
		logMsg.append("@param [openTime = " + openTime + "]");
		logMsg.append("@param [openPrice = " + openPrice + "]");
		logMsg.append("@param [openNumber = " + openNumber + "]");
		logMsg.append("@param [stopPrice = " + stopPrice + "]");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());
		
		try {
			
			EverySumPositionInfoPO po = new EverySumPositionInfoPO();
			
			/* id。*/
			po.setId(UUID.randomUUID().toString());
			
			// --- 
			
			/* 系统名称。*/
			po.setSystemName(systemName);
			/* 证券代码。*/
			po.setStockCode(stockCode);
			/* 证券名称。*/
			po.setStockName(stockName);

			// --- 
			/* 建仓合同编号。 */
			po.setOpenContractCode(UUID.randomUUID().toString());
			/* 系统建仓点。*/
			po.setSystemOpenPoint(systemOpenPoint);
			/* 系统建仓点名称。*/
			po.setSystemOpenName(systemOpenName);
			/* 建仓信号发出时间。*/
			po.setOpenSignalTime(openSignalTime);
			/* 成交日期（格式：%Y%m%d）。 */
			po.setOpenDate(openDate);
			/* 成交时间（格式：HH:mm:ss）。 */
			po.setOpenTime(openTime);
			/* 建仓价格。 */
			po.setOpenPrice(openPrice.setScale(3, RoundingMode.HALF_UP));
			/* 建仓数量。 */
			po.setOpenNumber(openNumber);
			
			BigDecimal amountMoney = openPrice.multiply(new BigDecimal(openNumber));              // 计算买入或卖出的发生金额。
			
			BigDecimal charges = dealFeeCalculator.calcCharges(amountMoney);                      // 计算手续费（目前手续费双向收取）。
			BigDecimal stampDuty = new BigDecimal(0);                                             // 计算印花税（目前只有在卖出时收取）。
			BigDecimal transferFee = dealFeeCalculator.calcTransferFee(stockCode, openNumber);    // 计算过户费（目前过户费只有在上交所双向收取，深交所不收）。
			BigDecimal clearingFee = new BigDecimal(0);                                           // 结算费（不购买B股不用计算）。
			BigDecimal totleFee = charges.add(stampDuty).add(transferFee).add(clearingFee);       // 计算总共的费用。
			
			BigDecimal openCost = amountMoney.add(totleFee);                                      // 计算建仓成本。买入时：成交金额 ==（交易总金额 + 手续费 + 印花税 + 过户费 + 结算费）。
			
			/* 建仓成本。 */
			po.setOpenCost(openCost.setScale(3, RoundingMode.HALF_UP));
			
			// ---
			/* 可平仓数量。*/
			po.setCanCloseNumber(0L);
			/* 止损价格。*/
			po.setStopPrice(stopPrice.setScale(3, RoundingMode.HALF_UP));
			
			// --- 
			/* 平仓合同编号。*/
			po.setCloseContractCode("no");
			/* 系统平仓点类型（卖出信号类型）。*/
			po.setSystemClosePoint(null);
			/* 系统平仓点名称（卖出信号名称）。*/
			po.setSystemCloseName(null);
			/* 平仓信号发出时间。*/
			po.setCloseSignalTime(null);
			/* 平仓日期（格式：%Y%m%d）。*/
			po.setCloseDate(null);
			/* 平仓时间（详细时间）。*/
			po.setCloseTime(null);
			/* 平仓价格。*/
			po.setClosePrice(null);
			/* 平仓数量。*/
			po.setCloseNumber(null);
			
			// ---
			BigDecimal newMarketValue = amountMoney;                                              // 最新市值。公式 = 证券价格 * 证券数量 
			BigDecimal floatProfitAndLoss = newMarketValue.subtract(openCost);                    // 浮动盈亏。公式 = 最新市值 * 建仓成本 
			BigDecimal profitAndLossRatio =                                                       // 盈亏比例。公式 = 浮动盈亏 / 建仓成本
				floatProfitAndLoss.divide(openCost, 3, RoundingMode.HALF_UP);
			
			/* 当前价。 */
			po.setNewPrice(openPrice.setScale(3, RoundingMode.HALF_UP));
			/* 最新市值。 */
			po.setNewMarketValue(newMarketValue.setScale(3, RoundingMode.HALF_UP));
			/* 浮动盈亏。 */
			po.setFloatProfitAndLoss(floatProfitAndLoss.setScale(3, RoundingMode.HALF_UP));
			/* 盈亏比例。 */
			po.setProfitAndLossRatio(profitAndLossRatio.setScale(3, RoundingMode.HALF_UP));
			
			// --- 
			/* 股东代码。*/
			po.setStockholder(stockholder);
			
			spanEverySumPositionInfoRepository.insert(po);
			
			log.info("插入 [证券代码 = " + stockCode + "] 的每一笔持仓记录成功。");
		} catch (Throwable e) {
			String errorMsg = "插入 [证券代码 = " + stockCode + "] 的每一笔持仓记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
		
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = true, rollbackFor = {RuntimeException.class})
	public List<PositionInfoBean> findAllPositionInfoByStockCode (String stockCode) {
		List<PositionInfoBean> positionInfoList = new ArrayList<PositionInfoBean>();                 // 装载每一笔仓位信息的集合。
		
		List<EverySumPositionInfoPO> everySumPositionInfoList =                                      // 查询出每一笔未平仓的仓位信息。   
			spanEverySumPositionInfoRepository.findByStockCode(stockCode);
		
		for (EverySumPositionInfoPO source : everySumPositionInfoList) {
			PositionInfoBean target = new PositionInfoBean();
			BeanUtils.copyProperties(source, target);
			positionInfoList.add(target);
		}
		
		return positionInfoList;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ, readOnly = false, rollbackFor = {RuntimeException.class})
	public void flushEverySumPositionInfoInClosing (String stockCode, BigDecimal newPrice) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke flushEverySumPositionInfoInClosing method").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]\n");
		logMsg.append("@param [newPrice = " + newPrice + "]\n");
		log.info(logMsg.toString());
		
		List<EverySumPositionInfoPO> everySumPositionInfoList =                                      // 查询出每一笔未平仓的仓位信息。
			spanEverySumPositionInfoRepository
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

			/* 平仓合同编号。*/
			updatePo.setCloseContractCode(null);
			/* 股东代码。*/
			updatePo.setStockholder(null);
			
			// --- 实际需要修改的部分 ---
			
			/* 可平仓数量。*/
			updatePo.setCanCloseNumber(po.getOpenNumber());
			/* 当前价。 */
			updatePo.setNewPrice(newPrice);
			
			BigDecimal newMarketValue = newPrice.multiply(new BigDecimal(po.getCanCloseNumber()));   // 最新市值。公式 = 证券价格 * 证券数量 
			BigDecimal floatProfitAndLoss = newMarketValue.subtract(po.getOpenCost());               // 浮动盈亏。公式 = 最新市值 * 建仓成本 
			BigDecimal profitAndLossRatio = 
				floatProfitAndLoss.divide(po.getOpenCost(), 3, RoundingMode.HALF_UP);                // 盈亏比例。公式 = 浮动盈亏 / 建仓成本
			
			/* 最新市值。 */
			updatePo.setNewMarketValue(newMarketValue.setScale(3, RoundingMode.HALF_UP));
			/* 浮动盈亏。 */
			updatePo.setFloatProfitAndLoss(floatProfitAndLoss.setScale(3, RoundingMode.HALF_UP));
			/* 盈亏比例。 */
			updatePo.setProfitAndLossRatio(profitAndLossRatio.setScale(3, RoundingMode.HALF_UP));
			
			spanEverySumPositionInfoRepository.updateById(po.getId(), updatePo);
		}
	}
}