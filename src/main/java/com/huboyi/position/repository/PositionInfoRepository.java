package com.huboyi.position.repository;

import java.util.List;

import com.huboyi.position.entity.po.PositionInfoPO;

/**
 * 持仓信息Repository。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
public interface PositionInfoRepository {
	
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
	
	/**
	 * 插入持仓信息。
	 * 
	 * @param po PositionInfoPO
	 */
	public void insert(PositionInfoPO po);
	
	/**
	 * 删除所有的持仓。
	 */
	public void truncate();
	
	/**
	 * 删除持仓信息。
	 * 
	 * @param stockholder 股东代码
	 */
	public void delete(String stockholder);
	
	/**
	 * 删除持仓信息。
	 * 
	 * @param stockholder 股东代码
	 * @param stockCode 证券代码
	 */
	public void delete(String stockholder, String stockCode);
	
	/**
	 * 修改持仓信息。
	 * 
	 * @param po PositionInfoPO
	 */
	public void update(PositionInfoPO po);
	
	/**
	 * 查询其中一条持仓信息记录。
	 * 
	 * @param stockholder 股东代码
	 * @param stockCode 证券代码
	 * @return PositionInfoPO
	 */
	public PositionInfoPO findOne(String stockholder, String stockCode);
	
	/**
	 * 查询全部的持仓信息。
	 * 
	 * @param stockholder 股东代码
	 * @param sortType SortType
	 * @return List<PositionInfoPO>
	 */
	public List<PositionInfoPO> findAll(String stockholder, SortType sortType);
}