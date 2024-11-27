package com.telecom.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 公示确认争议表
 * @TableName confirm
 */
@TableName(value ="confirm")
@Data
public class Confirm implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 考核时间
     */
    private String assessment_time;

    /**
     * 被考核单位
     */
    private String unit;

    /**
     * 被考核人
     */
    private String name;

    /**
     * 是否确认：0-没有 1-确认 
     */
    private Integer isConfirm;

    /**
     * 是否有争议：0-没有 1-有
     */
    private Integer isDispute;

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