package com.ruoyi.autotest;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 独立性能压测运行器 — 130并发 /system/notice/list
 * java com.ruoyi.autotest.PerformanceRunner [baseUrl]
 */
public class PerformanceRunner {

    private static final int CONCURRENT = 130;
    private static final int RAMP_UP_SECONDS = 20;

    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0] : "http://localhost";

        System.out.println("目标接口: " + base + "/system/notice/list");
        System.out.println("并发数: " + CONCURRENT + "  |  预热期: " + RAMP_UP_SECONDS + "秒");
        System.out.println("启动中...\n");

        List<Long> rt = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);
        AtomicLong totalBytes = new AtomicLong(0);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT);

        long t0 = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT; i++) {
            final int uid = i + 1;
            executor.submit(() -> {
                try {
                    long delay = (long)(((double)uid / CONCURRENT) * RAMP_UP_SECONDS * 1000);
                    Thread.sleep(delay);
                    startLatch.await();

                    CookieManager cm = new CookieManager();
                    cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                    HttpClient client = HttpClient.newBuilder()
                        .cookieHandler(cm)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                    String enc = StandardCharsets.UTF_8.name();
                    String body = "username=" + URLEncoder.encode("admin", enc)
                        + "&password=" + URLEncoder.encode("admin123", enc)
                        + "&rememberMe=false";

                    HttpRequest loginReq = HttpRequest.newBuilder()
                        .uri(URI.create(base + "/login"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                    HttpResponse<String> loginResp = client.send(loginReq,
                        HttpResponse.BodyHandlers.ofString());

                    if (loginResp.body().contains("\"code\":500")) {
                        failure.incrementAndGet(); return;
                    }

                    String listParams = "pageSize=10&pageNum=1&orderByColumn=createTime&isAsc=desc";
                    Instant reqStart = Instant.now();
                    HttpRequest listReq = HttpRequest.newBuilder()
                        .uri(URI.create(base + "/system/notice/list"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .POST(HttpRequest.BodyPublishers.ofString(listParams))
                        .build();
                    HttpResponse<String> listResp = client.send(listReq,
                        HttpResponse.BodyHandlers.ofString());

                    long elapsed = Duration.between(reqStart, Instant.now()).toMillis();
                    rt.add(elapsed);
                    totalBytes.addAndGet(listResp.body().length());

                    if (listResp.statusCode() == 200
                        && listResp.body().contains("\"code\":0")) {
                        success.incrementAndGet();
                    } else {
                        failure.incrementAndGet();
                    }
                } catch (Exception e) {
                    failure.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        long t1 = System.currentTimeMillis();
        executor.shutdown();

        List<Long> sorted = new ArrayList<>(rt);
        Collections.sort(sorted);

        int total = success.get() + failure.get();
        long avg = sorted.isEmpty() ? 0
            : (long) sorted.stream().mapToLong(Long::longValue).average().orElse(0);
        double tps = total * 1000.0 / (t1 - t0);
        double errRate = total > 0 ? (failure.get() * 100.0 / total) : 0;

        System.out.println("========================================");
        System.out.println("         性能测试结果");
        System.out.println("========================================");
        System.out.printf("  总请求: %d   成功: %d   失败: %d   错误率: %.2f%%%n",
            total, success.get(), failure.get(), errRate);
        System.out.printf("  总耗时: %d ms   吞吐量: %.2f req/s%n", t1 - t0, tps);
        System.out.printf("  传输量: %.2f KB%n", totalBytes.get() / 1024.0);

        if (!sorted.isEmpty()) {
            int size = sorted.size();
            System.out.printf("  平均: %d ms   最小: %d ms   最大: %d ms%n",
                avg, sorted.get(0), sorted.get(size - 1));
            System.out.printf("  中位数: %d ms   P90: %d ms   P95: %d ms   P99: %d ms%n",
                sorted.get(size / 2),
                sorted.get((int)(size * 0.9)),
                sorted.get((int)(size * 0.95)),
                sorted.get((int)(size * 0.99)));
        }
        System.out.println("========================================");
    }
}
