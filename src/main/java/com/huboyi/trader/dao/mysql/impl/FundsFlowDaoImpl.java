package com.huboyi.trader.dao.mysql.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.huboyi.trader.dao.FundsFlowDao;
import com.huboyi.trader.entity.po.FundsFlowPO;

/**
 * 资金流水 Dao 实现类。
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.2
 */
@Repository("fundsFlowDao")
public class FundsFlowDaoImpl implements FundsFlowDao {

	/** 日志。*/
	private final Logger log = Logger.getLogger(FundsFlowDaoImpl.class);
	
	@Autowired
	@Qualifier("npJdbcTemplate")
	private NamedParameterJdbcTemplate npJdbcTemplate;
	
	@Override
	public void insert(FundsFlowPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 insert 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO funds_flow ");
		
		sql.append(" ( ");
		sql.append("funds_flow_code, deal_order_code, ");
		sql.append("business_type, ");
		sql.append("currency_type, ");
		sql.append("stock_code, stock_name, ");
		sql.append("trade_date, trade_price, trade_volume, ");
		sql.append("charges, stamp_duty, transfer_fee, clearing_fee, ");
		sql.append("turnover, funds_balance, ");
		sql.append("stockholder, ");
		sql.append("create_time");
		sql.append(" ) "); 
		
		sql.append(" VALUES ");
		sql.append(" ( ");
		sql.append(":fundsFlowCode, :dealOrderCode, ");
		sql.append(":businessType, ");
		sql.append(":currencyType, ");
		sql.append(":stockCode, :stockName, ");
		sql.append(":tradeDate, :tradePrice, :tradeVolume, ");
		sql.append(":charges, :stampDuty, :transferFee, :clearingFee, ");
		sql.append(":turnover, :fundsBalance, ");
		sql.append(":stockholder, ");
		sql.append(":createTime");
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
		sql.append("TRUNCATE funds_flow");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		npJdbcTemplate.getJdbcOperations().update(sql.toString());
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
		
		npJdbcTemplate.update(sql.toString(), paramMap);
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

		return npJdbcTemplate.queryForObject(sql.toString(), paramMap, FundsFlowPO.class);
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
		
		
		return npJdbcTemplate.queryForList(sql.toString(), paramMap, FundsFlowPO.class);
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		final SynchronousQueue<String> q = new SynchronousQueue<String>();
		
		for (int i = 0; i < 10; i++) {			
			new Thread(new Runnable() {
				public void run() {
					try {
						System.out.println("线程 ：" + Thread.currentThread() + ", " + q.take());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		TimeUnit.SECONDS.sleep(2);
		
		Thread.currentThread().isInterrupted()
	}
}