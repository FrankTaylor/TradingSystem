package com.huboyi.other.shard.bean;

/**
 * 远程主机信息。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/3/31
 * @version 1.0
 */
public class RemoteServerInfo {
	
	/** 主机名。*/
	private String host;
	/** 端口（默认22）。*/
	private int port = 22;
	/** 用户名。*/
	private String username;
	/** 密码。*/
	private String password;
	
	/** 股票行情文件夹路径。*/
	private String stockDataDir;
	/** 指数行情文件夹路径。*/
	private String indexDataDir;
	
	/** 股票行情压缩文件名。*/
	private String stockCompressFileName;
	/** 指数行情压缩文件名。*/
	private String indexCompressFileName;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("host = ").append(host).append("\n")
		.append("\t").append("port = ").append(port).append("\n")
		.append("\t").append("username = ").append(username).append("\n")
		.append("\t").append("password = ").append(password).append("\n")
		.append("\t").append("stockDataDir = ").append(stockDataDir).append("\n")
		.append("\t").append("indexDataDir = ").append(indexDataDir).append("\n")
		.append("\t").append("stockCompressFileName = ").append(stockCompressFileName).append("\n")
		.append("\t").append("indexCompressFileName = ").append(indexCompressFileName).append("\n")
		.append("]\n");
		return builder.toString();
	}
	
	// --- get and set method ---
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStockDataDir() {
		return stockDataDir;
	}

	public void setStockDataDir(String stockDataDir) {
		this.stockDataDir = stockDataDir;
	}

	public String getIndexDataDir() {
		return indexDataDir;
	}

	public void setIndexDataDir(String indexDataDir) {
		this.indexDataDir = indexDataDir;
	}

	public String getStockCompressFileName() {
		return stockCompressFileName;
	}

	public void setStockCompressFileName(String stockCompressFileName) {
		this.stockCompressFileName = stockCompressFileName;
	}

	public String getIndexCompressFileName() {
		return indexCompressFileName;
	}

	public void setIndexCompressFileName(String indexCompressFileName) {
		this.indexCompressFileName = indexCompressFileName;
	}
}