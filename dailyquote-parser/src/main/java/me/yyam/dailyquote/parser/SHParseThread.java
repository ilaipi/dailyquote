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
public class SHParseThread extends ParseDBFThread {

    /**
     * 构造器
     *
     * @param dbfFile      文件对象
     * @param market       行情所属市场
     * @param lastModified 源文件最后修改时间
     */
    public SHParseThread(File dbfFile, String market, long lastModified) {
        super(dbfFile, market, lastModified);
    }

    /**
     * 上证的dbf文件前面一些行被隐藏了，在dbf文件中被标记为×开头，认为是删除的行
     * 第一行是时间戳行，以特定的字节读取，可以通过16进制打开dbf查看
     * @throws UnsupportedEncodingException
     */
    @Override
    public void parseDateAndTime() throws UnsupportedEncodingException {
        dbfReader.skipBytes(7);
        byte[] timeBytes = dbfReader.readBytes(6);
        time = new String(timeBytes, Constants.FILE_ENCODE);
        dbfReader.skipBytes(30);
        byte[] dateBytes = dbfReader.readBytes(8);
        date = new String(dateBytes, Constants.FILE_ENCODE);
        dbfReader.skipBytes(32);
        closeMarket = new String(dbfReader.readBytes(10), Constants.FILE_ENCODE);
        dbfReader.skipBytes(172);
    }

    @Override
    public boolean isParsedTime() {
        synchronized (Constants.PARSED_TIME_SH) {
            if (Constants.PARSED_TIME_SH.contains(datetime)) {
                return true;
            }
            Constants.PARSED_TIME_SH.add(datetime);
            return false;
        }
    }

    @Override
    protected void setEnd() {
        super.setEnd();
        end -= 1;
    }

    @Override
    protected String updateLastTime() throws SQLException {
        if (Constants.INSERT_END_SH) {
            return null;
        }
        Constants.INSERT_END_SH = Boolean.TRUE;
        return updateLastTime("t_sh_day_last_second", Constants.PRE_SECOND_SH);
    }

    @Override
    protected String insertOrUpdateLast(String datetime) {
        return super.insertOrUpdateLast("t_sh_minute_last_second", Constants.PARSED_MINUTE_SECOND_MAP_SH, datetime);
    }

    @Override
    protected String insertParsedSecond(String datetime) {
        return super.insertParsedSecond("t_sh_parsed_second", datetime);
    }

    @Override
    protected void setTradingEnd() {
        Constants.TRADING_END_SH = Boolean.TRUE;
    }

    @Override
    protected void setPreSecond(String datetime) {
        Constants.PRE_SECOND_SH = datetime;
        List<String> seconds = null;
        if (Constants.PARSED_MINUTE_SECOND_MAP_SH.containsKey(dateOfMinute)) {
            seconds = Constants.PARSED_MINUTE_SECOND_MAP_SH.get(dateOfMinute);
            seconds.add(datetime);
            Collections.sort(seconds, Constants.SECOND_COMPARATOR);
        } else {
            seconds = new ArrayList<>();
            seconds.add(datetime);
            Constants.PARSED_MINUTE_SECOND_MAP_SH.put(dateOfMinute, seconds);
        }
    }

    @Override
    protected boolean isParsedModified() {
        if (Constants.PARSED_MODIFIED_SH.contains(lastModified)) {
            return true;
        }
        Constants.PARSED_MODIFIED_SH.add(lastModified);
        return false;
    }
}
