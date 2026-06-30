package org.example.comm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.jsonwebtoken.Claims;
import org.example.comm.entity.Message;
import org.example.comm.mapper.MessageMapper;
import org.example.comm.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/message")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageMapper messageMapper;

    // 获取我的消息列表
    @GetMapping("/my")
    public List<Message> myMessages(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) return null;
        token = token.substring(7);
        Claims claims = JwtUtil.parseToken(token);
        Long userId = Long.parseLong(claims.get("userId").toString());

        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId);
        wrapper.orderByDesc("created_at");
        return messageMapper.selectList(wrapper);
    }

    // 标记消息为已读
    @PutMapping("/read/{id}")
    public String markAsRead(@PathVariable Long id) {
        Message msg = new Message();
        msg.setIsRead(1);
        messageMapper.update(msg, new UpdateWrapper<Message>().eq("id", id));
        return "success";
    }

    // 获取未读消息数量（用于小红点）
    @GetMapping("/unread/count")
    public Integer unreadCount(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) return 0;
        token = token.substring(7);
        Claims claims = JwtUtil.parseToken(token);
        Long userId = Long.parseLong(claims.get("userId").toString());

        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId).eq("is_read", 0);
        return Math.toIntExact(messageMapper.selectCount(wrapper));
    }
}