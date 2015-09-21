package com.huboyi.system.test.db.redis.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import com.huboyi.system.po.OrderInfoPO;
import com.huboyi.system.test.db.TestOrderInfoRepository;

/**
 * 订单信息DAO实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/03/25
 * @version 1.0
 */
public class TestOrderInfoRepositoryImpl extends RedisTemplate<String, OrderInfoPO> implements TestOrderInfoRepository {
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestOrderInfoRepositoryImpl.class);

	@Override
	public void createIndex(String stockCode) {
		log.warn("当前数据库是Redis不用创建索引。");
	}
	
	@Override
	public void insert (OrderInfoPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke insert method").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());
		
		try {
			
			// --- 把记录保存到Redis。
			po.setId(UUID.randomUUID().toString() + "-" + TestOrderInfoRepository.class.getName());
			opsForList().rightPush(getListKey(po.getStockCode()), po);
			
			log.info("插入 [证券代码 = " + po.getStockCode() + "] 的订单信息记录成功。");
		} catch (Throwable e) {
			String errorMsg = "插入 [证券代码 = " + po.getStockCode() + "] 的订单信息记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public OrderInfoPO findNewOne(String stockCode) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke findNewOne method").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		log.info(logMsg.toString());
		
		try {

			// --- 查询Redis。
			List<OrderInfoPO> poList = findOrderInfoList(stockCode, null, null, null, null);
			if (poList == null || poList.isEmpty()) {
				return null;
			}
			
			log.info("查询  [证券代码 = " + stockCode + "] 最近的一条订单记录成功。");
			return poList.get(poList.size() - 1);
		} catch (Throwable e) {
			String errorMsg = "查询 [证券代码 = " + stockCode + "] 最近的一条订单记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public List<OrderInfoPO> findOrderInfoList (String stockCode, Integer beginTradeDate, Integer endTradeDate, Integer beginPage, Integer endPage) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke findFundsFlowList method").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]").append("\n");
		logMsg.append("@param [beginTradeDate = " + beginTradeDate + "]").append("\n");
		logMsg.append("@param [endTradeDate = " + endTradeDate + "]").append("\n");
		logMsg.append("@param [beginPage = " + beginPage + "]").append("\n");
		logMsg.append("@param [endPage = " + endPage + "]").append("\n");
		log.info(logMsg.toString());
		
		try {

			// --- 查询Redis。
			List<OrderInfoPO> poList = opsForList().range(getListKey(stockCode), 0, -1);
			
			// --- 由于Redis没有其他数据库中的排序功能，这里需要自己实现按照trade_date升序。
			Collections.sort(poList, new Comparator<OrderInfoPO>() {
				@Override
				public int compare(OrderInfoPO o1, OrderInfoPO o2) {
					return (o1.getTradeDate() > o2.getTradeDate()) ? 1    :
						   (o1.getTradeDate() < o2.getTradeDate()) ? -1   :
				           0;
				}
			});
			
			// --- 把符合时间范围的记录载入集合。
			List<OrderInfoPO> resultList = new ArrayList<OrderInfoPO>();
			for (OrderInfoPO po : poList) {
				
				if (beginTradeDate != null && po.getTradeDate() < beginTradeDate) {
					continue;
				}
				if (endTradeDate != null && po.getTradeDate() > endTradeDate) {
					continue;
				}
				
				resultList.add(po);
			}
			
			// --- 对集合进行截取范围操作。
			int fromIndex = (beginPage != null && beginPage > 0 && resultList.size() > beginPage) ? beginPage : 0;
			int toIndex = (endPage != null && endPage > 0 && (resultList.size() - fromIndex) >= endPage) ? (endPage + fromIndex) : resultList.size();

			log.info("按照条件查询 [证券代码 = " + stockCode + "] 的订单信息记录成功。");
			return resultList.subList(fromIndex, toIndex);
		} catch (Throwable e) {
			String errorMsg = "按照条件查询 [证券代码 = " + stockCode + "] 的订单信息记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public void dropCollection (String stockCode) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke dropCollection method").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		log.info(logMsg.toString());
		
		try {
			// --- 把记录从Redis中删除。
			delete(getListKey(stockCode));
			log.info("删除  [证券代码 = " + stockCode + "] 用于记录订单信息的集合成功。");
		} catch (Throwable e) {
			String errorMsg = "删除  [证券代码 = " + stockCode + "] 用于记录订单信息的集合失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	/**
	 * 得到集合键值。
	 * 
	 * @param stockCode 证券代码
	 * @return String
	 */
	private String getListKey (String stockCode) {
		return "test" + ":" + "orderInfo" + ":" + stockCode;
	}
}