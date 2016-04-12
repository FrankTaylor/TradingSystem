package com.huboyi.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.tools.benchmark.targets.Person;

/**
 * Apache HttpClient框架使用快捷帮助类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class HttpClientHelper {

	public static void deleteUser() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        HttpDelete httpDelete = new HttpDelete("http://192.168.19.110/activiti-rest/service/identity/users/gonzo");  
        httpDelete.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("fozzie:fozzie".getBytes()));
        httpDelete.addHeader("Authorization", "Basic " + up);
        
        try {  
            // 执行 delete 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpDelete);  
            // 获取响应消息实体。
            HttpEntity entity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (entity != null) {  
                System.out.println("系统返回的内容:" + EntityUtils.toString(entity));  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
	}
	
	public static void addUser() {
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        HttpPost httpPost = new HttpPost("http://192.168.19.110/activiti-rest/service/identity/users");  
        httpPost.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("fozzie:fozzie".getBytes()));
        httpPost.addHeader("Authorization", "Basic " + up);
        
        // 创建用户信息实体。
        Map<String, Object> map = new HashMap<String, Object>();  
        map.put("id", "zqdl");  
        map.put("firstName", "zq");
        map.put("lastName", "dl");
        map.put("email", "zqdl@300.cn");
        map.put("password", "zqdl");
        String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(map);
		} catch (JsonProcessingException e) {}  
        
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        
        try {  
            // 执行 post 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
                System.out.println("系统返回的内容:" + EntityUtils.toString(resposeEntity, "UTF-8"));  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
	}
	
	public void getUsers() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        
        HttpGet httpGet = new HttpGet("http://00001:123456@192.168.19.110/activiti-rest/service/identity/users/00002");  
        httpGet.setConfig(RequestConfig.DEFAULT);
        
        try {  
            // 执行 get 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
            	String meg = EntityUtils.toString(resposeEntity, "UTF-8");
                System.out.println("系统返回的内容:" + meg);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
	}
	
	public void updateUser() {
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        HttpPut httpPut = new HttpPut("http://192.168.19.110/activiti-rest/service/identity/users/zqdl");  
        httpPut.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("fozzie:fozzie".getBytes()));
        httpPut.addHeader("Authorization", "Basic " + up);
        
        // 创建用户信息实体。
        Map<String, Object> map = new HashMap<String, Object>();  
        map.put("firstName", "zzzzzzzzzzz");
        String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(map);
		} catch (JsonProcessingException e) {}  

        try {
        	
        	// 绑定实体信息。
        	httpPut.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON)); 
            // 执行 post 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpPut);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
                System.out.println("系统返回的内容:" + EntityUtils.toString(resposeEntity, "UTF-8"));  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
	}
	
	public void addDeployment() {
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        HttpPost httpPost = new HttpPost("http://192.168.19.110/activiti-rest/service/repository/deployments");  
        httpPost.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("fozzie:fozzie".getBytes()));
        httpPost.addHeader("Authorization", "Basic " + up);

        // 创建部署内容实体。
        InputStream input = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("UserTask.bpmn20.xml");

        HttpEntity entity = MultipartEntityBuilder.create()
        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        .addBinaryBody("upfile", input, ContentType.MULTIPART_FORM_DATA, "UserTask.bpmn20.xml")
        .build();
        httpPost.setEntity(entity);
        
        try {
            // 执行 post 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
                System.out.println("系统返回的内容:" + EntityUtils.toString(resposeEntity, "UTF-8"));  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
				input.close();
			} catch (IOException e) {}
        }
	}
	
	public static void addTaskVariable() {
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        HttpPost httpPost = new HttpPost("http://192.168.19.110/activiti-rest/service/runtime/tasks/4087/variables");  
        httpPost.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("fozzie:fozzie".getBytes()));
        httpPost.addHeader("Authorization", "Basic " + up);
        
        // 创建变量信息实体。
        Map<String, Object> map = new HashMap<String, Object>();  
        map.put("name", "input");  
        map.put("scope", "local");
        map.put("type", "integer");
        map.put("value", 1);
        String json = "";
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			list.add(map);
			json = new ObjectMapper().writeValueAsString(list);
		} catch (JsonProcessingException e) {}  
        
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        
        try {  
            // 执行 post 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
                System.out.println("系统返回的内容:" + EntityUtils.toString(resposeEntity, "UTF-8"));  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
	}
	
	public void completeTask() {
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        HttpPost httpPost = new HttpPost("http://192.168.19.110/activiti-rest/service/runtime/tasks/5019");  
        httpPost.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("00001:123456".getBytes()));
        httpPost.addHeader("Authorization", "Basic " + up);
        
        // 创建变量信息实体。
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("action", "complete");
        
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put("name", "input");
        variablesMap.put("scope", "local");
        variablesMap.put("type", "integer");
        variablesMap.put("value", 1);
        list.add(variablesMap);
        
        map.put("variables", list);
        
        String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(map);
		} catch (JsonProcessingException e) {}  
		System.out.println(json);
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        
        try {  
            // 执行 post 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
                System.out.println("系统返回的内容:" + EntityUtils.toString(resposeEntity, "UTF-8"));  
            }  
        } catch (IOException e) {
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
        
	}
	
	public void getTasks() {
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
        
        HttpGet httpGet = new HttpGet("http://192.168.19.110/activiti-rest/service/runtime/tasks?start=0&size=5&order=desc");  
        httpGet.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("00001:123456".getBytes()));
        httpGet.addHeader("Authorization", "Basic " + up);
        
        try {  
            // 执行 get 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpGet);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
            	String meg = EntityUtils.toString(resposeEntity, "UTF-8");
                System.out.println("系统返回的内容:" + meg);
                
                // 使用 jackson 来解析 json 搞出自己想要的数据。
                @SuppressWarnings({ "unchecked", "unused" })
				Map<String, Object> map = new ObjectMapper().readValue(meg, Map.class);
                
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
	}
	
	public void queryTask() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        HttpPost httpPost = new HttpPost("http://192.168.19.110/activiti-rest/service/query/tasks");  
        httpPost.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("00001:123456".getBytes()));
        httpPost.addHeader("Authorization", "Basic " + up);
        
        // 创建变量信息实体。
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "process");
        map.put("owner", "");
        String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(map);
		} catch (JsonProcessingException e) {}
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        
        try {  
            // 执行 post 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
                System.out.println("系统返回的内容:" + EntityUtils.toString(resposeEntity, "UTF-8"));  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
	}
	
	public void claimTask() {
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
        HttpPost httpPost = new HttpPost("http://192.168.19.110/activiti-rest/service/runtime/tasks/5019");  
        httpPost.setConfig(RequestConfig.DEFAULT);
        
        // 加入认证信息头，注意头部信息需要进行 Base64 编码。
        String up = new String(new Base64().encode("00001:123456".getBytes()));
        httpPost.addHeader("Authorization", "Basic " + up);
        
        // 创建变量信息实体。
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("action", "claim");
        map.put("assignee", "00006");
        
        String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(map);
		} catch (JsonProcessingException e) {}  
		System.out.println(json);
        HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        
        try {  
            // 执行 post 请求。  
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost);  
            
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();  
            // 响应状态 。
            System.out.println("系统返回状态码:" + httpResponse.getStatusLine());  
            // 查看响应实体的内容。  
            if (resposeEntity != null) {  
                System.out.println("系统返回的内容:" + EntityUtils.toString(resposeEntity, "UTF-8"));  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
        	try {
				closeableHttpClient.close();
			} catch (IOException e) {}
        }
        
	}
	
	/**
	 * 登录北京市预约挂号统一平台。
	 * 
	 * @param client CloseableHttpClient
	 * @param mobile 手机号
	 * @param password 密码
	 * @return boolean
	 */
	private static boolean loginBeiJingYYGHPT(CloseableHttpClient client, String mobile, String password) {
		
        HttpPost httpPost = new HttpPost("http://www.bjguahao.gov.cn/quicklogin.htm"); 
        
        try {
        	
        	// 模拟浏览器提交登录请求，虽然这不是必须的，但是为了防止被屏蔽，还是加上好了。
        	httpPost.addHeader("Host", "www.bjguahao.gov.cn");
            httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
            httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
            httpPost.addHeader("Accept-Encoding", "gzip, deflate");
            httpPost.addHeader("X-Requested-With", "XMLHttpRequest");
            httpPost.addHeader("Referer", "http://www.bjguahao.gov.cn/index.htm");
            httpPost.addHeader("Connection", "keep-alive");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            
            // 模拟提交表单。
        	List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        	params.add(new BasicNameValuePair("mobileNo", mobile));
        	params.add(new BasicNameValuePair("password", password));
        	params.add(new BasicNameValuePair("yzm", ""));
        	params.add(new BasicNameValuePair("isAjax", "true"));
        	UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
        	httpPost.setEntity(entity);
        	
            // 执行 post 请求。  
            HttpResponse httpResponse = client.execute(httpPost);
            
            // 得到请求响应状态。
            StatusLine statusLine = httpResponse.getStatusLine();
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();
            
            if (statusLine != null && statusLine.getStatusCode() == 200) {
            	return true;
            } else {
            	System.out.println("系统返回状态码:" + statusLine.getStatusCode()); 
            	if (resposeEntity != null) {  
                    System.out.println("系统返回的内容:" + EntityUtils.toString(resposeEntity, "UTF-8"));  
                }  
            }
            
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
        
        return false;
	}
	
	/**
	 * 得到某医院全部的科室信息。
	 * 
	 * @param client CloseableHttpClient 
	 * @param idOfHospital 医院 ID。
	 */
	public static Map<String, Map<String, String>> getDepIdsOfHospital(CloseableHttpClient client, String idOfHospital) {
		
		Map<String, Map<String, String>> depMap = new HashMap<String, Map<String, String>>();
		
        HttpGet httpGet = new HttpGet("http://www.bjguahao.gov.cn/hp/appoint/" + idOfHospital + ".htm");  
        httpGet.setConfig(RequestConfig.DEFAULT);
        try {  
            // 执行 get 请求。  
            HttpResponse httpResponse = client.execute(httpGet);  
            
            // 得到请求响应状态。
            StatusLine statusLine = httpResponse.getStatusLine();
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();
            
            if (statusLine != null && statusLine.getStatusCode() == 200) {
            	String html = EntityUtils.toString(resposeEntity, "UTF-8");
            	Document doc = Jsoup.parse(html);
            	
            	// 得到一级分类。
            	Elements firstLevels = doc.select("div.kfyuks_yyksbox");
            	for (Element first : firstLevels) {
            		
            		Map<String, String> secondMap = new HashMap<String, String>();
            		for (Element a : first.select("a.kfyuks_islogin")) {
            			secondMap.put(a.text(), a.attr("href"));
            		}
            		
            		depMap.put(first.select("div.kfyuks_yyksdl").first().text(), secondMap);
            	}
            	
            }
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
        return depMap;
	}
	
	/**
	 * 得到某医院全部的科室信息。
	 * 
	 * @param client CloseableHttpClient 
	 * @param idOfDep 科室 ID
	 * @param week 周数
	 */
	public static Map<String, String> getTicketsOfDep(CloseableHttpClient client, String idOfDep, int week) {
		
		if (week <= 0 ) { week = 1; }
		
		
		Map<String, String> ticketMap = new HashMap<String, String>();
		
        HttpGet httpGet = new HttpGet("http://www.bjguahao.gov.cn/dpt/appoint/" + idOfDep + ".htm?week=" + week);  
        httpGet.setConfig(RequestConfig.DEFAULT);
        try {  
            // 执行 get 请求。  
            HttpResponse httpResponse = client.execute(httpGet);  
            
            // 得到请求响应状态。
            StatusLine statusLine = httpResponse.getStatusLine();
            // 获取响应消息实体。
            HttpEntity resposeEntity = httpResponse.getEntity();
            
            if (statusLine != null && statusLine.getStatusCode() == 200) {
            	String html = EntityUtils.toString(resposeEntity, "UTF-8");
            	Document doc = Jsoup.parse(html);
            	
            	Element table = doc.select("div.ksorder_cen_l_t_c").first();
            	Elements trs = table.select("tr");
            	for (int i = 1; i < trs.size(); i++) {
            		Element tr = trs.get(i);
            		for (Element td : tr.select("td.ksorder_kyy")) {
            			String tdText = td.text().trim();
            			if (tdText.startsWith("预约")) {
            				String inputValue = td.select("input").first().attr("value");
            				String date = inputValue.substring(inputValue.lastIndexOf("_") + 1, inputValue.length());
            				
            				if (i == 1) {
            					ticketMap.put(date + "(上午)", tdText);
            				}
            				
            				if (i == 2) {
            					ticketMap.put(date + "(下午)", tdText);
            				}
            				
            				if (i == 3) {
            					ticketMap.put(date + "(晚上)", tdText);
            				}
            			}
            		}
            	}
            	
            	
            }
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
        return ticketMap;
	}
	
	
//	http://www.bjguahao.gov.cn/dpt/appoint/102-200000464.htm

	public static void main(String[] args) {

//		while (true) {
//			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
//			CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  
//			try {
//				Map<String, String> ticketMap = getTicketsOfDep(closeableHttpClient, "102-200000464", 1);
//				for (Map.Entry<String, String> m : ticketMap.entrySet()) {
//					System.out.println(m.getKey() + ", " + m.getValue());
//				}
//				
//				TimeUnit.SECONDS.sleep(1);
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				if (closeableHttpClient != null) {
//					try {
//						closeableHttpClient.close();
//					} catch (IOException e) {}
//				}
//			}
//		}
		
		int[] a = {3, 2, 4};
		int target = 6;
		
		for (int aa : twoSum(a, target)) {
			System.out.println("aa = " + aa);
		}
	}
	
	public static int[] twoSum(int[] nums, int target) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	    int[] ret = new int[2];
	    for (int i = 0; i< nums.length; i++) {
	        if (map.containsKey(target-nums[i])) {
	            ret[0] = map.get(target-nums[i]);
	            ret[1] = i;
	            break;
	        }
	        map.put(nums[i], i);
	    }
	    return ret;
    }
	
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