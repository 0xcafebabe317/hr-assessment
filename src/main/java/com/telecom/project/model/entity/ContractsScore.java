package com.telecom.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 打分表
 * @TableName contracts_score
 */
@TableName(value ="contracts_score")
@Data
public class ContractsScore implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 合同id
     */
    private Long contract_id;

    /**
     * 得分
     */
    private Double score;

    /**
     * 考核时间
     */
    private String assessment_time;

    /**
     * 是否锁定
     */
    private Integer is_lock;

    /**
     * 评分人
     */
    private String assessment_people;

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