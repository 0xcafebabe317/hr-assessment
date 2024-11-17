package com.telecom.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * bu及客户经理表
 * @TableName bu_manager
 */
@TableName(value ="bu_manager")
@Data
public class BuManager implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名字
     */
    private String name;

    /**
     * 归属bu
     */
    private String addr;

    /**
     * 角色:负责人/客户经理
     */
    private String role;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}