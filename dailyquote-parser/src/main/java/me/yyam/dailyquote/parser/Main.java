package me.yyam.dailyquote.parser;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 接收命令行参数，控制启动扫描线程<br/>
 * <pre>
 * 参数：
 *      args[0]     directory
 *      args[1]     filename1
 *      args[2]     filename2
 *      ...
 * </pre>
 * Created by yyam on 15-4-8.
 */
public class Main {
    private static Log logger = LogFactory.getLog(Main.class);
    private static DataSource dataSource;
    private static Properties config;
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("参数不合法。参数1为文件所在目录，参数2为文件名(多个文件名依次传递)。e.g. java -jar xxx.jar Y: SHOW2003.DBF SJSHQ.DBF");
        }
        Set<String> scanFilenames = new HashSet<>();
        for (int i = 1; i < args.length; i++) {
            scanFilenames.add(args[i]);
        }
        try {
            readConfig();
            buildConnectionPool();
            String today = DateUtils.parse(new Date(), "yyyy-MM-dd");
            String sql = "SELECT COUNT(*) AS num FROM " + Constants.TRADING_DAY_TABLE + " WHERE " + Constants.TRADING_DAY_COL + "='" + today + "'";
            try (
                    Connection connection = getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)
            ) {
                resultSet.next();
                int num = resultSet.getInt("num");
                if (num < 1) {
                    logger.warn("today(" + today + ") is not a trading day");
                    return;
                }
            }
        } catch (Exception e) {
            logger.warn("程序启动过程出现异常", e);
        }

        //clear history directory
        File historyDirectory = new File(Main.getConfig("dbf.backup.dir", "history"));
        logger.debug("main.history.directory:" + historyDirectory.getAbsolutePath());
        if (historyDirectory.exists()) {
            logger.debug("main.history.directory exist, delete!");
            try {
                long startTime = System.currentTimeMillis();
                FileUtils.deleteDirectory(historyDirectory);
                logger.debug("delete history dir cost:" + (System.currentTimeMillis() - startTime) + "ms");
            } catch (IOException e) {
                logger.warn("删除历史文件夹出现异常", e);
            }
        }
        historyDirectory.mkdirs();
        DBFScanner scanner = new DBFScanner(new File(args[0]), scanFilenames);
        Thread t = new Thread(scanner);
        t.start();
    }

    public static DataSource buildConnectionPool() throws IOException, SQLException {
        dataSource = new DruidDataSource();
        ((DruidDataSource) dataSource).setMaxActive(Integer.parseInt(config.getProperty("maxActive")));
        ((DruidDataSource) dataSource).setInitialSize(Integer.parseInt(config.getProperty("initialSize")));
        ((DruidDataSource) dataSource).setMaxWait(Long.parseLong(config.getProperty("maxWait")));
        ((DruidDataSource) dataSource).setUrl(config.getProperty("url"));
        ((DruidDataSource) dataSource).setUsername(config.getProperty("username"));
        ((DruidDataSource) dataSource).setPassword(config.getProperty("password"));
        ((DruidDataSource) dataSource).init();
        return dataSource;
    }

    public static String getConfig(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private static Properties readConfig() throws IOException {
        config = new Properties();
        config.setProperty("url", "jdbc:mysql://localhost:3306/rzfindb?useUnicode=true&characterEncoding=UTF-8");
        config.setProperty("username", "root");
        config.setProperty("password", "12345678");
        config.setProperty("maxActive", "200");
        config.setProperty("initialSize", "80");
        config.setProperty("maxWait", "60000");
        config.setProperty("removeAbandoned", "true");
        config.setProperty("removeAbandonedTimeout", "180");
        config.setProperty("logAbandoned", "true");
        InputStream is = Thread.currentThread().getClass().getResourceAsStream("/config.properties");
        if (is == null) {
            logger.warn("Warning! Not found [config.properties], use default config!");
        } else {
            config.load(is);
            is.close();
        }
        return config;
    }
}
