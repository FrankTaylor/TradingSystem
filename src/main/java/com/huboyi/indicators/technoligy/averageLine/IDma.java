package com.jd.quant.api.indicators.technoligy.averageLine;


import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.function.QuoteFunction;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.Period;

/**
 * 加权移动平均。
 *
 * Created by hubin3 on 2016/8/4.
 */
public class IDma implements Ma {

    /** 行情镜像。*/
    private final IStatistics statistics;
    /** 行情周期。*/
    private final Period period;
    /** 历史数据取值类型。*/
    private final IStatisticsDataType dataType;
    /** 权重。*/
    private final double weight;

    /** 精度（由于平台的的默认精度是 4，因此该精度仅供 “不对外公开的构造器” 使用。目的是为了校验 “自开发结果” 与 “参照结果” 的正确性）。*/
    private static final int DEFAULT_SCALE = 4;
    private int scale = DEFAULT_SCALE;
    // --- 暴露给用户的数据 ---

    /** 计算出的均值数组，该结构采用由近到远方式来排序数据，即 avgs[0] 为近期的均值数据。*/
    private Double[] avgs = {};

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param dataType IStatisticsDataType
     * @param weight 权重
     */
    public IDma(IStatistics statistics, Period period, IStatisticsDataType dataType, double weight) {
        this.statistics = statistics;
        this.period = period;
        this.dataType = dataType;
        this.weight = weight;

        init();
    }

    // --- 不对外公开的构造函数 ---
    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param weight 权重
     */
    public IDma(Double[] vs, double weight) {
        this(vs, weight, DEFAULT_SCALE);
    }

    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param weight 权重
     * @param scale 精度
     */
    public IDma(Double[] vs, double weight, int scale) {
        this.statistics = null;
        this.period = null;
        this.dataType = null;
        this.weight = weight;

        this.scale = scale;

        init(vs);
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
     * 供构造函数调用的 “初始函数”，负责　“参数校验”、“取行情数据” 和 “计算均值”。
     */
    private void init() {

        int range = 1000;                                                                                   // 由于 DMA 计算的特殊性，历史数据取值范围暂定为 1000 条。
        Double[] hvs = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, dataType, range);     // 取得历史数据。
        avgs = compute(hvs);                                                                                // 计算出均值。
    }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验” 和 “计算均值”。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     */
    private void init(final Double[] vs) {
        avgs = compute(vs);
    }

    /**
     * 根据数据序列计算均值，返回的均值数列 “由近到远” 排列，同时对小数精度进行了处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @return Double[]
     */
    private Double[] compute(final Double[] vs) {
        Double[] temp = QuoteFunction.dma(vs, weight);
        return TechnoligyTools.reverseAndScaleDoubles(temp, scale);
    }
}
