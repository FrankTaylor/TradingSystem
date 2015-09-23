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
import java.util.concurrent.TimeUnit;

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
	
	/** 交易日内上午开盘时间。*/
	private final String AM_OPEN_TIME = "9:30";
	/** 交易日内上午闭盘时间。*/
	private final String AM_CLOSE_TIME = "11:30";
	
	/** 交易日内下午开盘时间。*/
	private final String PM_OPEN_TIME = "13:00";
	/** 交易日内下午闭盘时间。*/
	private final String PM_CLOSE_TIME = "15:00";

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
			
			Map<String, List<StockDataBean>> mergeStockDataListMap = new HashMap<String, List<StockDataBean>>();
			
			for (Map.Entry<String, List<StockDataBean>> entrySet : stockDataListMap.entrySet()) {
				String code = entrySet.getKey();
				List<StockDataBean> stockDataList = entrySet.getValue();
				
				Bar bar = new Bar();
				List<Bar> barList = new ArrayList<Bar>();
				long startDate = 0L, rangeDate = 0L;
				
				for (int i = 0; i < stockDataList.size(); i++) {
					
					StockDataBean stockData = stockDataList.get(i);
					String yearAndMonthAndDay = String.valueOf(stockData.getDate()).substring(0, 8);
					
					System.out.println(stockData);
					
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
				
				mergeStockDataListMap.put(code, mergeList);
			}
			
			return mergeStockDataListMap;
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
	
	private HourAndMinuteRange computeHourAndMinuteRange(MergeTimeType type) {
		
		String amOpenTime[] = AM_OPEN_TIME.split(":");
		String amCloseTime[] = AM_CLOSE_TIME.split(":");
		
		String pmOpenTime[] = PM_OPEN_TIME.split(":");
		String pmCloseTime[] = PM_CLOSE_TIME.split(":");
		
		int amOpenHour = Integer.valueOf(amOpenTime[0]);
		int amOpenMinute = Integer.valueOf(amOpenTime[1]);
		int amCloseHour = Integer.valueOf(amCloseTime[0]);
		int amCloseMinute = Integer.valueOf(amCloseTime[1]);
		
		int pmOpenHour = Integer.valueOf(pmOpenTime[0]);
		int pmOpenMinute = Integer.valueOf(pmOpenTime[1]);
		int pmCloseHour = Integer.valueOf(pmCloseTime[0]);
		int pmCloseMinute = Integer.valueOf(pmCloseTime[1]);
		
		int addRange = 
			(type == MergeTimeType.MINUTE_1) ? 1 :
				(type == MergeTimeType.MINUTE_5) ? 5 :
					(type == MergeTimeType.MINUTE_10) ? 10 :
						(type == MergeTimeType.MINUTE_15) ? 15 :
							(type == MergeTimeType.MINUTE_30) ? 30 :
								(type == MergeTimeType.MINUTE_60) ? 60 :0;
		
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, amOpenHour);
		calendar.set(Calendar.MINUTE, amOpenMinute);
		
		List<HourAndMinuteRange> rangeList = new ArrayList<HourAndMinuteRange>();
		while (true) {
			calendar.add(Calendar.MINUTE, addRange);
			
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			
			if (hour > pmCloseHour || (hour == pmCloseHour && minute > pmCloseMinute)) {
				break;
			}
			
			if (hour == amCloseHour && minute > amCloseMinute) {
				calendar.set(Calendar.HOUR_OF_DAY, pmOpenHour);
				calendar.set(Calendar.MINUTE, pmOpenMinute);
				continue;
			}
			
			rangeList.add(new HourAndMinuteRange().setHour(hour).setMinute(minute));
		}
		
		for (int i = 0; i < rangeList.size() - 1; i++) {
			rangeList.get(i).setNext(rangeList.get(i + 1));
		}
		rangeList.get(rangeList.size() - 1).setNext(rangeList.get(0));
		
		return rangeList.get(0);
	}
	
	/** 主要用于，在合并日内分钟级别K线时，记录合并的时间范围。*/
	private class HourAndMinuteRange {
		private int hour, minute;
		private HourAndMinuteRange next;
		
		public int getHour() { return hour; }
		public HourAndMinuteRange setHour(int hour) { this.hour = hour; return this; }
		public int getMinute() { return minute; }
		public HourAndMinuteRange setMinute(int minute) { this.minute = minute; return this; }
		public HourAndMinuteRange getNext() { return next; }
		public HourAndMinuteRange setNext(HourAndMinuteRange next) { this.next = next; return this; }
		
		@Override
		public String toString() {
			return "[Hour = " + getHour() + ", Minute = " + getMinute() + "]";
		}
	}
	
	public static void main(String[] args) {
		MergeByTime mbt = new MergeByTime();
		HourAndMinuteRange range = mbt.computeHourAndMinuteRange(MergeTimeType.MINUTE_15);
		
		try {
			for (int i = 0; i < 200; i++) {
				System.out.println(range);
				range = range.getNext();
				
				TimeUnit.MILLISECONDS.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}