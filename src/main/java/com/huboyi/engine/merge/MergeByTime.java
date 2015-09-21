package com.huboyi.engine.merge;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
	
	/** 处理日期和时间的格式类。（YYYY是国际标准ISO 8601所指定的以周来纪日的历法。yyyy是格里高利历，它以400年为一个周期，在这个周期中，一共有97个闰日，在这种历法的设计中，闰日尽可能均匀地分布在各个年份中，所以一年的长度有两种可能：365天或366天。）*/
	private static DateFormat dataFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	
	public static List<Bar> merge(List<StockDataBean> stockDataList, int addSeconds) {
		List<Bar> barList = new ArrayList<Bar>();
		
		Bar bar = new Bar();
		Long timeRange = computeTimeRange(stockDataList.get(0).getTime(), addSeconds);
		
		for (StockDataBean stockData : stockDataList) {
			
			if (stockData.getTime() > timeRange) {
				
				if (!barList.isEmpty()) {
					Bar prev = barList.get(barList.size() - 1);
					prev.setNext(bar);
					bar.setPrev(prev);
				}
				barList.add(bar);		
				
				bar = new Bar();
				timeRange = computeTimeRange(stockData.getTime(), addSeconds);
			}
			
			mergeBar(
					bar,
					stockData.getTime(),
					stockData.getOpen(), stockData.getHigh(), stockData.getLow(), stockData.getClose(),
					stockData.getVolume(), stockData.getAmount());
		}
		
		return barList;
	}
	
	/**
	 * 把行情信息合并到 Bar 对象中。
	 * 
	 * @param bar 合并的 Bar 对象
	 * @param time 行情时间
	 * @param open 开盘价
	 * @param high 最高价
	 * @param low 最低价
	 * @param close 收盘价
	 * @param volume 成交量
	 * @param amount 成交额
	 */
	private static void mergeBar(
			Bar bar, 
			Long time, 
			BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, 
			BigDecimal volume, BigDecimal amount) {
		
		if (bar == null) { bar = new Bar(); }
		
		// --- 时间信息 ---
		bar.setTime(time);
		
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
	 * @param startTime 该参数的格式为 yyyyMMddhhmmssSSS
	 * @param addSeconds 从 startTime 开始增加的秒数
	 * @return Long 返回结果的格式为 yyyyMMddhhmmssSSS
	 */
	private static Long computeTimeRange(long startTime, int addSeconds) {
		
		try {
			long timeMillis = dataFormat.parse(String.valueOf(startTime)).getTime();
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(timeMillis));
			calendar.add(Calendar.SECOND, addSeconds);
			
			return Long.valueOf(dataFormat.format(new Date(calendar.getTimeInMillis())));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}