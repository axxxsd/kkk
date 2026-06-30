package org.example.comm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.comm.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // 继承BaseMapper后，增删改查方法都已自带，这里不需要写任何代码
}