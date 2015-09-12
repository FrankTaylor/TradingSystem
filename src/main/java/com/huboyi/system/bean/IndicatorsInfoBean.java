package com.huboyi.system.bean;

import java.util.List;

import com.huboyi.engine.load.bean.StockDataBean;


/**
 * 指标集合信息Bean。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/9/12
 * @version 1.0
 */
public abstract class IndicatorsInfoBean {
	
	/**
	 * 得到最后一根行情数据。
	 * 
	 * @param stockDataBeanList 股票行情数据集合
	 * @return StockDataBean
	 */
	public StockDataBean getLastStockData (List<StockDataBean> stockDataList) {
		if (stockDataList != null && !stockDataList.isEmpty()) {
			return stockDataList.get(stockDataList.size() - 1);
		}
		return null;
	}
}