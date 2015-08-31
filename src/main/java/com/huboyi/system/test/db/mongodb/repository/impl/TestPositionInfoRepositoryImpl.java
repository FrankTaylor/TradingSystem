package com.huboyi.system.test.db.mongodb.repository.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.IndexOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.Index.Duplicates;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.huboyi.system.po.PositionInfoPO;
import com.huboyi.system.test.db.TestPositionInfoRepository;

/**
 * 持仓信息DAO实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/12/29
 * @version 1.0
 */
public class TestPositionInfoRepositoryImpl implements TestPositionInfoRepository {

	/** 日志。*/
	private final Logger log = Logger.getLogger(TestPositionInfoRepositoryImpl.class);
	
	/** Spring Data MongoTemplate。*/
	@Resource private MongoTemplate mongoTemplate;  
	
	@Override
	public void createIndex (String stockCode) {
		try {
			IndexOperations indexOps = mongoTemplate.indexOps(getCollectionPath());
			
			/*
			 * 1.删除该集合上的所有索引。
			 * 原生语句：db.test_positionInfo.dropIndexes();
			 */
			indexOps.dropAllIndexes();
			
			/*
			 * 2.在该集合上建立复合索引，且该索引还是唯一和稀疏的。
			 * 原生语句：db.test_positionInfo.ensureIndex({"stockCode" : 1}, {"unique" : true, "dropDups" : true, "sparse" : true, "name" : "stockCode", "background" : 1})
			 */
			String indexName = "stockCode";
			indexOps.ensureIndex(
					new Index()
					.on("stockCode", Direction.ASC)
					.unique(Duplicates.DROP)
					.sparse()
					.named(indexName)
					.background()
			);
			
			/*
			 * 3.验证索引是否建立成功。
			 * 原生语句：db.test_positionInfo.getIndexes();
			 */
			boolean isSuccessEnsureIndex = false;
			List<IndexInfo> indexInfoList = indexOps.getIndexInfo();
			for (IndexInfo info : indexInfoList) {
				if (info.getName().equals(indexName)) {
					isSuccessEnsureIndex = true;
				}
			}
			if (isSuccessEnsureIndex) {				
				log.info("创建持仓信息记录的索引成功。");
			} else {
				throw new RuntimeException();
			}
			
		} catch (Exception e) {
			String errorMsg = "创建持仓信息记录的索引失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public void insert (PositionInfoPO po) {
		try {			
			mongoTemplate.insert(po, getCollectionPath());
			log.info("插入 [证券代码 = " + po.getStockCode() + "] 的持仓信息记录成功。");
		} catch (Exception e) {
			String errorMsg = "插入 [证券代码 = " + po.getStockCode() + "] 的持仓信息记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}

	@Override
	public List<PositionInfoPO> findAll () {
		try {
			/*
			 * 原生语句：db.test_positionInfo.find();
			 */
			
			log.info("查询所有的持仓信息记录成功。");
			
			return mongoTemplate.findAll(PositionInfoPO.class, getCollectionPath());
		} catch (Exception e) {
			String errorMsg = "查询所有的持仓信息记录记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}

	@Override
	public void update (PositionInfoPO po) {
		try {
			/*
			 * 原生语句：db.test_prositionInfo.update({"stockCode" : "123456"}, {$set : {"stockNumber" : 100}});
			 */
			Criteria criteria = Criteria.where("stockCode").is(po.getStockCode());
			Query query = new Query();
			query.addCriteria(criteria);

			Update update = new Update();
			if (po.getStockNumber() != null) { update.set("stockNumber", po.getStockNumber()); }                      // 证券数量。
			if (po.getCanSellNumber() != null) { update.set("canSellNumber", po.getCanSellNumber()); }                // 可卖数量。
			if (po.getCostPrice() != null) { update.set("costPrice", po.getCostPrice()); }                            // 成本价。
			if (po.getCostMoney() != null) { update.set("costMoney", po.getCostMoney()); }                            // 成本金额。
			if (po.getNewPrice() != null) { update.set("newPrice", po.getNewPrice()); }                               // 当前价。
			if (po.getNewMarketValue() != null) { update.set("newMarketValue", po.getNewMarketValue()); }             // 最新市值。
			if (po.getFloatProfitAndLoss() != null) { update.set("floatProfitAndLoss", po.getFloatProfitAndLoss()); } // 浮动盈亏。
			if (po.getProfitAndLossRatio() != null) { update.set("profitAndLossRatio", po.getProfitAndLossRatio()); } // 盈亏比例。
			if (po.getTodayBuyNumber() != null) {update.set("todayBuyNumber", po.getTodayBuyNumber()); }              // 今买数量。
			if (po.getTodaySellNumber() != null) { update.set("todaySellNumber", po.getTodaySellNumber()); }          // 今卖数量。
			
			mongoTemplate.updateMulti(query, update, getCollectionPath());
			
			log.info("修改 [证券代码 = " + po.getStockCode() + "] 的持仓信息记录成功。");
		} catch (Exception e) {
			String errorMsg = "修改 [证券代码 = " + po.getStockCode() + "] 的持仓信息记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}

	@Override
	public void removeByStockCode (String stockCode) {
		try {
			/*
			 * 原生语句：db.test_positionInfo.remove({"stockCode" : "123456"});
			 */
			Criteria criteria = Criteria.where("stockCode").is(stockCode);
			Query query = new Query();
			query.addCriteria(criteria);
			
			mongoTemplate.remove(query, getCollectionPath());
			log.info("删除 [证券代码 = " + stockCode + "] 的持仓信息记录成功。");
		} catch (Exception e) {
			String errorMsg = "删除 [证券代码 = " + stockCode + "] 的持仓信息记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public void removeForNoStockNumber () {
		try {
			/*
			 * 原生语句：db.test_positionInfo.remove({"stockNumber" : 0, "canSellNumber" : 0});
			 */
			Criteria criteria_1 = Criteria.where("stockNumber").is(0);
			Criteria criteria_2 = Criteria.where("canSellNumber").is(0);
			
			Query query = new Query();
			query.addCriteria(criteria_1);
			query.addCriteria(criteria_2);
			
			mongoTemplate.remove(query, getCollectionPath());
			log.info("删除没有仓位的持仓信息记录成功。");
		} catch (Exception e) {
			String errorMsg = "删除没有仓位的持仓信息记录失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	@Override
	public void dropCollection () {
		try {
			/*
			 * 原生语句：db.test_positionInfo.drop();
			 */
			mongoTemplate.dropCollection(getCollectionPath());
			log.info("删除用于记录持仓信息的集合成功。");
		} catch (Exception e) {
			String errorMsg = "删除用于记录持仓信息的集合失败!";
			log.error(errorMsg, e);
			throw new RuntimeException(errorMsg, e);
		}
	}
	
	/**
	 * 得到操作集合路径。
	 * 
	 * @return String
	 */
	private String getCollectionPath () {
		return "test_positionInfo";
	}
}