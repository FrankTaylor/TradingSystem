package com.huboyi.position.repository.mysql.impl;

import java.math.BigDecimal;
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
import com.huboyi.position.entity.po.OrderInfoPO;
import com.huboyi.position.repository.OrderInfoRepository;

/**
 * 订单信息Repository实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
@Repository
public class OrderInfoRepositoryImpl implements OrderInfoRepository {

	/** 日志。*/
	private final Logger log = Logger.getLogger(OrderInfoRepositoryImpl.class);
	
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Override
	public void insert(OrderInfoPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 insert 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("INSERT INTO order_info ");
		
		sqlBuilder.append(" ( ");
		sqlBuilder.append("contract_code, ");
		sqlBuilder.append("stock_code, stock_name, trade_date, trade_flag, trade_price, trade_number, trade_money, ");
		sqlBuilder.append("stockholder");
		sqlBuilder.append(" ) "); 
		
		sqlBuilder.append(" VALUES ");
		sqlBuilder.append(" ( ");
		sqlBuilder.append(":contractCode, ");
		sqlBuilder.append(":stockCode, :stockName, :tradeDate, :tradeFlag, :tradePrice, :tradeNumber, :tradeMoney, ");
		sqlBuilder.append(":stockholder");
		sqlBuilder.append(" ) "); 
		
		log.info("执行的 sql 语句 -> " + sqlBuilder);
		
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(po);
		
		namedParameterJdbcTemplate.update(sqlBuilder.toString(), paramSource);
	}

	@Override
	public void truncate() {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 truncate 方法").append("\n");
		log.info(logMsg.toString());

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("truncate order_info");
		
		log.info("执行的 sql 语句 -> " + sqlBuilder);
		
		namedParameterJdbcTemplate.getJdbcOperations().update(sqlBuilder.toString());
	}

	@Override
	public OrderInfoPO findLastOne(String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findLastOne 方法").append("\n");
		log.info(logMsg.toString());
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT * FROM order_info WHERE stockholder = :stockholder ORDER BY trade_date DESC LIMIT 1");
		
		log.info("执行的 sql 语句 -> " + sqlBuilder);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);

		return namedParameterJdbcTemplate.queryForObject(sqlBuilder.toString(), paramMap, OrderInfoPO.class);
	}

	@Override
	public List<OrderInfoPO> findAll(String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findAll 方法").append("\n");
		log.info(logMsg.toString());
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT * FROM order_info WHERE stockholder = :stockholder ORDER BY trade_date ASC");
		
		log.info("执行的 sql 语句 -> " + sqlBuilder);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);
		
		
		return namedParameterJdbcTemplate.queryForList(sqlBuilder.toString(), paramMap, OrderInfoPO.class);
	}
	
}