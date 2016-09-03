package com.jd.quant.api.indicators.technoligy;

/**
 * 用于定出 K 线数据的取值类型。
 *
 * Created by hubin3 on 2016/8/3.
 */
public enum IStatisticsDataType {
    /** 最高价格。*/
    HIGH_PRICE,
    /** 最低价格。*/
    LOW_PRICE,
    /** 开盘价格。*/
    OPENING_PRICE,
    /** 收盘价格。*/
    CLOSING_PRICE,
    /** 成交金额。*/
    TURNOVER,
    /** 总交易的股数。*/
    TURNOVER_VOLUME
}
