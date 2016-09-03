package com.jd.quant.api.indicators.technoligy.trend;


import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.function.QuoteFunction;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.*;

import java.util.*;

/**
 * Created by hubin3 on 2016/7/26.
 */
public class IMacd {

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

    /** 计算出的 DIFF 数组，该结构采用由近到远方式来排序数据，即 diffs[0] 为近期的均值数据。*/
    private Double[] diffs = {};

    /** 计算出的 DEA 数组，该结构采用由近到远方式来排序数据，即 deas[0] 为近期的均值数据。*/
    private Double[] deas = {};

    /** 计算出的 MACD 数组，该结构采用由近到远方式来排序数据，即 macds[0] 为近期的均值数据。*/
    private Double[] macds = {};

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
    public IMacd(IStatistics statistics, Period period, int fast, int slow, int step, int num) {
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
    public IMacd(IStatistics statistics, Period period, IStatisticsDataType dataType, int fast, int slow, int step, int num) {
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
    public IMacd(Double[] vs, int fast, int slow, int step, int num) {
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
    public IMacd(Double[] vs, int fast, int slow, int step, int num, int scale) {
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
     * 黄金交叉（diff 上穿 dea）。
     * @param underZero boolean 是否要求 diff 在 0 轴下方穿越 dea
     * @param lowerLimit 下限，即——要求 diff 和 dea 必须在该数值之下
     * @return boolean boolean
     */
    public boolean goldCross(boolean underZero, double lowerLimit) {
        if (ArrayUtils.isEmpty(diffs) || ArrayUtils.isEmpty(deas)) { return false; }
        if (getDiff() >= lowerLimit || getDea() >= lowerLimit) { return false; }
        return goldCross(underZero);
    }

    /**
     * 黄金交叉（diff 上穿 dea）。
     * @param underZero boolean 是否要求 diff 在 0 轴下方穿越 dea
     * @return boolean
     */
    public boolean goldCross(boolean underZero) {
        if ((diffs != null && diffs.length >= 2) ||
                (deas != null && deas.length >= 2)) {

            if (underZero && getDiff() > 0) { return false; }

            if ((diffs[1].doubleValue() <= deas[1].doubleValue()) &&
                    (getDiff().doubleValue() > getDea().doubleValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 死亡交叉（diff 下穿 dea）。
     * @param topZero boolean 是否要求 diff 在 0 轴上方穿越 dea
     * @param upperLimit 上限，即——要求 diff 和 dea 必须在该数值之上
     * @return boolean
     */
    public boolean deathCross(boolean topZero, double upperLimit) {
        if (ArrayUtils.isEmpty(diffs) || ArrayUtils.isEmpty(deas)) { return false; }
        if (getDiff() <= upperLimit || getDea() <= upperLimit) { return false; }
        return deathCross(topZero);
    }

    /**
     * 死亡交叉（diff 下穿 dea）。
     * @param topZero boolean 是否要求 diff 在 0 轴上方穿越 dea
     * @return boolean
     */
    public boolean deathCross(boolean topZero) {
        if ((diffs != null && diffs.length >= 2) ||
                (deas != null && deas.length >= 2)) {

            if (topZero && getDiff() < 0) { return false; }

            if ((diffs[1].doubleValue() >= deas[1].doubleValue()) &&
                    (getDiff().doubleValue() < getDea().doubleValue())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 得到最近的一个 diff。
     * @return Double
     */
    public Double getDiff() {
        return (!ArrayUtils.isEmpty(diffs)) ? diffs[0] : null;
    }

    /**
     * 得到计算后的 diff 数组，按照由近到远排序。
     * @return Double[]
     */
    public Double[] getDiffs() {
        return diffs;
    }

    /**
     * 得到最近的一个 dea。
     * @return Double
     */
    public Double getDea() {
        return (!ArrayUtils.isEmpty(deas)) ? deas[0] : null;
    }

    /**
     * 得到计算后的 dea 数组，按照由近到远排序。
     * @return Double[]
     */
    public Double[] getDeas() {
        return deas;
    }

    /**
     * 得到最近的一个 macd。
     * @return Double
     */
    public Double getMacd() {
        return (!ArrayUtils.isEmpty(macds)) ? macds[0] : null;
    }

    /**
     * 得到计算后的 macd 数组，按照由近到远排序。
     * @return Double[]
     */
    public Double[] getMacds() {
        return macds;
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
        if (!ArrayUtils.isEmpty(temp) && temp.length == 3) { diffs = temp[0]; deas = temp[1]; macds = temp[2]; }
    }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验” 和 “计算均值”。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     */
    private void init(final Double[] vs) {
        if (!consturctorParamValidate()) { return; }
        Double[][] temp = compute(vs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 3) { diffs = temp[0]; deas = temp[1]; macds = temp[2]; }
    }

    /**
     * 根据数据序列计算均值，返回的均值数列 “由近到远” 排列，同时对小数精度进行了处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @return Double[][] [0][0]：diff；[1][0]：dea；[2][0]：macd
     */
    private Double[][] compute(final Double[] vs) {

        if (ArrayUtils.isEmpty(vs)) { return null; }

        /**
         * 股票软件中的公式：
         *
         * DIF:EMA(CLOSE,SHORT)-EMA(CLOSE,LONG);
         * DEA:EMA(DIF,MID);
         * MACD:(DIF-DEA)*2,COLORSTICK;
         */
        // --- 计算 快慢线 ---
        Double[] fAvgs = QuoteFunction.ema(vs, fast);
        if (ArrayUtils.isEmpty(fAvgs)) { return null; }

        Double[] sAvgs = QuoteFunction.ema(vs, slow);
        if (ArrayUtils.isEmpty(sAvgs)) { return null; }

        // --- 计算 DIFFS ---
        LinkedList<Double> tempDiffList = new LinkedList<>();
        for (int i = 0; i < sAvgs.length; tempDiffList.addLast(fAvgs[i] - sAvgs[i]), i++) ;
        if (tempDiffList.size() < step) { return null; }

        // --- 计算 DEAS ---
        Double[] tempDeas = QuoteFunction.ema(tempDiffList.toArray(new Double[0]), step);
        if (ArrayUtils.isEmpty(tempDeas)) { return null; }

        // --- 计算 MACDS ---
        LinkedList<Double> tempMacds = new LinkedList<>();
        for (int i = 0; i < tempDeas.length; i++) {
            tempMacds.addLast( (tempDiffList.get(i) - tempDeas[i]) * 2);
        }

        // --- 对计算结果进行整理（由近到远排序，且控制返回数量） ---

        Double[][] temp = new Double[3][1];

        temp[0] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempDiffList.toArray(new Double[0]), 0, (tempDiffList.size() > num ? num : tempDiffList.size()), scale);

        temp[1] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempDeas, 0, (tempDeas.length > num ? num : tempDeas.length), scale);

        temp[2] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempMacds.toArray(new Double[0]), 0, (tempMacds.size() > num ? num : tempMacds.size()), scale);

        return temp;
    }
}