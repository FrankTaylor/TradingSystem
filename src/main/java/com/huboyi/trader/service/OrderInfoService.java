package com.huboyi.trader.service;

import java.util.List;

import com.huboyi.trader.entity.po.OrderInfoPO;

/**
 * 订单信息Service。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.2
 */
public interface OrderInfoService {
	
	/**
	 * 删除所有的订单信息记录。
	 */
	public void deleteAllRecords();
	
	/**
	 * 删除订单信息。
	 * 
	 * @param stockholder 股东代码
	 */
	public void deleteRecords(String stockholder);
	
	/**
	 * 查询所有的订单信息（按照tradeDate升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<OrderInfoPO>
	 */
	public List<OrderInfoPO> findRecords(String stockholder);
}