package com.jd.quant.api.indicators.technoligy.obos;

import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.Period;

/**
 * 随机指标。
 *
 * Created by hubin3 on 2016/8/18.
 */
public class IKdj {

    /** 行情镜像。*/
    private final IStatistics statistics;
    /** 行情周期。*/
    private final Period period;

    /** “极值（最高价 Or 最低价）” 步长。*/
    private final int step;
    /** k 步长。*/
    private final int kStep;
    /** d 步长。*/
    private final int dStep;

    /** 取值个数（该参数决定了 ks 和 ds 的取值个数）。*/
    private final int num;

    /** 精度（由于平台的的默认精度是 4，因此该精度仅供 “不对外公开的构造器” 使用。目的是为了校验 “自开发结果” 与 “参照结果” 的正确性）。*/
    private static final int DEFAULT_SCALE = 4;
    private int scale = DEFAULT_SCALE;

    /** 行情数据最大取值范围。*/
    private static final int MAX_RANGE = 1000;

    // --- 暴露给用户的数据 ---

    /** 计算出的 K 数组，该结构采用由近到远方式来排序数据，即 ks[0] 为近期的均值数据。*/
    private Double[] ks = {};
    /** 计算出的 D 数组，该结构采用由近到远方式来排序数据，即 ds[0] 为近期的均值数据。*/
    private Double[] ds = {};
    /** 计算出的 J 数组，该结构采用由近到远方式来排序数据，即 js[0] 为近期的均值数据。*/
    private Double[] js = {};

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param step “极值（最高价 Or 最低价）” 步长
     * @param kStep k 步长
     * @param dStep d 步长
     * @param num 取值个数
     */
    public IKdj(IStatistics statistics, Period period, int step, int kStep, int dStep, int num) {
        this.statistics = statistics;
        this.period = period;
        this.step = step;
        this.kStep = kStep;
        this.dStep = dStep;
        this.num = num;

        init();
    }

    // --- 不对外公开的构造函数 ---
    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param hs 最高价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param ls 最低价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step “极值（最高价 Or 最低价）” 步长
     * @param kStep k 步长
     * @param dStep d 步长
     * @param num 取值个数
     */
    public IKdj(Double[] hs, Double[] ls, Double[] cs, int step, int kStep, int dStep, int num) {
        this(hs, ls, cs, step, kStep, dStep, num, DEFAULT_SCALE);
    }

    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param hs 最高价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param ls 最低价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step “极值（最高价 Or 最低价）” 步长
     * @param kStep k 步长
     * @param dStep d 步长
     * @param num 取值个数
     * @param scale 精度
     */
    public IKdj(Double[] hs, Double[] ls, Double[] cs, int step, int kStep, int dStep, int num, int scale) {
        this.statistics = null;
        this.period = null;
        this.step = step;
        this.kStep = kStep;
        this.dStep = dStep;
        this.num = num;

        this.scale = scale;

        init(hs, ls, cs);
    }

    /**
     * 得到 K 数组最近的一个值。
     * @return Double
     */
    public Double getK() { return (!ArrayUtils.isEmpty(ks)) ? ks[0] : null; }

    /**
     * 得到 K 数组。
     * @return Double[]
     */
    public Double[] getKs() {
        return ks;
    }

    /**
     * 得到 D 数组最近的一个值。
     * @return Double
     */
    public Double getD() { return (!ArrayUtils.isEmpty(ds)) ? ds[0] : null; }

    /**
     * 得到 D 数组。
     * @return Double[]
     */
    public Double[] getDs() {
        return ds;
    }

    /**
     * 得到 J 数组最近的一个值。
     * @return Double
     */
    public Double getJ() { return (!ArrayUtils.isEmpty(js)) ? js[0] : null; }

    /**
     * 得到 J 数组。
     * @return Double[]
     */
    public Double[] getJs() {
        return js;
    }

    // --- private method ---

    /**
     * 构造器参数有效性验证。
     *
     * @return boolean
     */
    private boolean consturctorParamValidate() {
        return (step < 0 || kStep < 0 || dStep < 0 || num < 1) ? false : true;

    }

    /**
     * 在构造对象后，即把均值计算出来。
     */
    private void init() {
        if (!consturctorParamValidate()) { return; }
        Double[] hs = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, IStatisticsDataType.HIGH_PRICE, MAX_RANGE);
        Double[] lv = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, IStatisticsDataType.LOW_PRICE, MAX_RANGE);
        Double[] cs = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, IStatisticsDataType.CLOSING_PRICE, MAX_RANGE);
        Double[][] temp = compute(hs, lv, cs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 3) { ks = temp[0]; ds = temp[1]; js = temp[2]; }
    }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验” 和 “计算均值”。
     *
     * @param hs 最高价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param ls 最低价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     */
    private void init(final Double[] hs, final Double[] ls, final Double[] cs) {
        if (!consturctorParamValidate()) { return; }
        Double[][] temp = compute(hs, ls, cs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 3) { ks = temp[0]; ds = temp[1]; js = temp[2]; }
    }

    /**
     * 根据数据序列计算均值，返回的均值数列 “由近到远” 排列，同时对小数精度进行了处理。
     *
     * @param hs 最高价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param ls 最低价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @return Double[][] [0][0]：ks；[1][0]：ds；[2][0]：js;
     */
    private Double[][] compute(final Double[] hs, final Double[] ls, final Double[] cs) {

        /**
         * 股票软件中的公式：
         * RSV:=(CLOSE-LLV(LOW,N))/(HHV(HIGH,N)-LLV(LOW,N))*100;
         * K:SMA(RSV,M1,1);
         * D:SMA(K,M2,1);
         * J:3*K-2*D;
         */
        Double[][] tempKD = IKd.computeKD(hs, ls, cs, step, kStep, dStep);
        if (tempKD == null || tempKD.length != 2) { return null; }

        Double[] tempKs = tempKD[0];
        Double[] tempDs = tempKD[1];
        Double[] tempJs = new Double[tempKs.length];

        // --- 计算 J ---
        for (int i = 0; i < tempKs.length; i++) {
            tempJs[i] = (3 * tempKs[i]) - (2 * tempDs[i]);
        }

        // --- 对计算结果进行整理（由近到远排序，且控制返回数量） ---

        Double[][] temp = new Double[3][1];

        temp[0] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempKs, 0, (tempKs.length > num ? num : tempKs.length), scale);

        temp[1] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempDs, 0, (tempDs.length > num ? num : tempDs.length), scale);

        temp[2] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempJs, 0, (tempJs.length > num ? num : tempJs.length), scale);

        return temp;

    }
}
