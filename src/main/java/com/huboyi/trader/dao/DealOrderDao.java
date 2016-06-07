package com.huboyi.trader.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.huboyi.trader.entity.po.DealOrderPO;

/**
 * 交易单 Dao。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
@Repository
public interface DealOrderDao {
	
	/**
	 * 插入一条交易单信息。
	 * 
	 * @param po DealOrderPO
	 */
	public void insert(DealOrderPO po);
	
	/**
	 * 删除所有的交易单信息。
	 */
	public void truncate();
	
	/**
	 * 删除持仓信息。
	 * 
	 * @param stockCode 股票代码
	 * @param stockholder 股东代码
	 */
	public void delete(String stockCode, String stockholder);
	
	/**
	 * 找到最后的一条交易单信息（按照 tradeDate 降序）。
	 * 
	 * @param stockCode 股票代码
	 * @param stockholder 股东代码
	 * @return DealOrderPO
	 */
	public DealOrderPO findLastOne(String stockCode, String stockholder);
	
	/**
	 * 查询所有的交易单信息（按照 tradeDate 升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<DealOrderPO>
	 */
	public List<DealOrderPO> findAll(String stockCode, String stockholder);
}