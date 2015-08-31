package com.huboyi.engine.load.worker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.huboyi.engine.load.bean.StockDataBean;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 装载股票数据的具体工作线程类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/10/16
 * @version 1.0
 */
public class LoadDataWorker implements Callable<Map<String, List<StockDataBean>>> {

	/** 日志。*/
	private static final Logger log = LogManager.getLogger(LoadDataWorker.class);
	
	/** 行情数据文件路径集合。*/
	private final Map<String, String> marketDataFilepathMap;
	
	/** 当前已经载入的股票行情数据的个数。*/
	private AtomicInteger currentReadMarketDataNum;
	
	/**
	 * 构造函数。
	 * 
	 * @param marketDataFilepathMap 行情数据文件路径集合
	 * @param currentReadMarketDataNum 当前已经载入的股票行情数据的个数
	 */
	public LoadDataWorker (Map<String, String> marketDataFilepathMap, AtomicInteger currentReadMarketDataNum) {
		this.marketDataFilepathMap = marketDataFilepathMap;
		this.currentReadMarketDataNum = currentReadMarketDataNum;
	}
	
	/**
	 * 返回已读取的行情数据集合。
	 * 
	 * @return Map<String, List<StockDataBean>>
	 */
	@Override
	public Map<String, List<StockDataBean>> call () throws Exception {
		Thread current = Thread.currentThread();
		long id = current.getId();
		String name = current.getName();
		Map<String, List<StockDataBean>> dataMap = new ConcurrentHashMap<String, List<StockDataBean>>();
		try {
			for (Map.Entry<String, String> entry : marketDataFilepathMap.entrySet()) {
				// 股票代码。
				String code = entry.getKey();
				// 股票行情数据的文件路径。
				String marketDataFilepath = URLDecoder.decode(entry.getValue(), "UTF-8");
				
				log.debug("当前线程[name = " + name + "]正在读取[证券代码：" + code + "]的行情数据。");
				// 把股票的行情数据载入缓存。
				dataMap.put(code, loadDataIntoStockDataBean(marketDataFilepath, ","));
				// 把当前完成读取的股票数量加一。
				currentReadMarketDataNum.addAndGet(1);
				log.debug("当前线程[name = " + name + "]完成读取[证券代码：" + code + "]的股票行情数据。");
			}
		} catch (Exception e) {
			log.error("当前线程[id = " + id + ", name = " + name + "]在读取股票行情数据的过程中出现错误！", e);
		}
		return dataMap;
	}
	
	/**
	 * 装载股票行情数据到StockDataBean中（适用于读取使用招商证券中高级导出功能所导出的没有标题头的数据文件）。
	 * 
	 * @param marketDataFilepath 行情数据的文件路径
	 * @param separator 数据之间的分隔符
	 * @return List<StockDataBean>
	 */
	private List<StockDataBean> loadDataIntoStockDataBean (final String marketDataFilepath, final String separator) {
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
						
						StockDataBean bean = new StockDataBean();
						
						/*---------- 时间信息 ---------*/
						Integer date = Integer.valueOf(dataArray[0]);
	                    bean.setDate(date);
	                    
	                    /*---------- 价格信息 ---------*/
	                    BigDecimal open = BigDecimal.valueOf(Float.valueOf(dataArray[1])).setScale(2, RoundingMode.HALF_UP);
	                    BigDecimal high = BigDecimal.valueOf(Float.valueOf(dataArray[2])).setScale(2, RoundingMode.HALF_UP);
	                    BigDecimal low = BigDecimal.valueOf(Float.valueOf(dataArray[3])).setScale(2, RoundingMode.HALF_UP);
	                    BigDecimal close = BigDecimal.valueOf(Float.valueOf(dataArray[4])).setScale(2, RoundingMode.HALF_UP);
	                    bean.setOpen(open);
	                    bean.setHigh(high);
	                    bean.setLow(low);
	                    bean.setClose(close);
	                    
	                    /*---------- 成交信息 ---------*/
	                    BigDecimal volume = BigDecimal.valueOf(Float.valueOf(dataArray[5])).setScale(2, RoundingMode.HALF_UP);
	                    BigDecimal amount = BigDecimal.valueOf(Float.valueOf(dataArray[6])).setScale(3, RoundingMode.HALF_UP);
	                    bean.setVolume(volume);
	                    bean.setAmount(amount);
	                    
	                    /*---------- 其他信息 ---------*/
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
	private List<StockDataBean> loadDataIntoStockDataBeanWithWin32 (final String marketDataFilepath) {
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
                    bean.setDate(date);
                    
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