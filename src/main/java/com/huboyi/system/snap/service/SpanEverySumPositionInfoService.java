package com.huboyi.system.snap.service;

import java.math.BigDecimal;
import java.util.List;

import com.huboyi.system.module.fractal.signal.bean.FractalPositionInfoBean;

/**
 * 每一笔持仓信息Service。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/04/17
 * @version 1.0
 */
public interface SpanEverySumPositionInfoService {
	
	/**
	 * 插入每一笔的持仓信息。
	 * 
	 * @param systemName 系统名称
	 * @param stockCode 证券代码
	 * @param stockName 证券名称
	 * @param systemOpenPoint 系统建仓点类型（买入信号类型）
	 * @param systemOpenName 系统建仓点名称（买入信号名称）
	 * @param openSignalTime 建仓信号发出时间
	 * @param openDate 建仓日期（格式：%Y%m%d）
	 * @param openTime 建仓时间（详细时间）
	 * @param openPrice 建仓价格
	 * @param openNumber 建仓数量
	 * @param stopPrice 止损价格
	 * @param stockholder 股东代码
	 */
	public void insert (
			String systemName, String stockCode, String stockName, 
			String systemOpenPoint, String systemOpenName, Long openSignalTime, 
			Integer openDate, Long openTime, BigDecimal openPrice, 
			Long openNumber, BigDecimal stopPrice, String stockholder);
	
	/**
	 * 根据证券代码查询出每一笔持仓信息（按照open_date + open_time 升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<FractalPositionInfoBean>
	 */
	public List<FractalPositionInfoBean> findAllPositionInfoByStockCode (String stockCode);
	
	/**
	 * 每天收盘时刷新每笔仓位信息。
	 * 
	 * @param stockCode 证券代码
	 * @param newPrice 当前价格
	 */
	public void flushEverySumPositionInfoInClosing (String stockCode, BigDecimal newPrice);
	
}