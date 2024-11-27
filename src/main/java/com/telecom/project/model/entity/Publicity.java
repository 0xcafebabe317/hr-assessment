package com.telecom.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 功能表
 * @TableName publicity
 */
@TableName(value ="publicity")
@Data
public class Publicity implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 考评时间
     */
    private String assessment_time;

    /**
     * 是否公示：0-不 1-是 
     */
    private Integer isPublic;

    /**
     * 是否冻结
     */
    private Integer isFreeze;

    /**
     * 是否处于调整
     */
    private Integer isAdjust;

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