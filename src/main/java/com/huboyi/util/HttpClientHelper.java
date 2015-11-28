package com.huboyi.util;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * Apache HttpClient框架使用快捷帮助类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class HttpClientHelper {
	
	/**
	 * 创建访问http协议的HttpClient对象。
	 * 
	 * @param connectTimeout
	 * @param readTimeout
	 * @return DefaultHttpClient
	 * @throws KeyManagementException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static DefaultHttpClient  
	getDefaultHttpClient(int connectTimeout, int readTimeout) throws KeyManagementException, NoSuchAlgorithmException {
		// 1、对http链接进行设置。
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 800);                          // 最大连接数。
		ConnManagerParams.setTimeout(params, (connectTimeout + readTimeout));           // 最大等待时长。
		ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(400)); // 最大路由数。
		
		HttpConnectionParams.setConnectionTimeout(params, connectTimeout);              // 连接超时。
		HttpConnectionParams.setSoTimeout(params, readTimeout);                         // 读取超时。
		HttpConnectionParams.setTcpNoDelay(params, true);                               // 不使用NoDelay策略，以保证数据传输的实时性。
		
		params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);      // 关闭100-Continue，提升访问速度。
		
		// 2、注册支持的链接协议。
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		// 设置SSL连接。
		SSLContext sslContext = SSLContext.getInstance("TLS"); 
		sslContext.init(null, new TrustManager[] {
				new X509TrustManager() {
					public void 
					checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		            
					public void 
					checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		            
					public X509Certificate[] getAcceptedIssuers() {
						return null;
		            }
				}
		}, new SecureRandom());
		
		SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext);
		// 接受所有网站的证书（安全系数不高）。
		sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
		
		// 3、创建DefaultHttpClient对象。
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
		httpClient.setHttpRequestRetryHandler(getDefineHttpRequestRetryHandler(20));
		
		return httpClient;
	}
	
	/**
	 * 创建自定义的请求重试设置类。
	 * 
	 * @param maxExecutionCount 最大重复连接次数
	 * @return HttpRequestRetryHandler
	 */
	public static HttpRequestRetryHandler getDefineHttpRequestRetryHandler(final int maxExecutionCount) {
		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				// 超过最大重复次数，就不进行尝试了（总开关）。
				if (executionCount >= maxExecutionCount) {
		            return false;
		        }
				
				// 如果链接超时，就进行重试。
				if (exception instanceof SocketTimeoutException) {
					return true;
				}
				
				// 如果对方服务器无响应，就进行重试。
				if (exception instanceof NoHttpResponseException) {
		            return true;
		        }
				
				// 如果SSL握手出现异常就不进行重试。
				if (exception instanceof SSLHandshakeException) {
		            return false;
		        }
				
				// 如果请求被认为是幂等性的就进行重试。
				HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					return true;
			    }
				
				return false;
			}
			
		};
		return retryHandler;
	}
}