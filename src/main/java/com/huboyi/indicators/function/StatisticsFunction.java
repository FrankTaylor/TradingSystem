package com.jd.quant.api.indicators.function;

import com.jd.common.util.ArrayUtils;

import java.util.LinkedList;

/**
 * 统计函数。
 *
 * Created by hubin3 on 2016/8/21.
 */
public class StatisticsFunction {

    /**
     * 计算 “估值标准差（样本标准差）”。
     *
     * @param vs 数列
     * @param step 步长
     * @return Double[]
     */
    public static Double[] std(final Double[] vs, final int step) {
        return stdAndStdp(vs, step, StatisticsFunctionEnum.STD);
    }

    /**
     * 计算 “总体标准差”。
     *
     * @param vs 数列
     * @param step 步长
     * @return Double[]
     */
    public static Double[] stdp(final Double[] vs, final int step) {
        return stdAndStdp(vs, step, StatisticsFunctionEnum.STDP);
    }

    /**
     * 计算 “估值标准差 std（样本标准差）” 和 “总体标准差 stdp”（该方法仅供 std 和 stdp 使用，不对外公开）。
     *
     * @param vs 数列
     * @param step 步长
     * @param type StatisticsFunctionEnum
     * @return Double[]
     */
    private static Double[] stdAndStdp(final Double[] vs, final int step, final StatisticsFunctionEnum type) {

        if (ArrayUtils.isEmpty(vs) || step <= 1) { return new Double[0]; }
        if (type != StatisticsFunctionEnum.STD && type != StatisticsFunctionEnum.STDP) { return new Double[0]; }

        LinkedList<Double> tempList = new LinkedList<>();
        Double[] mas = QuoteFunction.ma(vs, step);
        for (int outer = 0; outer < mas.length; outer++) {
            double tempMa = mas[outer];

            // --- 计算 “当前周期中，每一数值 与 当前均值之差的平方和” ---
            double subPowTotal = 0D;
            int inner = outer + (step - 1);
            int innerLength = inner - (step - 1);
            while (inner >= innerLength) {
                subPowTotal += Math.pow((vs[inner--] - tempMa), 2);
            }

            // --- 计算 “差值平方和的均值” ---
            double subPowAvg = (type == StatisticsFunctionEnum.STD) ? subPowTotal / (step - 1) : subPowTotal / step;

            // --- 计算 “平方和均值的开方” ---
            double subPowAvgSqrt = Math.sqrt(subPowAvg);

            tempList.addLast(subPowAvgSqrt);
        }

        return tempList.toArray(new Double[0]);
    }

    /**
     * 仅供 StatisticsFunction 内部使用的枚举类，不对外公开。
     */
    private enum StatisticsFunctionEnum {
        STD, STDP
    }

}
