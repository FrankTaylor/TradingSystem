package com.huboyi.other.shard;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.common.util.NoCloseOutputStream;

import com.huboyi.other.shard.bean.RemoteServerInfo;

/**
 * 把行情数据发送到远程主机。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/3/31
 * @version 1.0
 */
public class SendDataToRemoteServer {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(SendDataToRemoteServer.class);
	
	/** 对行情数据文件进行压缩。*/
	@Resource
	private CompressMarketDataFile compressMarketDataFile;
	
	/** 远程主机信息集合。*/
	private List<RemoteServerInfo> remoteServerList;
	
	/**
	 * 把行情数据发送到远程主机。
	 */
	public void sendData () {
		log.info("把行情数据发送到远程主机。");
		
		if (remoteServerList == null || remoteServerList.isEmpty()) {
			throw new IllegalArgumentException("没有远程主机信息！");
		}
		
		try {
			
			// --- 压缩股票行情数据。
			String[] tarGzFilePathArray = compressMarketDataFile.createMarketDataTarGzFile();			
			log.info("把股票行情压缩成若干tar.gz文件已完成！");
			
			if (tarGzFilePathArray == null || tarGzFilePathArray.length == 0) {
				throw new RuntimeException("没有得到股票行情tar.gz文件！");
			}
			if (tarGzFilePathArray.length > remoteServerList.size()) {
				throw new RuntimeException("股票行情tar.gz文件数量超出远程主机数量，导致无法均匀分配！");
			}
			for (int i = 0; i < tarGzFilePathArray.length; i++) {
				log.info((i + 1) + ". 已经生成tar.gz文件路径：" + URLDecoder.decode(tarGzFilePathArray[i], "UTF-8"));
			}
			
			// --- 把压缩后的股票行情数据分发到各个远程主机上。
			for (int i = 0; i < tarGzFilePathArray.length; i++) {
				String compressFilePath = URLDecoder.decode(tarGzFilePathArray[i], "UTF-8");   // 得到股票行情tar.gz文件路径。
				RemoteServerInfo remoteServer = remoteServerList.get(i);                       // 得到远程主机信息。 
				
				InputStream in = null;                                                         // 读取本地tar.gz文件的内容。
				OutputStream out = null;                                                       // 把本地tar.gz文件输出远程主机。
				
				SshClient client = SshClient.setUpDefaultClient();
				SftpClient sftpClient = null;
				
				client.start();
				
				try {
					
					// --- 1、连接远程主机。
				    ConnectFuture connectFuture = client.connect(remoteServer.getUsername(), new InetSocketAddress(remoteServer.getHost(), remoteServer.getPort()));
				    connectFuture.await(120, TimeUnit.SECONDS);
				    log.info("1、连接远程主机[" + remoteServer.getHost() + ":" + remoteServer.getPort() + "]成功！");
				    
				    // --- 2、用户身份认证。
				    ClientSession session = connectFuture.getSession();
				    session.addPasswordIdentity(remoteServer.getPassword());
				    AuthFuture authFuture = session.auth();
				    authFuture.await(120, TimeUnit.SECONDS);
				    log.info("2、用户身份[" + remoteServer.getUsername() + "]认证通过！");
				    
				    // --- 3、删除远程主机历史数据，并重新建立数据文件目录。
				    StringBuilder rmAndMkcommand = new StringBuilder();
		            rmAndMkcommand.append("rm -rf " + remoteServer.getStockDataDir()).append("\n");
		            rmAndMkcommand.append("rm -rf " + remoteServer.getIndexDataDir()).append("\n");
		            rmAndMkcommand.append("mkdir -p " + remoteServer.getStockDataDir()).append("\n");
		            rmAndMkcommand.append("mkdir -p " + remoteServer.getIndexDataDir()).append("\n");
		            rmAndMkcommand.append("\n");
		            
				    ClientChannel rmAndMkChannel = session.createExecChannel(rmAndMkcommand.toString());
				    
				    rmAndMkChannel.setOut(new NoCloseOutputStream(System.out));   // 重定向标准输出。
				    rmAndMkChannel.setErr(new NoCloseOutputStream(System.err));   // 重定向错误输出。
				    rmAndMkChannel.open().await();                                // 等待执行命令。
				    rmAndMkChannel.waitFor(ClientChannel.CLOSED, 0);              // 等待客户端关闭再关闭，如果不加这句，执行完成后就会关闭，这样就看不到输出的结果了。
				    log.info("3、删除远程主机历史数据，并已重新建立数据文件目录[" + remoteServer.getStockDataDir() + "]！");
				    
				    // --- 4、使用sftp发送股票行情tar.gz文件。
				    in = new FileInputStream(compressFilePath);
				    byte[] compressFileBytes = new byte[in.available()];
				    in.read(compressFileBytes);
				    
				    String compressFilePathOnRemoteServer = remoteServer.getStockDataDir() + "/" + remoteServer.getStockCompressFileName();
				    
					sftpClient = session.createSftpClient();
					out = sftpClient.write(compressFilePathOnRemoteServer);
					out.write(compressFileBytes);
					/* 
					 * 由于这里是网络传输，之后还需要执行解压缩的命令，所以只能把flush和close等方法放到这里，而不能放到finally方法中。
					 * 要不然会抛出 org.apache.sshd.common.SshException: Already closed 异常。
					 */
					out.flush();
					out.close();
					sftpClient.close();
					log.info("4、已成功使用sftp协议把本地股票行情tar.gz文件[" + compressFilePath + "]发送到远程主机的数据文件目录中！");
					
					// --- 5、解压缩股票行情ZIP文件，并删除。
					StringBuilder uncompressCommand = new StringBuilder();
				    /*
				     * 如果不先使用 "cd /usr/local/marketData/stock"，而直接使用 "tar -xzvf /usr/local/marketData/stock/stockData.tar.gz"
				     * 会直接把tar.gz文件中的内容解压到"/root/"目录中。
				     */
				    uncompressCommand.append("cd " + remoteServer.getStockDataDir()).append("\n");
				    uncompressCommand.append("tar -xzvf " + remoteServer.getStockCompressFileName()).append("\n");
				    uncompressCommand.append("rm -rf " + remoteServer.getStockCompressFileName()).append("\n");
				    uncompressCommand.append("\n");
				    
				    ClientChannel uncompressChannel = session.createExecChannel(uncompressCommand.toString());
				    
				    uncompressChannel.setOut(new NoCloseOutputStream(System.out));   // 重定向标准输出。
				    uncompressChannel.setErr(new NoCloseOutputStream(System.err));   // 重定向错误输出。
				    uncompressChannel.open().await();                                // 等待执行命令。
				    uncompressChannel.waitFor(ClientChannel.CLOSED, 0);              // 等待客户端关闭再关闭，如果不加这句，执行完成后就会关闭，这样就看不到输出的结果了。
					log.info("5、已成功解压并删除缩股票行情文件[" + compressFilePathOnRemoteServer + "]！");
					
					session.close(false);
				} finally {
					if (in != null) {
						in.close();
					}
					if (client != null) {
						client.stop();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}
	
	// --- spring inject ---
	
	public void setRemoteServerList(List<RemoteServerInfo> remoteServerList) {
		this.remoteServerList = remoteServerList;
	}
}