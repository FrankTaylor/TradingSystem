package com.huboyi.system;

import java.util.List;

import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.bean.DealSignalBean;
import com.huboyi.system.bean.IndicatorsInfoBean;
import com.huboyi.system.bean.PositionInfoBean;

/**
 * 捕捉交易信号接口类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/12
 * @version 1.0
 */
public interface SnapDealSignal {
	
	/**
	 * 捕捉买入开仓信号。
	 * 
	 * @param stockCode 股票代码
	 * @param stockDataList 股票数据集合
	 * @param indicatorsInfo 指标信息
	 * @param positionInfoList 仓位信息集合
	 * @return DealSignalBean
	 */
	public DealSignalBean 
	snapBuyToOpenSignal(
			String stockCode, 
			List<StockDataBean> stockDataList, 
			IndicatorsInfoBean indicatorsInfo, 
			List<PositionInfoBean> positionInfoList);
	
	/**
	 * 捕捉卖出平仓信号。
	 * 
	 * @param stockCode 股票代码
	 * @param stockDataList 股票数据集合
	 * @param indicatorsInfo 指标信息
	 * @param positionInfoList 仓位信息集合
	 * @return DealSignalBean
	 */
	public DealSignalBean 
	snapSellToCloseSignal(
			String stockCode, 
			List<StockDataBean> stockDataList, 
			IndicatorsInfoBean indicatorsInfo, 
			List<PositionInfoBean> positionInfoList);
}