package com.huboyi.system.test.db;

import java.util.List;

import com.huboyi.system.po.FundsFlowPO;

/**
 * 资金流水DAO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/12/29
 * @version 1.0
 */
public interface TestFundsFlowRepository {
	
	/**
	 * 创建资金流水索引（创建 trade_date + tradetime 的升序复合索引）。
	 * 
	 * @param stockCode 证券代码
	 */
	public void createIndex (String stockCode);
	
	/**
	 * 插入资金流水。
	 * 
	 * @param po FundsFlowPO
	 */
	public void insert (FundsFlowPO po);
	
	/**
	 * 为了找到剩余资金，需要找到最新的一条资金流水（按照trade_date + trade_time 降序）。
	 * 
	 * @param stockCode 证券代码
	 * @return FundsFlowPO
	 */
	public FundsFlowPO findNewOne (String stockCode);
	
	/**
	 * 按照条件查询资金流水记录（按照trade_date + tradetime 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @param beginTradeDate 开始成交日期
	 * @param endTradeDate 结束成交日期
	 * @param beginPage 开始页
	 * @param endLimit 结束页
	 * @return List<FundsFlowPO>
	 */
	public List<FundsFlowPO> findFundsFlowList (String stockCode, Integer beginTradeDate, Integer endTradeDate, Integer beginPage, Integer endPage);
	
	/**
	 * 删除资金流水信息集合。
	 * 
	 * @param stockCode 证券代码
	 */
	public void dropCollection (String stockCode);
}