package com.huboyi.trader.service;

import java.util.List;

import com.huboyi.trader.entity.po.EntrustOrderPO;

/**
 * 委托单 Service。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public interface EntrustOrderService {
	
	/**
	 * 删除所有的委托单记录。
	 */
	public void deleteAllRecords();
	
	/**
	 * 删除委托单信息。
	 * 
	 * @param stockholder 股东代码
	 */
	public void deleteRecords(String stockholder);
	
	/**
	 * 查询所有的委托单信息（按照 tradeDate 升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<EntrustOrderPO>
	 */
	public List<EntrustOrderPO> findRecords(String stockholder);
}