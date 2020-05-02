package com.verygood.island.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.verygood.island.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
* <p>
* 树洞留言 Mapper 接口
* </p>
*
* @author chaos
* @since 2020-05-02
*/
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}
