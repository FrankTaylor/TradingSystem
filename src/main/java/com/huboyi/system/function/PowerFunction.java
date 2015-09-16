package com.huboyi.system.function;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.engine.indicators.technology.pattern.bean.PowerBean;
import com.huboyi.engine.indicators.technology.pattern.bean.PowerBean.PowerType;
import com.huboyi.system.bean.PositionInfoBean;

/**
 * 交易模块中使用的中枢函数。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/15
 * @version 1.0
 */
public class PowerFunction {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(PowerFunction.class);
	
	/**
	 * 得到最后一个无包含关系的中枢。
	 * 
	 * @param noContainPowerList 无包含关系的中枢集合
	 * @return PowerBean
	 */
	public static PowerBean 
	getLastNoContainPower (final List<PowerBean> noContainPowerList) {
		if (noContainPowerList != null && !noContainPowerList.isEmpty()) {
			return noContainPowerList.get(noContainPowerList.size() - 1);
		}
		return null;
	}
	
	/**
	 * 得到有效的依次下降的中枢集合。
	 * 
	 * @param powerList 中枢集合
	 * @return List<PowerBean>
	 */
	public static List<PowerBean> 
	getOneByOneDownPowerList (final List<PowerBean> powerList) {
		
		List<PowerBean> oneByOneDownPowerList =                                                                                   // 得到还未经过特殊情况处理的依次下降的中枢集合
			getOneByOneArrangePowerList(powerList, PowerType.DOWN);
		
		if (null == oneByOneDownPowerList || oneByOneDownPowerList.size() < 2) {
			return null;
		}
		
		
		/*                                        
		 * 目前的中枢合并规则还不能处理下图的情况，所以需要后续的处理。
		 * 
		 * 处理的原因：虽然最后两个中枢时依次下跌的，但依旧在第一个中枢的范围内，由于下跌空间太小，可能会造成虚假的一买信号。
		 * 处理的策略：如果依次下降的中枢仅有两个，取依次下降中枢内的第一个中枢的前两个中枢为参考中枢。如果依次下降的两中枢，
		 *         仍都在参考中枢的价格范围内，就说明下跌空间过小，行情可能还需横向震荡，程序主动放弃本次判断。
		 *                                        
		 *  \                    
		 *  _\__________________ 
		 * |                    |               ________
		 * |                    |              |________|
		 * |                    |             /         \
		 * |                    |            /           \
		 * |                    |           /             \________
		 * |                    |          /              |        |
		 * |                    |         /               |________|
		 * |                    |        /                        \
		 * |____________________|       /                          \ 
		 *                     \       /                            \
		 *                      \_____/
		 *                      |_____|
		 *                      
		 * 图1.当连续下跌的中枢被之前的中枢包含时，由于下跌空间太小，可能会产生虚假的一买信号
		 * 
		 */
		if (oneByOneDownPowerList.size() == 2) {
			PowerBean firstPower = oneByOneDownPowerList.get(oneByOneDownPowerList.size() - 1);                                   // 得到依次向下中枢集合中的第一个中枢。
			PowerBean refer = (firstPower.getPrev() != null && firstPower.getPrev().getPrev() != null)                            // 得到依次下降中枢中第一个中枢的前一个的前一个中枢。 
			? firstPower.getPrev().getPrev() : null;
			
			if (refer != null) {
				BigDecimal HighOfOneByOneDown = firstPower.getHigh();
				BigDecimal LowOfOneByOneDown = firstPower.getNext().getLow();
				if (refer.getHigh().compareTo(HighOfOneByOneDown) == 1 && refer.getLow().compareTo(LowOfOneByOneDown) == -1) {
					return null;
				}
			}
		}
		
		return  oneByOneDownPowerList;
	}
	
	/**
	 * 得到有效的依次上升的中枢集合。
	 * 
	 * @param powerList 中枢集合
	 * @param oneBuyPositionInfo 一买仓位信息
	 * @return List<PowerBean>
	 */
	public static List<PowerBean> 
	getOneByOneUpPowerList (final List<PowerBean> powerList, final PositionInfoBean oneBuyPositionInfo) {
		
		List<PowerBean> oneByOneUpPowerList = new ArrayList<PowerBean>();                                                         // 装载有效的、无包含关系的、依次上升的中枢集合。
		List<PowerBean> tempOneByOneUpPowerList =                                                                                 // 得到临时的、无效的、无包含关系的、依次上升的中枢集合。
			getOneByOneArrangePowerList(powerList, PowerType.UP);
		
		/*
		 * 
		 * 注意：在下图中虽然中枢依次向上，但是第一个中枢的结束时间小于一买的建仓时间，所以不能算参与比较的中枢。
		 * 
		 *                  /
		 * \           ____/_
		 *  \         |______|
		 *   \_____    /
		 *   |_____|  /
		 *        \  /
		 *         \/
		 *         1B
		 * 图1.无效的依次上升的中枢
		 */
		if (tempOneByOneUpPowerList != null && oneBuyPositionInfo != null) {
			for (PowerBean power : tempOneByOneUpPowerList) {
				if (
						power.getStartKLine().getDate() >= oneBuyPositionInfo.getOpenDate() ||
						power.getEndKLine().getDate() >= oneBuyPositionInfo.getOpenDate()) {
					oneByOneUpPowerList.add(power);
				}
			}
		} else {
			if (tempOneByOneUpPowerList != null) {				
				oneByOneUpPowerList.addAll(tempOneByOneUpPowerList);
			}
		}
		
		return oneByOneUpPowerList;
	}
	
	/**
	 * 得到还未经过特殊情况处理的依次上升或下降的中枢集合。
	 * 
	 * @param powerBeanList 中枢集合
	 * @param powerType 合并类型
	 * @return List<PowerBean>
	 */
	public static List<PowerBean> 
	getOneByOneArrangePowerList (final List<PowerBean> powerList, final PowerType powerType) {
		
		if (powerList == null || powerType == null) {
			log.info("在执行得到依次上升或下降的中枢集合时参数不符合要求！[powerList = " + powerList + "] | [powerType = " + powerType + "]");
			return null;
		}
		
		if (powerList.size() < 2) {
			return null;
		}
		
		List<PowerBean> partakePowerBeanList = new ArrayList<PowerBean>();   // 用于记录参与本次比较的中枢。
		
		for (int i = (powerList.size() - 1); i >= 1; i--) {
			PowerBean lastPower = powerList.get(i);                          // 相对最后一个中枢。
			PowerBean prevPower = powerList.get(i - 1);                      // 相对倒数第二个中枢。
			
			if (powerType == PowerType.UP) {
				
				/*
				 * 相对最后一个中枢的最高价和最低价，均要高于其前一个中枢的最高价和最低价。
				 */
				if (lastPower.getHigh().compareTo(prevPower.getHigh()) == 1 &&
						lastPower.getLow().compareTo(prevPower.getLow()) == 1) {
					
					if (!partakePowerBeanList.contains(lastPower)) {
						partakePowerBeanList.add(lastPower);
					}
					
					if (!partakePowerBeanList.contains(prevPower)) {
						partakePowerBeanList.add(prevPower);
					}
					
				} else {				
					break;
				}
			} else {
				/*
				 * 相对最后一个中枢的最高价和最低价，均要低于其前一个中枢的最高价和最低价
				 */
				if (prevPower.getHigh().compareTo(lastPower.getHigh()) == 1 &&
						prevPower.getLow().compareTo(lastPower.getLow()) == 1) {
					
					if (!partakePowerBeanList.contains(lastPower)) {
						partakePowerBeanList.add(lastPower);
					}
					
					if (!partakePowerBeanList.contains(prevPower)) {
						partakePowerBeanList.add(prevPower);
					}
					
				} else {				
					break;
				}
			}
			
		}
		return partakePowerBeanList;
	}
}