package com.jd.quant.api.indicators.technoligy;


import com.jd.quant.api.statistics.IStatistics;
import com.jd.quant.api.statistics.IStatisticsHistory;
import com.jd.quant.api.statistics.Period;
import org.apache.commons.lang.ArrayUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * 技术分析指标的工具类
 *
 * Created by hubin3 on 2016/8/3.
 */
public class TechnoligyTools {

    /**
     * 根据 “周期” 和 “数据类型” 得到相应的行情历史数据。
     *
     * @param statistics IStatistics
     * @param period Period
     * @param dataType IStatisticsDataType
     * @param range 取值范围
     * @return Double[]
     */
    public static Double[] getIStatisticsHistoryDatas(final IStatistics statistics, Period period, final IStatisticsDataType dataType, final int range) {

        if (statistics == null || period == null || dataType == null || range <= 0) { return new Double[0]; }

        IStatisticsHistory history = statistics.history(range, period);                          // 历史行情镜像。
        Double[] hvs = (dataType == IStatisticsDataType.HIGH_PRICE) ?                            // 历史数据集合。
                history.getHighPrice() : (dataType == IStatisticsDataType.LOW_PRICE) ?
                history.getLowPrice() : (dataType == IStatisticsDataType.OPENING_PRICE) ?
                history.getOpeningPrice() : (dataType == IStatisticsDataType.CLOSING_PRICE) ?
                history.getClosingPrice() : (dataType == IStatisticsDataType.TURNOVER) ?
                history.getTurnover() : (dataType == IStatisticsDataType.TURNOVER_VOLUME) ?
                history.getTurnoverVolume() : null;
        return hvs;
    }

    /**
     * 返回逆序的 Double[]。
     *
     * @param src 原始 Double[]
     * @return 逆序后 Double[]
     */
    public static Double[] reverseDoubles(final Double[] src) {
        if (ArrayUtils.isEmpty(src)) { return new Double[0]; }

        Double[] temp = new Double[src.length];
        for (int i = 0; i < src.length; temp[i] = src[src.length - 1 - i], i++) ;

        return temp;
    }

    /**
     * 按照范围截取 Double[] 中的数据。
     *
     * @param src 原始 Double[]
     * @param start 截取起始位置
     * @param end 截取结束位置
     * @return Double[]
     */
    public static Double[] subDoubles(final Double[] src, final int start, final int end) {

        if (ArrayUtils.isEmpty(src)) { return new Double[0]; }
        final int num = end - start;

        Double[] desc = new Double[src.length > num ? num : src.length];
        System.arraycopy(src, start, desc, 0, desc.length);

        return desc;
    }

    /**
     * 控制 Double[] 中数据的精度。
     *
     * @param src 原始 Double[]
     * @param newScale 小数精度
     * @return Double[]
     */
    public static Double[] scaleDoubles(final Double[] src, int newScale) {

        if (ArrayUtils.isEmpty(src)) { return new Double[0]; }
        final int scale = (newScale < 0) ? 4 : newScale;
        Double[] desc = new Double[src.length];
        for (int i = 0; i < desc.length; i++) {
            desc[i] = BigDecimal.valueOf(src[i])
                    .setScale(scale, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return desc;
    }

    // --- reverseDoubles、subDoubles 和 scaleDoubles 方法的组合 ---

    /**
     * 逆序 Double[] 中的数据，并进行小数精度控制。
     *
     * @param src 原始 Double[]
     * @param newScale 小数精度
     * @return Double[]
     */
    public static Double[] reverseAndScaleDoubles(final Double[] src, final int newScale) {
        return scaleDoubles(reverseDoubles(src), newScale);
    }

    /**
     * 按照范围截取 Double[] 中的数据，并进行小数精度控制。
     *
     * @param src 原始 Double[]
     * @param start 截取起始位置
     * @param end 截取结束位置
     * @param newScale 小数精度
     * @return Double[]
     */
    public static Double[] subAndScaleDoubles(final Double[] src, final int start, final int end, final int newScale) {
        return scaleDoubles(subDoubles(src, start, end), newScale);
    }

    /**
     * 逆序、按范围截取 Double[] 中的数据，并进行小数精度控制。
     *
     * @param src 原始 Double[]
     * @param start 截取起始位置
     * @param end 截取结束位置
     * @param newScale 小数精度
     * @return Double[]
     */
    public static Double[] reverseAndSubAndScaleDoubles(final Double[] src, final int start, final int end, final int newScale) {
        return subAndScaleDoubles(reverseDoubles(src), start, end, newScale);
    }

}
