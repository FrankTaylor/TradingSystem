package com.jd.quant.api.indicators.technoligy.path;


import com.jd.common.util.ArrayUtils;
import com.jd.quant.api.indicators.function.QuoteFunction;
import com.jd.quant.api.indicators.function.StatisticsFunction;
import com.jd.quant.api.indicators.technoligy.IStatisticsDataType;
import com.jd.quant.api.indicators.technoligy.TechnoligyTools;
import com.jd.quant.api.statistics.*;

import java.util.*;

/**
 * Created by hubin3 on 2016/7/28.
 */
public class IBoll {

    /** 行情镜像。*/
    private final IStatistics statistics;
    /** 行情周期。*/
    private final Period period;
    /** 历史数据取值类型。*/
    private final IStatisticsDataType dataType;
    /** 步长。*/
    private final int step;
    /** 倍数。*/
    private final int multiple;

    /** 取值个数（该参数决定了 uTracks、mTracks 和 dTracks 的取值个数）。*/
    private final int num;

    /** 精度（由于平台的的默认精度是 4，因此该精度仅供 “不对外公开的构造器” 使用。目的是为了校验 “自开发结果” 与 “参照结果” 的正确性）。*/
    private static final int DEFAULT_SCALE = 4;
    private int scale = DEFAULT_SCALE;

    /** 行情数据最大取值范围。*/
    private static final int MAX_RANGE = 1000;
    // --- 暴露给用户的数据 ---

    /** 计算出的 上轨 数组，该结构采用由近到远方式来排序数据，即 uTracks[0] 为近期的均值数据。*/
    private Double[] uTracks = {};

    /** 计算出的 中轨 数组，该结构采用由近到远方式来排序数据，即 mTracks[0] 为近期的均值数据。*/
    private Double[] mTracks = {};

    /** 计算出的 下轨 数组，该结构采用由近到远方式来排序数据，即 dTracks[0] 为近期的均值数据。*/
    private Double[] dTracks = {};

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param step 步长
     * @param multiple 倍数
     * @param num 取值个数
     */
    public IBoll(IStatistics statistics, Period period, int step, int multiple, int num) {
        this(statistics, period, IStatisticsDataType.CLOSING_PRICE, step, multiple, num);
    }

    /**
     * 对外公开的构造函数。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param dataType IStatisticsDataType
     * @param step 步长
     * @param multiple 倍数
     * @param num 取值个数
     */
    public IBoll(IStatistics statistics, Period period, IStatisticsDataType dataType, int step, int multiple, int num) {
        this.statistics = statistics;
        this.period = period;
        this.dataType = dataType;
        this.step = step;
        this.multiple = multiple;
        this.num = num;

        init();
    }

    // --- 不对外公开的构造函数 ---
    /**
     * 该构造函数直接根据 “入参数列” 进行计算（目前该构造函数仅供 “测试” 时使用，不建议对外公开）。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @param multiple 倍数
     * @param num 取值个数
     */
    public IBoll(Double[] vs, int step, int multiple, int num) {
        this(vs, step, multiple, num, DEFAULT_SCALE);
    }

    /**
     * 对外公开的构造函数。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @param multiple 倍数
     * @param num 取值个数
     * @param scale 精度
     */
    public IBoll(Double[] vs, int step, int multiple, int num, int scale) {
        this.statistics = null;
        this.period = null;
        this.dataType = null;
        this.step = step;
        this.multiple = multiple;
        this.num = num;

        this.scale = scale;

        init(vs);
    }

    /**
     * 得到最近的一个上轨值。
     * @return Double
     */
    public Double getUTrack() {
        return (!ArrayUtils.isEmpty(uTracks)) ? uTracks[0] : null;
    }

    /**
     * 得到计算后的上轨数组，按照由近到远排序。
     * @return Double[]
     */
    public Double[] getUTracks() {
        return uTracks;
    }

    /**
     * 得到最近的一个中轨值。
     * @return Double
     */
    public Double getMTrack() {
        return (!ArrayUtils.isEmpty(mTracks)) ? mTracks[0] : null;
    }

    /**
     * 得到计算后的中轨数组，按照由近到远排序。
     * @return Double[]
     */
    public Double[] getMTracks() {
        return mTracks;
    }

    /**
     * 得到最近的一个下轨值。
     * @return Double
     */
    public Double getDTrack() {
        return (!ArrayUtils.isEmpty(dTracks)) ? dTracks[0] : null;
    }

    /**
     * 得到计算后的下轨数组，按照由近到远排序。
     * @return Double[]
     */
    public Double[] getDTracks() {
        return dTracks;
    }

    /**
     * 构造器参数有效性验证。
     *
     * @return boolean
     */
    private boolean consturctorParamValidate() {
        return (step < 2 || multiple <= 0 || num < 1) ? false : true;
    }

    /**
     * 在构造对象后，即把均值计算出来。
     */
    private void init() {

        if (!consturctorParamValidate()) { return; }

        Double[] hvs = TechnoligyTools.getIStatisticsHistoryDatas(statistics, period, dataType, MAX_RANGE);
        Double[][] temp = compute(hvs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 3) { uTracks = temp[0]; mTracks = temp[1]; dTracks = temp[2]; }
    }

    /**
     * 供构造函数调用的 “初始函数”，负责　“参数校验” 和 “计算均值”。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     */
    private void init(final Double[] vs) {
        if (!consturctorParamValidate()) { return; }
        Double[][] temp = compute(vs);
        if (!ArrayUtils.isEmpty(temp) && temp.length == 3) { uTracks = temp[0]; mTracks = temp[1]; dTracks = temp[2]; }
    }

    /**
     * 根据数据序列计算均值，返回的均值数列 “由近到远” 排列，同时对小数精度进行了处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @return Double[][] [0][0]：uTracks；[1][0]：mTracks；[2][0]：dTracks
     */
    private Double[][] compute(final Double[] vs) {

        if (vs == null || vs.length < step) { return null; }

        /**
         * 股票软件中的公式：
         *
         * BOLL:MA(CLOSE,N);
         * UPPER:BOLL+M*STD(CLOSE,N);
         * LOWER:BOLL-M*STD(CLOSE,N);
         */
        // --- 计算 “中轨” ---
        Double[] tempMTracks = QuoteFunction.ma(vs, step);
        if (ArrayUtils.isEmpty(tempMTracks)) { return null; }

        // --- 计算 “估值标准差” ---
        Double[] stds = StatisticsFunction.std(vs, step);
        if (ArrayUtils.isEmpty(stds)) { return null; }

        // --- 计算 “上轨和下轨” ---
        List<Double> tempUTrackList = new LinkedList<>();
        List<Double> tempDTrackList = new LinkedList<>();
        for (int i = 0; i < tempMTracks.length; i++) {
            tempUTrackList.add(tempMTracks[i] + multiple * stds[i]);
            tempDTrackList.add(tempMTracks[i] - multiple * stds[i]);
        }

        // --- 对计算结果进行整理（由近到远排序，且控制返回数量） ---

        Double[][] temp = new Double[3][1];

        temp[0] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempUTrackList.toArray(new Double[0]), 0, (tempUTrackList.size() > num ? num : tempUTrackList.size()), scale);

        temp[1] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempMTracks, 0, (tempMTracks.length > num ? num : tempMTracks.length), scale);

        temp[2] = TechnoligyTools.reverseAndSubAndScaleDoubles(
                tempDTrackList.toArray(new Double[0]), 0, (tempDTrackList.size() > num ? num : tempDTrackList.size()), scale);

        return temp;
    }
}
