package com.huboyi.trader.service;

import java.util.List;

import com.huboyi.trader.entity.po.DealOrderPO;

/**
 * 交易单 Service。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.2
 */
public interface DealOrderService {
	
	/**
	 * 删除所有的交易单记录。
	 */
	public void deleteAllRecords();
	
	/**
	 * 删除交易单信息。
	 * 
	 * @param stockholder 股东代码
	 */
	public void deleteRecords(String stockholder);
	
	/**
	 * 查询所有的交易单信息（按照 tradeDate 升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<DealOrderPO>
	 */
	public List<DealOrderPO> findRecords(String stockholder);
}