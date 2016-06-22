//package com.huboyi.system.module.stanWeinstein.signal.calc;
//
//import java.util.List;
//
//import com.huboyi.data.load.bean.StockDataBean;
//import com.huboyi.engine.indicators.technology.TechAlgorithm;
//import com.huboyi.system.module.stanWeinstein.signal.bean.StanWeinDataCalcResultBean;
//import com.huboyi.system.module.stanWeinstein.signal.param.StanWeinMACDParam;
//import com.huboyi.system.module.stanWeinstein.signal.param.StanWeinMAParam;
//import com.huboyi.system.module.stanWeinstein.signal.param.StanWeinVMAParam;
//
///**
// * 计算StanWeinstein交易系统所需数据的计算类。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 2014/10/31
// * @version 1.0
// */
//public class StanWeinDataCalculator {
//	
//	/** StanWeinstein交易系统中均线的参数类。*/
//	private final StanWeinMAParam maParam;
//	/** StanWeinstein交易系统中MACD指标的参数类。*/
//	@SuppressWarnings("unused")
//	private final StanWeinMACDParam macdParam;
//	/** StanWeinstein交易系统中成交量均线的参数类。*/
//	private final StanWeinVMAParam vmaParam;
//	
//	/**
//	 * 构造函数。
//	 * 
//	 * @param maParam StanWeinstein交易系统中均线的参数类
//	 * @param macdParam StanWeinstein交易系统中MACD指标的参数类
//	 * @param vmaParam StanWeinstein交易系统中成交量均线的参数类
//	 */
//	public StanWeinDataCalculator (StanWeinMAParam maParam, StanWeinMACDParam macdParam, StanWeinVMAParam vmaParam) {
//		this.maParam = maParam;
//		this.macdParam = macdParam;
//		this.vmaParam = vmaParam;
//	}
//	
//	/**
//	 * 计算StanWeinstein交易系统所需数据。
//	 * 
//	 * @param stockDataBeanList 行情数据集合
//	 * @return StanWeinDataCalcResultBean
//	 */
//	public StanWeinDataCalcResultBean
//	calc (List<StockDataBean> stockDataBeanList) {
//		StanWeinDataCalcResultBean bean = new StanWeinDataCalcResultBean();
//		
//		// 行情数据。
//		bean.setStockDataBeanList(stockDataBeanList);
//		// 短期均线数据。
//		bean.setShortMAList(TechAlgorithm.MA(stockDataBeanList, maParam.getShortCycle()));
//		// 长期均线数据。
//		bean.setLongMAList(TechAlgorithm.MA(stockDataBeanList, maParam.getLongCycle()));
//		// MACD数据。
////		bean.setMacdBeanList(TechAlgorithm.MACD(stockDataBeanList, macdParam.getShortCycle(), macdParam.getLongCycle(), macdParam.getDeaCycle()));
//		// 短期成交量均线数据。
//		bean.setShortVMAList(TechAlgorithm.VMA(stockDataBeanList, stockDataBeanList.size(), vmaParam.getShortCycle()));
//		// 长期成交量均线数据。
//		bean.setLongVMAList(TechAlgorithm.VMA(stockDataBeanList, stockDataBeanList.size(), vmaParam.getLongCycle()));
//		
//		return bean;
//	}
//}