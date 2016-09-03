package com.jd.quant.api.indicators.technoligy.averageLine;

/**
 * Created by hubin3 on 2016/8/3.
 */
public interface Ma {

    /**
     * 该均线是否上穿另一条均线。
     * @param o ISma
     * @return boolean
     */
    boolean crossUp(Ma o) ;

    /**
     * 该均线是否下穿另一条均线。
     * @param o ISma
     * @return boolean
     */
    boolean crossDown(Ma o) ;

    /**
     * 得到最近的一个均值。
     * @return Double
     */
    Double getAvg() ;

    /**
     * 得到计算后的均值数组，按照由近到远排序。
     * @return Double[]
     */
    Double[] getAvgs();
}
