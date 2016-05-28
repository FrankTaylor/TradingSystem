package com.huboyi.system.strategy;

import java.util.List;

import com.huboyi.data.entity.StockDataBean;

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
	 * 报单相关回报预处理函数，当有报单的委托回报、成交回报、撤单回报返回时调用。
	 * 
	 * 注意：此方法的调用发生在
	 * processOrderStatus(OrderStatusResult result, OrderRelevant relevant, MD md)、 
	 * processOrderDeal(OrderStatusResult result, OrderRelevant relevant, MD md)、
	 * processOrderCancel(OrderStatusResult result, OrderRelevant relevant, MD md)之前。
	 * 
	 * @param result 报单的回报信息基类
	 * @param relevant 报单关联对象：存储报单信息以及与报单相关的各种回报信息有关报单各属性的常量值定义参照Constants
	 */
	public boolean preProcessOrderStatus (OrderStatusResult result, OrderRelevant relevant);
	
	/**
	 * 报单的委托回报处理函数，当有新报单被接收(柜台已接收或交易所已接收)、报单部分成交、全部成交时，都会有对应的委托回报返回，此时都会调用。
	 * 
	 * 注意：只有以 “orderinsert” 作为委托指令的异步报单，该方法才会被调用。 而以“sell、buy”作为委托指令的同步报单，该方法就不会被调用。
	 * 
	 * @param result 委托回报
	 * @param relevant 报单关联对象：存储报单信息以及与报单相关的各种回报信息有关报单各属性的常量值定义参照Constants
	 * @param md 快照行情信息
	 */
	@Override
	public void processOrderStatus (OrderStatusResult result, OrderRelevant relevant, MD md) {
	
}