package com.huboyi.system.function;

import java.util.List;

import com.huboyi.data.entity.StockDataBean;
import com.huboyi.indicators.technology.constant.FractalType;
import com.huboyi.indicators.technology.constant.KTypeInFractal;
import com.huboyi.indicators.technology.entity.pattern.BandBean;
import com.huboyi.indicators.technology.entity.pattern.FractalBean;

/**
 * 交易模块中使用的分型函数。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/15
 * @version 1.0
 */
public class FractalFunction {
	
	/**
	 * 得到最后一个分型数据。
	 * 
	 * @return FractalBean
	 */
	public static FractalBean getLastValidFractal (final List<FractalBean> validFractalList) {
		if (validFractalList != null && !validFractalList.isEmpty()) {
			return validFractalList.get(validFractalList.size() - 1);
		}
		return null;
	}
	
	/**
	 * 得到分型中未经包含处理的实际K线。
	 * 
	 * @param stockDataList 股票行情集合
	 * @param fractal K线分型
	 * @param type K线在分型中的类型
	 * @return StockDataBean
	 */
	public static StockDataBean 
	getNoContainKLineInFractalBean (final List<StockDataBean> stockDataList, final FractalBean fractal, final KTypeInFractal type) {
		if (fractal == null || type == null ) {
			throw new RuntimeException("得到分型中未经包含处理的实际K线时，分型和K线类型均不能为null！");
		} 
		
		Long date = type == KTypeInFractal.LEFT ? fractal.getLeft().getDate() : 
			        type == KTypeInFractal.CENTER ? fractal.getCenter().getDate() : 
			        type == KTypeInFractal.RIGHT ? fractal.getRight().getDate() : 
			        fractal.getCenter().getDate();
		
	    for (StockDataBean stockData : stockDataList) {
	    	if (stockData.getDate().equals(date)) {
	    		return stockData;
	    	}
		}
	    
	    return null;
	    
	}
	
	/**
	 * 判断当前形成的底分型，是否是相对有效的底分型。
	 * 
	 * @param lastStockData 最后一根K线
	 * @param bottom 需要比较的底分型
	 * @return boolean true：该底分型时有效的底分型；false：该底分型不是有效的底分型
	 */
	public static boolean 
	isRelativelyEffectiveBottom (final StockDataBean lastStockData, final FractalBean bottom) {
		
		if (lastStockData == null || bottom == null || bottom.getFractalType() == FractalType.TOP) { return false; }
		
		StockDataBean leftOfBottom = bottom.getLeft();                                          // 得到该底分型的左侧K线。
		StockDataBean rightOfBottom = bottom.getRight();                                        // 得到该底分型的右侧K线。
		
		/*
		 * 1、最后一根K线不能是底分型的一部分。
		 */
		if (lastStockData.getDate() <= rightOfBottom.getDate()) { return false; }
		
		/*
		 * 2、最后一根K线的收盘价必须高于底分型左侧和右侧K线的收盘价。
		 */
		if (
				lastStockData.getClose().compareTo(leftOfBottom.getClose()) == -1 || 
				lastStockData.getClose().compareTo(rightOfBottom.getClose()) == -1) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 判断当前形成的顶分型，是否是相对有效的顶分型。
	 * 
	 * @param lastStockData 最后一根K线
	 * @param top 需要比较的顶分型
	 * @return boolean true：该底分型时有效的顶分型；false：该底分型不是有效的顶分型
	 */
	public static boolean 
	isRelativelyEffectiveTop (final StockDataBean lastStockData, final FractalBean top) {
		
		if (top == null || top.getFractalType() == FractalType.BUTTOM) { return false; }
		
		StockDataBean rightOfTop = top.getRight();                                              // 得到该底分型的右侧K线。
		
		/*
		 * 1、最后一根K线不能是顶分型的一部分。
		 */
		if (lastStockData.getDate() <= rightOfTop.getDate()) { return false; }
		
		/*
		 * 2、最后一根K线的收盘价必须高于顶分型右侧K线的收盘价，与底分型相比，没有比较左侧K线是因为，建仓要稳，出场要快。
		 */
		if (lastStockData.getClose().compareTo(rightOfTop.getClose()) == 1) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 在行情数据中的最后一根K线是否是最后一个底分型中的最后一根K线。
	 * 
	 * @param validFractalList 有效的顶底分型集合
	 * @return true：是；false：否
	 */
	public static boolean 
	isLastStockDataOfLastBottom (final List<StockDataBean> stockDataList, final List<FractalBean> validFractalList, final List<BandBean> bandList) {
		
		// 最后一个分型必须是底分型。
		FractalBean lastFractal = getLastValidFractal(validFractalList);                                                            // 最后一个有效的分型信息。
		if (lastFractal.getFractalType() != FractalType.BUTTOM) {
			return false;
		}
		
		/*
		 * 如果最后一根K线是不是最后一个底分型的一部分，那后续K线还会继续参与判断，从而产生频繁产生不必要的信号。
		 * 
		 * 
		 *           |   |
		 *           | | | 
		 *             |   |
		 *             |   1 |
		 *                   2 |
		 *                     3
		 *                     
		 *   图1.在最后的底分型后，K线1、2、3依然会参与判断
		 */
		
		// 最后一个底分型是最后一根波段的底分型。
		FractalBean bottomOfLastBand = BandFunction.getLastBand(bandList).getBottom();                                              // 最后一根波段的底分型。
		if (
				(lastFractal.getLeft().getDate().intValue() != bottomOfLastBand.getLeft().getDate().intValue()) ||
				(lastFractal.getCenter().getDate().intValue() != bottomOfLastBand.getCenter().getDate().intValue()) ||
				(lastFractal.getRight().getDate().intValue() != bottomOfLastBand.getRight().getDate().intValue())) {
			return false;
		}

		// 最后一根K线是最后一个底分型中的右侧K线。
		StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                            // 最后一根K线。
		StockDataBean rightStockData = getNoContainKLineInFractalBean(stockDataList, bottomOfLastBand, KTypeInFractal.RIGHT);       // 得到最后一个分型中真实的右侧K线。

		if (rightStockData == null || (lastStockData.getDate().intValue() != rightStockData.getDate().intValue())) {
			return false;
		}
		
		return true;
	}
}