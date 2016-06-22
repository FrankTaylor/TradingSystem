//package com.huboyi.system.module.stanWeinstein.signal.rule;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import com.huboyi.data.load.bean.StockDataBean;
//import com.huboyi.engine.indicators.technology.TechAlgorithm;
//import com.huboyi.engine.indicators.technology.trend.bean.MoveAverageBean;
//import com.huboyi.engine.indicators.technology.volume.bean.VolMoveAverageBean;
//import com.huboyi.system.module.stanWeinstein.signal.bean.StanWeinDataCalcResultBean;
//import com.huboyi.system.module.stanWeinstein.signal.param.StanWeinRuleParam;
//
///**
// * StanWeinstein交易系统进出场规则。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 2014/10/31
// * @version 1.0
// */
//public class StanWeinDealRule {
//
//	/** StanWeinstein交易系统中交易规则的参数类。*/
//	private final StanWeinRuleParam stanWeinRuleParam;
//	
//	/**
//	 * 构造函数。
//	 * 
//	 * @param stanWeinRuleParam StanWeinRuleParam
//	 */
//	public StanWeinDealRule (StanWeinRuleParam stanWeinRuleParam) {
//		this.stanWeinRuleParam = stanWeinRuleParam;
//	}
//
//	/**
//	 * 交易系统买入规则。
//	 * 
//	 * @param resultBean 交易系统所需数据
//	 * @return StockDataBean StockDataBean
//	 */
//	public StockDataBean buyRule (StanWeinDataCalcResultBean resultBean) {
//		
//		/*
//		 * 买入规则：
//		 * 1、长期均线在某周期内走平；
//		 * 2、当前K线突破长期均线，这里有两个情况满足其中一个即可：
//		 *   2.1、当前K线跳空突破长期均线，且收盘价在长期均线之上；
//		 *   2.2、当前K线的收盘价高于长期均线，且收盘价高于其涨停价的某一百分比。
//		 * 3、当前K线放量突破，下面这两个条件必须同时满足：
//		 *   3.1、当前成交量高于其短期成交量；
//		 *   3.2、其短期成交量高于其长期成交量。
//		 */
//		
//		List<MoveAverageBean> shortMAList = resultBean.getShortMAList();      // 短期均线集合。
//		List<MoveAverageBean> longMAList = resultBean.getLongMAList();        // 长期均线集合。
//		List<VolMoveAverageBean> shortVMAList = resultBean.getShortVMAList(); // 短期成交量集合。
//		List<VolMoveAverageBean> longVMAList = resultBean.getLongVMAList();   // 长期成交量集合。
//
//		// --- 基值判断 ---
//		// 当长期均线价格不满足测试周期长度时返回null。
//		if (longMAList.size() < (stanWeinRuleParam.getFlatTestCycle() + 1)) {
//			return null;
//		}
//		
//		// 在测试周期前无有效的长期均线时返回null。
//		if (longMAList.get(longMAList.size() - stanWeinRuleParam.getFlatTestCycle()).getAvg().intValue() == 0) {
//			return null;
//		}
//		
//		// --- 基值判断 ---
//		
//		// 得到当前K线，如果符合买入规则则把该对象返回。
//		StockDataBean currentSDBean = 
//			resultBean.getStockDataBeanList().get(
//					resultBean.getStockDataBeanList().size() - 1);
//
//		// ------------ 规则1：长期均线在某周期内走平 ------------
//		
//		// --- 长期均线在某周期内走平 ---
//		int flatTestCycleSuccessNums = 0;
//		for (int i = longMAList.size() - 1; i > longMAList.size() - stanWeinRuleParam.getFlatTestCycle(); i--) {
//			
//			BigDecimal currentPrice = longMAList.get(i).getAvg();  // 当前长期均线价格。
//			BigDecimal prevPrice = longMAList.get(i - 1).getAvg(); // 前一长期均线价格。
//			
//			// 当前长期均线价格减去前一长期均线价格的绝对值小于阀值时表示满足条件。
//			if (Math.abs(currentPrice.subtract(prevPrice).doubleValue()) <= 
//				stanWeinRuleParam.getFlatTestDiffRange()) {
//				flatTestCycleSuccessNums++;
//			}
//		}
//		
//		// 如果不能满足规则1则返回null。
//		if (flatTestCycleSuccessNums < stanWeinRuleParam.getFlatTestCycleSuccessNums()) {
//			return null;
//		}
//		// --- 长期均线在某周期内走平 ---
//		
//		// ------------ 规则2:当前K线突破长期均线 ------------
//		
//		// --- 当前K线收阳线突破长期均线 ---
//		StockDataBean prevSDBean = currentSDBean.getPrev();                          // 前一K线。
//		BigDecimal currentLongMA = longMAList.get(longMAList.size() - 1).getAvg();   // 当前K线所对应的长期均线。
//		
//		// 如果超越这天收阴线则放弃购买。
//		if (currentSDBean.getClose().compareTo(currentSDBean.getOpen()) != 1) {
//			return null;
//		}
//		
//		boolean isBeyondLongMA = false;
//		if (currentSDBean.getHigh().compareTo(currentLongMA) == 1) {                 // 当前K线突破长期均线。
//			if (currentSDBean.getLow().compareTo(prevSDBean.getHigh()) == 1) {       // 当前K线跳空高开。
//				isBeyondLongMA = true;
//			} else {
//				// 涨停价格的某一百分比价格。
//				BigDecimal beyondPrecentPrice = 
//					TechAlgorithm.calcLimitUp(currentSDBean)
//					.multiply(BigDecimal.valueOf(stanWeinRuleParam.getBeyondPercent()));
//				if (currentSDBean.getClose().compareTo(beyondPrecentPrice) == 1) {   // 当前K线的收盘价高于其涨停价的某一百分比。
//					isBeyondLongMA = true;
//				}
//			}
//		}
//		
//		// 如果不能满足规则2则返回null。
//		if (!isBeyondLongMA) {
//			return null;
//		}
//		// --- 当前K线突破长期均线 ---
//		
//		// ------------ 规则3:短期均线在长期均线之上 ------------
//		BigDecimal currentShortMA = shortMAList.get(shortMAList.size() - 1).getAvg(); // 当前K线所对应的长期均线。
//		if (currentShortMA.compareTo(currentLongMA) == -1) {
//			return null;
//		}
//		
//		// ------------ 规则4:当前K线放量突破 ------------
//		VolMoveAverageBean shortVMABean = shortVMAList.get(shortVMAList.size() - 1); // 当前短期成交量。
//		VolMoveAverageBean longVMABean = longVMAList.get(longVMAList.size() - 1);    // 当前长期成交量。
//		
//		boolean isBeyond = false;
//		if (currentSDBean.getVolume().compareTo(shortVMABean.getAvg()) == 1 &&
//				shortVMABean.getAvg().compareTo(longVMABean.getAvg()) == 1) {
//			isBeyond = true;
//		}
//		
//		return isBeyond ? currentSDBean : null;
//	}
//	
//	/**
//	 * 交易系统卖出规则。
//	 * 
//	 * @param resultBean 交易系统所需数据
//	 * @return StockDataBean StockDataBean
//	 */
//	public StockDataBean sellRule (StanWeinDataCalcResultBean resultBean) {
//
//		StockDataBean currentSDBean = 
//			resultBean.getStockDataBeanList()
//			.get(resultBean.getStockDataBeanList().size() - 1);                          // 得到当前K线，如果符合买入规则则把该对象返回。
//		
//		List<MoveAverageBean> shortMAList = resultBean.getShortMAList();                 // 短期均线集合。
//		List<MoveAverageBean> longMAList = resultBean.getLongMAList();                   // 长期均线集合。
//		
//		BigDecimal currentshortMA = shortMAList.get(shortMAList.size() - 1).getAvg();  // 当前K线所对应短期均线。
//		BigDecimal currentlongMA = longMAList.get(longMAList.size() - 1).getAvg();
//		
//		/*
//		 * 规则1：短期均线在长期均线之下。
//		 */
//		if (currentshortMA.compareTo(currentlongMA) != -1) {
//			return null;
//		}
//		
//		/*
//		 * 规则2：价格跌破短期均线。
//		 */
//		boolean isBeyond = false;
//		if (currentSDBean.getClose().compareTo(currentlongMA) == -1) {
//			isBeyond = true;
//		}
//		
//		return isBeyond ? currentSDBean : null;
//	}
//
//}