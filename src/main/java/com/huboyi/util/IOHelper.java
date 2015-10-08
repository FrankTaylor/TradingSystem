package com.huboyi.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 提供对IO的操作。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/8/13
 * @version 1.0
 */
public class IOHelper {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(IOHelper.class);
	
	/** 锁对象。*/
	private static final Object lockObject = new Object();
	
	private IOHelper() {}
	
	/**
	 * 创建文件夹。
	 * @param floderPath 文件夹路径.
	 */
	public static void mkFloder (String floderPath) {
		synchronized (lockObject) {
			File file = new File(floderPath);
			if (file.exists()) {
				log.warn("文件夹 floderPath = " + floderPath + " 已经存在不需要在进行创建 ");
			} else {
				file.mkdir();
				log.info("创建文件夹 floderPath = " + floderPath + " 成功 ");
			}
		}
	}
	
	/**
	 * 以二进制的方式读取文件中的内容。
	 * 
	 * @param filepath 文件路径
	 * @return byte[]
	 */
	public static byte[] readFileToBytes(String filepath) {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(filepath)));
			try {
				byte[] bytes = new byte[bis.available()];
				bis.read(bytes);
				return bytes;
			} finally {
				if (null != bis) {
					bis.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info("以二进制的方式读取文件中的内容失败！" + e);
		}
		return new byte[0];
	}
	
	/**
	 * 由于测试的时间非常长，为方便调试需要把测试的结果序列化保存起来。
	 * 
	 * @param obj 序列化对象
	 * @param filepath 输出地址。
	 */
	public static void writeSerializable (final Object obj, final String filepath) {
		if (null == obj) {
			log.error("序列化对象不能为null！");
			return;
		}
		
		if (null == filepath || "".equals(filepath.trim())) {
			log.error("输出路径filepath不能为null或空字符串！");
			return;
		}
		
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filepath));
			try {
				oos.writeObject(obj);
			} finally {
				if (oos != null) {
					oos.flush();
					oos.close();
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * 读取序列化对象信息。
	 * 
	 * @param <T>
	 * @param clazz 对象标识
	 * @param filepath 读取路径
	 * @return T
	 */
	public static <T> T readSerializable (final Class<T> clazz, final String filepath) {
		if (null == clazz) {
			log.error("对象标识不能为null！");
			return null;
		}
		
		if (null == filepath || "".equals(filepath.trim())) {
			log.error("读取路径filepath不能为null或空字符串！");
			return null;
		}
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath));
			try {
				return clazz.cast(ois.readObject());
			} finally {
				if (ois != null) {
					ois.close();
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		
		return null;
	}
	
	/**
	 * 向指定路径输出内容。
	 * 
	 * @param list 输出信息的集合
	 * @param outputFilepath 输出路径
	 */
	public static void 
	saveFileToHardDisk (final List<String> list, final String outputFilepath) {
		if (null == list) {
			log.error("输出信息集合不能为null！");
			return;
		}
		
		// 对集合中的信息进行组合。
		StringBuilder builder = new StringBuilder();
		for (String s : list) {
			builder.append(s).append("\n");
		}
		
		saveFileToHardDisk(builder.toString().getBytes(), outputFilepath);
	}
	
	/**
	 * 向指定路径输出内容。
	 * 
	 * @param array 保存要输出的字节数组
	 * @param outputFilepath 输出路径
	 */
	public static void 
	saveFileToHardDisk (final byte[] array, final String outputFilepath) {
		
		if (null == array) {
			log.error("输出字节array不能为null！");
			return;
		}
		
		if (null == outputFilepath || "".equals(outputFilepath.trim())) {
			log.error("输出路径outputFilepath不能为null或空字符串！");
			return;
		}
		
		try {
			File file = new File(outputFilepath);
			if (file.exists()) {
				file.delete();
			}
			
			RandomAccessFile rag = null;
			FileChannel channel = null;
			FileLock lock = null;
			try {
				rag = new RandomAccessFile(outputFilepath, "rw");
				channel = rag.getChannel();
				// 使用文件锁对象,对本地文件进行操作.
				lock = channel.tryLock();
				lock.channel().write(ByteBuffer.wrap(array));
			} finally {
				if (null != lock) {
					lock.release();
				}
				if (null != channel) {
					channel.close();
				}
				if (null != rag) {
					rag.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}