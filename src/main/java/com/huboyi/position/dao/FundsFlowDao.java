package com.huboyi.position.dao;

import java.util.List;

import com.huboyi.position.entity.po.FundsFlowPO;

/**
 * 资金流水DAO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
public interface FundsFlowDao {
	
	/**
	 * 插入一条资金流水记录。
	 * 
	 * @param po FundsFlowPO
	 */
	public void insert(FundsFlowPO po);
	
	/**
	 * 删除所有的资金流水记录。
	 */
	public void truncate();

	/**
	 * 找到最后的一条资金流水记录（按照 tradeDate 降序）。
	 * 
	 * @param stockholder 股东代码
	 * @return FundsFlowPO
	 */
	public FundsFlowPO findLastOne(String stockholder);
	
	/**
	 * 查询所有的资金流水记录（按照tradeDate升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<FundsFlowPO>
	 */
	public List<FundsFlowPO> findAll(String stockholder);
}