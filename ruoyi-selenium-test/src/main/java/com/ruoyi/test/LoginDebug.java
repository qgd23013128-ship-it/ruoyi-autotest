package com.ruoyi.test;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginDebug {
    public static void main(String[] args) throws Exception {
        CookieManager cm = new CookieManager();
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        HttpClient c = HttpClient.newBuilder()
                .cookieHandler(cm)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        String encode = StandardCharsets.UTF_8.name();
        String formBody = "username=" + URLEncoder.encode("admin", encode)
                + "&password=" + URLEncoder.encode("admin123", encode)
                + "&rememberMe=false";

        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:80/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();
        HttpResponse<String> loginResp = c.send(loginReq, BodyHandlers.ofString());
        System.out.println("Login: " + loginResp.body());

        String listParams = "pageSize=10&pageNum=1&orderByColumn=createTime&isAsc=desc";
        HttpRequest noticeReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:80/system/notice/list"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Requested-With", "XMLHttpRequest")
                .POST(HttpRequest.BodyPublishers.ofString(listParams))
                .build();
        HttpResponse<String> noticeResp = c.send(noticeReq, BodyHandlers.ofString());
        System.out.println("Notice list status: " + noticeResp.statusCode());
        String body = noticeResp.body();
        System.out.println("Body length: " + body.length());
        if (body.length() > 100) {
            System.out.println("Body (first 300): " + body.substring(0, Math.min(300, body.length())));
        } else {
            System.out.println("Body: " + body);
        }
    }
}
