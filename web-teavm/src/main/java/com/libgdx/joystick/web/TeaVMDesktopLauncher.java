package com.libgdx.joystick.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

final class TeaVMDesktopLauncher {
    private static final int PORT = 8080;
    private static final String APP_URL = "http://localhost:" + PORT + "/";

    private TeaVMDesktopLauncher() {
    }

    static void launch() {
        buildIfNeeded();
        HttpServer server = createServer();
        server.start();
        System.out.println("网页版本已启动，访问地址：" + APP_URL);
        openBrowser();
        keepProcessAlive();
    }

    private static void buildIfNeeded() {
        Path webAppDir = TeaVMPaths.webAppDir();
        if (Files.isDirectory(webAppDir) && Files.isRegularFile(webAppDir.resolve("index.html"))) {
            return;
        }

        System.out.println("没有找到现成的网页资源，开始重新构建……");
        TeaVMBuilder.main(new String[0]);
    }

    private static HttpServer createServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", TeaVMDesktopLauncher::handleRequest);
            server.setExecutor(Executors.newCachedThreadPool());
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("本地网页服务启动失败。", exception);
        }
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        Path root = TeaVMPaths.webAppDir();
        String requestPath = exchange.getRequestURI().getPath();
        String normalized = "/".equals(requestPath) ? "/index.html" : requestPath;
        Path target = root.resolve(normalized.substring(1)).normalize();

        // 只允许访问构建目录里的文件，顺手拦一下路径穿越。
        if (!target.startsWith(root) || Files.isDirectory(target) || !Files.exists(target)) {
            sendNotFound(exchange);
            return;
        }

        byte[] bytes = Files.readAllBytes(target);
        exchange.getResponseHeaders().set("Content-Type", contentType(target));
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void sendNotFound(HttpExchange exchange) throws IOException {
        byte[] bytes = "未找到资源".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String contentType(Path target) throws IOException {
        String detected = Files.probeContentType(target);
        if (detected != null) {
            return detected;
        }

        String fileName = target.getFileName().toString();
        if (fileName.endsWith(".wasm")) {
            return "application/wasm";
        }
        if (fileName.endsWith(".js")) {
            return "application/javascript";
        }
        if (fileName.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (fileName.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private static void openBrowser() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        try {
            Desktop.getDesktop().browse(URI.create(APP_URL));
        } catch (IOException exception) {
            System.err.println("自动打开浏览器失败，请手动访问：" + APP_URL);
            System.err.println("失败原因：" + exception.getMessage());
        }
    }

    private static void keepProcessAlive() {
        try {
            Thread.currentThread().join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
