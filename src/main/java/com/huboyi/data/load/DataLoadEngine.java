package com.huboyi.data.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.data.entity.MarketDataBean;
import com.huboyi.data.load.task.DataLoadMonitorTask;
import com.huboyi.data.load.task.DataLoadTask;

/**
 * 装载股票数据的引擎类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.5
 */
public class DataLoadEngine {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(DataLoadEngine.class);
	
	/** 市场行情数据文件夹路径。*/
	private String marketDataFolderpath;

	/** CPU 个数。*/
	private int cpuNums = 1;
	/** CPU 核心数。*/
	private int coreNums = Runtime.getRuntime().availableProcessors();
	/** 装载市场行情数据的线程数。*/
	private int loadDataThreadNums;
	
	/** 是否启动监听线程。*/
	private boolean startMonitorTask = true;
	/** 监听任务的监控间隔时间（毫秒）。*/
	private long monitoringInterval = 1000;

	/**
	 * 读取全部的行情数据。
	 * 
	 * @return List<MarketDataBean>
	 */
	public List<MarketDataBean> loadMarketData() {
		log.info("准备读取上交所和深交所的股票数据！");
		// 根据 “CPU 个数” 和 “单个 CPU 核数” 计算出 “虚拟 CPU 数量”。
		int virtualCPUNums = cpuNums * coreNums;
		
		// --- 参数验证 ---
		
		if (StringUtils.isBlank(marketDataFolderpath)) {
			throw new IllegalArgumentException("市场行情数据文件夹路径不能为空 [" + marketDataFolderpath + "]！");
		}
		
		/* 
		 * 任务可分为 “计算密集型” 和 “IO 密集型” 两种，该任务属于 I/O 密集型操作，阻塞系数暂定为 0.6
		 * 第一种计算方法：线程数 = 虚拟 CPU 数量 / (1 - 阻塞系数)
		 * loadDataThreadNums = (int)(virtualCPUNums / (1 - 0.6));
		 * 
		 * 参考 Java Concurrency in Practice 得出了一个估算线程池大小的经验公式
		 * 第二种计算方法：线程数 = 虚拟 CPU 数量 * 目标 CPU 使用率 * (1 + 等待时间 / 计算时间)
		 * loadDataThreadNums = (int)(virtualCPUNums * 0.8 * (1 + 1/100))
		 * 
		 * 经测试，第一种方法计算出的线程数为 118，平均执行耗时 16 秒，第二种方法计算出的线程数为 57，平均执行耗时 29 秒，第一种明显优于第二种，
		 * 可见第二种方法没有考虑到任务的场景。
		 */
		if (loadDataThreadNums <= 0) { loadDataThreadNums = (int)(virtualCPUNums / (1 - 0.6)); }
		
		if (monitoringInterval < 1000) { monitoringInterval = 1000; }
		
		// --- 具体业务 ---
		
		System.out.println("虚拟 CPU 数量 = " + virtualCPUNums + ", 装载市场行情数据的线程数 = " + loadDataThreadNums);
		System.out.println("是否启动监听线程 = " + startMonitorTask + ", 监听任务的监控间隔时间（毫秒） = " + monitoringInterval);
		
		// 开始时间。
		long startTime = System.nanoTime();
		
		// 初始市场行情数据的集合。
		List<MarketDataBean> marketDataList = new ArrayList<MarketDataBean>(0);
		
		try {
			
			/*
			 * 1、得到市场行情文件，重新初始装载股票行情数据的Bean类集合，避免重新 Hash 的开销。 
			 */
			File[] marketDataFiles = getMarketDataFiles(marketDataFolderpath);
			if (marketDataFiles == null || marketDataFiles.length == 0) { return marketDataList; }
			
			/*
			 * 2、读取市场行情数据文件路径集合。
			 */
			Map<String, String> marketDataFilepathMap = getMarketDataFilepath(marketDataFiles);

			/*
			 * 3、根据 “数据文件数量” 和 “线程数” 计算分隔系数，对装载市场行情数据文件路径的集合进行分割。
			 * 分组系数 = 文件数量 / 线程数 * 0.2；也就是说每个线程只承担 “最大读取量的 20%”，目的是减少每线程执行任务的时长，但这会增加任务量。
			 */
			BigDecimal numberOfFiles = BigDecimal.valueOf(marketDataFilepathMap.size());
			int splitNum = numberOfFiles
			.divide(BigDecimal.valueOf(loadDataThreadNums), 0, RoundingMode.UP)
			.multiply(BigDecimal.valueOf(0.2)).setScale(0, RoundingMode.HALF_UP)
			.intValue();
			List<Map<String, String>> marketDataFilepathMapList = splitMarketDataFilepathMap(marketDataFilepathMap, splitNum);
			
			log.info("总共要读取的行情文件数 = " + numberOfFiles + ", 行情文件分组系数（每线程读取行情文件数） = " + splitNum);
			
			/*
			 * 4、启用一根线程对处理进度进行监控。
			 */
			ExecutorService moniterExec = getMonitorLoadMarketDataThreadPool();
			AtomicInteger currentReadMarketDataNum = new AtomicInteger(0);
			DataLoadMonitorTask dataLoadMonitorTask = null;
			if (this.startMonitorTask) {
				dataLoadMonitorTask = new DataLoadMonitorTask(marketDataFilepathMap.size(), currentReadMarketDataNum, this.monitoringInterval);
				moniterExec.execute(dataLoadMonitorTask);
				moniterExec.shutdown();
			}
			
			/*
			 * 5、多线程读取沪深A股和沪深指数的行情数据。
			 */
			ExecutorService workerExec = getLoadMarketDataThreadPool(loadDataThreadNums);
			marketDataList = readMarketDataToBean(workerExec, marketDataFilepathMapList, startMonitorTask, currentReadMarketDataNum);
			if (startMonitorTask && dataLoadMonitorTask != null) {
				dataLoadMonitorTask.stop();
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// 结束时间。
		long endTime = System.nanoTime();
		
		// 耗时。
		long elapsedTime = TimeUnit.NANOSECONDS.toSeconds((endTime - startTime));
		
		log.info("此次共载入行情数据文件 = " + marketDataList.size() + " 个，耗时 = " + elapsedTime +" 秒");
		
		return marketDataList;
	}
	
	// --- private method ---
	
	/**
	 * 读取得到市场行情文件。
	 * 
	 * @param marketDataFolderpath 市场行情数据文件夹路径
	 * @return File[] 
	 * @throws FileNotFoundException 
	 * @throws NotDirectoryException 
	 */
	private File[] 
	getMarketDataFiles(final String marketDataFolderpath) 
	throws FileNotFoundException, NotDirectoryException {
		// 得到市场行情文件，重新初始装载股票行情数据的Bean类集合。
		Path filePath = Paths.get(marketDataFolderpath);
		File marketDataFolder = filePath.toFile();
		
		if (!marketDataFolder.exists()) {
			throw new FileNotFoundException("该路径[" + marketDataFolderpath + "]在计算机中不存在！");
		}
		if (!marketDataFolder.isDirectory()) {
			throw new NotDirectoryException("该路径[" + marketDataFolder + "]在计算机中不是目录！");
		}
		return marketDataFolder.listFiles();
	}

	/**
	 * 读取装载行情数据的文件路径（注意：路径进行了编码）。
	 * 数据格式：<SH600758=F%3A%5CBondData%5CStock%5CSH600758.txt>
	 * @param marketDataFiles 股票行情数据文件数组
	 * @return Map<String, String>
	 * @throws UnsupportedEncodingException
	 */
	private Map<String, String> 
	getMarketDataFilepath(final File[] marketDataFiles) 
	throws UnsupportedEncodingException {
		log.debug("读取装载行情数据的文件路径。");
		
		// 得到上交所或深交所的股票行情数据，并装载到集合中。
		Map<String, String> marketDataMap = new HashMap<String, String>(marketDataFiles.length * 2);
		for (File f : marketDataFiles) {
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
	splitMarketDataFilepathMap(Map<String, String> marketDataFilepathMap, int unit) {
		log.debug("把装载行情数据路径的大集合，分割成若干个小集合。");
		
		if (null == marketDataFilepathMap || marketDataFilepathMap.isEmpty()) {
			log.warn("实参（装载行情数据路径的大集合）中没有数据！");
			return new ArrayList<Map<String, String>>(0);
		}
		
		// 装载分割好的股票行情数据路径。
		int initialCapacity = BigDecimal.valueOf(marketDataFilepathMap.size()).divide(BigDecimal.valueOf(unit), 0, RoundingMode.UP).intValue();
		List<Map<String, String>> loadSplitMapList = new ArrayList<Map<String, String>>(initialCapacity * 2);
		// 对以读取到的股票行情数据路径集合进行分割。
		Map<String, String> splitMap = new ConcurrentHashMap<String, String>(unit * 2);
		String[] keyArray = marketDataFilepathMap.keySet().toArray(new String[0]);
		int length = keyArray.length;
		for (int i = 0; i < length; i++) {
			String key = keyArray[i];
			String value = marketDataFilepathMap.get(key);
			splitMap.put(key, value);
			
			if ((i + 1) % unit == 0) {
				loadSplitMapList.add(splitMap);
				splitMap = new ConcurrentHashMap<String, String>(unit * 2);
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
	 * @param worker 驱动工作任务的线程池
	 * @param marketDataFilepathMapList 装载市场行情数据文件路径的分割集合
	 * @param startMonitorTask 是否启动监听线程
	 * @param currentReadMarketDataNum 当前已经载入的股票行情数据的个数
	 * @return List<MarketDataBean>
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private List<MarketDataBean> 
	readMarketDataToBean(
			ExecutorService worker, List<Map<String, String>> marketDataFilepathMapList, 
			boolean startMonitorTask, AtomicInteger currentReadMarketDataNum) 
			throws InterruptedException, ExecutionException {
		
		// 根据装载市场数据文件路径集合的尺寸，创建同样多个装载数据的任务类。
		List<DataLoadTask> dataLoadTaskList = getDataLoadTaskList(marketDataFilepathMapList, startMonitorTask, currentReadMarketDataNum);
		
		// 使用线程池驱动任务。
		List<Future<List<MarketDataBean>>> futureList = worker.invokeAll(dataLoadTaskList);
		worker.shutdown();
		
		List<MarketDataBean> marketDataList = new CopyOnWriteArrayList<MarketDataBean>();
		
		if (null != futureList && !futureList.isEmpty()) {
			for (Future<List<MarketDataBean>> future : futureList) {
				for (MarketDataBean marketData : future.get()) {
					marketDataList.add(marketData);
				}
			}
		}

		return marketDataList;
	}
	
	/**
	 * 载入处理装载股票行情数据的工作类。
	 * 
	 * @param startMonitorTask 是否启动监听线程
	 * @param marketDataFilepathMapList 装载股票行情数据文件路径的集合类
	 * @return List<DataLoadTask> 
	 */
	private List<DataLoadTask> 
	getDataLoadTaskList(List<Map<String, String>> marketDataFilepathMapList, boolean startMonitorTask, AtomicInteger currentReadMarketDataNum) {
		List<DataLoadTask> workerList = new ArrayList<DataLoadTask>(marketDataFilepathMapList.size());
		if (null == marketDataFilepathMapList || marketDataFilepathMapList.isEmpty()) {
			log.warn("装载股票行情数据文件路径的集合类中没有任何信息！");
			return workerList;
		}
		
		for (Map<String, String> marketDataFilepathMap : marketDataFilepathMapList) {
			workerList.add(new DataLoadTask(marketDataFilepathMap, startMonitorTask, currentReadMarketDataNum));
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
		
		ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("监控载入股票行情数据线程");
				t.setPriority(Thread.MAX_PRIORITY);
				t.setDaemon(true);
				
				t.setUncaughtExceptionHandler(new UncaughtExceptionHandler () {
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						log.error("监控载入股票行情数据线程在执行过程中出现错误！" + e);
					}
				});
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
	private ExecutorService getLoadMarketDataThreadPool(int num) {
		log.info("得到处理装载股票行情数据的线程池。");
		ExecutorService es = Executors.newFixedThreadPool(num, new ThreadFactory() {
			
			int threadNum = 1;
			
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("执行载入股票行情数据的线程 " + threadNum++);
				
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
	
	public DataLoadEngine setMarketDataFolderpath(String marketDataFolderpath) {
		this.marketDataFolderpath = marketDataFolderpath;
		return this;
	}

	public DataLoadEngine setCpuNums(int cpuNums) {
		if (cpuNums <= 0) { cpuNums = 1; }
		this.cpuNums = cpuNums;
		return this;
	}
	
	public DataLoadEngine setLoadDataThreadNums(int loadDataThreadNums) {
		this.loadDataThreadNums = loadDataThreadNums;
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