package com.richeninfo.dailyquote.backup;

import java.io.File;

/**
 * Created by yyam on 15-4-28.
 */
public class BackupSHQuoteThread extends DailyQuoteBackupThread {

    @Override
    protected File createMarketDir(File dayDir) {
        return super.createMarketDir(dayDir, "SH");
    }

    @Override
    protected String generateHeader() {
        return super.generateHeader(Constants.SH_FIELD_2_COLUMN);
    }

    @Override
    protected void setTableName() {
        this.tableName = "t_daily_quote_sh";
    }

    @Override
    protected String truncateSql() {
        return "truncate table t_daily_quote_sh";
    }
}
