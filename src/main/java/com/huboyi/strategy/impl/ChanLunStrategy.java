package com.huboyi.strategy.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.huboyi.data.entity.StockDataBean;
import com.huboyi.indicators.technology.PatternAlogrithm;
import com.huboyi.indicators.technology.constant.BandType;
import com.huboyi.indicators.technology.entity.pattern.BandBean;
import com.huboyi.indicators.technology.entity.pattern.FractalBean;
import com.huboyi.indicators.technology.entity.pattern.PowerBean;
import com.huboyi.strategy.BaseStrategy;
import com.huboyi.strategy.function.BPAndSPFunction;
import com.huboyi.strategy.function.BandFunction;
import com.huboyi.strategy.function.FractalFunction;
import com.huboyi.strategy.function.PowerFunction;
import com.huboyi.strategy.function.StockDataFunction;
import com.huboyi.trader.entity.po.DealOrderPO;
import com.huboyi.trader.entity.po.EntrustOrderPO;

public class ChanLunStrategy extends BaseStrategy {
	
	/** 有效的顶底分型集合。*/
	private List<FractalBean> validFractalList;
	
	/** 波段集合。*/
	private List<BandBean> bandList;
	
	/** 中枢集合。*/
	private List<PowerBean> powerList;
	
	/** 无包含关系的中枢集合。*/
	private List<PowerBean> noContainPowerList;
	
	// ------ processStockDataBean
	
	@Override
	public boolean preProcessStockData(StockDataBean stockData, List<StockDataBean> stockDataList) {
		
		// 得到没有包含关系的 K 线集合。
		List<StockDataBean> noContainKLineList = PatternAlogrithm.getNoContainKLineList(stockDataList);
		// 得到无效的顶底分型集合。
		List<FractalBean> invalidFractalList = (PatternAlogrithm.getInvalidFractalBeanList(noContainKLineList));
		
		/* 初始化各项参数。*/
		try {
			
			validFractalList = PatternAlogrithm.getValidFractalBeanList(invalidFractalList);
			bandList = PatternAlogrithm.getBandBeanList(validFractalList);
			if (!CollectionUtils.isEmpty(bandList)) {			
				powerList = PatternAlogrithm.getPowerBeanList(bandList.get(0));
				noContainPowerList = PatternAlogrithm.getNoContainPowerBeanList(powerList);
			}
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public void processStockData(StockDataBean stockData, List<StockDataBean> stockDataList) {
		
		/*
		 * ##################################################
		 * # 1、基础条件判断                                                                                                              #
		 * ##################################################
		 */
		
		/*
		 * 1.1、存在两个及以上，依次向下排列的“参与比较的中枢” 。
		 */
		
		// 得到无包含关系的、依次下降的中枢集合。
		List<PowerBean> oneByOneDownPowerList = PowerFunction.getOneByOneDownPowerList(noContainPowerList);
		if (CollectionUtils.isEmpty(oneByOneDownPowerList) || oneByOneDownPowerList.size() < 2) {
			return;
		}
		
		/*
 		 * ##################################################
		 * # 2、形态学判断                                                                                                                  #
		 * ##################################################
		 */
		{
			/*
			 * 2.1、（相对）最后一个“参与比较中枢”（该中枢不必发生破坏） 的最后一个波段必须向下。
			 */
			BandBean lastBand = BandFunction.getLastBand(bandList);                                // 最后一个波段。
			if (lastBand.getBandType() != BandType.DOWN) {
				return;
			}
			
			/*
			 * 2.2、该向下波段的最低价要低于从若干参与比较中枢的第一个中枢开始，除该波段外，所有向下波段的最低价。
			 */
			PowerBean firstPower = oneByOneDownPowerList.get(oneByOneDownPowerList.size() - 1);                                   // 得到依次向下中枢集合中的第一个中枢。
			BandBean tempFirstBand = firstPower.getBandList().get(0);                                                             // 得到依次向下中枢集合中的第一个中枢的第一个根波段。
			while (!tempFirstBand.isOneAndTheSame(lastBand)) {
				// 如果该向下波段不是从第一个中枢开始最低的，就退出本次的一买建仓捕捉程序。
				if (lastBand.compareBandBottom(tempFirstBand, "l") != -1) {
					return;
				}
				tempFirstBand = tempFirstBand.getNext();
			}
			
			/*
			 * 2.3、当前K线不能是当下形成底分型的一部分，而且该K线的收盘价必须高于底分型中左侧和右侧K线的收盘价。
			 * 
			 * 目的：尽可能减少在捕捉买入信号过程中，由于无效的底分型，所造成的虚假买入信号，以节省大量的止损费。
			 * 代价：会使有效的建仓信号稍迟一些发出。
			 * 
			 */
			StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                      // 得到最后一根行情数据。
			FractalBean bottom = lastBand.getBottom();                                                                            // 得到最后一个波段中的底分型。
			if (!FractalFunction.isRelativelyEffectiveBottom(lastStockData, bottom)) {
				return;
			}
		}
		
		/*
		 * ##################################################
		 * # 3、动力学判断                                                                                                                  #
		 * ##################################################
		 */
		{
			
			/*
			 * 在计算时要注意把每根被比较波段的成交量和成交额累计后，再与最后一个比较波段相比。
			 * 
			 * \
			 *  \_______
			 *  |_______|
			 *          \ 实际 addTotalAmount = 100万，计算后 addTotalAmount = 300万 
			 *           \ 被比较波段2
			 *            \_______
			 *            |_______|
			 *                   \ 实际 addTotalAmount = 200万，计算后 addTotalAmount = 300万 
			 *                    \ 被比较波段1
			 *                     \_______
			 *                     |_______|
			 *                             \ 实际 addTotalAmount = 280万
			 *                              \ 比较波段
			 *                              
			 * 图1.如不事先进行成交量和成交额的累计，被比较波段1就会失去与比较波段，进行比较的机会
			 */
			List<BandBean> mergeBandOfPrvePowerList = new ArrayList<BandBean>();                                                  // 用于装载相对最后一个中枢的前一个中枢的用于力度比较的组合波段。
			BigDecimal addTotalVolume = new BigDecimal(0);                                                                        // 用于存储累计被比较波段的总成交量。
			BigDecimal addTotalAmount = new BigDecimal(0);                                                                        // 用于存储累计被比较波段的总成交额。
				
			for (int i = 0; i < oneByOneDownPowerList.size() - 1; i++) {
				PowerBean relativeLastPower = oneByOneDownPowerList.get(i);                                                       // 得到相对最后一个中枢。
				PowerBean relativePrevPower = relativeLastPower.getPrev();                                                        // 得到相对最后一个中枢的前一个中枢。
				
				if (relativePrevPower == null) {
					return;
				}

				BandBean lastBandOfRelativePrevPower =                                                                            // 得到相对最后一个中枢的前一个中枢的最后一根波段。
					relativePrevPower.getBandList().get(relativePrevPower.getBandList().size() - 1);
				BandBean mergeFirstBandOfRelativePrevPower = (lastBandOfRelativePrevPower.getBandType() == BandType.DOWN)         // 得到相对最后一个中枢的前一个中枢的参与组合的第一根波段。
	            ? lastBandOfRelativePrevPower : lastBandOfRelativePrevPower.getNext();

				BandBean firstBandOfRelativePrevPower = relativeLastPower.getReference();                                         // 得到相对最后一个中枢的第一根波段。
				BandBean mergeLastBandOfRelativeLastPower = (firstBandOfRelativePrevPower.getBandType() == BandType.DOWN)         // 得到相对最后一个中枢的参与组合的最后一根波段。
	            ? firstBandOfRelativePrevPower : firstBandOfRelativePrevPower.getPrev();

				BandBean mergeBandOfPrvePower = null;                                                                             // 得到相对最后一个中枢的前一个中枢的用于力度比较的组合波段。
				if (mergeFirstBandOfRelativePrevPower.isOneAndTheSame(mergeLastBandOfRelativeLastPower)) {
					mergeBandOfPrvePower = mergeFirstBandOfRelativePrevPower;
				} else {
					mergeBandOfPrvePower = 
						BandFunction.mergeBand(
								BandType.DOWN, 
								mergeFirstBandOfRelativePrevPower, 
								mergeLastBandOfRelativeLastPower);
				}
				
				addTotalVolume = addTotalVolume.add(mergeBandOfPrvePower.getTotalVolume());                                       // 累计被比较波段的总成交量。
				addTotalAmount = addTotalAmount.add(mergeBandOfPrvePower.getTotalAmount());                                       // 累计被比较波段的总成交额。
				
				mergeBandOfPrvePowerList.add(mergeBandOfPrvePower);
			}
			
			// 对每一条被比较波段进行事先进行成交量和成交额的累计。
			for (BandBean mergeBandOfPrvePower : mergeBandOfPrvePowerList) {
				mergeBandOfPrvePower.setTotalVolume(addTotalVolume);                                                              // 重新设置本次被比较波段的总成交量。
				mergeBandOfPrvePower.setTotalAmount(addTotalAmount);                                                              // 重新设置本次被比较波段的总成交额。
			}
			
			/*
			 * 3.1、判断最后一根下跌的组合波段和各下跌中枢间的下跌组合波段是否发生了一买背离。
			 */
			BandBean lastBand = BandFunction.getLastBand(bandList);                                // 最后一个波段。
			PowerBean lastPower = oneByOneDownPowerList.get(0);                                                                   // 最后一个中枢。
			BandBean lastBandOfLastPower = lastPower.getBandList().get(lastPower.getBandList().size() - 1);                       // 得到最后一个中枢的最后一根波段。
			BandBean mergeFirstBandOfLastPower = (lastBandOfLastPower.getBandType() == BandType.DOWN)                             // 得到最后一个中枢的第一根参与组合的波段。
			? lastBandOfLastPower : lastBandOfLastPower.getNext();
			BandBean mergeBandOfLastPower = BandFunction.mergeBand(BandType.DOWN, mergeFirstBandOfLastPower, lastBand);           // 得到最后一个中枢的用于力度比较的组合波段。
			StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                      // 得到最后一根行情数据。
			
			for (BandBean mergeBandOfPrvePower : mergeBandOfPrvePowerList) {
				if (BPAndSPFunction.isProduceOneBuyPoint(stockDataList, mergeBandOfLastPower, mergeBandOfPrvePower)) {
					System.out.println("|CHANLUN|BUY|" + lastStockData.getDate() + "|RIGHT|1|");
				}
			}
			
			/*
			 * 3.2、判断最后一根下跌波段和其组合波段是否发生背离。
			 */
			
			/*
			 * 如果连续下跌中枢后比较波段是组合型波段，就十分可能造成，组合波段内背离，而与被比较波段不背离的情况。
			 * \
			 *  \________
			 *  |________|
			 *           \
			 *            \
			 *             \
			 *              \
			 *               \________
			 *               |________|
			 *                        \
			 *                         \  
			 *                          \  /\
			 *                           \/  \
			 *                                \
			 *                                 \  /\
			 *                                  \/  \
			 *                                       \
			 *                                       
			 *   图2.组合波段内背离，而与被比较波段不背离的情况。
			 *                             
			 */
			while (!lastBand.isOneAndTheSame(mergeFirstBandOfLastPower)) {
				addTotalVolume = addTotalVolume.add(mergeFirstBandOfLastPower.getTotalVolume());                                  // 累计被比较波段的总成交量。
				addTotalAmount = addTotalAmount.add(mergeFirstBandOfLastPower.getTotalAmount());                                  // 累计被比较波段的总成交额。
				mergeFirstBandOfLastPower.setTotalVolume(addTotalVolume);                                                         // 重新设置本次被比较波段的总成交量。
				mergeFirstBandOfLastPower.setTotalAmount(addTotalAmount);                                                         // 重新设置本次被比较波段的总成交额。
				
				if (BPAndSPFunction.isProduceOneBuyPoint(stockDataList, lastBand, mergeFirstBandOfLastPower)) {
					System.out.println("|CHANLUN|BUY|" + lastStockData.getDate() + "|RIGHT|2|");
				} 
				
				// --- 累计向上波段的成交量和成交额 ---
				mergeFirstBandOfLastPower = mergeFirstBandOfLastPower.getNext();
				addTotalVolume = addTotalVolume.add(mergeFirstBandOfLastPower.getTotalVolume());                                  // 累计被比较波段的总成交量。
				addTotalAmount = addTotalAmount.add(mergeFirstBandOfLastPower.getTotalAmount());                                  // 累计被比较波段的总成交额。
				
				mergeFirstBandOfLastPower = mergeFirstBandOfLastPower.getNext();                                                  // 得到下一根向下波段。
			}
		}
	}

	@Override
	public void postProcessStockData(StockDataBean stockData, List<StockDataBean> stockDataList) {
		
	}
	
	// ------ processOrder
	
	@Override
	public void processEntrustOrder(EntrustOrderPO entrustOrderPO) {
		
	}

	@Override
	public void processCancelEntrustOrder(EntrustOrderPO entrustOrderPO) {
		
	}

	@Override
	public void processDealOrder(DealOrderPO dealOrderPO) {
		
	}
	
}