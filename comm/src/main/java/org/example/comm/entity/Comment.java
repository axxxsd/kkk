package org.example.comm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("comments") // 对应数据库表名
public class Comment {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("product_id")
    private Integer productId; // 对应商品ID

    @TableField("user_id")
    private Integer userId; // 对应用户ID

    private String content; // 评论内容
    private Integer score; // 评分
    private Integer status; // 状态

    @TableField("created_at")
    private LocalDateTime createdAt; // 评论时间

    // 必须加getter/setter（Lombok的@Data也可以，不用手写）
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}