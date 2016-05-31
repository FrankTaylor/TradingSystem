package com.huboyi.position.repository;

import java.util.List;

import com.huboyi.position.entity.po.OrderInfoPO;

/**
 * 订单信息Repository。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
public interface OrderInfoRepository {
	
	/**
	 * 插入一条订单信息。
	 * 
	 * @param po OrderInfoPO
	 */
	public void insert(OrderInfoPO po);
	
	/**
	 * 删除所有的订单信息。
	 */
	public void truncate();
	
	/**
	 * 删除持仓信息。
	 * 
	 * @param stockholder 股东代码
	 */
	public void delete(String stockholder);
	
	/**
	 * 找到最后的一条订单信息（按照 tradeDate 降序）。
	 * 
	 * @param stockholder 股东代码
	 * @return OrderInfoPO
	 */
	public OrderInfoPO findLastOne(String stockholder);
	
	/**
	 * 查询所有的订单信息（按照tradeDate升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<OrderInfoPO>
	 */
	public List<OrderInfoPO> findAll(String stockholder);
}