package com.huboyi.system.function;

import java.util.List;

import com.huboyi.engine.indicators.technology.TechAlgorithm;
import com.huboyi.engine.indicators.technology.trend.bean.MoveAverageBean;
import com.huboyi.engine.load.bean.StockDataBean;

/**
 * 交易模块中使用的均线函数。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/12
 * @version 1.0
 */
public class MoveAverageFunction {
	
	/**
	 * 是否发生了均线向下穿越。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param shortCycle 短期均线的周期
	 * @param longCycle 长期均线的周期
	 * @return boolean
	 */
	public static <T extends StockDataBean> 
	boolean isCrossoverUp (final List<T> stockDataList, final int shortCycle, final int longCycle) {
		
		MoveAverageBean needCompareMAs[] = getNeedCompareMAs(stockDataList, shortCycle, longCycle);   // 得到需要比较的长短期均线数据。
		if (needCompareMAs == null) { return false; }
		
		MoveAverageBean prevOfShort = needCompareMAs[0];                                              // 得到短期均线的前一结果。
		MoveAverageBean currOfShort = needCompareMAs[1];                                              // 得到短期均线的当前结果。
		
		MoveAverageBean prevOfLong = needCompareMAs[2];                                               // 得到长期均线的前一结果。
		MoveAverageBean currOfLong = needCompareMAs[3];                                               // 得到长期均线的当前结果。
		
		if (
				
				// 条件1：只有当短期均线上穿长期均线时的幅度超过0.03时，才能认定为有效升破。
				(prevOfShort.getAvg().compareTo(prevOfLong.getAvg()) != 1 &&
				currOfShort.getAvg().compareTo(currOfLong.getAvg()) == 1 &&
				currOfShort.getAvg().subtract(currOfLong.getAvg()).doubleValue() >= 0.03) ||
				
				// 条件2：条件2可以看做是条件1的保险，如果前一短期均线上穿前一长期均线的幅度没有超过0.03时，那么继续采用当前的短期和长期均线判断，直到下穿幅度超过0.03为止。
				(prevOfShort.getAvg().compareTo(prevOfLong.getAvg()) == 1 && 
				prevOfShort.getAvg().subtract(prevOfLong.getAvg()).doubleValue() < 0.03 &&
				currOfShort.getAvg().subtract(currOfLong.getAvg()).doubleValue() >= 0.03)) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 是否发生了均线向下穿越。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param shortCycle 短期均线的周期
	 * @param longCycle 长期均线的周期
	 * @return boolean
	 */
	public static <T extends StockDataBean> 
	boolean isCrossoverDown (final List<T> stockDataList, final int shortCycle, final int longCycle) {
		
		MoveAverageBean needCompareMAs[] = getNeedCompareMAs(stockDataList, shortCycle, longCycle);   // 得到需要比较的长短期均线数据。
		if (needCompareMAs == null) { return false; }
		
		MoveAverageBean prevOfShort = needCompareMAs[0];                                              // 得到短期均线的前一结果。
		MoveAverageBean currOfShort = needCompareMAs[1];                                              // 得到短期均线的当前结果。
		
		MoveAverageBean prevOfLong = needCompareMAs[2];                                               // 得到长期均线的前一结果。
		MoveAverageBean currOfLong = needCompareMAs[3];                                               // 得到长期均线的当前结果。

		if (
				
				// 条件1：只有当短期均线下穿长期均线时的幅度超过0.03时，才能认定为有效跌破。
				(prevOfShort.getAvg().compareTo(prevOfLong.getAvg()) != -1 &&
				currOfShort.getAvg().compareTo(currOfLong.getAvg()) == -1 &&
				currOfLong.getAvg().subtract(currOfShort.getAvg()).doubleValue() >= 0.03) ||
				
				// 条件2：条件2可以看做是条件1的保险，如果前一短期均线下穿前一长期均线的幅度没有超过0.03时，那么继续采用当前的短期和长期均线判断，直到下穿幅度超过0.03为止。
				(prevOfShort.getAvg().compareTo(prevOfLong.getAvg()) == -1 && 
				prevOfLong.getAvg().subtract(prevOfShort.getAvg()).doubleValue() < 0.03 &&
				currOfLong.getAvg().subtract(currOfShort.getAvg()).doubleValue() >= 0.03)) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 得到需要比较的长短期均线数据。
	 * MoveAverageBean[0]：短期均线的前一结果。
	 * MoveAverageBean[1]：短期均线的当前结果。
	 * MoveAverageBean[2]：长期均线的前一结果。
	 * MoveAverageBean[3]：长期均线的当前结果。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param shortCycle 短期均线的周期
	 * @param longCycle 长期均线的周期
	 * @return MoveAverageBean[]
	 */
	public static <T extends StockDataBean>
	MoveAverageBean[] getNeedCompareMAs (final List<T> stockDataList, final int shortCycle, final int longCycle) {
		
		if (shortCycle < 2 || longCycle <= shortCycle) { return null; }
		
		List<MoveAverageBean> shortList = TechAlgorithm.partOfMA(stockDataList, (2 * shortCycle), shortCycle);   // 得到短期均线集合。
		List<MoveAverageBean> longList = TechAlgorithm.partOfMA(stockDataList, (2 * longCycle), longCycle);      // 得到长期均线集合。
		
		if (shortList.size() < 2 || longList.size() < 2) { return null; }
		
		MoveAverageBean prevOfShort = shortList.get(shortList.size() - 2);                                       // 得到短期均线的前一结果。
		MoveAverageBean currOfShort = shortList.get(shortList.size() - 1);                                       // 得到短期均线的当前结果。
		
		MoveAverageBean prevOfLong = longList.get(longList.size() - 2);                                          // 得到长期均线的前一结果。
		MoveAverageBean currOfLong = longList.get(longList.size() - 1);                                          // 得到长期均线的当前结果。
		
		return new MoveAverageBean[] {prevOfShort, currOfShort, prevOfLong, currOfLong};
	}
	
}