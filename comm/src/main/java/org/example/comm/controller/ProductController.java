package org.example.comm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.jsonwebtoken.Claims;
import org.example.comm.entity.Product;
import org.example.comm.mapper.ProductMapper;
import org.example.comm.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*")
public class    ProductController {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 1. 发布商品（完全保留你原有逻辑）
     */
    @PostMapping("/publish")
    public String publish(@RequestBody Product product, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return "请先登录后再发布商品";
        }
        token = token.substring(7);
        try {
            Claims claims = JwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.get("userId").toString());
            product.setUserId(userId);
            product.setStatus(1);
            product.setCreatedAt(java.time.LocalDateTime.now());
            int result = productMapper.insert(product);
            return result > 0 ? "商品发布成功！ID: " + product.getId() : "发布失败，请重试";
        } catch (Exception e) {
            return "发布失败：Token无效或已过期，请重新登录";
        }
    }

    /**
     * 2. 根据ID查商品（完全保留你原有逻辑）
     */
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 3. 查询在售商品列表（只返回status=1的商品，下架后自动消失）
     */
    @GetMapping("/list")
    public List<Product> listAllProducts() {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("created_at");
        return productMapper.selectList(queryWrapper);
    }

    /**
     * 4. 删除商品（完全保留你原有逻辑）
     */
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        int result = productMapper.deleteById(id);
        return result > 0 ? "商品删除成功！" : "删除失败，商品可能不存在";
    }

    /**
     * 5. 更新商品（完全保留你原有逻辑）
     */
    @PutMapping("/{id}")
    public String updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        int result = productMapper.updateById(product);
        return result > 0 ? "商品更新成功！" : "更新失败，商品可能不存在";
    }

    /**
     * 6. 管理员下架商品（完全保留你原有逻辑）
     */
    /**
     * 6. 下架商品（管理员可下架所有，普通用户仅可下架自己的）
     */
    @PutMapping("/offline/{id}")
    public String offlineProduct(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return "请先登录";
        }
        try {
            token = token.substring(7);
            Claims claims = JwtUtil.parseToken(token);
            Long currentUserId = Long.parseLong(claims.get("userId").toString());
            Integer currentUserRole = (Integer) claims.get("role");

            Product product = productMapper.selectById(id);
            if (product == null) {
                return "商品不存在";
            }
            // 权限校验：管理员 OR 商品发布者
            if (currentUserRole != 1 && !product.getUserId().equals(currentUserId)) {
                return "权限不足，只能下架自己发布的商品";
            }

            product.setStatus(0);
            int result = productMapper.updateById(product);
            return result > 0 ? "商品下架成功！" : "商品不存在或下架失败";
        } catch (Exception e) {
            return "操作失败，Token可能已过期";
        }
    }

    /**
     * 7. 新增：购买商品接口（拍下即下架）
     */
    @PostMapping("/purchase/{id}")
    public String purchaseProduct(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody, // 正确接收前端传的地址参数
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return "请先登录";
        }
        try {
            token = token.substring(7);
            Claims claims = JwtUtil.parseToken(token);
            Long buyerId = Long.parseLong(claims.get("userId").toString());

            // 查商品
            Product product = productMapper.selectById(id);
            if (product == null) {
                return "商品不存在";
            }
            // 检查是否在售
            if (product.getStatus() != 1) {
                return "商品已下架或已售出";
            }
            // 不能买自己的商品
            if (product.getUserId().equals(buyerId)) {
                return "不能购买自己发布的商品";
            }
            // 检查地址是否为空
            String address = requestBody.get("address");
            if (address == null || address.trim().isEmpty()) {
                return "请输入收货地址";
            }

            // 执行购买：直接下架商品
            product.setStatus(0);
            int result = productMapper.updateById(product);
            System.out.println("用户" + buyerId + "购买了商品" + id + "，收货地址：" + address);
            return result > 0 ? "购买成功！请留意卖家的联系信息" : "购买失败，请重试";
        } catch (Exception e) {
            return "操作失败，Token可能已过期";
        }
    }
    @GetMapping("/listByCategory")
    public List<Product> listByCategory(@RequestParam String category) { // 重点：改成String
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 仅查询在售商品
        if (!"全部".equals(category)) { // 全部分类不筛选
            queryWrapper.eq("category", category); // 匹配数据库里的分类名称
        }
        queryWrapper.orderByDesc("created_at");
        return productMapper.selectList(queryWrapper);
    }
    /**
     * 新增：获取当前用户发布的商品列表
     */
    @GetMapping("/my")
    public Map<String, Object> getMyProducts(HttpServletRequest request) {
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

            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId); // 关键：只查当前用户发布的商品
            queryWrapper.orderByDesc("created_at");
            List<Product> products = productMapper.selectList(queryWrapper);

            result.put("code", 200);
            result.put("data", products);
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败");
            return result;
        }
    }
}