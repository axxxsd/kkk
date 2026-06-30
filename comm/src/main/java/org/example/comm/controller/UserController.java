package org.example.comm.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.example.comm.entity.User;
import org.example.comm.entity.UserLogin;
import org.example.comm.mapper.UserMapper;
import org.example.comm.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // ===== 你之前的测试接口，全部保留 =====
    @GetMapping("/test/users")
    public List<User> testQueryUsers() {
        return userMapper.selectList(null);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Campus Second-hand Platform is running!";
    }

    @GetMapping("/test/add")
    public String addUser() {
        User user = new User();
        user.setUsername("student001");
        user.setPassword("123456");
        user.setNickname("张三");
        user.setPhone("13800138000");
        user.setRole(0); // 0=普通用户，1=管理员
        userMapper.insert(user);
        return "用户添加成功！ID是：" + user.getId();
    }

    // ===== 新增：登录接口 =====
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody UserLogin loginUser) {
        Map<String, Object> result = new HashMap<>();
        // 1. 根据用户名查用户
        User dbUser = userMapper.selectByUsername(loginUser.getUsername());
        // 2. 用户不存在
        if (dbUser == null) {
            result.put("code", 401);
            result.put("msg", "用户不存在");
            return result;
        }
        // 3. 密码错误（⚠️ 现在是明文比对，后续可以换成BCrypt加密）
        if (!dbUser.getPassword().equals(loginUser.getPassword())) {
            result.put("code", 401);
            result.put("msg", "密码错误");
            return result;
        }
        // 4. 登录成功，生成Token
        String token = JwtUtil.generateToken(dbUser);
        result.put("code", 200);
        result.put("msg", "登录成功");
        result.put("token", token);
        result.put("userInfo", dbUser); // 返回用户信息给前端
        return result;
    }

    /**
     * 新增：获取当前登录用户的详细信息（从Token解析，更安全）
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            result.put("code", 401);
            result.put("msg", "请先登录");
            return result;
        }
        try {
            token = token.substring(7);
            Claims claims = JwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.get("userId").toString());
            User user = userMapper.selectById(userId);
            result.put("code", 200);
            result.put("data", user);
            return result;
        } catch (Exception e) {
            result.put("code", 401);
            result.put("msg", "Token无效或已过期");
            return result;
        }
    }
}