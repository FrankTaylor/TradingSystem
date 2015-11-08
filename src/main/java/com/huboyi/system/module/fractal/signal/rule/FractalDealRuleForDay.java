package com.huboyi.system.module.fractal.signal.rule;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.data.load.bean.StockDataBean;
import com.huboyi.engine.indicators.technology.constant.BandType;
import com.huboyi.engine.indicators.technology.pattern.bean.BandBean;
import com.huboyi.engine.indicators.technology.pattern.bean.FractalBean;
import com.huboyi.engine.indicators.technology.pattern.bean.PowerBean;
import com.huboyi.system.SnapDealSignal;
import com.huboyi.system.bean.DealSignalBean;
import com.huboyi.system.bean.IndicatorsInfoBean;
import com.huboyi.system.bean.PositionInfoBean;
import com.huboyi.system.constant.DealSignal;
import com.huboyi.system.function.BPAndSPFunction;
import com.huboyi.system.function.BandFunction;
import com.huboyi.system.function.FractalFunction;
import com.huboyi.system.function.PositionFunction;
import com.huboyi.system.function.PowerFunction;
import com.huboyi.system.function.StockDataFunction;
import com.huboyi.system.module.fractal.signal.bean.FractalIndicatorsInfoBean;

/**
 * 日线级别的顶底分型交易系统进出场规则。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/1/27
 * @version 1.0
 */
public class FractalDealRuleForDay implements SnapDealSignal {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(FractalDealRuleForDay.class);

	@Override
	public DealSignalBean snapBuyToOpenSignal(
			String stockCode,
			List<StockDataBean> stockDataList,
			IndicatorsInfoBean indicatorsInfo,
			List<PositionInfoBean> positionInfoList) {
		log.info("调用 [捕捉一买建仓信号] 方法。");
		
		FractalIndicatorsInfoBean fractalIndicatorsInfo = (FractalIndicatorsInfoBean)indicatorsInfo;
		
		/*
		 * +-----------------------------------------------------------+
		 * + 建仓总纲：下注时要谨慎，尽最大努力买在右侧                                                                                          +
		 * +-----------------------------------------------------------+
		 * 
		 */
		
		/*
		 * ##################################################
		 * # 1、基础条件判断                                                                                                              #
		 * ##################################################
		 */
		
		/*
		 * 1.1、存在两个及以上，依次向下排列的“参与比较的中枢” 。
		 */
		
		// 得到无包含关系的、依次下降的中枢集合。
		List<PowerBean> oneByOneDownPowerList = 
			PowerFunction.getOneByOneDownPowerList(fractalIndicatorsInfo.getNoContainPowerBeanList());
		if (null == oneByOneDownPowerList || oneByOneDownPowerList.size() < 2) {
			return null;
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
			BandBean lastBand = BandFunction.getLastBand(fractalIndicatorsInfo.getBandBeanList());                                // 最后一个波段。
			if (lastBand.getBandType() != BandType.DOWN) {
				return null;
			}
			
			/*
			 * 2.2、该向下波段的最低价要低于从若干参与比较中枢的第一个中枢开始，除该波段外，所有向下波段的最低价。
			 */
			PowerBean firstPower = oneByOneDownPowerList.get(oneByOneDownPowerList.size() - 1);                                   // 得到依次向下中枢集合中的第一个中枢。
			BandBean tempFirstBand = firstPower.getBandList().get(0);                                                             // 得到依次向下中枢集合中的第一个中枢的第一个根波段。
			while (!tempFirstBand.isOneAndTheSame(lastBand)) {
				// 如果该向下波段不是从第一个中枢开始最低的，就退出本次的一买建仓捕捉程序。
				if (lastBand.compareBandBottom(tempFirstBand, "l") != -1) {
					return null;
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
				return null;
			}
			
			
			
			// 仓位控制：------------------------------------------------------------------------------------------------
			/*
			 * +-----------------------------------------------------------+
			 * + 仓位管控1：如果在某一波段内已建仓，则直接退出捕捉买入建仓程序。                                               +
			 * +-----------------------------------------------------------+
			 * 
			 * 风控目的：避免在某一波段内重复建仓。
			 */
			PositionInfoBean lastBuyPosition = PositionFunction.getLastNoClosePosition(positionInfoList, DealSignal.ONE_B);       // 查询出最后一笔未平仓的一买仓位。
			if (lastBuyPosition != null) {
				BandBean lastOpenBand = 
					BandFunction.getBandBeanByDate(fractalIndicatorsInfo.getBandBeanList(), lastBuyPosition.getOpenDate());       // 查询出最后一个建仓的所在波段。
				if (lastOpenBand != null && lastOpenBand.isOneAndTheSame(lastBand)) {					
					return null;
				}
			}
			
			/*
			 * +-----------------------------------------------------------+
			 * + 仓位管控2：从最后一个底分型到当前K线，如果已建仓，则直接退出捕捉买入建仓程序。                +
			 * +-----------------------------------------------------------+
			 * 
			 * 风控目的：避免在该底分型附近重复建仓。
			 */				
			if (
					lastBuyPosition != null &&
					lastBuyPosition.getOpenDate() >= bottom.getLeft().getDate() && 
					lastBuyPosition.getOpenDate() <= lastStockData.getDate()) {
				return null;
			}
			// 仓位控制：------------------------------------------------------------------------------------------------

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
					return null;
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
			BandBean lastBand = BandFunction.getLastBand(fractalIndicatorsInfo.getBandBeanList());                                // 最后一个波段。
			PowerBean lastPower = oneByOneDownPowerList.get(0);                                                                   // 最后一个中枢。
			BandBean lastBandOfLastPower = lastPower.getBandList().get(lastPower.getBandList().size() - 1);                       // 得到最后一个中枢的最后一根波段。
			BandBean mergeFirstBandOfLastPower = (lastBandOfLastPower.getBandType() == BandType.DOWN)                             // 得到最后一个中枢的第一根参与组合的波段。
			? lastBandOfLastPower : lastBandOfLastPower.getNext();
			BandBean mergeBandOfLastPower = BandFunction.mergeBand(BandType.DOWN, mergeFirstBandOfLastPower, lastBand);           // 得到最后一个中枢的用于力度比较的组合波段。
			StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                      // 得到最后一根行情数据。
			for (BandBean mergeBandOfPrvePower : mergeBandOfPrvePowerList) {
				if (BPAndSPFunction.isProduceOneBuyPoint(stockDataList, mergeBandOfLastPower, mergeBandOfPrvePower)) {
					System.out.println("|CHANLUN|BUY|" + lastStockData.getDate() + "|RIGHT|1|");
					return new DealSignalBean(lastStockData, DealSignal.ONE_B);
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
					return new DealSignalBean(lastStockData, DealSignal.ONE_B);
				} 
				
				// --- 累计向上波段的成交量和成交额 ---
				mergeFirstBandOfLastPower = mergeFirstBandOfLastPower.getNext();
				addTotalVolume = addTotalVolume.add(mergeFirstBandOfLastPower.getTotalVolume());                                  // 累计被比较波段的总成交量。
				addTotalAmount = addTotalAmount.add(mergeFirstBandOfLastPower.getTotalAmount());                                  // 累计被比较波段的总成交额。
				
				mergeFirstBandOfLastPower = mergeFirstBandOfLastPower.getNext();                                                  // 得到下一根向下波段。
			}
		}
		
		return null;
	}
	
	@Override
	public DealSignalBean snapSellToCloseSignal(
			String stockCode,
			List<StockDataBean> stockDataList,
			IndicatorsInfoBean indicatorsInfo,
			List<PositionInfoBean> positionInfoList) {
		log.info("调用 [捕捉一买平仓信号] 方法。");
		
		FractalIndicatorsInfoBean fractalIndicatorsInfo = (FractalIndicatorsInfoBean)indicatorsInfo;
		
		/*
		 * ##################################################
		 * # 1、基础条件判断                                                                                                              #
		 * ##################################################
		 */
		
		/*
		 * 1.1、如果不存在任何的一买仓位，就不用再捕捉一买平仓信号。
		 */
		PositionInfoBean lastNoClosePosition = PositionFunction.getLastNoClosePosition(positionInfoList, DealSignal.ONE_B);       // 查询出最后一笔未平仓的一买仓位。
		if (lastNoClosePosition == null) {
			return null;
		}
		
		
		/*
		 * 1.2、存在2个及以上，依次向上排列的“参与比较的中枢”。
		 */
		List<PowerBean> oneByOneUpPowerList =
			PowerFunction.getOneByOneUpPowerList(fractalIndicatorsInfo.getNoContainPowerBeanList(), lastNoClosePosition);         // 得到有效的、无包含关系的、依次上升的中枢集合。
		if (null == oneByOneUpPowerList || oneByOneUpPowerList.size() < 2) {
			return null;
		}

		/*
		 * ##################################################
		 * # 2、形态学判断                                                                                                                  #
		 * ##################################################
		 */
		{
			/*
			 * 2.1、（相对）最后一个“参与比较中枢”（该中枢不必发生破坏） 的最后一个波段必须向上。
			 */
			BandBean lastBand = BandFunction.getLastBand(fractalIndicatorsInfo.getBandBeanList());                               // 最后一个波段。
			if (lastBand.getBandType() != BandType.UP) {
				return null;
			}
			
			/*
			 * 2.2、该向上波段的最高价要高于从若干参与比较中枢的第一个中枢开始，除该波段外，所有向上波段的最高价。
			 */
			PowerBean firstPower = oneByOneUpPowerList.get(oneByOneUpPowerList.size() - 1);                                       // 得到依次向上中枢集合中的第一个中枢。
			BandBean tempFirstBand = firstPower.getBandList().get(0);                                                             // 得到依次向上中枢集合中的第一个中枢的第一个根波段。
			while (!tempFirstBand.isOneAndTheSame(lastBand)) {
				// 如果该向上波段不是从第一个中枢开始最高的，就退出本次的一买平仓捕捉程序。
				if (lastBand.compareBandTop(tempFirstBand, "h") != 1) {
					return null;
				}
				tempFirstBand = tempFirstBand.getNext();
			}
			
			/*
			 * 2.3、当前K线不能是当下形成顶分型的一部分，而且该K线的收盘价必须低于顶分型中右侧K线的收盘价。
			 * 
			 * 目的：尽可能减少在捕捉平仓信号过程中，由于无效的顶分型，所造成的虚假平仓信号，以避免错过大幅的上涨。
			 * 代价：会使有效的一买平仓平仓信号稍迟一些发出。
			 * 
			 */
			StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                      // 得到最后一根行情数据。
			FractalBean top = lastBand.getTop();                                                                                  // 得到最后一个波段中的顶分型。
			if (!FractalFunction.isRelativelyEffectiveTop(lastStockData, top)) {
				return null;
			}
			
			
			
			// 仓位控制：------------------------------------------------------------------------------------------------
			/*
			 * +-----------------------------------------------------------+
			 * + 仓位管控1：如果在某一波段内已平仓，则直接退出捕捉买入平仓程序。                                               +
			 * +-----------------------------------------------------------+
			 * 
			 * 风控目的：避免在某一波段内重复平仓。
			 */
			PositionInfoBean lastClosePosition = PositionFunction.getLastClosePosition(positionInfoList, DealSignal.ONE_B);       // 查询出最后一笔已平仓的一买仓位。
			if (lastClosePosition != null) {
				BandBean lastOpenBand = 
					BandFunction.getBandBeanByDate(fractalIndicatorsInfo.getBandBeanList(), lastClosePosition.getCloseDate());    // 查询出最后一个建仓的所在波段。
				if (lastOpenBand != null && lastOpenBand.isOneAndTheSame(lastBand)) {
					return null;
				}
			}
			
			/*
			 * +-----------------------------------------------------------+
			 * + 仓位管控2：从最后一个顶分型到当前K线，如果已平仓，则直接退出捕捉买入平仓程序。                +
			 * +-----------------------------------------------------------+
			 * 
			 * 风控目的：避免在该底分型附近重复平仓。
			 */					
			if (
					lastClosePosition != null &&
					lastClosePosition.getCloseDate() >= top.getLeft().getDate() && 
					lastClosePosition.getCloseDate() <= lastStockData.getDate()) {
				return null;
			}
			// 仓位控制：------------------------------------------------------------------------------------------------
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
			 *                             / 实际 addTotalAmount = 280万
			 *                     _______/ 比较波段
			 *                    |_______|
			 *                    / 实际 addTotalAmount = 200万，计算后 addTotalAmount = 300万 
			 *                   / 被比较波段1
			 *            ______/
			 *           |______|
			 *           / 实际 addTotalAmount = 100万，计算后 addTotalAmount = 300万 
			 *          / 被比较波段2
			 *   ______/
			 *  |______|
			 *  /  
			 * /
			 * 图1.如不事先进行成交量和成交额的累计，被比较波段1就会失去与比较波段，进行比较的机会
			 */
			List<BandBean> mergeBandOfPrvePowerList = new LinkedList<BandBean>();                                                 // 用于装载相对最后一个中枢的前一个中枢的用于力度比较的组合波段。
			BigDecimal addTotalVolume = new BigDecimal(0);                                                                        // 用于存储累计被比较波段的总成交量。
			BigDecimal addTotalAmount = new BigDecimal(0);                                                                        // 用于存储累计被比较波段的总成交额。
			
			for (int i = 0; i < oneByOneUpPowerList.size() - 1; i++) {
				PowerBean relativeLastPower = oneByOneUpPowerList.get(i);                                                         // 得到相对最后一个中枢。
				PowerBean relativePrevPower = relativeLastPower.getPrev();                                                        // 得到相对最后一个中枢的前一个中枢。
				
				if (relativePrevPower == null) {
					return null;
				}
				
				BandBean lastBandOfRelativePrevPower =                                                                            // 得到相对最后一个中枢的前一个中枢的最后一根波段。
					relativePrevPower.getBandList().get(relativePrevPower.getBandList().size() - 1);
				BandBean mergeFirstBandOfRelativePrevPower = (lastBandOfRelativePrevPower.getBandType() == BandType.UP)           // 得到相对最后一个中枢的前一个中枢的参与组合的第一根波段。
	            ? lastBandOfRelativePrevPower : lastBandOfRelativePrevPower.getNext();
				
				BandBean firstBandOfRelativePrevPower = relativeLastPower.getReference();                                         // 得到相对最后一个中枢的第一根波段。
				BandBean mergeLastBandOfRelativeLastPower = (firstBandOfRelativePrevPower.getBandType() == BandType.UP)           // 得到相对最后一个中枢的参与组合的最后一根波段。
	            ? firstBandOfRelativePrevPower : firstBandOfRelativePrevPower.getPrev();
				
				
				BandBean mergeBandOfPrvePower = null;                                                                             // 得到相对最后一个中枢的前一个中枢的用于力度比较的组合波段。
				if (mergeFirstBandOfRelativePrevPower.isOneAndTheSame(mergeLastBandOfRelativeLastPower)) {
					mergeBandOfPrvePower = mergeFirstBandOfRelativePrevPower;
				} else {
					mergeBandOfPrvePower = 
						BandFunction.mergeBand(
								BandType.UP, 
								mergeFirstBandOfRelativePrevPower, 
								mergeLastBandOfRelativeLastPower);
				}
				
				addTotalVolume = addTotalVolume.add(mergeBandOfPrvePower.getTotalVolume());                                       // 累计被比较波段的总成交量。
				addTotalAmount = addTotalAmount.add(mergeBandOfPrvePower.getTotalAmount());                                       // 累计被比较波段的总成交额。
				
				mergeBandOfPrvePowerList.add(mergeBandOfPrvePower);
			}
			
			/*
			 * 对每一条被比较波段进行事先进行成交量和成交额的累计。
			 */
			for (BandBean mergeBandOfPrvePower : mergeBandOfPrvePowerList) {
				mergeBandOfPrvePower.setTotalVolume(addTotalVolume);                                                              // 重新设置本次被比较波段的总成交量。
				mergeBandOfPrvePower.setTotalAmount(addTotalAmount);                                                              // 重新设置本次被比较波段的总成交额。				
			}
			
			/*
			 * 3.1、判断最后一根上升的组合波段和各上升中枢间的上升组合波段是否发生了一买背离。
			 */
			BandBean lastBand = BandFunction.getLastBand(fractalIndicatorsInfo.getBandBeanList());                                // 最后一个波段。
			PowerBean lastPower = oneByOneUpPowerList.get(0);                                                                     // 最后一个中枢。
	
			BandBean lastBandOfLastPower = lastPower.getBandList().get(lastPower.getBandList().size() - 1);                       // 得到最后一个中枢的最后一根波段。
			BandBean mergeFirstBandOfLastPower = (lastBandOfLastPower.getBandType() == BandType.UP)                               // 得到最后一个中枢的第一根参与组合的波段。
			? lastBandOfLastPower : lastBandOfLastPower.getNext();
			BandBean mergeBandOfLastPower = BandFunction.mergeBand(BandType.UP, mergeFirstBandOfLastPower, lastBand);             // 得到最后一个中枢的用于力度比较的组合波段。
			
			StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                      // 得到最后一根行情数据。
			for (BandBean mergeBandOfPrvePower : mergeBandOfPrvePowerList) {
				if (BPAndSPFunction.isProduceOneSellPoint(stockDataList, mergeBandOfLastPower, mergeBandOfPrvePower)) {
					System.out.println("|CHANLUN|SELL|" + lastStockData.getDate() + "|LEFT|1|");
					return new DealSignalBean(lastStockData, DealSignal.SELL_ALL);
				}
			}
			
			/*
			 * 3.2、判断最后一根上涨波段和其组合波段是否发生背离。
			 */
			
			/*
			 * 如果连续上涨中枢后比较波段是组合型波段，就十分可能造成，组合波段内背离，而与被比较波段不背离的情况。
			 * 
			 *                                           /
			 *                                      /\  /
			 *                                     /  \/
			 *                                    /
			 *                               /\  /
			 *                              /  \/
			 *                             /
			 *                 ___________/
			 *                |___________|
			 *                /
			 *               /
			 *              /
			 *             /
			 *   _________/
			 *  |_________|
			 *  /
			 * /
			 *        图3.组合波段内背离，而与被比较波段不背离的情况。
			 */
			while (!lastBand.isOneAndTheSame(mergeFirstBandOfLastPower)) {
				addTotalVolume = addTotalVolume.add(mergeFirstBandOfLastPower.getTotalVolume());                                  // 累计被比较波段的总成交量。
				addTotalAmount = addTotalAmount.add(mergeFirstBandOfLastPower.getTotalAmount());                                  // 累计被比较波段的总成交额。
				mergeFirstBandOfLastPower.setTotalVolume(addTotalVolume);                                                         // 重新设置本次被比较波段的总成交量。
				mergeFirstBandOfLastPower.setTotalAmount(addTotalAmount);                                                         // 重新设置本次被比较波段的总成交额。
				
				if (BPAndSPFunction.isProduceOneSellPoint(stockDataList, lastBand, mergeFirstBandOfLastPower)) {
					System.out.println("|CHANLUN|SELL|" + lastStockData.getDate() + "|LEFT|1|");
					return new DealSignalBean(lastStockData, DealSignal.SELL_ALL);
				} 
				
				// --- 累计向上波段的成交量和成交额 ---
				mergeFirstBandOfLastPower = mergeFirstBandOfLastPower.getNext();
				addTotalVolume = addTotalVolume.add(mergeFirstBandOfLastPower.getTotalVolume());                                  // 累计被比较波段的总成交量。
				addTotalAmount = addTotalAmount.add(mergeFirstBandOfLastPower.getTotalAmount());                                  // 累计被比较波段的总成交额。
				
				mergeFirstBandOfLastPower = mergeFirstBandOfLastPower.getNext();                                                  // 得到下一根向上波段。
			}
		}
		
		return null;
	}
	
}