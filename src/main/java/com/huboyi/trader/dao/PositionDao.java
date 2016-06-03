package com.huboyi.trader.repository;

import java.util.List;

import com.huboyi.trader.entity.po.PositionPO;
import com.huboyi.trader.service.PositionInfoService.SortType;

/**
 * 持仓信息Repository。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.2
 */
public interface PositionDao {
	
	/**
	 * 插入持仓信息。
	 * 
	 * @param po PositionPO
	 */
	public void insert(PositionPO po);
	
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
	 * @param po PositionPO
	 */
	public void update(PositionPO po);
	
	/**
	 * 查询其中一条持仓信息记录。
	 * 
	 * @param stockholder 股东代码
	 * @param stockCode 证券代码
	 * @return PositionPO
	 */
	public PositionPO findOne(String stockholder, String stockCode);
	
	/**
	 * 查询全部的持仓信息。
	 * 
	 * @param stockholder 股东代码
	 * @param sortType SortType
	 * @return List<PositionPO>
	 */
	public List<PositionPO> findAll(String stockholder, SortType sortType);
}