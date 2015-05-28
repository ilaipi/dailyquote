package com.richeninfo.dailyquote.backup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yyam on 15-4-8.
 */
public class Constants {
    private static Log logger = LogFactory.getLog(Constants.class);
    public static final Map<String, String> SH_FIELD_2_COLUMN = new HashMap<>();
    public static final Map<String, String> SZ_FIELD_2_COLUMN = new HashMap<>();

    private static final Properties properties = new Properties();

    public static String market = "market";
    public static String lastModified = "lastModified";

    static {
        SH_FIELD_2_COLUMN.put("F01", "S1");//证券代码
        SH_FIELD_2_COLUMN.put("F02", "S2");//证券名称
        SH_FIELD_2_COLUMN.put("F03", "S3");//前收盘价格
        SH_FIELD_2_COLUMN.put("F04", "S4");//今开盘价格
        SH_FIELD_2_COLUMN.put("F05", "S5");//今成交金额
        SH_FIELD_2_COLUMN.put("F06", "S6");//最高价格
        SH_FIELD_2_COLUMN.put("F07", "S7");//最低价格
        SH_FIELD_2_COLUMN.put("F08", "S8");//最新价格
        SH_FIELD_2_COLUMN.put("F09", "S9");//当前买入价格
        SH_FIELD_2_COLUMN.put("F10", "S10");//当前卖出价格
        SH_FIELD_2_COLUMN.put("F11", "S11");//成交数量
        SH_FIELD_2_COLUMN.put("F13", "S13");//市盈率

        SH_FIELD_2_COLUMN.put("F15", "S15");//申买量一

        SH_FIELD_2_COLUMN.put("F16", "S16");//申买价二
        SH_FIELD_2_COLUMN.put("F17", "S17");//申买量二

        SH_FIELD_2_COLUMN.put("F18", "S18");//申买价三
        SH_FIELD_2_COLUMN.put("F19", "S19");//申买量三

        SH_FIELD_2_COLUMN.put("F26", "S26");//申买价四
        SH_FIELD_2_COLUMN.put("F27", "S27");//申买量四

        SH_FIELD_2_COLUMN.put("F28", "S28");//申买价五
        SH_FIELD_2_COLUMN.put("F29", "S29");//申买量五

        SH_FIELD_2_COLUMN.put("F21", "S21");//申卖量一

        SH_FIELD_2_COLUMN.put("F22", "S22");//申卖价二
        SH_FIELD_2_COLUMN.put("F23", "S23");//申卖量二

        SH_FIELD_2_COLUMN.put("F24", "S24");//申卖价三
        SH_FIELD_2_COLUMN.put("F25", "S25");//申卖量三

        SH_FIELD_2_COLUMN.put("F30", "S30");//申卖价四
        SH_FIELD_2_COLUMN.put("F31", "S31");//申卖量四

        SH_FIELD_2_COLUMN.put("F32", "S32");//申卖价五
        SH_FIELD_2_COLUMN.put("F33", "S33");//申卖量五

        SH_FIELD_2_COLUMN.put("F88", market);//市场   SH/SZ
        SH_FIELD_2_COLUMN.put("F99", lastModified);//行情时间戳 yyyyMMddHHmmss

        SZ_FIELD_2_COLUMN.put("F01", "HQZQDM");//证券代码
        SZ_FIELD_2_COLUMN.put("F02", "HQZQJC");//证券简称
        SZ_FIELD_2_COLUMN.put("F03", "HQZRSP");//昨日收盘价
        SZ_FIELD_2_COLUMN.put("F04", "HQJRKP");//今日开盘价
        SZ_FIELD_2_COLUMN.put("F05", "HQCJJE");//成交金额
        SZ_FIELD_2_COLUMN.put("F06", "HQZGCJ");//最高成交价
        SZ_FIELD_2_COLUMN.put("F07", "HQZDCJ");//最低成交价
        SZ_FIELD_2_COLUMN.put("F08", "HQZJCJ");//最近成交价
        SZ_FIELD_2_COLUMN.put("F09", "HQBJW1");//买价位一/叫买揭示价
        SZ_FIELD_2_COLUMN.put("F10", "HQSJW1");//卖价位一/叫卖揭示价
        SZ_FIELD_2_COLUMN.put("F11", "HQCJSL");//成交数量
        SZ_FIELD_2_COLUMN.put("F13", "HQSYL1");//市盈率 1
        SZ_FIELD_2_COLUMN.put("F14", "HQSYL2");//市盈率2

        SZ_FIELD_2_COLUMN.put("F15", "HQBSL1");//买数量一

        SZ_FIELD_2_COLUMN.put("F16", "HQBJW2");//买价位二
        SZ_FIELD_2_COLUMN.put("F17", "HQBSL2");//买数量二

        SZ_FIELD_2_COLUMN.put("F18", "HQBJW3");//买价位三
        SZ_FIELD_2_COLUMN.put("F19", "HQBSL3");//买数量三


        SZ_FIELD_2_COLUMN.put("F26", "HQBJW4");//买价位四
        SZ_FIELD_2_COLUMN.put("F27", "HQBSL4");//买数量四

        SZ_FIELD_2_COLUMN.put("F28", "HQBJW5");//买价位五
        SZ_FIELD_2_COLUMN.put("F29", "HQBSL5");//买数量五

        SZ_FIELD_2_COLUMN.put("F21", "HQSSL1");//卖数量一

        SZ_FIELD_2_COLUMN.put("F22", "HQSJW2");//卖价位二
        SZ_FIELD_2_COLUMN.put("F23", "HQSSL2");//卖数量二

        SZ_FIELD_2_COLUMN.put("F24", "HQSJW3");//卖价位三
        SZ_FIELD_2_COLUMN.put("F25", "HQSSL3");//卖数量三

        SZ_FIELD_2_COLUMN.put("F30", "HQSJW4");//卖价位四
        SZ_FIELD_2_COLUMN.put("F31", "HQSSL4");//卖数量四

        SZ_FIELD_2_COLUMN.put("F32", "HQSJW5");//卖价位五
        SZ_FIELD_2_COLUMN.put("F33", "HQSSL5");//卖数量五

        SZ_FIELD_2_COLUMN.put("F34", "HQCJBS");//成交笔数
        SZ_FIELD_2_COLUMN.put("F35", "HQJSD1");//价格升跌 1
        SZ_FIELD_2_COLUMN.put("F36", "HQJSD2");//价格升跌 2
        SZ_FIELD_2_COLUMN.put("F37", "HQHYCC");//合约持仓量

        SZ_FIELD_2_COLUMN.put("F88", market);//市场   SH/SZ
        SZ_FIELD_2_COLUMN.put("F99", lastModified);//行情时间戳 yyyyMMddHHmmss

        try {
            init();
        } catch (IOException e) {
            throw new IllegalStateException("不能解析配置文件config.properties");
        }
    }

    public static String getConfig(String key) {
        return properties.getProperty(key);
    }

    private static void init() throws IOException {
        properties.setProperty("database.driver", "com.mysql.jdbc.Driver");
        properties.setProperty("url", "jdbc:mysql://localhost:3306/rzfindb?useUnicode=true&characterEncoding=UTF-8");
        properties.setProperty("username", "root");
        properties.setProperty("password", "12345678");
        properties.setProperty("quote.backup.dir", "daily_quote_backup");
        InputStream is = Thread.currentThread().getClass().getResourceAsStream("/config.properties");
        if (is == null) {
            logger.warn("Warning! Not found [config.properties], use default config!");
        } else {
            properties.load(is);
            is.close();
        }
    }
}
