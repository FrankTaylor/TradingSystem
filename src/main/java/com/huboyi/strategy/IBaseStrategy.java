package com.huboyi.strategy;

import java.util.List;

import com.huboyi.data.entity.StockDataBean;
import com.huboyi.trader.entity.po.DealOrderPO;
import com.huboyi.trader.entity.po.EntrustOrderPO;

/**
 * 策略实现类的基础接口。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public interface IBaseStrategy {
	
	// ------ processStockDataBean
	
	/**
	 * K 线预处理函数。
	 * 
	 * <b>注意</b>：此方法的调用发生在 processStockData() 之前。
	 * 
	 * @param stockData 触发此次调用的 K 线
	 * @param stockDataList 此次 K 线所对应的K线序列(stockDataList.get(0) 与 stockData 是等价的)
	 * @return boolean
	 */
	public boolean preProcessStockData(StockDataBean stockData, List<StockDataBean> stockDataList) ;
	
	/**
	 * K 线处理函数。
	 * 
	 * @param stockData 触发此次调用的 K 线
	 * @param stockDataList 此次 K 线所对应的K线序列(stockDataList.get(0) 与 stockData 是等价的)
	 */
	public void processStockData(StockDataBean stockData, List<StockDataBean> stockDataList) ;
	
	/**
	 * 处理 K 线之后的操作。
	 * 
	 * <b>注意</b>：此方法的调用发生在 processStockData() 之后。
	 * 
	 * @param stockData 触发此次调用的 K 线
	 * @param stockDataList 此次 K 线所对应的K线序列(stockDataList.get(0) 与 stockData 是等价的)
	 */
	public void postProcessStockData(StockDataBean stockData, List<StockDataBean> stockDataList) ;
	
	// ------ processOrder

	/**
	 * 委托单回调函数。
	 * 
	 * @param entrustOrderPO 委托单
	 */
	public void processEntrustOrder(EntrustOrderPO entrustOrderPO);
	
	/**
	 * 撤销委托单回调函数。
	 * 
	 * @param entrustOrderPO 委托单
	 */
	public void processCancelEntrustOrder(EntrustOrderPO entrustOrderPO);
	
	/**
	 * 交易单回调函数。
	 * 
	 * @param dealOrderPO 交易单
	 */
	public void processDealOrder(DealOrderPO dealOrderPO);
}