package com.huboyi.deal.auxiliary.graphical;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.indicators.technology.bean.pattern.BandBean;
import com.huboyi.indicators.technology.bean.pattern.PowerBean;
import com.huboyi.indicators.technology.constant.BandType;
import com.huboyi.system.po.EverySumPositionInfoPO;

/**
 * 根据计算结果生成可用于在招商证券上展示的代码。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2014/10/25
 * @version 1.0
 */
public class ZhaoShangZhengQuan {

	/** 日志。*/
	private static final Logger log = LogManager.getLogger(ZhaoShangZhengQuan.class);
	
	/** 处理日期和时间的格式类。（YYYY是国际标准ISO 8601所指定的以周来纪日的历法。yyyy是格里高利历，它以400年为一个周期，在这个周期中，一共有97个闰日，在这种历法的设计中，闰日尽可能均匀地分布在各个年份中，所以一年的长度有两种可能：365天或366天。）*/
	private static final DateFormat dataFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
	
	/**
	 * 得到展示中枢的代码。
	 * 
	 * @param powerBeanList 装载中枢的集合
	 * @return List<String>
	 */
	public static List<String> getShowPowerCode (List<PowerBean> powerBeanList) {
		List<String> codeList = new LinkedList<String>();
		if (null == powerBeanList || powerBeanList.isEmpty()) {
			log.warn("装载中枢的集合类中没有任何信息！");
			return codeList;
		}
		
		for (int i = 0; i < powerBeanList.size(); i++) {
			
			PowerBean powerBean = powerBeanList.get(i);
			
			String startDateName = "ST" + i;
			String endDateName = "ET" + i;
			
			/*
			 * String m1 = startDateName + ":=BARSLAST(DATE==" + powerBean.getStartKLine().getPrev().getDate() + " - 19000000);";
			 * 上面这条语句中计算的部分 (powerBean.getStartKLine().getPrev().getDate() + " - 19000000)最好实在程序中完成，因为如果把该计算放到招商证券的
			 * 函数中，就有可能不能正确的执行了。
			 */
			// --- 生成招商证券的专用代码 ---
//			String m1 = startDateName + ":=BARSLAST(DATE=" + (powerBean.getStartKLine().getPrev().getDate() - 19000000) + ");";
//			String m2 = endDateName + ":=BARSLAST(DATE=" + (powerBean.getEndKLine().getPrev().getDate() - 19000000) + ");";
//			
//			String m3 = "DRAWLINE(CURRBARSCOUNT=CONST(" + startDateName + "), " + powerBean.getHigh() + ", CURRBARSCOUNT=CONST(" + endDateName + "), " + powerBean.getHigh() + ", 0), LINETHICK1, COLORYELLOW;";
//			String m4 = "DRAWLINE(CURRBARSCOUNT=CONST(" + startDateName + "), " + powerBean.getLow() + ", CURRBARSCOUNT=CONST(" + endDateName + "), " + powerBean.getLow() + ", 0), LINETHICK1, COLORYELLOW;";
//			
//			String m5 = "STICKLINE(CURRBARSCOUNT=CONST(" + startDateName + "), " + powerBean.getLow() + ", " + powerBean.getHigh() + ", 0, 0), LINETHICK1, COLORYELLOW;";
//			String m6 = "STICKLINE(CURRBARSCOUNT=CONST(" + endDateName + "), " + powerBean.getLow() + ", " + powerBean.getHigh() + ", 0, 0), LINETHICK1, COLORYELLOW;";
			// --- 生成招商证券的专用代码 ---
			
			// --- 生成和讯飞狐交易师的专用代码 ---
			String m1 = startDateName + ":=BARSLAST(DATE=" + (powerBean.getStartKLine().getPrev().getDate() - 19000000) + ");";
			String m2 = endDateName + ":=BARSLAST(DATE=" + (powerBean.getEndKLine().getPrev().getDate() - 19000000) + ");";
			
			String m3 = "DRAWLINE((DATACOUNT-BARPOS)=" + startDateName + "[DATACOUNT], " + powerBean.getHigh() + ", (DATACOUNT-BARPOS)=" + endDateName + "[DATACOUNT], " + powerBean.getHigh() + ", 0), LINETHICK2,COLORYELLOW;";
			String m4 = "DRAWLINE((DATACOUNT-BARPOS)=" + startDateName + "[DATACOUNT], " + powerBean.getLow() + ", (DATACOUNT-BARPOS)=" + endDateName + "[DATACOUNT], " + powerBean.getLow() + ", 0), LINETHICK2,COLORYELLOW;";
			
			String m5 = "STICKLINE((DATACOUNT-BARPOS)=" + startDateName + "[DATACOUNT], " + powerBean.getLow() + ", " + powerBean.getHigh() + ", 0, 0), LINETHICK2,COLORYELLOW;";
			String m6 = "STICKLINE((DATACOUNT-BARPOS)=" + endDateName + "[DATACOUNT], " + powerBean.getLow() + ", " + powerBean.getHigh() + ", 0, 0), LINETHICK2,COLORYELLOW;";
			// --- 生成和讯飞狐交易师的专用代码 ---
			
			codeList.add(m1);
			codeList.add(m2);
			codeList.add(m3);
			codeList.add(m4);
			codeList.add(m5);
			codeList.add(m6);
		}
		
		return codeList;
	}
	
	/**
	 * 得到显示买卖点的代码。
	 * 
	 * @param dealRecodeBeanList 交易记录集合
	 * @return List<String>
	 */
	public static List<String> getShowBuyAndSellCode (List<EverySumPositionInfoPO> positionInfoList) {
		List<String> codeList = new LinkedList<String>();
		if (null == positionInfoList || positionInfoList.isEmpty()) {
			log.warn("装载交易记录的集合类中没有任何信息！");
			return codeList;
		}
		
		Map<Long, EverySumPositionInfoPO> buyPointMap = new LinkedHashMap<Long, EverySumPositionInfoPO>();
		Map<Long, EverySumPositionInfoPO> sellPointMap = new LinkedHashMap<Long, EverySumPositionInfoPO>();
		
		for (EverySumPositionInfoPO po : positionInfoList) {
			if (!buyPointMap.containsKey(po.getOpenDate())) {
				buyPointMap.put(po.getOpenDate(), po);
			}
			if (!po.getCloseContractCode().equalsIgnoreCase("no")) {
				if (!sellPointMap.containsKey(po.getCloseDate())) {
					sellPointMap.put(po.getCloseDate(), po);
				}
			}
		}
		
		for (Map.Entry<Long, EverySumPositionInfoPO> entry : buyPointMap.entrySet()) {
			EverySumPositionInfoPO po = entry.getValue();
			
			int openSignalDate = (int)(Long.valueOf(dataFormat.format(new Date(po.getOpenSignalDate()))) - 19000000);
			int openDate = (int)(Long.valueOf(dataFormat.format(new Date(po.getOpenDate()))) - 19000000);
			String showRate = po.getOpenPrice().multiply(new BigDecimal(0.02)).setScale(2, RoundingMode.HALF_UP).toString();
			
			StringBuilder builder = new StringBuilder();
			builder.append("DRAWICON").append("(DATE=").append(openDate).append(", LOW-"+showRate+", 1);");	
			builder.append("DRAWTEXT").append("(DATE=").append(openSignalDate).append(", LOW-"+showRate+", '"+po.getSystemOpenPoint()+"'),COLORYELLOW;");	
			codeList.add(builder.toString());
		}
		
		for (Map.Entry<Long, EverySumPositionInfoPO> entry : sellPointMap.entrySet()) {
			
			EverySumPositionInfoPO po = entry.getValue();
			
			int closeSignalDate = (int)(Long.valueOf(dataFormat.format(new Date(po.getCloseSignalDate()))) - 19000000);
			int closeDate = (int)(Long.valueOf(dataFormat.format(new Date(po.getCloseDate()))) - 19000000);
			String showRate = po.getClosePrice().multiply(new BigDecimal(0.05)).setScale(2, RoundingMode.HALF_UP).toString();
			
			StringBuilder builder = new StringBuilder();
			builder.append("DRAWICON").append("(DATE=").append(closeDate).append(", HIGH+"+showRate+", 2);");	
			builder.append("DRAWTEXT").append("(DATE=").append(closeSignalDate).append(", HIGH+"+showRate+", '"+po.getSystemClosePoint()+"'),COLORWHITE;");	
			codeList.add(builder.toString());
		}

		return codeList;
	}

	/**
	 * 得到展示波段的代码。
	 * 
	 * @param bandBeanList 装载股票行情波段的集合
	 * @return List<String>
	 */
	public static List<String> getShowBandCode (List<BandBean> bandBeanList) {
		List<String> codeList = new LinkedList<String>();
		if (null == bandBeanList || bandBeanList.isEmpty()) {
			log.warn("装载股票行情波段的集合类中没有任何信息！");
			return codeList;
		}
		
		/* 布林带代码，暂时不用
		codeList.add("MID:MA(CLOSE, 26), COLORGREEN, LINETHICK2;");
		codeList.add("RATE:=SQRT(MA(POW((CLOSE - MID), 2), 26));");
		codeList.add("UPPER:MID + (2 * RATE), COLORGREEN, LINETHICK2;");
		codeList.add("LOWER:MID - (2 * RATE), COLORGREEN, LINETHICK2;");
		*/
		
		codeList.add("M1:MA(CLOSE, 5), LINETHICK2;");
		codeList.add("M2:MA(CLOSE, 10), LINETHICK2;");
		codeList.add("M3:MA(CLOSE, 60), LINETHICK2;");
		codeList.add("M4:MA(CLOSE, 120), LINETHICK2;");
		
		for (BandBean bean : bandBeanList) {
			String cond1;   // 条件1。
			String price1;  // 起点价格。
			String cond2;   // 条件2。
			String price2;  // 终点价格。
			
			// --- 生成招商证券的专用代码 ---
			if (bean.getBandType() == BandType.UP) {
				cond1 = "DATE=" + (bean.getBottom().getCenter().getDate() - 19000000);
				price1 = "LOW";
				cond2 = "DATE=" + (bean.getTop().getCenter().getDate() - 19000000);
				price2 = "HIGH";
			} else {
				cond1 = "DATE=" + (bean.getTop().getCenter().getDate() - 19000000);
				price1 = "HIGH";
				cond2 = "DATE=" + (bean.getBottom().getCenter().getDate() - 19000000);
				price2 = "LOW";
			}
			
			// --- 生成招商证券的专用代码 ---
			StringBuilder builder = new StringBuilder();
			builder.append("DRAWLINE").append("(")
			.append(cond1).append(",")
			.append(price1).append(",")
			.append(cond2).append(",")
			.append(price2).append(",")
			.append("0)");
			
			if (bean.getBandType() == BandType.UP) {
				builder.append(",LINETHICK2,COLORRED;");
			} else {
				builder.append(",LINETHICK2,COLORCYAN;");
			}
			// --- 生成招商证券的专用代码 ---
			
			codeList.add(builder.toString());
		}
		
		return codeList;
	}

	/**
	 * 得到截取后的代码集合。
	 * 
	 * @param codeList 需要截取的代码集合
	 * @param direction 截取方向
	 * @param num 截取的数量
	 * @return List<String>
	 */
	public static List<String> 
	getCutOutCode (List<String> codeList, CutOutDirection direction, int num) {
		List<String> cutOutCodeList = new LinkedList<String>();
		if (null == codeList || codeList.isEmpty()) {
			log.warn("需要截取的代码集合中没有任何信息！");
			return codeList;
		}
		
		if (num == 0) {
			log.warn("截取的条数为0！");
			return codeList;
		}
		
		// 如果没有传入截取方向，那就把其设置为从头截取。
		direction = (direction == null) ? CutOutDirection.HEAD : direction;
		
		// 如果截取的条数超过集合的范围，就把该截取的条数甚至为集合的最大长度。
		num = (num > codeList.size()) ? codeList.size() : num;
		
		if (direction == CutOutDirection.HEAD) {
			cutOutCodeList = codeList.subList(0, num);
		} else {
			cutOutCodeList = codeList.subList(codeList.size() - num, codeList.size());
		}
		
		return cutOutCodeList;
	}
	
	/**
	 * 因为招商证券只能支持100条语句，所以需要对代码集合进行截取。
	 * 
	 * @author FrankTaylor <mailto:franktaylor@163.com>
	 * @since 2014/10/25
	 * @version 1.0
	 */
	public static enum CutOutDirection {
		/** 从头截取。*/
		HEAD,
		/** 从尾截取。*/
		TAIL
	}
}