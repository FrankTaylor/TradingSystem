package com.huboyi.trader.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.huboyi.trader.entity.po.EntrustOrderPO;

/**
 * 委托单 Dao。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
@Repository
public interface EntrustOrderDao {
	
	/**
	 * 插入一条委托单信息。
	 * 
	 * @param po EntrustOrderPO
	 */
	public void insert(EntrustOrderPO po);
	
	/**
	 * 删除所有的委托单信息。
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
	 * 找到最后的一条委托单信息（按照 entrustDate 降序）。
	 * 
	 * @param stockCode 股票代码
	 * @param stockholder 股东代码
	 * @return EntrustOrderPO
	 */
	public EntrustOrderPO findLastOne(String stockCode, String stockholder);
	
	/**
	 * 查询所有的委托单信息（按照 entrustDate 升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<EntrustOrderPO>
	 */
	public List<EntrustOrderPO> findAll(String stockCode, String stockholder);
}