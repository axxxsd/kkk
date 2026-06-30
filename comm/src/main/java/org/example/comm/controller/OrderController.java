package org.example.comm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.jsonwebtoken.Claims;
import org.example.comm.entity.Message;
import org.example.comm.entity.Order;
import org.example.comm.entity.Product;
import org.example.comm.mapper.MessageMapper;
import org.example.comm.mapper.OrderMapper;
import org.example.comm.mapper.ProductMapper;
import org.example.comm.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private MessageMapper messageMapper;

    // 创建订单（核心接口）
    @PostMapping("/create")
    public String createOrder(@RequestBody Order order, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) return "请先登录";
        try {
            token = token.substring(7);
            Claims claims = JwtUtil.parseToken(token);
            Long buyerId = Long.parseLong(claims.get("userId").toString());

            Product product = productMapper.selectById(order.getProductId());
            if (product == null) return "商品不存在";
            if (product.getStatus() != 1) return "商品已下架";
            if (product.getUserId().equals(buyerId)) return "不能购买自己的商品";

            // 1. 创建订单
            order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
            order.setBuyerId(buyerId);
            order.setSellerId(product.getUserId());
            order.setTotalPrice(product.getPrice());
            order.setStatus(1); // 1=待交易
            order.setCreatedAt(LocalDateTime.now());
            orderMapper.insert(order);

            // 2. 下架商品
            product.setStatus(0);
            productMapper.updateById(product);

            // 3. 给卖家发站内信
            Message msg = new Message();
            msg.setSenderId(buyerId);
            msg.setReceiverId(product.getUserId());
            msg.setContent("你的商品《" + product.getTitle() + "》已被购买，订单号：" + order.getOrderNo());
            msg.setIsRead(0);
            msg.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(msg);

            return "订单创建成功！";
        } catch (Exception e) {
            return "操作失败";
        }
    }

    // 查询我的订单（我买的 + 我卖的）
    @GetMapping("/my")
    public List<Order> myOrders(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) return null;
        token = token.substring(7);
        Claims claims = JwtUtil.parseToken(token);
        Long userId = Long.parseLong(claims.get("userId").toString());

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", userId).or().eq("seller_id", userId);
        wrapper.orderByDesc("created_at");
        return orderMapper.selectList(wrapper);
    }
}