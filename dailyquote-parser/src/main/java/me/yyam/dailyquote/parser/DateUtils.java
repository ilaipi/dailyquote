package me.yyam.dailyquote.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yyam on 15-4-17.
 */
public class DateUtils {
    private static final ThreadLocal<Map<String, SimpleDateFormat>> FORMATTER_POOL = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        @Override
        protected Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<>();
        }
    };

    private static SimpleDateFormat getFormatter(String format) {
        if (FORMATTER_POOL.get().containsKey(format)) {
            return FORMATTER_POOL.get().get(format);
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        FORMATTER_POOL.get().put(format, formatter);
        return formatter;
    }

    public static String parse(Date date, String format) {
        return getFormatter(format).format(date);
    }

    public static Date format(String date, String format) {
        try {
            return getFormatter(format).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("非法的日期格式:" + format, e);
        }
    }
}
