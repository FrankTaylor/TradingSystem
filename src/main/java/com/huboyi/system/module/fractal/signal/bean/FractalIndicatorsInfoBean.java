package com.huboyi.system.module.fractal.signal.bean;

import java.util.List;

import com.huboyi.engine.indicators.technology.pattern.bean.BandBean;
import com.huboyi.engine.indicators.technology.pattern.bean.FractalBean;
import com.huboyi.engine.indicators.technology.pattern.bean.PowerBean;
import com.huboyi.system.bean.IndicatorsInfoBean;

/**
 * 指标集合信息Bean。
 * 
 * @author FrankTaylor <mailto:hubin@300.cn>
 * @since 2015/9/12
 * @version 1.0
 */
public class FractalIndicatorsInfoBean extends IndicatorsInfoBean {

	/** 有效的顶底分型集合。*/
	private List<FractalBean> validFractalBeanList;
	
	/** 波段集合。*/
	private List<BandBean> bandBeanList;
	
	/** 中枢集合。*/
	private List<PowerBean> powerBeanList;
	
	/** 无包含关系的中枢集合。*/
	private List<PowerBean> noContainPowerBeanList;
	
	// --- get method and set method ---
	
	public List<FractalBean> getValidFractalBeanList() {
		return validFractalBeanList;
	}

	public void setValidFractalBeanList(List<FractalBean> validFractalBeanList) {
		this.validFractalBeanList = validFractalBeanList;
	}

	public List<BandBean> getBandBeanList() {
		return bandBeanList;
	}

	public void setBandBeanList(List<BandBean> bandBeanList) {
		this.bandBeanList = bandBeanList;
	}

	public List<PowerBean> getPowerBeanList() {
		return powerBeanList;
	}

	public void setPowerBeanList(List<PowerBean> powerBeanList) {
		this.powerBeanList = powerBeanList;
	}
	
	public List<PowerBean> getNoContainPowerBeanList() {
		return noContainPowerBeanList;
	}

	public void setNoContainPowerBeanList(List<PowerBean> noContainPowerBeanList) {
		this.noContainPowerBeanList = noContainPowerBeanList;
	}
}