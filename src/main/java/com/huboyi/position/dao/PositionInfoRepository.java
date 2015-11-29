package com.huboyi.position.dao;

import java.util.List;

import com.huboyi.position.po.PositionInfoPO;

/**
 * 持仓信息DAO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public interface PositionInfoRepository {

	/**
	 * 创建持仓信息索引。
	 * 
	 * @param stockCode 证券代码
	 */
	public void createIndex(String stockCode);
	
	/**
	 * 插入持仓信息。
	 * 
	 * @param po PositionInfoPO
	 */
	public void insert(PositionInfoPO po);
	
	/**
	 * 查询全部的持仓信息记录。
	 * 
	 * @return List<PositionInfoPO>
	 */
	public List<PositionInfoPO> findAll();
	
	/**
	 * 修改持仓信息记录。
	 * 
	 * @param po PositionInfoPO
	 */
	public void update(PositionInfoPO po);
	
	/**
	 * 根据证券代码删除持仓信息记录。
	 * 
	 * @param stockCode 证券代码
	 */
	public void removeByStockCode(String stockCode);
	
	/**
	 * 删除没有仓位的持仓信息记录。
	 */
	public void removeForNoStockNumber();
	
	/**
	 * 删除持仓信息集合。
	 */
	public void dropCollection();
}