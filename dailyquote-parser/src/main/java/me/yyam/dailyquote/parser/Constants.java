package me.yyam.dailyquote.parser;

import java.util.*;

/**
 * Created by yyam on 15-4-8.
 */
public class Constants {
    public static final Map<String, String> dbfMap2DB = new HashMap<>();

    public static String market = "market";
    public static String lastModified = "lastModified";

    public static String FILE_ENCODE = "GBK";

    public static final String TRADING_DAY_TABLE = "qt_tradingdaynew";
    public static final String TRADING_DAY_COL = "tradingdate";

    public static final Map<String, String> marketMap = new HashMap<>();

    public static final Map<String, String> market2Table = new HashMap<>();


    protected static final Set<String> PARSED_TIME_SH = new HashSet<>();
    protected static final Set<String> PARSED_TIME_SZ = new HashSet<>();

    protected static final Set<Long> PARSED_MODIFIED_SH = new HashSet<>();
    protected static final Set<Long> PARSED_MODIFIED_SZ = new HashSet<>();

    protected static Boolean TRADING_END_SH = Boolean.FALSE;
    protected static Boolean TRADING_END_SZ = Boolean.FALSE;
    protected static Boolean INSERT_END_SH = Boolean.FALSE;
    protected static Boolean INSERT_END_SZ = Boolean.FALSE;

    public static Map<String, List<String>> PARSED_MINUTE_SECOND_MAP_SH = new HashMap<>();
    public static Map<String, List<String>> PARSED_MINUTE_SECOND_MAP_SZ = new HashMap<>();

    /**
     * 记录上一个时间戳的分钟。<br/>
     * 当新的时间戳开始解析，如果分钟不同，则更新对应的表，触发更新事件，执行对应的触发器<br/>
     * table:t_sh_last_minute
     */
    protected static String PRE_SECOND_SH = DateUtils.parse(new Date(), "yyyyMMddHHmmss");

    /**
     * 记录上一个时间戳的分钟。<br/>
     * 当新的时间戳开始解析，如果分钟不同，则更新对应的表，触发更新事件，执行对应的触发器<br/>
     * table:t_sz_last_minute
     */
    protected static String PRE_SECOND_SZ = DateUtils.parse(new Date(), "yyyyMMddHHmmss");

    public static final SecondComparator SECOND_COMPARATOR = new SecondComparator();


    static {
        dbfMap2DB.put("S1", "F01");//证券代码
        dbfMap2DB.put("S2", "F02");//证券名称
        dbfMap2DB.put("S3", "F03");//前收盘价格
        dbfMap2DB.put("S4", "F04");//今开盘价格
        dbfMap2DB.put("S5", "F05");//今成交金额
        dbfMap2DB.put("S6", "F06");//最高价格
        dbfMap2DB.put("S7", "F07");//最低价格
        dbfMap2DB.put("S8", "F08");//最新价格
        dbfMap2DB.put("S9", "F09");//当前买入价格
        dbfMap2DB.put("S10", "F10");//当前卖出价格
        dbfMap2DB.put("S11", "F11");//成交数量
        dbfMap2DB.put("S13", "F13");//市盈率

        dbfMap2DB.put("S15", "F15");//申买量一

        dbfMap2DB.put("S16", "F16");//申买价二
        dbfMap2DB.put("S17", "F17");//申买量二

        dbfMap2DB.put("S18", "F18");//申买价三
        dbfMap2DB.put("S19", "F19");//申买量三

        dbfMap2DB.put("S26", "F26");//申买价四
        dbfMap2DB.put("S27", "F27");//申买量四

        dbfMap2DB.put("S28", "F28");//申买价五
        dbfMap2DB.put("S29", "F29");//申买量五

        dbfMap2DB.put("S21", "F21");//申卖量一

        dbfMap2DB.put("S22", "F22");//申卖价二
        dbfMap2DB.put("S23", "F23");//申卖量二

        dbfMap2DB.put("S24", "F24");//申卖价三
        dbfMap2DB.put("S25", "F25");//申卖量三

        dbfMap2DB.put("S30", "F30");//申卖价四
        dbfMap2DB.put("S31", "F31");//申卖量四

        dbfMap2DB.put("S32", "F32");//申卖价五
        dbfMap2DB.put("S33", "F33");//申卖量五

        dbfMap2DB.put("HQZQDM", "F01");//证券代码
        dbfMap2DB.put("HQZQJC", "F02");//证券简称
        dbfMap2DB.put("HQZRSP", "F03");//昨日收盘价
        dbfMap2DB.put("HQJRKP", "F04");//今日开盘价
        dbfMap2DB.put("HQCJJE", "F05");//成交金额
        dbfMap2DB.put("HQZGCJ", "F06");//最高成交价
        dbfMap2DB.put("HQZDCJ", "F07");//最低成交价
        dbfMap2DB.put("HQZJCJ", "F08");//最近成交价
        dbfMap2DB.put("HQBJW1", "F09");//买价位一/叫买揭示价
        dbfMap2DB.put("HQSJW1", "F10");//卖价位一/叫卖揭示价
        dbfMap2DB.put("HQCJSL", "F11");//成交数量
        dbfMap2DB.put("HQSYL1", "F13");//市盈率 1
        dbfMap2DB.put("HQSYL2", "F14");//市盈率2

        dbfMap2DB.put("HQBSL1", "F15");//买数量一

        dbfMap2DB.put("HQBJW2", "F16");//买价位二
        dbfMap2DB.put("HQBSL2", "F17");//买数量二

        dbfMap2DB.put("HQBJW3", "F18");//买价位三
        dbfMap2DB.put("HQBSL3", "F19");//买数量三


        dbfMap2DB.put("HQBJW4", "F26");//买价位四
        dbfMap2DB.put("HQBSL4", "F27");//买数量四

        dbfMap2DB.put("HQBJW5", "F28");//买价位五
        dbfMap2DB.put("HQBSL5", "F29");//买数量五

        dbfMap2DB.put("HQSSL1", "F21");//卖数量一

        dbfMap2DB.put("HQSJW2", "F22");//卖价位二
        dbfMap2DB.put("HQSSL2", "F23");//卖数量二

        dbfMap2DB.put("HQSJW3", "F24");//卖价位三
        dbfMap2DB.put("HQSSL3", "F25");//卖数量三

        dbfMap2DB.put("HQSJW4", "F30");//卖价位四
        dbfMap2DB.put("HQSSL4", "F31");//卖数量四

        dbfMap2DB.put("HQSJW5", "F32");//卖价位五
        dbfMap2DB.put("HQSSL5", "F33");//卖数量五

        dbfMap2DB.put("HQCJBS", "F34");//成交笔数
        dbfMap2DB.put("HQJSD1", "F35");//价格升跌 1
        dbfMap2DB.put("HQJSD2", "F36");//价格升跌 2
        dbfMap2DB.put("HQHYCC", "F37");//合约持仓量

        dbfMap2DB.put(market, "F88");//市场   SH/SZ
        dbfMap2DB.put(lastModified, "F99");//行情时间戳 yyyyMMddHHmmss

        marketMap.put("SHOW2003", "SH");
        marketMap.put("show2003", "SH");
        marketMap.put("SHOW2003.DBF", "SH");
        marketMap.put("show2003.dbf", "SH");
        marketMap.put("SJSHQ", "SZ");
        marketMap.put("sjshq", "SZ");
        marketMap.put("SJSHQ.DBF", "SZ");
        marketMap.put("sjshq.dbf", "SZ");

        market2Table.put("SH", "t_daily_quote_sh");
        market2Table.put("SZ", "t_daily_quote_sz");
    }

    public static String get(String key) {
        return dbfMap2DB.get(key);
    }

    public static String getMarket(String key) {
        return marketMap.get(key);
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
    }
}
