package com.huboyi.position.service;

import java.math.BigDecimal;
import java.util.List;

import com.huboyi.position.constant.FundsFlowBusiness;
import com.huboyi.position.po.EverySumPositionInfoPO;
import com.huboyi.position.po.FundsFlowPO;
import com.huboyi.position.po.OrderInfoPO;

/**
 * 仓位操作服务的接口。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
public interface PositionService {
	
	/**
	 * 保存银行转存记录。
	 * 
	 * @param businessType 业务类型
	 * @param stockCode 证券代码
	 * @param tradeDate 转账日期 （格式：yyyyMMddhhmmssSSS）
	 * @param transferMoney 转存金额
	 * @param stockholder 股东代码
	 * @return boolean true：转存成功；false：转存失败
	 */
	public boolean 
	insertBankTransfer(FundsFlowBusiness businessType, String stockCode, Long tradeDate, BigDecimal transferMoney, String stockholder);
	
	/**
	 * 保存买入证券时的资金流水记录和仓位信息。
	 * 执行顺序： 1、增加一条资金流水记录；
	 *        2、增加一条买入订单信息；
	 *        3、增加每一笔持仓信息;
	 *        4、修改之前持仓记录的止损价格。
	 * 
	 * @param stockCode 证券代码
	 * @param openSignalDate 建仓信号发出时间 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeDate 交易日期 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeNumber 成交数量
	 * @param tradePrice 成交价格
	 * @param stockholder 股东代码
	 */
	public void 
	insertBuyInfo(String stockCode, Long openSignalDate, Long tradeDate, Long tradeNumber, BigDecimal tradePrice, String stockholder);

	/**
	 * 保存卖出证券时的资金流水记录和仓位信息。
	 * 执行顺序： 1、修改每一笔持仓记录；
	 *        2、增加一条卖出订单信息；
	 *        3、增加一条资金流水记录。
	 * 
	 * @param stockCode 证券代码
	 * @param closeSignalDate 平仓信号发出时间 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeDate 交易日期 （格式：yyyyMMddhhmmssSSS）
	 * @param tradeNumber 成交数量
	 * @param tradePrice 成交价格
	 * @param stockholder 股东代码
	 */
	public void 
	insertSellInfo(String stockCode, Long closeSignalDate, Long tradeDate, Long tradeNumber, BigDecimal tradePrice, String stockholder);
	
	/**
	 * 查询某证券全部的资金流水信息（按照tradeDate升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<EverySumPositionInfoPO>
	 */
	public List<FundsFlowPO> findAllFundsFlow(String stockCode) ;
	
	/**
	 * 查询某证券全部的订单信息（按照tradeDate升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<EverySumPositionInfoPO>
	 */
	public List<OrderInfoPO> findAllOrderInfo(String stockCode) ;
	
	/**
	 * 查询某证券每一笔持仓信息（按照tradeDate升序）。
	 * 
	 * @param stockCode 证券代码
	 * @return List<EverySumPositionInfoPO>
	 */
	public List<EverySumPositionInfoPO> findAllEverySumPositionInfo(String stockCode) ;
	
	/**
	 * 每天收盘时刷新每笔仓位信息。
	 * 
	 * @param stockCode 证券代码
	 * @param date 日期 （格式：yyyyMMddhhmmssSSS）
	 * @param newPrice 当前价格
	 */
	public void flushEverySumPositionInfoInClosing(String stockCode, Long date, BigDecimal newPrice);
}