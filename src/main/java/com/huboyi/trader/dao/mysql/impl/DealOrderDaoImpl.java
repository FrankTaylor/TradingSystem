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

import com.huboyi.trader.dao.DealOrderDao;
import com.huboyi.trader.entity.po.DealOrderPO;

/**
 * 交易单 PO 实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.2
 */
@Repository("dealOrderDao")
public class DealOrderDaoImpl implements DealOrderDao {

	/** 日志。*/
	private final Logger log = Logger.getLogger(DealOrderDaoImpl.class);
	
	@Autowired
	@Qualifier("npJdbcTemplate")
	private NamedParameterJdbcTemplate npJdbcTemplate;
	
	@Override
	public void insert(DealOrderPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 insert 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO deal_order ");
		sql.append(" ( ");
		sql.append("deal_order_code, entrust_order_code, ");
		sql.append("deal_type, ");
		sql.append("stock_code, stock_name, ");
		sql.append("trade_date, trade_price, trade_volume, turnover, ");
		sql.append("stockholder, ");
		sql.append("create_time, ");
		sql.append(" ) "); 
		
		sql.append(" VALUES ");
		sql.append(" ( ");
		sql.append(":dealOrderCode, :entrustOrderCode, ");
		sql.append(":dealType, ");
		sql.append(":stockCode, :stockName, ");
		sql.append(":tradeDate, :tradePrice, :tradeVolume, :turnover, ");
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
		sql.append("TRUNCATE deal_order");
		
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
		sql.append("DELETE FROM deal_order WHERE stock_code = :stockCode AND stockholder = :stockholder");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockCode", stockCode);
		paramMap.put(":stockholder", stockholder);
		
		npJdbcTemplate.update(sql.toString(), paramMap);
	}
	
	@Override
	public DealOrderPO findLastOne(String stockCode, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findLastOne 方法").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM deal_order WHERE stock_code = :stockCode AND stockholder = :stockholder ");
		sql.append("ORDER BY trade_date DESC LIMIT 1");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockCode", stockCode);
		paramMap.put(":stockholder", stockholder);

		return npJdbcTemplate.queryForObject(sql.toString(), paramMap, DealOrderPO.class);
	}

	@Override
	public List<DealOrderPO> findAll(String stockCode, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findAll 方法").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM deal_order WHERE stock_code = :stockCode AND stockholder = :stockholder ");
		sql.append("ORDER BY trade_date ASC");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockCode", stockCode);
		paramMap.put(":stockholder", stockholder);
		
		return npJdbcTemplate.queryForList(sql.toString(), paramMap, DealOrderPO.class);
	}
}