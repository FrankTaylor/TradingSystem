package com.huboyi.engine.merge;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.huboyi.engine.constant.MergeTimeType;
import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.engine.merge.bean.Bar;

/**
 * 按照时间合并K线数据。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/18
 * @version 1.0
 */
public class MergeByTime {
	
	/** 交易日内的开盘时间。*/
	private final String OPEN_TIME = "093000000";
	
	/** 日内交易时长（小时）。*/
	private final int INTRADAY_DEAL_TIME_DURATION_HOUR = 4;
	/** 日内交易时长（分钟）。*/
	private final int INTRADAY_DEAL_TIME_DURATION_MINUTE = 4 * 60;
	
	/** 处理日期和时间的格式类。（YYYY是国际标准ISO 8601所指定的以周来纪日的历法。yyyy是格里高利历，它以400年为一个周期，在这个周期中，一共有97个闰日，在这种历法的设计中，闰日尽可能均匀地分布在各个年份中，所以一年的长度有两种可能：365天或366天。）*/
	private DateFormat dataFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	
	public Map<String, List<StockDataBean>> 
	merge(Map<String, List<StockDataBean>> stockDataListMap, MergeTimeType mergeTimeType, int range) {
		
		if (mergeTimeType == MergeTimeType.MINUTE) {
			if (range == 0 || range > INTRADAY_DEAL_TIME_DURATION_MINUTE || (INTRADAY_DEAL_TIME_DURATION_MINUTE % range) != 0) {
				throw new RuntimeException("在对行情数据进行分钟级别合并时，合并范围 [range = " + range + "] 不能为 0；" +
						"不能大于 [" + INTRADAY_DEAL_TIME_DURATION_MINUTE + "]；" +
								"还必须能被 [" + INTRADAY_DEAL_TIME_DURATION_MINUTE + "]整除！");
			}
			
			Map<String, List<StockDataBean>> mergeMap = new HashMap<String, List<StockDataBean>>();
			
			for (Map.Entry<String, List<StockDataBean>> m : stockDataListMap.entrySet()) {
				String code = m.getKey();
				List<StockDataBean> stockDataList = m.getValue();
				
				long startDate = 0L, rangeDate = 0L;
				Bar bar = new Bar();
				List<Bar> barList = new ArrayList<Bar>();
				for (int i = 0; i < stockDataList.size(); i++) {
					
					StockDataBean stockData = stockDataList.get(i);
					String yearAndMonthAndDay = String.valueOf(stockData.getDate()).substring(0, 8);
					
					// --- 初始时间范围 ---
					if (startDate == 0 && i == 0) {
						startDate = Long.valueOf(yearAndMonthAndDay + OPEN_TIME);
					}
					if (rangeDate == 0) {
						rangeDate = computeTimeRange(startDate, mergeTimeType, range);
					}
					
					// --- 实际数据合并 ---
					if (!Long.valueOf(yearAndMonthAndDay).equals(Long.valueOf(String.valueOf(rangeDate).substring(0, 8)))) {
						continue;
					}
					
					if (stockData.getDate() <= rangeDate) {
						combineBar(bar, stockData.getOpen(), stockData.getHigh(), stockData.getLow(), stockData.getClose(), stockData.getVolume(), stockData.getAmount());
					}
					
					if (stockData.getDate() == rangeDate) {
						
						if (!barList.isEmpty()) {
							Bar prev = barList.get(barList.size() - 1);
							prev.setNext(bar);
							bar.setPrev(prev);
						}
						
						barList.add(bar);
						
						bar = new Bar();
						startDate = rangeDate;
						rangeDate = computeTimeRange(startDate, mergeTimeType, range);
					}
				}
				
				List<StockDataBean> mergeList = new ArrayList<StockDataBean>();
				for (Bar b : barList) {
					StockDataBean source = new StockDataBean();
					BeanUtils.copyProperties(source, b);
					mergeList.add(source);
				}
				
				mergeMap.put(code, mergeList);
			}
			
			return mergeMap;
		}
		
		if (mergeTimeType == MergeTimeType.HOUR) {
			if (range == 0 || range > INTRADAY_DEAL_TIME_DURATION_HOUR || (INTRADAY_DEAL_TIME_DURATION_HOUR % range) != 0) {
				throw new RuntimeException("在对行情数据进行小时级别合并时，合并范围 [range = " + range + "] 不能为 0；" +
						"不能大于 [" + INTRADAY_DEAL_TIME_DURATION_HOUR + "]；" +
								"还必须能被 [" + INTRADAY_DEAL_TIME_DURATION_HOUR + "]整除！");
			}
		}
		
		if (mergeTimeType == MergeTimeType.DAY) {
			if (range == 0 || range != 1) {
				throw new RuntimeException("在对行情数据进行天级别合并时，合并范围 [range = " + range + "] 不能为 0；还必须等于 1！");
			}
		}
		
		if (mergeTimeType == MergeTimeType.MONTH) {
			if (range == 0 || range != 1) {
				throw new RuntimeException("在对行情数据进行月级别合并时，合并范围 [range = " + range + "] 不能为 0；还必须等于 1！");
			}
		}
		
		if (mergeTimeType == MergeTimeType.QUARTER) {
			if (range == 0 || range != 1) {
				throw new RuntimeException("在对行情数据进行季级别合并时，合并范围 [range = " + range + "] 不能为 0；还必须等于 1！");
			}
		}
		
		if (mergeTimeType == MergeTimeType.YEAR) {
			if (range == 0 || range != 1) {
				throw new RuntimeException("在对行情数据进行年级别合并时，合并范围 [range = " + range + "] 不能为 0；还必须等于 1！");
			}
		}
		
		return null;
	}

//	public List<Bar> merge(List<StockDataBean> stockDataList, int addSeconds) {
//		List<Bar> barList = new ArrayList<Bar>();
//		
//		Bar bar = new Bar();
//		Long timeRange = computeTimeRange(stockDataList.get(0).getTime(), addSeconds);
//		
//		for (StockDataBean stockData : stockDataList) {
//			
//			if (stockData.getTime() > timeRange) {
//				
//				if (!barList.isEmpty()) {
//					Bar prev = barList.get(barList.size() - 1);
//					prev.setNext(bar);
//					bar.setPrev(prev);
//				}
//				barList.add(bar);		
//				
//				bar = new Bar();
//				timeRange = computeTimeRange(stockData.getTime(), addSeconds);
//			}
//			
//			combineBar(
//					bar,
//					stockData.getTime(),
//					stockData.getOpen(), stockData.getHigh(), stockData.getLow(), stockData.getClose(),
//					stockData.getVolume(), stockData.getAmount());
//		}
//		
//		return barList;
//	}
	
	/**
	 * 把行情信息组合到 Bar 对象中。
	 * 
	 * @param bar 合并的 Bar 对象
	 * @param open 开盘价
	 * @param high 最高价
	 * @param low 最低价
	 * @param close 收盘价
	 * @param volume 成交量
	 * @param amount 成交额
	 */
	private void combineBar(
			Bar bar, 
			BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, 
			BigDecimal volume, BigDecimal amount) {
		
		// --- 价格信息 ---
		if (bar.getOpen() == null) { bar.setOpen(open); }
		
		if (bar.getHigh() == null) {
			bar.setHigh(high); 
		} else {
			if (bar.getHigh().compareTo(high) == -1) {
				bar.setHigh(high);
			}
		}
		
		if (bar.getLow() == null) {
			bar.setLow(low);
		} else {
			if (bar.getLow().compareTo(low) >= 0) {				
				bar.setLow(low);
			}
		}
		
		bar.setClose(close);
		
		// --- 成交信息 ---
		if (bar.getVolume() == null) {
			bar.setVolume(volume);
		} else {
			bar.setVolume(bar.getVolume().add(volume));
		}
		
		if (bar.getAmount() == null) {
			bar.setAmount(amount);
		} else {
			bar.setAmount(bar.getAmount().add(amount));
		}
	}
	
	/**
	 * 根据给定的开始时间，计算出终止的时间范围。
	 * 
	 * @param startDate 开始日期 yyyyMMddhhmmssSSS
	 * @param mergeTimeType 合并K线的时间类型枚举
	 * @param addRange 从 startDate 开始增加的时间
	 * @return Long 返回结果的格式为 yyyyMMddhhmmssSSS
	 */
	private Long computeTimeRange(long startDate, MergeTimeType mergeTimeType, int addRange) {
		
		try {
			long timeMillis = dataFormat.parse(String.valueOf(startDate)).getTime();
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(timeMillis));
			
			if (mergeTimeType == MergeTimeType.MINUTE) { calendar.add(Calendar.MINUTE, addRange); }
			if (mergeTimeType == MergeTimeType.HOUR) { calendar.add(Calendar.HOUR, addRange); }
			if (mergeTimeType == MergeTimeType.DAY) { calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, addRange); }
			if (mergeTimeType == MergeTimeType.MONTH) { calendar.add(Calendar.MONTH, addRange); }
			if (mergeTimeType == MergeTimeType.YEAR) { calendar.add(Calendar.YEAR, addRange); }
			
			return Long.valueOf(dataFormat.format(new Date(calendar.getTimeInMillis())));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
}