package com.huboyi.other;

import java.net.URLDecoder;

import javax.annotation.Resource;

import com.huboyi.other.shard.CompressMarketDataFile;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
/**
 * 对{@link CompressMarketDataFile}的测试。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/03/31
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/config/other/shard-spring.xml"})
public class TestCompressMarketDataFile {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestCompressMarketDataFile.class);
	
	/** 对行情数据文件进行压缩。*/
	@Resource
	private CompressMarketDataFile compressMarketDataFile;
	
	/**
	 * test {@link CompressMarketDataFile#createMarketDataGZipFile} method.
	 * 
	 * @throws Exception
	 */
	@Test 
	public void createMarketDataGZipFile () throws Exception {
		String[] gzipFilePathArray = compressMarketDataFile.createMarketDataTarGzFile();
		
		for (String gzipFilePath : gzipFilePathArray) {
			log.info("gzip文件：" + URLDecoder.decode(gzipFilePath, "UTF-8"));
		}
	}
	
	/**
	 * test {@link CompressMarketDataFile#createMarketDataZipFile} method.
	 * 
	 * @throws Exception
	 */
//	@Test 
	public void createMarketDataZipFile () throws Exception {
		String[] zipFilePathArray = compressMarketDataFile.createMarketDataZipFile();
		
		for (String zipFilePath : zipFilePathArray) {
			log.info("zip文件：" + URLDecoder.decode(zipFilePath, "UTF-8"));
		}
	}
}