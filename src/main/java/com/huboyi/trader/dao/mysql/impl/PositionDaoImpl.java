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

import com.huboyi.trader.dao.PositionDao;
import com.huboyi.trader.entity.po.PositionPO;

/**
 * 持仓信息 Dao 实现类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.2
 */
@Repository("positionDao")
public class PositionDaoImpl implements PositionDao {

	/** 日志。*/
	private final Logger log = Logger.getLogger(PositionDaoImpl.class);
	
	@Autowired
	@Qualifier("npJdbcTemplate")
	private NamedParameterJdbcTemplate npJdbcTemplate;
	
	@Override
	public void insert(PositionPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 insert 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO position ");
		sql.append(" ( ");
		sql.append("stock_code, stock_name, ");
		sql.append("stock_number, can_sell_number, ");
		sql.append("cost_price, cost_money, ");
		sql.append("new_price, new_market_value, float_profit_and_loss, profit_and_loss_ratio, ");
		sql.append("today_buy_volume, today_sell_volume, ");
		sql.append("stockholder");
		sql.append("create_time");
		sql.append(" ) "); 
		
		sql.append(" VALUES ");
		sql.append(" ( ");
		sql.append(":stockCode, :stockName, ");
		sql.append(":stockNumber, :canSellNumber, ");
		sql.append(":costPrice, :costMoney, ");
		sql.append(":newPrice, :newMarketValue, :floatProfitAndLoss, :profitAndLossRatio, ");
		sql.append(":todayBuyVolume, :todaySellVolume, ");
		sql.append(":stockholder");
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
		sql.append("TRUNCATE position");
		
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
		sql.append("DELETE FROM position WHERE stockholder = :stockholder");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockholder", stockholder);
		
		npJdbcTemplate.update(sql.toString(), paramMap);
	}

	@Override
	public void delete(String stockCode, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 delete 方法").append("\n");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		log.info(logMsg.toString());

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM position WHERE stock_code = :stockCode AND stockholder = :stockholder");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockCode", stockCode);
		paramMap.put(":stockholder", stockholder);
		
		npJdbcTemplate.update(sql.toString(), paramMap);
	}

	@Override
	public void update(PositionPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 update 方法").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE position SET ");
		
		// --- 
		if (po.getStockCode() != null) { sql.append("stock_code = :stockCode, "); }
		if (po.getStockName() != null) { sql.append("stock_name = :stockName, "); }
		
		// --- 
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
		if (po.getTodayBuyVolume() != null) { sql.append("today_buy_volume = :todayBuyVolume, "); }
		if (po.getTodaySellVolume() != null) { sql.append("today_sell_volume = :todaySellVolume, "); }

		// ---
		if (po.getStockholder() != null) { sql.append("stockholder = :stockholder, "); }
		
		// 去掉sql语句末尾的“,”
		sql = sql.deleteCharAt((sql.length() - 1));
		sql.append(" WHERE id = :id ");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		SqlParameterSource paramSource = new BeanPropertySqlParameterSource(po);
		
		npJdbcTemplate.update(sql.toString(), paramSource);
	}

	@Override
	public PositionPO findOne(String stockCode, String stockholder) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findOne 方法").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM position WHERE stock_code = :stockCode AND stockholder = :stockholder");
		
		log.info("执行的 sql 语句 -> " + sql);
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(":stockCode", stockCode);
		paramMap.put(":stockholder", stockholder);
		
		return npJdbcTemplate.queryForObject(sql.toString(), paramMap, PositionPO.class);
	}
	
	@Override
	public List<PositionPO> findAll(String stockholder, SortType sortType) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("调用 findAll 方法").append("\n");
		logMsg.append("@param [stockholder = " + stockholder + "]");
		logMsg.append("@param [sortType = " + sortType + "]");
		log.info(logMsg.toString());
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM position WHERE stockholder = :stockholder ");
		
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
		
		return npJdbcTemplate.queryForList(sql.toString(), paramMap, PositionPO.class);
	}
}