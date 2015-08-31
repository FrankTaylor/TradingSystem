package com.huboyi.other.shard;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 对行情数据文件进行压缩。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/3/30
 * @version 1.0
 */
public class CompressMarketDataFile {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(CompressMarketDataFile.class);
	
	/** 股票行情文件夹路径。*/
	private String stockDataDir;
	/** 股票行情压缩文件夹路径。*/
	private String compressDir;
	/** 压缩文件数量。*/
	private int compressNums;
	/** 股票行情压缩文件名前缀。*/
	private String compressFilePrefix;
	
	/**
	 * 把股票行情归档文件TAR压缩成GZIP文件。
	 * 
	 * @return String[]
	 */
	public String[] createMarketDataTarGzFile () {
		log.info("把股票行情压缩成若干tar.gz文件。");
		
		
		try {
			String[] tarFilePathArray = createMarketDataTarFile();                      // 把股票行情打包成TAR文件。
			String[] tarGzFilePathArray = new String[tarFilePathArray.length];          // 用于保存股票行情tar.gz文件路径的数组。

			for (int i = 0; i < tarGzFilePathArray.length; i++) {
				String tarGzFilePath = compressDir + System.getProperty("file.separator") + compressFilePrefix + (i + 1) + ".tar.gz";   // 创建tar.gz文件路径。
				tarGzFilePathArray[i] = URLEncoder.encode(tarGzFilePath, "UTF-8");                                                     // 把tar.gz文件路径放入数组中。
				
				InputStream in = null;
				GzipCompressorOutputStream out = null;
				try {
					in = new BufferedInputStream(new FileInputStream(URLDecoder.decode(tarFilePathArray[i], "UTF-8")));
					out = new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(tarGzFilePath)));
					
					// --- 把tar文件压缩到gzip文件中。
                    IOUtils.copy(in, out);
	            	
				} finally {
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
			}
			return tarGzFilePathArray;
		} catch (Exception e) {
			log.error(e);
		}
		
		return new String[0];
	}
	
	/**
	 * 把股票行情压缩成若干ZIP文件。（由于使用Apache Compress框架压缩的zip文件在Centos中不能用unzip解压缩，所以暂且不用。）
	 * 
	 * @return String[]
	 */
	public String[] createMarketDataZipFile () {
		log.info("把股票行情压缩成若干ZIP文件。");
		
		validateArgument();                                                             // 验证参数的有效性。
		
		try {
			List<String> marketDataList = getMarketDataList();                          // 载入装载行情数据的文件路径。
			List<String>[] marketDataListArray = splitMarketDataList(marketDataList);   // 把装载股票行情的大集合，按给定的份数分割若干个小集合。
			String[] zipFilePathArray = new String[marketDataListArray.length];         // 用于保存股票行情zip文件路径的数组。
			
			if (marketDataListArray == null || marketDataListArray.length == 0) {
				throw new IllegalArgumentException("在股票行情集合中没有任何数据！");
			}
			
			for (int i = 0; i < marketDataListArray.length; i++) {
				String zipFilePath = compressDir + System.getProperty("file.separator") + compressFilePrefix + (i + 1) + ".zip";   // 创建zip文件路径。
				zipFilePathArray[i] = URLEncoder.encode(zipFilePath, "UTF-8");                                                     // 把zip文件路径放入数组中。
				
				List<String> mdList = marketDataListArray[i];
				ZipArchiveOutputStream zipOutput = null;
				try {
					
					zipOutput = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(zipFilePath))); 
					zipOutput.setEncoding("UTF-8");
		            zipOutput.setUseZip64(Zip64Mode.AsNeeded);
		            
		            for (String filePath : mdList) {
		            	File marketDataFile = new File(URLDecoder.decode(filePath, "UTF-8"));
		            	
		            	if (marketDataFile.exists() && marketDataFile.isFile()) {
		            		InputStream in = new BufferedInputStream(new FileInputStream(marketDataFile));
		                    try {
		                        ZipArchiveEntry entry = new ZipArchiveEntry(marketDataFile, marketDataFile.getName());
		                        zipOutput.putArchiveEntry(entry);
		                        
		                        // --- 把文件拷贝到zip文件中。
		                        IOUtils.copy(in, zipOutput);
		                        
		                        zipOutput.closeArchiveEntry();
		                    } finally {
		                    	IOUtils.closeQuietly(in);
		                    } 
		            	}
		            	
		            }
				} finally {
					IOUtils.closeQuietly(zipOutput);
				}
			}
			return zipFilePathArray;
		} catch (Exception e) {
			log.error(e);
		}
		
		return new String[0];
	}
	
	// --- private method ---
	
	/**
	 * 把股票行情打包成TAR文件。
	 * 
	 * @return String[]
	 */
	private String[] createMarketDataTarFile () {
		
		validateArgument();                                                             // 验证参数的有效性。
		
		try {
			List<String> marketDataList = getMarketDataList();                          // 载入装载行情数据的文件路径。
			List<String>[] marketDataListArray = splitMarketDataList(marketDataList);   // 把装载股票行情的大集合，按给定的份数分割若干个小集合。
			String[] tarFilePathArray = new String[marketDataListArray.length];         // 用于保存股票行情zip文件路径的数组。
			
			if (marketDataListArray == null || marketDataListArray.length == 0) {
				throw new IllegalArgumentException("在股票行情集合中没有任何数据！");
			}
			
			for (int i = 0; i < marketDataListArray.length; i++) {
				String tarFilePath = compressDir + System.getProperty("file.separator") + compressFilePrefix + (i + 1) + ".tar";   // 创建tar文件路径。
				tarFilePathArray[i] = URLEncoder.encode(tarFilePath, "UTF-8");                                                     // 把tar文件路径放入数组中。
				
				List<String> mdList = marketDataListArray[i];
				TarArchiveOutputStream tarOutput = null;
				try {
					tarOutput = new TarArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(tarFilePath)));
					
		            for (String filePath : mdList) {
		            	File marketDataFile = new File(URLDecoder.decode(filePath, "UTF-8"));
		            	
		            	if (marketDataFile.exists() && marketDataFile.isFile()) {
		            		InputStream in = new BufferedInputStream(new FileInputStream(marketDataFile));
		                    try {
		                    	TarArchiveEntry entry = new TarArchiveEntry(marketDataFile, marketDataFile.getName());
		                    	tarOutput.putArchiveEntry(entry);
		                    	
		                        // --- 把文件归档到tar文件中。
		                        IOUtils.copy(in, tarOutput);
		                        
		                        tarOutput.closeArchiveEntry();
		                    } finally {
		                    	IOUtils.closeQuietly(in);
		                    } 
		            	}
		            	
		            }
				} finally {
					IOUtils.closeQuietly(tarOutput);
				}
			}
			return tarFilePathArray;
		} catch (Exception e) {
			log.error(e);
		}
		
		return new String[0];
	}
	
	// --- private method ---
	
	/**
	 * 验证参数有效性。
	 */
	private void validateArgument () {
		
		if (StringUtils.isEmpty(stockDataDir)) {
			throw new IllegalArgumentException("股票行情文件夹路径不能为空！");
		}
		
		if (StringUtils.isEmpty(compressDir)) {
			throw new IllegalArgumentException("股票行情压缩文件夹路径！");
		}
		
		if (compressNums <= 0) {
			throw new IllegalArgumentException("压缩文件数量必须大于等于1！");
		}
		
		if (StringUtils.isEmpty(compressFilePrefix)) {
			throw new IllegalArgumentException("股票行情压缩文件名前缀不能为空！");
		}
	}
	
	/**
	 * 载入装载行情数据的文件路径。
	 * 
	 * @param marketDataFilepath 装载股票行情数据的文件夹名称
	 * @return Map<String, String>
	 * @throws FileAlreadyExistsException
	 * @throws UnsupportedEncodingException
	 */
	private List<String> 
	getMarketDataList () throws FileNotFoundException, UnsupportedEncodingException {
		log.info("载入装载行情数据的文件路径。");
		File marketDataFile = new File(stockDataDir);
		if (!marketDataFile.exists()) {
			throw new FileNotFoundException("该路径[" + stockDataDir + "]在计算机中不存在！");
		}
		
		// 把股票行情数据并装载到集合中。
		List<String> marketDataList = new ArrayList<String>();
		for (File f : marketDataFile.listFiles()) {
			if (f.isFile()) {
				// 对股票行情数据的文件路径进行编码。
				String filepath = URLEncoder.encode(f.getAbsolutePath(), "UTF-8");
				marketDataList.add(filepath);
			}
		}
		
		return marketDataList;
	}
	
	/**
	 * 把装载股票行情的大集合，按给定的份数分割若干个小集合。
	 * 
	 * @param marketDataList
	 * @param shareNums
	 * @return List<String>[]
	 */
	private List<String>[] splitMarketDataList (List<String> marketDataList) {
		log.info("对所有的股票行情数据进行等份分割。");
		if (marketDataList == null || marketDataList.isEmpty()) {
			throw new IllegalArgumentException("在股票行情集合中没有任何数据！");
		}
		
		
		@SuppressWarnings("unchecked")
		List<String>[] marketDataListArray =                            // 用来保存分割后的每一份股票行情文件集合。
			(marketDataList.size() < compressNums) ? new ArrayList[1] : new ArrayList[compressNums];

		if (compressNums == 1 || marketDataList.size() <= compressNums) {
			marketDataListArray[0] = marketDataList;
			return marketDataListArray;
		}
		
		int totalFileNums = marketDataList.size();                      // 全部股票行情文件的数量。
		int shareFileNums = totalFileNums / compressNums;               // 每一份股票行情文件的数量。
		
		for (int i = 0; i < compressNums; i++) {
			int begin = i * shareFileNums;
			int end = (i != (compressNums - 1)) ? (begin + shareFileNums) : totalFileNums;
			
			List<String> tempList = new ArrayList<String>();
			while (begin < end) {
				tempList.add(marketDataList.get(begin));
				begin++;
			}
			
			marketDataListArray[i] = tempList;
		}
		
		return marketDataListArray;
	}

	// --- spring inject method ---
	
	public void setStockDataDir(String stockDataDir) {
		this.stockDataDir = stockDataDir;
	}

	public void setCompressDir(String compressDir) {
		this.compressDir = compressDir;
	}

	public void setCompressNums(int compressNums) {
		this.compressNums = compressNums;
	}

	public void setCompressFilePrefix(String compressFilePrefix) {
		this.compressFilePrefix = compressFilePrefix;
	}
	
}