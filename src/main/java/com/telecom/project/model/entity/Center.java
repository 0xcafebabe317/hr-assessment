package com.telecom.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 中心表
 * @TableName center
 */
@TableName(value ="center")
@Data
public class Center implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 中心名称
     */
    private String center_name;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}