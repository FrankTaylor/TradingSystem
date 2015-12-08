package com.huboyi.position.repository.mysql.impl;

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

import com.huboyi.position.entity.po.FundsFlowPO;
import com.huboyi.position.repository.FundsFlowRepository;

/**
 * 资金流水Repository的实现类。
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
@Repository
public class FundsFlowRepositoryImpl implements FundsFlowRepository {

	/** 日志。*/
	private final Logger log = Logger.getLogger(FundsFlowRepositoryImpl.class);
	
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Override
	public void insert(FundsFlowPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 insert 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO funds_flow ");
		
		sql.append(" ( ");
		sql.append("contract_code, currency, ");
		sql.append("stock_code, stock_name, trade_date, trade_price, trade_number, trade_money, funds_balance, ");
		sql.append("business_name, ");
		sql.append("charges, stamp_duty, transfer_fee, clearing_fee, ");
		sql.append("stockholder");
		sql.append(" ) "); 
		
		sql.append(" VALUES ");
		sql.append(" ( ");
		sql.append(":contractCode, :currency, ");
		sql.append(":stockCode, :stockName, :tradeDate, :tradePrice, :tradeNumber, :tradeMoney, :fundsBalance, ");
		sql.append(":businessName, ");
		sql.append(":charges, :stampDuty, :transferFee, :clearingFee, ");
		sql.append(":stockholder");
		sql.append(" ) "); 
		
		log.info("执行的 sql 语句 -> " + sql);
		
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(po);
		
		namedParameterJdbcTemplate.update(sql.toString(), paramSource);
	}

	@Override
	public void truncate() {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 truncate 方法").append("\n");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("TRUNCATE funds_flow");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		namedParameterJdbcTemplate.getJdbcOperations().update(sql.toString());
	}
	
	@Override
	public void delete(String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 delete 方法").append("\n");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM funds_flow WHERE stockholder = :stockholder");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);
		
		namedParameterJdbcTemplate.update(sql.toString(), paramMap);
	}
	
	@Override
	public FundsFlowPO findLastOne(String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findLastOne 方法").append("\n");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM funds_flow WHERE stockholder = :stockholder ORDER BY trade_date DESC LIMIT 1");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);

		return namedParameterJdbcTemplate.queryForObject(sql.toString(), paramMap, FundsFlowPO.class);
	}

	@Override
	public List<FundsFlowPO> findAll(String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findAll 方法").append("\n");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM funds_flow WHERE stockholder = :stockholder ORDER BY trade_date ASC");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);
		
		
		return namedParameterJdbcTemplate.queryForList(sql.toString(), paramMap, FundsFlowPO.class);
	}
}