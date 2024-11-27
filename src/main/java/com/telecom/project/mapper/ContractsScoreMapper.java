package com.telecom.project.mapper;

import com.telecom.project.model.entity.ContractsScore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
* @author tiscy
* @description 针对表【contracts_score(打分表)】的数据库操作Mapper
* @createDate 2024-11-01 12:58:28
* @Entity com.telecom.project.model.entity.ContractsScore
*/
@Mapper
public interface ContractsScoreMapper extends BaseMapper<ContractsScore> {

    @Update("UPDATE contracts_score SET is_lock = 1 WHERE assessment_time = #{date}")
    boolean lockRes(String date);
}




