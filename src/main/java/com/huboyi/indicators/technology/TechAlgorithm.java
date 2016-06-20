package com.huboyi.indicators.technology;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.huboyi.data.entity.StockDataBean;
import com.huboyi.indicators.technology.constant.SingleMaPattern;
import com.huboyi.indicators.technology.entity.energy.MACDBean;
import com.huboyi.indicators.technology.entity.energy.RSIBean;
import com.huboyi.indicators.technology.entity.trend.BollBean;
import com.huboyi.indicators.technology.entity.trend.MoveAverageBean;
import com.huboyi.indicators.technology.entity.trend.MoveAverageSpeedChangeBean;
import com.huboyi.indicators.technology.entity.trend.MoveAverageStatisticsBean;
import com.huboyi.indicators.technology.entity.volume.VolMoveAverageBean;

/**
 * 技术指标算法类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class TechAlgorithm {
	
	/**
	 * 计算今日的涨停价格（如果入参为 null 将返回 null）。</p>
	 * <b>涨停价计算规则</b>：昨日的收盘价 * 110%
	 * 
	 * @param today 今日的行情数据
	 * @return BigDecimal
	 */
	public static BigDecimal calcLimitUp(StockDataBean today) {
		if (today.getPrev() == null) {
			return null;
		}
		
		BigDecimal limitUp = 
			today.getPrev().getClose()
			.multiply(BigDecimal.valueOf(1.1))
			.setScale(3, RoundingMode.HALF_UP);
		
		return limitUp;
	}
	
	/**
	 * 计算今日的跌停价格（如果入参为 null 将返回 null）。</p>
	 * <b>跌停价计算规则</b>：昨日的收盘价 * 90%
	 * @param today 今日的行情数据
	 * @return BigDecimal
	 */
	public static BigDecimal calcLimitDown(StockDataBean today) {
		if (today.getPrev() == null) {
			return null;
		}
		
		BigDecimal limitDown = 
			today.getPrev().getClose()
			.multiply(BigDecimal.valueOf(0.9))
			.setScale(3, RoundingMode.HALF_UP);
		
		return limitDown;
	}
	
	/**
	 * 计算普通成交量的均值。</p>
	 * <b>注意</b>：可用的行情数据的数量必须大于周期，且最好是最长周期的两倍或以上，若不然不能返回较完整的计算数据。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param n 计算普通成交量均值的周期
	 * @return List<VolMoveAverageBean> 
	 */
	public static <T extends StockDataBean> List<VolMoveAverageBean> 
	VMA(final List<T> stockDataList, final int usefulSdBeanNums, final int n) {
				
		if (stockDataList == null || stockDataList.isEmpty() || (usefulSdBeanNums < n)) {
			return new ArrayList<VolMoveAverageBean>(0);
		}
		
		List<VolMoveAverageBean> vmaBeanList = new ArrayList<VolMoveAverageBean>();
		int toIndex = stockDataList.size();
		int fromIndex = stockDataList.size() - (usefulSdBeanNums + n);
		List<T> usefulSdBeanList = 
			(fromIndex < 0) ? stockDataList : stockDataList.subList(fromIndex, toIndex);
		
		/*
		 * 由于计算均值的方法中使用的值为收盘价，而在这里却为成交量，为了不修改代码，方便的计算均值，
		 * 这里采用了构造若干个收盘价为成交量的StockDataBean对象，且放到集合中。
		 */
		List<StockDataBean>  tempSdBeanList = new ArrayList<StockDataBean>();
		for (StockDataBean sdBean : usefulSdBeanList) {
			StockDataBean tempSdBean = new StockDataBean();
			tempSdBean.setDate(sdBean.getDate());
			tempSdBean.setClose(sdBean.getVolume());
			
			tempSdBeanList.add(tempSdBean);
		}
		
		// 计算成交量均值。
		List<MoveAverageBean> maBeanList = MA(tempSdBeanList, n);
		
		// 把计算结果放到VolMoveAverageBean对象中。
		for (MoveAverageBean maBean : maBeanList) {
			VolMoveAverageBean vmaBean = new VolMoveAverageBean();
			vmaBean.setDate(maBean.getDate());
			vmaBean.setSource(maBean.getSource());
			vmaBean.setAvg(maBean.getAvg());
			
			vmaBeanList.add(vmaBean);
		}
		
		return (vmaBeanList.size() < usefulSdBeanNums) ? vmaBeanList : vmaBeanList.subList(vmaBeanList.size() - usefulSdBeanNums, vmaBeanList.size());
	}
	
	/**
	 * 计算MACD。
	 * </p> 注意：可用的行情数据的数量必须大于周期，且最好是最长周期的两倍或以上，若不然不能返回较完整的计算数据。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param s 短均值周期
	 * @param l 长均值周期
	 * @param m dea的周期
	 * @return List<MACDBean> 
	 */
	public static <T extends StockDataBean> List<MACDBean> 
	MACD(final List<T> stockDataList, final int usefulSdBeanNums, final int s, final int l, final int m) {
		List<MACDBean> macdBeanList = new ArrayList<MACDBean>();
		if (null != stockDataList && !stockDataList.isEmpty()) {
			// 首先、计算长、短均线。
			List<MoveAverageBean> sMaBeanList = partOfMA(stockDataList, (usefulSdBeanNums + l), s);
			List<MoveAverageBean> lMaBeanList = partOfMA(stockDataList, (usefulSdBeanNums + l), l);
		
			if (sMaBeanList.isEmpty() || lMaBeanList.isEmpty()) {
				return macdBeanList;
			}
			
			// --- 计算DIFF ---
			/*
			 * 由于计算均值的方法之能接受StockDataBean的集合，为了方便的计算DEA，
			 * 所以这里采用StockDataBean来存储DIFF的值。
			 */
			List<StockDataBean> diffBeanList = new ArrayList<StockDataBean>();
			for (int i = 0; i < sMaBeanList.size(); i++) {
				if (lMaBeanList.size() > i) {
					// 取出长短均线的值。
					MoveAverageBean sMaBean = sMaBeanList.get(i);
					MoveAverageBean lMaBean = lMaBeanList.get(i);
					
					// 只有同一天的才能计算diff差值。
					if (sMaBean.getDate().intValue() == lMaBean.getDate().intValue()) {
						// 为了方便计算DEA，这里也要给StockDataBean赋值。
						StockDataBean tempSdBean = new StockDataBean();
						// 日期。
						tempSdBean.setDate(sMaBean.getDate());
						// DIFF。
						tempSdBean.setClose(sMaBean.getAvg().subtract(lMaBean.getAvg()).setScale(10, RoundingMode.HALF_UP));
						
						diffBeanList.add(tempSdBean);
					}
				}
			}
			// --- 计算DIFF ---
			
			// --- 计算DEA ---
			List<MoveAverageBean> deaBeanList = MA(diffBeanList, m);
			// --- 计算DEA ---
			
			for (int i = 0; i < deaBeanList.size(); i++) {
				MACDBean macdBean = new MACDBean();
				// 日期。
				macdBean.setDate(deaBeanList.get(i).getDate());
				// DIFF。
				macdBean.setDiff(deaBeanList.get(i).getSource().setScale(3, RoundingMode.HALF_UP));
				// DEA。
				macdBean.setDea(deaBeanList.get(i).getAvg().setScale(3, RoundingMode.HALF_UP));
				// MACD。
				macdBean.setMacd(macdBean.getDiff().subtract(
						macdBean.getDea()).multiply(
								BigDecimal.valueOf(2)).setScale(3, RoundingMode.HALF_UP));
				
				macdBeanList.add(macdBean);
			}
		}
		
		return (macdBeanList.size() < usefulSdBeanNums) ? macdBeanList : macdBeanList.subList(macdBeanList.size() - usefulSdBeanNums, macdBeanList.size());
	}
	
	/**
	 * 计算RSI。
	 * </p> 注意：可用的行情数据的数量必须大于周期，且最好是周期的一倍或以上，若不然不能返回较完整的计算数据。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param n 计算RSI周期
	 * @return List<RSIBean> 
	 */
	public static <T extends StockDataBean> List<RSIBean> 
	RSI(final List<T> stockDataList, final int usefulSdBeanNums, final int n) {
		List<RSIBean> rsiBeanList = new ArrayList<RSIBean>();
		
		if (stockDataList == null || stockDataList.isEmpty() || (usefulSdBeanNums < n)) {
			return new ArrayList<RSIBean>(0);
		}
		
		int toIndex = stockDataList.size();
		int fromIndex = stockDataList.size() - (usefulSdBeanNums + n);
		List<T> usefulSdBeanList = 
			(fromIndex < 0) ? stockDataList : stockDataList.subList(fromIndex, toIndex);
				
		for (int i = 0; i < usefulSdBeanList.size(); i++) {
			StockDataBean sdBean = usefulSdBeanList.get(i);
			
			// 只有当行情数据的数量大于n时才能计算其平均值。
			if (i >= (n - 1)) {
				// --- 计算RSI ---
				RSIBean rsiBean = new RSIBean();
				// 日期。
				rsiBean.setDate(sdBean.getDate());
				
				BigDecimal maxTotal = new BigDecimal(0);
				BigDecimal absTotal = new BigDecimal(0);
				
				for (int j = i; j > (i - n); j--) {
					StockDataBean tempStockData = usefulSdBeanList.get(j);
					
					BigDecimal difference = (tempStockData.getPrev() != null) 
					? tempStockData.getClose().subtract(tempStockData.getPrev().getClose()) : new BigDecimal(0);
					
					
					if (difference.doubleValue() >= 0) {
						maxTotal = maxTotal.add(difference);
					}
					
					absTotal = absTotal.add(difference.abs());
				}
				
				BigDecimal upAvg = (maxTotal.doubleValue() == 0)                                                        // 计算周期内上涨的平均价格。
				? new BigDecimal(0) : maxTotal.divide(new BigDecimal(n), 5, RoundingMode.HALF_UP);
				BigDecimal downAvg = (absTotal.doubleValue() == 0)                                                      // 计算周期内下跌的平均价格。
				? new BigDecimal(0) : absTotal.divide(new BigDecimal(n), 5, RoundingMode.HALF_UP);
				BigDecimal rsi = (upAvg.doubleValue() == 0 || downAvg.doubleValue() == 0)                               // 计算RSI。
				? new BigDecimal(0) : upAvg.divide(downAvg, 3, RoundingMode.HALF_UP).multiply(new BigDecimal(100)); 
				rsiBean.setRsi(rsi);
				
				rsiBeanList.add(rsiBean);
			}
			// --- 计算RSI ---
		}

		return (rsiBeanList.size() < usefulSdBeanNums) ? rsiBeanList : rsiBeanList.subList(rsiBeanList.size() - usefulSdBeanNums, rsiBeanList.size());
	}
	
	/**
	 * 计算布林带。
	 * </p> 注意：可用的行情数据的数量必须大于周期，且最好是周期的两倍或以上，若不然不能返回较完整的计算数据。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param n 计算布林带周期
	 * @return List<BollBean> 
	 */
	public static <T extends StockDataBean> List<BollBean> 
	BOLL(final List<T> stockDataList, final int usefulSdBeanNums, final int n) {
		List<BollBean> bollBeanList = new ArrayList<BollBean>();
		if (null != stockDataList && !stockDataList.isEmpty()) {
			// 首先、计算中轨值（使用MA函数是因为布林带中轨值就是普通移动平均），为了提高计算速度，这里没有使用完整的均线数据。
			List<MoveAverageBean> maBeanList = partOfMA(stockDataList, (usefulSdBeanNums + n), n);
			
			// 其次、计算布林带。
			for (int i = 0; i < maBeanList.size(); i++) {
				/*
				 * 可以这样判断中轨是否有值是因为，中轨值就是均值，而计算均值时的就已经使用了这样的条件（i >= (n - 1)）。
				 */
				// 只有中轨有值才进行后续的计算。
				if (i >= (n - 1)) {
					
					// --- 计算布林带 ---
					MoveAverageBean maBean = maBeanList.get(i);
					BollBean bollBean = new BollBean();
					// 日期。
					bollBean.setDate(maBean.getDate());
					
					
					BigDecimal md = new BigDecimal(0);
					// --- 计算波动率MD ---
					for (int j = i; j > (i - n); j--) {
						// 计算收盘价和均价的差值。
						/*
						 * 注意：公式里利用的是maBeanList.get(j).getSource()，而不是过去理解的：差值 = 每日的价格 - 计算日的均值，
						 * 而是：差值 = 每日的价格 - 每日的均值。
						 */
						double subtract = maBeanList.get(j).getSource().subtract(maBeanList.get(j).getAvg()).doubleValue();
						// 计算差值的平方根。
						BigDecimal subtractPow = BigDecimal.valueOf(Math.pow(subtract, 2)).setScale(10, RoundingMode.HALF_UP);
						// 把差值的平方根进行累加。
						md = md.add(subtractPow);
					}
					
					if (md.doubleValue() > 0) {
						// 计算差值平方根的均值。
						md = md.divide(BigDecimal.valueOf(n), 10, RoundingMode.HALF_UP);
						// 对差值平方根的均值进行开方，计算出波动。
						md = BigDecimal.valueOf(Math.sqrt(md.doubleValue())).setScale(5, RoundingMode.HALF_UP);
					}
					// 波动率。
					bollBean.setMdValue(md);
					// --- 计算波动率MD ---
					
					// 上轨值 = 均值 + 2 * MD。
					bollBean.setUpValue(maBean.getAvg().add(md.multiply(BigDecimal.valueOf(2))).setScale(3, RoundingMode.HALF_UP));
					// 中轨值 = 均值。
					bollBean.setMiddleValue(maBean.getAvg());
					// 下轨值 = 均值  - 2 * MD。
					bollBean.setDownValue(maBean.getAvg().subtract(md.multiply(BigDecimal.valueOf(2))).setScale(3, RoundingMode.HALF_UP));
					
					bollBeanList.add(bollBean);
					// --- 计算布林带 ---
				}
			}
		}
		
		return (bollBeanList.size() < usefulSdBeanNums) ? bollBeanList : bollBeanList.subList(bollBeanList.size() - usefulSdBeanNums, bollBeanList.size());
	}
	
	/**
	 * 计算均线中速度的改变。
	 * 
	 * @param maList 均线结果集
	 * @param timeRangeOfLongTrend 均线运行速度的取值范围
	 * @param speedSpanOfLongTrend 均线运行速度的取值跨度
	 * @return MaSpeedChangeResult
	 */
	public static <T extends StockDataBean> 
	MoveAverageSpeedChangeBean MASC(final List<MoveAverageBean> maList, int timeRangeOfLongTrend, int speedSpanOfLongTrend) {
		
		// --- 参数整理 ---
		if (maList == null || maList.isEmpty()) { return null; }
		
		timeRangeOfLongTrend = (timeRangeOfLongTrend == 0 || timeRangeOfLongTrend > maList.size()) ? maList.size() : timeRangeOfLongTrend;
		speedSpanOfLongTrend = (speedSpanOfLongTrend == 0 || speedSpanOfLongTrend > maList.size()) ? 2 : speedSpanOfLongTrend;
		
		// --- 速度计算 ---
		/* 某段均线中运动速度的集合。*/
		List<BigDecimal> speedList = new ArrayList<BigDecimal>();
		
		for (int i = (maList.size() - 1); i >= (maList.size() - timeRangeOfLongTrend); i--) {
			
			if ((i - speedSpanOfLongTrend) < 0) { break; }
			
			MoveAverageBean last = maList.get(i);                                                                       // 在均线跨度范围内，最后一个均线数据。
			MoveAverageBean first = maList.get(i - speedSpanOfLongTrend);                                               // 在均线跨度范围内，第一个均线数据。
			
			BigDecimal speed = last.getAvg()                                                                            // 均线跨度范围内的运行速度。
			.subtract(first.getAvg())
			.divide(new BigDecimal((speedSpanOfLongTrend + 1)), 8, RoundingMode.HALF_UP);
			
			speedList.add(speed);                                                                                       // 把均线跨度范围内的运行速度放入集合中。
		}
		
		if (speedList.size() < (timeRangeOfLongTrend / 2)) { return null; }
		
		// --- 速变分析 ---
		/* 某段均线中运动速度的总和。*/
		BigDecimal sumSpeed  = new BigDecimal(0);
		/* 某段均线中运动速度之间差值的集合。*/
		List<BigDecimal> diffSpeedList = new ArrayList<BigDecimal>();
		
		for (int i = 0; i < speedList.size(); i++) {
			
			BigDecimal last = speedList.get(i);                                                                         // 得到某段均线中的最后一个运动速度。
			sumSpeed = sumSpeed.add(last);                                                                              // 计算某段均线中运动速度的总和。
			
			if ((i + 1) >= speedList.size()) { break; }
			
			BigDecimal first = speedList.get(i + 1);                                                                    // 得到某段均线中的第一个运动速度。
			diffSpeedList.add(last.subtract(first));                                                                    // 把某段均线中最后一个和第一个运动速度的差值放入集合。
		}
		
		/* 某段均线中运动速度的均值。*/
		BigDecimal avgSpeed = sumSpeed.divide(new BigDecimal(speedList.size()), 5, RoundingMode.HALF_UP);
		
		/* 某段均线中运动速度之间差值的总和。*/
		BigDecimal sumDiffSpeed = new BigDecimal(0);
		for (BigDecimal subSpeed : diffSpeedList) { sumDiffSpeed = sumDiffSpeed.add(subSpeed); }
		/* 某段均线中运动速度之间差值的均值。*/
		BigDecimal avgDiffSpeed = sumDiffSpeed.divide(new BigDecimal(diffSpeedList.size()), 5, RoundingMode.HALF_UP);
		
		// --- 构造结果对象 ---
		MoveAverageSpeedChangeBean masc = new MoveAverageSpeedChangeBean();
		
		// 之前的speedList是倒序的，这里修改为正序。
		List<BigDecimal> tempSpeedList = new ArrayList<BigDecimal>();
		for (int i = speedList.size() - 1; i >= 0; i--) {
			tempSpeedList.add(speedList.get(i));
		}
		
		masc.setSpeedList(tempSpeedList);
		masc.setSumSpeed(sumSpeed);
		masc.setAvgSpeed(avgSpeed);
		
		// 之前的diffSpeedList是倒序的，这里修改为正序。
		List<BigDecimal> tempDiffSpeedList = new ArrayList<BigDecimal>();
		for (int i = diffSpeedList.size() - 1; i >= 0; i--) {
			tempDiffSpeedList.add(diffSpeedList.get(i));
		}
		
		masc.setDiffSpeedList(tempDiffSpeedList);
		masc.setSumDiffSpeed(sumDiffSpeed);
		masc.setAvgDiffSpeed(avgDiffSpeed);
		
		return masc;
	}
	
	/**
	 * 计算均线的形态。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param cycle 均线周期
	 * @param compareNums 均线内需要比较的均线个数
	 * @param backwardsNums 以倒数第N个K线为最后的K线点，backwardsNums只为为0或负数
	 * @return MoveAverageStatisticsBean
	 */
	public static <T extends StockDataBean> MoveAverageStatisticsBean 
	MAS(final List<T> stockDataList, final int cycle, final int compareNums, int backwardsNums) {
		
		// --- 参数整理 ---
		backwardsNums = (backwardsNums > 0) ? 0 : backwardsNums;                                                                                           // backwardsNums只能为0或负数。
		
		List<StockDataBean> tempSdBeanList = new ArrayList<StockDataBean>(stockDataList);
		if (backwardsNums < 0 && (stockDataList.size() > Math.abs(backwardsNums))) {                                                                       // 如果 backwardsNums < 0 ，则需要重新计算K线集合。
			tempSdBeanList = tempSdBeanList.subList(0, (tempSdBeanList.size() - Math.abs(backwardsNums)));
		}
		
		int needKLineNums = (compareNums > cycle) ? ((2 * cycle) + compareNums) : (2 * cycle);                                                             // 计算在均线数据计算的过程中所需的K线数量。

		// --- 计算均线 ---
		List<MoveAverageBean> sourceMaList = TechAlgorithm.partOfMA(tempSdBeanList, needKLineNums, cycle);                                                 // 得到均线集合。

		// 如果没有均线数据，就直接返回“未知形态”。
		if (sourceMaList == null || sourceMaList.isEmpty()) {
			return new MoveAverageStatisticsBean().setMaList(sourceMaList).setPattern(SingleMaPattern.UNKNOWN);
		}
		
		List<MoveAverageBean> targetMaList =                                                                                                               // 得到一定数量的，本次比较所需要用到的均线数据。
			(sourceMaList.size() < compareNums) ? sourceMaList : sourceMaList.subList(sourceMaList.size() - compareNums, sourceMaList.size());                             
		
		// --- 数据分析 ---
		int upNums = 0;                                                                                                                                    // 在给定的均线数量内，累计两相邻均线升高的数量。
		int downNums = 0;                                                                                                                                  // 在给定的均线数量内，累计两相邻均线下降的数量。
		for (int i = (targetMaList.size() - 1); i > 0; i--) {
			MoveAverageBean current = targetMaList.get(i);
			MoveAverageBean prev = targetMaList.get(i - 1);
			
			if (current.getAvg().compareTo(prev.getAvg()) == 1) {
				upNums++;
			} else {
				downNums++;
			}
		}
		
		double upRate = new BigDecimal(upNums).divide(new BigDecimal(compareNums), 3, RoundingMode.HALF_UP).doubleValue();                                 // 计算“升高数量”在参与比较均线数量的占比。
		double downRate = new BigDecimal(downNums).divide(new BigDecimal(compareNums), 3, RoundingMode.HALF_UP).doubleValue();                             // 计算“下降数量”在参与比较均线数量的占比。0

		MoveAverageBean last = targetMaList.get(targetMaList.size() - 1);                                                                                  // 得到给定均线集合内的最后一个均线数据。
		MoveAverageBean first = targetMaList.get(0);                                                                                                       // 得到给定均线集合内的第一个均线数据。
		double speed = last.getAvg().subtract(first.getAvg()).divide(new BigDecimal(compareNums), 3, RoundingMode.HALF_UP).doubleValue();                  // 计算在单个时间内均线的“升高或下降”的速度。
		
		// --- 根据分析的均线数据，构造 MoveAverageStatisticsBean 对象。
		MoveAverageStatisticsBean mas = new MoveAverageStatisticsBean().setMaList(sourceMaList).setUpRate(upRate).setDownRate(downRate).setSpeed(speed);   // 创建均线结果类。
		
		// 当最后一个均线高于第一个均线，且上升占比大于下降占比时，返回“上升形态”。
		if ((last.getAvg().compareTo(first.getAvg()) == 1) && upRate > downRate) {
			return mas.setPattern(SingleMaPattern.UP);
		}
		
		// 当最后一个均线低于第一个均线，且下降占比大于上升占比时，返回“下降形态”。
		if ((last.getAvg().compareTo(first.getAvg()) == -1) && downRate > upRate) {
			return mas.setPattern(SingleMaPattern.DOWN);
		}
		
		return mas.setPattern(SingleMaPattern.SHOCK);
	}
	
	/**
	 * 由于在复杂度计算中多次计算完整的均线数据很慢，所以去除部分用不到的前期数据，保留部分用得到的行情数据的做法，
	 * </p> 注意：可用的行情数据的数量必须大于周期，且最好是周期的一倍或以上，若不然不能返回较完整的计算数据。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param n 计算普通平均线周期
	 * @return List<MoveAverageBean> 
	 */
	public static <T extends StockDataBean> List<MoveAverageBean> 
	partOfMA(final List<T> stockDataList, final int usefulSdBeanNums, final int n) {
		
		if (CollectionUtils.isEmpty(stockDataList) || (usefulSdBeanNums < n)) {
			return new ArrayList<MoveAverageBean>(0);
		}
		
		int toIndex = stockDataList.size();
		int fromIndex = stockDataList.size() - (usefulSdBeanNums + n);
		List<T> usefulSdBeanList = 
			(fromIndex < 0) ? stockDataList : stockDataList.subList(fromIndex, toIndex);
		
		List<MoveAverageBean> maList = MA(usefulSdBeanList, n);
		return (maList.size() < usefulSdBeanNums) ? maList : maList.subList(maList.size() - usefulSdBeanNums, maList.size());
	}

	/**
	 * 计算完整的普通平均线（均线数据按照日期“从远到近”保存在集合中）。
	 * 
	 * @param <T>
	 * @param stockDataList 行情数据集合
	 * @param n 计算普通平均线周期
	 * @return List<MoveAverageBean> 
	 */
	public static <T extends StockDataBean> List<MoveAverageBean> 
	MA(final List<T> stockDataList, final int n) {
		
		if (CollectionUtils.isEmpty(stockDataList) || stockDataList.size() < n) {
			return new ArrayList<MoveAverageBean>(0);
		}
		
		List<MoveAverageBean> maBeanList = new ArrayList<MoveAverageBean>(stockDataList.size());
		
		for (int i = 0; i < stockDataList.size(); i++) {
			
			// 只有当行情数据的数量大于n时才能计算其平均值。
			if (i < (n - 1)) { continue; }
			
			StockDataBean sdBean = stockDataList.get(i);
			
			// --- 计算移动平均 ---
			MoveAverageBean maBean = new MoveAverageBean();
			// 日期。
			maBean.setDate(sdBean.getDate());
			// 计算前的值。
			maBean.setSource(sdBean.getClose());
			
			// 计算收盘价之和。
			BigDecimal temp = BigDecimal.valueOf(0);
			for (int j = i; j > (i - n); j--) {
				temp = temp.add(stockDataList.get(j).getClose());
			}
			
			// 计算移动平均。
			if (temp.doubleValue() != 0) {
				maBean.setAvg(temp.divide(BigDecimal.valueOf(n), 3, RoundingMode.HALF_UP));
			}
			
			maBeanList.add(maBean);
		}
		
		return maBeanList;
	}
}