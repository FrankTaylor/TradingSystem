package com.huboyi.system.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.huboyi.system.bean.PositionInfoBean;

/**
 * 交易模块中使用的仓位函数。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/12
 * @version 1.0
 */
public class PositionFunction {
	
	/**
	 * 查询出某一买点，尚未平仓的最后一笔仓位信息（按照open_date + open_time 倒序）。
	 * 
	 * @param positionInfoList 仓位集合
	 * @param dealSignalType 交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	public static PositionInfoBean getLastNoClosePosition (List<PositionInfoBean> positionInfoList, DealSignal dealSignalType) {
		List<PositionInfoBean> allNoClosePositionList = getAllNoClosePositionList(positionInfoList, dealSignalType);   // 查询出某一买点，尚未平仓的全部仓位信息（按照open_date + open_time 倒序）。
		return (!allNoClosePositionList.isEmpty()) ? allNoClosePositionList.get(0) : null;
	}
	
	/**
	 * 查询出某一买点，已平仓的最后一笔仓位信息（按照close_date + close_time 倒序）。
	 * 
	 * @param positionInfoList 仓位集合
	 * @param dealSignalType 交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	public static PositionInfoBean getLastClosePosition (List<PositionInfoBean> positionInfoList, DealSignal dealSignalType) {
		List<PositionInfoBean> allClosePositionList = getAllClosePositionList(positionInfoList, dealSignalType);   // 查询出某一买点，已平仓的全部仓位信息（按照close_date + close_time 倒序）。
		return (!allClosePositionList.isEmpty()) ? allClosePositionList.get(0) : null;
	}
	
	/**
	 * 查询出某一买点，尚未平仓的全部仓位信息（按照open_date + open_time 倒序）。
	 * 
	 * @param positionInfoList 仓位集合
	 * @param dealSignalType 交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	public static List<PositionInfoBean> getAllNoClosePositionList (List<PositionInfoBean> positionInfoList, DealSignal dealSignalType) {
		
		// --- 查询到所有的仓位信息。
		List<PositionInfoBean> allPositionList = getAllPositionList(positionInfoList, dealSignalType);            // 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
		if (allPositionList == null || allPositionList.isEmpty()) { return new ArrayList<PositionInfoBean>(); }
	
		// --- 找出未平仓的仓位信息。
		List<PositionInfoBean> allNoClosePositionList = new ArrayList<PositionInfoBean>();                        // 装载某一买点，尚未平仓的全部仓位信息。
		for (PositionInfoBean position : allPositionList) {
			if (
					position.getCloseContractCode() == null ||
					position.getCloseContractCode().trim().equals("") ||
					position.getCloseContractCode().equalsIgnoreCase("no")) {
				allNoClosePositionList.add(position);
			}
		}
		
		// --- 按照open_date + open_time 降序。
		Collections.sort(allNoClosePositionList, new Comparator<PositionInfoBean>() {
			@Override
			public int compare(PositionInfoBean o1, PositionInfoBean o2) {
				return (o1.getOpenDate() > o2.getOpenDate()) ? -1  :
					   (o1.getOpenDate() < o2.getOpenDate()) ? 1   :
			           0;
			}
		});
		
		return allNoClosePositionList;
	}
	
	/**
	 * 查询出某一买点，已平仓的全部仓位信息（按照close_date + close_time 倒序）。
	 * 
	 * @param positionInfoList 仓位集合
	 * @param dealSignalType 交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	public static List<PositionInfoBean> getAllClosePositionList (List<PositionInfoBean> positionInfoList, DealSignal dealSignalType) {
		
		// --- 查询到所有的仓位信息。
		List<PositionInfoBean> allPositionList = getAllPositionList(positionInfoList, dealSignalType);            // 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
		if (allPositionList == null || allPositionList.isEmpty()) { return new ArrayList<PositionInfoBean>(); }
		
		// --- 找出已平仓的仓位信息。
		List<PositionInfoBean> allClosePositionList = new ArrayList<PositionInfoBean>();                          // 装载某一买点，已平仓的全部仓位信息。
		for (PositionInfoBean position : allPositionList) {
			if (
					position.getCloseContractCode() != null &&
					!position.getCloseContractCode().trim().equals("") &&
					!position.getCloseContractCode().equalsIgnoreCase("no")) {
				allClosePositionList.add(position);
			}
		}
		
		// --- 按照close_date + close_time 降序。
		Collections.sort(allClosePositionList, new Comparator<PositionInfoBean>() {
			@Override
			public int compare(PositionInfoBean o1, PositionInfoBean o2) {
				return (o1.getCloseDate() > o2.getCloseDate()) ? -1  :
					   (o1.getCloseDate() < o2.getCloseDate()) ? 1   :
			           0;
			}
		});
		return allClosePositionList;
	}
	
	/**
	 * 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
	 * 
	 * @param positionInfoList 仓位集合
	 * @param dealSignalType 交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	public static List<PositionInfoBean> getAllPositionList (List<PositionInfoBean> positionInfoList, DealSignal dealSignalType) {
		// 把符合买点的仓位信息装载到集合中。
		List<PositionInfoBean> tempPositionInfoList = new ArrayList<PositionInfoBean>();
		if (null != positionInfoList && !positionInfoList.isEmpty()) {
			for (PositionInfoBean positionInfo : positionInfoList) {
				if (positionInfo.getSystemOpenPoint().equalsIgnoreCase(dealSignalType.getType())) {
					tempPositionInfoList.add(positionInfo);
				}
			}
		}
		
		return tempPositionInfoList;
	}
}