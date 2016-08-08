package com.huboyi.other;

import javax.annotation.Resource;

import com.huboyi.other.shard.SendDataToRemoteServer;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
/**
 * 对{@link SendDataToRemoteServer}的测试。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/4/1
 * @version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/config/other/shard-spring.xml"})
public class TestSendDataToRemoteServer {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestSendDataToRemoteServer.class);
	
	/** 把行情数据发送到远程主机。*/
	@Resource
	private SendDataToRemoteServer sendDataToRemoteServer;

	/**
	 * test {@link SendDataToRemoteServer#sendData} method.
	 * 
	 * @throws Exception
	 */
	@Test 
	public void sendData () throws Exception {
		sendDataToRemoteServer.sendData();
		log.info("已经把行情数据发送到远程主机。");
	}
}