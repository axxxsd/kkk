package org.example.comm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.comm.entity.Comment;

public interface CommentMapper extends BaseMapper<Comment> {
    // MyBatis-Plus自带CRUD，无需手写方法
}