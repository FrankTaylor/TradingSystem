package com.huboyi.data.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URLEncoder;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.data.entity.StockDataBean;
import com.huboyi.data.load.task.DataLoadMonitorTask;
import com.huboyi.data.load.task.DataLoadTask;

/**
 * 装载股票数据的引擎类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class DataLoadEngine {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(DataLoadEngine.class);
	
	/** 市场行情数据文件夹路径。*/
	private String marketDataFolderpath;
	/** 装载市场行情数据的线程数。*/
	private int loadDataThreadNums = 2;
	/** 装载市场行情数据的超时时长（单位：分钟）*/
	private int loadDataTimeout = 10;
	
	/** 是否启动监听线程。*/
	private boolean startMonitorTask = true;
	/** 监听任务的监控间隔时间（单位毫秒）。*/
	private long monitoringInterval = 1000;
	
	/**
	 * 读取全部的行情数据。
	 * 
	 * @return Map<String, List<StockDataBean>>
	 */
	public Map<String, List<StockDataBean>> getStockData () {
		log.info("准备读取上交所和深交所的股票数据！");
		
		// --- 参数验证 ---
		if (StringUtils.isBlank(marketDataFilepath)) {
			throw new IllegalArgumentException("市场行情数据文件夹路径不能为空 [" + marketDataFilepath + "]！");
		}
		
		if (loadDataThreadNums <= 0) {
			loadDataThreadNums = 2;
			log.warn("读取文件的线程数必须大于 0 [" + loadDataThreadNums + "]，当不满足该条件时，系统将自动调整该参数为 2");
		}
		
		if (loadDataTimeout <= 0) {
			loadDataTimeout = 10;
			log.warn("装载市场行情数据的超时时长必须大于 0 [" + loadDataTimeout + "]，当不满足该条件时，系统将自动调整该参数为 10");
		}
		
		if (monitoringInterval < 1000) {
			monitoringInterval = 1000;
			log.warn("监听任务的监控间隔时间必须大于等于 1000 [" + monitoringInterval + "]，当不满足该条件时，系统将自动调整该参数为 1000");
		}
		
		// --- 具体业务 ---
		
		// 开始时间。
		long startTime = System.nanoTime();
		
		// 装载股票行情数据的Bean类集合。
		Map<String, List<StockDataBean>> beanMap = new ConcurrentHashMap<String, List<StockDataBean>>();
		
		try {
			
			File marketDataFolder = new File(this.marketDataFilepath);
			if (!marketDataFolder.exists()) {
				throw new FileNotFoundException("该路径[" + marketDataFilepath + "]在计算机中不存在！");
			}
			
			File[] marketDataFiles = marketDataFolder.listFiles();
			
			// 得到监控装载行情数据进度的线程池。
			ExecutorService moniterExec = getMonitorLoadMarketDataThreadPool();
			// 得到处理装载行情数据的线程池。
			ExecutorService workerExec = getLoadMarketDataThreadPool(this.loadDataThreadNums);
			
			// 1、读取市场行情数据文件路径集合。
			Map<String, String> marketDataFilepathMap = getMarketDataFilepath(this.marketDataFilepath);

			// 2、对装载市场行情数据文件路径的集合进行分割。
			List<Map<String, String>> marketDataFilepathMapList = splitMarketDataFilepathMap(marketDataFilepathMap, 10);
			
			// 3、启用一根线程对处理进度进行监控。
			AtomicInteger currentReadMarketDataNum = new AtomicInteger(0);
			DataLoadMonitorTask dataLoadMonitorTask = null;
			if (this.startMonitorTask) {
				dataLoadMonitorTask = new DataLoadMonitorTask(marketDataFilepathMap.size(), currentReadMarketDataNum, this.monitoringInterval);
				moniterExec.execute(dataLoadMonitorTask);
				moniterExec.shutdown();
			}
			
			// 4、多线程读取沪深A股和沪深指数的行情数据。
			List<Map<String, List<StockDataBean>>> stockDataBeanLML = 
				readMarketDataToBean(
						moniterExec, 
						workerExec, 
						marketDataFilepathMapList, 
						currentReadMarketDataNum, this.loadDataTimeout);
			// 关闭监听线程。
			if (this.startMonitorTask && dataLoadMonitorTask != null) {
				dataLoadMonitorTask.shutdown();
			}
			
			// 5、把多线程分批次返回的行情数据整合到一个集合类中。
			for (Map<String, List<StockDataBean>> batchMap : stockDataBeanLML) {
				for (String code : batchMap.keySet()) {
					beanMap.put(code, batchMap.get(code));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// 结束时间。
		long endTime = System.nanoTime();
		
		log.info("此次载入原始行情数据共花费：" + (endTime - startTime) / 1000000000 + "秒");
		
		return beanMap;
	}
	
	// --- private method ---
	
	/**
	 * 读取装载行情数据的文件路径。
	 * 
	 * @param marketDataFilepath 装载股票行情数据的文件夹名称
	 * @return Map<String, String>
	 * @throws FileAlreadyExistsException
	 * @throws UnsupportedEncodingException
	 */
	private Map<String, String> 
	getMarketDataFilepath (final String marketDataFilepath) throws FileNotFoundException, UnsupportedEncodingException {
		log.info("读取装载行情数据的文件路径。");
		File marketDataFile = new File(marketDataFilepath);
		if (!marketDataFile.exists()) {
			throw new FileNotFoundException("该路径[" + marketDataFilepath + "]在计算机中不存在！");
		}
		
		// 2、得到上交所或深交所的股票行情数据并装载到集合中。
		Map<String, String> marketDataMap = new HashMap<String, String>();
		for (File f : marketDataFile.listFiles()) {
			if (f.isFile()) {
				// 通过截取文件得到股票的代码。
				String name = f.getName().substring(0, f.getName().indexOf("."));
				// 对股票行情数据的文件路径进行编码。
				String filepath = URLEncoder.encode(f.getAbsolutePath(), "UTF-8");
				marketDataMap.put(name, filepath);
			}
		}
		
		return marketDataMap;
	}
	
	/**
	 * 把装载行情数据路径的大集合，分割成若干个小集合，以充分发挥多线程的处理能力。
	 * 
	 * @param marketDataFilepathMap 装载股票行情数据路径的大集合
	 * @param unit 分割单位
	 * @return List<Map<String, String>> 
	 */
	private List<Map<String, String>> 
	splitMarketDataFilepathMap (Map<String, String> marketDataFilepathMap, int unit) {
		log.info("把装载行情数据路径的大集合，分割成若干个小集合。");
		// 装载分割好的股票行情数据路径。
		List<Map<String, String>> loadSplitMapList = new LinkedList<Map<String, String>>();
		if (null == marketDataFilepathMap || marketDataFilepathMap.isEmpty()) {
			log.warn("实参（装载行情数据路径的大集合）中没有数据！");
			return loadSplitMapList;
		}
		
		// 对以读取到的股票行情数据路径集合进行分割。
		Map<String, String> splitMap = new ConcurrentHashMap<String, String>();
		String[] keyArray = marketDataFilepathMap.keySet().toArray(new String[0]);
		int length = keyArray.length;
		for (int i = 0; i < length; i++) {
			String key = keyArray[i];
			String value = marketDataFilepathMap.get(key);
			splitMap.put(key, value);
			
			if ((i + 1) % unit == 0) {
				loadSplitMapList.add(splitMap);
				splitMap = new ConcurrentHashMap<String, String>();
				continue;
			} else {
				if (i == length - 1) {
					loadSplitMapList.add(splitMap);
				}
			}
		}
		
		return loadSplitMapList;
	}
	
	/**
	 * 读取行情数据到Bean中。
	 * 
	 * @param moniter 驱动监控任务的线程池
	 * @param worker 驱动工作任务的线程池
	 * @param marketDataFilepathMapList 装载市场行情数据文件路径的分割集合
	 * @param currentReadMarketDataNum 当前已经载入的股票行情数据的个数
	 * @param loadDataTimeout 装载市场行情数据的超时时长（单位：分钟）
	 * @return List<Map<String, List<StockDataBean>>>
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private List<Map<String, List<StockDataBean>>> 
	readMarketDataToBean (
			ExecutorService moniter, ExecutorService worker,
			List<Map<String, String>> marketDataFilepathMapList, 
			AtomicInteger currentReadMarketDataNum, int loadDataTimeout) 
			throws InterruptedException, ExecutionException, TimeoutException {
		
		List<Map<String, List<StockDataBean>>> stockDataBeanLML = new CopyOnWriteArrayList<Map<String, List<StockDataBean>>>();
		
		// 根据装载市场数据文件路径集合的尺寸，创建同样多个装载数据的任务类。
		List<DataLoadTask> dataLoadTaskList = getDataLoadTaskList(marketDataFilepathMapList, currentReadMarketDataNum);
		
		// 使用线程池驱动任务。
		List<Future<Map<String, List<StockDataBean>>>> stockFutureList = worker.invokeAll(dataLoadTaskList);
		worker.shutdown();
		
		if (null != stockFutureList && !stockFutureList.isEmpty()) {
			for (Future<Map<String, List<StockDataBean>>> future : stockFutureList) {
				stockDataBeanLML.add(future.get(loadDataTimeout, TimeUnit.MINUTES));
			}
		}

		// 关闭监控线程池。
		moniter.shutdownNow();
		
		return stockDataBeanLML;
	}
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println(Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * 载入处理装载股票行情数据的工作类。
	 * 
	 * @param marketDataFilepathMapList 装载股票行情数据文件路径的集合类
	 * @return List<DataLoadTask> 
	 */
	private List<DataLoadTask> 
	getDataLoadTaskList(List<Map<String, String>> marketDataFilepathMapList, AtomicInteger currentReadMarketDataNum) {
		List<DataLoadTask> workerList = new LinkedList<DataLoadTask>();
		if (null == marketDataFilepathMapList || marketDataFilepathMapList.isEmpty()) {
			log.warn("装载股票行情数据文件路径的集合类中没有任何信息！");
			return workerList;
		}
		
		for (Map<String, String> marketDataFilepathMap : marketDataFilepathMapList) {
			workerList.add(new DataLoadTask(marketDataFilepathMap, currentReadMarketDataNum));
		}
		return workerList;
	}
	
	/**
	 * 得到监控装载股票行情数据进度的线程池。
	 * 
	 * @return ExecutorService
	 */
	private ExecutorService getMonitorLoadMarketDataThreadPool() {
		log.info("得到监控装载股票行情数据进度的线程池。");
		ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactory () {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("监控载入股票行情数据线程");
				t.setPriority(Thread.MAX_PRIORITY);
				return t;
			}
		});
		return es;
	}
	
	/**
	 * 得到处理装载股票行情数据的线程池。
	 * 
	 * @param num 池中线程的数量
	 * @return ExecutorService
	 */
	private ExecutorService getLoadMarketDataThreadPool (int num) {
		log.info("得到处理装载股票行情数据的线程池。");
		ExecutorService es = Executors.newFixedThreadPool(num, new ThreadFactory () {
			
			int threadNum = 1;
			
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("执行载入股票行情数据的线程" + threadNum++);
				
				t.setUncaughtExceptionHandler(new UncaughtExceptionHandler () {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						log.error("载入股票行情数据的线程在执行过程中出现错误！[线程ID = " + t.getId() + "、线程名称 = " + t.getName() + "]" + e);
					}
				});
				return t;
			}
		});
		return es;
	}
	
	// --- get and set method ---
	
	public DataLoadEngine setMarketDataFilepath(String marketDataFilepath) {
		this.marketDataFilepath = marketDataFilepath;
		return this;
	}

	public DataLoadEngine setLoadDataThreadNums(int loadDataThreadNums) {
		this.loadDataThreadNums = loadDataThreadNums;
		return this;
	}

	public DataLoadEngine setLoadDataTimeout(int loadDataTimeout) {
		this.loadDataTimeout = loadDataTimeout;
		return this;
	}

	public DataLoadEngine setStartMonitorTask(boolean startMonitorTask) {
		this.startMonitorTask = startMonitorTask;
		return this;
	}

	public DataLoadEngine setMonitoringInterval(long monitoringInterval) {
		this.monitoringInterval = monitoringInterval;
		return this;
	}
}