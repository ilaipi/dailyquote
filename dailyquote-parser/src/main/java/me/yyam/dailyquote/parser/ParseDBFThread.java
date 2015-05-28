package me.yyam.dailyquote.parser;

import me.yyam.dbf.DbfReader;
import me.yyam.dbf.structure.DbfHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.*;

/**
 * Created by yyam on 15-4-8.
 */
public abstract class ParseDBFThread implements Runnable{
    protected Log logger = LogFactory.getLog(this.getClass());
    protected String filename;//非文件的原名，是mv操作后的目标文件的名字
    protected File dbfFile;
    protected DbfReader dbfReader;
    protected DbfHeader dbfHeader;
    protected List<String> dbfFields;
    protected String market;

    protected String date;//文件内的日期字段
    protected String time;//文件内的时间字段
    /**
     * 原本想用这个字段来判断是否收盘，发现效果不好。可以删除
     */
    @Deprecated
    protected String closeMarket;
    protected String datetime;//date + time
    protected Integer end;//文件最后一行的行号。因为上海的文件内最后一行数据不要
    protected Integer hour;//文件内时间的小时
    protected Integer minute;//文件内时间的分钟

    protected String dateOfMinute;//把时间截取到分钟

    protected long lastModified;//文件的最后修改时间

    /**
     * 构造器
     * @param dbfFile       文件对象
     * @param market        行情所属市场
     * @param lastModified  源文件最后修改时间
     */
    public ParseDBFThread(File dbfFile, String market, long lastModified) {
        this.dbfFile = dbfFile;
        this.market = market;
        this.lastModified = lastModified;
    }

    public void init() {
        long currTime = System.currentTimeMillis();
        this.dbfReader = new DbfReader(this.dbfFile);
        this.dbfHeader = dbfReader.getHeader();
        this.dbfFields = new ArrayList<>();
        for (int i = 0; i < dbfHeader.getFieldsCount(); i++) {
            dbfFields.add(dbfHeader.getField(i).getName());
        }
        this.filename = dbfFile.getName();
        logger.debug("3----open[" + this.filename + "] and parse header cost:" + (System.currentTimeMillis() - currTime) + "ms");
    }

    /**
     * 解析文件中的日期和时间字段
     * @throws UnsupportedEncodingException
     */
    public abstract void parseDateAndTime() throws UnsupportedEncodingException;

    /**
     * 判断得到的时间戳是否解析过
     * @return
     */
    public abstract boolean isParsedTime();

    /**
     * 处理上海市场的文件最后一行
     */
    protected void setEnd(){
        end = dbfReader.getRecordCount();
    }

    /**
     * 每天收盘后执行一次，触发日行情的触发器，抽取日行情
     * @return
     * @throws SQLException
     */
    protected abstract String updateLastTime() throws SQLException;

    /**
     * 每个文件都要执行一次，保证分钟行情数据不丢并且及时，为触发分钟行情的触发器，抽取分钟行情
     * @param datetime
     * @return
     */
    protected abstract String insertOrUpdateLast(String datetime);

    /**
     * 没有特别意义，只为记录解析过的时间戳
     * @param datetime
     * @return
     */
    protected abstract String insertParsedSecond(String datetime);

    protected abstract void setTradingEnd();

    /**
     * 认为15:01后收盘
     * @param time
     * @return
     */
    protected boolean isTradingEnd(String time) {
        if (time.startsWith("1501")) {
            return true;
        }
        return false;
    }

    protected abstract void setPreSecond(String datetime);

    protected abstract boolean isParsedModified();

    @Override
    public void run() {
        if (isParsedModified()) {
            return;
        }
        init();
        String insertPrefix = buildInsertPrefix();
        long currTime = System.currentTimeMillis();
        Connection connection = null;
        Statement stmt = null;
        try {
            connection = Main.getConnection();
            stmt = connection.createStatement();
            logger.debug("4----[" + filename + "] get connection cost:" + (System.currentTimeMillis() - currTime) + "ms");
            connection.setAutoCommit(false);
            currTime = System.currentTimeMillis();

            parseDateAndTime();
            datetime = date + time;
            dateOfMinute = datetime.substring(0, 12);

            if (isParsedTime()) {
                logger.debug("5----has parsed [" + filename + "] datetime:" + datetime);
                return;
            }
            closeMarket = closeMarket.trim();
            logger.debug("5----[" + filename + "] datetime:" + datetime + " close market:" + closeMarket);

            setEnd();

            if (!tradingTime(time)) {//非交易时间
                logger.warn("6----[" + filename + "] time:" + time + " is not trading time!");

                //if close market then exit
                if (isTradingEnd(time)) {
                    setTradingEnd();
                    String sql = updateLastTime();
                    if (sql != null) {
                        stmt.execute(sql);
                        connection.commit();
                    }
                    //两个市场都收盘了才能退出程序
                    if (Constants.TRADING_END_SZ && Constants.TRADING_END_SH) {
                        logger.warn("it's time:" + time + ", system exit!");
                        System.exit(0);
                    }
                }
                return;
            }

            int num = 0;
            for (int i = 1; i < end; i++) {
                Object[] record = dbfReader.nextRecordIgnoreDeleteAndFieldError();
                if (record == null) {
                    break;
                }
                num++;
                stmt.addBatch(buildInsertValues(insertPrefix, record, datetime));
            }
            logger.debug("6----parse [" + filename + "] record and generate sql cost:" + (System.currentTimeMillis() - currTime) + "ms sql num:" + num);
            currTime = System.currentTimeMillis();
            stmt.executeBatch();
            connection.commit();
            logger.debug("7----commit [" + filename + "] cost:" + (System.currentTimeMillis() - currTime) + "ms");
            stmt.clearBatch();

            //这个锁是为了避免多个线程引起的数据库异常
            //java.sql.BatchUpdateException: Lock wait timeout exceeded; try restarting transaction
            //这段代码已经不影响整个线程的效率，所以在这里加一个统一的对象锁，所有线程共享同一个锁
            //这样就再也不会出现这个异常了

            //网上说这个异常是因为一个事务在操作一条记录还没有提交的时候
            //另一个事务要操作同一条记录
            //这时候就会出现这个异常

            //这里出现这个异常，可能是因为一个上海的线程要做insert或update
            //而后面的线程执行的比较快也到了这里
            //后面的这个线程可能就抛异常了
            synchronized (Constants.SECOND_COMPARATOR) {
                //insert datetime to t_s*_parsed_second
                stmt.execute(insertParsedSecond(datetime));
                connection.commit();

                String sql = insertOrUpdateLast(datetime);
                if (sql != null) {
                    stmt.execute(sql);
                    connection.commit();
                }
            }

            setPreSecond(datetime);
        } catch (Exception e) {
            logger.warn("解析文件出现异常[" + filename + "]", e);
        } finally {
            dbfReader.close();
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.warn("关闭stmt出现异常", e);
            }
            try {
                connection.close();
            } catch (SQLException e) {
                logger.warn("关闭数据库连接出现异常", e);
            }
            FileUtils.deleteQuietly(dbfFile);
        }
    }

    protected String insertOrUpdateLast(String tableName, Map<String, List<String>> parsedMinuteSecondMap, String datetime) {
        StringBuffer buffer = new StringBuffer();
        if (parsedMinuteSecondMap.containsKey(dateOfMinute)) {
            //update the same minute with the datetime
            //如果前面更新的时间大于当前的时间，就不做更新操作
            String preDatetime = parsedMinuteSecondMap.get(dateOfMinute).get(parsedMinuteSecondMap.get(dateOfMinute).size() - 1);
            if (Constants.SECOND_COMPARATOR.compare(preDatetime, datetime) >= 0) {
                return null;
            }
            buffer.append("update ").append(tableName).append(" set last_second='").append(datetime).append("'");
            buffer.append(" where left(last_second, 12)='").append(dateOfMinute).append("'");
            return buffer.toString();
        }
        //insert the minute
        buffer.append("insert into ").append(tableName).append(" values('").append(datetime).append("')");
        return buffer.toString();
    }

    protected String insertParsedSecond(String tableName, String datetime) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("insert into ").append(tableName).append("(parsed_second, parse_time) values('");
        buffer.append(datetime).append("', now())");
        return buffer.toString();
    }

    protected String updateLastTime(String tableName, String lastTime) throws SQLException {
        return "INSERT INTO " + tableName + "(last_second) VALUES ('" + lastTime + "')";
    }

    /**
     * 判断是否交易时间
     * @param time  format: HHmmss
     * @return
     * @throws ParseException
     */
    private Boolean tradingTime(String time) throws ParseException {
        Date now = DateUtils.format(time, "HHmmss");
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        if (
                (hour >= 9 && hour <= 10) ||
                        (hour == 11 && minute <= 30) ||
                        (hour >= 13 && hour < 15) ||
                        (hour == 15 && minute < 1)
                ) {
            return true;
        }
        return false;
    }

    /**
     * 拼接insert语句的values部分
     * @param insertPrefix
     * @param record
     * @param datetime
     * @return
     * @throws UnsupportedEncodingException
     */
    private String buildInsertValues(String insertPrefix, Object[] record, String datetime) throws UnsupportedEncodingException {
        StringBuffer buffer = new StringBuffer(insertPrefix);
        buffer.append("'").append(new String((byte[]) record[0], "GBK")).append("', '")
                .append(new String((byte[]) record[1], "GBK")).append("'");
        for (int i = 2; i < record.length; i++) {
            buffer.append(", ").append(record[i]);
        }
        buffer.append(", '").append(market).append("', '").append(datetime).append("')");
        return buffer.toString();
    }

    /**
     * 拼接insert语句的insert into部分
     * @return
     */
    public String buildInsertPrefix() {
        StringBuffer buffer = new StringBuffer("INSERT INTO ");
        buffer.append(Constants.market2Table.get(this.market)).append("(");
        buffer.append(Constants.get(dbfFields.get(0)));
        for (int i = 1; i < dbfFields.size(); i++) {
            buffer.append(", ").append(Constants.get(dbfFields.get(i)));
        }
        buffer.append(", ").append(Constants.get(Constants.market));
        buffer.append(", ").append(Constants.get(Constants.lastModified));
        buffer.append(") values(");
        return buffer.toString();
    }
}
