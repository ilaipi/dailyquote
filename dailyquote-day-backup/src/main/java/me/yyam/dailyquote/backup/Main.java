package com.richeninfo.dailyquote.backup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by yyam on 15-4-28.
 */
public class Main {
    private static Log logger = LogFactory.getLog(Main.class);
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DailyQuoteBackupThread sh = new BackupSHQuoteThread();
        if (args.length == 1) {
            sh.setDays(args[0]);
        }
        Thread tsh = new Thread(sh);
        tsh.start();
        logger.debug("上海线程启动");

        DailyQuoteBackupThread sz = new BackupSZQuoteThread();
        if (args.length == 1) {
            sz.setDays(args[0]);
        }
        Thread tsz = new Thread(sz);
        tsz.start();
        logger.debug("深圳线程启动");
    }

    public static File getBaseDir() {
        File baseDir = new File(Constants.getConfig("quote.backup.dir"));
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        return baseDir;
    }

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(Constants.getConfig("database.driver"));
        String url = Constants.getConfig("url");
        String username = Constants.getConfig("username");
        String password = Constants.getConfig("password");
        return DriverManager.getConnection(url, username, password);
    }
}
