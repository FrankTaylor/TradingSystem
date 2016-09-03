package com.jd.quant.api.indicators.technoligy.obos;


import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.function.QuoteFunction;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.Period;

import java.util.LinkedList;

/**
 * Created by hubin3 on 2016/7/28.
 */
public class IRsi {

    /** 行情镜像。*/
    private final IStatistics statistics;
    /** 行情周期。*/
    private final Period period;
    /** 历史数据取值类型。*/
    private final IStatisticsDataType dataType;

    /** 短期 RSI 步长。*/
    private final int shortStep;
    /** 中期 RSI 步长。*/
    private final int midStep;
    /** 长期 RSI 步长。*/
    private final int longStep;

    /** 取值个数（该参数决定了 shortRsis、midRsis 和 longRsis 的取值个数）。*/
    private final int num;

    /** 精度（由于平台的的默认精度是 4，因此该精度仅供 “不对外公开的构造器” 使用。目的是为了校验 “自开发结果” 与 “参照结果” 的正确性）。*/
    private static final int DEFAULT_SCALE = 4;
    private int scale = DEFAULT_SCALE;

    /** 行情数据最大取值范围。*/
    private static final int MAX_RANGE = 1000;

    // --- 暴露给用户的数据 ---

    /** 计算出的 短期 RIS 数组，该结构采用由近到远方式来排序数据，即 shortRsis[0] 为近期的均值数据。*/
    private Double[] shortRsis = {};
    /** 计算出的 中期 RIS 数组，该结构采用由近到远方式来排序数据，即 midRsis[0] 为近期的均值数据。*/
    private Double[] midRsis = {};
    /** 计算出的 长期 RIS 数组，该结构采用由近到远方式来排序数据，即 longRsis[0] 为近期的均值数据。*/
    private Double[] longRsis = {};

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param shortStep 短期 RSI 步长
     * @param midStep 中期 RSI 步长
     * @param longStep 长期 RSI 步长
     * @param num 取值个数
     */
    public IRsi(IStatistics statistics, Period period, int shortStep, int midStep, int longStep, int num) {
        this(statistics, period, IStatisticsDataType.CLOSING_PRICE, shortStep, midStep, longStep, num);
    }

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param dataType IStatisticsDataType
     * @param shortStep 短期 RSI 步长
     * @param midStep 中期 RSI 步长
     * @param longStep 长期 RSI 步长
     * @param num 取值个数
     */
    public IRsi(IStatistics statistics, Period period, IStatisticsDataType dataType, int shortStep, int midStep, int longStep, int num) {
        this.statistics = statistics;
        this.period = period;
        this.dataType = dataType;
        this.shortStep = shortStep;
        this.midStep = midStep;
        this.longStep = longStep;
        this.num = num;

        init();
    }

    // --- 不对外公开的构造函数 ---
    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param shortStep 短期 RSI 步长
     * @param midStep 中期 RSI 步长
     * @param longStep 长期 RSI 步长
     * @param num 取值个数
     */
    public IRsi(Double[] vs, int shortStep, int midStep, int longStep, int num) {
        this(vs, shortStep, midStep, longStep, num, DEFAULT_SCALE);
    }

    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param shortStep 短期 RSI 步长
     * @param midStep 中期 RSI 步长
     * @param longStep 长期 RSI 步长
     * @param num 取值个数
     * @param scale 精度
     */
    public IRsi(Double[] vs, int shortStep, int midStep, int longStep, int num, int scale) {
        this.statistics = null;
        this.period = null;
        this.dataType = null;
        this.shortStep = shortStep;
        this.midStep = midStep;
        this.longStep = longStep;
        this.num = num;

        this.scale = scale;

        init(vs);
    }

    /**
     * 得到短期 RSI最近的一个值。
     * @return Double
     */
    public Double getShortRsi() { return (!ArrayUtils.isEmpty(shortRsis)) ? shortRsis[0] : null; }

    /**
     * 得到短期 RSI 数组。
     * @return Double[]
     */
    public Double[] getShortRsis() {
        return shortRsis;
    }

    /**
     * 得到中期 RSI最近的一个值。
     * @return Double
     */
    public Double getMidRsi() {
        return (!ArrayUtils.isEmpty(midRsis)) ? midRsis[0] : null;
    }

    /**
     * 得到中期 RSI 数组。
     * @return Double[]
     */
    public Double[] getMidRsis() {
        return midRsis;
    }

    /**
     * 得到长期 RSI最近的一个值。
     * @return Double
     */
    public Double getLongRsi() {
        return (!ArrayUtils.isEmpty(longRsis)) ? longRsis[0] : null;
    }

    /**
     * 得到长期 RSI 数组。
     * @return Double[]
     */
    public Double[] getLongRsis() {
        return longRsis;
    }

    // --- private method ---

    /**
     * 构造器参数有效性验证。
     *
     * @return boolean
     */
    private boolean consturctorParamValidate() {
        return (shortStep < 2 || midStep < shortStep || longStep < midStep || num < 1) ? false : true;

    }

    /**
     * 在构造对象后，即把均值计算出来。
     */
    private void init() {
        if (!consturctorParamValidate()) { return; }
        Double[] hvs = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, dataType, MAX_RANGE);
        Double[][] temp = compute(hvs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 3) { shortRsis = temp[0]; midRsis = temp[1]; longRsis = temp[2]; }
    }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验” 和 “计算均值”。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     */
    private void init(final Double[] vs) {
        if (!consturctorParamValidate()) { return; }
        Double[][] temp = compute(vs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 3) { shortRsis = temp[0]; midRsis = temp[1]; longRsis = temp[2]; }
    }

    /**
     * 根据数据序列计算均值，返回的均值数列 “由近到远” 排列，同时对小数精度进行了处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @return Double[][] [0][0]：shortRsis；[1][0]：midRsis；[2][0]：longRsis
     */
    private Double[][] compute(final Double[] vs) {

        if (ArrayUtils.isEmpty(vs)) { return null; }

        /**
         * 股票软件中的公式：
         * LC:=REF(CLOSE,1);
         * RSI1:SMA(MAX(CLOSE-LC,0),N1,1)/SMA(ABS(CLOSE-LC),N1,1)*100;
         * RSI2:SMA(MAX(CLOSE-LC,0),N2,1)/SMA(ABS(CLOSE-LC),N2,1)*100;
         * RSI3:SMA(MAX(CLOSE-LC,0),N3,1)/SMA(ABS(CLOSE-LC),N3,1)*100;
         */
        LinkedList<Double> shortRsiList = new LinkedList<>();
        LinkedList<Double> midRsiList = new LinkedList<>();
        LinkedList<Double> longRsiList = new LinkedList<>();

        LinkedList<Double> maxList = new LinkedList<>();
        LinkedList<Double> absList = new LinkedList<>();
        Double[] smaMaxs, smaAbss;
        Double prev, curr, sub;
        for (int i = 0; i < vs.length; i++) {

            // --- 累计 MAX 和 ABS 数组中的值 ---
            if (i == 0) {    // 根据研究，在处理数列中第一个值时，MAX 和 ABS 数列的第一个值都为 0。
                maxList.add(0D);
                absList.add(0D);
            } else {
                curr = vs[i];
                prev = vs[i - 1];
                sub = curr - prev;

                maxList.addLast(Math.max(sub, 0));
                absList.addLast(Math.abs(sub));
            }

            // --- 计算短期 RSI ---
            {
                smaMaxs = QuoteFunction.sma(maxList.toArray(new Double[0]), shortStep, 1);
                smaAbss = QuoteFunction.sma(absList.toArray(new Double[0]), shortStep, 1);

                shortRsiList.addLast(smaMaxs[smaMaxs.length - 1] / smaAbss[smaAbss.length - 1] * 100);
            }

            // --- 计算中期 RSI ---
            {
                smaMaxs = QuoteFunction.sma(maxList.toArray(new Double[0]), midStep, 1);
                smaAbss = QuoteFunction.sma(absList.toArray(new Double[0]), midStep, 1);

                midRsiList.addLast(smaMaxs[smaMaxs.length - 1] / smaAbss[smaAbss.length - 1] * 100);
            }

            // --- 计算长期 RSI ---
            {
                smaMaxs = QuoteFunction.sma(maxList.toArray(new Double[0]), longStep, 1);
                smaAbss = QuoteFunction.sma(absList.toArray(new Double[0]), longStep, 1);

                longRsiList.addLast(smaMaxs[smaMaxs.length - 1] / smaAbss[smaAbss.length - 1] * 100);
            }
        }

        // --- 对计算结果进行整理（由近到远排序，且控制返回数量） ---

        Double[][] temp = new Double[3][1];

        temp[0] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                shortRsiList.toArray(new Double[0]), 0, (shortRsiList.size() > num ? num : shortRsiList.size()), scale);

        temp[1] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                midRsiList.toArray(new Double[0]), 0, (midRsiList.size() > num ? num : midRsiList.size()), scale);

        temp[2] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                longRsiList.toArray(new Double[0]), 0, (longRsiList.size() > num ? num : longRsiList.size()), scale);

        return temp;

    }
}
