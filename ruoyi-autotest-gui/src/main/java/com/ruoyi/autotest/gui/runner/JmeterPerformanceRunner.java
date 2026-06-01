package com.ruoyi.autotest.gui.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

/**
 * JMeter performance test runner for dict type list API (110 threads).
 */
public final class JmeterPerformanceRunner {

    private static final int THREAD_COUNT = 110;

    private JmeterPerformanceRunner() {
    }

    public static void runDictTypeListTest(String baseUrl, Consumer<String> logger) {
        String jmeterHome = findJmeterHome();
        if (jmeterHome == null) {
            logger.accept("[2.3 Performance] JMeter not found. Install to C:\\apache-jmeter or set JMETER_HOME.");
            return;
        }

        File scriptFile = resolveJmeterScript();
        if (scriptFile == null || !scriptFile.exists()) {
            logger.accept("[2.3 Performance] JMX script dict_type_performance_test.jmx not found.");
            return;
        }

        logger.accept("[2.3 Performance] JMeter home: " + jmeterHome);
        logger.accept("[2.3 Performance] Script: " + scriptFile.getAbsolutePath());
        logger.accept("[2.3 Performance] Target: POST " + baseUrl + "/system/dict/list (110 threads)");

        File resultFile = new File("jmeter_dict_result.jtl");
        File jmeterLog = new File("jmeter_dict_run.log");

        ProcessBuilder pb = new ProcessBuilder(
            jmeterHome + File.separator + "bin" + File.separator + "jmeter.bat",
            "-n",
            "-t", scriptFile.getAbsolutePath(),
            "-l", resultFile.getAbsolutePath(),
            "-j", jmeterLog.getAbsolutePath(),
            "-Jbase_url=" + baseUrl,
            "-Jthread_count=" + THREAD_COUNT
        );
        pb.redirectErrorStream(true);

        try {
            logger.accept("[2.3 Performance] Running load test...");
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        logger.accept("[JMeter] " + line);
                    }
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.accept("[2.3 Performance] Done. Result file: " + resultFile.getAbsolutePath());
                summarizeResult(resultFile, logger);
            } else {
                logger.accept("[2.3 Performance] Failed. Exit code: " + exitCode);
                logger.accept("[2.3 Performance] Log file: " + jmeterLog.getAbsolutePath());
            }
        } catch (Exception ex) {
            logger.accept("[2.3 Performance] Error: " + ex.getMessage());
        }
    }

    private static void summarizeResult(File resultFile, Consumer<String> logger) {
        if (!resultFile.exists()) {
            return;
        }
        int total = 0;
        int success = 0;
        try {
            for (String line : Files.readAllLines(resultFile.toPath())) {
                if (line.startsWith("timeStamp") || line.isBlank()) {
                    continue;
                }
                total++;
                if (line.contains(",true,")) {
                    success++;
                }
            }
        } catch (IOException ignored) {
            return;
        }
        logger.accept("[2.3 Performance] Samples=" + total + ", success=" + success + ", failed=" + (total - success));
    }

    private static File resolveJmeterScript() {
        String[] candidates = {
            "src/main/resources/jmeter/dict_type_performance_test.jmx",
            "ruoyi-autotest-gui/src/main/resources/jmeter/dict_type_performance_test.jmx",
            "RuoYi/sql/dict_type_performance_test.jmx",
            "../RuoYi/sql/dict_type_performance_test.jmx"
        };
        for (String path : candidates) {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }

        try (var in = JmeterPerformanceRunner.class.getResourceAsStream("/jmeter/dict_type_performance_test.jmx")) {
            if (in != null) {
                Path temp = Files.createTempFile("dict_type_performance_test", ".jmx");
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                temp.toFile().deleteOnExit();
                return temp.toFile();
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private static String findJmeterHome() {
        String envHome = System.getenv("JMETER_HOME");
        if (envHome != null && !envHome.isEmpty()) {
            File f = new File(envHome, "bin/jmeter.bat");
            if (f.exists()) {
                return envHome;
            }
        }

        String[] searchDirs = {
            "C:\\apache-jmeter\\apache-jmeter-5.6.3",
            "C:\\apache-jmeter\\apache-jmeter-5.6.2",
            "C:\\apache-jmeter\\apache-jmeter-5.5",
            "D:\\apache-jmeter\\apache-jmeter-5.6.3"
        };

        for (String dir : searchDirs) {
            File f = new File(dir, "bin/jmeter.bat");
            if (f.exists()) {
                return dir;
            }
        }

        for (String root : new String[]{"C:\\apache-jmeter", "D:\\apache-jmeter"}) {
            File rootDir = new File(root);
            File[] subs = rootDir.listFiles();
            if (subs == null) {
                continue;
            }
            for (File sub : subs) {
                File f = new File(sub, "bin/jmeter.bat");
                if (f.exists()) {
                    return sub.getAbsolutePath();
                }
            }
        }
        return null;
    }
}
