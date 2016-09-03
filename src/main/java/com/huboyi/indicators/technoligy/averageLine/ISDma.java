package com.jd.quant.api.indicators.technoligy.averageLine;

import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.Period;

/**
 * 加强加权移动平均。
 *
 * Created by hubin3 on 2016/8/4.
 */
public class ISDma implements Ma {

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
    public ISDma(IStatistics statistics, Period period, IStatisticsDataType dataType) {
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

    /**
     * 根据数据序列计算均值。
     *
     * @param vs 数组序列（该数列必须符合由远到近方式排序）
     * @param step 步长
     * @param num 取值个数
     * @return Double[]
     */
    public static Double[] compute(final Double[] vs, final int step, final int num) { return new Double[] {}; }

    // --- private method ---
    /**
     * 在构造对象后，即把均值计算出来。
     */
    private void initCompute() {

        /**
         * 该指标是 Dma 的加强版，在该算法中，使用 “当前换手” 作为 “动态权重”，因此能较为客观地 “衡量当前价格的重要性”。
         * 我认为，通过该算法构造出的价格序列，“可能会” 较为准确的反应价格的走势。请一定要注意，我这里用了 “可能会”，这
         * 个带有不确定性的 “词汇”。
         *
         * 算法：若 Y = SDMA(X, M) 则 Y = (M * X + (1 - M) * Y')；Y'为上一周期 Y，且 M = 当前成交量 / 当前流通股数量，即：换手率
         *
         * 注意：由于目前历史行情接口中还未提供 “当前流通股数量” 于 “换手”，因此该算法还无法实现。
         */
        int range = 1000;                                                                                                              // 由于 SDMA 计算的特殊性，历史数据取值范围暂定为 1000 条。

        Double[] prices = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, dataType, range);                             // 取得历史价格数据。
        if (prices == null || prices.length < 1) { return; }

//        Double[] vols = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, IStatisticsDataType.TURNOVER_VOLUME, range);    // 取得历史成交量数据。
//
//
//        avgs = compute(hvs, weight);                                                                        // 计算出均值。
    }
}
