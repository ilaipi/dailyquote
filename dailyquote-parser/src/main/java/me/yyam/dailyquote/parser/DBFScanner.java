package me.yyam.dailyquote.parser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

/**
 * 扫描dbf文件的变化，每次变化就复制一份，同时启动线程解析复制的文件
 * Created by yyam on 15-4-8.
 */
public class DBFScanner implements Runnable{
    private Log logger = LogFactory.getLog(DBFScanner.class);
    private File scanDir;
    private Set<String> scanFilenames;

    public DBFScanner(File scanDir, Set<String> scanFilenames) {
        this.scanDir = scanDir;
        this.scanFilenames = scanFilenames;
    }

    @Override
    public void run() {
        WatchService watcher = null;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            logger.debug("watcher is created");
        } catch (IOException e) {
            logger.warn("获取监听服务器出现异常", e);
            return;
        }
        Path file = Paths.get(scanDir.getAbsolutePath());
        try {
            file.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            logger.warn("注册监听事件出现异常", e);
            return;
        }
        while (true) {
            WatchKey key = null;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                logger.warn("捕获事件出现异常", e);
                continue;
            }
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path path = ev.context();
                String filename = path.toFile().getName();
                if (!scanFilenames.contains(filename)) {
                    continue;
                }
                WatchEvent.Kind kind = event.kind();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                File srcFile = new File(scanDir, filename);

                long lastModified = srcFile.lastModified();

                if (lastModified == 0) {
                    continue;
                }

                logger.debug("1----file:" + filename + " event:" + kind.name() + " lastModified:" + lastModified);


                String destFilename = filename.replace(".", "_" + lastModified + ".");

                long currTime = System.currentTimeMillis();
                File destFile = new File(Main.getConfig("dbf.backup.dir", "history") + File.separator + destFilename);
                if (destFile.exists()) {
                    destFile.delete();
                }
//                srcFile.renameTo(destFile);
                try {
                    FileUtils.copyFile(srcFile, destFile);
                } catch (IOException e) {
                    logger.debug("复制文件出现异常", e);
                    continue;
                }
                logger.debug("2----rename [" + destFilename + "] cost:" + (System.currentTimeMillis() - currTime) + "ms");


                ParseDBFThread parser = null;
                switch (Constants.marketMap.get(filename)) {
                    case "SH":
                        parser = new SHParseThread(destFile, "SH", lastModified);
                        break;
                    case "SZ":
                        parser = new SZParseThread(destFile, "SZ", lastModified);
                        break;
                    default:
                        throw new IllegalArgumentException("不支持的文件名" + filename);
                }
                Thread t = new Thread(parser);
                t.start();

            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}
