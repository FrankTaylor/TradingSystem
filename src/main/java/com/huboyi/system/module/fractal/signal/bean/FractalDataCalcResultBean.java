package com.huboyi.system.module.fractal.signal.bean;

import java.io.Serializable;
import java.util.List;

import com.huboyi.engine.indicators.technology.pattern.bean.BandBean;
import com.huboyi.engine.indicators.technology.pattern.bean.FractalBean;
import com.huboyi.engine.indicators.technology.pattern.bean.PowerBean;
import com.huboyi.engine.load.bean.StockDataBean;

/**
 * 顶底分型交易系统所需数据的计算结果。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2014/11/10
 * @version 1.0
 */
public class FractalDataCalcResultBean implements Serializable {

	private static final long serialVersionUID = -5216047687160924198L;

	/** 行情数据。*/
	private List<StockDataBean> stockDataBeanList;
	
	/** 有效的顶底分型集合。*/
	private List<FractalBean> validFractalBeanList;
	
	/** 波段集合。*/
	private List<BandBean> bandBeanList;
	
	/** 中枢集合。*/
	private List<PowerBean> powerBeanList;
	
	/** 无包含关系的中枢集合。*/
	private List<PowerBean> noContainPowerBeanList;
	
	/** 装载每一笔仓位信息的集合。*/
	private List<FractalPositionInfoBean> fractalPositionInfoBeanList;
	
	/**
	 * 得到最后一根行情数据。
	 * 
	 * @return StockDataBean
	 */
	public StockDataBean getLastStockData () {
		if (stockDataBeanList != null && !stockDataBeanList.isEmpty()) {
			return stockDataBeanList.get(stockDataBeanList.size() - 1);
		}
		return null;
	}
	
	/**
	 * 得到最后一个分型数据。
	 * 
	 * @return FractalBean
	 */
	public FractalBean getLastValidFractal () {
		if (validFractalBeanList != null && !validFractalBeanList.isEmpty()) {
			return validFractalBeanList.get(validFractalBeanList.size() - 1);
		}
		return null;
	}
	
	/**
	 * 得到最后一个波段数据。
	 * 
	 * @return FractalBean
	 */
	public BandBean getLastBand () {
		if (bandBeanList != null && !bandBeanList.isEmpty()) {
			return bandBeanList.get(bandBeanList.size() - 1);
		}
		return null;
	}
	
	/**
	 * 得到最后一个无包含关系的中枢。
	 * 
	 * @return PowerBean
	 */
	public PowerBean getLastNoContainPower () {
		if (noContainPowerBeanList != null && !noContainPowerBeanList.isEmpty()) {
			return noContainPowerBeanList.get(noContainPowerBeanList.size() - 1);
		}
		return null;
	}
	
	/**
	 * 得到分型中未经包含处理的实际K线。
	 * 
	 * @param type left:分型左边的K线；center:分型中间的K线；right:分型右边的K线
	 * @return StockDataBean
	 */
	public StockDataBean getNoContainKLineInFractalBean (FractalBean fractalBean, String type) {
		if (fractalBean == null || type == null || type.isEmpty()) {
			throw new RuntimeException("得到分型中未经包含处理的实际K线时，分型和K线类型均不能为null！");
		} 
		
		Integer date = type.equalsIgnoreCase("left") ? fractalBean.getLeft().getDate() : 
			           type.equalsIgnoreCase("center") ? fractalBean.getCenter().getDate() : 
			           type.equalsIgnoreCase("right") ? fractalBean.getRight().getDate() : 
			           fractalBean.getCenter().getDate();
		
	    for (StockDataBean stockData : stockDataBeanList) {
	    	if (stockData.getDate().equals(date)) {
	    		return stockData;
	    	}
		}
	    
	    return null;
	    
	}
	
	// --- get method and set method ---
	
	public List<StockDataBean> getStockDataBeanList() {
		return stockDataBeanList;
	}

	public void setStockDataBeanList(List<StockDataBean> stockDataBeanList) {
		this.stockDataBeanList = stockDataBeanList;
	}

	public List<FractalBean> getValidFractalBeanList() {
		return validFractalBeanList;
	}

	public void setValidFractalBeanList(List<FractalBean> validFractalBeanList) {
		this.validFractalBeanList = validFractalBeanList;
	}

	public List<BandBean> getBandBeanList() {
		return bandBeanList;
	}

	public void setBandBeanList(List<BandBean> bandBeanList) {
		this.bandBeanList = bandBeanList;
	}

	public List<PowerBean> getPowerBeanList() {
		return powerBeanList;
	}

	public void setPowerBeanList(List<PowerBean> powerBeanList) {
		this.powerBeanList = powerBeanList;
	}
	
	public List<PowerBean> getNoContainPowerBeanList() {
		return noContainPowerBeanList;
	}

	public void setNoContainPowerBeanList(List<PowerBean> noContainPowerBeanList) {
		this.noContainPowerBeanList = noContainPowerBeanList;
	}

	public List<FractalPositionInfoBean> getFractalPositionInfoBeanList() {
		return fractalPositionInfoBeanList;
	}

	public void setFractalPositionInfoBeanList(
			List<FractalPositionInfoBean> fractalPositionInfoBeanList) {
		this.fractalPositionInfoBeanList = fractalPositionInfoBeanList;
	}
}