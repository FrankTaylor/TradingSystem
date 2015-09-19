package com.huboyi.system.snap.db.mysql.repository.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import com.huboyi.system.po.EverySumPositionInfoPO;
import com.huboyi.system.snap.db.SpanEverySumPositionInfoRepository;

/**
 * 每一笔持仓信息DAO实现类 。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/04/17
 * @version 1.0
 */
public class SpanEverySumPositionInfoRepositoryImpl implements SpanEverySumPositionInfoRepository {
	
	/** 日志。*/
	private final Logger log = Logger.getLogger(SpanEverySumPositionInfoRepositoryImpl.class);
	
	@Resource
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void insert (final EverySumPositionInfoPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke insert method").append("\n");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("INSERT INTO every_sum_position_info ");
		
		sqlBuilder.append(" ( ");
		sqlBuilder.append("system_name, stock_code, stock_name, ");
		sqlBuilder.append("open_contract_code, system_open_point, system_open_name, open_signal_date, open_date, open_price, open_number, open_cost, ");
		sqlBuilder.append("can_close_number, stop_price, ");
		sqlBuilder.append("close_contract_code, system_close_point, system_close_name, close_signal_date, close_date, close_price, close_number, ");
		sqlBuilder.append("new_price, new_market_value, float_profit_and_loss, profit_and_loss_ratio, ");
		sqlBuilder.append("stockholder");
		sqlBuilder.append(" ) "); 
		
		sqlBuilder.append(" VALUES ");
		sqlBuilder.append(" ( ");
		sqlBuilder.append("?, ?, ?, ");
		sqlBuilder.append("?, ?, ?, ?, ?, ?, ?, ?, ");
		sqlBuilder.append("?, ?, ");
		sqlBuilder.append("?, ?, ?, ?, ?, ?, ?, ");
		sqlBuilder.append("?, ?, ?, ?, ");
		sqlBuilder.append("?");
		sqlBuilder.append(" ) "); 
		
		jdbcTemplate.update(sqlBuilder.toString(), new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				
				// ---
				if (po.getSystemName() != null) { ps.setString(1, po.getSystemName()); } else { ps.setNull(1, Types.VARCHAR, "system_name"); }                                             // 系统名称。
				if (po.getStockCode() != null) { ps.setString(2, po.getStockCode());  } else { ps.setNull(2, Types.VARCHAR, "stock_code"); }                                               // 证券代码。
				if (po.getStockName() != null) { ps.setString(3, po.getStockName()); } else { ps.setNull(3, Types.VARCHAR, "stock_name"); }                                                // 证券名称。
				
				// ---
				if (po.getOpenContractCode() != null) { ps.setString(4, po.getOpenContractCode()); } else { ps.setNull(4, Types.VARCHAR, "open_contract_code"); }                          // 建仓合同编号。
				if (po.getSystemOpenPoint() != null) { ps.setString(5, po.getSystemOpenPoint()); } else { ps.setNull(5, Types.VARCHAR, "system_open_point"); }                             // 系统建仓点类型（买入信号类型）。
				if (po.getSystemOpenName() != null) { ps.setString(6, po.getSystemOpenName()); } else { ps.setNull(6, Types.VARCHAR, "system_open_name"); }                                // 系统建仓点名称（买入信号名称）。
				if (po.getOpenSignalDate() != null) { ps.setLong(7, po.getOpenSignalDate()); } else { ps.setNull(7, Types.BIGINT, "open_signal_date"); }                                   // 建仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
				if (po.getOpenDate() != null) { ps.setLong(8, po.getOpenDate()); } else { ps.setNull(8, Types.BIGINT, "open_date"); }                                                      // 建仓日期（格式：yyyyMMddhhmmssSSS）。
				if (po.getOpenPrice() != null) { ps.setDouble(9, po.getOpenPrice().doubleValue()); } else { ps.setNull(9, Types.DECIMAL, "open_price"); }                                  // 建仓价格。
				if (po.getOpenNumber() != null) { ps.setLong(10, po.getOpenNumber()); } else { ps.setNull(10, Types.BIGINT, "open_number"); }                                              // 建仓数量。
				if (po.getOpenCost() != null) { ps.setDouble(11, po.getOpenCost().doubleValue()); } else { ps.setNull(11, Types.DECIMAL, "open_cost"); }                                   // 建仓成本。
				
				// --- 
				if (po.getCanCloseNumber() != null) { ps.setLong(12, po.getCanCloseNumber()); } else { ps.setNull(12, Types.BIGINT, "can_close_number"); }                                 // 可平仓数量。
				if (po.getStopPrice() != null) { ps.setDouble(13, po.getStopPrice().doubleValue()); } else { ps.setNull(13, Types.DECIMAL, "stop_price"); }                                // 止损价格。
				
				// --- 
				if (po.getCloseContractCode() != null) { ps.setString(14, po.getCloseContractCode()); } else { ps.setNull(14, Types.VARCHAR, "close_contract_code"); }                     // 平仓合同编号。
				if (po.getSystemClosePoint() != null) { ps.setString(15, po.getSystemClosePoint()); } else { ps.setNull(15, Types.VARCHAR, "system_close_point"); }                        // 系统平仓点类型（卖出信号类型）。
				if (po.getSystemCloseName() != null) { ps.setString(16, po.getSystemCloseName()); } else { ps.setNull(16, Types.VARCHAR, "system_close_name"); }                           // 系统平仓点名称（卖出信号名称）。	
				if (po.getCloseSignalDate() != null) { ps.setLong(17, po.getCloseSignalDate()); } else { ps.setNull(17, Types.BIGINT, "close_signal_date"); }                              // 平仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
				if (po.getCloseDate() != null) { ps.setLong(18, po.getCloseDate()); } else { ps.setNull(18, Types.BIGINT, "close_date"); }                                                 // 平仓日期（格式：yyyyMMddhhmmssSSS）。
				if (po.getClosePrice() != null) { ps.setDouble(19, po.getClosePrice().doubleValue()); } else { ps.setNull(19, Types.DECIMAL, "close_price"); }                             // 平仓价格。
				if (po.getCloseNumber() != null) { ps.setLong(20, po.getCloseNumber()); } else { ps.setNull(20, Types.BIGINT, "close_number"); }                                           // 平仓数量。
				
				// --- 
				if (po.getNewPrice() != null) { ps.setDouble(21, po.getNewPrice().doubleValue()); } else { ps.setNull(21, Types.DECIMAL, "new_price"); }                                   // 当前价。
				if (po.getNewMarketValue() != null) { ps.setDouble(22, po.getNewMarketValue().doubleValue()); } else { ps.setNull(22, Types.DECIMAL, "new_market_value"); }                // 最新市值。
				if (po.getFloatProfitAndLoss() != null) { ps.setDouble(23, po.getFloatProfitAndLoss().doubleValue()); } else { ps.setNull(23, Types.DECIMAL, "float_profit_and_loss"); }   // 浮动盈亏。
				if (po.getProfitAndLossRatio() != null) { ps.setDouble(24, po.getProfitAndLossRatio().doubleValue()); } else { ps.setNull(24, Types.DECIMAL, "profit_and_loss_ratio"); }   // 盈亏比例。
				
				// --- 
				if (po.getStockholder() != null) { ps.setString(25, po.getStockholder()); } else { ps.setNull(25, Types.VARCHAR, "stockholder"); }                                         // 股东代码。
				
			}
		});
	}

	@Override
	public List<EverySumPositionInfoPO> findByStockCode(String stockCode) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke findByStockCode method").append("\n");
		logMsg.append("@param [stockCode = " + stockCode + "]");
		log.info(logMsg.toString());
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT * FROM every_sum_position_info WHERE stock_code = ? ORDER BY open_date ASC");
		
		return jdbcTemplate.query(sqlBuilder.toString(), new Object[] { stockCode }, new int[] { Types.VARCHAR }, new RowMapper<EverySumPositionInfoPO> () {
			
			public EverySumPositionInfoPO mapRow(ResultSet rs, int rowNum) throws SQLException {
				EverySumPositionInfoPO po = new EverySumPositionInfoPO();
				
				po.setId(String.valueOf(rs.getInt("id")));                             // id。
				
				// --- 
				po.setSystemName(rs.getString("system_name"));                         // 系统名称。
				po.setStockCode(rs.getString("stock_code"));                           // 证券代码。
				po.setStockName(rs.getString("stock_name"));                           // 证券名称。
				
				// --- 
				po.setOpenContractCode(rs.getString("open_contract_code"));            // 建仓合同编号。
				po.setSystemOpenPoint(rs.getString("system_open_point"));              // 系统建仓点类型（买入信号类型）。
				po.setSystemOpenName(rs.getString("system_open_name"));                // 系统建仓点名称（买入信号名称）。
				po.setOpenSignalDate(rs.getLong("open_signal_date"));                  // 建仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
				po.setOpenDate(rs.getLong("open_date"));                               // 建仓日期（格式：yyyyMMddhhmmssSSS）。
				po.setOpenPrice(rs.getBigDecimal("open_price"));                       // 建仓价格。
				po.setOpenNumber(rs.getLong("open_number"));                           // 建仓数量。
				po.setOpenCost(rs.getBigDecimal("open_cost"));                         // 建仓成本。
				
				// --- 
				po.setCanCloseNumber(rs.getLong("can_close_number"));                  // 可平仓数量。
				po.setStopPrice(rs.getBigDecimal("stop_price"));                       // 止损价格。
				
				// --- 
				po.setCloseContractCode(rs.getString("close_contract_code"));          // 平仓合同编号。
				po.setSystemClosePoint(rs.getString("system_close_point"));            // 系统平仓点类型（卖出信号类型）。
				po.setSystemCloseName(rs.getString("system_close_name"));              // 系统平仓点名称（卖出信号名称）。
				po.setCloseSignalDate(rs.getLong("close_signal_date"));                // 平仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
				po.setCloseDate(rs.getLong("close_date"));                             // 平仓日期（格式：yyyyMMddhhmmssSSS）。
				po.setClosePrice(rs.getBigDecimal("close_price"));                     // 平仓价格。
				po.setCloseNumber(rs.getLong("close_number"));                         // 平仓数量。

				// --- 
				po.setNewPrice(rs.getBigDecimal("new_price"));                         // 当前价。
				po.setNewMarketValue(rs.getBigDecimal("new_market_value"));            // 最新市值。
				po.setFloatProfitAndLoss(rs.getBigDecimal("float_profit_and_loss"));   // 浮动盈亏。
				po.setProfitAndLossRatio(rs.getBigDecimal("profit_and_loss_ratio"));   // 盈亏比例。

				// --- 
				po.setStockholder(rs.getString("stockholder"));                        // 股东代码。
				
				return po;
	        }
	    });

	}
	
	@Override
	public List<EverySumPositionInfoPO> findEverySumPositionInfoList (String stockCode, String openContractCode, Long beginOpenDate, Long endOpenDate, String isClose, Integer beginPage, Integer endPage) {
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
		
		// --- sql
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT * FROM every_sum_position_info WHERE 1 = 1 ");
		
		if (stockCode != null) { sqlBuilder.append("And stock_code = ? "); }
		if (openContractCode != null) { sqlBuilder.append("And open_contract_code = ? "); }
		if (beginOpenDate != null) { sqlBuilder.append("And open_date >= ? "); }
		if (endOpenDate != null) { sqlBuilder.append("And open_date <= ? "); }
		if (isClose != null) {
			if (isClose.equalsIgnoreCase("0")) {
				sqlBuilder.append("And close_contract_code = 'no' ");
			}
			if (isClose.equalsIgnoreCase("1")) {
				sqlBuilder.append("And close_contract_code != 'no' ");
			}
		}
		
		sqlBuilder.append(" ORDER BY open_date ASC ");
		
		if (beginPage != null && endPage != null) {
			sqlBuilder.append(" LIMIT ?, ?");
		}
		
		// --- paramas
		List<Object> paramsList = new ArrayList<Object>();
		List<Integer> paramsTypeList = new ArrayList<Integer>();
		
		if (stockCode != null) { paramsList.add(stockCode); paramsTypeList.add(Types.VARCHAR); }
		if (openContractCode != null) { paramsList.add(openContractCode); paramsTypeList.add(Types.VARCHAR); }
		if (beginOpenDate != null) { paramsList.add(beginOpenDate); paramsTypeList.add(Types.BIGINT); }
		if (endOpenDate != null) { paramsList.add(endOpenDate); paramsTypeList.add(Types.BIGINT); }
		if (beginPage != null && endPage != null) { paramsList.add(beginPage); paramsList.add(endPage); paramsTypeList.add(Types.INTEGER); paramsTypeList.add(Types.INTEGER); }
		
		int[] paramsTypeArrays = new int[paramsTypeList.size()];
		for (int i = 0; i < paramsList.size(); i++) {
			paramsTypeArrays[i] = paramsTypeList.get(i);
		}
		
		return jdbcTemplate.query(sqlBuilder.toString(), paramsList.toArray(new Object[0]), paramsTypeArrays, new RowMapper<EverySumPositionInfoPO> () {
			
			public EverySumPositionInfoPO mapRow(ResultSet rs, int rowNum) throws SQLException {
				EverySumPositionInfoPO po = new EverySumPositionInfoPO();
				
				po.setId(String.valueOf(rs.getInt("id")));                             // id。
				
				// --- 
				po.setSystemName(rs.getString("system_name"));                         // 系统名称。
				po.setStockCode(rs.getString("stock_code"));                           // 证券代码。
				po.setStockName(rs.getString("stock_name"));                           // 证券名称。
				
				// --- 
				po.setOpenContractCode(rs.getString("open_contract_code"));            // 建仓合同编号。
				po.setSystemOpenPoint(rs.getString("system_open_point"));              // 系统建仓点类型（买入信号类型）。
				po.setSystemOpenName(rs.getString("system_open_name"));                // 系统建仓点名称（买入信号名称）。
				po.setOpenSignalDate(rs.getLong("open_signal_date"));                  // 建仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
				po.setOpenDate(rs.getLong("open_date"));                               // 建仓日期（格式：yyyyMMddhhmmssSSS）。
				po.setOpenPrice(rs.getBigDecimal("open_price"));                       // 建仓价格。
				po.setOpenNumber(rs.getLong("open_number"));                           // 建仓数量。
				po.setOpenCost(rs.getBigDecimal("open_cost"));                         // 建仓成本。
				
				// --- 
				po.setCanCloseNumber(rs.getLong("can_close_number"));                  // 可平仓数量。
				po.setStopPrice(rs.getBigDecimal("stop_price"));                       // 止损价格。
				
				// --- 
				po.setCloseContractCode(rs.getString("close_contract_code"));          // 平仓合同编号。
				po.setSystemClosePoint(rs.getString("system_close_point"));            // 系统平仓点类型（卖出信号类型）。
				po.setSystemCloseName(rs.getString("system_close_name"));              // 系统平仓点名称（卖出信号名称）。
				po.setCloseSignalDate(rs.getLong("close_signal_date"));                // 平仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
				po.setCloseDate(rs.getLong("close_date"));                             // 平仓日期（格式：yyyyMMddhhmmssSSS）。
				po.setClosePrice(rs.getBigDecimal("close_price"));                     // 平仓价格。
				po.setCloseNumber(rs.getLong("close_number"));                         // 平仓数量。

				// --- 
				po.setNewPrice(rs.getBigDecimal("new_price"));                         // 当前价。
				po.setNewMarketValue(rs.getBigDecimal("new_market_value"));            // 最新市值。
				po.setFloatProfitAndLoss(rs.getBigDecimal("float_profit_and_loss"));   // 浮动盈亏。
				po.setProfitAndLossRatio(rs.getBigDecimal("profit_and_loss_ratio"));   // 盈亏比例。

				// --- 
				po.setStockholder(rs.getString("stockholder"));                        // 股东代码。
				
				return po;
	        }
	    });
	}
	
	@Override
	public void updateById(String id, final EverySumPositionInfoPO po) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke updateById method").append("\n");
		logMsg.append("@param [id = " + id + "]");
		logMsg.append("@param [po = " + po + "]");
		log.info(logMsg.toString());

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("UPDATE every_sum_position_info SET ");
		
		if (po.getId() != null) { sqlBuilder.append("id = ?, "); }
		
		// ---
		if (po.getSystemName() != null) { sqlBuilder.append("system_name = ?, "); }
		if (po.getStockCode() != null) { sqlBuilder.append("stock_code = ?, "); }
		if (po.getStockName() != null) { sqlBuilder.append("stock_name = ?, "); }
		
		// ---
		if (po.getOpenContractCode() != null) { sqlBuilder.append("open_contract_code = ?, "); }
		if (po.getSystemOpenPoint() != null) { sqlBuilder.append("system_open_point = ?, "); }
		if (po.getSystemOpenName() != null) { sqlBuilder.append("system_open_name = ?, "); }
		if (po.getOpenSignalDate() != null) { sqlBuilder.append("open_signal_date = ?, "); }
		if (po.getOpenDate() != null) { sqlBuilder.append("open_date = ?, "); }
		if (po.getOpenPrice() != null) { sqlBuilder.append("open_price = ?, "); }
		if (po.getOpenNumber() != null) { sqlBuilder.append("open_number = ?, "); }
		if (po.getOpenCost() != null) { sqlBuilder.append("open_cost = ?, "); }
		
		// ---
		if (po.getCanCloseNumber() != null) { sqlBuilder.append("can_close_number = ?, "); }
		if (po.getStopPrice() != null) { sqlBuilder.append("stop_price = ?, "); }
		
		// ---
		if (po.getCloseContractCode() != null) { sqlBuilder.append("close_contract_code = ?, "); }
		if (po.getSystemClosePoint() != null) { sqlBuilder.append("system_close_point = ?, "); }
		if (po.getSystemCloseName() != null) { sqlBuilder.append("system_close_name = ?, "); }
		if (po.getCloseSignalDate() != null) { sqlBuilder.append("close_signal_date = ?, "); }
		if (po.getCloseDate() != null) { sqlBuilder.append("close_date = ?, "); }
		if (po.getClosePrice() != null) { sqlBuilder.append("close_price = ?, "); }
		if (po.getCloseNumber() != null) { sqlBuilder.append("close_number = ?, "); }
		
		// ---
		if (po.getNewPrice() != null) { sqlBuilder.append("new_price = ?, "); }
		if (po.getNewMarketValue() != null) { sqlBuilder.append("new_market_value = ?, "); }
		if (po.getFloatProfitAndLoss() != null) { sqlBuilder.append("float_profit_and_loss = ?, "); }
		if (po.getProfitAndLossRatio() != null) { sqlBuilder.append("profit_and_loss_ratio = ?, "); }
		
		// ---
		if (po.getStockholder() != null) { sqlBuilder.append("stockholder = ?, "); }
		
		// 去掉sql语句末尾的“,”
		sqlBuilder = sqlBuilder.deleteCharAt((sqlBuilder.length() - 1));
		
		sqlBuilder.append(" WHERE id = ? ");
		
		jdbcTemplate.update(sqlBuilder.toString(), new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				if (po.getId() != null) { ps.setInt(1, Integer.valueOf(po.getId())); }                                    // id。
				
				// ---
				if (po.getSystemName() != null) { ps.setString(2, po.getSystemName()); }                                  // 系统名称。
				if (po.getStockCode() != null) { ps.setString(3, po.getStockCode());  }                                   // 证券代码。
				if (po.getStockName() != null) { ps.setString(4, po.getStockName()); }                                    // 证券名称。
				
				// ---
				if (po.getOpenContractCode() != null) { ps.setString(5, po.getOpenContractCode()); }                      // 建仓合同编号。
				if (po.getSystemOpenPoint() != null) { ps.setString(6, po.getSystemOpenPoint()); }                        // 系统建仓点类型（买入信号类型）。
				if (po.getSystemOpenName() != null) { ps.setString(7, po.getSystemOpenName()); }                          // 系统建仓点名称（买入信号名称）。
				if (po.getOpenSignalDate() != null) { ps.setLong(8, po.getOpenSignalDate()); }                            // 建仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
				if (po.getOpenDate() != null) { ps.setLong(9, po.getOpenDate()); }                                        // 建仓日期（格式：yyyyMMddhhmmssSSS）。
				if (po.getOpenPrice() != null) { ps.setDouble(11, po.getOpenPrice().doubleValue()); }                     // 建仓价格。
				if (po.getOpenNumber() != null) { ps.setLong(12, po.getOpenNumber()); }                                   // 建仓数量。
				if (po.getOpenCost() != null) { ps.setDouble(13, po.getOpenCost().doubleValue()); }                       // 建仓成本。
				
				// --- 
				if (po.getCanCloseNumber() != null) { ps.setLong(14, po.getCanCloseNumber()); }                           // 可平仓数量。
				if (po.getStopPrice() != null) { ps.setDouble(15, po.getStopPrice().doubleValue()); }                     // 止损价格。
				
				// --- 
				if (po.getCloseContractCode() != null) { ps.setString(16, po.getCloseContractCode()); }                   // 平仓合同编号。
				if (po.getSystemClosePoint() != null) { ps.setString(17, po.getSystemClosePoint()); }                     // 系统平仓点类型（卖出信号类型）。
				if (po.getSystemCloseName() != null) { ps.setString(18, po.getSystemCloseName()); }                       // 系统平仓点名称（卖出信号名称）。	
				if (po.getCloseSignalDate() != null) { ps.setLong(19, po.getCloseSignalDate()); }                         // 平仓信号发出时间（格式：yyyyMMddhhmmssSSS）。
				if (po.getCloseDate() != null) { ps.setLong(20, po.getCloseDate()); }                                     // 平仓日期（格式：yyyyMMddhhmmssSSS）。
				if (po.getClosePrice() != null) { ps.setDouble(22, po.getClosePrice().doubleValue()); }                   // 平仓价格。
				if (po.getCloseNumber() != null) { ps.setLong(23, po.getCloseNumber()); }                                 // 平仓数量。
				
				// --- 
				if (po.getNewPrice() != null) { ps.setDouble(24, po.getNewPrice().doubleValue()); }                       // 当前价。
				if (po.getNewMarketValue() != null) { ps.setDouble(25, po.getNewMarketValue().doubleValue()); }           // 最新市值。
				if (po.getFloatProfitAndLoss() != null) { ps.setDouble(26, po.getFloatProfitAndLoss().doubleValue()); }   // 浮动盈亏。
				if (po.getProfitAndLossRatio() != null) { ps.setDouble(27, po.getProfitAndLossRatio().doubleValue()); }   // 盈亏比例。
				
				// --- 
				if (po.getStockholder() != null) { ps.setString(28, po.getStockholder()); }                               // 股东代码。
				
			}
		});
	}

	@Override
	public void deleteById(final String id) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append("invoke deleteById method").append("\n");
		logMsg.append("@param [id = " + id + "]");
		log.info(logMsg.toString());

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("DELETE FROM every_sum_position_info WHERE id = ? ");
		
		jdbcTemplate.update(sqlBuilder.toString(), new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setInt(1, Integer.valueOf(id));   // id。
			}
		});
	}

}