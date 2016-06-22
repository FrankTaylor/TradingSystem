package com.huboyi.strategy;

import java.util.ArrayList;
import java.util.List;

import com.huboyi.data.entity.StockDataBean;
import com.huboyi.trader.entity.po.EntrustOrderPO;

/**
 * 策略实现类的抽象类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public abstract class BaseStrategy implements IBaseStrategy {

    /** 股东代码。*/
    private String stockholder;
    
    /** 股票代码。*/
    private String stockCode;
    /** 股票名称。*/
    private String stockName;
    
    /** 触发此次调用的 K 线。*/
    private StockDataBean stockData;
    /** 此次 K 线所对应的K线序列(stockDataList.get(stockDataList.size() - 1) 与 stockData 是等价的)。*/
    private List<StockDataBean> stockDataList;
    
    // --- 买入 ---
    
    /**
     * 买入。
     *
     * @param vol 买入数量
     * @return List<EntrustOrderPO>
     */
    protected List<EntrustOrderPO> buy(long vol) {
        return buy(stockholder, stockCode, vol, stockData.getClose().doubleValue());
    }

    /**
     * 买入。
     *
     * @param vol 买入数量
     * @param price 买入价格
     * @return List<EntrustOrderPO>
     */
    protected List<EntrustOrderPO> buy(long vol, double price) {
        return buy(stockholder, stockCode, vol, price);
    }

    /**
     * 买入。
     *
     * @param stockCode 股票代码
     * @param vol 买入数量
     * @param price 买入价格
     * @return List<EntrustOrderPO>
     */
    protected List<EntrustOrderPO> buy(String stockCode, long vol, double price) {
        return buy(stockholder, stockCode, vol, price);
    }

    /**
     * 买入。
     *
     * @param stockholder 股东代码
     * @param stockCode 股票代码
     * @param vol 买入数量
     * @param price 买入价格
     * @return List<EntrustOrderPO>
     */
    protected List<EntrustOrderPO> buy(String stockholder, String stockCode, long vol, double price) {
        return new ArrayList<EntrustOrderPO>();
    }
    
    // --- 卖出 ---
    
    /**
     * 卖出。
     *
     * @param vol 卖出数量
     * @return List<EntrustOrderPO>
     */
    protected List<EntrustOrderPO> sell(long vol) {
        return sell(stockholder, stockCode, vol, stockData.getClose().doubleValue());
    }

    /**
     * 卖出。
     *
     * @param vol 卖出数量
     * @param price 卖出价格
     * @return List<EntrustOrderPO>
     */
    protected List<EntrustOrderPO> sell(long vol, double price) {
        return sell(stockholder, stockCode, vol, price);
    }

    /**
     * 卖出。
     *
     * @param stockCode 股票代码
     * @param vol 卖出数量
     * @param price 卖出价格
     * @return List<EntrustOrderPO>
     */
    protected List<EntrustOrderPO> sell(String stockCode, long vol, double price) {
        return sell(stockholder, stockCode, vol, price);
    }

    /**
     * 卖出。
     *
     * @param stockholder 股东代码
     * @param stockCode 股票代码
     * @param vol 卖出数量
     * @param price 卖出价格
     * @return List<EntrustOrderPO>
     */
    protected List<EntrustOrderPO> sell(String stockholder, String stockCode, long vol, double price) {
        return new ArrayList<EntrustOrderPO>();
    }
    
    // --- get and set method ---
    
	public String getStockholder() {
		return stockholder;
	}

	public void setStockholder(String stockholder) {
		this.stockholder = stockholder;
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

	public StockDataBean getStockData() {
		return stockData;
	}

	public void setStockData(StockDataBean stockData) {
		this.stockData = stockData;
	}

	public List<StockDataBean> getStockDataList() {
		return stockDataList;
	}

	public void setStockDataList(List<StockDataBean> stockDataList) {
		this.stockDataList = stockDataList;
	}
}