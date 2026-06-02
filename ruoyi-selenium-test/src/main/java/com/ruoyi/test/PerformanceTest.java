package com.ruoyi.test;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
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

public class PerformanceTest {

    private static final String BASE_URL = "http://localhost:80";
    private static final int CONCURRENT_USERS = 130;
    private static final int RAMP_UP_SECONDS = 20;

    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("  若依通知公告列表 性能测试");
        System.out.println("  目标接口: /system/notice/list");
        System.out.println("  并发用户数: " + CONCURRENT_USERS);
        System.out.println("  预热时间: " + RAMP_UP_SECONDS + "秒");
        System.out.println("========================================");

        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalBytes = new AtomicLong(0);

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_USERS);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i + 1;
            executor.submit(() -> {
                try {
                    long delayMs = (long) (((double) userId / CONCURRENT_USERS) * RAMP_UP_SECONDS * 1000);
                    Thread.sleep(delayMs);
                    startLatch.await();

                    CookieManager cookieManager = new CookieManager();
                    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

                    HttpClient client = HttpClient.newBuilder()
                            .cookieHandler(cookieManager)
                            .connectTimeout(Duration.ofSeconds(10))
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();

                    String encode = StandardCharsets.UTF_8.name();
                    String formBody = "username=" + URLEncoder.encode("admin", encode)
                            + "&password=" + URLEncoder.encode("admin123", encode)
                            + "&rememberMe=false";

                    HttpRequest loginRequest = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/login"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(HttpRequest.BodyPublishers.ofString(formBody))
                            .build();

                    HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

                    if (loginResponse.body().contains("\"code\":500")) {
                        failureCount.incrementAndGet();
                        return;
                    }

                    String listParams = "pageSize=10&pageNum=1&orderByColumn=createTime&isAsc=desc";

                    Instant reqStart = Instant.now();
                    HttpRequest noticeRequest = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/system/notice/list"))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("X-Requested-With", "XMLHttpRequest")
                            .POST(HttpRequest.BodyPublishers.ofString(listParams))
                            .build();

                    HttpResponse<String> noticeResponse = client.send(noticeRequest, HttpResponse.BodyHandlers.ofString());

                    long elapsedMs = Duration.between(reqStart, Instant.now()).toMillis();
                    responseTimes.add(elapsedMs);
                    totalBytes.addAndGet(noticeResponse.body().length());

                    String body = noticeResponse.body();
                    if (noticeResponse.statusCode() == 200 && body.contains("\"code\":0")) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();

        long endTime = System.currentTimeMillis();
        executor.shutdown();

        List<Long> sorted = new ArrayList<>(responseTimes);
        Collections.sort(sorted);

        int total = successCount.get() + failureCount.get();
        double avg = sorted.isEmpty() ? 0 : sorted.stream().mapToLong(Long::longValue).average().orElse(0);
        double throughput = (endTime - startTime) > 0
                ? (total * 1000.0 / (endTime - startTime))
                : 0;
        double errorRate = total > 0 ? (failureCount.get() * 100.0 / total) : 0;

        System.out.println();
        System.out.println("========================================");
        System.out.println("  性能测试结果");
        System.out.println("========================================");
        System.out.println("  总请求数:        " + total);
        System.out.println("  成功数:           " + successCount.get());
        System.out.println("  失败数:           " + failureCount.get());
        System.out.println("  错误率:           " + String.format("%.2f%%", errorRate));
        System.out.println("  总耗时:           " + (endTime - startTime) + " ms");
        System.out.println("  吞吐量:           " + String.format("%.2f", throughput) + " req/s");
        System.out.println("  总传输量:         " + String.format("%.2f", totalBytes.get() / 1024.0) + " KB");
        if (!sorted.isEmpty()) {
            System.out.println("  平均响应时间:     " + String.format("%.0f", avg) + " ms");
            System.out.println("  最小响应时间:     " + sorted.get(0) + " ms");
            System.out.println("  最大响应时间:     " + sorted.get(sorted.size() - 1) + " ms");
            System.out.println("  中位数(50%):      " + sorted.get(sorted.size() / 2) + " ms");
            int p90Idx = (int) (sorted.size() * 0.9);
            System.out.println("  P90:              " + sorted.get(Math.min(p90Idx, sorted.size() - 1)) + " ms");
            int p95Idx = (int) (sorted.size() * 0.95);
            System.out.println("  P95:              " + sorted.get(Math.min(p95Idx, sorted.size() - 1)) + " ms");
            int p99Idx = (int) (sorted.size() * 0.99);
            System.out.println("  P99:              " + sorted.get(Math.min(p99Idx, sorted.size() - 1)) + " ms");
        }
        System.out.println("========================================");
    }
}
