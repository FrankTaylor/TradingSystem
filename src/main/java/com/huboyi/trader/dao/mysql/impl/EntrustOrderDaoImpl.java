package com.huboyi.trader.dao.mysql.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.huboyi.trader.dao.EntrustOrderDao;
import com.huboyi.trader.entity.po.EntrustOrderPO;

/**
 * 委托单 PO 实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.2
 */
@Repository("entrustOrderDao")
public class EntrustOrderDaoImpl implements EntrustOrderDao {

	/** 日志。*/
	private final Logger log = Logger.getLogger(EntrustOrderDaoImpl.class);
	
	@Autowired
	@Qualifier("npJdbcTemplate")
	private NamedParameterJdbcTemplate npJdbcTemplate;
	
	@Override
	public void insert(EntrustOrderPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 insert 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO entrust_order ");
		sql.append(" ( ");
		sql.append("entrust_order_code, cancel_entrust_order_code, ");
		sql.append("deal_type, deal_status, quote_type, ");
		sql.append("stock_code, stock_name, ");
		sql.append("entrust_date, entrust_price, entrust_volume, trade_volume, ");
		sql.append("stockholder, ");
		sql.append("create_time, ");
		sql.append(" ) "); 
		
		sql.append(" VALUES ");
		sql.append(" ( ");
		sql.append(":entrustOrderCode, :cancelEntrustOrderCode, ");
		sql.append(":dealType, :dealStatus, :quoteType, ");
		sql.append(":stockCode, :stockName, ");
		sql.append(":entrustDate, :entrustPrice, :entrustVolume, :tradeVolume, ");
		sql.append(":stockholder, ");
		sql.append(":createTime, ");
		sql.append(" ) "); 
		
		log.info("执行的 sql 语句 -> " + sql);
		
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(po);
		
		npJdbcTemplate.update(sql.toString(), paramSource);
	}

	@Override
	public void truncate() {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 truncate 方法").append("\n");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("TRUNCATE entrust_order");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		npJdbcTemplate.getJdbcOperations().update(sql.toString());
	}
	
	@Override
	public void delete(String stockCode, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 delete 方法").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM entrust_order WHERE stock_code = :stockCode AND stockholder = :stockholder");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockCode", stockCode);
		paramMap.put(":stockholder", stockholder);
		
		npJdbcTemplate.update(sql.toString(), paramMap);
	}
	
	@Override
	public EntrustOrderPO findLastOne(String stockCode, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findLastOne 方法").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM entrust_order WHERE stock_code = :stockCode AND stockholder = :stockholder ");
		sql.append("ORDER BY trade_date DESC LIMIT 1");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockCode", stockCode);
		paramMap.put(":stockholder", stockholder);

		return npJdbcTemplate.queryForObject(sql.toString(), paramMap, EntrustOrderPO.class);
	}

	@Override
	public List<EntrustOrderPO> findAll(String stockCode, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findAll 方法").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM entrust_order WHERE stock_code = :stockCode AND stockholder = :stockholder ");
		sql.append("ORDER BY trade_date ASC");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockCode", stockCode);
		paramMap.put(":stockholder", stockholder);
		
		return npJdbcTemplate.queryForList(sql.toString(), paramMap, EntrustOrderPO.class);
	}
}