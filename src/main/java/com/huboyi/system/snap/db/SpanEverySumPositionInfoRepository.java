package com.huboyi.system.snap.db;

import java.util.List;

import com.huboyi.system.po.EverySumPositionInfoPO;

/**
 * 每一笔持仓信息DAO。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/04/17
 * @version 1.0
 */
public interface SpanEverySumPositionInfoRepository {
	
	
	/**
	 * 插入每一笔的持仓信息。
	 * 
	 * @param po EverySumPositionInfoPO
	 */
	public void insert (EverySumPositionInfoPO po);
	
	/**
	 * 根据证券代码查询出每一笔持仓信息（按照open_date + open_time 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<EverySumPositionInfoPO>
	 */
	public List<EverySumPositionInfoPO> findByStockCode (String stockCode);
	
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
	public List<EverySumPositionInfoPO> findEverySumPositionInfoList (String stockCode, String openContractCode, Long beginOpenDate, Long endOpenDate, String isClose, Integer beginPage, Integer endPage);
	
	/**
	 * 修改每一笔的持仓信息。
	 * 
	 * @param id 记录id
	 * @param po EverySumPositionInfoPO
	 */
	public void updateById (String id, EverySumPositionInfoPO po);
	
	/**
	 * 删除每一笔持仓信息集合。
	 * 
	 * @param id 记录id
	 */
	public void deleteById (String id);
}