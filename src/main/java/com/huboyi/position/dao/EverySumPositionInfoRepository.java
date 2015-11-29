package com.huboyi.position.dao;

import java.util.List;

import com.huboyi.position.po.EverySumPositionInfoPO;

/**
 * 每一笔持仓信息DAO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public interface EverySumPositionInfoRepository {
	
	/**
	 * 创建每一笔的持仓索引（创建 open_date + open_time 的升序复合索引）。
	 * 
	 * @param stockCode 证券代码
	 */
	public void createIndex(String stockCode);
	
	/**
	 * 插入每一笔的持仓信息。
	 * 
	 * @param po EverySumPositionInfoPO
	 */
	public void insert(EverySumPositionInfoPO po);
	
	/**
	 * 找到最新的一条订单（按照open_date + open_time 降序）。
	 * 
	 * @param stockCode 证券代码
	 * @return EverySumPositionInfoPO
	 */
	public EverySumPositionInfoPO findNewOne(String stockCode);
	
	/**
	 * 按照条件查询每一个持仓记录（按照open_date + open_time 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @param openContractCode 建仓合同编号
	 * @param beginOpenDate 建仓日期
	 * @param endOpenDate 建仓时间
	 * @param isClose 是否已平仓 0:未平仓，1:已平仓
	 * @param beginPage 开始页
	 * @param endLimit 结束页
	 * @return List<EverySumPositionInfoPO>
	 */
	public List<EverySumPositionInfoPO> findEverySumPositionInfoList(String stockCode, String openContractCode, Integer beginOpenDate, Integer endOpenDate, String isClose, Integer beginPage, Integer endPage);
	
	/**
	 * 修改每一笔的持仓信息。
	 * 
	 * @param po EverySumPositionInfoPO
	 */
	public void update(EverySumPositionInfoPO po);
	
	/**
	 * 删除每一笔持仓信息集合。
	 * 
	 * @param stockCode 证券代码
	 */
	public void dropCollection(String stockCode);
}