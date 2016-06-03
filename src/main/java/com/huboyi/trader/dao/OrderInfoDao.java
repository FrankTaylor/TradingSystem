package com.huboyi.trader.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * 订单信息Repository。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
@Repository
public interface OrderInfoDao {
	
	/**
	 * 插入一条订单信息。
	 * 
	 * @param po OrderPO
	 */
	public void insert(OrderPO po);
	
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
	 * @return OrderPO
	 */
	public OrderPO findLastOne(String stockholder);
	
	/**
	 * 查询所有的订单信息（按照tradeDate升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<OrderPO>
	 */
	public List<OrderPO> findAll(String stockholder);
}