package com.jd.quant.api.indicators.function;

import com.jd.common.util.ArrayUtils;

import java.util.LinkedList;

/**
 * 引用函数。
 *
 * Created by hubin3 on 2016/8/19.
 */
public class QuoteFunction {

    // --- llv、hhv ---

    /**
     * 得到步长内，数列中的 “最大值”。
     *
     * @param vs 数列
     * @param step 步长
     * @return Double[]
     */
    public static Double[] llv(final Double[] vs, final int step) {
        return llvAndHhvBase(vs, step, true);
    }

    /**
     * 得到步长内，数列中的 “最小值”。
     *
     * @param vs 数列
     * @param step 步长
     * @return Double[]
     */
    public static Double[] hhv(final Double[] vs, final int step) {
        return llvAndHhvBase(vs, step, false);
    }

    /**
     * 得到步长内，数列中的 “最大值” 或 “最小值”（该方法仅供 llv 和 hhv 使用，不对外公开）。
     * 注意：1、该方法不是等聚集 “步长” 个元素后才开始比较，而是 “边聚集，边比较”；
     *      2、当集合在上一循环中，已经聚集到 “步长” 个元素时，在 “添加下一元素”，“执行比较” 前，要先删除集合中的第一个元素。
     *
     * @param vs 数列
     * @param step 步长
     * @param isLlv true 找出最小值；false 找出最大值
     * @return Double[]
     */
    private static Double[] llvAndHhvBase(final Double[] vs, final int step, final boolean isLlv) {

        if (ArrayUtils.isEmpty(vs) || step <= 0) { return new Double[0]; }

        Double[] llvOrHhv = new Double[vs.length];
        if (step == 1) {
            System.arraycopy(vs, 0, llvOrHhv, 0, llvOrHhv.length);
            return llvOrHhv;
        }

        LinkedList<Double> tempList = new LinkedList<>();
        for (int i = 0; i < vs.length; i++) {

            if (tempList.size() == step) {
                tempList.removeFirst();
            }
            tempList.addLast(vs[i]);

            /**
             * 在 JDK 1.6 中，可用下面的语句。
             *
             * llvOrHhv[i] = (isLlv) ? Collections.min(tempList) : Collections.max(tempList);
             */
            llvOrHhv[i] = (isLlv) ?
                    tempList.stream().mapToDouble(x -> x).summaryStatistics().getMin() :
                    tempList.stream().mapToDouble(x -> x).summaryStatistics().getMax();
        }

        return llvOrHhv;
    }

    // --- rsv ---

    /**
     * 计算 “RSV”。该函数在 “通达信的引用函数” 中并不存在，而是我为了计算方便而自行加入的。</p>
     * RSV 用于描述 “当日收盘价距最低价的涨跌幅” 占 “此期间最大涨跌幅” 的 “占比”，由于该公式反复在 “KD”、“KDJ”
     * 和其他公式中反复出现，因此单独提出，放到引入函数中。
     *
     * @param hs 最高价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param ls 最低价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param cs 收盘价数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] rsv(final Double[] hs, final Double[] ls, final Double[] cs, final int step) {

        if (step < 0) { return new Double[0]; }
        if (hs == null || hs.length < step) { return new Double[0]; }
        if (ls == null || ls.length < step) { return new Double[0]; }
        if (cs == null || cs.length < step) { return new Double[0]; }
        if (hs.length != ls.length || ls.length != cs.length) { return new Double[0]; }

        Double[] tempLlvs = QuoteFunction.llv(ls, step);
        Double[] tempHhvs = QuoteFunction.hhv(hs, step);
        if (ArrayUtils.isEmpty(tempLlvs) || ArrayUtils.isEmpty(tempHhvs)) { return null; }

        // --- 计算 RSV ---
        Double[] tempRsvs = new Double[cs.length];
        for (int i = 0; i < cs.length; i++) {
            tempRsvs[i] = ((cs[i] - tempLlvs[i]) / (tempHhvs[i] - tempLlvs[i])) * 100D;
        }

        return tempRsvs;
    }

    // --- ma、ema、sma、dma、wma ---

    /**
     * 计算 “移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合由远到近方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] ma(final Double[] vs, final int step) {

        if (vs == null || vs.length < step || step < 0) { return new Double[0]; }

        if (step == 1) {
            Double[] desc = new Double[vs.length];
            System.arraycopy(vs, 0, desc, 0, desc.length);
            return desc;
        }

        LinkedList<Double> maList = new LinkedList<>();

        double counter = 0D; int trailingValue = 0;
        for (int i = 0; i < vs.length; i++) {

            counter += vs[i];

            if ((i + 1) >= step) {
                maList.addLast(counter / step);

                counter -= vs[trailingValue++];
            }
        }

        return maList.toArray(new Double[0]);
    }

    /**
     * 计算 “加权移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     * 注意：该算法来源于 TALIB，与 “通达信” 等软件的算法不一样。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] emaOfTaLib(final Double[] vs, final int step) {

        if (vs == null || vs.length < step || step <= 2) { return new Double[0]; }

        /**
         * 若 Y = EMA(X, N)，则 Y = (2 * X + (N - 1) * Y') / (N - 1)；Y' 表示上一周期的 Y 值，该公式可化简为下面的形式：
         * 算法：（当日）指数平滑移动平均 = （昨日）指数平滑移动平均 + (（当日）指数 - （昨日）指数平滑移动平均) * 平滑系数
         *
         * 网上对这个指标的解释乱七八糟，例如：“该公式着重于，对较近的价格，赋予较大的权重”。但从计算的过程来看，该指标只不过
         * 对 “今日价格” 与 “前一指数平滑移动平均” 的波动，又进行了一次平滑（波动 * 2/步长 + 1），之后加上“前一指数平滑移动
         * 平均”，这么做只会使该指标（EMA）比 “普通移动平均线”（MA）更加平滑而已。
         *
         * 注意：目前该指标的实现，我是参考 TA-LIB 的，并未按照 Y = (2 * X + (N - 1) * Y') / (N - 1) 来实现，但，我在实
         * 现 SMA 指标的过程中，发现了，该公式是正确的。具体的心路历程，可参考 ISma 类中的注释。
         *
         * 注意：1、根据计算中 “系数” 的构造规则，要求 “步长” 的最小值应为 2。
         */

        LinkedList<Double> eamList = new LinkedList<>();

        // --- 计算 “初始均值” ---
        double tempReal = 0; int day;
        for (day = 0; day < step; tempReal += vs[day++]) ;
        eamList.addLast(tempReal / (double)step);

        // --- 计算 “其余 EMA” ---
        double coefficient = 2D / (step + 1D);
        while (day < vs.length) {
            Double prev = eamList.getLast();
            prev += (vs[day++] - prev) * coefficient;
            eamList.addLast(prev);
        }

        return eamList.toArray(new Double[0]);
    }

    /**
     * 计算 “加权移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     * 注意：该算法与 “通达信” 等软件中的 “EMA” 算法一致，与 “TALIB” 中的 “EMA” 算法不一致。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] ema(final Double[] vs, final int step) {

        if (vs == null || vs.length < 1 || step <= 0 ) { return new Double[0]; }

        if (step == 1) {
            Double[] desc = new Double[vs.length];
            System.arraycopy(vs, 0, desc, 0, desc.length);
            return desc;
        }

        /**
         * 注意：TALIB 中的 “ema 算法” 和 “通达信” 中的 “ema 算法” 不一样。这一点是我加班时发现的。
         *      在我破解 “通达信” 的 “ema 算法” 时，借鉴了 “emaOfTaLib 和 dma” 算法的思想。
         *
         * 通过计算过程可了解到，除了 “初始 EMA” 外，“下一 EMA” 都是根据 “前一 EMA” + “差值（当前值 - 前一 EMA）波动” 不断修正的。
         * “权重” 越大，“波动” 就越被稀释的厉害，得出的 EMA 就越平滑。注意：当 “权重为 1” 时，将不产生任何的平滑。
         */

        // --- 计算 “初始均值” ---
        LinkedList<Double> emaList = new LinkedList<>();
        emaList.addLast(vs[0]);                                           // 载入初始 Y' 值。

        // --- 计算 “其余 EMA” ---
        double coefficient = 2D / (step + 1D);
        for (int i = 1; i < vs.length; i++) {
            Double prev = emaList.getLast();
            prev += (vs[i] - prev) * coefficient;
            emaList.addLast(prev);
        }

        return  emaList.toArray(new Double[0]);
    }

    /**
     * 计算 “加权移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @param weight 权重
     * @return Double[]
     */
    public static Double[] smaOfTaLib(final Double[] vs, final int step, final int weight) {

        if (vs == null || vs.length < step || step <= 2 || weight <= 0 || weight >= step) { return new Double[0]; }

        /**
         * 注意：1、在 TALIB 中是没有关于这种 sma 算法的，TALIB 中的 sma 仅是 “简单移动平均线”。该 sma 的算法是我根据 TALIB
         *         中的 ema 算法改进而来的；
         *      2、该 sma 算法与 招商证券中的 sma 算法也不相同。
         *
         * 算法：若 Y = SMA(X, N, M) 则 Y = (M * X + (N - M) * Y') / N；Y'为上一周期 Y。
         *
         * 网上对这个指标的解释于 EMA 类似，同样是乱七八糟，例如：“SMA 与  EMA 的区别在于，SMA 中使用权重 M 代替 EMA 中的 2”，
         * 如果是这样的话，那 SMA(C, 5, 2) 就应该等于 EMA(C, 5)，为此，我在 “通达信” 和 “东方财富” 上都进行了验证，但两个公式
         * 输出的结果并不相同。
         *
         * 晚上，我在地铁等车时，不甘心于此，又拿出手机搜索 SMA 的实现算法，偶然找到了上面的公式，窃喜，回家编码测试，搞定。
         *
         * 最开始我的代码是这样的：
         *
         * #########################################################################################################
         * LinkedList<Double> tempAvgs = new LinkedList<>();            // 装载临时 SMA 由远到近排序。
         * tempAvgs.add(0D);                                            // 载入初始 Y' 值。
         *
         * // --- 计算 “其余 SMA” ---
         * for (int i = 0; i < vs.length; i++) {
         *     double d = ((double)weight * vs[i] + (double)(step - weight) * tempAvgs.getLast()) / (double)step;
         *     tempAvgs.add(d);
         * }
         * #########################################################################################################
         *
         * 该算法主要利用 SMA 公式进行计算，在实现上比较简单。但存在 “计算初期数据极不准确的情况”，在算法的中后期，经由对前值的不
         * 断修正，计算结果才逐步正确。
         *
         * 为此，我想到了 TA-LIB 中对 EMA 指标的实现，稍加改造后（主要是调整了 “系数”），发现计算结果也是正确的，这充分证实了我的
         * 猜想，公式：（当日）指数平滑移动平均 = （昨日）指数平滑移动平均 + (（当日）指数 - （昨日）指数平滑移动平均) * 平滑系数
         * 是 Y = (M * X + (N - M) * Y') / N 的化简形式。
         *
         * OK，说了这么多，下面简单谈谈 SMA 和 EMA 的具体区别：
         * 如果把 EMA 当做是 “（固定的）更加平滑的 MA” 来看待的话，那 SMA 则是 “（灵活的）更加平滑 或 更加剧烈的 EMA”，原因就在
         * 于其公式中的 “系数” 是可控的。特别是在 “今日价格” 比 “前几日价格范围” 大幅波动的情况下，当 “权重” 与 “步长” 较接近时，
         * 其 “波动（今日价格 - 前一指数平滑移动平均）” 越明显。
         *
         * 注意：1、虽然公式中没有明确要求 “权重” 必须小于 “步长”，但从公式的 “本意” 上看，以及从股票软件的测试效果上看，“权重”
         *         还应要小于 “步长” 的。
         *      2、当设置较小的 “权重” 和 较大的 “步长” 时，每个点的 “波动” 就越小。
         */
        LinkedList<Double> smaList = new LinkedList<>();

        // --- 计算 “初始均值” ---
        double tempReal = 0; int day;
        for (day = 0; day < step; tempReal += vs[day++]) ;
        smaList.addLast(tempReal / (double)step);

        // --- 计算 “其余 SMA” ---
        double coefficient = (double)weight / (double)(step);
        while (day < vs.length) {
            Double prev = smaList.getLast();
            prev += (vs[day++] - prev) * coefficient;
            smaList.addLast(prev);
        }

        return smaList.toArray(new Double[0]);
    }

    /**
     * 计算 “加权移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @param weight 权重
     * @return Double[]
     */
    public static Double[] sma(final Double[] vs, final int step, final int weight) {

        if (vs == null || vs.length < 1 || step <= 0 || weight <= 0 || weight >= step) { return new Double[0]; }

        /**
         * 该 sma 算法与 招商证券中的 sma 算法是一样的。是我在开发过程中发现 “原有的 sma 算法（即，smaOfTaLib）” 与 招商证券中 sma 计算
         * 出的结果不一致。因此，参考 “ema 和 emaOfTalib” 的差异，摸索出了 招商证券中的 “sma 算法”。
         */
        // --- 计算 “初始均值” ---
        LinkedList<Double> smaList = new LinkedList<>();
        smaList.addLast(vs[0]);                                           // 载入初始 Y' 值。

        // --- 计算 “其余 SMA” ---
        double coefficient = (double)weight / (double)(step);
        for (int i = 1; i < vs.length; i++) {
            Double prev = smaList.getLast();
            prev += (vs[i] - prev) * coefficient;
            smaList.addLast(prev);
        }

        return smaList.toArray(new Double[0]);
    }

    /**
     * 计算 “加权移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param weight 权重
     * @return Double[]
     */
    public static Double[] dma(final Double[] vs, final double weight) {

        if (vs == null || vs.length < 1 || weight <= 0 || weight >= 1) { return new Double[0]; }

        /**
         * 算法：若 Y = DMA(X, M) 则 Y = (M * X + (1 - M) * Y')；Y'为上一周期 Y，且 M > 0 && M < 1。
         *
         * 有了开发 EMA 和 SMA 指标的经验后，开发 DMA 就显得轻松许多了。从公式上看，用户可通过控制 “权重” 来调节 “当日价格”
         * 和 “前一指数平滑移动平均” 在公式中所占的比重，从而计算出新的 “指数平滑移动平均”。但，我认为使用者只会 “盲目” 地使
         * 用该指标，因为他不知道何时选择合适的权重。因此，我更希望构造一种能够 “动态调节权重” 的 DMA 指标，而且该 “权重” 的
         * 大小，能够较为客观的反映 “当日价格” 的重要性。
         *
         * 根据股票软件上该公式的提示，我准备采用 “每日成交量” / “该股流通股数”，即 “换手率”，来作为 “权重”，以衡量 “当日价
         * 格” 的重要性。
         *
         * 注意：1、“权重” M，必须 M > 0 && M < 1；
         *      2、“初始均值” 取行情序列的第一个值（通达信是这么做的）。
         */

        // --- 计算 “初始均值” ---
        LinkedList<Double> dmaList = new LinkedList<>();
        dmaList.addLast(vs[0]);                                           // 载入初始 Y' 值。

        // --- 计算 “其余 DMA” ---
        for (int i = 1; i < vs.length; i++) {
            dmaList.addLast(weight * vs[i] + (1 - weight) * dmaList.getLast());
        }

        return  dmaList.toArray(new Double[0]);
    }

    /**
     * 计算 “加权移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] wma(final Double[] vs, final int step) {

        if (vs == null || vs.length < step || step <= 2) { return new Double[0]; }

        /**
         * 一、算法 “初始部分” 分析：
         * 1、periodSub = vs[0] + vs[1] + vs[2] + vs[3] + …… vs[step - 2]，即数列中第 1 个，到第 (step - 1) 个值的累加；
         * 2、periodSum = (vs[0] * 1) + (vs[1] * 2) + (vs[2] * 3) + …… (vs[step - 2] * (step - 1))，即数列中第 1 个，
         *    乘以 “权重 (1)” + 第 2 个乘以 “权重 (2)”，…… + 第 (step - 1) 个乘以 “权重 (step - 1)”；
         * 3、today 从下标 0 开始，累计下标到 step - 1；
         *
         * 注意：
         * 1、在初始计算中不论是 periodSub，还是 periodSum，都是从数列第 1 个值，一直累积到 (step - 1) 个值，两者只是累计的
         *    算法不同。periodSub 是单纯性的累加，而 periodSum 则是给每一个值乘上一个权值后累加。
         *
         * 2、在计算 periodSum 的过程中，每一个值所乘的权值都是其（下标 + 1），即从 1 开始，直至 (step - 1)，逐步增加的。越
         *   近的数值，其权重就越大。
         *
         * 二、算法 “计算 WMA 部分” 分析：
         * 1、被除数 divider = 步长 * (步长 + 1) / 2；值得注意的是，在计算过程中使用了 “>> 1” 代替 “ / 2”，以加快计算速度。
         *    在 Java 中有三种移位运算符：
         *    <<：左移运算符，num << 1，相当于 num * 2；
         *    >>：右移运算符，num >> 1,相当于num / 2；
         *    >>>：无符号右移，忽略符号位，空位都以 0 补齐；
         *    根据计算规则，divider 随着 “步长的增大” 而逐步增加，且 “(步长 + 1) / 2” 比 “步长 / 2” 大了 0.5；研究到这里，
         *    我还是没有搞懂 divider 为什么要这么算。在我请教肖红时，她偶然提到了 “三角形数” 这个概念，我顺藤摸瓜，在网上搜了
         *    一下 “三角形数”，有了一些新的收获：
         *    OK，当用 “|”，从上到下，每层都比上一层多一个 “|”，排列一个三角形时，可以用 “等差数列求和（差为 1）” 的方式计算
         *    出到底使用了多少个 “|”。
         *    |
         *    ||
         *    |||
         *    ||||
         *    |||||         5 + 4 + 3 + 2 + 1 = 15
         *    ||||||        6 + 5 + 4 + 3 + 2 + 1 = 21；除此之外，还可用公式 6 * (6 + 1) / 2 = 21 计算出使用了多少个 “|”；
         *    |||||||       7 + 6 + 5 + 4 + 3 + 2 + 1 = 28；7 * (7 + 1) / 2 = 28
         *    ||||||||      8 * (8 + 1) / 2 = 36
         *    |||||||||     9 * (9 + 1) / 2 = 45
         *    ||||||||||    10 * (10 + 1) / 2 = 55
         *
         * 2、从下标 (step - 1)，即数列中第 (步长) 个值开始，继续计算 periodSub = vs[step - 1] + …… vs[vs.length - 1]。
         *    值得注意的是，periodSub 并不总是一直加下去的，其总是保持数列中 “较近的、step（步长）个” 值的和。具体算法如下：
         *    2.1、当 periodSub = vs[0] + …… + vs[step - 1] 时；
         *    2.2、执行 periodSub += vs[step] 后，periodSub -= vs[0]，此时 periodSub = vs[1] + …… + vs[step] 的和。
         *    2.3、执行 periodSub += vs[step + 1] 后，periodSub -= vs[1]，此时 periodSub = vs[2] + …… + vs[step + 1] 的和。
         *    简而言之，periodSub 通过不断的添加新值，剔除掉旧值，始终 “步长” 个数列的和。
         *
         * 3、从下标 (step - 1)，即数列中第 (步长) 个值开始，继续计算 periodSum = 权重（step）* vs[step - 1] + …… + 权重（step）* vs[vs.length - 1]。
         *    注意，在此部分计算中，“权重” 不在像 “计算初始部分” 中，从 1 一直递增到 (step - 1)。而是一直保持为 “步长” 的长度。根据计算过程可知，在执行若
         *    干步之后，periodSum 中都是由 “恒等于步长的权重 * 数列中的值” 累计组成。这一点充分地体现了 “较近的值，具有较大权重” 的特点。通过在循环的末尾
         *    执行：periodSum -= periodSub 操作，使其按照 “一定的范围” 不断地缩减。
         *
         * 4、通过 periodSum / divider 计算出 WMA。
         *
         * 我写到这里感到有些无力了，对于如何加的权，这个公式具体有什么含义，我还没搞清楚，我让肖红帮我分析分析，同时我还要查阅一些资料了。
         */

        // --- 计算初始 “下标”、“periodSub” 和 “periodSum” ---
        int today = 0;
        double tempReal;
        double periodSub = 0.0D, periodSum = 0.0D;
        for (int i = 1; today < (step - 1); ++i) {
            tempReal = vs[today++];

            periodSub += tempReal;
            periodSum += tempReal * (double)i;
        }

        // --- 计算 WMA 部分 ---
        LinkedList<Double> wmaList = new LinkedList<>();
        int trailingIdx = 0;
        int divider = step * (step + 1) >> 1;
        for (double trailingValue = 0.0D; today < vs.length; periodSum -= periodSub) {
            tempReal = vs[today++];

            periodSub += tempReal;
            periodSub -= trailingValue;
            periodSum += tempReal * (double)step;

            wmaList.addLast((periodSum / (double)divider));

            trailingValue = vs[trailingIdx++];
        }

        return wmaList.toArray(new Double[0]);
    }

    // --- dema、tema、t3 ---

    /**
     * 计算 “二重指数移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] dema(final Double[] vs, final int step) {

        if (vs == null || vs.length < (2 * step) || step <= 2) { return new Double[0]; }

        /**
         * 在解释 DEMA 前，我对其算法做了一个简单的图解，希望通过这种方式更加直观地了解，DEMA 的存在价值。
         * 假设有一个数列 vs，其中有 17 个元素（用 | 表示），计算 5 周期的 DEMA，算法共分为 3 个步骤：
         *
         * 第 1 步：使用 EMA 算法，对 vs 数列进行计算，得到 firstMEAs 数列：
         * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
         *                             f[0]   f[1]   f[2]   f[3]   f[4]   f[5]   f[6]   f[7]   f[8]   f[9]   f[10]  f[11]  f[12]
         *                             MA5
         *
         * 第 2 步：使用 EMA 算法，对 firstMEAs 数列进行计算，得到 secondEMAs 数列：
         *                             |      |      |      |      |      |      |      |      |      |      |      |      |
         *                                                         s[0]   s[1]   s[2]   s[3]   s[4]   s[5]   s[6]   s[7]   s[8]
         *                                                         MA5
         *
         * 第 3 步：根据 (2 * firstMEAs) - secondEMAs 公式，计算出 DEMA 序列：
         *                                                         |      |      |      |      |      |      |      |      |
         *                                                         2 * f[4] - s[0] ……
         *
         * 注意：1、由于 EMA 需要先根据 “步长” 计算出 “初始 EMA（普通 MA）”，因此上述 “第 1 步” 和 “第 2 步” 都需要 “浪费（步长 - 1）个元素”，
         *         因此，在获取历史数据的范围时，最少要为 “步长 * 2 - 1”，才够计算出 secondEMAs 数列中的 “初始 EMA”。但由于 “初始 EMA”，仅仅
         *         是 “普通 MA”，因此较为合适的取值范围应该为 “步长 * 2”；
         *      2、在第 3 步中 DEMAs[0] = 2 * firstMEAs[4] - secondEMAs[0]; 由于在计算 secondEMAs 数列时会浪费 4 个 firstMEAs 元素，因此
         *         在计算 DEMA 时，需要注意下标的位置；
         *
         * OK，分析做到这里，我是这么理解 DEMA 的，由于 firstMEAs 是 MA 的平滑，而 secondEMAs 又是对 firstMEAs 的平滑，就是说 secondEMAs[0]
         * 与 firstMEAs[0] …… firstMEAs[4] 相比是上下浮动的，根据公式：DEMAs[0] = 2 * f[4] - s[0]；其实等价于 f[4] += f[4] - s[0]；也就是
         * 说，DEMAs[0] 是 firstMEAs[4] 加上或减去 “firstMEAs[4] 与 secondEMAs[0] 间的波动”，本质上无非是对 firstMEAs[4] 又进行了一次修正。
         *
         */
        LinkedList<Double> demaList = new LinkedList<>();

        // --- 计算 “第一组 EMA 数列” ---
        Double[] firstEMAs = emaOfTaLib(vs, step);

        // --- 计算 “第二组 EMA 数列” ---
        Double[] secondEMAs = emaOfTaLib(firstEMAs, step);

        // --- 计算 “DEMA 数列” ---
        for (int i = 0; i < secondEMAs.length; i++) {
            demaList.addLast(2.0D * firstEMAs[step - 1 + i] - secondEMAs[i]);
        }

        return demaList.toArray(new Double[0]);
    }

    /**
     * 计算 “三重指数移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] tema(final Double[] vs, final int step) {

        if (vs == null || vs.length < (3 * step) || step <= 2) { return new Double[0]; }

        /**
         * 为了更好地理解 TEMA，我继续采取分析 DEMA 的办法，先进行图解。
         * 假设有一个数列 vs，其中有 17 个元素（用 | 表示），计算 5 周期的 TEMA，算法共分为 4 个步骤：
         *
         * 第 1 步：使用 EMA 算法，对 vs 数列进行计算，得到 firstMEAs 数列：
         * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
         *                             f[0]   f[1]   f[2]   f[3]   f[4]   f[5]   f[6]   f[7]   f[8]   f[9]   f[10]  f[11]  f[12]
         *                             MA5
         *
         * 第 2 步：使用 EMA 算法，对 firstMEAs 数列进行计算，得到 secondEMAs 数列：
         *                             |      |      |      |      |      |      |      |      |      |      |      |      |
         *                                                         s[0]   s[1]   s[2]   s[3]   s[4]   s[5]   s[6]   s[7]   s[8]
         *                                                         MA5
         *
         * 第 3 步：使用 EMA 算法，对 secondEMAs 数列进行计算，得到 thirdEMAs 数列：
         *                                                         |      |      |      |      |      |      |      |      |
         *                                                                                     t[0]   t[1]   t[2]   t[3]   t[4]
         *
         * 第 4 步：根据 thirdEMAs + (3 * (firstMEAs - secondEMAs)) 公式，计算出 TEMA 序列：
         *                                                                                     |      |      |      |      |
         *                                                                                     t[0] + (3 * f[8] - s[4])……
         *
         * 注意事项与 DEMA 中的差不多，这里就不写了。
         *
         * OK，分析做到这里，我是这么理解 TEMA 的，TEMA 无非就是，第三次平滑 + 3 倍前两次平滑的波动。
         *
         */
        LinkedList<Double> temaList = new LinkedList<>();

        // --- 计算 “第一组 EMA 数列” ---
        Double[] firstEMAs = emaOfTaLib(vs, step);

        // --- 计算 “第二组 EMA 数列” ---
        Double[] secondEMAs = emaOfTaLib(firstEMAs, step);

        // --- 计算 “第三组 EMA 数列” ---
        Double[] thirdEMAs = emaOfTaLib(secondEMAs, step);

//        outReal[outIdx] += 3.0D * firstEMA[firstEMAIdx++] - 3.0D * secondEMA[secondEMAIdx++];

        // --- 计算 “DEMA 数列” ---
        /**
         * 注意：虽然 (3.0D * firstEMAs[2 * step - 2 + i] - 3.0D * secondEMAs[step - 1 + i]) 可根据公式搞成下面这种形式：
         *           (3.0D * (firstEMAs[2 * step - 2 + i] - secondEMAs[step - 1 + i])) 但千万别这么做。如果这么做了，那
         *           计算出的结果将于 “不提取 3.0D” 计算出的结果 “不一致”。我猜想这是由于 “精度太高” 的原因。
         */
        for (int i = 0; i < thirdEMAs.length; i++) {
            temaList.addLast(thirdEMAs[i] + (3.0D * firstEMAs[2 * step - 2 + i] - 3.0D * secondEMAs[step - 1 + i]));
        }

        return temaList.toArray(new Double[0]);
    }

    /**
     * 计算 “三重指数移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @param factor 因子
     * @return Double[]
     */
    public static Double[] t3(final Double[] vs, final int step, final double factor) {

        if (vs == null || vs.length < (step + 5 * (step - 1)) || step <= 2 || factor <= 0 || factor >= 1) { return new Double[0]; }

        /**
         * 第 1 步：计算 “系数”。
         *
         * 系数 COEFFICIENT_ONE 的计算方式与 EMA 算法中 “系数” 的计算方式相同，当 “步长” 越大时，COEFFICIENT_ONE 越小；
         * 系数 COEFFICIENT_TWO = 1D - COEFFICIENT_ONE；当 “步长” 越大时，COEFFICIENT_TWO 越大于 COEFFICIENT_ONE；
         */
        final double COEFFICIENT_ONE = 2D / (step + 1D);
        final double COEFFICIENT_TWO = 1D - COEFFICIENT_ONE;

        /**
         * 第 2 步：使用数列中第 [1 至 步长] 个值计算 firstMA。 图解以数列为 20，步长为 3 举例：
         *
         * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
         *               firstMA
         *
         * 注意：firstMA 的计算方式比较简单，就是普通的 SMA。
         */
        int today = 0;
        double tempReal = 0D;
        for(int i = 0; i < step; i++) {
            tempReal += vs[today++];
        }

        double firstMA = tempReal / (double)step;
        tempReal = firstMA;

        /**
         * 第 3 步：使用数列中第 [(步长 + 1) 至 (步长 + (步长 - 1))] 个值计算 secondMA。
         *
         * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
         *               firstMA       secondMA
         *
         * 注意：1、secondMA 可以简单看作是 “步长个 firstMA” 的 SMA；
         *      2、在计算 secondMA 前，除了首个 firstMA 之外，其余的 firstMA 都是用公式：(系数 1 * 数列) + (系数 2 * 前一 firstMA)；实现的，
         *         在 “步长” > 3 的情况下，“系数 2” > “系数 1”，根据这项特性可以看出，“后一 firstMA” 是主要根据 “前一 firstMA” 修正后得到的，
         *         但这个论证有一个前提的条件，便是 “步长” 必须大于 3。如果 “步长 == 3”，那么 “数列” 和 “前一 firstMA” 的权重就一样了，甚至当
         *         “步长 == 2” 时，“数列” 的权重将大于 “前一 firstMA” 的权重，那 “后一 firstMA” 就是主要根据 “数列” 修正后得到得了。
         */
        for(int i = 0; i < (step - 1); i++) {
            firstMA = (COEFFICIENT_ONE * vs[today++]) + (COEFFICIENT_TWO * firstMA);
            tempReal += firstMA;
        }

        double secondMA = tempReal / (double)step;
        tempReal = secondMA;

        /**
         * 第 4 步：使用数列中第 [((步长 + (步长 - 1)) + 1) 至 (步长 + 2 * (步长 - 1))] 个值计算 secondMA。
         *
         * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
         *               firstMA       secondMA      thirdMA
         *
         * 注意：1、thirdMA 可以简单看作是 “步长个 secondMA” 的 SMA；
         *      2、在计算 thirdMA 前，除了首个 secondMA 之外，其余的 secondMA 都是修正后得到的，具体过程参考计算公式，论述参考第 3 步的解释。
         */
        for(int i = 0; i < (step - 1); i++) {
            firstMA = (COEFFICIENT_ONE * vs[today++]) + (COEFFICIENT_TWO * firstMA);
            secondMA = (COEFFICIENT_ONE * firstMA) + (COEFFICIENT_TWO * secondMA);

            tempReal += secondMA;
        }

        double thirdMA = tempReal / (double)step;
        tempReal = thirdMA;

        /**
         * 第 5 步：使用数列中第 [((步长 + 2 * (步长 - 1)) + 1) 至 (步长 + 3 * (步长 - 1))] 个值计算 secondMA。
         *
         * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
         *               firstMA       secondMA      thirdMA       fourthMA
         *
         * 注意：1、fourthMA 可以简单看作是 “步长个 thirdMA” 的 SMA；
         *      2、在计算 fourthMA 前，除了首个 thirdMA 之外，其余的 thirdMA 都是修正后得到的，具体过程参考计算公式，论述参考第 3 步的解释。
         */
        for(int i = 0; i < (step - 1); i++) {
            firstMA = (COEFFICIENT_ONE * vs[today++]) + (COEFFICIENT_TWO * firstMA);
            secondMA = (COEFFICIENT_ONE * firstMA) + (COEFFICIENT_TWO * secondMA);
            thirdMA = (COEFFICIENT_ONE * secondMA) + (COEFFICIENT_TWO * thirdMA);

            tempReal += thirdMA;
        }

        double fourthMA = tempReal / (double)step;
        tempReal = fourthMA;

        /**
         * 第 6 步：使用数列中第 [((步长 + 3 * (步长 - 1)) + 1) 至 (步长 + 4 * (步长 - 1))] 个值计算 secondMA。
         *
         * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
         *               firstMA       secondMA      thirdMA       fourthMA      fifthMA
         *
         * 注意：1、fifthMA 可以简单看作是 “步长个 fourthMA” 的 SMA；
         *      2、在计算 fifthMA 前，除了首个 fourthMA 之外，其余的 fourthMA 都是修正后得到的，具体过程参考计算公式，论述参考第 3 步的解释。
         */
        for(int i = 0; i < (step - 1); i++) {
            firstMA = (COEFFICIENT_ONE * vs[today++]) + (COEFFICIENT_TWO * firstMA);
            secondMA = (COEFFICIENT_ONE * firstMA) + (COEFFICIENT_TWO * secondMA);
            thirdMA = (COEFFICIENT_ONE * secondMA) + (COEFFICIENT_TWO * thirdMA);
            fourthMA = (COEFFICIENT_ONE * thirdMA) + (COEFFICIENT_TWO * fourthMA);

            tempReal += fourthMA;
        }

        double fifthMA = tempReal / (double)step;
        tempReal = fifthMA;

        /**
         * 第 7 步：使用数列中第 [((步长 + 4 * (步长 - 1)) + 1) 至 (步长 + 5 * (步长 - 1))] 个值计算 secondMA。
         *
         * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
         *               firstMA       secondMA      thirdMA       fourthMA      fifthMA       sixthMA
         *
         * 注意：1、sixthMA 可以简单看作是 “步长个 fifthMA” 的 SMA；
         *      2、在计算 sixthMA 前，除了首个 fifthMA 之外，其余的 fifthMA 都是修正后得到的，具体过程参考计算公式，论述参考第 3 步的解释；
         *      3、在 “第 2 - 7 步” 的循环中，除了 “第 2 步” 循环了 “步长” 次外，其余的，只循环了 "步长 - 1" 次。因为这些循环（2 - 7 次）
         *         中，其负责计算 “目的均值” 的 “主要数值中的第一个” 已经在上一个循环中计算好了，因此只需在循环 “步长 - 1” 次，计算出其余的
         *         算 “主要数值”，加上 “初始主要数值” 正好是 “步长” 个；
         */
        for(int i = 0; i < (step - 1); i++) {
            firstMA = (COEFFICIENT_ONE * vs[today++]) + (COEFFICIENT_TWO * firstMA);
            secondMA = (COEFFICIENT_ONE * firstMA) + (COEFFICIENT_TWO * secondMA);
            thirdMA = (COEFFICIENT_ONE * secondMA) + (COEFFICIENT_TWO * thirdMA);
            fourthMA = (COEFFICIENT_ONE * thirdMA) + (COEFFICIENT_TWO * fourthMA);
            fifthMA = (COEFFICIENT_ONE * fourthMA) + (COEFFICIENT_TWO * fifthMA);

            tempReal += fifthMA;
        }

        double sixthMA = tempReal / (double)step;

        /**
         * 第 8 步：计算用于 “均线 3（thirdMA）” 至 “均线 6（sixthMA）” 占比的系数。
         *
         * 这里 c1 是 sixthMA 的占比；c2 是 fifthMA 的占比；c3 是 fourthMA 的占比；c4 是 thirdMA 的占比。
         */
        double twiceFactor = Math.pow(factor, 2);
        double c1 = -(Math.pow(factor, 3));
        double c2 = 3.0D * (twiceFactor - c1);
        double c3 = -6.0D * twiceFactor - 3.0D * (factor - c1);
        double c4 = 1.0D + 3.0D * factor - c1 + 3.0D * twiceFactor;

        /**
         * 第 9 步：根据 “sixthMA、fifthMA、fourthMA 和 thirdMA” 与 “它们的占比系数” 计算出 T3。
         */
        LinkedList<Double> t3List = new LinkedList<>();
        t3List.addLast(c1 * sixthMA + c2 * fifthMA + c3 * fourthMA + c4 * thirdMA);

        while (today < vs.length) {
            firstMA = (COEFFICIENT_ONE * vs[today++]) + (COEFFICIENT_TWO * firstMA);
            secondMA = (COEFFICIENT_ONE * firstMA) + (COEFFICIENT_TWO * secondMA);
            thirdMA = (COEFFICIENT_ONE * secondMA) + (COEFFICIENT_TWO * thirdMA);
            fourthMA = (COEFFICIENT_ONE * thirdMA) + (COEFFICIENT_TWO * fourthMA);
            fifthMA = (COEFFICIENT_ONE * fourthMA) + (COEFFICIENT_TWO * fifthMA);
            sixthMA = (COEFFICIENT_ONE * fifthMA) + (COEFFICIENT_TWO * sixthMA);

            t3List.addLast(c1 * sixthMA + c2 * fifthMA + c3 * fourthMA + c4 * thirdMA);
        }

        return t3List.toArray(new Double[0]);
    }

    // --- trima ---

    /**
     * 计算 “三角形移动平均线”，返回的均值数列 “由远到近” 排列，且没有对小数精度进行处理。
     *
     * @param vs 数组序列（该数列必须符合 “由远到近” 方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] trima(final Double[] vs, final int step) {

        if (vs == null || vs.length < step || step <= 2) { return new Double[0]; }

        LinkedList<Double> trimaList = new LinkedList<>();

        /** step >> 1 是指，将 step / 2，这个在 wma 类中有详细的介绍，这里就不重复了。 */
        int halfOfStep = step >> 1;
        if (step % 2 == 1) {

            /**
             * 第 1 步：用 “一半的步长” 计算 “因子”。从公式中看出，步长越大，因子越小。
             */
            double factor = 1.0D / (Math.pow((halfOfStep + 1D), 2));

            /**
             * 第 2 步：计算第 1 个 TRIMA。在该步骤中包含 3 个部分：
             *         （1）“0 至 (步长 / 2)” 的循环；
             *         （2）“(步长 / 2) + 1 至 2 * (步长 / 2)” 的循环；
             *         （3）计算 TRIMA；
             *
             * 2.1：“0 至 (步长 / 2)” 的循环图解（以数列为 15，步长为 7 举例）：int halfOfStep = step / 2 = 2;
             *
             * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
             * ^结束                ^开始（从此处向 “左” 执行算法）
             *                      （1）numeratorSub 是每一次循环中 “该值” 与 “数列中相应值” 的和；
             *                      （2）numerator 是每一次循环中 “该值” 与 “numeratorSub” 的和。
             *
             * 2.2：“(步长 / 2) + 1 至 2 * (步长 / 2)” 的循环图解：
             *
             * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
             *     （从此处向 “右” 执行算法）^开始          ^结束
             *                             （1）numeratorAdd 是每一次循环中 “该值” 与 “数列中相应值” 的和；
             *                             （2）numerator 是每一次循环中 “该值” 与 numeratorAdd 的和；
             *
             * 2.3：这步相对直观，但不好理解，为什么是 numerator 除以 factor（系数），numerator 代表什么，factor 为什么这么计算，都有待研究。
             *
             * 注意：在 “循环计算” 的步骤中，从 “左往右” 还是从 “右往左” 累加，对于 numerator 的结果是很不同的。以下面的计算为例，对此进行说明：
             *
             * double a = 0.0D, b = 0.0D;
             *
             * --- 测试 1 ---
             * for(int i = 0; i <= 3; a += i, b += a, i++) ;
             * Out：每一次循环过程中，a 和 b 的变化情况
             * a = 0.0; b = 0.0
             * a = 1.0; b = 1.0
             * a = 3.0; b = 4.0
             * a = 6.0; b = 10.0
             *
             * --- 测试 2---
             * for(int i = 3; i >= 0; a += i, b += a, i--) ;
             * Out：每一次循环过程中，a 和 b 的变化情况
             * a = 3.0; b = 3.0
             * a = 5.0; b = 8.0
             * a = 6.0; b = 14.0
             * a = 6.0; b = 20.0
             *
             * 可以看到，每当 FOR 循环执行后，a 的值不受循环顺序的影响，两次的累加值都一样。而 b 值却受到了循环顺序的影响。即，当数列中的值，位置
             * 固定，且从较大一端开始循环计算 “累加的累加” 时，其结果就越大。
             */
            double numerator = 0.0D, numeratorSub = 0.0D;
            for (int i = halfOfStep; i >= 0; i--) {
                numeratorSub += vs[i];
                numerator += numeratorSub;
            }

            int todayIdx = 2 * halfOfStep;
            double numeratorAdd = 0.0D;
            for (int i = (++halfOfStep); i <= todayIdx; i++) {
                numeratorAdd += vs[i];
                numerator += numeratorAdd;
            }

            trimaList.addLast(numerator * factor);

            /**
             * 第 3 步：循环计算其余的 TRIMA。在该循环中包含以下几个个部分：
             *         （1）numerator 抛弃前 numeratorSub，以 numeratorAdd 替代；
             *         （2）重新计算 numeratorSub，从数列第 1 个逐步抛弃，从数列第 (步长 / 2) + 1 个逐步累加；
             *         （3）重新计算 numeratorAdd 从数列第 (步长 / 2) + 1 逐步抛弃，从数列第 2 * (步长 / 2) + 1 个逐步累加；
             *         （4）numerator 从数列第 2 * (步长 / 2) + 1 个逐步累加；
             *         （5）使用公式 numerator / factor（因子），计算后续的 TRIMA。
             */
            ++todayIdx;
            int trailingIdx = 0;
            for (double tempReal = vs[trailingIdx++]; todayIdx < vs.length; todayIdx++, tempReal = vs[trailingIdx++]) {
                numerator -= numeratorSub;
                numerator += numeratorAdd;

                numeratorSub -= tempReal;
                tempReal = vs[halfOfStep++];
                numeratorSub += tempReal;

                numeratorAdd -= tempReal;
                tempReal = vs[todayIdx];
                numeratorAdd += tempReal;

                numerator += tempReal;

                trimaList.addLast(numerator * factor);
            }

        } else {
            /**
             * 第 1 步：用 “一半的步长” 计算 “因子”。从公式中看出，步长越大，因子越小。
             *
             * 注意：该因子的计算方式，与 “步长” 为单数时不同。
             */
            double factor = 1.0D / (halfOfStep * (halfOfStep + 1D));

            /**
             * 第 2 步：计算第 1 个 TRIMA。在该步骤中包含 3 个部分：
             *         （1）“0 至 (步长 / 2) - 1” 的循环；
             *         （2）“(步长 / 2) 至 2 * 步长 - 1” 的循环；
             *         （3）计算 TRIMA；
             *
             * 该计算部分与 “步长为单数” 时基本相同，就不具体介绍了，这里只给出图解部分：
             *
             * 2.1：“0 至 (步长 / 2) - 1” 的循环图解（以数列为 15，步长为 8 举例）：
             * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
             * ^结束                ^开始（从此处向 “左” 执行算法）
             *
             * 2.2：“(步长 / 2) 至 2 * 步长 - 1” 的循环图解：
             *
             * |      |      |      |      |      |      |      |      |      |      |      |      |      |      |
             *     （从此处向 “右” 执行算法）^开始                 ^结束
             *
             *
             * 只有以下几点需要注意：
             * （1）不管 “步长” 的单双，在计算第 1 个循环时，“相邻两个步长（例如，7 和 8）” 的计算范围都是相同的。但由于 int 间的除法会向下取整，
             *     因此，当 “步长” 为双数时，int i = 步长 / 2 - 1；
             * （2）不管 “步长” 的单双，在计算第 2 个循环时，计算的终止都是数列中第 “步长” 个值，即下标中的 “步长 - 1”；
             */
            double numerator = 0.0D, numeratorSub = 0.0D;
            for (int i = (halfOfStep - 1); i >= 0; i--) {
                numeratorSub += vs[i];
                numerator += numeratorSub;
            }

            int todayIdx = 2 * halfOfStep - 1;
            double numeratorAdd = 0.0D;
            for (int i = halfOfStep; i <= todayIdx; i++) {
                numeratorAdd += vs[i];
                numerator += numeratorAdd;
            }

            trimaList.addLast(numerator * factor);

            /**
             * 第 3 步：循环计算其余的 TRIMA。该部分与 “步长” 为单数时的计算基本相同，只有一点需要注意：
             *
             * 在 “步长” 为单数的计算中，循环一开始就执行了 numerator -= numeratorSub; 和 numerator += numeratorAdd; 之后便是
             * 对 numeratorSub 和 numeratorAdd 的修正。而在 “步长” 为双数的计算中，循环一开始同样执行了 numerator -= numeratorSub；
             * 之后便执行了对 numeratorSub 的修正，同时还执行了 numeratorAdd 对其首值的抛弃操作，之后才执行 numerator += numeratorAdd；
             * 之后才对 numeratorAdd 进行了修正。
             */
            ++todayIdx;
            int trailingIdx = 0;
            for (double tempReal = vs[trailingIdx++]; todayIdx < vs.length; todayIdx++, tempReal = vs[trailingIdx++]) {
                numerator -= numeratorSub;

                numeratorSub -= tempReal;

                tempReal = vs[halfOfStep++];
                numeratorSub += tempReal;
                numeratorAdd -= tempReal;

                numerator += numeratorAdd;

                tempReal = vs[todayIdx];
                numeratorAdd += tempReal;

                numerator += tempReal;

                trimaList.addLast(numerator * factor);
            }

        }

        return trimaList.toArray(new Double[0]);
    }

    // --- kama ---

    /**
     * 根据数据序列计算均值。
     *
     * @param vs 数组序列（该数列必须符合由远到近方式排序）
     * @param step 步长
     * @return Double[]
     */
    public static Double[] kama(final Double[] vs, final int step) {

        if (vs == null || vs.length < step || step <= 2) { return new Double[0]; }

        /**
         * 在 KAMA 算法中有三个比较重要的要素：
         * 1、“步长” 范围内，相邻两值的 “波动累加”；
         * 2、“下周期初值” 与 “前周期初值” 间波动；
         * 3、系数；
         *
         * 其中，“系数” 的计算跟前两项相关，在每一次计算中，前两个要素都会 “剔除较远的值，纳入较新的值”，这也说明，
         * 上面三个要素都会在每一次迭代中不断修正。
         *
         * 在算法的初期 “（要素 1、2 和 3 都已计算完成的情况下）”，初始 KAMA 被设置为数列中第 “步长” 个元素，之后便是不断执行：
         * 下一个 KAMA = (数列中下一个元素 - 前一个 KAKA) * 系数。
         *
         * OK，在上面的公式中可以看出，“系数” 对 “下一个 KAMA” 的影响非常巨大，而直接影响 “系数” 的则是 要素 1 和 2，即两种不
         * 同的 “波动”，也就是说：“考夫曼移动平均线是一种以 相邻波动累加 和 下周期初和上周期末的波动 为权重的动态加权平均线”。
         */

        // --- 算法分步解释 ---

        /**
         * 第一步：计算 [0, (step -1)] 范围内，相邻两值的 “波动累加”。
         *
         * 计算过程为：sumROC = Math.abs(vs[0] - vs[1]) + …… + Math.abs(vs[step - 1] - vs[step])；
         * 即，在一定范围内，“当前值” - “下一值” 的 “绝对值” 的累加。
         *
         * 注意：该步骤完成后，下标（today） 将为 step。指向数列中第（step + 1）个值。
         */
        double sumROC = 0.0D;
        int today = 0;
        int trailingIdx = today;

        double tempReal;
        for(int i = step; i > 0; i--) {
            tempReal = vs[today];
            tempReal -= vs[++today];

            sumROC += Math.abs(tempReal);
        }

        /**
         * 第二步：计算数列中 “第（step + 1）个”值 与 “第 1 个值” 间的 “波动”（差）。
         *
         * 计算过程为：periodROC = vs[step] - vs[0]；
         *
         * 注意：该步骤完成后，trailingIdx == 1，today 依然为 step。
         */
        tempReal = vs[today];
        double tempReal2 = vs[trailingIdx++];
        double periodROC = tempReal - tempReal2;

        /**
         * 第三步：计算系数。
         */
        double coefficient = computeCoefficient(sumROC, periodROC);

        /**
         * 第四步：计算和修正（指计算 FOR 循环）第一个 KAMA。
         * 首先、（1）把数列中第 “步长” 个值作为 “初始 KAMA”；
         *      （2）记录数列中第 1 个值，即 trailingValue = vs[trailingIdx++] = vs[0]；
         * 其次、执行 FOR 循环；
         *      （1）修正 prevKAMA；prevKAMA = 数列中第 “步长 + 1” 个值 - “数列中第 步长 个值” * “系数” + “数列中第 步长 个值”（此时 today = “步长 +２”）；
         *      （2）由于　today 此时等于 “步长 + 2” <= “步长” 不成立，下面的语句压根不会执行（但为了理解算法思想，我这里还是进行了解读）。
         *
         *      （3）重置 “下周期初值” 与 “前周期初值” 间波动；
         *          --- 下面是第一次循环是发生的情况 ---
         *          tempReal = 数列中第 “步长 + 2” 个值；
         *          tempReal2 = 数列中第 “2” 个值（此时 trailingIdx = trailingIdx + 1）；
         *          重新计算 periodROC = tempReal - tempReal2；
         *
         *      （4）重置 “步长” 范围内，相邻两值的 “波动累加”
         *          trailingValue 为前一周期内第 1 个值；tempReal2 为前一周期内第 2 个值；
         *          sumROC -= Math.abs(trailingValue - tempReal2)；从当前 “波动累加” 中排除掉第一个 “波动”；
         *          sumROC += Math.abs(tempReal - vs[today - 1]); 在当前 “波动累加” 中，加入下一个 “波动”；
         *
         *      （5）将 trailingValue 赋值为 前一周期内第 2 个值，为下一次修正做准备。
         *
         *      （6）根据本周期内的 “波动累加” 和 “下周期初值” 与 “前周期初值” 间波动，重新计算 “系数”。
         *
         *       (7) 在重新计算系数后，计算当前 KAMA。
         * 最后、today++，开始下一个循环。
         */
        LinkedList<Double> kamaList = new LinkedList<>();

        double prevKAMA = vs[today - 1];
        double trailingValue = tempReal2;
        for(prevKAMA += (vs[today++] - prevKAMA) * coefficient; today <= step; today++) {
            tempReal = vs[today];
            tempReal2 = vs[trailingIdx++];
            periodROC = tempReal - tempReal2;

            sumROC -= Math.abs(trailingValue - tempReal2);
            sumROC += Math.abs(tempReal - vs[today - 1]);

            trailingValue = tempReal2;

            coefficient = computeCoefficient(sumROC, periodROC);
            prevKAMA += (vs[today] - prevKAMA) * coefficient;
        }

        kamaList.addLast(prevKAMA);

        /**
         * 第五步：循环计算 KAMA。
         *
         * 由于循环中的代码与 第四步 FOR 循环中的代码相同，这里就不再进行解释了。
         */
        while (today < vs.length) {

            tempReal = vs[today];
            tempReal2 = vs[trailingIdx++];
            periodROC = tempReal - tempReal2;

            sumROC -= Math.abs(trailingValue - tempReal2);
            sumROC += Math.abs(tempReal - vs[today - 1]);

            trailingValue = tempReal2;

            coefficient = computeCoefficient(sumROC, periodROC);
            prevKAMA += (vs[today] - prevKAMA) * coefficient;

            today++;

            kamaList.addLast(prevKAMA);
        }

        return kamaList.toArray(new Double[0]);
    }

    /**
     * 计算系数。
     *
     * @param sumROC “步长” 范围内，相邻两值的 “波动累加”
     * @param periodROC “下周期初值” 与 “前周期初值” 间波动
     * @return double
     */
    private static double computeCoefficient(double sumROC, double periodROC) {

        /** 在计算过程中用到了 “科学计数法”，为了更直观的理解，下面先对此进行了解读：
         * （1）、-1.0E-8D = -1.0D * 10 的 -8 次方 = -1.0 * 1 / 100000000 = -0.00000001
         * （2）、1.0E-8D = 1.0D * 10 的 -8 次方 = 1.0 * 1 / 100000000 = 0.00000001
         *
         * 对 “第一阶段” 的解读：
         * 当 “相邻波动累加” > “下周期初值与前周期初值间波动”，同时 “相邻波动累加” 要具有一定的 “范围” 时，
         * 系数 = “下周期初值与前周期初值间波动” 占 “相邻波动累加” 比率的绝对值，否则就直接把系数赋值为 1。
         *
         * 这里我用了 “范围” 这个词，即使 “相邻波动累加” 是负数，只要其大于 “下周期初值与前周期初值间波动”，
         * 且小于 “-0.00000001” 即可。但值得注意的是，当 sumROC = -100，periodROC = -200 时，也是满足条
         * 件的。此时 “系数 = |-200/-100| = 2.0”；
         *
         * 对 “第二阶段” 的解读：
         * 1、coefficient = coefficient * 0.6021505376344085D + 0.06451612903225806D;
         *    系数 = 系数的60% + 一个固定常量；
         * 2、coefficient *= coefficient;
         *
         * 我实在搞不懂，为什么系数要这么算。
         */
        double coefficient;

        // --- 第一阶段 ---
        if (sumROC > periodROC && (sumROC <= -1.0E-8D || sumROC >= 1.0E-8D)) {
            coefficient = Math.abs(periodROC / sumROC);
        } else {
            coefficient = 1.0D;
        }

        // --- 第二阶段 ---
        coefficient = coefficient * 0.6021505376344085D + 0.06451612903225806D;
        coefficient *= coefficient;

        return coefficient;
    }
}
