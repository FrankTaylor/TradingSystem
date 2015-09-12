package com.huboyi.system.module.fractal.signal.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.huboyi.engine.indicators.technology.TechAlgorithm;
import com.huboyi.engine.indicators.technology.pattern.bean.BandBean;
import com.huboyi.engine.indicators.technology.pattern.bean.BandBean.BandType;
import com.huboyi.engine.indicators.technology.pattern.bean.FractalBean;
import com.huboyi.engine.indicators.technology.pattern.bean.FractalBean.FractalType;
import com.huboyi.engine.indicators.technology.trend.bean.MoveAverageBean;
import com.huboyi.engine.load.bean.StockDataBean;
import com.huboyi.system.SnapDealSignal;
import com.huboyi.system.bean.DealSignalBean;
import com.huboyi.system.bean.IndicatorsInfoBean;
import com.huboyi.system.bean.PositionInfoBean;
import com.huboyi.system.constant.DealSignalEnum;
import com.huboyi.system.module.fractal.signal.bean.FractalDataCalcResultBean;
import com.huboyi.system.module.fractal.signal.bean.FractalDealSignalBean;
import com.huboyi.system.module.fractal.signal.bean.FractalIndicatorsInfoBean;
import com.huboyi.system.module.fractal.signal.constant.FractalDealSignalEnum;
import com.huboyi.system.module.fractal.signal.constant.SingleMaPatternEnum;

/**
 * 长期趋势交易系统进出场规则。
 * 
 * @author FrankTaylor <mailto:franktaylor@163.com>
 * @since 2015/9/12
 * @version 1.0
 */
public class LongTimeTrendDealRuleForDay implements SnapDealSignal {
	
	/** 日志。*/
	private static final Logger log = LogManager.getLogger(LongTimeTrendDealRuleForDay.class);

	@Override
	public DealSignalBean snapBuyToOpenSignal(
			String stockCode,
			List<StockDataBean> stockDataList,
			IndicatorsInfoBean indicatorsInfo,
			List<PositionInfoBean> positionInfoList) {
		log.info("调用 [捕捉长期趋势建仓信号] 方法。"); 
		
		FractalIndicatorsInfoBean fractalIndicatorsInfo = (FractalIndicatorsInfoBean)indicatorsInfo;
		
		/*
		 * +-----------------------------------------------------------+
		 * + 长期趋势建仓战法本质：只有当长期趋势向上，股价充分回调，使股价在未来具有充分上涨空间时  +
		 * + 才可以运用的战法。                                                                                                                                          +                                                                                       +
		 * +-----------------------------------------------------------+
		 */
		
		/*
		 * ##################################################
		 * # 1、势                                                                                                                                  #
		 * ##################################################
		 * 
		 * 在牛市行情中任何一个点位（时间）买入，（1）只要牛市的行情仍能延续一段时间；（2）只要投资标的不出现什么问题（有炒作点更好）；那么在理论上都是正确的。
		 * 但由于大盘的顶底是不可预测的，有可能你刚在一个相对的低点买入，没过多久大盘就见顶了，如果此时你还没有出场，就有可能造成损失。
		 * 
		 * 所以说判断长期趋势的强弱，对于辨别走势的牛熊是重要的，而对于辨别买/卖点的准确性（风险的高低）而言， 就不是那么重要了。
		 * 
		 * 另外还需要注意的是，由于牛市出现的次数较少，所以该战法在本质上是“低准确性，高收益”的。
		 */
		
		// ---------------------------- 1.1、判断长期趋势 ----------------------------
		/*
		 * 只有下列条件都满足时，才能证明长期趋势稳定向上：
		 * 
		 * 1.1.1、前10天的120日均线的上升速度 > 0；
		 * 1.1.2、近10天的120日均线的上升速度 >= 0.003；（发现利润要趁早）
		 */
		MaDataResult befMa120DataResult = maPattern(stockDataList, 120, 10, -10);                                                 // 得到前10天的120日均线的计算结果。
		MaDataResult curMa120DataResult = maPattern(stockDataList, 120, 10, 0);                                                   // 得到近10天的120日均线的计算结果。
		if (
				// 1.1.1
				(befMa120DataResult == null || 
				befMa120DataResult.getPattern() != SingleMaPatternEnum.UP ||
				befMa120DataResult.getSpeed() < 0) ||
				
				// 1.1.2
				(curMa120DataResult == null ||
				curMa120DataResult.getPattern() != SingleMaPatternEnum.UP ||
				curMa120DataResult.getSpeed() < 0.003)
				
			) {
			
			return null;
		}
		
		// ---------------------------- 1.2、判断中期趋势 ----------------------------
		/*
		 * 中期趋势是长期趋势未来发展的催化剂，若其向上发展的力度较弱，则不能建立仓位。
		 * 
		 * 1.2.1、近60日均线在120日均线之上。（中期趋势仍强于长期趋势）
		 * 1.2.2、近60日均线成上升状态。（中期趋势仍健康发展）
		 * 1.2.3、近60日均线运动速度的5日均线 >= 0。（中期趋势的运动速度还未出现走弱的迹象）
		 */
		MaDataResult curMa60DataResult = maPattern(stockDataList, 60, 5, 0);                                                      // 得到近5天的60日均线的计算结果。		
		
		MoveAverageBean maBeanOfCurMa120 = curMa120DataResult.getMaList().get(curMa120DataResult.getMaList().size() - 1);         // 得到当前120日均线的价格。
		MoveAverageBean maBeanOfCurMa60 = curMa60DataResult.getMaList().get(curMa60DataResult.getMaList().size() - 1);            // 得到当前60日均线的价格。
		
		MaSpeedChangeResult curMa60SpeedChangeResult = maSpeedPattern(curMa60DataResult.getMaList(), 10, 2);                      // 得到近60天的10日均线的计算结果。
		List<MoveAverageBean> curMa60SpeedMa5MA = curMa60SpeedChangeResult.getSpeedMA(5);                                         // 得到60日均线运动速度的5日均线。
		MoveAverageBean lastOfCurMa60SpeedMa5MA = curMa60SpeedMa5MA.get(curMa60SpeedMa5MA.size() - 1);                            // 得到最后一个60日均线运动速度的5日均线价格。
		
		if (
				// 1.2.1
				(maBeanOfCurMa60.getAvg().compareTo(maBeanOfCurMa120.getAvg()) != 1) ||
				
				// 1.2.2
				(curMa60DataResult.getSpeed() <= 0) ||
				
				// 1.2.3
				(lastOfCurMa60SpeedMa5MA.getAvg().doubleValue() <= 0)
			) {
			return null;
		}
		
		// ---------------------------- 1.3、判断短期趋势 ----------------------------
		/*
		 * 只有中、长期趋势稳定向上，短期趋势由跌转升时才有可能出现买点：
		 * 
		 * 1.2.1、近60日均线在120日均线之上。（中期趋势还未发生本质改变）
		 * 1.2.2、近60日均线成上升状态。（中期趋势还未走弱）
		 * 1.2.3、近3天5日线呈震荡或上升状态，且上升速度 >= 0。（超短期趋势向上）
		 */	
		MaDataResult curMa5DataResult = maPattern(stockDataList, 5, 3, 0);                                                        // 得到近3天的5日均线的计算结果。
		
		MaSpeedChangeResult curMa5SpeedChangeResult = maSpeedPattern(curMa5DataResult.getMaList(), 5, 2);                         // 得到近5天的5日均线的计算结果。
		List<MoveAverageBean> curMa5SpeedMa3MA = curMa5SpeedChangeResult.getSpeedMA(3);                                           // 得到5日均线运动速度的3日均线。
		MoveAverageBean lastOfCurMa5SpeedMa3MA = curMa5SpeedMa3MA.get(curMa5SpeedMa3MA.size() - 1);                               // 得到最后一个5日均线运动速度的3日均线价格。

		if (
				// 1.3.1
				/*
				 * 如果近3天的5日均线的速度为负，就说明近期5均线趋势仍然向下，为保险起见，即使此刻出现了一个底分型，也应以过滤。
				 * 
				 *  T
				 *  |
				 * | |
				 *    |
				 *     |
				 *      |
				 *       | 
				 *        | NT
				 *         | |
				 *          | |
				 *         NB  |
				 *              |    
				 * 图1、在下降途中出现的虚假底分型。       
				 */
				(lastOfCurMa5SpeedMa3MA.getAvg().doubleValue() <= 0)
			) {
			return null;
		}
		
		/*
		 * ##################################################
		 * # 2、位                                                                                                                                  #
		 * ##################################################
		 * 
		 * 虽然理论上说，牛市中任何的买点都是正确的，但是买点与买点之间的风险系数是不同的。如果你在一个上涨的波段中刚一买入，该走势就进行了回调，
		 * 虽然可能过不了多久，价格就会涨上来，但你必须承受短期的账户浮亏，从而降低了资金利用率，这是风险之一；由于行情是不可预测的，万一回调后，
		 * 市场就由牛转熊，那么你就必须承受长期的账户浮亏了，这是风险之二。
		 * 
		 * 由此可见，判断“位”的根本意义在于：降低在牛使中买入的风险。
		 */
		
		/*
		 * 2.1、最后一根K线的收盘价距对应的120日均线的涨幅不能超过50%。
		 * 
		 */
		StockDataBean lastStockData = fractalIndicatorsInfo.getLastStockData(stockDataList);                                      // 得到最后一根K线。
		
		BigDecimal closeOfLastStockData = lastStockData.getClose();                                                               // 得到最后一根K线的收盘价。
		BigDecimal avgOfMa120 = curMa120DataResult.getMaList().get(curMa120DataResult.getMaList().size() - 1).getAvg();           // 得到当天120日均线的均价。
		
		BigDecimal diffOfCloseAndMa120 = closeOfLastStockData.subtract(avgOfMa120).setScale(3, RoundingMode.HALF_UP);             // 得到最后一根K线的收盘价和与其对应的120日均线的价格之间的差值。
		BigDecimal rateOfCloseAndMa120 = diffOfCloseAndMa120.divide(avgOfMa120, 3, RoundingMode.HALF_UP);                         // 得到最后一根K线的收盘价和与其对应的120日均线的价格之间的涨跌幅。
		
		if (rateOfCloseAndMa120.doubleValue() >= 0.5) {
			return null;
		}
		
		/*
		 * 2.1、当前波段必须是向下波段。
		 */
		BandBean lastBand = fractalIndicatorsInfo.getLastBand();                                                                  // 得到最后一个波段。
		if (lastBand.getBandType() != BandType.DOWN) {
			return null;
		}
		
		// 仓位控制：------------------------------------------------------------------------------------------------
		/*
		 * +-----------------------------------------------------------+
		 * + 仓位管控1：如果在某上升一波段内已建仓，则直接退出捕捉买入建仓程序。                                       +
		 * +-----------------------------------------------------------+
		 * 
		 * 风控目的：避免在某一上升波段内重复建仓。
		 */
		PositionInfoBean lastBuyPosition = getLastNoClosePosition(positionInfoList, FractalDealSignalEnum.FIBO_B);                 // 查询出最后一笔未平仓的斐波那契仓位。
		if (lastBuyPosition != null) {
			BandBean lastOpenBand = getBandBeanByDate(fractalIndicatorsInfo.getBandBeanList(), lastBuyPosition.getOpenDate());     // 查询出最后一个建仓的所在波段。
			
			/*
			 * 在最后一个波段和平仓波段是同一个波段的情况下，只有当平仓波段为上波段时，才退出建仓信号捕捉程序，有以下两个原因：
			 * 1、在下降波段中不能因为“相对过早的建仓点”而影响“潜在的建仓点”；
			 * 2、在上升波段中，不能因为重复建仓，而增大风险。
			 * 
			 *                            /\
			 *                           /  \
			 *                          /<-应被屏蔽的建仓点
			 *  相对过早的建仓点 -> \        /
			 *                 \      /
			 *                  \    /
			 *    被影响的潜在建仓点-> \  /<-建仓点
			 *                    \/ <-向上波段起点
			 *
			 * 图3、当平仓波段没有被限制时，所引发的的问题
			 */
			if (lastOpenBand != null && lastOpenBand.isOneAndTheSame(lastBand) && lastOpenBand.getBandType() == BandType.UP) {					
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
		
		FractalBean bottomOfLastBand = lastBand.getBottom();                                                                      // 得到最后一个波段中的底分型。
		if (
				lastBuyPosition != null &&
				lastBuyPosition.getOpenDate() >= bottomOfLastBand.getLeft().getDate() && 
				lastBuyPosition.getOpenDate() <= lastStockData.getDate()) {
			return null;
		}
		// 仓位控制：------------------------------------------------------------------------------------------------
		
		/*
		 * ##################################################
		 * # 3、态                                                                                                                                  #
		 * ##################################################
		 */
		
		/*
		 * 3.1、底分型形成后，从中间K线到当前K线中任意一根K线的上升力度必须大于0.03，才能证明有足够的上升力度。
		 */

		if (isHaveEnoughUpStrength(stockDataList, fractalIndicatorsInfo, 0.03)) {
			System.out.println("---> " + lastStockData.getDate() + " 日，出现斐波那契建仓信号 " + " <---" );
			return new FractalDealSignalBean(lastStockData, DealSignalEnum.FIBO_B);
		}
		
		return null;
	}

	@Override
	public DealSignalBean snapSellToCloseSignal(String stockCode,
			List<StockDataBean> stockDataList,
			IndicatorsInfoBean indicatorsInfo,
			List<PositionInfoBean> positionInfoList) {
		log.info("调用 [捕捉长期趋势平仓信号] 方法。"); 
		
		return null;
	}

	/**
	 * 捕捉斐波那契平仓信号。
	 * 
	 * @param resultBean 顶底分型交易系统所需数据的计算结果
	 * @param positionInfoList 目前仓位信息
	 * @return FractalDealSignalBean 返回：FractalDealSignalBean 表示出现斐波那契买入平仓信号；null：表示没有出现斐波那契买入平仓信号
	 */
	private FractalDealSignalBean snapFiboBuyCloseSignal (FractalDataCalcResultBean resultBean, List<PositionInfoBean> positionInfoList) {
		log.info("调用 [捕捉斐波那契平仓信号] 方法。");

		/*
		 * 
		 * 
		 * 风控目的：风控的实质是平稳主操盘手的心态，使进出场成为理性的产物。
		 * 
		 * 核心思想：市场是不可预测的，对已建仓的股票，应通过阶段性的波段操作和合理的仓位控制来回避不可预知的风险。
		 * 
		 * 思想基础：要尽力避开在上升过程中的每一次相对较大的回调，及时锁定利润、提高资金利用率、节约时间成本、降低风险。
		 * 因为每一次较大的回调都可以看成是主力的一次出货。虽然长期趋势不会因某一次大的回调而改变，但每发生一次，就增加了
		 * 一份风险，总有那么一次的回调后，会成为压垮骆驼的最后一根稻草。而且作为有程序交易系统的小散也没有必要参与回调的
		 * 过程，即便回调的时间较短，会损失一部分利润，但可以通过程序在众多投资标的中找到新的投资目标，从而弥补这部分损失。
		 * 
		 * 可行分析：股票价格涨跌的原因有很多，但根本的因素还是钱，而具备大资金的必定是机构，所以机构会根据其自身的情况来
		 * 调控股票的价格，但什么时候会调控，这个是不可预知的。我只能把自己想成一名赌徒，由于优秀的赌徒和拙劣的赌徒之间最
		 * 大的区别，就是对'行情的判断'、'赌资的管理'和'欲望的控制'等，所以要针对这三个方面做一些思考。
		 * 
		 * 在阶段性出场这个环节，我认为分析的重点有三个：
		 * 
		 * 1、'某只股票的整体仓位盈利幅度'：由于股价是不可预测的，所以风险只能降低，而却不能避免。因此当盈利幅度到达某一
		 * 界线时，及时的落袋为安就成为进一步的降低风险手段。
		 * 
		 * 2、'股价在一定涨幅后的巨大下跌'：涨幅后的突然暴跌，说明该股发生了某种大事件导致了股价的巨幅下挫，可能是庄家洗
		 * 盘、可能是热钱撤走、也可能是某种利空，但不管怎样这也是一种风险，应该进行回避。
		 * 
		 * 3、'120日均线的价格和与其对应的向上波段的高点之间的涨幅'：当该涨幅较大时，说明近一段时间内，股价向上发展的势
		 * 头很旺，股价与长期趋势间的距离逐步加大，由于发展迅速，这也加深了股价回调的空间，所以这种力度的上涨是存在风险的。
		 * 
		 * 
		 * 那为什么不考虑档较小的涨幅和较小的盈利幅度呢？
		 * 1、涨幅小，获利少，风险相对就底，及时最后被迫出场，在心里上没有什么可惜的；
		 * 2、在涨幅较小、回调较小，之后又大幅上扬这种情况中，频繁的进出场，会增加手续费，同时可能就会错失行情；
		 */
		
		/*
		 * 限制1：如果没有菲薄纳妾信号，就不用捕捉该平仓信号了，以降低计算复杂度。
		 */
		PositionInfoBean lastNoClosePosition = getLastNoClosePosition(positionInfoList, FractalDealSignalEnum.FIBO_B);              // 查询出最后的斐波仓位。
		if (lastNoClosePosition == null) {
			return null;
		}
		
		
		/*
		 * ##################################################
		 * # 1、左侧主动平仓 —— 以盈利幅度为主，K线与长期趋势乖离幅度为辅。             #
		 * ##################################################
		 * 
		 * 在盈利幅度达到50%的情况下，通过改变持仓来降低风险系数的参照：
		 * 
		 * 10（建仓股价） * 10000（股数） = 100000（建仓本金）
		 * 15（当前股价） * 10000（股数） = 150000（当前资产）
		 * 
		 * 100000 - (15 * 5000) = 25000         （平掉5/10）
		 * 25000 / 100000 = 0.25（风险系数）
		 * 
		 * 100000 - (15 * 4000) = 40000         （平掉4/10）
		 * 40000 / 100000 = 0.4（风险系数）
		 * 
		 * 100000 - (15 * 3000) = 55000         （平掉3/10）
		 * 55000 / 100000 = 0.55（风险系数）
		 * 
		 * 100000 - (15 * 2000) = 70000         （平掉2/10）
		 * 70000 / 100000 = 0.7（风险系数）
		 * 
		 * 100000 - (15 * 1000) = 85000         （平掉1/10）
		 * 85000 / 100000 = 0.85（风险系数）
		 * 
		 */
		StockDataBean lastStockData = resultBean.getLastStockData();                                                                       // 得到最后一根K线。
		
		// --- 计算整体仓位的盈利情况。
		List<PositionInfoBean> noClosePositionList = getAllNoClosePositionList(positionInfoList, FractalDealSignalEnum.FIBO_B);     // 查询出所有未平仓的斐波仓位。
		BigDecimal openCost = new BigDecimal(0);                                                                                           // 记录整体仓位的建仓成本。
		Long canCloseNumber = 0L;                                                                                                          // 记录整体仓位的可平仓数量。
		for (PositionInfoBean position : noClosePositionList) {
			openCost = openCost.add(position.getOpenCost());
			canCloseNumber += position.getCanCloseNumber();
		}
		BigDecimal newMarketValue = lastStockData.getClose().multiply(new BigDecimal(canCloseNumber));                                     // 计算整体仓位的最新市值。
		BigDecimal floatProfitAndLoss = newMarketValue.subtract(openCost);                                                                 // 浮动盈亏。公式 = 最新市值 - 建仓成本 
		BigDecimal profitAndLossRatio = floatProfitAndLoss.divide(openCost, 3, RoundingMode.HALF_UP);                                      // 盈亏比例。公式 = 浮动盈亏 / 建仓成本
		
		// --- 计算当前K线与120日均线之间涨跌幅。
		BigDecimal highOfLastStockData = lastStockData.getHigh();                                                                          // 得到最后一根K线的高价。
		MaDataResult curMa120DataResult = maPattern(resultBean, 120, 10, 0);                                                               // 得到近10天的120日均线的计算结果。
		MoveAverageBean avgOfLastCurMa120 = curMa120DataResult.getMaList().get(curMa120DataResult.getMaList().size() - 1);                 // 得到最后一根K线的高点和与其相对应的120日均线的价格。
		
		BigDecimal diffOfLastSdAndMa120 = highOfLastStockData.subtract(avgOfLastCurMa120.getAvg()).setScale(3, RoundingMode.HALF_UP);      // 得到最后一根K线的高点和与其相对应的120日均线的价格之间的差值。
		BigDecimal rateOfLastSdAndMa120 = diffOfLastSdAndMa120.divide(avgOfLastCurMa120.getAvg(), 3, RoundingMode.HALF_UP);                // 得到最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅。
		
		// --- 具体的出场规则。
		
		/* ---------------------------------------------------------------------------------------------------------------------------*/
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件1：                                                                                                                                           +
		 * + 1、整体仓位盈利幅度在[0.5,+∞)；                                                                                                       +
		 * + 2、最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅在[0.5,+∞)；               +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉5/10的仓位，把风险降低到0.25。
		 */
		if (profitAndLossRatio.doubleValue() >= 0.5 && rateOfLastSdAndMa120.doubleValue() >= 0.5) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|1|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_FIVE_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件2：                                                                                                                                           +
		 * + 1、整体仓位盈利幅度在[0.5,+∞)；                                                                                                       +
		 * + 2、最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅在[0.4,0.5)；            +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉4/10的仓位，把风险降低到0.4。
		 */
		if (
				profitAndLossRatio.doubleValue() >= 0.5 && 
				(rateOfLastSdAndMa120.doubleValue() >= 0.4 && rateOfLastSdAndMa120.doubleValue() < 0.5)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|2|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_FOUR_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件3：                                                                                                                                           +
		 * + 1、整体仓位盈利幅度在[0.5,+∞)；                                                                                                       +
		 * + 2、最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅在[0.3,0.4)；            +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉3/10的仓位，把风险降低到0.55。
		 */
		if (
				profitAndLossRatio.doubleValue() >= 0.5 && 
				(rateOfLastSdAndMa120.doubleValue() >= 0.3 && rateOfLastSdAndMa120.doubleValue() < 0.4)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|3|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_THREE_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件4：                                                                                                                                           +
		 * + 1、整体仓位盈利幅度在[0.5,+∞)；                                                                                                       +
		 * + 2、最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅在[0.2,0.3)；            +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉2/10的仓位，把风险降低到0.7。
		 */
		if (
				profitAndLossRatio.doubleValue() >= 0.5 && 
				(rateOfLastSdAndMa120.doubleValue() >= 0.2 && rateOfLastSdAndMa120.doubleValue() < 0.3)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|4|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_TWO_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件5：                                                                                                                                           +
		 * + 1、整体仓位盈利幅度在[0.5,+∞)；                                                                                                       +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉1/10的仓位，把风险降低到0.85。
		 */
		if (profitAndLossRatio.doubleValue() >= 0.5) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|5|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_ONE_TENTH);
		}
		
		/* ---------------------------------------------------------------------------------------------------------------------------*/
		
		/*
		 *
		 * ##################################################
		 * # 2、左侧主动平仓 —— 以K线与长期趋势乖离幅度为主，盈利幅度为辅。             #
		 * ##################################################
		 * 
		 * 在盈利幅度达到40%的情况下，通过兑现4/10的持仓来降低风险系数的参照：
		 * 
		 * 10（建仓股价） * 10000（股数） = 100000（建仓本金）
		 * 14（当前股价） * 10000（股数） = 140000（当前资产）
		 * 
		 * 100000 - (14 * 4000) = 44000         （平掉4/10）
		 * 44000 / 100000 = 0.44（风险系数）
		 * 
		 * 在盈利幅度达到30%的情况下，通过兑现3/10的持仓来降低风险系数的参照：
		 * 
		 * 10（建仓股价） * 10000（股数） = 100000（建仓本金）
		 * 13（当前股价） * 10000（股数） = 130000（当前资产）
		 * 
		 * 100000 - (13 * 3000) = 61000         （平掉3/10）
		 * 61000 / 100000 = 0.61（风险系数）
		 * 
		 * 在盈利幅度达到20%的情况下，通过兑现2/10的持仓来降低风险系数的参照：
		 * 
		 * 10（建仓股价） * 10000（股数） = 100000（建仓本金）
		 * 12（当前股价） * 10000（股数） = 120000（当前资产）
		 * 
		 * 100000 - (12 * 2000) = 76000         （平掉2/10）
		 * 76000 / 100000 = 0.76（风险系数）
		 * 
		 * 在盈利幅度达到10%的情况下，通过兑现1/10的持仓来降低风险系数的参照：
		 * 
		 * 10（建仓股价） * 10000（股数） = 100000（建仓本金）
		 * 11（当前股价） * 10000（股数） = 110000（当前资产）
		 * 
		 * 100000 - (11 * 1000) = 89000         （平掉1/10）
		 * 89000 / 100000 = 0.89（风险系数）
		 */
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件6：                                                                                                                                           +
		 * + 1、最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅在[0.5,+∞)；               +
		 * + 2、整体仓位盈利幅度在[0.4,0.5)；                                                                                                    +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉4/10的仓位，把风险降低到0.44。
		 */
		if (
				rateOfLastSdAndMa120.doubleValue() >= 0.5 &&
				(profitAndLossRatio.doubleValue() >= 0.4 && profitAndLossRatio.doubleValue() < 0.5)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|6|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_FOUR_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件7：                                                                                                                                           +
		 * + 1、最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅在[0.5,+∞)；               +
		 * + 2、整体仓位盈利幅度在[0.3,0.4)；                                                                                                    +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉3/10的仓位，把风险降低到0.61。
		 */
		if (
				rateOfLastSdAndMa120.doubleValue() >= 0.5 &&
				(profitAndLossRatio.doubleValue() >= 0.3 && profitAndLossRatio.doubleValue() < 0.4)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|7|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_THREE_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件8：                                                                                                                                           +
		 * + 1、最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅在[0.5,+∞)；               +
		 * + 2、整体仓位盈利幅度在[0.2,0.3)；                                                                                                    +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉2/10的仓位，把风险降低到0.76。
		 */
		if (
				rateOfLastSdAndMa120.doubleValue() >= 0.5 &&
				(profitAndLossRatio.doubleValue() >= 0.2 && profitAndLossRatio.doubleValue() < 0.3)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|8|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_TWO_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件9：                                                                                                                                           +
		 * + 1、最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅在[0.5,+∞)；               +
		 * + 2、整体仓位盈利幅度在[0.1,0.2)；                                                                                                    +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉1/10的仓位，把风险降低到0.89。
		 */
		if (
				rateOfLastSdAndMa120.doubleValue() >= 0.5 &&
				(profitAndLossRatio.doubleValue() >= 0.1 && profitAndLossRatio.doubleValue() < 0.2)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|9|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_ONE_TENTH);
		}
		
		/* ---------------------------------------------------------------------------------------------------------------------------*/
		
		/*
		 *
		 * ##################################################
		 * # 3、左侧主动平仓 —— 以单日跌幅为主，盈利幅度为辅。                                        #
		 * ##################################################
		 * 
		 */
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件10：                                                                                                                                           +
		 * + 1、当日跌幅在[-0.08,-∞)；                                                                                                                 +
		 * + 2、整体仓位盈利幅度在[0.4,0.5)；                                                                                                    +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉4/10的仓位，把风险降低到0.44。
		 */
		if (
				isOverstepkLineRate(lastStockData, -0.08) &&
				(profitAndLossRatio.doubleValue() >= 0.4 && profitAndLossRatio.doubleValue() < 0.5)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|10|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_FOUR_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件11：                                                                                                                                           +
		 * + 1、当日跌幅在[-0.08,-∞)；                                                                                                                 +
		 * + 2、整体仓位盈利幅度在[0.3,0.4)；                                                                                                    +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉3/10的仓位，把风险降低到0.61。
		 */
		if (
				isOverstepkLineRate(lastStockData, -0.08) &&
				(profitAndLossRatio.doubleValue() >= 0.3 && profitAndLossRatio.doubleValue() < 0.4)
		) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|11|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_THREE_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件12：                                                                                                                                        +
		 * + 1、当日跌幅在[-0.08,-∞)；                                                                                                                 +
		 * + 2、整体仓位盈利幅度在[0.2,0.3)；                                                                                                    +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉2/10的仓位，把风险降低到0.76。
		 */
		if (
				isOverstepkLineRate(lastStockData, -0.08) &&
				(profitAndLossRatio.doubleValue() >= 0.2 && profitAndLossRatio.doubleValue() < 0.3)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|12|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_TWO_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件13：                                                                                                                                        +
		 * + 1、当日跌幅在[-0.08,-∞)；                                                                                                                 +
		 * + 2、整体仓位盈利幅度在[0.1,0.2)；                                                                                                    +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉1/10的仓位，把风险降低到0.89。
		 */
		if (
				isOverstepkLineRate(lastStockData, -0.08) &&
				(profitAndLossRatio.doubleValue() >= 0.1 && profitAndLossRatio.doubleValue() < 0.2)
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|13|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_ONE_TENTH);
		}
		
		/* ---------------------------------------------------------------------------------------------------------------------------*/
		
		/*
		 *
		 * ##################################################
		 * # 4、左侧主动平仓 —— 以亏损幅度为主，长期均线运动速度为辅。                        #
		 * ##################################################
		 * 
		 * 在亏损幅度达到10%的情况下，通过兑现5/10的持仓来降低风险系数的参照：
		 * 
		 * 10（建仓股价） * 10000（股数） = 100000（建仓本金）
		 * 9（当前股价） * 10000（股数） = 90000（当前资产）
		 * 
		 * 100000 - (9 * 5000) = 55000         （平掉5/10）
		 * 55000 / 100000 = 0.55（风险系数）
		 * 
		 * 在亏损幅度达到10%的情况下，通过兑现2/10的持仓来降低风险系数的参照：
		 * 
		 * 10（建仓股价） * 10000（股数） = 100000（建仓本金）
		 * 9（当前股价） * 10000（股数） = 90000（当前资产）
		 * 
		 * 100000 - (9 * 2000) = 82000         （平掉2/10）
		 * 82000 / 100000 = 0.82（风险系数）
		 * 
		 */

		MaSpeedChangeResult curMa120SpeedChangeResult = maSpeedPattern(curMa120DataResult.getMaList(), 10, 2);                             // 得到120日均线运动速度改变的结果类。

		List<MoveAverageBean> curMa120SpeedMa5MA = curMa120SpeedChangeResult.getSpeedMA(5);                                                // 得到120日均线运动速度的5日均线。
		List<MoveAverageBean> curMa120SpeedMa10MA = curMa120SpeedChangeResult.getSpeedMA(10);                                              // 得到120日均线运动速度的10日均线。
		
		MoveAverageBean lastOfCurMa120SpeedMa5MA = curMa120SpeedMa5MA.get(curMa120SpeedMa5MA.size() - 1);                                  // 得到最后一个120日均线运动速度的5日均线。
		MoveAverageBean lastOfCurMa120SpeedMa10MA = curMa120SpeedMa10MA.get(curMa120SpeedMa10MA.size() - 1);                               // 得到最后一个120日均线运动速度的10日均线。
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件14：                                                                                                                                           +
		 * + 1、整体仓位亏损幅度在[0.1,-∞)；                                                                                                       +
		 * + 2、最后一个120日均线运动速度的5日均线在[0,+∞)；                                                                     +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉2/10的仓位，把风险降低到0.82。
		 */
		if (
				profitAndLossRatio.doubleValue() <= -0.1 && 
				lastOfCurMa120SpeedMa5MA.getAvg().doubleValue() >= 0
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|14|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_TWO_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件15：                                                                                                                                           +
		 * + 1、整体仓位亏损幅度在[0.1,-∞)；                                                                                                       +
		 * + 2、最后一个120日均线运动速度的5日均线在(0,-∞)；                                                                     +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉5/10的仓位，把风险降低到0.55。
		 */
		if (
				profitAndLossRatio.doubleValue() <= -0.1 && 
				lastOfCurMa120SpeedMa5MA.getAvg().doubleValue() < 0
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|15|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_FIVE_TENTH);
		}
		
		/*
		 * +-----------------------------------------------------------+
		 * + 阶段性出场条件16：                                                                                                                                           +
		 * + 1、整体仓位亏损幅度在[0.1,-∞)；                                                                                                       +
		 * + 2、最后一个120日均线运动速度的10日均线在(0,-∞)；                                                                     +
		 * +-----------------------------------------------------------+
		 * 
		 * 平掉10/10的仓位，把风险降低到0。
		 */
		if (
				profitAndLossRatio.doubleValue() <= -0.1 && 
				lastOfCurMa120SpeedMa10MA.getAvg().doubleValue() < 0
			) {
			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|16|");
			return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_ALL);
		}
		
		// ---
//		MaDataResult curMa10DataResult = maPattern(resultBean, 10, 10, 0);                                                                 // 得到近10天的10日均线的计算结果。
//		MaSpeedChangeResult curMa10SpeedChangeResult = maSpeedPattern(curMa10DataResult.getMaList(), 10, 2);                               // 得到10日均线运动速度改变的结果类。
//
//		List<MoveAverageBean> curMa10SpeedMa5MA = curMa10SpeedChangeResult.getSpeedMA(5);                                                  // 得到10日均线运动速度的5日均线。
//		
//		MoveAverageBean lastOfCurMa10SpeedMa5MA = curMa10SpeedMa5MA.get(curMa10SpeedMa5MA.size() - 1);                                     // 得到最后一个10日均线运动速度的5日均线价格。
//		MoveAverageBean prevOfCurMa10SpeedMa5MA = curMa10SpeedMa5MA.get(curMa10SpeedMa5MA.size() - 2);                                     // 得到倒数第二个10日均线运动速度的5日均线价格。
		
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 阶段性出场条件2：平掉十分之二的仓位                                                                                                       +
//		 * + 1、最后一根K线之间有某根K线的跌幅超过了80%；                                                                                +
//		 * + 2、最后一根K线的收盘价和与其对应的120日均线的价格之间的涨跌幅在[0.3,+∞)；               +
//		 * +-----------------------------------------------------------+
//		 * 
//		 * 出场理由：股市是不可预测的，上涨途中的大跌可视为风险信号。如果大跌过后，股价距120日均线仍有较大距离，
//		 * 则说明仍有较大向下调整空间，为避免风险加剧，应及时兑现部分仓位。
//		 */
		
//		MoveAverageBean maBeanOfCurMa120 = curMa120DataResult.getMaList().get(curMa120DataResult.getMaList().size() - 1);                  // 得到当前120日均线的价格。
//		BigDecimal diffOfLastAndMa120 = lastStockData.getClose().subtract(maBeanOfCurMa120.getAvg()).setScale(3, RoundingMode.HALF_UP);    // 得到最后一根K线的收盘价和与其对应的120日均线的价格之间的差值。
//		BigDecimal rateOfLastAndMa120 = diffOfLastAndMa120.divide(maBeanOfCurMa120.getAvg(), 3, RoundingMode.HALF_UP);                     // 得到最后一根K线的收盘价和与其对应的120日均线的价格之间的涨跌幅。
//		
//		if (isOverstepkLineRate(lastStockData, -0.08)) {
//			if (rateOfLastAndMa120.doubleValue() >= 0.3) {
//				System.out.println("---> " + resultBean.getLastStockData().getDate() + " 日，出现（阶段）斐波那契平仓信号（出场条件2） " + " <---" );
//				return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_TWO_TENTH);
//			}
//		}
//		
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 阶段性出场条件3：平掉十分之三的仓位                                                                                                       +
//		 * + 1、60日均线运动速度的10日均线向下运行；                                                                                           +
//		 * + 2、整体仓位亏损幅度在[-0.10,-∞)。                                                                                                 +
//		 * +-----------------------------------------------------------+
//		 * 
//		 * 出场条件理由：如果向上发展的长期趋势已持续了一段时间，我相信通过之前的阶段性出场，大部分浮盈应该已落袋为安，甚至把
//		 * 风险降到了0，但走势的拐点是不可预测的，任何人都不可能准确的猜出顶部和底部，一旦60日均线向下运行，且账户也出现了一
//		 * 定程度的亏损，就应该及时出局。
//		 * 
//		 */
//		MaDataResult curMa60DataResult = maPattern(resultBean, 60, 5, 0);                                                                  // 得到近5天的60日均线的计算结果。
//		
//		MaSpeedChangeResult curMa60SpeedChangeResult = maSpeedPattern(curMa60DataResult.getMaList(), 20, 2);                               // 得到60日均线运动速度改变的结果类。
//		List<MoveAverageBean> curMa60SpeedMa10MA = curMa60SpeedChangeResult.getSpeedMA(10);                                                // 得到60日均线运动速度的10日均线。
//		MoveAverageBean lastOfCurMa60SpeedMa10MA = curMa60SpeedMa10MA.get(curMa60SpeedMa10MA.size() - 1);                                  // 得到60日均线运动速度的10日均线价格。
//		
//		if (lastOfCurMa60SpeedMa10MA.getAvg().doubleValue() < 0) {
//			if (profitAndLossRatio.doubleValue() < -0.5) {
//				System.out.println("---> " + resultBean.getLastStockData().getDate() + " 日，出现（阶段）斐波那契平仓信号（出场条件3） " + " <---" );
//				return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_THREE_TENTH);
//			}
//		}
//		
//		
//		
//		
//		
////		MaDataResult curMa10DataResult = maPattern(resultBean, 10, 5, 0);                                                                  // 得到近5天的10日均线的计算结果。	
////		MaDataResult curMa5DataResult = maPattern(resultBean, 5, 3, 0);                                                                    // 得到近3天的5日均线的计算结果。	
////		
////		MoveAverageBean maBeanOfCurMa10 = curMa10DataResult.getMaList().get(curMa10DataResult.getMaList().size() - 1);                     // 得到当前10日均线的价格。
////		MoveAverageBean maBeanOfCurMa5 = curMa5DataResult.getMaList().get(curMa5DataResult.getMaList().size() - 1);                        // 得到当前5日均线的价格。
////		
////		
////		if (profitAndLossRatio.doubleValue() >= 0.25) {
////			if (maBeanOfCurMa10.getAvg().subtract(maBeanOfCurMa5.getAvg()).doubleValue() >= 0.001) {
////				System.out.println("---> " + resultBean.getLastStockData().getDate() + " 日，出现（阶段）斐波那契平仓信号（出场条件4） " + " <---" );
////				return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_ONE_TENTH);
////			}
////		}
//		
//		/*
//		 * 限制2：最后一个波段必须向上。
//		 */
//		BandBean lastBand = resultBean.getLastBand();                                                                                      // 最后一个波段。
//		if (lastBand.getBandType() != BandType.UP) {
//			return null;
//		}
//		
//		/*
//		 * 当前K线不能是当下形成顶分型的一部分，而且该K线的收盘价必须低于顶分型中右侧K线的收盘价。  
//		 * 
//		 * 目的：尽可能减少在捕捉平仓信号过程中，由于无效的顶分型，所造成的虚假平仓信号，以避免错过大幅的上涨。
//		 * 代价：会使有效的平仓信号稍迟一些发出。
//		 * 
//		 */
//		FractalBean top = lastBand.getTop();                                                                                               // 得到最后一个波段中的顶分型。
//		if (!isRelativelyEffectiveTop(resultBean, top)) {
//			return null;
//		}
//
//		// 仓位控制：------------------------------------------------------------------------------------------------
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 仓位管控1：如果在某一波段内已平仓，则直接退出捕捉买入平仓程序。                                               +
//		 * +-----------------------------------------------------------+
//		 * 
//		 * 风控目的：避免在某一波段内重复平仓。
//		 */
//		PositionInfoBean lastClosePosition = getLastClosePosition(positionInfoList, FractalDealSignalEnum.FIBO_B);                  // 查询出最后一笔已平仓的一买仓位。
//		BandBean lastOpenBand = (lastClosePosition != null) ? getBandBeanByDate(resultBean, lastClosePosition.getCloseDate()) : null;      // 查询出最后一个建仓的所在波段。
//		if (lastOpenBand != null) {
//			
//			/*
//			 * 在最后一个波段和平仓波段是同一个波段的情况下，只有当平仓波段为向下波段时，才退出平仓信号捕捉程序，否则会影响到潜在出场点。
//			 * 如果在该潜在出场点后，长期趋势急转直下，会加大账户的损失。
//			 * 
//			 *                 /\
//			 *                /  \
//			 *               /    \ <-被影响的潜在出场点
//			 *     \        /
//			 *      \      /
//			 *       \    /
//			 *        \  /<-平仓点
//			 * 平仓信号  ->\/ <-向上波段起点
//			 *
//			 * 图3、当平仓波段为向上波段时，而影响潜在出场点
//			 */
//			if (lastOpenBand.isOneAndTheSame(lastBand) && lastOpenBand.getBandType() == BandType.DOWN) {
//				return null;
//			}
//		}
//		
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 仓位管控2：从最后一个顶分型到当前K线，如果已平仓，则直接退出捕捉平仓信号程序。                +
//		 * +-----------------------------------------------------------+
//		 * 
//		 * 风控目的：避免在该顶分型附近重复平仓。
//		 */
//		if (
//				lastClosePosition != null &&
//				lastClosePosition.getCloseDate() >= top.getLeft().getDate() && 
//				lastClosePosition.getCloseDate() <= lastStockData.getDate()) {
//			return null;
//		}
//		// 仓位控制：------------------------------------------------------------------------------------------------
//			
//		
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 阶段性出场条件5：平掉十分之二的仓位                                                                                                       +
//		 * + 1、向上波段的高点和与其对应的120日均线的价格之间的涨跌幅在[0.5,+∞)；                          +
//		 * + 2、形成了顶分型，且最后一根K线是从顶分型的中间K线开始后最低的第一根K线；                          +
//		 * + 3、最后一根K线的收盘价和与其对应的120日均线的价格之间的涨跌幅在[0.2,+∞)；               +
//		 * +-----------------------------------------------------------+
//		 * 
//		 * 出场条件理由：股市是不可预测的，以120日均线为基准，当向上波段最高点的涨幅达到50%及以上时，说明股价攀升速度较快。
//		 * 此时大部分参与者在账面上可能都已浮赢，但是这种迅猛的增长势头也很难保持。当形成顶分型时，说明风险可能接踵而至，为了
//		 * 回避市场风险，应及时兑现部分仓位。
//		 * 
//		 */
////		boolean lastIsLowThanTop = true;                                                                                                   // 最后一根K线是否是顶分型后最低的K线。
////		StockDataBean tempCycleStockData = resultBean.getNoContainKLineInFractalBean(top, "center");
////		while (tempCycleStockData != null && tempCycleStockData.getDate() < lastStockData.getDate()) {
////			if (tempCycleStockData.getLow().compareTo(lastStockData.getLow()) == -1) {
////				lastIsLowThanTop = false;
////				break;
////			}
////			
////			tempCycleStockData = tempCycleStockData.getNext();
////		}
//		
//		// 找出需要进行比较的波段 （如果前一个向上波段中没有卖出，且高于最后一个向上波段，就把前一个向上波段放入集合。）
//		BandBean prevUpPushBand = (lastBand.getPrev() != null && lastBand.getPrev().getPrev() != null)                                     // 前一根向上的波段。
//		? lastBand.getPrev().getPrev() : null;
//		List<BandBean> needCompareUpPushBandList = new ArrayList<BandBean>();                                                              // 装载需要比较的向上波段。
//		if (prevUpPushBand != null) {
//			BandBean prevDownPushBand = lastBand.getPrev();                                                                                // 得到前一根向下的波段。
//			if (lastOpenBand != null && !lastOpenBand.isOneAndTheSame(prevDownPushBand)) {
//				if (prevUpPushBand.getTop().getCenter().getHigh().compareTo(lastBand.getTop().getCenter().getHigh()) == 1) {
//					needCompareUpPushBandList.add(prevUpPushBand);                                                                         // 把前一向上波段放入集合。
//				}
//			}
//		}
//		needCompareUpPushBandList.add(lastBand);                                                                                           // 把最后一个向上波段放入集合。
//		
//		// 对找出每一根向上波段都进行比较。
//		for (BandBean band : needCompareUpPushBandList) {
//			
//			BigDecimal highOfUpPushBand = band.getTop().getCenter().getHigh();                                                             // 得到该向上波段的最高价。
//			BigDecimal avgOfOneToOneMa120 = null;                                                                                          // 得到与向上波段最高价相对应的120日均线的均价。
//			for (MoveAverageBean ma : curMa120DataResult.getMaList()) {
//				if (ma.getDate().equals(band.getTop().getCenter().getDate())) {
//					avgOfOneToOneMa120 = ma.getAvg();
//					break;
//				}
//			}
//			
//			BigDecimal diffOfHighAndMa120 = highOfUpPushBand.subtract(avgOfOneToOneMa120).setScale(3, RoundingMode.HALF_UP);               // 得到向上波段的高点和与其相对应的120日均线的价格之间的差值。
//			BigDecimal rateOfHighAndMa120 = diffOfHighAndMa120.divide(avgOfOneToOneMa120, 3, RoundingMode.HALF_UP);                        // 得到向上波段的高点和与其对应的120日均线的价格之间的涨跌幅。
//			
//			if (rateOfHighAndMa120.doubleValue() >= 0.5) {
//				if (rateOfLastAndMa120.doubleValue() >= 0.2) {
//					System.out.println("---> " + resultBean.getLastStockData().getDate() + " 日，出现（阶段）斐波那契平仓信号（出场条件5） " + " <---" );
//					return new FractalDealSignalBean(resultBean.getLastStockData(), FractalDealSignalEnum.SELL_TWO_TENTH);
//				}
//			}
//		}
		
		return null;
	}
	
	// --------------------- private method 查询仓位及其所属波段信息 ----------------
	
	/**
	 * 查询出某一买点，尚未平仓的最后一笔仓位信息（按照open_date + open_time 倒序）。
	 * 
	 * @param positionInfoList 分型战法仓位集合
	 * @param dealSignalType 分型战法交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	private PositionInfoBean getLastNoClosePosition (List<PositionInfoBean> positionInfoList, FractalDealSignalEnum dealSignalType) {
		List<PositionInfoBean> allNoClosePositionList = getAllNoClosePositionList(positionInfoList, dealSignalType);     // 查询出某一买点，尚未平仓的全部仓位信息（按照open_date + open_time 倒序）。
		return (!allNoClosePositionList.isEmpty()) ? allNoClosePositionList.get(0) : null;
	}
	
	/**
	 * 查询出某一买点，已平仓的最后一笔仓位信息（按照close_date + close_time 倒序）。
	 * 
	 * @param positionInfoList 分型战法仓位集合
	 * @param dealSignalType 分型战法交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	private PositionInfoBean getLastClosePosition (List<PositionInfoBean> positionInfoList, FractalDealSignalEnum dealSignalType) {
		List<PositionInfoBean> allClosePositionList = getAllClosePositionList(positionInfoList, dealSignalType);         // 查询出某一买点，已平仓的全部仓位信息（按照close_date + close_time 倒序）。
		return (!allClosePositionList.isEmpty()) ? allClosePositionList.get(0) : null;
	}
	
	/**
	 * 查询出某一买点，尚未平仓的全部仓位信息（按照open_date + open_time 倒序）。
	 * 
	 * @param positionInfoList 分型战法仓位集合
	 * @param dealSignalType 分型战法交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	private List<PositionInfoBean> getAllNoClosePositionList (List<PositionInfoBean> positionInfoList, FractalDealSignalEnum dealSignalType) {
		
		// --- 查询到所有的仓位信息。
		List<PositionInfoBean> allPositionList = getAllPositionList(positionInfoList, dealSignalType);                   // 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
		if (allPositionList == null || allPositionList.isEmpty()) {
			return new ArrayList<PositionInfoBean>();
		}
	
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
					   (o1.getOpenTime() > o2.getOpenTime()) ? -1  :
					   (o1.getOpenTime() < o2.getOpenTime()) ? 1   :
			           0;
			}
		});
		
		return allNoClosePositionList;
	}
	
	/**
	 * 查询出某一买点，已平仓的全部仓位信息（按照close_date + close_time 倒序）。
	 * 
	 * @param positionInfoList 分型战法仓位集合
	 * @param dealSignalType 分型战法交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	private List<PositionInfoBean> getAllClosePositionList (List<PositionInfoBean> positionInfoList, FractalDealSignalEnum dealSignalType) {
		
		// --- 查询到所有的仓位信息。
		List<PositionInfoBean> allPositionList = getAllPositionList(positionInfoList, dealSignalType);                   // 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
		if (allPositionList == null || allPositionList.isEmpty()) {
			return new ArrayList<PositionInfoBean>();
		}
		
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
					   (o1.getCloseTime() > o2.getCloseTime()) ? -1  :
					   (o1.getCloseTime() < o2.getCloseTime()) ? 1   :
			           0;
			}
		});
		return allClosePositionList;
	}
	
	/**
	 * 查询出某一买点的全部仓位信息（按照open_date + open_time 升序）。
	 * 
	 * @param positionInfoList 分型战法仓位集合
	 * @param dealSignalType 分型战法交易信号类型枚举
	 * @return List<PositionInfoBean>
	 */
	private List<PositionInfoBean> getAllPositionList (List<PositionInfoBean> positionInfoList, FractalDealSignalEnum dealSignalType) {
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
	
	// --------------------- private method 得到建仓点和平仓点所在的波段 ----------------
	
	/**
	 * 根据日期来查询其所在波段。
	 * 
	 * @param bandList 行情波段集合
	 * @param date 日期
	 * @return BandBean 建仓所在的波段
	 */
	private BandBean getBandBeanByDate (List<BandBean> bandList, Integer date) {
		
		for (BandBean band : bandList) {
			// --- 计算波段的时间范围 ---
			Integer startDate, endDate;
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
	
	
	// --------------------- private method 是否达到上涨或下跌比率 ---------------------
	/**
	 * 判断底分型是否有足够的上升力度。
	 * 
	 * @param stockDataList 股票行情信息集合
	 * @param fractalIndicatorsInfo 顶底分型交易系统所需数据的计算结果
	 * @param rate 上涨比率
	 * @return boolean true:有足够上升力度；false:没足够上升力度
	 */
	private boolean isHaveEnoughUpStrength (List<StockDataBean> stockDataList, FractalIndicatorsInfoBean fractalIndicatorsInfo, double rate) {
		
		if (fractalIndicatorsInfo == null || rate <= 0) {
			throw new RuntimeException("判断底分型是否有足够的上升力度时参数不符合要求！[fractalIndicatorsInfo = " + fractalIndicatorsInfo + "] | [rate = " + rate + "]");
		}
		
		/*
		 * ##################################################
		 * # 只要底分型内的中间K线或右侧K线只要有一个满足条件即可。                               #
		 * ##################################################
		 */
		List<StockDataBean> compareList = new ArrayList<StockDataBean>();                                                                //  记录参与比较的K线集合。
		
		FractalBean bottomOfLastBand = fractalIndicatorsInfo.getLastBand().getBottom();                                                  // 最后一个向下波段的底分型。
		StockDataBean centerStockData = fractalIndicatorsInfo.getNoContainKLineInFractalBean(stockDataList, bottomOfLastBand, "center"); // 得到底分型的中间K线。
		
		/*
		 * +-----------------------------------------------------------+
		 * + 由于图形经过了K线包含处理，所以最右侧K线的前一K线并不一定是底分型的中间K线，因此为了  +
		 * + 不影响判断的准确性，要找到这些中间被合并的K线一起参与判断。                                                       +
		 * +-----------------------------------------------------------+
		 */
		StockDataBean temp = centerStockData;
		while (temp != null && temp.getDate() <= fractalIndicatorsInfo.getLastStockData(stockDataList).getDate()) {
			compareList.add(temp);
			temp = temp.getNext();
		}
		
		for (StockDataBean compare : compareList) {
			if (isOverstepkLineRate(compare, rate)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 判断顶分型是否有足够的下跌力度。
	 * 
	 * @param resultBean 顶底分型交易系统所需数据的计算结果
	 * @param rate 下跌比率
	 * @return boolean true:有足够下跌力度；false:没足够下跌力度
	 */
	@SuppressWarnings("unused")
	private boolean isHaveEnoughDownStrength (FractalDataCalcResultBean resultBean, double rate) {
		
		if (resultBean == null || rate >= 0) {
			throw new RuntimeException("判断顶分型是否有足够的下跌力度时参数不符合要求！[resultBean = " + resultBean + "] | [rate = " + rate + "]");
		}
		
		/*
		 * ##################################################
		 * # 只要顶分型内的中间K线或右侧K线只要有一个满足条件即可。                               #
		 * ##################################################
		 */
		List<StockDataBean> compareList = new ArrayList<StockDataBean>();                                      //  记录参与比较的K线集合。
		
		FractalBean topOfLastBand = resultBean.getLastBand().getTop();                                         // 最后一个向上波段的顶分型。
		StockDataBean centerStockData = resultBean.getNoContainKLineInFractalBean(topOfLastBand, "center");    // 得到顶分型的中间K线。

		/*
		 * +-----------------------------------------------------------+
		 * + 由于图形经过了K线包含处理，所以最右侧K线的前一K线并不一定是顶分型的中间K线，因此为了  +
		 * + 不影响判断的准确性，要找到这些中间被合并的K线一起参与判断。                                                       +
		 * +-----------------------------------------------------------+
		 */
		StockDataBean temp = centerStockData;
		while (temp != null && temp.getDate() <= resultBean.getLastStockData().getDate()) {
			compareList.add(temp);
			temp = temp.getNext();
		}
		
		for (StockDataBean compare : compareList) {
			if (isOverstepkLineRate(compare, rate)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 判断当天K线收盘价的涨跌幅度是否超过其某一比率。
	 * 
	 * @param stockData 行情数据
	 * @param rate 参考比率
	 * @return boolean true:超过；false:没超过
	 */
	private boolean isOverstepkLineRate (StockDataBean stockData, double rate) {
		
		StockDataBean stockDataPrev = stockData.getPrev();                         // 得到昨天的行情数据。
		if (stockDataPrev == null) {
			throw new RuntimeException("没有昨日的K线数据，导致不能判断当天K线收盘价的涨跌幅度是否超过" + rate);
		}
		
		/*
		 * 涨跌幅=(现价-上一个交易日收盘价）/ 上一个交易日收盘价 
		 */
		BigDecimal difference = stockData.getClose().subtract(stockDataPrev.getClose());
		BigDecimal rateOfUp = difference.divide(stockDataPrev.getClose(), 3, RoundingMode.HALF_UP);
		
		/*
		 * 涨跌幅=(今日收盘价-今日开盘价）/ 今日开盘价
		 * 
		 * 为什么还要算这个呢？因为，如果今日比昨日低开，但是却走出了一根大阳线，这时如果仅按照上面的公式算，就会丢掉入场信号了。
		 */
		BigDecimal differenceOfDay = stockData.getClose().subtract(stockData.getOpen());
		BigDecimal rateOfUpOfDay = differenceOfDay.divide(stockData.getOpen(), 3, RoundingMode.HALF_UP);
		
		/*
		 * 当发生一字涨跌板时：
		 * 1、如果 rate > 0 时，只要今日收盘价高于昨日收盘价，就返回true，否则返回false；
		 * 2、如果 rate < 0 时，只要今日收盘价低于昨日收盘价，就返回true，否则返回false。
		 */
		if (
				(rateOfUpOfDay.doubleValue() == 0) &&
				(stockData.getClose().doubleValue() == stockData.getOpen().doubleValue()) && 
				(stockData.getClose().doubleValue() == stockData.getHigh().doubleValue()) &&
				(stockData.getClose().doubleValue() == stockData.getLow().doubleValue())
		) {
			if (rate > 0) {
				return (stockData.getClose().compareTo(stockDataPrev.getClose()) == 1);
			} else {
				return (stockData.getClose().compareTo(stockDataPrev.getClose()) == -1);
			}
		}
		
		if (rate > 0) {			
			return (rateOfUp.doubleValue() >= rate) || (rateOfUpOfDay.doubleValue() >= rate);
		} else {
			return (rateOfUp.doubleValue() <= rate) || (rateOfUpOfDay.doubleValue() <= rate);
		}
	}
	
	/**
	 * 在行情数据中的最后一根K线是否是最后一个底分型中的最后一根K线。
	 * 
	 * @param resultBean 顶底分型交易系统所需数据的计算结果
	 * @return true：是；false：否
	 */
	@SuppressWarnings("unused")
	private boolean isLastStockDataOfLastBottom (FractalDataCalcResultBean resultBean) {
		
		// 最后一个分型必须是底分型。
		FractalBean lastFractal = resultBean.getLastValidFractal();                                                           // 最后一个有效的分型信息。
		if (lastFractal.getFractalType() != FractalType.BUTTOM) {
			return false;
		}
		
		/*
		 * 如果最后一根K线是不是最后一个底分型的一部分，那后续K线还会继续参与判断，从而产生频繁产生不必要的信号。
		 * 
		 * 
		 *           |   |
		 *           | | | 
		 *             |   |
		 *             |   1 |
		 *                   2 |
		 *                     3
		 *                     
		 *   图1.在最后的底分型后，K线1、2、3依然会参与判断
		 */
		
		// 最后一个底分型是最后一根波段的底分型。
		FractalBean bottomOfLastBand = resultBean.getLastBand().getBottom();                                                  // 最后一根波段的底分型。
		if (
				(lastFractal.getLeft().getDate().intValue() != bottomOfLastBand.getLeft().getDate().intValue()) ||
				(lastFractal.getCenter().getDate().intValue() != bottomOfLastBand.getCenter().getDate().intValue()) ||
				(lastFractal.getRight().getDate().intValue() != bottomOfLastBand.getRight().getDate().intValue())) {
			return false;
		}

		// 最后一根K线是最后一个底分型中的右侧K线。
		StockDataBean lastStockData = resultBean.getLastStockData();                                                          // 最后一根K线。
		StockDataBean rightStockData = resultBean.getNoContainKLineInFractalBean(bottomOfLastBand, "right");                  // 得到最后一个分型中真实的右侧K线。

		if (rightStockData == null || (lastStockData.getDate().intValue() != rightStockData.getDate().intValue())) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * 计算均线的形态。
	 * 
	 * @param stockDataList 股票行情数据集合
	 * @param cycle 均线周期
	 * @param compareNums 均线内需要比较的均线个数
	 * @param backwardsNums 以倒数第N个K线为最后的K线点，backwardsNums只为为0或负数
	 * @return MaDataResult
	 */
	private MaDataResult maPattern (List<? extends StockDataBean> stockDataList, int cycle, int compareNums, int backwardsNums) {
		
		backwardsNums = (backwardsNums > 0) ? 0 : backwardsNums;
		
		if (backwardsNums < 0 && (stockDataList.size() > Math.abs(backwardsNums))) {                                                                 // 如果 backwardsNums < 0 ，则需要重新计算K线集合。
			List<StockDataBean> tempSdBeanList = new ArrayList<StockDataBean>();
			tempSdBeanList.addAll(stockDataList);
			stockDataList = tempSdBeanList.subList(0, (tempSdBeanList.size() - Math.abs(backwardsNums)));
		}
		
		int needKLineNums = (compareNums > cycle) ? ((2 * cycle) + compareNums) : (2 * cycle);                                                    // 计算在均线数据计算的过程中所需的K线数量。

		List<MoveAverageBean> sourceMaList = TechAlgorithm.partOfMA(stockDataList, needKLineNums, cycle);                                            // 得到均线集合。

		// 如果没有均线数据，就直接返回“未知形态”。
		if (sourceMaList == null || sourceMaList.isEmpty()) {
			return new MaDataResult().setMaList(sourceMaList).setPattern(SingleMaPatternEnum.UNKNOWN);
		}
		
		List<MoveAverageBean> targetMaList =                                                                                                     // 得到一定数量的，本次比较所需要用到的均线数据。
			(sourceMaList.size() < compareNums) ? sourceMaList : sourceMaList.subList(sourceMaList.size() - compareNums, sourceMaList.size());                             
		
		int upNums = 0;                                                                                                                           // 在给定的均线数量内，累计两相邻均线升高的数量。
		int downNums = 0;                                                                                                                         // 在给定的均线数量内，累计两相邻均线下降的数量。
		for (int i = (targetMaList.size() - 1); i > 0; i--) {
			MoveAverageBean current = targetMaList.get(i);
			MoveAverageBean prev = targetMaList.get(i - 1);
			
			if (current.getAvg().compareTo(prev.getAvg()) == 1) {
				upNums++;
			} else {
				downNums++;
			}
		}
		
		double upRate = new BigDecimal(upNums).divide(new BigDecimal(compareNums), 3, RoundingMode.HALF_UP).doubleValue();                        // 计算“升高数量”在参与比较均线数量的占比。
		double downRate = new BigDecimal(downNums).divide(new BigDecimal(compareNums), 3, RoundingMode.HALF_UP).doubleValue();                    // 计算“下降数量”在参与比较均线数量的占比。0

		MoveAverageBean last = targetMaList.get(targetMaList.size() - 1);                                                                         // 得到给定均线集合内的最后一个均线数据。
		MoveAverageBean first = targetMaList.get(0);                                                                                              // 得到给定均线集合内的第一个均线数据。
		double speed = last.getAvg().subtract(first.getAvg()).divide(new BigDecimal(compareNums), 3, RoundingMode.HALF_UP).doubleValue();         // 计算在单个时间内均线的“升高或下降”的速度。

		MaDataResult maDataResult = new MaDataResult().setMaList(sourceMaList).setUpRate(upRate).setDownRate(downRate).setSpeed(speed);           // 创建均线结果类。
		
		// 当最后一个均线高于第一个均线，且上升占比大于下降占比时，返回“上升形态”。
		if ((last.getAvg().compareTo(first.getAvg()) == 1) && upRate > downRate) {
			return maDataResult.setPattern(SingleMaPatternEnum.UP);
		}
		
		// 当最后一个均线低于第一个均线，且下降占比大于上升占比时，返回“下降形态”。
		if ((last.getAvg().compareTo(first.getAvg()) == -1) && downRate > upRate) {
			return maDataResult.setPattern(SingleMaPatternEnum.DOWN);
		}
		
		return maDataResult.setPattern(SingleMaPatternEnum.SHOCK);
	}
	
	/**
	 * 计算均线中速度的改变。
	 * 
	 * @param maList 均线结果集
	 * @param timeRangeOfLongTrend 均线运行速度的取值范围
	 * @param speedSpanOfLongTrend 均线运行速度的取值跨度
	 * @return MaSpeedChangeResult
	 */
	private MaSpeedChangeResult maSpeedPattern (List<MoveAverageBean> maList, int timeRangeOfLongTrend, int speedSpanOfLongTrend) {
		
		if (maList == null || maList.isEmpty()) {
			return null;
		}
		
		timeRangeOfLongTrend = (timeRangeOfLongTrend == 0 || timeRangeOfLongTrend > maList.size()) ? maList.size() : timeRangeOfLongTrend;
		speedSpanOfLongTrend = (speedSpanOfLongTrend == 0 || speedSpanOfLongTrend > maList.size()) ? 2 : speedSpanOfLongTrend;

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
		
		/* 某段均线中运动速度的总和。*/
		BigDecimal sumSpeed  = new BigDecimal(0);
		/* 某段均线中运动速度之间差值的集合。*/
		List<BigDecimal> diffSpeedList = new ArrayList<BigDecimal>();
		
		for (int i = 0; i < speedList.size(); i++) {
			
			BigDecimal last = speedList.get(i);                                                                         // 得到某段均线中的最后一个运动速度。
			sumSpeed = sumSpeed.add(last).setScale(6, RoundingMode.HALF_UP);                                            // 计算某段均线中运动速度的总和。
			
			if ((i + 1) >= speedList.size()) { break; }
			
			BigDecimal first = speedList.get(i + 1);                                                                    // 得到某段均线中的第一个运动速度。
			diffSpeedList.add(last.subtract(first).setScale(6,RoundingMode.HALF_UP));                                   // 把某段均线中最后一个和第一个运动速度的差值放入集合。
		}
		
		/* 某段均线中运动速度的均值。*/
		BigDecimal avgSpeed = sumSpeed.divide(new BigDecimal(speedList.size()), 5, RoundingMode.HALF_UP);
		
		/* 某段均线中运动速度之间差值的总和。*/
		BigDecimal sumDiffSpeed = new BigDecimal(0);
		for (BigDecimal subSpeed : diffSpeedList) { sumDiffSpeed = sumDiffSpeed.add(subSpeed); }
		/* 某段均线中运动速度之间差值的均值。*/
		BigDecimal avgDiffSpeed = sumDiffSpeed.divide(new BigDecimal(diffSpeedList.size()), 5, RoundingMode.HALF_UP);
		
		// --- 构造结果对象 ---
		
		MaSpeedChangeResult maSpeedChangeResult = new MaSpeedChangeResult();
		
		// 之前的speedList是倒序的，这里修改为正序。
		List<BigDecimal> tempSpeedList = new ArrayList<BigDecimal>();
		for (int i = speedList.size() - 1; i >= 0; i--) {
			tempSpeedList.add(speedList.get(i));
		}
		maSpeedChangeResult.setSpeedList(tempSpeedList);
		
		maSpeedChangeResult.setSumSpeed(sumSpeed);
		maSpeedChangeResult.setAvgSpeed(avgSpeed);
		
		// 之前的diffSpeedList是倒序的，这里修改为正序。
		List<BigDecimal> tempDiffSpeedList = new ArrayList<BigDecimal>();
		for (int i = diffSpeedList.size() - 1; i >= 0; i--) {
			tempDiffSpeedList.add(diffSpeedList.get(i));
		}
		
		maSpeedChangeResult.setDiffSpeedList(tempDiffSpeedList);
		maSpeedChangeResult.setSumDiffSpeed(sumDiffSpeed);
		maSpeedChangeResult.setAvgDiffSpeed(avgDiffSpeed);
		
		return maSpeedChangeResult;
	}
	
	// ----------------------- private class ---------------------------
	
	/**
	 * 均线计算结果类。
	 * 
	 * @author FrankTaylor <mailto:franktaylor@163.com>
	 * @since 2015/2/6
	 * @version 1.0
	 */
	private class MaDataResult {
		/** 均线集合类。*/
		private List<MoveAverageBean> maList;
		/** 升高占比。*/
		private Double upRate;
		/** 下降占比。*/
		private Double downRate;
		/** 速度。*/
		private Double speed;
		/** 均线形态枚举。*/
		private SingleMaPatternEnum pattern;
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(getClass().getSimpleName());
			builder.append("[\n")
			.append("\t").append("upRate = ").append(upRate).append("\n")
			.append("\t").append("downRate = ").append(downRate).append("\n")
			.append("\t").append("speed = ").append(speed).append("\n")
			.append("\t").append("pattern = ").append(pattern).append("\n")
			.append("]\n");
			return builder.toString();
		}
		
		// --- get method and set method ---
		
		public List<MoveAverageBean> getMaList() {
			return maList;
		}
		public MaDataResult setMaList(List<MoveAverageBean> maList) {
			this.maList = maList;
			return this;
		}
		public MaDataResult setUpRate(Double upRate) {
			this.upRate = upRate;
			return this;
		}
		public MaDataResult setDownRate(Double downRate) {
			this.downRate = downRate;
			return this;
		}
		public Double getSpeed() {
			return speed;
		}
		public MaDataResult setSpeed(Double speed) {
			this.speed = speed;
			return this;
		}
		public SingleMaPatternEnum getPattern() {
			return pattern;
		}
		public MaDataResult setPattern(SingleMaPatternEnum pattern) {
			this.pattern = pattern;
			return this;
		}
	}
	
	/**
	 * 均线运动速度改变的结果类。
	 * 
	 * @author FrankTaylor <mailto:franktaylor@163.com>
	 * @since 2015/4/19
	 * @version 1.0
	 */
	private class MaSpeedChangeResult {
		
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
}