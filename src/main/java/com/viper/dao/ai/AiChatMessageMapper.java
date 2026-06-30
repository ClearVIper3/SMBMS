package com.viper.dao.ai;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.viper.pojo.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
}
