package com.huboyi.trader.repository.mysql.impl;

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

import com.huboyi.trader.entity.po.PositionInfoPO;
import com.huboyi.trader.repository.PositionInfoRepository;
import com.huboyi.trader.service.PositionInfoService.SortType;

/**
 * 持仓信息Repository的实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
@Repository("positionInfoRepository")
public class PositionInfoRepositoryImpl implements PositionInfoRepository {

	/** 日志。*/
	private final Logger log = Logger.getLogger(PositionInfoRepositoryImpl.class);
	
	@Autowired
	@Qualifier("namedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Override
	public void insert(PositionInfoPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 insert 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO position_info ");
		sql.append(" ( ");
		sql.append("stock_code, stock_name, stock_number, can_sell_number, ");
		sql.append("cost_price, cost_money, ");
		sql.append("new_price, new_market_value, float_profit_and_loss, profit_and_loss_ratio, ");
		sql.append("todayBuyNumber, todaySellNumber, ");
		sql.append("stockholder");
		sql.append(" ) "); 
		
		sql.append(" VALUES ");
		sql.append(" ( ");
		sql.append(":stockCode, :stockName, :stockNumber, :canSellNumber, ");
		sql.append(":costPrice, :costMoney, ");
		sql.append(":newPrice, :newMarketValue, :floatProfitAndLoss, :profitAndLossRatio, ");
		sql.append(":todayBuyNumber, :todaySellNumber, ");
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
		sql.append("TRUNCATE position_info");
		
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
		sql.append("DELETE FROM position_info WHERE stockholder = :stockholder");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);
		
		namedParameterJdbcTemplate.update(sql.toString(), paramMap);
	}

	@Override
	public void delete(String stockholder, String stockCode) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 delete 方法").append("\n");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM position_info WHERE stockholder = :stockholder AND stock_code = :stockCode");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);
		paramMap.put(":stockholder", stockCode);
		
		namedParameterJdbcTemplate.update(sql.toString(), paramMap);
	}

	@Override
	public void update(PositionInfoPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 update 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE position_info SET ");
		
		// --- 
		if (po.getStockCode() != null) { sql.append("stock_code = :stockCode, "); }
		if (po.getStockName() != null) { sql.append("stock_name = :stockName, "); }
		if (po.getStockNumber() != null) { sql.append("stock_number = :stockNumber, "); }
		if (po.getCanSellNumber() != null) { sql.append("can_sell_number = :canSellNumber, "); }
		
		// ---
		if (po.getCostPrice() != null) { sql.append("cost_price = :costPrice, "); }
		if (po.getCostMoney() != null) { sql.append("cost_money = :costMoney, "); }
		
		// ---
		if (po.getNewPrice() != null) { sql.append("new_price = :newPrice, "); }
		if (po.getNewMarketValue() != null) { sql.append("new_market_value = :newMarketValue, "); }
		if (po.getFloatProfitAndLoss() != null) { sql.append("float_profit_and_loss = :floatProfitAndLoss, "); }
		if (po.getProfitAndLossRatio() != null) { sql.append("profit_and_loss_ratio = :profitAndLossRatio, "); }
		
		// ---
		if (po.getTodayBuyNumber() != null) { sql.append("today_buy_number = :todayBuyNumber, "); }
		if (po.getTodaySellNumber() != null) { sql.append("today_sell_number = :todaySellNumber, "); }

		// ---
		if (po.getStockholder() != null) { sql.append("stockholder = :stockholder, "); }
		
		// 去掉sql语句末尾的“,”
		sql = sql.deleteCharAt((sql.length() - 1));
		sql.append(" WHERE id = :id ");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(po);
		
		namedParameterJdbcTemplate.update(sql.toString(), paramSource);
	}

	@Override
	public PositionInfoPO findOne(String stockholder, String stockCode) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findOne 方法").append("\n");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM position_info WHERE stockholder = :stockholder AND stock_code = :stockCode");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);
		paramMap.put(":stockCode", stockCode);
		
		return namedParameterJdbcTemplate.queryForObject(sql.toString(), paramMap, PositionInfoPO.class);
	}
	
	@Override
	public List<PositionInfoPO> findAll(String stockholder, SortType sortType) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findAll 方法").append("\n");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		logMsg.append("@param [sortType = " + sortType + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM position_info WHERE stockholder = :stockholder ");
		
		if (sortType != null) {
			sql.append("ORDER BY ");
			
			if (sortType == SortType.CAN_SELL_NUMBER_ASC) {
				sql.append("can_sell_number ASC");
			}
			if (sortType == SortType.CAN_SELL_NUMBER_DESC) {
				sql.append("can_sell_number DESC");
			}
			
			if (sortType == SortType.COST_MONEY_ASC) {
				sql.append("cost_money ASC");
			}
			if (sortType == SortType.COST_MONEY_DESC) {
				sql.append("cost_money DESC");
			}
			
			if (sortType == SortType.NEW_MARKET_VALUE_ASC) {
				sql.append("new_market_value ASC");
			}
			if (sortType == SortType.NEW_MARKET_VALUE_DESC) {
				sql.append("new_market_value DESC");
			}
			
			if (sortType == SortType.FLOAT_PROFIT_AND_LOSS_ASC) {
				sql.append("float_profit_and_loss ASC");
			}
			if (sortType == SortType.FLOAT_PROFIT_AND_LOSS_DESC) {
				sql.append("float_profit_and_loss DESC");
			}
			
			if (sortType == SortType.PROFIT_AND_LOSS_RATIO_ASC) {
				sql.append("profit_and_loss_ratio ASC");
			}
			if (sortType == SortType.PROFIT_AND_LOSS_RATIO_DESC) {
				sql.append("profit_and_loss_ratio DESC");
			}
		}
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);
		
		return namedParameterJdbcTemplate.queryForList(sql.toString(), paramMap, PositionInfoPO.class);
	}
}