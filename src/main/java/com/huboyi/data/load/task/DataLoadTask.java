package com.huboyi.data.load.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.huboyi.data.entity.MarketDataBean;
import com.huboyi.data.entity.StockDataBean;
import com.huboyi.data.load.DataLoadEngine;

/**
 * 这个类主要用于，读取由“招商证券、金魔方、飞狐交易师”导出的股票行情文件。</p>
 * 
 * 文件名及内容格式解释：
 * 1、文件名示例：SH600015.txt。其中，SH代表上证交易所、SZ代表深证交易所；600015则表示挣钱代码。
 * 2、内容的实例：20030912,1.20,1.25,1.04,1.06,480212200,3516635904.00
 *           20150616,0931,7.04,7.04,6.98,6.99,9179800,64864208.00
 * 由于，不管什么证券类的软件都可以导出好几种格式的数据，为提高程序的适应性，在此规定导出数据的格式：“时间，时分，开，高，低，收，成交量，成交额”。
 * 在此还需要说明一下，如果导出的数据是日线，那就不会有“时分”这个列了。所以在上边给出的内容示例有两个，一个是7列，一个是8列。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @see DataLoadEngine
 * @since 1.5
 */
public class DataLoadTask implements Callable<List<MarketDataBean>> {

	/** 日志。*/
	private final Logger log = LogManager.getLogger(DataLoadTask.class);
	
	/** 日期格式处理类。*/
	private final DateFormat dataFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	
	/** 行情数据文件路径集合。*/
	private final Map<String, String> marketDataFilepathMap;
	
	/** 是否启动监听线程。*/
	private final boolean startMonitorTask;
	/** 当前已经载入的股票行情数据的个数。*/
	private final AtomicInteger currentReadMarketDataNum;
	
	/**
	 * 构造函数。
	 * 
	 * @param marketDataFilepathMap 行情数据文件路径集合
	 * @param currentReadMarketDataNum 当前已经载入的股票行情数据的个数
	 */
	public DataLoadTask(Map<String, String> marketDataFilepathMap, boolean startMonitorTask, AtomicInteger currentReadMarketDataNum) {
		this.marketDataFilepathMap = marketDataFilepathMap;
		this.startMonitorTask = startMonitorTask;
		this.currentReadMarketDataNum = currentReadMarketDataNum;
	}
	
	/**
	 * 返回已读取的行情数据集合。
	 * 
	 * @return List<MarketDataBean>
	 */
	@Override
	public List<MarketDataBean> call() throws Exception {
		
		Thread current = Thread.currentThread();
		long id = current.getId();
		String name = current.getName();
		
		List<MarketDataBean> marketDataList = new ArrayList<MarketDataBean>(marketDataFilepathMap.size() * 2);
		
		try {
			for (Map.Entry<String, String> entry : marketDataFilepathMap.entrySet()) {
				// 股票代码。
				String code = entry.getKey();
				// 股票行情数据的文件路径。
				String marketDataFilepath = URLDecoder.decode(entry.getValue(), "UTF-8");
				
				log.debug("当前线程[name = " + name + "]正在读取[证券代码：" + code + "]的行情数据。");
				
				// 把市场行情数据载入集合。
				MarketDataBean marketData = new MarketDataBean();
				marketData.setCode(code);
				marketData.setName("");
				marketData.setDataPath(marketDataFilepath);
				marketData.setStockDataList(loadDataIntoStockDataBean(marketDataFilepath, ","));
				
				marketDataList.add(marketData);
				
				// 由于 CAS 在多线程竞争时有性能消耗，如果不需要监控，则可以避免消耗，从而加快读取速度。
				if (startMonitorTask) {
					// 把当前完成读取的股票数量加一。
					currentReadMarketDataNum.addAndGet(1);
					log.debug("当前线程[name = " + name + "]完成读取[证券代码：" + code + "]的股票行情数据。");
				}
				
			}
		} catch (Exception e) {
			log.error("当前线程[id = " + id + ", name = " + name + "]在读取股票行情数据的过程中出现错误！", e);
		}
		
		return marketDataList;
	}

	/**
	 * 装载股票行情数据到StockDataBean中（适用于读取使用招商证券中高级导出功能所导出的没有标题头的数据文件）。
	 * 
	 * @param marketDataFilepath 行情数据的文件路径
	 * @param separator 数据之间的分隔符
	 * @return MarketDataBean
	 */
	private List<StockDataBean> loadDataIntoStockDataBean(final String marketDataFilepath, final String separator) {
		// 装载读取行情数据Bean的集合。
		List<StockDataBean> beanList = new CopyOnWriteArrayList<StockDataBean>();
		
		try {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(marketDataFilepath), "UTF-8"));
				String s;
				while ((s = reader.readLine()) != null) {
					if (s.matches("[0-9]+.*")) {
						String[] dataArray = s.split(separator);
						
						// 目前程序只能装载，日间和日内行情数据信息，对于月、周和毫秒类型的数据格式是不支持的。
						if (dataArray != null && (dataArray.length == 7 || dataArray.length == 8)) {
							
							/* ---------------------- 一、读取数据 ---------------------- */
							
							// --- 时间信息 ---
							String yearAndMonthAndDay = dataArray[0];                                                           // 年月日。
							String hourAndMinute = null;                                                                        // 时分。
							
							// --- 价格信息 ---
							String open = null;                                                                                 // 开盘价。
							String high = null;                                                                                 // 最高价。
							String low = null;                                                                                  // 最低价。
							String close = null;                                                                                // 收盘价。
							
							// --- 成交信息 ---
							String volume = null;                                                                               // 成交量。
							String amount = null;                                                                               // 成交额。
							
							if (dataArray.length == 7) {                                                                        // 装载日线行情数据。 
								hourAndMinute = null;
								open = dataArray[1]; high = dataArray[2]; low = dataArray[3]; close = dataArray[4];
								volume = dataArray[5]; amount = dataArray[6];
							} else if (dataArray.length == 8) {                                                                 // 装载分钟行情数据。
								hourAndMinute = dataArray[1];
								open = dataArray[2]; high = dataArray[3]; low = dataArray[4]; close = dataArray[5];
								volume = dataArray[6]; amount = dataArray[7];
							}
							
							if (
									StringUtils.isEmpty(yearAndMonthAndDay) || yearAndMonthAndDay.length() != 8 ||
									(hourAndMinute != null && hourAndMinute.length() != 4) ||
									
									StringUtils.isEmpty(open) ||
									StringUtils.isEmpty(high) ||
									StringUtils.isEmpty(low) ||
									StringUtils.isEmpty(close) ||
									
									StringUtils.isEmpty(volume) ||
									StringUtils.isEmpty(amount)
								) {
								throw new RuntimeException("股票行情文件 [" + marketDataFilepath + "] 中的内容格式不符合程序的要求！");
							}
							
							/* ---------------------- 二、装载数据 ---------------------- */
							
							StockDataBean bean = new StockDataBean();
							
							// --- 时间信息 ---
							
							bean.setYear(Integer.valueOf(yearAndMonthAndDay.substring(0, 4)));                                  // 年。
							bean.setMonth(Integer.valueOf(yearAndMonthAndDay.substring(4, 6)));                                 // 月。
							bean.setDay(Integer.valueOf(yearAndMonthAndDay.substring(6, 8)));                                   // 日。
							bean.setHour((hourAndMinute != null) ? Integer.valueOf(hourAndMinute.substring(0, 2)) : 0);         // 时。
							bean.setMinute((hourAndMinute != null) ? Integer.valueOf(hourAndMinute.substring(2, 4)) : 0);       // 分。
							bean.setSecond(0);                                                                                  // 秒。
							bean.setMillisecond(0);                                                                             // 毫秒。
							
							bean.setDate(Long.valueOf(                                                                          // 日期（格式：yyyyMMddhhmmssSSS）。
									yearAndMonthAndDay
									.concat((hourAndMinute != null ? hourAndMinute : "0000"))
									.concat((bean.getSecond() > 0 ? String.valueOf(bean.getSecond()) : "00"))
									.concat((bean.getMillisecond() > 0 ? String.valueOf(bean.getMillisecond()) : "000"))));
							bean.setTime(dataFormat.parse(String.valueOf(bean.getDate())).getTime());                           // 时间（格式为当前计算机时间和GMT时间(格林威治时间)1970年1月1号0时0分0秒所差的毫秒数）。
							
							// --- 价格信息 ---
							bean.setOpen(BigDecimal.valueOf(Float.valueOf(open)).setScale(3, RoundingMode.HALF_UP));            // 开盘价。
							bean.setHigh(BigDecimal.valueOf(Float.valueOf(high)).setScale(3, RoundingMode.HALF_UP));            // 最高价。
							bean.setLow(BigDecimal.valueOf(Float.valueOf(low)).setScale(3, RoundingMode.HALF_UP));              // 最低价。
							bean.setClose(BigDecimal.valueOf(Float.valueOf(close)).setScale(3, RoundingMode.HALF_UP));          // 收盘价。
							
							// --- 成交信息 ---
							bean.setVolume(BigDecimal.valueOf(Float.valueOf(volume)).setScale(2, RoundingMode.HALF_UP));        // 成交量。
							bean.setAmount(BigDecimal.valueOf(Float.valueOf(amount)).setScale(3, RoundingMode.HALF_UP));        // 成交额。
							
							// --- 构造链表 ---
							if (beanList.size() > 0) {
		                    	StockDataBean prev = beanList.get(beanList.size() - 1);
		                    	if (null != prev) {
		                    		bean.setPrev(prev);
		                    		prev.setNext(bean);
		                    	}
		                    }
		                    
		                    beanList.add(bean);
						}
					}
				}
			} finally {
				if (null != reader) {
					reader.close();
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return beanList;
	}
	
	/**
	 * 装载股票行情数据到StockDataBean中（适用于读取使用c++写的二进制文件）。
	 * 
	 * @param marketDataFilepath 行情数据的文件路径
	 * @return List<StockDataBean>
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private List<StockDataBean> loadDataIntoStockDataBeanWithWin32(final String marketDataFilepath) {
		// 装载读取行情数据Bean的集合。
		List<StockDataBean> beanList = new CopyOnWriteArrayList<StockDataBean>();
		
		try {	
			BufferedInputStream bis = null;
			byte[] bytes = null;
			try {
				// 读取行情数据的二进制数据。
				bis = new BufferedInputStream(new FileInputStream(marketDataFilepath));
				bytes = new byte[bis.available()];
				if (null == bytes || bytes.length < 32) {
					return beanList;
				}
				bis.read(bytes);
								
				/*
				 * 由于C++写入的字节顺序是从低到高，而Java读取的顺序是从高到低。所以需要用
				 * ByteBuffer类来设置一下字节的顺序。
				 */
				ByteBuffer buffer = ByteBuffer.wrap(bytes);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				
				// 由于一个完整的行情数据（这里是以天为单位）占32个字节，所以总字节数除以32就是需要读取的周期个数。
				int days = buffer.capacity() / 32;
				for (int i = 0; i < days; i++) {
					StockDataBean bean = new StockDataBean();
					
					/*---------- 时间信息 ---------*/
					Integer date = Integer.valueOf(buffer.getInt());
                    bean.setDate(Long.valueOf(date));
                    
                    /*---------- 价格信息 ---------*/
                    BigDecimal open = BigDecimal.valueOf(buffer.getInt()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal high = BigDecimal.valueOf(buffer.getInt()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal low = BigDecimal.valueOf(buffer.getInt()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal close = BigDecimal.valueOf(buffer.getInt()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    bean.setOpen(open);
                    bean.setHigh(high);
                    bean.setLow(low);
                    bean.setClose(close);
                    
                    /*---------- 其他信息 ---------*/
                    BigDecimal amount = BigDecimal.valueOf(buffer.getFloat()).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal volume = BigDecimal.valueOf(buffer.getInt()).setScale(2, RoundingMode.HALF_UP);
                    
                    // --- 
                    /*
                     * 在vipdoc中招商证券保存了前一日收盘价的数据，但是这个数据并不准确。同时在其提供的高级导出功能中导出的数据却没有这一项，
                     * 我就把StockDataBean中的这一项去掉了，但是在读取c++产生的二进制时还是要读一遍，要不位数就不对了。
                     */
					BigDecimal preclose = BigDecimal.valueOf(buffer.getInt()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    // ---
                    
                    bean.setAmount(amount);
                    bean.setVolume(volume);
                    
                    beanList.add(bean);
				}
			} finally {
				if (null != bis) {
					bis.close();
				}
				if (null != bytes) {
					bytes = null;
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return beanList;
	}
}