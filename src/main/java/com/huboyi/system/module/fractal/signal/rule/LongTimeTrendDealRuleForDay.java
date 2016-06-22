//package com.huboyi.system.module.fractal.signal.rule;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.List;
//
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//
//import com.huboyi.data.entity.StockDataBean;
//import com.huboyi.indicators.technology.TechAlgorithm;
//import com.huboyi.indicators.technology.constant.BandType;
//import com.huboyi.indicators.technology.constant.SingleMaPattern;
//import com.huboyi.indicators.technology.entity.pattern.BandBean;
//import com.huboyi.indicators.technology.entity.pattern.FractalBean;
//import com.huboyi.indicators.technology.entity.trend.MoveAverageBean;
//import com.huboyi.indicators.technology.entity.trend.MoveAverageSpeedChangeBean;
//import com.huboyi.indicators.technology.entity.trend.MoveAverageStatisticsBean;
//import com.huboyi.strategy.function.BandFunction;
//import com.huboyi.strategy.function.StockDataFunction;
//import com.huboyi.system.SnapDealSignal;
//import com.huboyi.system.bean.DealSignalBean;
//import com.huboyi.system.bean.IndicatorsInfoBean;
//import com.huboyi.system.bean.PositionInfoBean;
//import com.huboyi.system.function.PositionFunction;
//import com.huboyi.system.module.fractal.signal.bean.FractalIndicatorsInfoBean;
//
///**
// * 长期趋势交易系统进出场规则。
// * 
// * @author FrankTaylor <mailto:franktaylor@163.com>
// * @since 2015/9/12
// * @version 1.0
// */
//public class LongTimeTrendDealRuleForDay implements SnapDealSignal {
//	
//	/** 日志。*/
//	private static final Logger log = LogManager.getLogger(LongTimeTrendDealRuleForDay.class);
//	
//	/** 最大盈利比例。*/
//	private BigDecimal maxProfitRatio;
//	/** 最大亏损比例。*/
//	private BigDecimal maxLossRatio;
//	
//	/** 止盈比例。*/
//	private BigDecimal stopProfitRatio;
//	/** 是否已到达止盈警戒线。*/
//	private boolean isStopProfitWarningLine;
//	/** 止盈回撤比例。*/
//	private BigDecimal stopProfitDrawdownRatio;
//	
//	/** 止损比例。*/
//	private BigDecimal stopLossRatio;
//	
//	/** 最大最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅。*/
//	private BigDecimal maxRateOfLastSdAndMa120;
//	
//	/**
//	 * 构造函数。
//	 */
//	public LongTimeTrendDealRuleForDay() {
//		initialize();
//	}
//	
//	/**
//	 * 参数初始化。
//	 */
//	private void initialize() {
//		maxProfitRatio = BigDecimal.ZERO;                     // 最大盈利比例。
//		maxLossRatio = BigDecimal.ZERO;                       // 最大亏损比例。
//		
//		stopProfitRatio = BigDecimal.valueOf(0.1);            // 止盈比例。
//		isStopProfitWarningLine = false;                      // 是否已到达止盈警戒线。
//		stopProfitDrawdownRatio = BigDecimal.valueOf(0.03);   // 止盈回撤比例。
//		stopLossRatio = BigDecimal.valueOf(-0.05);            // 止损比例。
//		
//		maxRateOfLastSdAndMa120 = BigDecimal.ZERO;            // 最大最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅。
//	}
//	
//	@Override
//	public DealSignalBean snapBuyToOpenSignal(
//			String stockCode,
//			List<StockDataBean> stockDataList,
//			IndicatorsInfoBean indicatorsInfo,
//			List<PositionInfoBean> positionInfoList) {
//		log.info("调用 [捕捉长期趋势建仓信号] 方法。"); 
//		
//		FractalIndicatorsInfoBean fractalIndicatorsInfo = (FractalIndicatorsInfoBean)indicatorsInfo;
//		
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 长期趋势建仓战法本质：只有当长期趋势向上，股价充分回调，使股价在未来具有充分上涨空间时  +
//		 * + 才可以运用的战法。                                                                                                                                          + 
//		 * +-----------------------------------------------------------+
//		 */
//		
//		/*
//		 * ##################################################
//		 * # 1、势                                                                                                                                  #
//		 * ##################################################
//		 * 
//		 * 在牛市行情中任何一个点位（时间）买入，（1）只要牛市的行情仍能延续一段时间；（2）只要投资标的不出现什么问题（有炒作点更好）；那么在理论上都是正确的。
//		 * 但由于大盘的顶底是不可预测的，有可能你刚在一个相对的低点买入，没过多久大盘就见顶了，如果此时你还没有出场，就有可能造成损失。
//		 * 
//		 * 所以说判断长期趋势的强弱，对于辨别走势的牛熊是重要的，而对于辨别买/卖点的准确性（风险的高低）而言， 就不是那么重要了。
//		 * 
//		 * 另外还需要注意的是，由于牛市出现的次数较少，所以该战法在本质上是“低准确性，高收益”的。
//		 */
//		
//		// ---------------------------- 1.1、判断长期趋势 ----------------------------
//		/*
//		 * 只有下列条件都满足时，才能证明长期趋势稳定向上：
//		 * 
//		 * 1.1.1、前10天的120日均线的上升速度 > 0；
//		 * 1.1.2、近10天的120日均线的上升速度 >= 0.003；（发现利润要趁早）
//		 */
//		MoveAverageStatisticsBean bef120MAS = TechAlgorithm.MAS(stockDataList, 120, 10, -10);                                                   // 得到前10天的120日均线的计算结果。
//		MoveAverageStatisticsBean cur120MAS = TechAlgorithm.MAS(stockDataList, 120, 10, 0);                                                     // 得到近10天的120日均线的计算结果。
//		if (
//				// 1.1.1
//				(
//						bef120MAS == null || 
//						bef120MAS.getPattern() != SingleMaPattern.UP ||
//						bef120MAS.getSpeed() < 0
//				) 
//				
//				||
//				
//				// 1.1.2
//				(
//						cur120MAS == null ||
//						cur120MAS.getPattern() != SingleMaPattern.UP ||
//						cur120MAS.getSpeed() < 0.003
//				)
//				
//			) {
//			
//			return null;
//		}
//		
//		// ---------------------------- 1.2、判断中期趋势 ----------------------------
//		/*
//		 * 中期趋势是长期趋势未来发展的催化剂，若其向上发展的力度较弱，则不能建立仓位。
//		 * 
//		 * 1.2.1、近60日均线在120日均线之上。（中期趋势仍强于长期趋势）
//		 * 1.2.2、近60日均线成上升状态。（中期趋势仍健康发展）
//		 * 1.2.3、近60日均线运动速度的5日均线 >= 0。（中期趋势的运动速度还未出现走弱的迹象）
//		 */
//		MoveAverageStatisticsBean cur60MAS = TechAlgorithm.MAS(stockDataList, 60, 5, 0);                                                        // 得到近5天的60日均线的计算结果。		
//		
//		MoveAverageBean lastMaOfCurMa120 = cur120MAS.getMaList().get(cur120MAS.getMaList().size() - 1);                                         // 得到当前120日均线的价格。
//		MoveAverageBean lastMaOfCurMa60 = cur60MAS.getMaList().get(cur60MAS.getMaList().size() - 1);                                            // 得到当前60日均线的价格。
//		
//		MoveAverageSpeedChangeBean cur60MASC = TechAlgorithm.MASC(cur60MAS.getMaList(), 10, 2);                                                 // 得到近60天的10日均线的计算结果。
//		List<MoveAverageBean> speedMa5ListOfCur60MASC = cur60MASC.getSpeedMA(5);                                                                // 得到60日均线运动速度的5日均线。
//		MoveAverageBean lastOfSpeedMa5ListOfCur60MASC = speedMa5ListOfCur60MASC.get(speedMa5ListOfCur60MASC.size() - 1);                        // 得到最后一个60日均线运动速度的5日均线价格。
//		
//		if (
//				// 1.2.1
//				(lastMaOfCurMa60.getAvg().compareTo(lastMaOfCurMa120.getAvg()) != 1) 
//				
//				||
//				
//				// 1.2.2
//				(cur60MAS.getSpeed() <= 0) 
//				
//				||
//				
//				// 1.2.3
//				(lastOfSpeedMa5ListOfCur60MASC.getAvg().doubleValue() <= 0)
//			) {
//			return null;
//		}
//		
//		// ---------------------------- 1.3、判断短期趋势 ----------------------------
//		/*
//		 * 只有中、长期趋势稳定向上，短期趋势由跌转升时才有可能出现买点：
//		 * 
//		 * 1.2.1、近60日均线在120日均线之上。（中期趋势还未发生本质改变）
//		 * 1.2.2、近60日均线成上升状态。（中期趋势还未走弱）
//		 * 1.2.3、近3天5日线呈震荡或上升状态，且上升速度 >= 0。（超短期趋势向上）
//		 */	
//		MoveAverageStatisticsBean cur5MAS = TechAlgorithm.MAS(stockDataList, 5, 3, 0);                                                          // 得到近3天的5日均线的计算结果。
//
//		MoveAverageSpeedChangeBean cur5MASC = TechAlgorithm.MASC(cur5MAS.getMaList(), 5, 2);                                                    // 得到近5天的5日均线的计算结果。
//		List<MoveAverageBean> speedMa5ListOfCur5MASC = cur5MASC.getSpeedMA(3);                                                                  // 得到5日均线运动速度的3日均线。
//		MoveAverageBean lastOfSpeedMa5ListOfCur5MASC = speedMa5ListOfCur5MASC.get(speedMa5ListOfCur5MASC.size() - 1);                           // 得到最后一个5日均线运动速度的3日均线价格。
//
//		if (
//				// 1.3.1
//				/*
//				 * 如果近3天的5日均线的速度为负，就说明近期5均线趋势仍然向下，为保险起见，即使此刻出现了一个底分型，也应以过滤。
//				 * 
//				 *  T
//				 *  |
//				 * | |
//				 *    |
//				 *     |
//				 *      |
//				 *       | 
//				 *        | NT
//				 *         | |
//				 *          | |
//				 *         NB  |
//				 *              |    
//				 * 图1、在下降途中出现的虚假底分型。       
//				 */
//				(lastOfSpeedMa5ListOfCur5MASC.getAvg().doubleValue() <= 0)
//			) {
//			return null;
//		}
//		
//		/*
//		 * ##################################################
//		 * # 2、位                                                                                                                                  #
//		 * ##################################################
//		 * 
//		 * 虽然理论上说，牛市中任何的买点都是正确的，但是买点与买点之间的风险系数是不同的。如果你在一个上涨的波段中刚一买入，该走势就进行了回调，
//		 * 虽然可能过不了多久，价格就会涨上来，但你必须承受短期的账户浮亏，从而降低了资金利用率，这是风险之一；由于行情是不可预测的，万一回调后，
//		 * 市场就由牛转熊，那么你就必须承受长期的账户浮亏了，这是风险之二。
//		 * 
//		 * 由此可见，判断“位”的根本意义在于：降低在牛使中买入的风险。
//		 */
//		
//		/*
//		 * 2.1、最后一根K线的收盘价距对应的120日均线的涨幅不能超过50%。
//		 * 
//		 */
//		StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                                        // 得到最后一根K线。
//		
//		BigDecimal closeOfLastStockData = lastStockData.getClose();                                                                             // 得到最后一根K线的收盘价。
//		BigDecimal avgOfMa120 = cur120MAS.getMaList().get(cur120MAS.getMaList().size() - 1).getAvg();                                           // 得到当天120日均线的均价。
//		
//		BigDecimal diffOfCloseAndMa120 = closeOfLastStockData.subtract(avgOfMa120).setScale(3, RoundingMode.HALF_UP);                           // 得到最后一根K线的收盘价和与其对应的120日均线的价格之间的差值。
//		BigDecimal rateOfCloseAndMa120 = diffOfCloseAndMa120.divide(avgOfMa120, 3, RoundingMode.HALF_UP);                                       // 得到最后一根K线的收盘价和与其对应的120日均线的价格之间的涨跌幅。
//		
//		if (rateOfCloseAndMa120.doubleValue() >= 0.5) {
//			return null;
//		}
//		
//		/*
//		 * 2.1、当前波段必须是向下波段。
//		 */
//		BandBean lastBand = BandFunction.getLastBand(fractalIndicatorsInfo.getBandBeanList());                                                  // 得到最后一个波段。
//		if (lastBand.getBandType() != BandType.DOWN) {
//			return null;
//		}
//		
//		// 仓位控制：------------------------------------------------------------------------------------------------
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 仓位管控1：如果在某上升一波段内已建仓，则直接退出捕捉买入建仓程序。                                       +
//		 * +-----------------------------------------------------------+
//		 * 
//		 * 风控目的：避免在某一上升波段内重复建仓。
//		 */
//		PositionInfoBean lastBuyPosition = PositionFunction.getLastNoClosePosition(positionInfoList, DealSignal.FIBO_B);                        // 查询出最后一笔未平仓的斐波那契仓位。
//		if (lastBuyPosition != null) {
//			BandBean lastOpenBand = BandFunction.getBandBeanByDate(fractalIndicatorsInfo.getBandBeanList(), lastBuyPosition.getOpenDate());     // 查询出最后一个建仓的所在波段。
//			
//			/*
//			 * 在最后一个波段和平仓波段是同一个波段的情况下，只有当平仓波段为上波段时，才退出建仓信号捕捉程序，有以下两个原因：
//			 * 1、在下降波段中不能因为“相对过早的建仓点”而影响“潜在的建仓点”；
//			 * 2、在上升波段中，不能因为重复建仓，而增大风险。
//			 * 
//			 *                            /\
//			 *                           /  \
//			 *                          /<-应被屏蔽的建仓点
//			 *  相对过早的建仓点 -> \        /
//			 *                 \      /
//			 *                  \    /
//			 *    被影响的潜在建仓点-> \  /<-建仓点
//			 *                    \/ <-向上波段起点
//			 *
//			 * 图3、当平仓波段没有被限制时，所引发的的问题
//			 */
//			if (lastOpenBand != null && lastOpenBand.isOneAndTheSame(lastBand) && lastOpenBand.getBandType() == BandType.UP) {					
//				return null;
//			}
//		}
//		
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 仓位管控2：从最后一个底分型到当前K线，如果已建仓，则直接退出捕捉买入建仓程序。                +
//		 * +-----------------------------------------------------------+
//		 * 
//		 * 风控目的：避免在该底分型附近重复建仓。
//		 */
//		
//		FractalBean bottomOfLastBand = lastBand.getBottom();                                                                                    // 得到最后一个波段中的底分型。
//		if (
//				lastBuyPosition != null &&
//				lastBuyPosition.getOpenDate() >= bottomOfLastBand.getLeft().getDate() && 
//				lastBuyPosition.getOpenDate() <= lastStockData.getDate()) {
//			return null;
//		}
//		// 仓位控制：------------------------------------------------------------------------------------------------
//		
//		/*
//		 * ##################################################
//		 * # 3、态                                                                                                                                  #
//		 * ##################################################
//		 */
//		
//		/*
//		 * 3.1、底分型形成后，从中间K线到当前K线中任意一根K线的上升力度必须大于0.03，才能证明有足够的上升力度。
//		 */
//
//		if (StockDataFunction.isHaveEnoughUpStrength(stockDataList, fractalIndicatorsInfo.getBandBeanList(), 0.03)) {
//			System.out.println("---> " + lastStockData.getDate() + " 日，出现斐波那契建仓信号 " + " <---" );
//			return new DealSignalBean(lastStockData, DealSignal.FIBO_B);
//		}
//		
//		return null;
//	}
//
//	@Override
//	public DealSignalBean snapSellToCloseSignal(
//			String stockCode,
//			List<StockDataBean> stockDataList,
//			IndicatorsInfoBean indicatorsInfo,
//			List<PositionInfoBean> positionInfoList) {
//		log.info("调用 [捕捉长期趋势平仓信号] 方法。"); 
//		
//		@SuppressWarnings("unused")
//		FractalIndicatorsInfoBean fractalIndicatorsInfo = (FractalIndicatorsInfoBean)indicatorsInfo;
//		
//		/*
//		 * 
//		 * 
//		 * 风控目的：风控的实质是平稳主操盘手的心态，使进出场成为理性的产物。
//		 * 
//		 * 核心思想：市场是不可预测的，对已建仓的股票，应通过阶段性的波段操作和合理的仓位控制来回避不可预知的风险。
//		 * 
//		 * 思想基础：要尽力避开在上升过程中的每一次相对较大的回调，及时锁定利润、提高资金利用率、节约时间成本、降低风险。
//		 * 因为每一次较大的回调都可以看成是主力的一次出货。虽然长期趋势不会因某一次大的回调而改变，但每发生一次，就增加了
//		 * 一份风险，总有那么一次的回调后，会成为压垮骆驼的最后一根稻草。而且作为有程序交易系统的小散也没有必要参与回调的
//		 * 过程，即便回调的时间较短，会损失一部分利润，但可以通过程序在众多投资标的中找到新的投资目标，从而弥补这部分损失。
//		 * 
//		 * 可行分析：股票价格涨跌的原因有很多，但根本的因素还是钱，而具备大资金的必定是机构，所以机构会根据其自身的情况来
//		 * 调控股票的价格，但什么时候会调控，这个是不可预知的。我只能把自己想成一名赌徒，由于优秀的赌徒和拙劣的赌徒之间最
//		 * 大的区别，就是对'行情的判断'、'赌资的管理'和'欲望的控制'等，所以要针对这三个方面做一些思考。
//		 * 
//		 * 在阶段性出场这个环节，我认为分析的重点有三个：
//		 * 
//		 * 1、'某只股票的整体仓位盈利幅度'：由于股价是不可预测的，所以风险只能降低，而却不能避免。因此当盈利幅度到达某一
//		 * 界线时，及时的落袋为安就成为进一步的降低风险手段。
//		 * 
//		 * 2、'股价在一定涨幅后的巨大下跌'：涨幅后的突然暴跌，说明该股发生了某种大事件导致了股价的巨幅下挫，可能是庄家洗
//		 * 盘、可能是热钱撤走、也可能是某种利空，但不管怎样这也是一种风险，应该进行回避。
//		 * 
//		 * 3、'120日均线的价格和与其对应的向上波段的高点之间的涨幅'：当该涨幅较大时，说明近一段时间内，股价向上发展的势
//		 * 头很旺，股价与长期趋势间的距离逐步加大，由于发展迅速，这也加深了股价回调的空间，所以这种力度的上涨是存在风险的。
//		 * 
//		 * 
//		 * 那为什么不考虑档较小的涨幅和较小的盈利幅度呢？
//		 * 1、涨幅小，获利少，风险相对就底，及时最后被迫出场，在心里上没有什么可惜的；
//		 * 2、在涨幅较小、回调较小，之后又大幅上扬这种情况中，频繁的进出场，会增加手续费，同时可能就会错失行情；
//		 */
//		
//		/*
//		 * 限制1：如果没有菲薄纳妾信号，就不用捕捉该平仓信号了，以降低计算复杂度。
//		 */
//		PositionInfoBean lastNoClosePosition = PositionFunction.getLastNoClosePosition(positionInfoList, DealSignal.FIBO_B);                    // 查询出最后的斐波仓位。
//		if (lastNoClosePosition == null) {
//			return null;
//		}
//		
//		
//		/*
//		 * ##################################################
//		 * # 1、左侧主动平仓 —— 以盈利幅度为主，K线与长期趋势乖离幅度为辅。             #
//		 * ##################################################
//		 * 
//		 * 在盈利幅度达到50%的情况下，通过改变持仓来降低风险系数的参照：
//		 * 
//		 * 10（建仓股价） * 10000（股数） = 100000（建仓本金）
//		 * 15（当前股价） * 10000（股数） = 150000（当前资产）
//		 * 
//		 * 100000 - (15 * 5000) = 25000         （平掉5/10）
//		 * 25000 / 100000 = 0.25（风险系数）
//		 * 
//		 * 100000 - (15 * 4000) = 40000         （平掉4/10）
//		 * 40000 / 100000 = 0.4（风险系数）
//		 * 
//		 * 100000 - (15 * 3000) = 55000         （平掉3/10）
//		 * 55000 / 100000 = 0.55（风险系数）
//		 * 
//		 * 100000 - (15 * 2000) = 70000         （平掉2/10）
//		 * 70000 / 100000 = 0.7（风险系数）
//		 * 
//		 * 100000 - (15 * 1000) = 85000         （平掉1/10）
//		 * 85000 / 100000 = 0.85（风险系数）
//		 * 
//		 */
//		StockDataBean lastStockData = StockDataFunction.getLastStockData(stockDataList);                                                        // 得到最后一根K线。
//		
//		// --- 计算整体仓位的盈利情况。
//		List<PositionInfoBean> noClosePositionList = PositionFunction.getAllNoClosePositionList(positionInfoList, DealSignal.FIBO_B);           // 查询出所有未平仓的斐波仓位。
//		BigDecimal openCost = new BigDecimal(0);                                                                                                // 记录整体仓位的建仓成本。
//		Long canCloseNumber = 0L;                                                                                                               // 记录整体仓位的可平仓数量。
//		for (PositionInfoBean position : noClosePositionList) {
//			openCost = openCost.add(position.getOpenCost());
//			canCloseNumber += position.getCanCloseNumber();
//		}
//		BigDecimal newMarketValue = lastStockData.getClose().multiply(new BigDecimal(canCloseNumber));                                          // 计算整体仓位的最新市值。
//		BigDecimal floatProfitAndLoss = newMarketValue.subtract(openCost);                                                                      // 浮动盈亏。公式 = 最新市值 - 建仓成本 
//		BigDecimal profitAndLossRatio = floatProfitAndLoss.divide(openCost, 3, RoundingMode.HALF_UP);                                           // 盈亏比例。公式 = 浮动盈亏 / 建仓成本
//		
//		// --- 记录最大盈亏比例。
//		if (maxProfitRatio.compareTo(profitAndLossRatio) != 1) { maxProfitRatio = profitAndLossRatio; }
//		if (maxLossRatio.compareTo(profitAndLossRatio) != -1) { maxLossRatio = profitAndLossRatio; }
//		
//		// --- 判断是否已到达止盈警戒线。
//		if (profitAndLossRatio.compareTo(stopProfitRatio) != -1) { isStopProfitWarningLine = true; }
//		
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 出场条件1：                                                                                                                                                       +
//		 * + 1、整体仓位亏损超过止损比例时，平掉所有仓位；                                                                                   +
//		 * +-----------------------------------------------------------+
//		 */
//		if (profitAndLossRatio.compareTo(stopLossRatio) != 1) {
//			initialize();
//			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|RIGHT|1|");
//			return new DealSignalBean(lastStockData, DealSignal.SELL_ALL);
//		}
//		
//		/*
//		 * +-----------------------------------------------------------+
//		 * + 出场条件2：                                                                                                                                                       +
//		 * + 1、整体仓位盈利达到警戒线；                                                                                                                       +
//		 * + 2、最大盈利比例 - 当前整体仓位盈利 >= 止盈回撤比例；                                                                  +
//		 * +-----------------------------------------------------------+
//		 */
//		if (
//				isStopProfitWarningLine
//				&&
//				(maxProfitRatio.subtract(profitAndLossRatio).compareTo(stopProfitDrawdownRatio) != -1)
//		) {
//			initialize();
//			System.out.println("|FIBO|SELL|" + lastStockData.getDate() + "|LEFT|2|");
//			return new DealSignalBean(lastStockData, DealSignal.SELL_ALL);
//		}
//		
//		// --- 计算当前K线与120日均线之间涨跌幅。
//		BigDecimal highOfLastStockData = lastStockData.getHigh();                                                                               // 得到最后一根K线的高价。
//		
//		MoveAverageStatisticsBean cur120MAS = TechAlgorithm.MAS(stockDataList, 120, 10, 0);                                                     // 得到近10天的120日均线的计算结果。
//		MoveAverageBean lastMaOfCurMa120 = cur120MAS.getMaList().get(cur120MAS.getMaList().size() - 1);                                         // 得到当前120日均线的价格。
//		
//		BigDecimal diffOfLastSdAndMa120 = highOfLastStockData.subtract(lastMaOfCurMa120.getAvg()).setScale(3, RoundingMode.HALF_UP);            // 得到最后一根K线的高点和与其相对应的120日均线的价格之间的差值。
//		BigDecimal rateOfLastSdAndMa120 = diffOfLastSdAndMa120.divide(lastMaOfCurMa120.getAvg(), 3, RoundingMode.HALF_UP);                      // 得到最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅。
//		
//		// --- 记录最大最后一根K线的高点和与其相对应的120日均线的价格之间的涨跌幅。
//		if (maxRateOfLastSdAndMa120.compareTo(rateOfLastSdAndMa120) != 1) { maxRateOfLastSdAndMa120 = rateOfLastSdAndMa120; }
//		
//		return null;
//	}
//}