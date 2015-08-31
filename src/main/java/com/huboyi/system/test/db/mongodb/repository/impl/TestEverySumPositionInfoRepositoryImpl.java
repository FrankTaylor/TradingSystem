package com.huboyi.system.test.db.mongodb.repository.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.Index.Duplicates;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.huboyi.system.po.EverySumPositionInfoPO;
import com.huboyi.system.test.db.TestEverySumPositionInfoRepository;

/**
 * 每一笔持仓信息DAO实现类 。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/01/01
 * @version 1.0
 */
public class TestEverySumPositionInfoRepositoryImpl implements TestEverySumPositionInfoRepository {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(TestEverySumPositionInfoRepositoryImpl.class);
	
	/** Spring Data MongoTemplate。*/
	@Resource private MongoTemplate mongoTemplate;  

	@Override
	public void createIndex (String stockCode) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke createIndex method").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		log.info(logMsg.toString());
		
		try {
			IndexOperations indexOps = mongoTemplate.indexOps(getCollectionPath(stockCode));
			
			/*
			 * 1.删除该集合上的所有索引。
			 * 原生语句：db.test_everySumPositionInfo_123456.dropIndexes();
			 */
			indexOps.dropAllIndexes();
			
			/*
			 * 2.在该集合上建立复合索引，且该索引还是唯一和稀疏的。
			 * 原生语句：db.test_everySumPositionInfo_123456.ensureIndex({"openDate" : 1, "openTime" : 1}, {"unique" : true, "dropDups" : true, "sparse" : true, "name" : "openDate@openTime", "background" : 1})
			 */
			String indexName = "openDate@openTime";
			indexOps.ensureIndex(
					new Index()
					.on("openDate", Direction.ASC)
					.on("openTime", Direction.ASC)
					.unique(Duplicates.DROP)
					.sparse()
					.named(indexName)
					.background()
			);
			
			/*
			 * 3.验证索引是否建立成功。
			 * 原生语句：db.test_everySumPositionInfo_123456.getIndexes();
			 */
			boolean isSuccessEnsureIndex = false;
			List<IndexInfo> indexInfoList = indexOps.getIndexInfo();
			for (IndexInfo info : indexInfoList) {
				if (info.getName().equals(indexName)) {
					isSuccessEnsureIndex = true;
				}
			}
			if (isSuccessEnsureIndex) {				
				log.info("创建  [证券代码 = " + stockCode + "] 每一笔持仓记录的索引成功。");
			} else {
				throw new RuntimeException();
			}
			
		} catch (Exception e) {
			String errorMsg = "创建  [证券代码 = " + stockCode + "] 每一笔持仓记录的索引失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public void insert (EverySumPositionInfoPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke insert method").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());
		
		try {			
			mongoTemplate.insert(po, getCollectionPath(po.getStockCode()));
			log.info("插入 [证券代码 = " + po.getStockCode() + "] 的每一笔持仓记录成功。");
		} catch (Exception e) {
			String errorMsg = "插入 [证券代码 = " + po.getStockCode() + "] 的每一笔持仓记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public EverySumPositionInfoPO findNewOne (String stockCode) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke findNewOne method").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		log.info(logMsg.toString());
		
		try {
			/*
			 * 原生语句：db.test_everySumPositionInfo_123456.find().sort({openDate : -1, openTime : -1}).limit(1);
			 */
			Query query = new Query();
			query.with(new Sort(Direction.DESC, "openDate"));
			query.with(new Sort(Direction.DESC, "openTime"));
			query.limit(1);
			
			List<EverySumPositionInfoPO> poList = mongoTemplate.find(query, EverySumPositionInfoPO.class, getCollectionPath(stockCode));
			
			log.info("查询  [证券代码 = " + stockCode + "] 最近的每一笔持仓记录成功。");
			
			if (poList != null && poList.size() == 1) {
				return poList.get(0);
			} else {
				return null;
			}			
		} catch (Throwable e) {
			String errorMsg = "查询 [证券代码 = " + stockCode + "] 最近的每一笔持仓记录成功失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public List<EverySumPositionInfoPO> findEverySumPositionInfoList (String stockCode, String openContractCode, Integer beginOpenDate, Integer endOpenDate, String isClose, Integer beginPage, Integer endPage) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke findEverySumPositionInfoList method").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]").append("\n");
		logMsg.append("@param [openContractCode = " + openContractCode + "]").append("\n");
		logMsg.append("@param [beginOpenDate = " + beginOpenDate + "]").append("\n");
		logMsg.append("@param [endOpenDate = " + endOpenDate + "]").append("\n");
		logMsg.append("@param [isClose = " + isClose + "]").append("\n");
		logMsg.append("@param [beginPage = " + beginPage + "]").append("\n");
		logMsg.append("@param [endPage = " + endPage + "]").append("\n");
		log.info(logMsg.toString());
		
		try {
			/*
			 * 原生语句：
			 * db.test_everySumPositionInfo_123456
			 * .find({"openContractCode" : "000518", "beginOpenDate" : {"$gte" : 20100101, "$lte" : 20151212}, "closeContractCode" : {"$ne" : "no"}})
			 * .sort({openDate : 1, openTime : 1})
			 * .hint('openDate@openTime')
			 * .skip(10)
			 * .limit(10);
			 */
			
			Query query = new Query();
			
			if (openContractCode != null) {
				query.addCriteria(Criteria.where("openContractCode").is(openContractCode));
			}
			
			if (beginOpenDate != null && endOpenDate != null) {
				query.addCriteria(Criteria.where("openDate").gte(beginOpenDate).lte(endOpenDate));
			} else {
				if (beginOpenDate != null) {				
					query.addCriteria(Criteria.where("openDate").gte(beginOpenDate));
				}
				if (endOpenDate != null) {				
					query.addCriteria(Criteria.where("openDate").lte(endOpenDate));
				}
			}
			
			if (isClose != null) {
				if (isClose.equals("0")) {					
					query.addCriteria(Criteria.where("closeContractCode").is("no"));
				}
				if (isClose.equals("1")) {					
					query.addCriteria(Criteria.where("closeContractCode").ne("no"));
				}
			}
						
			query.with(new Sort(Direction.ASC, "openDate"));
			query.with(new Sort(Direction.ASC, "openTime"));
			query.withHint("openDate@openTime");
			
			if (beginPage != null) {
				query.skip(beginPage);
			}
			
			if (endPage != null) {
				query.limit(endPage);
			}
			
			log.info("按照条件查询 [证券代码 = " + stockCode + "] 的每一笔持仓记录成功。");
			
			return mongoTemplate.find(query, EverySumPositionInfoPO.class, getCollectionPath(stockCode));
		} catch (Exception e) {
			String errorMsg = "按照条件查询 [证券代码 = " + stockCode + "] 的每一笔持仓记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public void update (EverySumPositionInfoPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke update method").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());
		
		try {
			/*
			 * 我之前用的是这条原生语句，现在用_id来修改了，应该可以提升速度。
			 * 原生语句：db.test_everySumPositionInfo_123456.update({"openContractCode" : "879877-98987", "stockCode" : "123456"}, {$set : {"stockNumber" : 100}});
			 */
			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(po.getId()));
			
			Update update = new Update();
			if (po.getCanCloseNumber() != null) { update.set("canCloseNumber", po.getCanCloseNumber()); }             // 可平仓数量。
			if (po.getStopPrice() != null) { update.set("stopPrice", po.getStopPrice()); }                            // 止损价格。
			if (po.getCloseContractCode() != null) { update.set("closeContractCode", po.getCloseContractCode()); }    // 平仓合同编号。
			if (po.getSystemClosePoint() != null) { update.set("systemClosePoint", po.getSystemClosePoint()); }       // 系统平仓点。
			if (po.getCloseSignalTime() != null) { update.set("closeSignalTime", po.getCloseSignalTime()); }          // 平仓信号发出时间。
			if (po.getCloseDate() != null) { update.set("closeDate", po.getCloseDate()); }                            // 平仓日期（格式：%Y%m%d）。
			if (po.getCloseTime() != null) { update.set("closeTime", po.getCloseTime()); }                            // 平仓时间（格式：HH:mm:ss）。
			if (po.getClosePrice() != null) { update.set("closePrice", po.getClosePrice()); }                         // 平仓价格。
			if (po.getCloseNumber() != null) { update.set("closeNumber", po.getCloseNumber()); }                      // 平仓数量。
			if (po.getNewPrice() != null) {update.set("newPrice", po.getNewPrice()); }                                // 当前价。
			if (po.getNewMarketValue() != null) { update.set("newMarketValue", po.getNewMarketValue()); }             // 最新市值。
			if (po.getFloatProfitAndLoss() != null) {update.set("floatProfitAndLoss", po.getFloatProfitAndLoss()); }  // 浮动盈亏。
			if (po.getProfitAndLossRatio() != null) { update.set("profitAndLossRatio", po.getProfitAndLossRatio()); } // 盈亏比例。

			mongoTemplate.updateFirst(query, update, getCollectionPath(po.getStockCode()));
			
			log.info("修改 [证券代码 = " + po.getStockCode() + "] 的每一笔持仓记录成功。");
		} catch (Exception e) {
			String errorMsg = "修改 [证券代码 = " + po.getStockCode() + "] 的每一笔持仓记录失败!";
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
			/*
			 * 原生语句：db.test_everySumPositionInfo_123456.drop();
			 */
			mongoTemplate.dropCollection(getCollectionPath(stockCode));
			log.info("删除  [证券代码 = " + stockCode + "] 用于每一笔持仓记录的集合成功。");
		} catch (Exception e) {
			String errorMsg = "删除  [证券代码 = " + stockCode + "] 用于每一笔持仓记录的集合失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	/**
	 * 得到操作集合路径。
	 * 
	 * @param stockCode 证券代码
	 * @return String
	 */
	private String getCollectionPath (String stockCode) {
		return "test_everySumPositionInfo_" + stockCode;
	}
}