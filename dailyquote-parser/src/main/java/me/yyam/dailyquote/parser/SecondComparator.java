package me.yyam.dailyquote.parser;

import java.util.Comparator;

/**
 * Created by yyam on 15-4-27.
 */
public class SecondComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        int o1Second = Integer.parseInt(o1.substring(12, 14));
        int o2Second = Integer.parseInt(o2.substring(12, 14));

        return o1Second - o2Second;
    }
}
