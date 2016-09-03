package com.jd.quant.api.indicators.technoligy.trend;

import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.function.QuoteFunction;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.Period;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by hubin3 on 2016/9/2.
 */
public class IDma {

    /** 行情镜像。*/
    private final IStatistics statistics;
    /** 行情周期。*/
    private final Period period;
    /** 历史数据取值类型。*/
    private final IStatisticsDataType dataType;

    /** 快线。*/
    private final int fast;
    /** 慢线。*/
    private final int slow;
    /** 步长。*/
    private final int step;
    /** 取值个数（该参数决定了 diffs、deas 和 macds 的取值个数）。*/
    private final int num;

    /** 精度（由于平台的的默认精度是 4，因此该精度仅供 “不对外公开的构造器” 使用。目的是为了校验 “自开发结果” 与 “参照结果” 的正确性）。*/
    private static final int DEFAULT_SCALE = 4;
    private int scale = DEFAULT_SCALE;

    /** 行情数据最大取值范围。*/
    private static final int MAX_RANGE = 1000;

    // --- 暴露给用户的数据 ---

    /** 计算出的 DIF 数组，该结构采用由近到远方式来排序数据，即 diffs[0] 为近期的均值数据。*/
    private Double[] difs = {};

    /** 计算出的 DIFMA 数组，该结构采用由近到远方式来排序数据，即 deas[0] 为近期的均值数据。*/
    private Double[] difMas = {};

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param fast 快线
     * @param slow 慢线
     * @param step 步长
     * @param num 取值个数
     */
    public IDma(IStatistics statistics, Period period, int fast, int slow, int step, int num) {
        this(statistics, period, IStatisticsDataType.CLOSING_PRICE, fast, slow, step, num);
    }

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param dataType IStatisticsDataType
     * @param fast 快线
     * @param slow 慢线
     * @param step 步长
     * @param num 取值个数
     */
    public IDma(IStatistics statistics, Period period, IStatisticsDataType dataType, int fast, int slow, int step, int num) {
        this.statistics = statistics;
        this.period = period;
        this.dataType = dataType;
        this.fast = fast;
        this.slow = slow;
        this.step = step;
        this.num = num;

        init();
    }

    // --- 不对外公开的构造函数 ---
    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param fast 快线
     * @param slow 慢线
     * @param step 步长
     * @param num 取值个数
     */
    public IDma(Double[] vs, int fast, int slow, int step, int num) {
        this(vs, fast, slow, step, num, DEFAULT_SCALE);
    }

    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param fast 快线
     * @param slow 慢线
     * @param step 步长
     * @param num 取值个数
     * @param scale 精度
     */
    public IDma(Double[] vs, int fast, int slow, int step, int num, int scale) {
        this.statistics = null;
        this.period = null;
        this.dataType = null;
        this.fast = fast;
        this.slow = slow;
        this.step = step;
        this.num = num;

        this.scale = scale;

        init(vs);
    }

    /**
     * 黄金交叉（dif 上穿 difMa）。
     * @param underZero boolean 是否要求 dif 在 0 轴下方穿越 difMa
     * @param lowerLimit 下限，即——要求 dif 和 difMa 必须在该数值之下
     * @return boolean boolean
     */
    public boolean goldCross(boolean underZero, double lowerLimit) {
        if (ArrayUtils.isEmpty(difs) || ArrayUtils.isEmpty(difMas)) { return false; }
        if (getDif() >= lowerLimit || getDifMa() >= lowerLimit) { return false; }
        return goldCross(underZero);
    }

    /**
     * 黄金交叉（dif 上穿 difMa）。
     * @param underZero boolean 是否要求 dif 在 0 轴下方穿越 difMa
     * @return boolean
     */
    public boolean goldCross(boolean underZero) {
        if ((difs != null && difs.length >= 2) ||
                (difMas != null && difMas.length >= 2)) {

            if (underZero && getDif() > 0) { return false; }

            if ((difs[1].doubleValue() <= difMas[1].doubleValue()) &&
                    (getDif().doubleValue() > getDifMa().doubleValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 死亡交叉（dif 下穿 difMa）。
     * @param topZero boolean 是否要求 dif 在 0 轴上方穿越 difMa
     * @param upperLimit 上限，即——要求 dif 和 difMa 必须在该数值之上
     * @return boolean
     */
    public boolean deathCross(boolean topZero, double upperLimit) {
        if (ArrayUtils.isEmpty(difs) || ArrayUtils.isEmpty(difMas)) { return false; }
        if (getDif() <= upperLimit || getDifMa() <= upperLimit) { return false; }
        return deathCross(topZero);
    }

    /**
     * 死亡交叉（dif 下穿 difMa）。
     * @param topZero boolean 是否要求 dif 在 0 轴上方穿越 difMa
     * @return boolean
     */
    public boolean deathCross(boolean topZero) {
        if ((difs != null && difs.length >= 2) ||
                (difMas != null && difMas.length >= 2)) {

            if (topZero && getDif() < 0) { return false; }

            if ((difs[1].doubleValue() >= difMas[1].doubleValue()) &&
                    (getDif().doubleValue() < getDifMa().doubleValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 得到最近的一个 dif。
     * @return Double
     */
    public Double getDif() {
        return (!ArrayUtils.isEmpty(difs)) ? difs[0] : null;
    }

    /**
     * 得到计算后的 dif 数组，按照由近到远排序。
     * @return Double[]
     */
    public Double[] getDifs() {
        return difs;
    }

    /**
     * 得到最近的一个 difMa。
     * @return Double
     */
    public Double getDifMa() {
        return (!ArrayUtils.isEmpty(difMas)) ? difMas[0] : null;
    }

    /**
     * 得到计算后的 difMa 数组，按照由近到远排序。
     * @return Double[]
     */
    public Double[] getDifMas() {
        return difMas;
    }

    // --- private method ---

    /**
     * 构造器参数有效性验证。
     *
     * @return boolean
     */
    private boolean consturctorParamValidate() {
        return (fast < 2 || slow < 2 || fast >= slow || step < 2 || num < 1) ? false : true;
    }

    /**
     * 在构造对象后，即把均值计算出来。
     */
    private void init() {
        if (!consturctorParamValidate()) { return; }
        Double[] hvs = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, dataType, MAX_RANGE);
        Double[][] temp = compute(hvs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 2) { difs = temp[0]; difMas = temp[1]; }
    }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验” 和 “计算均值”。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     */
    private void init(final Double[] vs) {
        if (!consturctorParamValidate()) { return; }
        Double[][] temp = compute(vs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 2) { difs = temp[0]; difMas = temp[1]; }
    }

    /**
     * 根据数据序列计算均值，返回的均值数列 “由近到远” 排列，同时对小数精度进行了处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @return Double[][] [0][0]：dif；[1][0]：difMa
     */
    private Double[][] compute(final Double[] vs) {

        if (ArrayUtils.isEmpty(vs)) { return null; }

        /**
         * 股票软件中的公式：
         *
         * DIF:MA(CLOSE,N1)-MA(CLOSE,N2);
         * DIFMA:MA(DIF,M);
         */
        // --- 计算 DIF ---
        Double[] fastMas = QuoteFunction.ma(vs, fast);    if (ArrayUtils.isEmpty(fastMas)) { return null; }
        Double[] slowMas = QuoteFunction.ma(vs, slow);    if (ArrayUtils.isEmpty(slowMas)) { return null; }

        Double[] tempDifs = new Double[slowMas.length];
        for (int fastIndex = (fastMas.length - slowMas.length), slowIndex = 0; slowIndex < slowMas.length; slowIndex++) {
            tempDifs[slowIndex] = fastMas[fastIndex + slowIndex] - slowMas[slowIndex];
        }
        if (ArrayUtils.isEmpty(tempDifs)) { return null; }

        // --- 计算 DIFMA ---
        Double[] tempDifMas = QuoteFunction.ma(tempDifs, step);

        // --- 对计算结果进行整理（由近到远排序，且控制返回数量） ---
        Double[][] temp = new Double[2][1];

        temp[0] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempDifs, 0, (tempDifs.length > num ? num : tempDifs.length), scale);

        temp[1] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempDifMas, 0, (tempDifMas.length > num ? num : tempDifMas.length), scale);

        return temp;
    }

}
