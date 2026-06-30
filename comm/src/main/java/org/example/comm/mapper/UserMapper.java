package org.example.comm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.comm.entity.User;

public interface UserMapper extends BaseMapper<User> {

    // 新增：根据用户名查询用户（登录用）
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);
}