package com.huboyi.strategy.function;

import java.math.BigDecimal;
import java.util.List;

import com.huboyi.indicators.technology.constant.BandType;
import com.huboyi.indicators.technology.entity.pattern.BandBean;

/**
 * 交易模块中使用的波段函数。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/15
 * @version 1.0
 */
public class BandFunction {
	
	/**
	 * 得到最后一个波段数据。
	 * 
	 * @param bandList 行情波段集合
	 * @return FractalBean
	 */
	public static BandBean getLastBand (final List<BandBean> bandList) {
		if (bandList != null && !bandList.isEmpty()) {
			return bandList.get(bandList.size() - 1);
		}
		return null;
	}
	
	/**
	 * 根据日期来查询其所在波段。
	 * 
	 * @param bandList 行情波段集合
	 * @param date 日期
	 * @return BandBean 建仓所在的波段
	 */
	public static BandBean 
	getBandBeanByDate (final List<BandBean> bandList, final Long date) {
		
		if (bandList == null || bandList.isEmpty() || date == null) { return null; }
		
		for (BandBean band : bandList) {
			// --- 计算波段的时间范围 ---
			Long startDate, endDate;
			if (band.getBandType() == BandType.UP) {
				startDate = band.getBottom().getCenter().getDate();
				endDate = band.getTop().getCenter().getDate();
			} else {
				startDate = band.getTop().getCenter().getDate();
				endDate = band.getBottom().getCenter().getDate();
			}
			
			// 判断日期是否在该波段的时间范围内。
			if ((date >= startDate) && (date < endDate)) {
				return band;
			}
		}
		
		/*
		 * 注意：如果刚产生买点信信号，此时是不能捕捉到买点所在波段的。
		 */
		
		return null;
	}
	
	/**
	 * 通过合并K线生成一个新的波段，主要用于中枢间波段力度的对比。
	 * 
	 * @param bandType 合并波段的方向
	 * @param first 第一根波段
	 * @param last 最后一根波段
	 * @return BandBean
	 */
	public static BandBean 
	mergeBand (final BandType bandType, final BandBean first, final BandBean last) {
		
		// --- 方法前检验 ---
		if (bandType == null) {
			throw new RuntimeException("在合并波段时，必须要有指明合并的方向！");
		}
		
		if (bandType == BandType.DOWN) {
			
			if (first.getTop().getCenter().getDate() > last.getTop().getCenter().getDate()) {
				StringBuilder errorMsg = new StringBuilder();
				errorMsg.append("当向下合并波段时，第一根波段的起始日期必须大于最后一根波段的起始日期！");
				errorMsg.append("[first band top date = " + first.getTop().getCenter().getDate()).append(" | ");
				errorMsg.append("[last band top date = " + last.getTop().getCenter().getDate());
				errorMsg.append("]");
				throw new RuntimeException(errorMsg.toString());
			}
			
			if (first.getBandType() == BandType.UP || last.getBandType() == BandType.UP) {
				StringBuilder errorMsg = new StringBuilder();
				errorMsg.append("当向下合并波段时，第一根波段和最后一根波段都必须是向下的！");
				errorMsg.append("[first band | bandType = " + first.getBandType()).append(" | ");
				if (first.getBandType() == BandType.UP) {
					errorMsg
					.append("start date = " + first.getBottom().getCenter().getDate())
					.append(" | ")
					.append("end date = " + first.getTop().getCenter().getDate());
				} else {
					errorMsg
					.append("start date = " + first.getTop().getCenter().getDate())
					.append(" | ")
					.append("end date = " + first.getBottom().getCenter().getDate());
				}
				errorMsg.append("]");
				
				errorMsg.append(" | ");
				
				errorMsg.append("[last band | bandType = " + last.getBandType()).append(" | ");
				if (last.getBandType() == BandType.UP) {
					errorMsg
					.append("start date = " + last.getBottom().getCenter().getDate())
					.append(" | ")
					.append("end date = " + last.getTop().getCenter().getDate());
				} else {
					errorMsg
					.append("start date = " + last.getTop().getCenter().getDate())
					.append(" | ")
					.append("end date = " + last.getBottom().getCenter().getDate());
				}
				throw new RuntimeException(errorMsg.toString());
			}
		}
		
		if (bandType == BandType.UP) {
			
			if (first.getBottom().getCenter().getDate() > last.getBottom().getCenter().getDate()) {
				StringBuilder errorMsg = new StringBuilder();
				errorMsg.append("当向上合并波段时，第一根波段的起始日期必须大于最后一根波段的起始日期！");
				errorMsg.append("[first band bottom date = " + first.getBottom().getCenter().getDate()).append(" | ");
				errorMsg.append("[last band bottom date = " + last.getBottom().getCenter().getDate());
				errorMsg.append("]");
				throw new RuntimeException(errorMsg.toString());
			}
			
			if (first.getBandType() == BandType.DOWN || last.getBandType() == BandType.DOWN) {
				StringBuilder errorMsg = new StringBuilder();
				errorMsg.append("当向上合并波段时，第一根波段和最后一根波段都必须是向上的！");
				errorMsg.append("[first band | bandType = " + first.getBandType()).append(" | ");
				if (first.getBandType() == BandType.DOWN) {
					errorMsg
					.append("start date = " + first.getTop().getCenter().getDate())
					.append(" | ")
					.append("end date = " + first.getBottom().getCenter().getDate());
				} else {
					errorMsg
					.append("start date = " + first.getBottom().getCenter().getDate())
					.append(" | ")
					.append("end date = " + first.getTop().getCenter().getDate());
				}
				errorMsg.append("]");
				
				errorMsg.append(" | ");
				
				errorMsg.append("[last band | bandType = " + last.getBandType()).append(" | ");
				if (last.getBandType() == BandType.UP) {
					errorMsg
					.append("start date = " + last.getTop().getCenter().getDate())
					.append(" | ")
					.append("end date = " + last.getBottom().getCenter().getDate());
				} else {
					errorMsg
					.append("start date = " + last.getBottom().getCenter().getDate())
					.append(" | ")
					.append("end date = " + last.getTop().getCenter().getDate());
				}
				throw new RuntimeException(errorMsg.toString());
			}
		}
		
		/*
		 * 如果合并的波段是同一根时，就直接返回第一根波段。
		 */
		if (first.isOneAndTheSame(last)) {
			return first;
		}
		
		/*
		 * 开始具体的合并措施。
		 */
		int nums = 0;                                       // 记录波段内K线的数量。
		int upNums = 0;                                     // 记录波段内阳线的数量。
		int downNums = 0;                                   // 记录波段内阴线的数量。
		BigDecimal totalVolume = BigDecimal.valueOf(0);     // 记录波段内全部K线成交量的总和。
		BigDecimal upTotalVolume = BigDecimal.valueOf(0);   // 记录波段内全部阳线成交量的总和。
		BigDecimal downTotalVolume = BigDecimal.valueOf(0); // 记录波段内全部阴线成交量的总和。
		BigDecimal totalAmount = BigDecimal.valueOf(0);     // 记录波段内全部K线成交额的总和。
		BigDecimal upTotalAmount = BigDecimal.valueOf(0);   // 记录波段内全部阳线成交额的总和。
		BigDecimal downTotalAmount = BigDecimal.valueOf(0); // 记录波段内全部阴线成交额的总和。
		
		// --- 计算波段内的各项数据 ---
		BandBean temp = first;
	
		while (temp != last.getNext()) {
			
			/*
			 * 合并各个波段内的统计数据。
			 * 注意：此次合并不考虑是否是同向波段，如果只需要合并同向波段，加上这个判断就行了（if (temp.getBandType() == bandType) {}）。
			 */
			nums += temp.getNums();
			upNums += temp.getUpNums();
			downNums += temp.getDownNums();
			
			totalVolume = totalVolume.add(temp.getTotalVolume());
			upTotalVolume = upTotalVolume.add(temp.getUpTotalVolume());
			downTotalVolume = downTotalVolume.add(temp.getDownTotalVolume());
			
			totalAmount = totalAmount.add(temp.getTotalAmount());
			upTotalAmount = upTotalAmount.add(temp.getUpTotalAmount());
			downTotalAmount = downTotalAmount.add(temp.getDownTotalVolume());
			
			temp = temp.getNext();
		}
		// --- 计算波段内的各项数据 ---
		
		BandBean newBand = new BandBean();
		newBand.setNums(nums);                              // 波段内K线的数量。
		newBand.setUpNums(upNums);                          // 波段内阳线的数量。
		newBand.setDownNums(downNums);                      // 波段内阴线的数量。
		newBand.setTotalVolume(totalVolume);                // 波段内全部K线成交量的总和。
		newBand.setUpTotalVolume(upTotalVolume);            // 波段内全部阳线成交量的总和。
		newBand.setDownTotalVolume(downTotalVolume);        // 波段内全部阴线成交量的总和。
		newBand.setTotalAmount(totalAmount);                // 波段内全部K线成交额的总和。
		newBand.setUpTotalAmount(upTotalAmount);            // 波段内全部阳线成交额的总和。
		newBand.setDownTotalAmount(downTotalAmount);        // 波段内全部阴线成交额的总和。
		
		if (bandType == BandType.UP) {
			newBand.setBottom(first.getBottom());
			newBand.setTop(last.getTop());
		}
		
		if (bandType == BandType.DOWN) {
			newBand.setTop(first.getTop());
			newBand.setBottom(last.getBottom());
		}
		
		return newBand;
	}
	
}