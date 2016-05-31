package com.huboyi.trader.service;

import java.math.BigDecimal;
import java.util.List;

import com.huboyi.trader.entity.po.PositionInfoPO;

/**
 * 仓位信息Service。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.2
 */
public interface PositionInfoService {
	
	/**
	 * 买入开仓。
	 * 执行顺序： 1、增加一条资金流水记录；
	 *        2、增加一条买入订单信息；
	 *        3、修改持仓信息。
	 * 
	 * @param stockCode 证券代码
	 * @param tradeDate 交易日期 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeNumber 成交数量
	 * @param tradePrice 成交价格
	 * @param stockholder 股东代码
	 */
	public void 
	buyToOpen(String stockCode, Long tradeDate, Long tradeNumber, BigDecimal tradePrice, String stockholder);

	/**
	 * 卖出平仓。
	 * 执行顺序： 1、修改每一笔持仓记录；
	 *        2、增加一条卖出订单信息；
	 *        3、增加一条资金流水记录。
	 * 
	 * @param stockCode 证券代码
	 * @param tradeDate 交易日期 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeNumber 成交数量
	 * @param tradePrice 成交价格
	 * @param stockholder 股东代码
	 */
	public void 
	sellToClose(String stockCode, Long tradeDate, Long tradeNumber, BigDecimal tradePrice, String stockholder);
	
	/**
	 * 更新仓位的盈亏。
	 * 
	 * @param stockCode 证券代码
	 * @param date 收盘日期 （格式：yyyyMMddhhmmssSSS）
	 * @param close 收盘价格
	 */
	public void updateProfitAndLoss(String stockCode, Long date, BigDecimal close);
	
	/**
	 * 删除所有的仓位记录。
	 */
	public void deleteAllRecords();
	
	/**
	 * 删除仓位录。
	 * 
	 * @param stockholder 股东代码
	 */
	public void deleteRecords(String stockholder);
	
	/**
	 * 查询某一笔持仓记录。
	 * 
	 * @param stockholder 股东代码
	 * @param stockCode 证券代码
	 * @return PositionInfoPO
	 */
	public PositionInfoPO findRecord(String stockholder, String stockCode) ;
	
	/**
	 * 查询全部持仓记录（默认按照profitAndLossRatio降序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<PositionInfoPO>
	 */
	public List<PositionInfoPO> findRecords(String stockholder) ;
	
	/**
	 * 查询全部持仓记录。
	 * 
	 * @param stockholder 股东代码
	 * @param sortType SortType
	 * @return List<PositionInfoPO>
	 */
	public List<PositionInfoPO> findRecords(String stockholder, SortType sortType) ;
	
	/** 仓位信息排序枚举类。*/
	public enum SortType {
		/** 按可卖数量升序排列。*/
		CAN_SELL_NUMBER_ASC,
		/** 按可卖数量降序排列。*/
		CAN_SELL_NUMBER_DESC,
		
		/** 按成本金额升序排列。*/
		COST_MONEY_ASC,
		/** 按成本金额降序排列。*/
		COST_MONEY_DESC,

		/** 按最新市值升序排列。*/
		NEW_MARKET_VALUE_ASC,
		/** 按最新市值降序排列。*/
		NEW_MARKET_VALUE_DESC,
		
		/** 按浮动盈亏升序排列。*/
		FLOAT_PROFIT_AND_LOSS_ASC,
		/** 按浮动盈亏降序排列。*/
		FLOAT_PROFIT_AND_LOSS_DESC,
		
		/** 按盈亏比例升序排列。*/
		PROFIT_AND_LOSS_RATIO_ASC,
		/** 按盈亏比例降序排列。*/
		PROFIT_AND_LOSS_RATIO_DESC;
		
		private SortType () {}
	}
}