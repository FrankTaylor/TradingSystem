package com.huboyi.web.module.test.controller;

import java.io.InputStream;
import java.util.Scanner;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huboyi.system.snap.engine.SnapFractalForDayEngine;
import com.huboyi.util.HttpClientHelper;
import com.huboyi.web.module.test.command.StanWeinTestCommand;

/**
 * 交易系统测试的控制类。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2014/11/06
 * @version 1.0
 */
@Controller
public class SystemTestController {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(SystemTestController.class);

	/** 执行顶底分型交易系统捕捉信号的引擎类。*/
	@Resource private SnapFractalForDayEngine snapFractalForDayEngine;
	
	@RequestMapping(value = "/system", method = RequestMethod.GET)
    public String 
    forwardIndex(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.sendRedirect("http://localhost:8080/TradingSystem/system/abc");
		
//		return "testSystemIndex";
		
		return null;
    }
	
	@RequestMapping(value = "/system/abc", method = RequestMethod.GET)
    public String 
    forwardIndex1 (HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		
		DefaultHttpClient httpClient = HttpClientHelper.getDefaultHttpClient(3000, 3000);
		
		// 使用GET方式访问微信地址。
		HttpGet httpGet = new HttpGet("http://www.yabo51.com/products_list/&pmcId=554.html");
		// 处理微信返回信息。
		HttpResponse r = httpClient.execute(httpGet);
		HttpEntity receiveEntity = r.getEntity();
		int statusCode = r.getStatusLine().getStatusCode();
		log.info("statusCode ---> " + statusCode);
		if (statusCode == 200) {
			
			StringBuilder builder = new StringBuilder();
			Scanner scanner = null;
			try {
				scanner = new Scanner(receiveEntity.getContent());
				while (scanner.hasNextLine()) {
					builder.append(scanner.nextLine());
				}
			} finally {
				if (null != scanner) {				
					scanner.close();
				}
			}
			
			response.setContentType("text/html;charset=gb2312");
			response.setCharacterEncoding("gb2312");
			response.getWriter().println(new String(builder.toString().getBytes(), "UTF-8"));
			
			
			return null;
		} else {
			
		}
		
		return "testSystemIndex";
    }
	
	@RequestMapping(value = "/system/snap/fractal", method = RequestMethod.GET)
    public String 
    fractal(HttpServletRequest request, StanWeinTestCommand command) throws ServletException {
		try {
			snapFractalForDayEngine.executeSnap("/usr/local/snapDealSignal.txt");
		} catch (Exception e) {
			log.error(e);
		}
		
		return "testSystemIndex";
    }
}
