package com.huboyi.system.module.fractal.signal.calc;

import java.util.List;

import com.huboyi.engine.indicators.technology.PatternAlogrithm;
import com.huboyi.engine.indicators.technology.pattern.bean.FractalBean;
import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.module.fractal.signal.bean.FractalIndicatorsInfoBean;

/**
 * 计算顶底分型交易系统中所需数据的计算类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/11/10
 * @version 1.0
 */
public class FractalDataCalculator {
	
	/**
	 * 计算顶底分型交易系统所需数据。
	 * 
	 * @param stockDataBeanList 行情数据集合
	 * @return FractalIndicatorsInfoBean
	 * @throws CloneNotSupportedException 
	 */
	public FractalIndicatorsInfoBean
	calc (List<StockDataBean> stockDataList) throws CloneNotSupportedException {
		FractalIndicatorsInfoBean bean = new FractalIndicatorsInfoBean();

		// 得到没有包含关系的K线集合。
		List<StockDataBean> noContainKLineList = PatternAlogrithm.getNoContainKLineList(stockDataList);
		
		// 有效的顶底分型集合。
		List<FractalBean> fractalBeanList = (PatternAlogrithm.getFractalBeanList(noContainKLineList));
		bean.setValidFractalBeanList(PatternAlogrithm.getValidFractalBeanList(fractalBeanList));
		
		// 波段集合。
		bean.setBandBeanList(PatternAlogrithm.getBandBeanList(bean.getValidFractalBeanList(), stockDataList));
		
		// 中枢集合。
		if (null != bean.getBandBeanList() && !bean.getBandBeanList().isEmpty()) {			
			bean.setPowerBeanList(PatternAlogrithm.getPowerBeanList(bean.getBandBeanList().get(0)));
			bean.setNoContainPowerBeanList(PatternAlogrithm.getNoContainPowerBeanList(bean.getPowerBeanList()));
		}
		
		return bean;
	}
}