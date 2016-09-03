package com.jd.quant.api.indicators.technoligy.averageLine;

import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.Period;

/**
 * 市场成本加权移动平均。
 *
 * Created by hubin3 on 2016/8/5.
 */
public class IMcst implements Ma {

    /** 行情镜像。*/
    private final IStatistics statistics;
    /** 行情周期。*/
    private final Period period;
    /** 历史数据取值类型。*/
    private final IStatisticsDataType dataType;

    // --- 暴露给用户的数据 ---

    /** 计算出的均值数组，该结构采用由近到远方式来排序数据，即 avgs[0] 为近期的均值数据。*/
    private Double[] avgs = {};

    /**
     * Consturctor
     *
     * @param statistics IStatistics
     * @param period Period
     * @param dataType IStatisticsDataType权重
     */
    public IMcst(IStatistics statistics, Period period, IStatisticsDataType dataType) {
        this.statistics = statistics;
        this.period = period;
        this.dataType = dataType;

        initCompute();
    }

    // --- 接口方法 ---
    @Override
    public boolean crossUp(Ma o) {
        if ((this.avgs != null && this.avgs.length >= 2) &&
                (o.getAvgs() != null && o.getAvgs().length >= 2)) {
            if ((this.getAvgs()[1].doubleValue() <= o.getAvgs()[1].doubleValue()) &&
                    (this.getAvg().doubleValue() > o.getAvg().doubleValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean crossDown(Ma o) {
        if ((this.avgs != null && this.avgs.length >= 2) &&
                (o.getAvgs() != null && o.getAvgs().length >= 2)) {
            if ((this.getAvgs()[1].doubleValue() >= o.getAvgs()[1].doubleValue()) &&
                    (this.getAvg().doubleValue() < o.getAvg().doubleValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Double getAvg() {
        return (!ArrayUtils.isEmpty(avgs)) ? avgs[0] : null;
    }

    @Override
    public Double[] getAvgs() {
        return avgs;
    }


    // --- private method ---
    /**
     * 在构造对象后，即把均值计算出来。
     */
    private void initCompute() {

        /**
         * 该指标在通达信中被称为：市场成本。公式为：MCST:=DMA(AMOUNT/(100*VOL),VOL/CAPITAL);
         *
         * OK 我在开始编写代码前，先对公式中用到的各项定义，进行一下研究：
         *
         * --- 以 000158，2016-08-05 的日线数据为例 ---
         * CAPITAL（流通股数量，需在返回值后 * 100 以得到实际数值） = 7657856
         * VOL（成交量（手），需在返回值后 * 100 以得到实际数值） = 57995.172
         * AMOUNT（成交额，单位：元） = 77332480.000（元）
         *
         * 换手率 = VOL（57995.172 * 100）/ CAPITAL（7657856 * 100） = 0.0075732910099119
         * 每股成交均价 = AMOUNT（77332480）/ VOL（57995.172 * 100）= 13.33429617210205
         * 高开低收均价 = (13.30（开）+ 13.47（高）+ 13.18（开）+ 13.37（收）) / 4 = 13.33
         *
         * 研究到这里，我突然发现 “每股成交均价” 和 “高开低收均价” 这两项计算结果如此相近，是不是计算方式不同，但结果相同呢？
         * 为此，我又研究两组数据：
         *
         * --- 以 000158，2016-08-03 的日线数据为例 ---
         * 换手率 = 6219569.1 / 765785600 = 0.0081218151660204
         * 每股成交均价 = 82611624 / 6219569.1 = 13.28253174323604
         * 高开低收均价 = (13.38（开）+ 13.39（高）+ 13.19（开）+ 13.26（收）) / 4 = 13.305
         *
         * --- 以 000158，2016-08-02 的日线数据为例 ---
         * 换手率 = 4073776.2 / 765785600 = 0.0053197346620255
         * 每股成交均价 = 54417844.000 / 4073776.2 = 13.35808383386402
         * 高开低收均价 = (13.22（开）+ 13.44（高）+ 13.18（开）+ 13.44（收）) / 4 = 13.32
         *
         * 经过计算发现，这两项的计算结果并不相同，只是很接近，那是不是由于 “数据精度” 而导致的误差呢？关于这点我还尚未研究。
         *
         * OK 研究工作到这里就可以先告一段落了，下面将对公式的实现进行解读：
         * （1）AMOUNT/(100*VOL)：计算 “当日每股成交均价”；
         * （2）VOL/CAPITAL：计算 “当日换手率”，注意该 “换手率” 计算的结果并不准确，但由于这里只用 “换手率” 作为权重，因此，
         *     只要能反应出 “权重的大小” 即可，而不用十分准确。
         *
         * OK 研究到这里就明白了，MCST 本质上：只不过是以 “每日换手” 为 “权重”，对 “每日每股平均成交价格”，不断地进行修正。
         * 我之前就知道以 MA 和 MCST 为参考的战法，其主要思想如下：
         * （1）以 “收盘价” 是否站在 MA20 之上，作为区分牛熊的标准；
         * （2）在牛市中 “收盘价” 将普遍站在 MCST 之上，在回踩 MCST 线，企稳后便是买点。价格在 MCST 之上，乖离过大便是卖点。
         * （3）在熊市中 “收盘价” 将普遍站在 MCST 之下，在反弹 MCST 线，碰触后便是卖点。价格在 MCST 下上，乖离过大便是买点。
         *
         * 然而，从公式上看，MCST 只是对 “每日每股平均成交价格” 的不断修正，只能是体现出 “近似的市场成本”。如果想要精确地找出
         * “机构”、“游资”、“大散” 等重仓持有者的成本，还需做很多其他的工作。
         *
         * 注意：由于目前历史行情接口中还未提供 “当前流通股数量” 于 “换手”，因此该算法还无法实现。
         */
        int range = 1000;                                                                                                              // 由于 MCST 计算的特殊性，历史数据取值范围暂定为 1000 条。

        Double[] prices = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, dataType, range);                             // 取得历史价格数据。
        if (prices == null || prices.length < 1) { return; }

//        Double[] vols = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, IStatisticsDataType.TURNOVER_VOLUME, range);    // 取得历史成交量数据。
//
//
//        avgs = compute(hvs, weight);                                                                        // 计算出均值。
    }

}
