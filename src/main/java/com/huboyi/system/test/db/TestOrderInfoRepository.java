package com.huboyi.system.test.db;

import java.util.List;

import com.huboyi.system.po.OrderInfoPO;

/**
 * 订单信息DAO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/12/30
 * @version 1.0
 */
public interface TestOrderInfoRepository {
	
	/**
	 * 创建订单信息索引（创建 trade_date + tradetime 的升序复合索引）。
	 * 
	 * @param stockCode 证券代码
	 */
	public void createIndex (String stockCode);
	
	/**
	 * 插入订单信息。
	 * 
	 * @param po OrderInfoPO
	 */
	public void insert (OrderInfoPO po);
	
	/**
	 * 找到最新的一条订单（按照trade_date + trade_time 降序）。
	 * 
	 * @param stockCode 证券代码
	 * @return OrderInfoPO
	 */
	public OrderInfoPO findNewOne (String stockCode);
	
	/**
	 * 按照条件查询订单记录（按照trade_date + trade_time 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @param beginTradeDate 开始成交日期
	 * @param endTradeDate 结束成交日期
	 * @param beginPage 开始页
	 * @param endLimit 结束页
	 * @return List<OrderInfoPO>
	 */
	public List<OrderInfoPO> findOrderInfoList (String stockCode, Integer beginTradeDate, Integer endTradeDate, Integer beginPage, Integer endPage);
	
	/**
	 * 删除订单信息集合。
	 * 
	 * @param stockCode 证券代码
	 */
	public void dropCollection (String stockCode);
}