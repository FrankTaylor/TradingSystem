package com.huboyi.data.entity;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * 装载市场数据的Bean。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2016/5/21
 * @since 1.0
 */
@JsonPropertyOrder(value = {"code", "name", "dataPath", "stockDataList"}, alphabetic = false)
public class MarketDataBean implements Serializable {

	private static final long serialVersionUID = -2895420220961111181L;
	
	/** 股票编码。*/
	@JsonProperty(value = "code", required = true)
	private String code;
	/** 股票名称。*/
	@JsonProperty(value = "name", required = false)
	private String name;
	/** 数据源地址。*/
	@JsonProperty(value = "dataPath", required = true)
	private String dataPath;
	
	/** 股票数据集合。*/
	@JsonProperty(value = "stockDataList", required = true)
	private List<StockDataBean> stockDataList;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("code = ").append(code).append("\n")
		.append("\t").append("name = ").append(name).append("\n")
		.append("\t").append("dataPath = ").append(dataPath).append("\n");
		
		if (stockDataList != null) {
			for (StockDataBean stockData : stockDataList) {
				builder.append(stockData);
			}
		}
		
		builder.append("]\n");
		return builder.toString();
	}

	// --- get method and set method ---
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public List<StockDataBean> getStockDataList() {
		return stockDataList;
	}

	public void setStockDataList(List<StockDataBean> stockDataList) {
		this.stockDataList = stockDataList;
	}
}