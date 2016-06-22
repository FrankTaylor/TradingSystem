//package com.huboyi.system.module.stanWeinstein.signal.bean;
//
//import java.io.Serializable;
//import java.util.List;
//
//import com.huboyi.data.load.bean.StockDataBean;
//import com.huboyi.engine.indicators.technology.energy.bean.MACDBean;
//import com.huboyi.engine.indicators.technology.trend.bean.MoveAverageBean;
//import com.huboyi.engine.indicators.technology.volume.bean.VolMoveAverageBean;
//
///**
// * StanWeinstein交易系统所需数据的计算结果。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 2014/10/31
// * @version 1.0
// */
//public class StanWeinDataCalcResultBean implements Serializable {
//
//	private static final long serialVersionUID = 7152727561603478235L;
//
//	/** 行情数据。*/
//	private List<StockDataBean> stockDataBeanList;
//	
//	/** 短期均线数据。*/
//	private List<MoveAverageBean> shortMAList;
//	
//	/** 长期均线数据。*/
//	private List<MoveAverageBean> longMAList;
//	
//	/** MACD数据。*/
//	private List<MACDBean> macdBeanList;
//	
//	/** 短期成交量均线数据。*/
//	private List<VolMoveAverageBean> shortVMAList;
//	
//	/** 长期成交量均线数据。*/
//	private List<VolMoveAverageBean> longVMAList;
//	
//	@Override
//	public String toString() {
//		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
//		builder.append("[\n")
//		.append("\t").append("stockDataBeanList.size() = ").append(stockDataBeanList.size()).append("\n")
//		.append("\t").append("shortMAList.size() = ").append(shortMAList.size()).append("\n")
//		.append("\t").append("longMAList.size() = ").append(longMAList.size()).append("\n")
//		.append("\t").append("macdBeanList.size() = ").append(macdBeanList.size()).append("\n")
//		.append("\t").append("shortVMAList.size() = ").append(shortVMAList.size()).append("\n")
//		.append("\t").append("longVMAList.size() = ").append(longVMAList.size()).append("\n")
//		.append("]\n");
//		return builder.toString();
//	}
//	
//	// --- get method and set method ---
//	
//	public List<StockDataBean> getStockDataBeanList() {
//		return stockDataBeanList;
//	}
//
//	public void setStockDataBeanList(List<StockDataBean> stockDataBeanList) {
//		this.stockDataBeanList = stockDataBeanList;
//	}
//
//	public List<MoveAverageBean> getShortMAList() {
//		return shortMAList;
//	}
//
//	public void setShortMAList(List<MoveAverageBean> shortMAList) {
//		this.shortMAList = shortMAList;
//	}
//
//	public List<MoveAverageBean> getLongMAList() {
//		return longMAList;
//	}
//
//	public void setLongMAList(List<MoveAverageBean> longMAList) {
//		this.longMAList = longMAList;
//	}
//
//	public List<MACDBean> getMacdBeanList() {
//		return macdBeanList;
//	}
//
//	public void setMacdBeanList(List<MACDBean> macdBeanList) {
//		this.macdBeanList = macdBeanList;
//	}
//
//	public List<VolMoveAverageBean> getShortVMAList() {
//		return shortVMAList;
//	}
//
//	public void setShortVMAList(List<VolMoveAverageBean> shortVMAList) {
//		this.shortVMAList = shortVMAList;
//	}
//
//	public List<VolMoveAverageBean> getLongVMAList() {
//		return longVMAList;
//	}
//
//	public void setLongVMAList(List<VolMoveAverageBean> longVMAList) {
//		this.longVMAList = longVMAList;
//	}
//}