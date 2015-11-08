package com.huboyi.system.function;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.huboyi.data.load.bean.StockDataBean;
import com.huboyi.engine.indicators.technology.constant.KTypeInFractal;
import com.huboyi.engine.indicators.technology.pattern.bean.BandBean;
import com.huboyi.engine.indicators.technology.pattern.bean.FractalBean;

/**
 * 交易模块中使用的K线函数。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/15
 * @version 1.0
 */
public class StockDataFunction {
	
	/**
	 * 得到最后一根行情数据。
	 * 
	 * @param stockDataList 行情数据集合
	 * @return StockDataBean
	 */
	public static StockDataBean 
	getLastStockData (final List<StockDataBean> stockDataList) {
		if (stockDataList != null && !stockDataList.isEmpty()) {
			return stockDataList.get(stockDataList.size() - 1);
		}
		return null;
	}
	
	/**
	 * 判断底分型是否有足够的上升力度。
	 * 
	 * @param stockDataList 股票行情数据集合
	 * @param bandList 波段集合
	 * @param rate 上涨比率
	 * @return boolean true:有足够上升力度；false:没足够上升力度
	 */
	public static boolean 
	isHaveEnoughUpStrength (final List<StockDataBean> stockDataList, final List<BandBean> bandList, final double rate) {
		
		if (stockDataList == null || stockDataList.isEmpty() || bandList == null || bandList.isEmpty() == rate <= 0) {
			throw new RuntimeException("判断底分型是否有足够的上升力度时参数不符合要求！[stockDataList = " + stockDataList + "] | [bandList = " + bandList + "] | [rate = " + rate + "]");
		}
		
		/*
		 * ##################################################
		 * # 只要底分型内的中间K线或右侧K线只要有一个满足条件即可。                               #
		 * ##################################################
		 */
		
		FractalBean bottomOfLastBand = BandFunction.getLastBand(bandList).getBottom();                                                                // 最后一个向下波段的底分型。
		StockDataBean centerStockData = FractalFunction.getNoContainKLineInFractalBean(stockDataList, bottomOfLastBand, KTypeInFractal.CENTER);       // 得到底分型的中间K线。
		
		/*
		 * +-----------------------------------------------------------+
		 * + 由于图形经过了K线包含处理，所以最右侧K线的前一K线并不一定是底分型的中间K线，因此为了  +
		 * + 不影响判断的准确性，要找到这些中间被合并的K线一起参与判断。                                                       +
		 * +-----------------------------------------------------------+
		 */
		
		List<StockDataBean> compareList = new ArrayList<StockDataBean>();                                                                             //  记录参与比较的K线集合。
		StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                                              // 得到最后一根K线。 
		StockDataBean temp = centerStockData;
		while (temp != null && temp.getDate() <= lastStockData.getDate()) {
			compareList.add(temp);
			temp = temp.getNext();
		}
		
		for (StockDataBean compare : compareList) {
			if (isOverstepkLineRate(compare, rate)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 判断顶分型是否有足够的下跌力度。
	 * 
	 * @param stockDataList 股票行情数据集合
	 * @param bandList 波段集合
	 * @param rate 上涨比率
	 * @return boolean true:有足够下跌力度；false:没足够下跌力度
	 */
	public static boolean 
	isHaveEnoughDownStrength (final List<StockDataBean> stockDataList, final List<BandBean> bandList, final double rate) {
		
		if (stockDataList == null || stockDataList.isEmpty() || bandList == null || bandList.isEmpty() == rate <= 0) {
			throw new RuntimeException("判断顶分型是否有足够的下跌力度时参数不符合要求！[stockDataList = " + stockDataList + "] | [bandList = " + bandList + "] | [rate = " + rate + "]");
		}
		
		/*
		 * ##################################################
		 * # 只要顶分型内的中间K线或右侧K线只要有一个满足条件即可。                               #
		 * ##################################################
		 */
		
		FractalBean topOfLastBand = BandFunction.getLastBand(bandList).getTop();                                                                      // 最后一个向上波段的顶分型。
		StockDataBean centerStockData = FractalFunction.getNoContainKLineInFractalBean(stockDataList, topOfLastBand, KTypeInFractal.CENTER);          // 得到顶分型的中间K线。

		/*
		 * +-----------------------------------------------------------+
		 * + 由于图形经过了K线包含处理，所以最右侧K线的前一K线并不一定是顶分型的中间K线，因此为了  +
		 * + 不影响判断的准确性，要找到这些中间被合并的K线一起参与判断。                                                       +
		 * +-----------------------------------------------------------+
		 */
		List<StockDataBean> compareList = new ArrayList<StockDataBean>();                                                                             //  记录参与比较的K线集合。
		StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                                              // 得到最后一根K线。 
		StockDataBean temp = centerStockData;
		while (temp != null && temp.getDate() <= lastStockData.getDate()) {
			compareList.add(temp);
			temp = temp.getNext();
		}
		
		for (StockDataBean compare : compareList) {
			if (isOverstepkLineRate(compare, rate)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 判断当天K线收盘价的涨跌幅度是否超过其某一比率。
	 * 
	 * @param stockData 行情数据
	 * @param rate 参考比率
	 * @return boolean true:超过；false:没超过
	 */
	public static boolean 
	isOverstepkLineRate (final StockDataBean stockData, final double rate) {
		
		StockDataBean stockDataPrev = stockData.getPrev();                         // 得到昨天的行情数据。
		if (stockDataPrev == null) {
			throw new RuntimeException("没有昨日的K线数据，导致不能判断当天K线收盘价的涨跌幅度是否超过" + rate);
		}
		
		/*
		 * 涨跌幅=(现价-上一个交易日收盘价）/ 上一个交易日收盘价 
		 */
		BigDecimal difference = stockData.getClose().subtract(stockDataPrev.getClose());
		BigDecimal rateOfUp = difference.divide(stockDataPrev.getClose(), 5, RoundingMode.HALF_UP);
		
		/*
		 * 涨跌幅=(今日收盘价-今日开盘价）/ 今日开盘价
		 * 
		 * 为什么还要算这个呢？因为，如果今日比昨日低开，但是却走出了一根大阳线，这时如果仅按照上面的公式算，就会丢掉入场信号了。
		 */
		BigDecimal differenceOfDay = stockData.getClose().subtract(stockData.getOpen());
		BigDecimal rateOfUpOfDay = differenceOfDay.divide(stockData.getOpen(), 5, RoundingMode.HALF_UP);
		
		/*
		 * 当发生一字涨跌板时：
		 * 1、如果 rate > 0 时，只要今日收盘价高于昨日收盘价，就返回true，否则返回false；
		 * 2、如果 rate < 0 时，只要今日收盘价低于昨日收盘价，就返回true，否则返回false。
		 */
		if (
				(rateOfUpOfDay.doubleValue() == 0) &&
				(stockData.getClose().doubleValue() == stockData.getOpen().doubleValue()) && 
				(stockData.getClose().doubleValue() == stockData.getHigh().doubleValue()) &&
				(stockData.getClose().doubleValue() == stockData.getLow().doubleValue())
		) {
			if (rate > 0) {
				return (stockData.getClose().compareTo(stockDataPrev.getClose()) == 1);
			} else {
				return (stockData.getClose().compareTo(stockDataPrev.getClose()) == -1);
			}
		}
		
		if (rate > 0) {			
			return (rateOfUp.doubleValue() >= rate) || (rateOfUpOfDay.doubleValue() >= rate);
		} else {
			return (rateOfUp.doubleValue() <= rate) || (rateOfUpOfDay.doubleValue() <= rate);
		}
	}
}