package com.huboyi.engine.indicators.technology;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.huboyi.engine.indicators.technology.energy.bean.MACDBean;
import com.huboyi.engine.indicators.technology.energy.bean.RSIBean;
import com.huboyi.engine.indicators.technology.trend.bean.BollBean;
import com.huboyi.engine.indicators.technology.trend.bean.MoveAverageBean;
import com.huboyi.engine.indicators.technology.volume.bean.VolMoveAverageBean;
import com.huboyi.engine.load.bean.StockDataBean;

/**
 * 技术指标算法类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/10/20
 * @version 1.0
 */
public class TechAlgorithm {
	
	/**
	 * 计算今日的涨停价格。
	 * 
	 * @param today 今日的行情数据
	 * @return BigDecimal
	 */
	public static BigDecimal calcLimitUp (StockDataBean today) {
		if (today.getPrev() == null) {
			return null;
		}
		
		/*
		 * 涨停价计算规则：昨日的收盘价 * 110%
		 */
		BigDecimal limitUp = 
			today.getPrev().getClose().multiply(BigDecimal.valueOf(1.1)).setScale(3, RoundingMode.HALF_UP);
		
		return limitUp;
	}
	
	/**
	 * 计算今日的跌停价格。
	 * 
	 * @param today 今日的行情数据
	 * @return BigDecimal
	 */
	public static BigDecimal calcLimitDown (StockDataBean today) {
		if (today.getPrev() == null) {
			return null;
		}
		
		/*
		 * 跌停价计算规则：昨日的收盘价 * 90%
		 */
		BigDecimal limitDown = 
			today.getPrev().getClose().multiply(BigDecimal.valueOf(0.9)).setScale(3, RoundingMode.HALF_UP);
		
		return limitDown;
	}
	
	/**
	 * 计算普通成交量的均值。
	 * </p> 注意：可用的行情数据的数量必须大于周期，且最好是最长周期的两倍或以上，若不然不能返回较完整的计算数据。
	 * 
	 * @param <T>
	 * @param sdBeanList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param n 计算普通成交量均值的周期
	 * @return List<VolMoveAverageBean> 
	 */
	public static <T extends StockDataBean> List<VolMoveAverageBean> 
	VMA (final List<T> sdBeanList, final int usefulSdBeanNums, final int n) {
		List<VolMoveAverageBean> vmaBeanList = new ArrayList<VolMoveAverageBean>();
		
		if (sdBeanList == null || sdBeanList.isEmpty() || (usefulSdBeanNums < n)) {
			return new ArrayList<VolMoveAverageBean>(0);
		}
		
		int toIndex = sdBeanList.size();
		int fromIndex = sdBeanList.size() - (usefulSdBeanNums + n);
		List<T> usefulSdBeanList = 
			(fromIndex < 0) ? sdBeanList : sdBeanList.subList(fromIndex, toIndex);
		
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
	 * @param sdBeanList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param s 短均值周期
	 * @param l 长均值周期
	 * @param m dea的周期
	 * @return List<MACDBean> 
	 */
	public static <T extends StockDataBean> List<MACDBean> 
	MACD (final List<T> sdBeanList, final int usefulSdBeanNums, final int s, final int l, final int m) {
		List<MACDBean> macdBeanList = new ArrayList<MACDBean>();
		if (null != sdBeanList && !sdBeanList.isEmpty()) {
			// 首先、计算长、短均线。
			List<MoveAverageBean> sMaBeanList = partOfMA(sdBeanList, (usefulSdBeanNums + l), s);
			List<MoveAverageBean> lMaBeanList = partOfMA(sdBeanList, (usefulSdBeanNums + l), l);
		
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
	 * @param sdBeanList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param n 计算RSI周期
	 * @return List<RSIBean> 
	 */
	public static <T extends StockDataBean> List<RSIBean> 
	RSI (final List<T> sdBeanList, final int usefulSdBeanNums, final int n) {
		List<RSIBean> rsiBeanList = new ArrayList<RSIBean>();
		
		if (sdBeanList == null || sdBeanList.isEmpty() || (usefulSdBeanNums < n)) {
			return new ArrayList<RSIBean>(0);
		}
		
		int toIndex = sdBeanList.size();
		int fromIndex = sdBeanList.size() - (usefulSdBeanNums + n);
		List<T> usefulSdBeanList = 
			(fromIndex < 0) ? sdBeanList : sdBeanList.subList(fromIndex, toIndex);
				
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
	 * @param sdBeanList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param n 计算布林带周期
	 * @return List<BollBean> 
	 */
	public static <T extends StockDataBean> List<BollBean> 
	BOLL (final List<T> sdBeanList, final int usefulSdBeanNums, final int n) {
		List<BollBean> bollBeanList = new ArrayList<BollBean>();
		if (null != sdBeanList && !sdBeanList.isEmpty()) {
			// 首先、计算中轨值（使用MA函数是因为布林带中轨值就是普通移动平均），为了提高计算速度，这里没有使用完整的均线数据。
			List<MoveAverageBean> maBeanList = partOfMA(sdBeanList, (usefulSdBeanNums + n), n);
			
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
	 * 由于在复杂度计算中多次计算完整的均线数据很慢，所以去除部分用不到的前期数据，保留部分用得到的行情数据的做法，
	 * </p> 注意：可用的行情数据的数量必须大于周期，且最好是周期的一倍或以上，若不然不能返回较完整的计算数据。
	 * 
	 * @param <T>
	 * @param sdBeanList 行情数据集合
	 * @param usefulSdBeanNums 有用的行情数据的数量
	 * @param n 计算普通平均线周期
	 * @return List<MoveAverageBean> 
	 */
	public static <T extends StockDataBean> List<MoveAverageBean> 
	partOfMA (final List<T> sdBeanList, final int usefulSdBeanNums, final int n) {
		
		if (sdBeanList == null || sdBeanList.isEmpty() || (usefulSdBeanNums < n)) {
			return new ArrayList<MoveAverageBean>(0);
		}
		
		int toIndex = sdBeanList.size();
		int fromIndex = sdBeanList.size() - (usefulSdBeanNums + n);
		List<T> usefulSdBeanList = 
			(fromIndex < 0) ? sdBeanList : sdBeanList.subList(fromIndex, toIndex);
		
		List<MoveAverageBean> maList = MA(usefulSdBeanList, n);
		return (maList.size() < usefulSdBeanNums) ? maList : maList.subList(maList.size() - usefulSdBeanNums, maList.size());
	}

	/**
	 * 计算完整的普通平均线（均线数据按照日期“从远到近”保存在集合中）。
	 * 
	 * @param <T>
	 * @param sdBeanList 行情数据集合
	 * @param n 计算普通平均线周期
	 * @return List<MoveAverageBean> 
	 */
	public static <T extends StockDataBean> List<MoveAverageBean> 
	MA (final List<T> sdBeanList, final int n) {
		List<MoveAverageBean> maBeanList = new ArrayList<MoveAverageBean>();
		if (null != sdBeanList && !sdBeanList.isEmpty()) {
			
			if (sdBeanList.size() < n) {
				return maBeanList;
			}
			
			for (int i = 0; i < sdBeanList.size(); i++) {
				// 只有当行情数据的数量大于n时才能计算其平均值。
				if (i >= (n - 1)) {
					StockDataBean sdBean = sdBeanList.get(i);
					// --- 计算移动平均 ---
					MoveAverageBean maBean = new MoveAverageBean();
					// 日期。
					maBean.setDate(sdBean.getDate());
					// 计算前的值。
					maBean.setSource(sdBean.getClose());
					
					// 计算每日收盘价之和。
					BigDecimal temp = new BigDecimal(0);
					for (int j = i; j > (i - n); j--) {
						temp = temp.add(sdBeanList.get(j).getClose());
					}
					
					// 计算移动平均。
					if (temp.doubleValue() != 0) {
						maBean.setAvg(temp.divide(BigDecimal.valueOf(n), 3, RoundingMode.HALF_UP));
					}
					maBeanList.add(maBean);
				}
				// --- 计算移动平均 ---
			}
		}
		return maBeanList;
	}
}