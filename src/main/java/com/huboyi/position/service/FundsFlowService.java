package com.huboyi.position.service;

import java.math.BigDecimal;
import java.util.List;

import com.huboyi.position.entity.po.FundsFlowPO;

/**
 * 资金流水Service。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.1
 */
public interface FundsFlowService {
	
	/**
	 * 转入资金。
	 * 
	 * @param tradeDate 成交日期（格式：yyyyMMddhhmmssSSS）
	 * @param tradeMoney 成交金额
	 * @param stockholder 股东代码
	 * @return boolean true：成功；false：失败
	 */
	public boolean 
	transferInto(Long tradeDate, BigDecimal tradeMoney, String stockholder);
	
	/**
	 * 转出资金。
	 * 
	 * @param tradeDate 成交日期（格式：yyyyMMddhhmmssSSS）
	 * @param tradeMoney 成交金额
	 * @param stockholder 股东代码
	 * @return boolean true：成功；false：失败
	 */
	public boolean
	transferOut(Long tradeDate, BigDecimal tradeMoney, String stockholder);
	
	/**
	 * 删除所有的资金流水记录。
	 */
	public void deleteAllRecords();
	
	/**
	 * 删除资金流水记录。
	 * 
	 * @param stockholder 股东代码
	 */
	public void deleteRecords(String stockholder);
	
	/**
	 * 最新的一条资金流水记录。
	 * 
	 * @param stockholder 股东代码
	 * @return FundsFlowPO
	 */
	public FundsFlowPO findNewRecord(String stockholder);
	
	/**
	 * 查询所有的资金流水记录（按照tradeDate升序）。
	 * 
	 * @param stockholder 股东代码
	 * @return List<FundsFlowPO>
	 */
	public List<FundsFlowPO> findRecords(String stockholder);
}