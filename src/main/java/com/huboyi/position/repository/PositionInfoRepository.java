package com.huboyi.position.repository;

import java.util.List;

import com.huboyi.position.entity.po.PositionInfoPO;

/**
 * 持仓信息DAO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public interface PositionInfoRepository {
	
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
	 * 查询全部的持仓信息。
	 * 
	 * @param stockholder 股东代码
	 * @return List<PositionInfoPO>
	 */
	public List<PositionInfoPO> findAll(String stockholder);
	
	/**
	 * 查询其中一条持仓信息记录。
	 * 
	 * @param stockholder 股东代码
	 * @param stockCode 证券代码
	 * @return PositionInfoPO
	 */
	public PositionInfoPO findOne(String stockholder, String stockCode);
}