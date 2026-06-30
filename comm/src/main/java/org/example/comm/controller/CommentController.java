package org.example.comm.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.jsonwebtoken.Claims;
import org.example.comm.entity.Comment;
import org.example.comm.mapper.CommentMapper;
import org.example.comm.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentMapper commentMapper;

    /**
     * 1. 发布评论（普通用户可用）
     */
    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestBody Comment comment, HttpServletRequest request) {
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
            Integer userId = (Integer) claims.get("userId");
            comment.setUserId(userId);
            comment.setStatus(0); // 默认正常状态
            commentMapper.insert(comment);
            result.put("code", 200);
            result.put("msg", "评论发布成功");
            return result;
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "评论发布失败");
            return result;
        }
    }

    /**
     * 2. 查询商品评论（公开接口，无需登录）
     */
    @GetMapping("/list/{productId}")
    public Map<String, Object> listByProduct(@PathVariable Integer productId) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("status", 0); // 只查正常评论
        queryWrapper.orderByDesc("created_at"); // 按时间倒序
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        result.put("code", 200);
        result.put("data", comments);
        return result;
    }

    /**
     * 3. 删除评论（仅管理员可用，和你之前的下架商品权限一致）
     */
    @PutMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return "请先登录";
        }
        try {
            token = token.substring(7);
            Claims claims = JwtUtil.parseToken(token);
            Integer role = (Integer) claims.get("role");
            if (role != 1) {
                return "权限不足，仅管理员可删除评论";
            }
            Comment comment = new Comment();
            comment.setId(id);
            comment.setStatus(1); // 软删除，不直接删数据
            commentMapper.updateById(comment);
            return "评论删除成功";
        } catch (Exception e) {
            return "删除失败，Token可能已过期";
        }
    }
}