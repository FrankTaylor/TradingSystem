package com.huboyi.indicators.technology;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.huboyi.data.entity.StockDataBean;
import com.huboyi.indicators.technology.constant.BandType;
import com.huboyi.indicators.technology.constant.FractalType;
import com.huboyi.indicators.technology.entity.pattern.BandBean;
import com.huboyi.indicators.technology.entity.pattern.FractalBean;
import com.huboyi.indicators.technology.entity.pattern.PowerBean;
import com.huboyi.indicators.technology.entity.pattern.PowerBean.PowerType;

/**
 * 技术形态算法类。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 1.0
 */
public class PatternAlogrithm {
	
	/**
	 * 得到没有包含关系的中枢集合。
	 * 
	 * @param powerBeanList
	 * @return List<PowerBean> 
	 * @throws CloneNotSupportedException 
	 */
	@SuppressWarnings("unused")
	public static List<PowerBean> 
	getNoContainPowerBeanList (List<PowerBean> powerBeanList) throws CloneNotSupportedException {
		List<PowerBean> noContainPowerBeanList = new ArrayList<PowerBean>();
		
		if (powerBeanList == null || powerBeanList.size() < 2) {
			return noContainPowerBeanList;
		}
		
		noContainPowerBeanList.add(powerBeanList.get(0));
		for (int i = 1; i < powerBeanList.size(); i++) {
			PowerBean prev = noContainPowerBeanList.get(noContainPowerBeanList.size() - 1); // 前一个中枢。
			PowerBean current = powerBeanList.get(i);                                       // 当前的中枢。
			
			/*
			 * 是否需要合并的判定规则：
			 * 以[prevBandMaxLow, prevBandMaxHigh]为区间，只要存在以下几种情况就需要进行合并：
			 * 
			 * 1、[prevBandMaxLow, prevBandMaxHigh] 包含 [currentBandMaxLow]；
			 * 2、[prevBandMaxLow, prevBandMaxHigh] 包含 [currentBandMaxHigh]；
			 * 3、 [currentBandMaxLow, currentBandMaxHigh] 包含 [prevBandMaxLow]；
			 * 4、 [currentBandMaxLow, currentBandMaxHigh] 包含 [prevBandMaxHigh]；
			 * 
			 */
			
			// --- 尚未使用 ---
			BigDecimal currentBandMaxHigh = current.getBandMaxHigh();                       // 当前中枢内波段的最高点。
			BigDecimal currentBandMaxLow = current.getBandMaxLow();                         // 当前中枢内波段的最低点。
			BigDecimal prevBandMaxHigh = prev.getBandMaxHigh();                             // 前一中枢的高点。
			BigDecimal prevBandMaxLow = prev.getBandMaxLow();                               // 前一中枢的低点。
			// --- 尚未使用 ---
			
			BigDecimal currentHigh = current.getHigh();                                     // 当前中枢的高点。
			BigDecimal currentLow = current.getLow();                                       // 当前中枢的低点。
			BigDecimal prevHigh = prev.getHigh();                                           // 前一中枢的高点。
			BigDecimal prevLow = prev.getLow();                                             // 前一中枢的低点。

			
			if (
//					(currentBandMaxLow.compareTo(prevBandMaxLow) == 1 && currentBandMaxLow.compareTo(prevBandMaxHigh) == -1) ||
//					(currentBandMaxHigh.compareTo(prevBandMaxLow) == 1 && currentBandMaxHigh.compareTo(prevBandMaxHigh) == -1) ||
//					
//					(prevBandMaxLow.compareTo(currentBandMaxLow) == 1 && prevBandMaxLow.compareTo(currentBandMaxHigh) == -1) ||
//					(prevBandMaxHigh.compareTo(currentBandMaxLow) == 1 && prevBandMaxHigh.compareTo(currentBandMaxHigh) == -1)) {
					
					(currentLow.compareTo(prevLow) == 1 && currentLow.compareTo(prevHigh) == -1) ||
					(currentHigh.compareTo(prevLow) == 1 && currentHigh.compareTo(prevHigh) == -1) ||
					
					(prevLow.compareTo(currentLow) == 1 && prevLow.compareTo(currentHigh) == -1) ||
					(prevHigh.compareTo(currentLow) == 1 && prevHigh.compareTo(currentHigh) == -1)) {
				
				// 把前一个中枢和当前中枢进行组合。
				PowerBean newPowerBean = mergePower(prev, current);
				
				/*
				 * 把保存无包含关系中枢集合中的最后一个中枢删除，再把新组合的中枢放到集合中去。
				 */
				noContainPowerBeanList.remove(noContainPowerBeanList.size() - 1);
				noContainPowerBeanList.add(newPowerBean);
				
				
			} else {
				/*
				 * 把当前无包含关系的中枢放到无包含关系中枢集合中。
				 * 
				 * 注意：由于当前中枢的前一个中枢可能是经过包含关系处理的中枢，所以需要重新构造指向关系。
				 */
				PowerBean copy = (PowerBean)current.clone();
				copy.setPrev(noContainPowerBeanList.get(noContainPowerBeanList.size() - 1));
				noContainPowerBeanList.add(copy);
			}
		}
		
		// 处理中枢之间的包含关系（用于对第一次处理包含后的第二次再处理）。
		return processBetweenPowerContain(noContainPowerBeanList);
	}
	
	/**
	 * 得到可能含有包含关系的中枢集合。
	 * 
	 * @param refBandBean 参照波段（通常取波段集合中的第一根）
	 * @return List<PowerBean> 
	 */
	public static List<PowerBean> 
	getPowerBeanList (BandBean refBandBean) {
		
		List<PowerBean> powerBeanList = new ArrayList<PowerBean>();
		
		while (refBandBean != null) {
			
			// 获得新的参照波段。
			BandBean newRefBandBean = getReferenceBandBean(refBandBean, refBandBean);
			
			if (newRefBandBean != null) {
				
				// --- 装载从参照波段后的第一根波段到返回的参照波段 ---
				List<BandBean> intervalList = new ArrayList<BandBean>();
				BandBean next = refBandBean.getNext();
				while (next != newRefBandBean.getNext()) {
					intervalList.add(next);
					next = next.getNext();
				}
				
				// --- 装载从参照波段后的第一根波段到返回的参照波段 ---
				
				// 如果装载间隔波段的数量大于等于3根，则说明产生了中枢。
				if (intervalList.size() >= 3) {
					PowerBean powerBean = new PowerBean();
					// 初始参照波段。
					powerBean.setReference(refBandBean);
					// 中枢包含的波段。
					powerBean.setBandList(intervalList);
					
					if (refBandBean.getBandType() == BandType.UP) {
						// 设置中枢方向向上。
						powerBean.setPowerType(PowerType.UP);
						
						// --- 计算中枢的开始与结束K线 ---
						// 中枢的开始K线。
						powerBean.setStartKLine(intervalList.get(0).getTop().getCenter());
						// 中枢的结束K线。
						if (newRefBandBean.getBandType() == BandType.UP) {
							powerBean.setEndKLine(newRefBandBean.getBottom().getCenter());
						} else {
							powerBean.setEndKLine(newRefBandBean.getTop().getCenter());
						}
						// --- 计算中枢的开始与结束K线 ---
						
						// --- 计算中枢的高低价 ---
						BigDecimal powerHigh = refBandBean.getTop().getCenter().getHigh();
						BigDecimal powerLow = refBandBean.getNext().getBottom().getCenter().getLow();
						BigDecimal powerBandMaxHigh = powerHigh, powerBandMaxLow = powerLow;
						
						BandBean temp = refBandBean.getNext().getNext();
						while (temp != newRefBandBean) {
							if (temp.getBandType() == BandType.UP) {
								if (powerHigh.compareTo(temp.getTop().getCenter().getHigh()) == 1) {
									powerHigh = temp.getTop().getCenter().getHigh();
								} else {
									powerBandMaxHigh = temp.getTop().getCenter().getHigh();
								}
							} else {
								if (powerLow.compareTo(temp.getBottom().getCenter().getLow()) == -1) {
									powerLow = temp.getBottom().getCenter().getLow();
								} else {
									powerBandMaxLow = temp.getBottom().getCenter().getLow();
								}
							}
							
							temp = temp.getNext();
						}
						// 中枢的最高价。
						powerBean.setHigh(powerHigh);
						// 中枢的最低价。
						powerBean.setLow(powerLow);
						// 中枢中波段的最高价。
						powerBean.setBandMaxHigh(powerBandMaxHigh);
						// 中枢中波段的最低价。
						powerBean.setBandMaxLow(powerBandMaxLow);
						// --- 计算中枢的高低价 ---
						
					} else {
						// 设置中枢方向向下。
						powerBean.setPowerType(PowerType.DOWN);
						
						// --- 计算中枢的开始与结束K线 ---
						// 中枢的开始K线。
						powerBean.setStartKLine(intervalList.get(0).getBottom().getCenter());
						// 中枢的结束K线。
						if (newRefBandBean.getBandType() == BandType.UP) {
							powerBean.setEndKLine(newRefBandBean.getBottom().getCenter());
						} else {
							powerBean.setEndKLine(newRefBandBean.getTop().getCenter());
						}
						// --- 计算中枢的开始与结束K线 ---
						
						// --- 计算中枢的高低价 ---
						BigDecimal powerHigh = refBandBean.getNext().getTop().getCenter().getHigh();
						BigDecimal powerLow = refBandBean.getBottom().getCenter().getLow();
						BigDecimal powerBandMaxHigh = powerHigh, powerBandMaxLow = powerLow;
						
						BandBean temp = refBandBean.getNext().getNext();
						while (temp != newRefBandBean) {
							if (temp.getBandType() == BandType.UP) {
								if (powerHigh.compareTo(temp.getTop().getCenter().getHigh()) == 1) {
									powerHigh = temp.getTop().getCenter().getHigh();
								} else {
									powerBandMaxHigh = temp.getTop().getCenter().getHigh();
								}
							} else {
								if (powerLow.compareTo(temp.getBottom().getCenter().getLow()) == -1) {
									powerLow = temp.getBottom().getCenter().getLow();
								} else {
									powerBandMaxLow = temp.getBottom().getCenter().getLow();
								}
							}
							
							temp = temp.getNext();
						}
						// 中枢的最高价。
						powerBean.setHigh(powerHigh);
						// 中枢的最低价。
						powerBean.setLow(powerLow);
						// 中枢中波段的最高价。
						powerBean.setBandMaxHigh(powerBandMaxHigh);
						// 中枢中波段的最低价。
						powerBean.setBandMaxLow(powerBandMaxLow);
						// --- 计算中枢的高低价 ---
					}
					
					if (powerBeanList.size() > 0) {
						PowerBean prev = powerBeanList.get(powerBeanList.size() - 1);
						// 上一个中枢。
						powerBean.setPrev(prev);
						// 下一个中枢。
						prev.setNext(powerBean);
					}
					
					powerBeanList.add(powerBean);
				}
			}
			
			refBandBean = newRefBandBean;
		}
		
		return powerBeanList;
	}
	
	/**
	 * 得到行情波段集合。
	 * 
	 * @param validFractalBeanList 装载有效的顶底分型集合
	 * @param stockDataList 未经处理的K线集合
	 * 
	 * @return List<BandBean> 
	 */
	
	public static List<BandBean> 
	getBandBeanList (List<FractalBean> validFractalBeanList, List<StockDataBean> stockDataList) {

		List<BandBean> bandBeanList = new ArrayList<BandBean>();
		
		if (null == validFractalBeanList || validFractalBeanList.isEmpty()) {
			return bandBeanList;
		}
		
		//--- 从分型中找出波段 ---
		for (int i = 0; i < validFractalBeanList.size(); i++) {
			// 在处理完最后一个分型后退出程序。
			if (i == validFractalBeanList.size() - 1) {
				break;
			}
			
			// 找出两个相邻的分型构造波段。
			FractalBean one = validFractalBeanList.get(i);
			
			BandBean bandBean = new BandBean();
			if (one.getFractalType() == FractalType.TOP) {
				// 波段方向。
				bandBean.setBandType(BandType.DOWN);
				// 指定波段顶部。
				bandBean.setTop(one);
				// 指定波段底部。
				bandBean.setBottom(one.getNext());
			} else {
				// 波段方向。
				bandBean.setBandType(BandType.UP);
				// 指定波段顶部。
				bandBean.setTop(one.getNext());
				// 指定波段底部。
				bandBean.setBottom(one);
			}
			
			// --- 计算该波段中包含多少K线、全部成交量和全部成交额等信息 --- 
			
			int nums = 0;                                       // 波段内K线的数量。
			int upNums = 0;                                     // 波段内阳线的数量。
			int downNums = 0;                                   // 波段内阴线的数量。

			BigDecimal totalVolume = BigDecimal.valueOf(0);     // 波段内全部K线成交量的总和。
			BigDecimal upTotalVolume = BigDecimal.valueOf(0);   // 波段内全部阳线成交量的总和。
			BigDecimal downTotalVolume = BigDecimal.valueOf(0); // 波段内全部阴线成交量的总和。
			
			BigDecimal totalAmount = BigDecimal.valueOf(0);;    // 波段内全部K线成交额的总和。
			BigDecimal upTotalAmount = BigDecimal.valueOf(0);   // 波段内全部阳线成交额的总和。
			BigDecimal downTotalAmount = BigDecimal.valueOf(0); // 波段内全部阴线成交额的总和。

			StockDataBean current = one.getCenter();
			while (current.getDate() <= one.getNext().getCenter().getDate()) {
				nums++;
				totalVolume = totalVolume.add(current.getVolume());
				totalAmount = totalAmount.add(current.getAmount());
				if (current.getClose().compareTo(current.getOpen()) != -1) {
					upNums++;
					upTotalVolume = upTotalVolume.add(current.getVolume());
					upTotalAmount = upTotalAmount.add(current.getAmount());
				} else {
					downNums++;
					downTotalVolume = downTotalVolume.add(current.getVolume());
					downTotalAmount = downTotalAmount.add(current.getAmount());
				}
				
				current = current.getNext();
			}
			// --- 计算该波段中包含多少K线、全部成交量和全部成交额等信息 --- 
			
			// --- 波段内K线数量信息 ---
			bandBean.setNums(nums);                        // 波段内K线的数量。
			bandBean.setUpNums(upNums);                    // 波段内阳线的数量。
			bandBean.setDownNums(downNums);                // 波段内阴线的数量。
			
			// --- 波段内K线成交量信息 ---
			bandBean.setTotalVolume(totalVolume);          // 波段内全部K线成交量的总和。
			bandBean.setUpTotalVolume(upTotalVolume);      // 波段内全部阳线成交量的总和。
			bandBean.setDownTotalVolume(downTotalVolume);  // 波段内全部阴线成交量的总和。
			
			// --- 波段内K线成交额信息 ---
			bandBean.setTotalAmount(totalAmount);          // 波段内全部K线成交额的总和。
			bandBean.setUpTotalAmount(upTotalAmount);      // 波段内全部阳线成交额的总和。
			bandBean.setDownTotalAmount(downTotalAmount);  // 波段内全部阴线成交额的总和。
			
			// --- 其他信息 ---
			if (bandBeanList.size() > 0) {
				BandBean prev = bandBeanList.get(bandBeanList.size() - 1);
				// 上一个波段。
				bandBean.setPrev(prev);
				// 下一个波段。
				prev.setNext(bandBean);
			}
			
			bandBeanList.add(bandBean);
		}
        // --- 从分型中找出波段 ---
		
//		/*
//		 * 根据最后一根K线的
//		 */
//		if (bandBeanList != null && !bandBeanList.isEmpty()) {
//			BandBean lastBandBean = bandBeanList.get(bandBeanList.size() - 1);                            // 得到最后一个波段。
//			Integer lastFractalData = (lastBandBean.getBandType() == BandType.UP)                         // 得到最后一个波段中最后一个分型的右侧K线的日期。
//			? lastBandBean.getTop().getRight().getDate() : lastBandBean.getBottom().getRight().getDate();
//			
//			int cycleIndex = -1;                                                                          // 计算用于循环判断的索引。
//			for (int i = (stockDataList.size() - 1); i > 0; i--) {
//				if (stockDataList.get(i).getDate().equals(lastFractalData)) {
//					cycleIndex = i;
//					break;
//				}
//			}
//			
//			if (cycleIndex == -1 || (cycleIndex == (stockDataList.size() - 1))) {
//				return bandBeanList;
//			}
//			
//			for (int i = (cycleIndex + 1); i < stockDataList.size(); i++) {
//				StockDataBean sdBean = stockDataList.get(i);
//				
//				if (lastBandBean.getBandType() == BandType.UP) {
//					if (sdBean.getHigh().compareTo(lastBandBean.getTop().getCenter().getHigh()) == 1) {
//						bandBeanList.remove(bandBeanList.size() - 1);
//						break;
//					}
//				}
//				
//				if (lastBandBean.getBandType() == BandType.DOWN) {
//					if (sdBean.getLow().compareTo(lastBandBean.getBottom().getCenter().getLow()) == -1) {
//						bandBeanList.remove(bandBeanList.size() - 1);
//						break;
//					}
//				}
//			}
//		}
		
		return bandBeanList;
	}
	
	/**
	 * 过滤掉无效的顶底分型，返回有效的顶底分型集合。
	 * 
	 * @param fractalBeanList 装载顶底分型的集合
	 * @return List<FractalBean> 
	 * @throws ParseException 
	 */
	public static List<FractalBean> 
	getValidFractalBeanList (List<FractalBean> fractalBeanList) {
		// --- 过滤无效的顶底分型 ---
		for (int i = 0; i < fractalBeanList.size(); i++) {

			// 在处理完最后一个分型后退出程序。
			if (i == fractalBeanList.size() - 1) { break; }
			
			// 如果出现-1的情况，就重置循环。
			if (i < 0) { i = 0; }
			
			// 找出两个相邻的分型进行比对。
			FractalBean one = fractalBeanList.get(i);
			FractalBean two = one.getNext();

			/*
			 * 过滤无效顶底分型的规则：
			 * 1、当one与two的分型类型一样时：
			 *   1.1、当one和two都是顶分型时：
			 *       1.1.1、当two.high >= one.high时，过滤掉one；
			 *       1.1.2、当two.high < one.high时，过滤掉two；（这种可能性较低）；
			 *   1.2、当one和two都是底分型时：
			 *       1.2.1、当two.low <= one.low时，过滤掉one；
			 *       1.2.2、当two.low > one.low时，过滤掉two；（这种可能性较低）；
			 * 
			 * 2、当one与two的分型类型不一样时：
			 *   2.1、当one和two拥有共同的K线时（有两种情况）：过滤掉two；
			 *       2.1.1、one.center == two.left
			 *       2.1.2、one.right == two.left
			 *   2.2、当one和two虽不拥有共同的K线，但是其中间没有间隔的K线时，过滤掉two；
			 */
			if (one.getFractalType() == two.getFractalType()) {                                 // 当one与two的分型类型一时。
				
				if (one.getFractalType() == FractalType.TOP) {                                  // 当one和two都是顶分型时
					if (two.getCenter().getHigh().compareTo(
							one.getCenter().getHigh()) != -1) {
						if (i > 0) {
							two.setPrev(fractalBeanList.get(i - 1));
							fractalBeanList.get(i - 1).setNext(two);
						}
						fractalBeanList.remove(one);
						
					} else {
						if (two.getNext() != null) {
							two.getNext().setPrev(one);
							one.setNext(two.getNext());
						}
						fractalBeanList.remove(two);
					}
				} else {                                                                        // 当one和two都是底分型时
					if (two.getCenter().getLow().compareTo(
							one.getCenter().getLow()) != 1) {
						if (i > 0) {
							two.setPrev(fractalBeanList.get(i - 1));
							fractalBeanList.get(i - 1).setNext(two);
						}
						fractalBeanList.remove(one);
					} else {
						if (two.getNext() != null) {
							two.getNext().setPrev(one);
							one.setNext(two.getNext());
						}
						fractalBeanList.remove(two);
					}
				}
				i--;
			} else {                                                                            // 当one与two的分型类型不一样时
				
				
				/* 
				 * 当 one 和 two 拥有共同的K线时，处理掉 two 分型。
				 * 
				 *                  |
				 * |   |      |   | | |      |
				 * | | | | OR | | |   | OR | | | |   |
				 *   |   |      |          |   | | | |
				 *                                 |
				 */
				if (
						(one.getCenter().getDate().equals(two.getLeft().getDate())) ||          // one和two拥有共同的K线
						(one.getRight().getDate().equals(two.getLeft().getDate())) ||
						one.getRight().getNext().getDate().equals(two.getLeft().getDate())) {
					
					if (one.getFractalType() == FractalType.TOP) {
						/*
						 * 之前的顶分型高于 one ，且之前的底分型高于two，就删除one保留two，这两个相邻的底分型，就交给下一次循环来处理。
						 * 
						 *     T
						 *     |        T
						 *    | |       |
						 *       |     | |
						 *        |   |  |
						 *         | |    | |
						 *          |      |
						 *          B      | 
						 *                 B
						 * 
						 *             
 						 */     
						if (i > 1) {
							if (two.getCenter().getLow().compareTo(fractalBeanList.get(i - 1).getCenter().getLow()) == -1) {
								
								if (one.getCenter().getHigh().compareTo(fractalBeanList.get(i - 2).getCenter().getHigh()) == -1) {
									two.setPrev(fractalBeanList.get(i - 1));
									fractalBeanList.get(i - 1).setNext(two);
									
									fractalBeanList.remove(one);
									i--;i--;
									continue;
								}
							}
						}
						
					} else {
						
						/*
						 * 之前的低分型低于 one ，且之前的顶分型低于two，就删除one保留two，这两个相邻的顶分型，就交给下一次循环来处理。
						 *            
						 *              T
						 *              |
						 *       T      |
						 *       |     | |
						 *      | |    |  |
						 *     |   |   |
						 *    |     | |
						 * | |       |
						 *  |        B
						 *  B
						 *               
						 */
						if (i > 1) {
							if (two.getCenter().getHigh().compareTo(fractalBeanList.get(i - 1).getCenter().getHigh()) == 1) {
								
								if (one.getCenter().getLow().compareTo(fractalBeanList.get(i - 2).getCenter().getLow()) == 1) {
									two.setPrev(fractalBeanList.get(i - 1));
									fractalBeanList.get(i - 1).setNext(two);
									
									fractalBeanList.remove(one);
									i--;i--;
									continue;
								}
								
							}
						}
					}
					
					if (two.getNext() != null) {
						two.getNext().setPrev(one);
						one.setNext(two.getNext());
					}
					
					fractalBeanList.remove(two);
					i--;
					continue;
				}
			}
		}
		
		return fractalBeanList;
	}
	
	/**
	 * 找出顶底分型集合。
	 * 
	 * @param noContainKLineList 装载没有包换关系的K线集合
	 * @return List<FractalBean> 
	 */
	public static List<FractalBean> 
	getFractalBeanList (List<StockDataBean> noContainKLineList) {
		
		List<FractalBean> fractalBeanList = new ArrayList<FractalBean>();
		
		if (null == noContainKLineList || noContainKLineList.isEmpty()) {
			return fractalBeanList;
		}
		
		for (int i = 0; i < noContainKLineList.size(); i++) {
			
			// 在处理完最后一根K线后退出程序。
			if (i == noContainKLineList.size() - 1) {
				break;
			}
			
			// 因为判断分型需要三根K线，所以要从下标1开始。
			if (i == 0) {
				continue;
			}
			
			// 取出三个行情信息。
			StockDataBean one = noContainKLineList.get(i - 1);
			StockDataBean two = noContainKLineList.get(i);
			StockDataBean three = noContainKLineList.get(i + 1);
			
			// --- 找出顶底分型 ---
			String fractalType = judgeFractalType(one, two, three);
			if (!fractalType.equalsIgnoreCase("OTHER")) {
				FractalBean fractalBean = new FractalBean();
				
				// 分型类别。
				if (fractalType.equalsIgnoreCase("TOP")) {					
					fractalBean.setFractalType(FractalType.TOP);
				} else {
					fractalBean.setFractalType(FractalType.BUTTOM);
				}
				
				// 分型左边的行情数据。
				fractalBean.setLeft(one);
				// 分型中间的行情数据。
				fractalBean.setCenter(two);
			    // 分型右边的行情数据。
				fractalBean.setRight(three);
				
				// 把上一个分型的next赋值为这个分析。
				if (fractalBeanList.size() > 0) {
					FractalBean prev = fractalBeanList.get(fractalBeanList.size() - 1);
					// 上一个分型。
					fractalBean.setPrev(prev);
					// 下一个分型。
					prev.setNext(fractalBean);
				}
				
				fractalBeanList.add(fractalBean);
			}
			// --- 找出顶底分型 ---
		}

		return fractalBeanList;
	}
	
	/**
	 * 得到没有包含关系的K线集合。
	 * 
	 * @param sdBeanList 装载行情数据的集合
	 * @return List<StockDataBean>
	 */
	public static List<StockDataBean> 
	getNoContainKLineList (List<StockDataBean> sdBeanList) {
		List<StockDataBean> noContainKLineList = new ArrayList<StockDataBean>();
		
		// 为行情数据集合产生一个副本。
		try {			
			for (StockDataBean bean : sdBeanList) {
				noContainKLineList.add((StockDataBean)bean.clone());
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		// --- 处理K线之间包含的问题 ---
		for (int i = 0; i < noContainKLineList.size(); i++) {
			
			// 在处理完最后一根K线后退出程序。
			if (i == noContainKLineList.size() - 1) {
				break;
			}
			
			/*
			 * 1、one 和 two 的作用：区分出趋势，确定下一步的合并规则（需要考虑到 two 是第一根K线时， one 和 two 如何区分出趋势的规则）；
			 * 2、two 和 three 的作用：当发生包含关系时，根据合并规则，完成K线的合并。
			 */
			StockDataBean one = (i > 0) ? noContainKLineList.get(i - 1) : null;
			StockDataBean two = noContainKLineList.get(i);
			StockDataBean three = noContainKLineList.get(i + 1);
			
			// 得到需要删除的K线，并把其删除。
			StockDataBean pklcBean = processKLineContain(one, two, three);
			if (pklcBean != null) {
				noContainKLineList.remove(pklcBean);
				i--;
			}
			
		}
		// --- 处理K线之间包含的问题 ---
		
		return noContainKLineList;
	}
	
	// ---------------------------- private method --------------------------
	
	/**
	 * 处理中枢之间的包含关系（用于对第一次处理包含后的第二次再处理）。
	 * 
	 * @param powerBeanList 经过第一次处理包含关系的中枢集合
	 * @return List<PowerBean>
	 * @throws CloneNotSupportedException
	 */
	@SuppressWarnings("unused")
	private static List<PowerBean> processBetweenPowerContain 
	(List<PowerBean> powerBeanList) throws CloneNotSupportedException {
		
		if (powerBeanList == null || powerBeanList.size() < 2) {
			return powerBeanList;
		}
		
		List<PowerBean> noContainPowerBeanList = new ArrayList<PowerBean>();
		
		// 记录是否存在包含关系，如果还有包含关系，就继续合并。
		boolean isContain = false;
		
		// 只要中枢间的高低点有重合，这两个中枢就需要合并。
		noContainPowerBeanList.add(powerBeanList.get(0));
		for (int i = 1; i < powerBeanList.size(); i++) {
			PowerBean prev = noContainPowerBeanList.get(noContainPowerBeanList.size() - 1);       // 前一个中枢。
			PowerBean current = powerBeanList.get(i);                                             // 当前的中枢。
			
			/*
			 * 是否需要合并的判定规则：
			 * 以[prevLow, prevHigh]为区间，只要存在以下几种情况就需要进行合并：
			 * 
			 * 1、[prevLow, prevHigh] 包含 [currentLow]；
			 * 2、[prevLow, prevHigh] 包含 [currentHigh]；
			 * 3、 [currentLow, currentHigh] 包含 [prevLow]；
			 * 4、 [currentLow, currentHigh] 包含 [prevHigh]；
			 * 
			 */
			
			// --- 尚未使用 ---
			BigDecimal currentBandMaxHigh = current.getBandMaxHigh(); // 当前中枢内波段的最高点。
			BigDecimal currentBandMaxLow = current.getBandMaxLow();   // 当前中枢内波段的最低点。
			BigDecimal prevBandMaxHigh = prev.getBandMaxHigh();       // 前一中枢的高点。
			BigDecimal prevBandMaxLow = prev.getBandMaxLow();         // 前一中枢的低点。
			// --- 尚未使用 ---
			
			BigDecimal currentHigh = current.getHigh();                                           // 当前中枢的高点。
			BigDecimal currentLow = current.getLow();                                             // 当前中枢的低点。
			BigDecimal prevHigh = prev.getHigh();                                                 // 前一中枢的高点。
			BigDecimal prevLow = prev.getLow();                                                   // 前一中枢的低点。
			
			if (
					(currentLow.compareTo(prevLow) == 1 && currentLow.compareTo(prevHigh) == -1) ||
					(currentHigh.compareTo(prevLow) == 1 && currentHigh.compareTo(prevHigh) == -1) ||
					
					(prevLow.compareTo(currentLow) == 1 && prevLow.compareTo(currentHigh) == -1) ||
					(prevHigh.compareTo(currentLow) == 1 && prevHigh.compareTo(currentHigh) == -1)) {
				
				// 把前一个中枢和当前中枢进行组合。
				PowerBean newPowerBean = mergePower(prev, current);
				
				/*
				 * 把保存无包含关系中枢集合中的最后一个中枢删除，再把新组合的中枢放到集合中去。
				 */
				noContainPowerBeanList.remove(noContainPowerBeanList.size() - 1);
				noContainPowerBeanList.add(newPowerBean);
				
				isContain = true;
			} else {
				/*
				 * 把当前无包含关系的中枢放到无包含关系中枢集合中。
				 * 
				 * 注意：由于当前中枢的前一个中枢可能是经过包含关系处理的中枢，所以需要重新构造指向关系。
				 */
				PowerBean copy = (PowerBean)current.clone();
				copy.setPrev(noContainPowerBeanList.get(noContainPowerBeanList.size() - 1));
				noContainPowerBeanList.add(copy);
			}
		}
		
		// 如果仍存在包含关系，就在进行一次整理。
		return (isContain) ? processBetweenPowerContain(noContainPowerBeanList) : noContainPowerBeanList;
	}
	
	/**
	 * 把前一个中枢和当前的中枢组合起来。
	 * 
	 * @param prev 前一个中枢
	 * @param current 当前的中枢
	 * @return PowerBean
	 */
	private static PowerBean mergePower (PowerBean prev, PowerBean current) {
		
		if (prev == null || current == null) {
			throw new RuntimeException("在合并中枢时，前一中枢和当前中枢都不能为null！");
		}
		
		PowerBean newPowerBean = new PowerBean();
		
		/* 中枢方向。*/
		newPowerBean.setPowerType(prev.getPowerType());
		/* 该中枢的参照波段。*/
		newPowerBean.setReference(prev.getReference());
		/*
		 * 中枢包含的波段。
		 * 需要把 “前一个中枢所包含的波段 + 当前中枢的参考波段 + 当前中枢所包含的波段” 进行组合。
		 */
		ArrayList<BandBean> bandList = new ArrayList<BandBean>();
		bandList.addAll(prev.getBandList());
		if (!prev.getBandList().get(prev.getBandList().size() - 1)
				.isOneAndTheSame(current.getReference())) {
			bandList.add(current.getReference());
			
			BandBean checkRepeat = bandList.get(0);
			for (int i = 1; i < bandList.size(); i++) {
				if (checkRepeat.isOneAndTheSame(bandList.get(i))) {
					if (prev == null || current == null) {
						throw new RuntimeException("在合并中枢时，新创建的中枢中包含了重复的波段！");
					}
				}
			}
		}
		bandList.addAll(current.getBandList());
		newPowerBean.setBandList(bandList);
		/* 中枢的开始K线。*/
		newPowerBean.setStartKLine(prev.getStartKLine());
		/* 中枢的结束K线。*/
		newPowerBean.setEndKLine(current.getEndKLine());
		
		// ---
		
		BigDecimal prevBandMaxHigh = prev.getBandMaxHigh();                             // 前一中枢的高点。
		BigDecimal prevBandMaxLow = prev.getBandMaxLow();                               // 前一中枢的低点。
		BigDecimal currentBandMaxHigh = current.getBandMaxHigh();                       // 当前中枢的高点。
		BigDecimal currentBandMaxLow = current.getBandMaxLow();                         // 当前中枢的低点。
		
		BigDecimal prevHigh = prev.getHigh();                                           // 前一中枢的高点。
		BigDecimal prevLow = prev.getLow();                                             // 前一中枢的低点。
		BigDecimal currentHigh = current.getHigh();                                     // 当前中枢的高点。
		BigDecimal currentLow = current.getLow();                                       // 当前中枢的低点。
		
		/* 中枢的最高价。*/
		if (prevHigh.compareTo(currentHigh) == 1) {
			newPowerBean.setHigh(prevHigh);
		} else {
			newPowerBean.setHigh(currentHigh);
		}
		/* 中枢的最低价。*/
		if (prevLow.compareTo(currentLow) == -1) {
			newPowerBean.setLow(prevLow);
		} else {
			newPowerBean.setLow(currentLow);
		}
		/* 中枢中波段的最高价。*/
		if (prevBandMaxHigh.compareTo(currentBandMaxHigh) == 1) {
			newPowerBean.setBandMaxHigh(prevBandMaxHigh);
		} else {
			newPowerBean.setBandMaxHigh(currentBandMaxHigh);
		}
		/* 中枢中波段的最低价。*/
		if (prevBandMaxLow.compareTo(currentBandMaxLow) == -1) {
			newPowerBean.setBandMaxLow(prevBandMaxLow);
		} else {
			newPowerBean.setBandMaxLow(currentBandMaxLow);
		}
		
		// ---
		
		/* 上一个中枢。*/
		newPowerBean.setPrev(prev.getPrev());
		/* 下一个中枢。*/
		newPowerBean.setNext(current.getNext());
		
		return newPowerBean;
	}
	
	/**
	 * 得到参照波段。其实这段代码的本意是为了计算中枢，调用者在使用该方法时，根据传入的参照波段和返回的参照波段，
	 * 来计算是否产生了中枢。规则为：如果返回的参照波段到传入的参照波段大于等于3根则产生了中枢，否则不产生中枢。
	 * 
	 * @param reference 参照波段
	 * @param current 当前比较的波段
	 * @return BandBean
	 */
	private static BandBean getReferenceBandBean (BandBean reference, BandBean current) {
		
		/*
		 * 如果参照波段为null则直接返回null。
		 */
		if (reference == null) {
			return null;
		}
		
		/*
		 * 因为当current为null时，前面可能产生的中枢，也可能没有产生中枢。
		 * 所以这时返回以参照波段为起点的最有一根波段。
		 */
		if (current == null) {
			BandBean temp = reference;
			while (temp.getNext() != null) {
				temp = temp.getNext();
			}
			return temp;
		}
		
		// --- 以参照波段为起点的初始验证条件 ---
		/*
		 * 当前波段是参照波段时有两种情况需要处理（这种情况只发生在使用者第一次进行调用时）：
		 * 1、对参照波段后的波段进行预检测，如果不符合产生中枢的情况就直接退出；
		 * 2、如果满足产中中枢的情况，则把当前波段赋值为参照波段后的第四个波段继续调用。
		 * 
		 */
		if (reference == current) {
			
			/*
			 * 算上参照波段，要形成中枢至少需要4根波段，如果连基本条件都不能满足，就不可能形成中枢。
			 */
			if (reference.getNext() == null || 
					reference.getNext().getNext() == null || 
					reference.getNext().getNext().getNext() == null) {
				return null;
			}
			
			if (reference.getBandType() == BandType.UP) {
				// 参照波段后，第一根向下波段中底分型的中间K线。
				StockDataBean oneDown = reference.getNext().getBottom().getCenter();
				// 参照波段后，第三根向下波段中底分型的中间K线。
				StockDataBean threeDown = reference.getNext().getNext().getNext().getBottom().getCenter();
				
				/*      
				 *   /\      
				 *  /  \
				 * /    \
				 *       \
				 *  
				 * 规则1、如果第一根向下波段的最低价 < 参照波段的最低价时，返回第一根向下的波段（此情况下不产生中枢）。
				 */
				if (oneDown.getLow().compareTo(reference.getBottom().getCenter().getLow()) == -1) {
					return reference.getNext();
				}
				              
				/*
				 * 算上参照波段，已经有3条波段重合了，可为什么这种情况下不产生中枢呢？
				 * 我认为这种震荡在“时间”上太短、“空间”上太小，且向上的力度明显，导致该震荡不足以成为能量聚集带。
				 * 
				 *         /\
				 *        /
				 *   /\  /
				 *  /  \/
				 * /
				 *  
				 * 规则2、如果第三根向下波段的最低价 > 参照波段的最高价时，返回第二根向上的波段（此情况下不产生中枢）。
				 */
				if (threeDown.getLow().compareTo(reference.getTop().getCenter().getHigh()) == 1) {
					return reference.getNext().getNext();
				}
				
				/*
				 *        /\
				 *   /\  /  \
				 *  /  \/    \
				 * /          \
				 *             \
				 *               
				 * 规则3、如果第三根向下波段的最低价 < 参照波段的最低价时，返回第三根向下的波段（此情况产生中枢）。
				 */
				if (threeDown.getLow().compareTo(reference.getBottom().getCenter().getLow()) == -1) {
					return reference.getNext().getNext().getNext();
				}
				
			} else {
				// 参照波段后，第一根向上波段中顶分型的中间K线。
				StockDataBean oneUp = reference.getNext().getTop().getCenter();
				// 参照波段后，第三根向上波段中顶分型的中间K线。
				StockDataBean threeUp = reference.getNext().getNext().getNext().getTop().getCenter();
				
				/*       
				 *       /
				 * \    /  
				 *  \  /
				 *   \/
				 *
				 * 规则1、如果第一根向上波段的最高价 > 参照波段的最高价时，返回第一根向上的波段（此情况下不产生中枢）。
				 */
				if (oneUp.getHigh().compareTo(reference.getTop().getCenter().getHigh()) == 1) {
					return reference.getNext();
				}
				              
				/*
				 * 算上参照波段，已经有3条波段重合了，可为什么这种情况下不产生中枢呢？
				 * 我认为这种震荡在“时间”上太短、“空间”上太小，且向下的力度明显，导致该震荡不足以成为能量聚集带。
				 * 
				 * \
				 *  \  /\
				 *   \/  \
				 *        \
				 *         \/
				 * 
				 * 规则2、如果第三根向上波段的最高价 < 参照波段的最低价时，返回第二根向下的波段（此情况下不产生中枢）。
				 */
				if (threeUp.getHigh().compareTo(reference.getBottom().getCenter().getLow()) == -1) {
					return reference.getNext().getNext();
				}
				
				/*
				 *             /
				 * \          /
				 *  \  /\    /
				 *   \/  \  /
				 *        \/
				 *               
				 * 规则3、如果第三根向上波段的最高价 > 参照波段的最高价时，返回第三根向上的波段（此情况产生中枢）。
				 */
				if (threeUp.getHigh().compareTo(reference.getTop().getCenter().getHigh()) == 1) {
					return reference.getNext().getNext().getNext();
				}
			}
			
			// 当以上这些条件都验证后，至少说明该参照波段后可以产生中枢了，则直接把当前波段设置为参照波段后的第4根波段。
			return getReferenceBandBean(reference, reference.getNext().getNext().getNext().getNext());
		}
		// --- 以参照波段为起点的初始验证条件 ---
		
		
		
		if (reference.getBandType() == BandType.UP) {
			
			/*                   /\
			 *        /\  /\    /  \
			 *   /\  /  \/  \  /    \
			 *  /  \/        \/      \
			 * /                      \
			 *                         \
			 *               
			 * 规则4、此时中枢已经形成，但只要该向下波段的低点 < 参照波段的低点时，就返回当前这根向下的波段，
			 * 注意：如果此向下波段不是参照波段后第3+N(N >= 1)根波段，则此规则不生效。
			 */
			if (current.getBandType() == BandType.DOWN) {
				if (current.getBottom().getCenter().getLow().compareTo(
						reference.getBottom().getCenter().getLow()) == -1) {
					return current;
				}
			}

			/*            
			 * 规则5、如果满足了规则1、2、3、4，则此时就可以计算中枢的范围了。
			 * 5.1、中枢的高点为：以参照波段为起点的所有高点中的低点；
			 * 5.2、中枢的低点为：以参照波段为起点的所有低点中的高点。
			 */
			BigDecimal powerHigh = reference.getTop().getCenter().getHigh();
			BigDecimal powerLow = reference.getNext().getBottom().getCenter().getLow();
			
			BandBean temp = reference.getNext().getNext();
			while (temp != current) {
				if (temp.getBandType() == BandType.UP) {
					if (powerHigh.compareTo(temp.getTop().getCenter().getHigh()) == 1) {
						powerHigh = temp.getTop().getCenter().getHigh();
					}
				} else {
					if (powerLow.compareTo(temp.getBottom().getCenter().getLow()) == -1) {
						powerLow = temp.getBottom().getCenter().getLow();
					}
				}
				
				temp = temp.getNext();
			}
			
			
			/*
			 * 规则6、当中枢形成后，不论是上升还是下降的波段只要不在中枢范围内就算破坏了该中枢，返回该波段的前一个波段。
			 * 
			 * 这时不论是向上的波段还是向下的波段都只需要分别判断一种情况：
			 * 
			 *                   
			 *         /\  /\
			 *    /\  /  \/  \
			 *   /  \/        \
			 *  /              \/
			 * /
			 * 1、如果当前波段是向上的，那就只需要判断向上波段的最高点是否超过中枢的低点。
			 *                  
			 *                 /\
			 *                /
			 * 		   /\    /
			 *    /\  /  \  /
			 *   /  \/    \/
			 *  /
			 * 2、如果当前波段是向下的，那就只需要判断向下波段的最低点是否超过中枢的高点。
			 * 
			 * 因为，如果当前波段是向上的，那其前一波段就是向下的，且向下波段的低点只能出现在中枢内或低于中枢的低点，
			 * 否则该波段就是不符合规则的，那既然是这样，就只用考虑该向下波段低点低于中枢低点的情况了。同理，如果当前
			 * 波段是向下的，那其前一波段就是向上的，该向上波段的前一向上波段的高点只能出现在中枢内或超过中枢的高点。
			 */
			
			if (current.getBandType() == BandType.UP) {                                   // 当前波段是向上的。
				if (current.getTop().getCenter().getHigh().compareTo(powerLow) == -1) {   // 只需判断向上波段的高点是否大于等于中枢的低点。
					return current.getPrev();
				}
			} else {                                                                      // 当前波段是向下的。
				if (current.getBottom().getCenter().getLow().compareTo(powerHigh) == 1) { // 需要判断向下波段的低点是否大于中枢的高点。
					return current.getPrev();
				}
			}
		} else {
			/*                     
			 *                       /
			 * \                    /
			 *  \  /\          /\  /
			 *   \/  \  /\    /  \/
			 *        \/  \  /
			 *             \/
			 *           
			 * 规则4、此时中枢已经形成，但只要该向上波段的高点 > 参照波段的高点时，就返回当前这根向上的波段，
			 * 注意：如果此向上波段不是参照波段后第3+N(N >= 1)根波段，则此规则不生效。
			 */
			if (current.getBandType() == BandType.UP) {
				if (current.getTop().getCenter().getHigh().compareTo(
						reference.getTop().getCenter().getHigh()) == 1) {
					return current;
				}
			}

			/*            
			 * 规则5、如果满足了规则1、2、3、4，则此时就可以计算中枢的范围了。
			 * 5.1、中枢的高点为：以参照波段为起点的所有高点中的低点；
			 * 5.2、中枢的低点为：以参照波段为起点的所有低点中的高点。
			 */
			BigDecimal powerHigh = reference.getNext().getTop().getCenter().getHigh();
			BigDecimal powerLow = reference.getBottom().getCenter().getLow();
			
			BandBean temp = reference.getNext().getNext();
			while (temp != current) {
				if (temp.getBandType() == BandType.UP) {
					if (powerHigh.compareTo(temp.getTop().getCenter().getHigh()) == 1) {
						powerHigh = temp.getTop().getCenter().getHigh();
					}
				} else {
					if (powerLow.compareTo(temp.getBottom().getCenter().getLow()) == -1) {
						powerLow = temp.getBottom().getCenter().getLow();
					}
				}
				
				temp = temp.getNext();
			}
			
			/*
			 * 规则6、当中枢形成后，不论是上升还是下降的波段只要不在中枢范围内就算破坏了该中枢，返回该波段的前一个波段。
			 * 
			 * 这时不论是向上的波段还是向下的波段都只需要分别判断一种情况：
			 *                      
			 *                      /\
			 *                     /
			 * \                  /
			 *  \    /\          /
			 *   \  /  \    /\  /
			 *    \/    \  /  \/
			 *           \/
			 * 1、如果当前波段是向下的，那就只需要判断向下波段的最低点是否超过中枢的高点。
			 *                  
			 * \
			 *  \    /\
			 *   \  /  \    /\
			 *    \/    \  /  \
			 *           \/    \
			 *                  \
			 *                   \/
			 *                    
			 * 2、如果当前波段是向下的，那就只需要判断向下波段的最低点是否超过中枢的低点。
			 */
			
			if (current.getBandType() == BandType.DOWN) {                                   // 当前波段是向下的。
				if (current.getBottom().getCenter().getLow().compareTo(powerHigh) == 1) {   // 只需要判断向下波段的最低点是否超过中枢的高点。
					return current.getPrev();
				}
			} else {                                                                        // 当前波段是向上的。
				if (current.getTop().getCenter().getHigh().compareTo(powerLow) == -1) {     // 只需要判断向下波段的最低点是否超过中枢的低点。
					return current.getPrev();
				}
			}

		}
		
		return getReferenceBandBean(reference, current.getNext());
	}
	
	/**
	 * 根据给定的三个价格，来判断这些价格组成的分型形态。
	 * 
	 * @param one 第一个价格
	 * @param two 第二个价格
	 * @param three 第三个价格
	 * @return String “TOP”：顶分型；“BOTTOM”：底分型；“OTHER”：不是分型
	 */
	private static String 
	judgeFractalType (StockDataBean one, StockDataBean two, StockDataBean three) {
		String fractalType = "OTHER";
		
		/*
		 * 顶分型的判定标准：
		 * 1、[two的最高价] “大于” [one的最高价]和[three的最高价]；
		 * 2、[two的最低价] “大于” [one的最低价]和[three的最低价]。
		 */
		if ((two.getHigh().compareTo(one.getHigh()) == 1) && (two.getHigh().compareTo(three.getHigh()) == 1)) {
			if ((two.getLow().compareTo(one.getLow()) == 1) && (two.getLow().compareTo(three.getLow()) == 1)) {
				fractalType = "TOP";
			}
		}
		
		/*
		 * 底分型的判定标准：
		 * 1、[two的最低价] “小于” [one的最低价]和[three的最低价]；
		 * 2、[two的最高价] “小于” [one的最高价]和[three的最高价]。
		 */
		if ((two.getLow().compareTo(one.getLow()) == -1) && (two.getLow().compareTo(three.getLow()) == -1)) {
			if ((two.getHigh().compareTo(one.getHigh()) == -1) && (two.getHigh().compareTo(three.getHigh()) == -1)) {
				fractalType = "BOTTOM";
			}
		}
		
		return fractalType;
	}
	
	
	
	/**
	 * 处理K线的包含关系。
	 * 1、返回null：表示没有包含关系；
	 * 2、返回two：表示three包含two，其调用程序中需要把two从集合中删除；
	 * 3、返回three：表示two包含three或three等于two，其调用程序中需要把three从集合中删除；
	 * 
	 * @param one 第一个价格
	 * @param two 第二个价格
	 * @param three 第三个价格
	 * @return StockDataBean
	 */
	private static StockDataBean 
	processKLineContain (StockDataBean one, StockDataBean two, StockDataBean three) {
		/*
		 * 在处理K线合并时的规则：
		 * 1、相等包含关系条件：N等于N+1——即(N的最高价 == N+1的最高价) && (N的最低价 == N+1的最低价)时，采取返回N的策略；
		 * 
		 * 2、无包含的关系条件：N高于N+1——即(N的最高价 > N+1的最高价) && (N的最低价 > N+1的最低价)时，采取返回null的策略；
		 * 3、无包含的关系条件：N低于N+1——即(N的最高价 < N+1的最高价) && (N的最低价 < N+1的最低价)时，采取返回null的策略；
		 * 
		 * 4、前包含的关系条件：N包含N+1——即(N的最高价 > N+1的最高价) && (N的最低价 < N+1的最低价)时，
		 *   4.1、当N的最高价 > N-1的最高价时，采取把N+1的最低价赋值给N的最低价，返回N+1的策略；
		 *   4.2、当N的最低价 < N-1的最低价时，采取把N+1的最高价赋值给N的最高价，返回N+1的策略；
		 *   
		 * 5、后包含的关系条件：N+1包含N——即(N的最高价 < N+1的最高价) && (N的最低价 > N+1的最低价)时，
		 *   5.1、当N的最高价 > N-1的最高价时，采取把N的最低价赋值给N+1的最低价，返回N的策略；
		 *   5.2、当N的最低价 < N-1的最低价时，采取把N的最高价赋值给N+1的最高价，返回N的策略；
		 */
		if ((two.getHigh().compareTo(three.getHigh()) == 0) &&             // 1、相等包含关系条件：N等于N+1
				(two.getLow().compareTo(three.getLow()) == 0)) {
			return two;
		} else if ((two.getHigh().compareTo(three.getHigh()) == 1) &&      // 2、无包含的关系条件：N高于N+1
				(two.getLow().compareTo(three.getLow()) == 1)) {
			return null;
		} else if ((two.getHigh().compareTo(three.getHigh()) == -1) &&     // 3、无包含的关系条件：N低于N+1
				(two.getLow().compareTo(three.getLow()) == -1)) {
			return null;
		} else {
			// --- 判断N和N-1的趋势 ---
			/*
			 * 1、如果one为null时：
			 *   1.1、two.close >= two.open，趋势向上；
			 *   1.2、two.close < two.open，趋势向下。
			 *   
			 * 2、如果one不为null时（由于是从第一个行情数据开始合并的，所以one和two不存在包含的问题）：
			 *   2.1、two.high >= one.high，趋势向上；
			 *   2.2、two.low < one.low，趋势向下。
			 */
			String trend = "UP";
			if (one == null) {
				if (two.getClose().compareTo(two.getOpen()) == -1) {					
					trend = "DOWN";
				}
			} else {
				if (two.getLow().compareTo(one.getLow()) == -1) {
					trend = "DOWN";
				}
			}
			// --- 判断N和N-1的趋势 ---
			
			if ((two.getHigh().compareTo(three.getHigh()) != -1) &&         // 4、前包含的关系条件：N包含N+1
					(two.getLow().compareTo(three.getLow()) != 1)) {
				if (trend.equalsIgnoreCase("UP")) {
					two.setLow(three.getLow());
				} else {
					two.setHigh(three.getHigh());
				}
				return three;
			} else {                                                        // 5、后包含的关系条件：N+1包含N
				if (trend.equalsIgnoreCase("UP")) {
					three.setLow(two.getLow());
				} else {
					three.setHigh(two.getHigh());
				}
				return two;
			}
		}
	}
	
	/**
	 * 
	 * 得到下一个交易日的日期，返回格式为：yyyyMMdd。
	 * 
	 * @deprecated
	 * @param currentDealDate 当前交易日期
	 * @return Integer
	 * @throws ParseException
	 * 
	 */
	@SuppressWarnings("unused")
	private static Integer 
	getNextDealDate (Integer currentDealDate) throws ParseException {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		Calendar c = Calendar.getInstance();
		c.setTime(dateFormat.parse(String.valueOf(currentDealDate.intValue())));
		
		/*
		 * 日期计算规则：如果当前日期是周五（即：c.get(Calendar.DAY_OF_WEEK) == 6），
		 * 则向后增加3天（周六日不交易），否则向后增加1天。
		 * 
		 * 注意：这里没有考虑5.1、10.1、春节等问题。
		 */
		if (c.get(Calendar.DAY_OF_WEEK) == 6) {
			c.add(Calendar.DAY_OF_WEEK, 3);
		} else {
			c.add(Calendar.DAY_OF_WEEK, 1);
		}
		return Integer.valueOf(dateFormat.format(c.getTime()));
	}
}