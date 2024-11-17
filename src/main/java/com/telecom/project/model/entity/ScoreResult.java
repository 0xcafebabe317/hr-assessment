package com.telecom.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评分结果表
 * @TableName score_result
 */
@TableName(value ="score_result")
@Data
public class ScoreResult implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 得分结果
     */
    private Double score;

    /**
     * 考核时间
     */
    private String assessment_time;

    /**
     * 被考核人
     */
    private String assessed_people;

    /**
     * 是否确认公示
     */
    private Integer is_view;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}