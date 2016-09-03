package com.jd.quant.api.indicators.technoligy.volume;

import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.function.QuoteFunction;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.Period;

import java.util.LinkedList;

/**
 * 平衡交易量指标。
 *
 * Created by hubin3 on 2016/8/18.
 */
public class IObv {

    /** 行情镜像。*/
    private final IStatistics statistics;
    /** 行情周期。*/
    private final Period period;
    /** 步长。*/
    private final int step;
    /** 取值个数。*/
    private final int num;

    /** 精度（由于平台的的默认精度是 4，因此该精度仅供 “不对外公开的构造器” 使用。目的是为了校验 “自开发结果” 与 “参照结果” 的正确性）。*/
    private static final int DEFAULT_SCALE = 4;
    private int scale = DEFAULT_SCALE;

    /** 行情数据最大取值范围。*/
    private static final int MAX_RANGE = 1000;
    // --- 暴露给用户的数据 ---

    /** 计算出的 OBV 数组，该结构采用由近到远方式来排序数据，即 avgs[0] 为近期的均值数据。*/
    private Double[] obvs = {};
    /** 计算出的 OBV 均值数组，该结构采用由近到远方式来排序数据，即 avgs[0] 为近期的均值数据。*/
    private Double[] obvAvgs = {};

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param step 步长
     * @param num 取值个数
     */
    public IObv(IStatistics statistics, Period period, int step, int num) {
        this.statistics = statistics;
        this.period = period;
        this.step = step;
        this.num = num;

        init();
    }

    // --- 不对外公开的构造函数 ---
    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param vs 成交量数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @param num 取值个数
     */
    public IObv(Double[] cs, Double[] vs, int step, int num) {
        this(cs, vs, step, num, DEFAULT_SCALE);
    }

    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param vs 成交量数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @param num 取值个数
     * @param scale 精度
     */
    public IObv(Double[] cs, Double[] vs, int step, int num, int scale) {
        this.statistics = null;
        this.period = null;
        this.step = step;
        this.num = num;

        this.scale = scale;

        init(cs, vs);
    }

    /**
     * 得到 OBV 数组中的第一个值。
     *
     * @return Double
     */
    public Double getObv() {
        return (!ArrayUtils.isEmpty(obvs)) ? obvs[0] : null;
    }

    /**
     * 得到 OBV 数组。
     *
     * @return Double
     */
    public Double[] getObvs() {
        return obvs;
    }

    /**
     * 得到 OBV 均值数组中的第一个值。
     *
     * @return Double
     */
    public Double getObvAvg() {
        return (!ArrayUtils.isEmpty(obvAvgs)) ? obvAvgs[0] : null;
    }

    /**
     * 得到 OBV 均值数组。
     *
     * @return Double
     */
    public Double[] getObvAvgs() {
        return obvAvgs;
    }

    // --- private method ---

    /**
     * 构造器参数有效性验证。
     *
     * @return boolean
     */
    private boolean consturctorParamValidate() {
        return (step < 2 || num < 1) ? false : true;
    }

    /**
     * 在构造对象后，即把均值计算出来。
     */
    private void init() {

        if (!consturctorParamValidate()) { return; }

        Double[] cs = TechnoligyTools.getIStatisticsHistoryDatas(                                          // 取得历史收盘价。
                statistics, period, IStatisticsDataType.CLOSING_PRICE, MAX_RANGE);
        Double[] vs = TechnoligyTools.getIStatisticsHistoryDatas(                                          // 取得历史成交量。
                statistics, period, IStatisticsDataType.TURNOVER_VOLUME, MAX_RANGE);

        Double[][] temp = compute(cs, vs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 2) { obvs = temp[0]; obvAvgs = temp[1]; }
    }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验” 和 “计算均值”。
     *
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param vs 成交量数组序列（该数列必须符合 “由远到近” 方式排序）
     */
    private void init(final Double[] cs, final Double[] vs) {
        if (!consturctorParamValidate()) { return; }
        Double[][] temp = compute(cs, vs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 2) { obvs = temp[0]; obvAvgs = temp[1]; }
    }

    /**
     * 根据数据序列计算均值，返回的均值数列 “由近到远” 排列，同时对小数精度进行了处理。
     *
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param vs 成交量数组序列（该数列必须符合 “由远到近” 方式排序）
     * @return Double[][] [0][0]：obvs；[1][0]：obvAvgs
     */
    private Double[][] compute(final Double[] cs, final Double[] vs) {

        if (cs == null || cs.length < step) { return null; }
        if (vs == null || vs.length != cs.length) { return null; }
        if (step <= 2 || num < 1) { return null; }

        LinkedList<Double> tempVols = new LinkedList<>();       // 按照 “由远到近” 的顺序装载 VOL。
        LinkedList<Double> tempObvs = new LinkedList<>();       // 按照 “由远到近” 的顺序装载 OBV。
        LinkedList<Double> tempObvAvgs = new LinkedList<>();    // 按照 “由近到远” 的顺序装载 OBV 的均值。

        double totalObv = 0D;
        for (int i = 0; i < cs.length; i++) {

            /**
             * 当处理下标为 0，即数列中的第一个值时，通达信的算法默认：
             * 1、VOL（成交量） 取 “负”；
             * 2、OBV 为 0；
             * 3、OBV 的均值为 0；
             */
            if (i == 0) {
                tempVols.add(-vs[i]);
                tempObvs.add(0D);
                tempObvAvgs.addFirst(0D);

                continue;
            }

            Double t = cs[i];        // 取得 “当前值”。
            Double y = cs[i - 1];    // 取得 “前一值”。

            /**
             * 第 1 步：当 “前值 > 前一值” 时，当前VOL（成交量）取 “正”，否则取 “负”，并放入到 tempVols 集合中。
             */
            if (t.doubleValue() > y.doubleValue()) {
                tempVols.add(vs[i]);
            } else {
                tempVols.add(-vs[i]);
            }

            /**
             * 第 2 步：对 tempVols 集合中的值进行累加，并把结果放到 tempObvs 集合中。在该算法中，为了方便计算，
             *         使用变量 totalObv 记录累加结果。
             *
             * 累加规则：
             * 1、当 “前值 != 前一值” 时，“当前obv = totalObv + 当前 VOL”；
             * 2、当 “前值 == 前一值” 时，“当前 obv = totalObv + 0”，即 “当前需要计算的 OBV == 前一 OBV”；
             */
            if (t.doubleValue() != y.doubleValue()) {
                totalObv += tempVols.getLast();
                tempObvs.add(totalObv);
            } else {
                tempObvs.add(tempObvs.getLast());
            }

            /**
             * 第 3 步：使用 tempObvs 集合中的数据计算均值。
             */
            if (tempObvs.size() < step) {
                tempObvAvgs.addFirst(0D);
            } else {
                Double[] avgs = QuoteFunction.ma(tempObvs.subList(tempObvs.size() - step, tempObvs.size()).toArray(new Double[0]), 5);
                if (ArrayUtils.isEmpty(avgs)) {
                    tempObvAvgs.addFirst(0D);
                } else {
                    tempObvAvgs.addFirst(avgs[avgs.length - 1]);
                }
            }

        }

        // --- 对计算结果进行整理（由近到远排序，且控制返回数量） ---

        /**
         * 注意：由于计算过程的需要，tempObvs 是 “由远到近” 排列的，因此需要先把其转换成，“由近到远” 排列。
         */
        Double[][] temp = new Double[2][1];
        temp[0] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempObvs.toArray(new Double[0]), 0, (tempObvs.size() > num ? num : tempObvs.size()), scale);

        temp[1] = TechnoligyTools.subAndScaleDoubles(
                tempObvAvgs.toArray(new Double[0]), 0, (tempObvAvgs.size() > num ? num : tempObvAvgs.size()), scale);

        return temp;
    }
}
