package com.healthsmart.frontend.controller;

import com.google.gson.Gson;
import com.healthsmart.model.User;
import com.healthsmart.service.UserService;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户API控制器
 * User API Controller
 */
public class UserController {
    
    private static final Gson gson = new Gson();
    private static UserService userService;
    
    public static String handleRequest(String action, String body) {
        if (userService == null) {
            userService = new UserService();
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            switch (action) {
                case "login":
                    response = handleLogin(body);
                    break;
                case "register":
                    response = handleRegister(body);
                    break;
                case "profile":
                    response = handleGetProfile(body);
                    break;
                case "update":
                    response = handleUpdateProfile(body);
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "未知的操作类型");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "处理请求时发生错误: " + e.getMessage());
        }
        
        return gson.toJson(response);
    }
    
    private Map<String, Object> handleLogin(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = (String) data.get("username");
            String password = (String) data.get("password");
            
            User user = userService.login(username, password);
            
            response.put("success", true);
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("email", user.getEmail());
            response.put("data", userData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private Map<String, Object> handleRegister(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = (String) data.get("username");
            String password = (String) data.get("password");
            String email = (String) data.get("email");
            String nickname = (String) data.get("nickname");
            
            User user = userService.register(username, password, email, nickname);
            
            response.put("success", true);
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("email", user.getEmail());
            response.put("data", userData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private Map<String, Object> handleGetProfile(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            User user = userService.getUserById(userId);
            
            response.put("success", true);
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("username", user.getUsername());
            userData.put("nickname", user.getNickname());
            userData.put("email", user.getEmail());
            userData.put("phone", user.getPhone());
            userData.put("gender", user.getGender());
            userData.put("birthDate", user.getBirthDate());
            response.put("data", userData);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
    
    private Map<String, Object> handleUpdateProfile(String body) {
        Map<String, Object> data = gson.fromJson(body, Map.class);
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer userId = ((Number) data.get("userId")).intValue();
            User user = userService.getUserById(userId);
            
            if (data.containsKey("nickname")) {
                user.setNickname((String) data.get("nickname"));
            }
            if (data.containsKey("email")) {
                user.setEmail((String) data.get("email"));
            }
            if (data.containsKey("phone")) {
                user.setPhone((String) data.get("phone"));
            }
            if (data.containsKey("gender")) {
                user.setGender((String) data.get("gender"));
            }
            if (data.containsKey("birthDate")) {
                user.setBirthDate((String) data.get("birthDate"));
            }
            
            boolean success = userService.updateUser(user);
            response.put("success", success);
            response.put("message", success ? "更新成功" : "更新失败");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}