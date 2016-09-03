package com.jd.quant.api.indicators.technoligy.volume;

import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.function.QuoteFunction;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.indicators.technoligy.averageLine.IMa;
import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.Period;

import java.util.Arrays;

/**
 * 平均成交量。
 *
 * Created by hubin3 on 2016/8/18.
 */
public class IVol {

    /** 行情镜像。*/
    private final IStatistics statistics;
    /** 行情周期。*/
    private final Period period;
    /** 步长（快线）。*/
    private final int fast;
    /** 步长（慢线）。*/
    private final int slow;
    /** 取值个数。*/
    private final int num;

    /** 精度（由于平台的的默认精度是 4，因此该精度仅供 “不对外公开的构造器” 使用。目的是为了校验 “自开发结果” 与 “参照结果” 的正确性）。*/
    private static final int DEFAULT_SCALE = 4;
    private int scale = DEFAULT_SCALE;

    // --- 暴露给用户的数据 ---

    /** 计算出的快线均值数组，该结构采用由近到远方式来排序数据，即 fastAvgs[0] 为近期的均值数据。*/
    private Double[] fastAvgs = {};
    /** 计算出的慢线均值数组，该结构采用由近到远方式来排序数据，即 slowAvgs[0] 为近期的均值数据。*/
    private Double[] slowAvgs = {};

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param fast 步长（快线）
     * @param slow 步长（慢线）
     * @param num 取值个数
     */
    public IVol(IStatistics statistics, Period period, int fast, int slow, int num) {
        this.statistics = statistics;
        this.period = period;
        this.fast = fast;
        this.slow = slow;
        this.num = num;

        init();
    }

    // --- 不对外公开的构造函数 ---
    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param fast 步长（快线）
     * @param slow 步长（慢线）
     * @param num 取值个数
     */
    public IVol(Double[] vs, int fast, int slow, int num) {
        this(vs, fast, slow, num, DEFAULT_SCALE);
    }

    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param fast 步长（快线）
     * @param slow 步长（慢线）
     * @param num 取值个数
     * @param scale 精度
     */
    public IVol(Double[] vs, int fast, int slow, int num, int scale) {
        this.statistics = null;
        this.period = null;
        this.fast = fast;
        this.slow = slow;
        this.num = num;

        this.scale = scale;

        init(vs);
    }

    /**
     * 得到 “成交量快速均线” 数组中的第一个值。
     *
     * @return Double
     */
    public Double getFastAvg() {
        return (!ArrayUtils.isEmpty(fastAvgs)) ? fastAvgs[0] : null;
    }

    /**
     * 得到 “成交量快速均线” 数组。
     *
     * @return Double[]
     */
    public Double[] getFastAvgs() {
        return fastAvgs;
    }

    /**
     * 得到 “成交量慢速均线” 数组中的第一个值。
     *
     * @return Double
     */
    public Double getSlowAvg() {
        return (!ArrayUtils.isEmpty(slowAvgs)) ? slowAvgs[0] : null;
    }

    /**
     * 得到 “成交量慢速均线” 数组。
     *
     * @return Double[]
     */
    public Double[] getSlowAvgs() {
        return slowAvgs;
    }

    // --- private method ---

    /**
     * 构造器参数有效性验证。
     *
     * @return boolean
     */
    private boolean consturctorParamValidate() { return (fast < 2 || slow <= fast || num < 1) ? false : true; }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验”、“取行情数据” 和 “计算均值”。
     */
    private void init() {

        if (!consturctorParamValidate()) { return; }

        int range = slow + num - 1;                                                                        // 取值范围。
        Double[] vs = TechnoligyTools.getIStatisticsHistoryDatas(                                          // 取得历史成交量。
                statistics, period, IStatisticsDataType.TURNOVER_VOLUME, range);

        Double[][] temp = compute(vs);
        if (temp != null && temp.length == 2) {
            fastAvgs = temp[0];
            slowAvgs = temp[1];
        }
    }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验” 和 “计算均值”。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     */
    private void init(final Double[] vs) {
        if (!consturctorParamValidate()) { return; }
        Double[][] temp = compute(vs);
        if (temp != null && temp.length == 2) {
            fastAvgs = temp[0];
            slowAvgs = temp[1];
        }
    }

    /**
     * 根据数据序列计算均值，返回的均值数列 “由近到远” 排列，同时对小数精度进行了处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @return Double[] [0][0]：快线均值数组；[1][0]：慢线均值数组
     */
    private Double[][] compute(final Double[] vs) {
        Double[] tempFast = QuoteFunction.ma(vs, fast);
        Double[] tempSlow = QuoteFunction.ma(vs, slow);

        Double[][] temp = new Double[2][1];

        temp[0] = TechnoligyTools.reverseAndSubAndScaleDoubles(tempFast, 0, (tempFast.length > num ? num : tempFast.length), scale);
        temp[1] = TechnoligyTools.reverseAndSubAndScaleDoubles(tempSlow, 0, (tempSlow.length > num ? num : tempSlow.length), scale);

        return temp;
    }
}
