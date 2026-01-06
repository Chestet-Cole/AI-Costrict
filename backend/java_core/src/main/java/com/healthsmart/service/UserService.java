package com.healthsmart.service;

import com.healthsmart.dao.UserDAO;
import com.healthsmart.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * 用户服务类
 * User Service
 */
public class UserService {
    private UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * 用户注册
     */
    public User register(String username, String password, String email, String nickname) throws SQLException {
        // 检查用户名是否已存在
        if (userDAO.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User(username, password, email);
        user.setNickname(nickname);
        
        int userId = userDAO.insert(user);
        if (userId > 0) {
            user.setUserId(userId);
            return user;
        } else {
            throw new RuntimeException("用户注册失败");
        }
    }
    
    /**
     * 用户登录
     */
    public User login(String username, String password) throws SQLException {
        User user = userDAO.authenticate(username, password);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (!user.getIsActive()) {
            throw new RuntimeException("账户已禁用");
        }
        
        // 更新最后登录时间
        userDAO.updateLastLogin(user.getUserId());
        
        return user;
    }
    
    /**
     * 获取用户信息
     */
    public User getUserById(Integer userId) throws SQLException {
        return userDAO.findById(userId);
    }
    
    /**
     * 根据用户名获取用户信息
     */
    public User getUserByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }
    
    /**
     * 更新用户信息
     */
    public boolean updateUser(User user) throws SQLException {
        return userDAO.update(user);
    }
    
    /**
     * 删除用户
     */
    public boolean deleteUser(Integer userId) throws SQLException {
        return userDAO.delete(userId);
    }
    
    /**
     * 获取所有用户列表
     */
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }
}