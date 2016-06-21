package com.huboyi.data.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * 市场行情数据 Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2016/5/21
 * @since 1.0
 */
@JsonPropertyOrder(value = {"dataPath", "stockCode", "stockName", "stockDataList"}, alphabetic = false)
public class MarketDataBean implements Serializable {

	private static final long serialVersionUID = -6728472821671219309L;

	/** 数据源地址。*/
	@JsonProperty(value = "dataPath", required = true)
	private String dataPath;
	
	/** 股票编码。*/
	@JsonProperty(value = "stockCode", required = true)
	private String stockCode;
	/** 股票名称。*/
	@JsonProperty(value = "stockName", required = false)
	private String stockName;
	
	/** 股票数据集合。*/
	@JsonProperty(value = "stockDataList", required = true)
	private List<StockDataBean> stockDataList;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("dataPath = ").append(dataPath).append("\n")
		.append("\t").append("stockCode = ").append(stockCode).append("\n")
		.append("\t").append("stockName = ").append(stockName).append("\n");
		
		if (stockDataList != null) {
			for (StockDataBean stockData : stockDataList) {
				builder.append(stockData);
			}
		}
		
		builder.append("]\n");
		return builder.toString();
	}
	
	// --- get method and set method ---
	
	public String getDataPath() {
		return dataPath;
	}
	
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}
	
	public String getStockCode() {
		return stockCode;
	}

	public void setStockCode(String stockCode) {
		this.stockCode = stockCode;
	}

	public String getStockName() {
		return stockName;
	}

	public void setStockName(String stockName) {
		this.stockName = stockName;
	}
	
	public List<StockDataBean> getStockDataList() {
		return stockDataList;
	}

	public void setStockDataList(List<StockDataBean> stockDataList) {
		this.stockDataList = stockDataList;
	}
}