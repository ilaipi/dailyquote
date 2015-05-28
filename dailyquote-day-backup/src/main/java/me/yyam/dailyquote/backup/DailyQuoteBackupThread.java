package com.richeninfo.dailyquote.backup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by yyam on 15-4-28.
 */
public abstract class DailyQuoteBackupThread implements Runnable {
    protected Log logger = LogFactory.getLog(this.getClass());

    protected String tableName;

    protected String days;

    protected File marketDir;
    protected File csvFile;

    protected ResultSetMetaData metaData;

    protected String generateSelectCodeSql(String quoteDay) {
        return generateSelectCodeSql(tableName, quoteDay);
    }

    protected abstract File createMarketDir(File dayDir);

    protected abstract String generateHeader();

    protected abstract void setTableName();

    protected abstract String truncateSql();

    public void setDays(String days) {
        this.days = days;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        setTableName();
        //distinct left(F99,8)
        String selectSql = generateSelectDateSql(tableName);
        logger.debug(tableName + "----generate date sql:" + selectSql);
        ResultSet quoteRs = null;
        BufferedWriter writer = null;
        ResultSet codeRs = null;
        long currTime = System.currentTimeMillis();
        try (
                Connection connection = Main.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(selectSql)
        ) {
            File dayDir = null;
            File marketDir = null;
            boolean hasData = false;
            //首先查询所有的交易日
            //在程序按照计划每天执行的时候，这里每次应该只有一个交易日
            //如果数据库中意外有多个交易日的数据，可以通过命令行来指定备份哪个交易日
            //如果不指定，则每个交易日都备份一遍
            Set<String> quoteDays = new HashSet<>();
            if (days != null && days.length() > 0) {
                for (String day : days.split(",")) {
                    quoteDays.add(day);
                }
            } else {
                while (rs.next()) {
                    quoteDays.add(rs.getString(1));
                }
            }
            logger.debug(tableName + "----get connection and query date list cost:" + (System.currentTimeMillis() - currTime) + "ms");
            logger.debug(tableName + "----date list:" + Arrays.toString(quoteDays.toArray()));

            //遍历每个交易日
            //首先查出该交易日所有的股票代码
            //然后遍历股票代码，以股票代码+日期为条件查询，把每支股票的当天行情备份到文件
            for (String quoteDay : quoteDays) {
                Set<String> stockCodes = new HashSet<>();

                //create day dir
                dayDir = createDayDir(quoteDay);
                marketDir = createMarketDir(dayDir);
                logger.debug(tableName + "create directory：" + marketDir.getAbsolutePath());
                currTime = System.currentTimeMillis();
                selectSql = generateSelectCodeSql(quoteDay);
                codeRs = stmt.executeQuery(selectSql);
                logger.debug(tableName + "----select " + quoteDay + " codes cost:" + (System.currentTimeMillis() - currTime) + "ms");

                String header = null;
                while (codeRs.next()) {
                    stockCodes.add(codeRs.getString(1));
                }
                for (String stockCode : stockCodes) {
                    hasData = true;
                    currTime = System.currentTimeMillis();
                    //select daily quote
                    selectSql = generateSelectSql(tableName, quoteDay, stockCode);
                    logger.debug(tableName + "-----select sql:" + selectSql);
                    quoteRs = stmt.executeQuery(selectSql);
                    logger.debug(tableName + "----select " + quoteDay + "." + stockCode + " data cost:" + (System.currentTimeMillis() - currTime) + "ms");
                    metaData = quoteRs.getMetaData();
                    int columnNum = metaData.getColumnCount();
                    if (header == null) {
                        header = generateHeader();
                    }
                    int dataNum = 1;
                    createCsvByStockCode(marketDir, stockCode);
                    writer = new BufferedWriter(new FileWriter(csvFile));
                    writer.write(header);
                    writer.newLine();
                    while (quoteRs.next()) {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append(quoteRs.getObject(1));
                        for (int i = 2; i <= columnNum; i++) {
                            buffer.append(",").append(quoteRs.getObject(i));
                        }
                        writer.write(buffer.toString());
                        writer.newLine();
                        if (dataNum % 500 == 0) {
                            writer.flush();
                        }
                        dataNum++;
                    }
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                }
            }


            if (hasData) {
                //truncate
                stmt.execute(truncateSql());
            }
        } catch (Exception e) {
            logger.warn("备份出现异常", e);
        } finally {
            logger.info(tableName + "----end cost:" + (System.currentTimeMillis() - startTime) + "ms");
            if (quoteRs != null) {
                try {
                    quoteRs.close();
                } catch (SQLException e) {
                    logger.warn("关闭行情数据结果集出现异常", e);
                }
            }
            if (codeRs != null) {
                try {
                    codeRs.close();
                } catch (SQLException e) {
                    logger.warn("关闭股票代码结果集出现异常", e);
                }
            }

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.warn("关闭文件输出流出现异常", e);
                }
            }
        }
    }

    private File createDayDir(String quoteDay) {
        File dayDir = new File(Main.getBaseDir(), quoteDay);
        if (!dayDir.exists()) {
            dayDir.mkdirs();
        }
        return dayDir;
    }

    protected String generateHeader(Map<String, String> field2Column) {
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append(field2Column.get(metaData.getColumnName(1)));
            for (int i = 2; i <= metaData.getColumnCount(); i++) {
                buffer.append(",").append(field2Column.get(metaData.getColumnName(i)));
            }
            return buffer.toString();
        } catch (Exception e) {
            throw new IllegalStateException("不能解析结果集的元数据", e);
        }
    }

    protected String generateSelectSql(String tableName, String quoteDay, String code) {
        return "select * from " + tableName + " where F99 like '" + quoteDay + "%' and F01='" + code + "'";
    }

    protected String generateSelectDateSql(String tableName) {
        return "select distinct left(F99, 8) from " + tableName;
    }

    protected String generateSelectCodeSql(String tableName, String quoteDay) {
        return "select distinct F01 from " + tableName + " where F99 like '" + quoteDay + "1300%'";//这个时间是第一次运行时数据库中数据的最小日期
    }

    private void createCsvByStockCode(File marketDir, String stockCode) {
        csvFile = new File(marketDir, stockCode + ".csv");
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("不能创建目标文件" + csvFile.getAbsolutePath(), e);
            }
        }
    }

    protected File createMarketDir(File dayDir, String market) {
        marketDir = new File(dayDir, market);
        if (!marketDir.exists()) {
            marketDir.mkdirs();
        }
        return marketDir;
    }
}
