package com.huboyi.position;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 交易费用计算类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class DealFeeCalculator {
	
	/** 佣金费率（我现在是万分之7.5）。*/
	private double chargesRate = 0.00075;
	/** 印花税收费比率，目前是0.001（千分之1）。*/
	private double stampDutyRate = 0.001;
	/** 过户费收费比率，目前是0.0006（万分之6）。*/
	private double transferFeeRate = 0.0006;
	
	/**
	 * 计算手续费（目前手续费双向收取）。
	 * 
	 * 计算规则：
	 * 1、手续费 = 发生金额 * 佣金费率；
	 * 2、如果手续费小于5元，则按5元收取；
	 * 3、保留两位小数。
	 * 
	 * @param amountMoney 发生金额 == 股票单价 * 股票数量
	 * @return BigDecimal
	 */
	public BigDecimal calcCharges(BigDecimal amountMoney) {
		if (amountMoney == null || amountMoney.doubleValue() == 0) {
			throw new RuntimeException("在计算手续费时参数出现错误！[amountMoney = "+ amountMoney +"]");
		}
		
		BigDecimal charges = amountMoney.multiply(new BigDecimal(chargesRate));
		if (charges.compareTo(new BigDecimal(5)) == -1) {
			charges = new BigDecimal(5);
		}
		
		return charges.setScale(2, RoundingMode.HALF_UP);
	}
	
	/**
	 * 计算印花税（目前只有在卖出时收取）。
	 * 
	 * 计算规则：
	 * 1、印花税 = 发生金额 * 印花税收费比率；
	 * 2、保留两位小数。
	 * 
	 * @param amountMoney 发生金额 == 股票单价 * 股票数量
	 * @return BigDecimal
	 */
	public BigDecimal calStampDuty(BigDecimal amountMoney) {
		if (amountMoney == null || amountMoney.doubleValue() == 0) {
			throw new RuntimeException("在计算印花税时参数出现错误！[amountMoney = "+ amountMoney +"]");
		}
		
		BigDecimal stampDuty = amountMoney.multiply(new BigDecimal(stampDutyRate));
		return stampDuty.setScale(3, RoundingMode.HALF_UP);
	}
	
	/**
	 * 计算过户费（目前过户费只有在上交所双向收取，深交所不收）。
	 * 
	 * 计算规则：
	 * 1、过户费 = 成交数量 * 过户费收费比率；
	 * 2、保留两位小数。
	 * 
	 * @param stockCode 证券代码
	 * @param tradeNumber 成交数量
	 * @return BigDecimal
	 */
	public BigDecimal calcTransferFee(String stockCode, Long tradeNumber) {
		if (stockCode == null || stockCode.isEmpty() || tradeNumber == null || tradeNumber == 0) {
			throw new RuntimeException("在计算过户费时参数出现错误！[stockCode = "+ stockCode +"] | [tradeNumber = "+ tradeNumber +"]");
		}
		
		// 目前过户费只有上交所收取。
		if (stockCode.substring(0, 2).equalsIgnoreCase("SZ")) {
			return new BigDecimal(0);
		}
		
		BigDecimal transferFee = new BigDecimal(tradeNumber).multiply(new BigDecimal(transferFeeRate));
		return transferFee.setScale(2, RoundingMode.HALF_UP);
	}
	
	// --- private method ---
	
	public double getChargesRate() {
		return chargesRate;
	}

	public void setChargesRate(double chargesRate) {
		this.chargesRate = chargesRate;
	}

	public double getStampDutyRate() {
		return stampDutyRate;
	}

	public void setStampDutyRate(double stampDutyRate) {
		this.stampDutyRate = stampDutyRate;
	}

	public double getTransferFeeRate() {
		return transferFeeRate;
	}

	public void setTransferFeeRate(double transferFeeRate) {
		this.transferFeeRate = transferFeeRate;
	}
}