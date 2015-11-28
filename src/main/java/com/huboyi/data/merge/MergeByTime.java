package com.huboyi.data.merge;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.huboyi.data.bean.StockDataBean;
import com.huboyi.engine.constant.MergeTimeType;

/**
 * 按照时间合并K线数据。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
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
	
	/**
	 * 把小时间单位的行情数据合并成大时间单位的行情数据。
	 * 
	 * @param stockDataListMap 小时间单位的行情数据
	 * @param type 合并K线的时间类型枚举
	 * @return Map<String, List<StockDataBean>>
	 */
	public Map<String, List<StockDataBean>> 
	merge(Map<String, List<StockDataBean>> stockDataListMap, MergeTimeType type) {
		
		Map<String, List<StockDataBean>> mergeStockDataListMap = new HashMap<String, List<StockDataBean>>();   // 装载合并以后的行情数据。
		
		if (
				type == MergeTimeType.MINUTE_1 || type == MergeTimeType.MINUTE_5 || 
				type == MergeTimeType.MINUTE_10 || type == MergeTimeType.MINUTE_15 || 
				type == MergeTimeType.MINUTE_30 || type == MergeTimeType.MINUTE_60) {
			
			// 根据枚举类型，得到出日内合并的时间范围链表的表头。
			HourAndMinuteRange range = computeHourAndMinuteRange(type);
			if (range == null) {
				throw new RuntimeException("在计算日内合并的时间范围链表的表头的过程中出现问题，请检查调用 MergeByTime#merge() 方法时传入的枚举类型！");
			}
			
			// --- 得到上/下午的开闭盘时间（时:分） ---
			int amOpenHour = Integer.valueOf(AM_OPEN_TIME.split(":")[0]);
			int pmCloseHour = Integer.valueOf(PM_CLOSE_TIME.split(":")[0]);
			
			for (Map.Entry<String, List<StockDataBean>> entrySet : stockDataListMap.entrySet()) {
				String stockCode = entrySet.getKey();                                                              // 证券编码。
				List<StockDataBean> originalStockDataList = entrySet.getValue();                                   // 合并之前的原始行情数据集合。
				
				StockDataBean mergeStockData = new StockDataBean();                                                // 用于合并行情数据的 StockDataBean 对象。
				List<StockDataBean> mergeStockDataList = new ArrayList<StockDataBean>();                           // 用于装载合并行情数据的 StockDataBean 对象的集合。
				
				for (int i = 0; i < originalStockDataList.size(); i++) {
					
					StockDataBean originalStockData = originalStockDataList.get(i);
					int hour = originalStockData.getHour(), minute = originalStockData.getMinute();

					// 如果下一根K线的时间范围大于当前给定的范围，就说明当前时间范围的合并告一段落，可以开始下一阶段合并了。
					if (
							(hour > range.getHour() || (hour == range.getHour() && minute > range.getMinute())) 
							||
							(hour == amOpenHour && (range.getHour() >= pmCloseHour))
					) {
						
						i--;
						
						// --- 设置当前 StockDataBean 的时间信息 ---
						StockDataBean originalPrevStockData = originalStockDataList.get(i);
						
						mergeStockData.setYear(originalPrevStockData.getYear());
						mergeStockData.setMonth(originalPrevStockData.getMonth());
						mergeStockData.setDay(originalPrevStockData.getDay());
						mergeStockData.setHour(range.getHour());
						mergeStockData.setMinute(range.getMinute());
						mergeStockData.setSecond(0);
						mergeStockData.setMillisecond(0);

						// Calendar的月份从 0 开始计数，所以当前的月份要减1了。
						Calendar date = Calendar.getInstance();
						date.set(originalPrevStockData.getYear(), (originalPrevStockData.getMonth() - 1), originalPrevStockData.getDay(), range.getHour(), range.getMinute(), 0);
						date.set(Calendar.MILLISECOND, 0);
						
						mergeStockData.setDate(Long.valueOf(dataFormat.format(date.getTime())));
						mergeStockData.setTime(date.getTime().getTime());

						// --- 给当前 StockDataBean 对象设置前后关联的 StockDataBean 对象---
						if (!mergeStockDataList.isEmpty()) {
							StockDataBean prev = mergeStockDataList.get(mergeStockDataList.size() - 1);
							prev.setNext(mergeStockData);
							mergeStockData.setPrev(prev);
						}

						// --- 把当前 StockDataBean 对象放入集合中 ---
						mergeStockDataList.add(mergeStockData);
						
						// --- 重置，为下一个合并做准备 ---
						mergeStockData = new StockDataBean();
						range = range.next;
						continue;
					}
					
					// --- 把行情数据合并到 StockDataBean 对象中 ---
					combineStockData(
							mergeStockData, 
							originalStockData.getOpen(), originalStockData.getHigh(), originalStockData.getLow(), originalStockData.getClose(), 
							originalStockData.getVolume(), originalStockData.getAmount());
				}

				// --- 给合并后的 StockDataBean 对象设置前后关联关系 ---
				for (int i = 0; i < mergeStockDataList.size(); i++) {
					StockDataBean current = mergeStockDataList.get(i);
					StockDataBean prev = (i > 0) ? mergeStockDataList.get(i - 1) : null;
					StockDataBean next = (i < mergeStockDataList.size() - 1) ? mergeStockDataList.get(i + 1) : null;
					
					if (prev != null) {
						prev.setNext(current);
						current.setPrev(prev);
					}
					 
					if (next == null) { break; }
					
					current.setNext(next);
					next.setPrev(current);
				}
				
				// --- 把合并后的 StockDataBean 对象集合放入 Map 中。
				mergeStockDataListMap.put(stockCode, mergeStockDataList);
			}
		}
		
		return mergeStockDataListMap;
	}

	/**
	 * 把行情信息组合到 StockDataBean 对象中。
	 * 
	 * @param stockData StockDataBean
	 * @param open 开盘价
	 * @param high 最高价
	 * @param low 最低价
	 * @param close 收盘价
	 * @param volume 成交量
	 * @param amount 成交额
	 */
	private void combineStockData(
			StockDataBean stockData, 
			BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, 
			BigDecimal volume, BigDecimal amount) {
		
		// --- 价格信息 ---
		if (stockData.getOpen() == null) { stockData.setOpen(open); }
		
		if (stockData.getHigh() == null) {
			stockData.setHigh(high); 
		} else {
			if (stockData.getHigh().compareTo(high) == -1) {
				stockData.setHigh(high);
			}
		}
		
		if (stockData.getLow() == null) {
			stockData.setLow(low);
		} else {
			if (stockData.getLow().compareTo(low) >= 0) {				
				stockData.setLow(low);
			}
		}
		
		stockData.setClose(close);
		
		// --- 成交信息 ---
		if (stockData.getVolume() == null) {
			stockData.setVolume(volume);
		} else {
			stockData.setVolume(stockData.getVolume().add(volume));
		}
		
		if (stockData.getAmount() == null) {
			stockData.setAmount(amount);
		} else {
			stockData.setAmount(stockData.getAmount().add(amount));
		}
	}
	
	/**
	 * 根据枚举类型，得到出日内合并的时间范围链表的表头。
	 * 
	 * @param type 合并K线的时间类型枚举
	 * @return HourAndMinuteRange
	 */
	private HourAndMinuteRange computeHourAndMinuteRange(MergeTimeType type) {
		
		// --- 得到上/下午的开闭盘时间（时:分） ---
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
		
		// --- 根据枚举类型，得到计算时间范围时用到的增幅 ---
		int addRange = 
			(type == MergeTimeType.MINUTE_1) ? 1 :
				(type == MergeTimeType.MINUTE_5) ? 5 :
					(type == MergeTimeType.MINUTE_10) ? 10 :
						(type == MergeTimeType.MINUTE_15) ? 15 :
							(type == MergeTimeType.MINUTE_30) ? 30 :
								(type == MergeTimeType.MINUTE_60) ? 60 :0;
		
		// --- 根据增幅计算出日内合并的时间范围 ---
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, amOpenHour);
		calendar.set(Calendar.MINUTE, amOpenMinute);
		
		HourAndMinuteRange header = null, body = null;
		while (true) {
			calendar.add(Calendar.MINUTE, addRange);
			
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			
			// 当计算的时间范围超过了下午闭市的时间，就退出循环。
			if (hour > pmCloseHour || (hour == pmCloseHour && minute > pmCloseMinute)) {
				break;
			}

			// 当计算的时间范围超过了上午闭市的时间，就重置时间为下午开始的时间。
			if (
					(hour > amCloseHour && hour < pmOpenHour) 
					|| 
					(hour == amCloseHour && minute > amCloseMinute)
				) {
				calendar.set(Calendar.HOUR_OF_DAY, pmOpenHour);
				calendar.set(Calendar.MINUTE, pmOpenMinute);
				continue;
			}
			
			HourAndMinuteRange temp = new HourAndMinuteRange().setHour(hour).setMinute(minute);
			
			if (header == null) {
				header = temp;
			} else if (header != null && body == null) {
				body = temp;
				header.setNext(body);
			} else {
				body.setNext(temp);
				body = temp;
			}
		}
		
		// --- 把合并后的时间范围搞成单项链表 ---
		body.setNext(header);
		
		// --- 返回该链表的表头 ---
		return header;
	}
	
	/**
	 * 该内部采用单向链表结构，用于在合并日内分钟级别K线时，记录合并的时间范围。
	 * @author FrankTaylor <mailto:franktaylor@163.com>
	 * @since 1.1
	 */
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
			for (int i = 0; i < 3000; i++) {
				System.out.println(range);
				range = range.getNext();
				
				TimeUnit.MILLISECONDS.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}