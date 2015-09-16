package com.huboyi.system.function;

import java.math.BigDecimal;
import java.util.List;

import com.huboyi.engine.indicators.technology.TechAlgorithm;
import com.huboyi.engine.indicators.technology.energy.bean.MACDBean;
import com.huboyi.engine.indicators.technology.pattern.bean.BandBean;
import com.huboyi.engine.load.bean.StockDataBean;

/**
 * 交易模块中使用的买点和卖点函数。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/15
 * @version 1.0
 */
public class BPAndSPFunction {
	
	/**
	 * 判断是否产生了第一买点。
	 * 
	 * @param stockDataList 股票行情数据集合
	 * @param compare 比较波段
	 * @param comparand 被比较波段
	 * @return boolean true：发生背离；false：没有发生背离
	 */
	public static boolean 
	isProduceOneBuyPoint (final List<StockDataBean> stockDataList, final BandBean compare, final BandBean comparand) {
		
		if (compare == null || comparand == null) {
			throw new RuntimeException("判断是否产生了第一买点时参数不符合要求！[compare = " + compare + "] | [comparand = " + comparand + "]");
		}
		

		/*
		 * ##################################################
		 * # 条件1、比较波段的总成交额   < 被比较波段的总成交额。                                     #
		 * # （量能与价格、时间 背离。——下跌时间长，价格更低，成交额逐步变小。）      #
		 * ##################################################
		 */
		if (compare.getTotalAmount().compareTo(comparand.getTotalAmount()) == -1) {

			/*
			 * +-----------------------------------------------------------+
			 * + 计算比较波段和被比较波段内MACD的值。                                                                                                  +
			 * +-----------------------------------------------------------+
			 */
			Integer topDataOfComparand = comparand.getTop().getCenter().getDate();                       // 被比价波段起始时间。
			Integer bottomDataOfComparand = comparand.getBottom().getCenter().getDate();                 // 被比较波段结束时间。
			
			Integer topDataOfCompare = compare.getTop().getCenter().getDate();                           // 比较波段起始时间。
			Integer bottomDataOfCompare = compare.getBottom().getCenter().getDate();                     // 比较波段结束时间。

			StockDataBean topSdBeanOfComparand = comparand.getTop().getCenter();                         // 被比较波段顶分型的中间K线。
			Integer usefulSdBeanStartDate = topDataOfComparand;                                          // 有用的行情数据的起始日期。
			for (int i = 0; i < 26; i++) {
				topSdBeanOfComparand = topSdBeanOfComparand.getPrev();
				if (topSdBeanOfComparand != null && topSdBeanOfComparand.getDate() != null) {					
					usefulSdBeanStartDate = topSdBeanOfComparand.getDate();
				} else {
					break;
				}
			}
			
			int usefulSdBeanNums = 0;                                                                    // 有用的行情数据的数量。
			for (int i = (stockDataList.size() - 1); i >= 0; i--) {
				if (stockDataList.get(i).getDate() >= usefulSdBeanStartDate) {
					usefulSdBeanNums++;
				} else {
					break;
				}
			}
			
			List<MACDBean> macdList = TechAlgorithm.MACD(stockDataList, usefulSdBeanNums, 12, 26, 9);    // 计算MACD。
			if (macdList == null || macdList.size() < 26) { return false; }
			
			
			BigDecimal totalMacdOfComparand = new BigDecimal(0);                                         // 记录被比较波段内MACD中红绿柱高度的总和。
			{
				int startSeq = 0, endSeq = 0;                                                            // 记录在被比较波段MACD集合中，该波段开始和结束日期所对应的序列位置。
				
				// 记录被比较波段内，MACD绿柱子的面积。
				for (int i = 0; i < macdList.size(); i++) {
					MACDBean macd = macdList.get(i);
					
					if (macd.getDate() == topDataOfComparand) { startSeq = i; }
					if (macd.getDate() == bottomDataOfComparand) { endSeq = i; }
					
					if (macd.getDate() >= topDataOfComparand && macd.getDate() <= bottomDataOfComparand) {
						if (macd.getMacd().doubleValue() < 0) {
							totalMacdOfComparand = totalMacdOfComparand.add(macd.getMacd());
						}
					}
					
					if (macd.getDate() > bottomDataOfComparand) { break; }
				}
				
				// 对波段开始前，尚未累计的MACD绿柱子面积进行收录。
				for (int i = (startSeq - 1); i > 0; i--) {
					MACDBean macd = macdList.get(i);
					if (macd.getMacd().doubleValue() < 0) {
						totalMacdOfComparand = totalMacdOfComparand.add(macd.getMacd());
					} else {
						break;
					}
				}
				
				// 对波段结束后，尚未累计的MACD绿柱子面积进行收录。
				for (int i = (endSeq + 1); i < macdList.size(); i++) {
					MACDBean macd = macdList.get(i);
					if (macd.getMacd().doubleValue() < 0) {
						totalMacdOfComparand = totalMacdOfComparand.add(macd.getMacd());
					} else {
						break;
					}
				}
			}
			
			BigDecimal totalMacdOfCompare = new BigDecimal(0);                                           // 记录比较波段内MACD中红绿柱高度的总和。
			{
				int startSeq = 0, endSeq = 0;                                                            // 记录在比较波段MACD集合中，该波段开始和结束日期所对应的序列位置。
				
				// 记录比较波段内，MACD绿柱子面积。
				for (int i = 0; i < macdList.size(); i++) {
					MACDBean macd = macdList.get(i);
					
					if (macd.getDate() == topDataOfCompare) { startSeq = i; }
					if (macd.getDate() == bottomDataOfCompare) { endSeq = i; }
					
					if (macd.getDate() >= topDataOfCompare && macd.getDate() <= bottomDataOfCompare) {
						if (macd.getMacd().doubleValue() < 0) {
							totalMacdOfCompare = totalMacdOfCompare.add(macd.getMacd());
						}
					}
				}
				
				// 对波段开始前，尚未累计的MACD绿柱子面积进行收录。
				for (int i = (startSeq - 1); i > 0; i--) {
					MACDBean macd = macdList.get(i);
					if (macd.getMacd().doubleValue() < 0) {
						totalMacdOfCompare = totalMacdOfCompare.add(macd.getMacd());
					} else {
						break;
					}
				}
				
				// 对波段结束后，可能未累计的MACD绿柱子面积进行估算（趋势延续期0.8倍，衰退期0.2倍）。
				MACDBean lastMACD = macdList.get(endSeq);                                                // 最后一个MACD数据。
				MACDBean prevOflastMACD = macdList.get(endSeq - 1);                                      // 倒数第二个MACD数据。
				MACDBean prefOfPrevMACD = macdList.get(endSeq - 2);                                      // 倒数第三个MACD数据。
				
				if (
						prevOflastMACD.getMacd().compareTo(prefOfPrevMACD.getMacd()) == -1 &&
						lastMACD.getMacd().compareTo(prevOflastMACD.getMacd()) == -1) {
					totalMacdOfCompare = totalMacdOfCompare.add(totalMacdOfCompare.multiply(new BigDecimal(0.8)));
				} else {
					if (lastMACD.getMacd().doubleValue() < 0) {						
						totalMacdOfCompare = totalMacdOfCompare.add(totalMacdOfCompare.multiply(new BigDecimal(0.2)));
					}
				}
			}
			/*
			 * ##################################################
			 * # 条件2、比较波段的平均下跌幅度   < 被比较波段平均下跌幅度。                         #
			 * # （空间与价格、时间背离。——下跌时间长，价格更低，平均下跌幅度变小。）   #
			 * ##################################################
			 */
			
			return (totalMacdOfCompare.compareTo(totalMacdOfComparand) == 1) ? true : false;
		}
		
		return false;
	}
	
	/**
	 * 判断是否产生了第一卖点。
	 * 
	 * @param stockDataList 股票行情数据集合
	 * @param compare 比较波段
	 * @param comparand 被比较波段
	 * @return boolean true：发生背离；false：没有发生背离
	 */
	public static boolean 
	isProduceOneSellPoint (final List<StockDataBean> stockDataList, final BandBean compare, final BandBean comparand) {
		
		if (compare == null || comparand == null) {
			throw new RuntimeException("判断是否产生了第一卖点时参数不符合要求！[compare = " + compare + "] | [comparand = " + comparand + "]");
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 计算比较波段和被比较波段内MACD的值。                                                                                                  +
		 * +-----------------------------------------------------------+
		 */
		Integer bottomDataOfComparand = comparand.getBottom().getCenter().getDate();                 // 被比价波段起始时间。
		Integer topDataOfComparand = comparand.getTop().getCenter().getDate();                       // 被比较波段结束时间。
		
		Integer bottomDataOfCompare = compare.getBottom().getCenter().getDate();                     // 比较波段起始时间。
		Integer topDataOfCompare = compare.getTop().getCenter().getDate();                           // 比较波段结束时间。
		
		StockDataBean bottomSdBeanOfComparand = comparand.getBottom().getCenter();                   // 被比较波段底分型的中间K线。
		Integer usefulSdBeanStartDate = bottomDataOfComparand;                                       // 有用的行情数据的起始日期。
		for (int i = 0; i < 26; i++) {
			bottomSdBeanOfComparand = bottomSdBeanOfComparand.getPrev();
			if (bottomSdBeanOfComparand != null && bottomSdBeanOfComparand.getDate() != null) {					
				usefulSdBeanStartDate = bottomSdBeanOfComparand.getDate();
			} else {
				break;
			}
		}

		/*
		 * 计算MACD数据集合中有用的数据个数。
		 */
		int usefulSdBeanNums = 0;                                                                    // 有用的行情数据的数量。
		for (int i = (stockDataList.size() - 1); i >= 0; i--) {
			if (stockDataList.get(i).getDate() >= usefulSdBeanStartDate) {
				usefulSdBeanNums++;
			} else {
				break;
			}
		}

		List<MACDBean> macdList = TechAlgorithm.MACD(stockDataList, usefulSdBeanNums, 12, 26, 9);    // 计算MACD数据。
		if (macdList == null || macdList.size() < 26) {
			return false;
		}
		
		BigDecimal totalMacdOfComparand = new BigDecimal(0);                                         // 记录被比较波段内MACD中红绿柱高度的总和。
		{
			int startSeq = 0, endSeq = 0;                                                            // 记录在被比较波段MACD集合中，该波段开始和结束日期所对应的序列位置。
			
			// 记录被比较波段内，MACD红柱子的面积。
			for (int i = 0; i < macdList.size(); i++) {
				MACDBean macd = macdList.get(i);
				
				if (macd.getDate() == bottomDataOfComparand) { startSeq = i; }
				if (macd.getDate() == topDataOfComparand) { endSeq = i; }
				
				if (macd.getDate() >= bottomDataOfComparand && macd.getDate() <= topDataOfComparand) {
					if (macd.getMacd().doubleValue() > 0) {
						totalMacdOfComparand = totalMacdOfComparand.add(macd.getMacd());
					}
				}
				
				if (macd.getDate() > topDataOfComparand) { break; }
			}
			
			// 对波段开始前，尚未累计的MACD红柱子面积进行收录。
			for (int i = (startSeq - 1); i > 0; i--) {
				MACDBean macd = macdList.get(i);
				if (macd.getMacd().doubleValue() > 0) {
					totalMacdOfComparand = totalMacdOfComparand.add(macd.getMacd());
				} else {
					break;
				}
			}
			
			// 对波段结束后，尚未累计的MACD红柱子面积进行收录。
			for (int i = (endSeq + 1); i < macdList.size(); i++) {
				MACDBean macd = macdList.get(i);
				if (macd.getMacd().doubleValue() > 0) {
					totalMacdOfComparand = totalMacdOfComparand.add(macd.getMacd());
				} else {
					break;
				}
			}
		}
		
		BigDecimal totalMacdOfCompare = new BigDecimal(0);                                           // 记录比较波段内MACD中红绿柱高度的总和。
		{
			int startSeq = 0, endSeq = 0;                                                            // 记录在比较波段MACD集合中，该波段开始和结束日期所对应的序列位置。
			
			// 记录比较波段内，MACD红柱子的面积。
			for (int i = 0; i < macdList.size(); i++) {
				MACDBean macd = macdList.get(i);
				
				if (macd.getDate() == bottomDataOfCompare) { startSeq = i; }
				if (macd.getDate() == topDataOfCompare) { endSeq = i; }
				
				if (macd.getDate() >= bottomDataOfCompare && macd.getDate() <= topDataOfCompare) {
					if (macd.getMacd().doubleValue() > 0) {
						totalMacdOfCompare = totalMacdOfCompare.add(macd.getMacd());
					}
				}
			}
			
			// 对波段开始前，尚未累计的MACD红柱子面积进行收录。
			for (int i = (startSeq - 1); i > 0; i--) {
				MACDBean macd = macdList.get(i);
				if (macd.getMacd().doubleValue() > 0) {
					totalMacdOfCompare = totalMacdOfCompare.add(macd.getMacd());
				} else {
					break;
				}
			}
			
			// 对波段结束后，可能未累计的MACD红柱子面积进行估算（趋势延续期0.8倍，衰退期0.2倍）。
			MACDBean lastMACD = macdList.get(endSeq);                                                // 最后一个MACD数据。
			MACDBean prevOflastMACD = macdList.get(endSeq - 1);                                      // 倒数第二个MACD数据。
			MACDBean prefOfPrevMACD = macdList.get(endSeq - 2);                                      // 倒数第三个MACD数据。
			
			if (
					prevOflastMACD.getMacd().compareTo(prefOfPrevMACD.getMacd()) == 1 &&
					lastMACD.getMacd().compareTo(prevOflastMACD.getMacd()) == 1) {
				totalMacdOfCompare = totalMacdOfCompare.add(totalMacdOfCompare.multiply(new BigDecimal(0.8)));
			} else {
				if (lastMACD.getMacd().doubleValue() > 0) {
					totalMacdOfCompare = totalMacdOfCompare.add(totalMacdOfCompare.multiply(new BigDecimal(0.2)));
				}
			}
		}
		
		
		/*
		 * ##################################################
		 * # 条件2、比较波段的平均上涨幅度   < 被比较波段平均上涨幅度                             #
		 * # （空间与价格、时间背离。——上涨时间长，价格更高，平均上涨幅度变小。）   #
		 * ##################################################
		 */
		
		return (totalMacdOfCompare.compareTo(totalMacdOfComparand) == -1) ? true : false;
	}
	
}