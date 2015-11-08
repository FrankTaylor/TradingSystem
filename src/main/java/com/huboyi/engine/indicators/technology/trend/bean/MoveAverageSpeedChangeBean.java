package com.huboyi.engine.indicators.technology.trend.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.huboyi.data.load.bean.StockDataBean;
import com.huboyi.engine.indicators.technology.TechAlgorithm;

/**
 * 均线运动速度改变的结果类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/14
 * @version 1.0
 */
public class MoveAverageSpeedChangeBean {
	
	/** 某段均线中运动速度的集合。*/
	private List<BigDecimal> speedList;
	/** 某段均线中运动速度的总和。*/
	private BigDecimal sumSpeed;
	/** 某段均线中运动速度的均值。*/
	private BigDecimal avgSpeed;
	
	/** 某段均线中运动速度之间差值的集合。*/
	private List<BigDecimal> diffSpeedList;
	/** 某段均线中运动速度之间差值的总和。*/
	private BigDecimal sumDiffSpeed;
	/** 某段均线中运动速度之间差值的均值。*/
	private BigDecimal avgDiffSpeed;
	
	/**
	 * 某段均线中运动速度的某周期均线。
	 * 
	 * @param n 计算普通平均线周期
	 * @return List<MoveAverageBean>
	 */
	public List<MoveAverageBean> getSpeedMA (final int n) {
		
		if (speedList == null || speedList.size() < 2 || n < 2) {
			return new ArrayList<MoveAverageBean>(0);
		}
		
		List<StockDataBean> sdBeanList = new ArrayList<StockDataBean>();
		for (BigDecimal speed : speedList) {
			StockDataBean sdBean = new StockDataBean();
			sdBean.setClose(speed);
			
			sdBeanList.add(sdBean);
		}
		return TechAlgorithm.MA(sdBeanList, n);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[\n")
		.append("\t").append("speedList = ").append(speedList).append("\n")
		.append("\t").append("sumSpeed = ").append(sumSpeed).append("\n")
		.append("\t").append("avgSpeed = ").append(avgSpeed).append("\n")
		
		.append("\t").append("diffSpeedList = ").append(diffSpeedList).append("\n")
		.append("\t").append("sumDiffSpeed = ").append(sumDiffSpeed).append("\n")
		.append("\t").append("avgDiffSpeed = ").append(avgDiffSpeed).append("\n")
		.append("]\n");
		return builder.toString();
	}
	
	// --- get method and set method ---

	public List<BigDecimal> getSpeedList() {
		return speedList;
	}

	public void setSpeedList(List<BigDecimal> speedList) {
		this.speedList = speedList;
	}

	public BigDecimal getSumSpeed() {
		return sumSpeed;
	}

	public void setSumSpeed(BigDecimal sumSpeed) {
		this.sumSpeed = sumSpeed;
	}

	public BigDecimal getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(BigDecimal avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	public List<BigDecimal> getDiffSpeedList() {
		return diffSpeedList;
	}

	public void setDiffSpeedList(List<BigDecimal> diffSpeedList) {
		this.diffSpeedList = diffSpeedList;
	}

	public BigDecimal getSumDiffSpeed() {
		return sumDiffSpeed;
	}

	public void setSumDiffSpeed(BigDecimal sumDiffSpeed) {
		this.sumDiffSpeed = sumDiffSpeed;
	}

	public BigDecimal getAvgDiffSpeed() {
		return avgDiffSpeed;
	}

	public void setAvgDiffSpeed(BigDecimal avgDiffSpeed) {
		this.avgDiffSpeed = avgDiffSpeed;
	}
}