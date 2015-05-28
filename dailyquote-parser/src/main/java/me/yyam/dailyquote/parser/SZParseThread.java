package me.yyam.dailyquote.parser;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yyam on 15-4-24.
 */
public class SZParseThread extends ParseDBFThread {

    /**
     * 构造器
     *
     * @param dbfFile      文件对象
     * @param market       行情所属市场
     * @param lastModified 源文件最后修改时间
     */
    public SZParseThread(File dbfFile, String market, long lastModified) {
        super(dbfFile, market, lastModified);
    }

    /**
     * 深圳市场dbf文件第一行是特殊的记录
     * 第一列是 日期yyyyMMdd
     * 第七列是 时间Hmmss
     * 第一列和第七列组合之后是行情的具体时间
     * @throws UnsupportedEncodingException
     */
    @Override
    public void parseDateAndTime() throws UnsupportedEncodingException {
        Object[] record = dbfReader.nextRecord();
        date = new String((byte[]) record[1], Constants.FILE_ENCODE);
        time = ((Double) record[7]).intValue() + "";
        if (!time.startsWith("1")) {
            time = "0" + time;//深圳行情中时间表示为Hmmss
        }
        closeMarket = ((Double) record[5]).intValue() + "";
    }

    @Override
    public boolean isParsedTime() {
        synchronized (Constants.PARSED_TIME_SZ) {
            if (Constants.PARSED_TIME_SZ.contains(datetime)) {
                return true;
            }
            Constants.PARSED_TIME_SZ.add(datetime);
            return false;
        }
    }

    @Override
    protected String updateLastTime() throws SQLException {
        if (Constants.INSERT_END_SZ) {
            return null;
        }
        Constants.INSERT_END_SZ = Boolean.TRUE;
        return updateLastTime("t_sz_day_last_second", Constants.PRE_SECOND_SZ);
    }

    @Override
    protected String insertOrUpdateLast(String datetime) {
        return super.insertOrUpdateLast("t_sz_minute_last_second", Constants.PARSED_MINUTE_SECOND_MAP_SZ, datetime);
    }

    @Override
    protected String insertParsedSecond(String datetime) {
        return super.insertParsedSecond("t_sz_parsed_second", datetime);
    }

    @Override
    protected void setTradingEnd() {
        Constants.TRADING_END_SZ = Boolean.TRUE;
    }

    @Override
    protected void setPreSecond(String datetime) {
        Constants.PRE_SECOND_SZ = datetime;
        List<String> seconds = null;
        if (Constants.PARSED_MINUTE_SECOND_MAP_SZ.containsKey(dateOfMinute)) {
            seconds = Constants.PARSED_MINUTE_SECOND_MAP_SZ.get(dateOfMinute);
            seconds.add(datetime);
            Collections.sort(seconds, Constants.SECOND_COMPARATOR);
        } else {
            seconds = new ArrayList<>();
            seconds.add(datetime);
            Constants.PARSED_MINUTE_SECOND_MAP_SZ.put(dateOfMinute, seconds);
        }
    }

    @Override
    protected boolean isParsedModified() {
        if (Constants.PARSED_MODIFIED_SZ.contains(lastModified)) {
            return true;
        }
        Constants.PARSED_MODIFIED_SZ.add(lastModified);
        return false;
    }
}
