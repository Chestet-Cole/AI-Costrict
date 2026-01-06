package com.healthsmart.frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

/**
 * 健康智护系统 - 前端主应用程序
 * HealthSmart Desktop Application Main Entry
 */
public class HealthSmartApp extends Application {

    private static final String APP_TITLE = "健康智护 - 个人健康动态管理系统";
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 设置应用标题
            primaryStage.setTitle(APP_TITLE);
            
            // 设置窗口大小
            primaryStage.setWidth(WINDOW_WIDTH);
            primaryStage.setHeight(WINDOW_HEIGHT);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // 创建 WebView 来渲染 HTML 内容
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            
            // 加载 HTML 文件
            String htmlPath = getHtmlFilePath();
            File htmlFile = new File(htmlPath);
            
            if (htmlFile.exists()) {
                webEngine.load("file://" + htmlPath);
            } else {
                webEngine.loadContent(getFallbackHtml());
            }

            // 创建场景
            Scene scene = new Scene(webView, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            // 应用样式
            scene.getStylesheets().add(getCssFilePath());
            
            primaryStage.setScene(scene);
            
            // 设置窗口关闭事件处理
            primaryStage.setOnCloseRequest(this::handleWindowClose);

            // 显示窗口
            primaryStage.show();
            
            System.out.println("健康智护系统启动成功！");
            System.out.println("HTML 路径: " + htmlPath);
            
        } catch (Exception e) {
            System.err.println("应用启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取 HTML 文件路径
     */
    private String getHtmlFilePath() {
        String projectDir = System.getProperty("user.dir");
        // 优先查找前端目录，否则使用后端web目录
        File frontendHtml = new File(projectDir, "frontend/web/index.html");
        if (frontendHtml.exists()) {
            return frontendHtml.getAbsolutePath();
        }
        File backendHtml = new File(projectDir, "web/index.html");
        if (backendHtml.exists()) {
            return backendHtml.getAbsolutePath();
        }
        return projectDir + "/web/index.html";
    }

    /**
     * 获取 CSS 文件路径
     */
    private String getCssFilePath() {
        String projectDir = System.getProperty("user.dir");
        File cssFile = new File(projectDir, "frontend/web/css/app.css");
        if (cssFile.exists()) {
            return "file://" + cssFile.getAbsolutePath();
        }
        return "file://" + projectDir + "/web/css/app.css";
    }

    /**
     * 获取备用 HTML 内容（当文件不存在时）
     */
    private String getFallbackHtml() {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"zh-CN\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>健康智护 - 加载中...</title>\n" +
               "    <style>\n" +
               "        body { font-family: 'Microsoft YaHei', sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }\n" +
               "        .container { max-width: 1200px; margin: 0 auto; }\n" +
               "        .loading { text-align: center; padding: 50px; }\n" +
               "        .spinner { width: 40px; height: 40px; border: 4px solid #e0e0e0; border-top-color: #4CAF50; border-radius: 50%; animation: spin 1s linear infinite; margin: 20px auto; }\n" +
               "        @keyframes spin { to { transform: rotate(360deg); } }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <div class=\"loading\">\n" +
               "            <h1>健康智护系统</h1>\n" +
               "            <p>正在加载应用...</p>\n" +
               "            <div class=\"spinner\"></div>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }

    /**
     * 处理窗口关闭事件
     */
    private void handleWindowClose(WindowEvent event) {
        System.out.println("正在关闭健康智护系统...");
        cleanupResources();
    }

    /**
     * 清理资源
     */
    private void cleanupResources() {
        try {
            com.healthsmart.util.DatabaseConnection.closeConnection();
            System.out.println("数据库连接已关闭");
        } catch (Exception e) {
            System.err.println("关闭数据库连接时出错: " + e.getMessage());
        }
    }

    /**
     * 应用入口点
     */
    public static void main(String[] args) {
        launch(args);
    }
}