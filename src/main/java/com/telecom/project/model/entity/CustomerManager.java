package com.telecom.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 客户经理表
 * @TableName customer_manager
 */
@TableName(value ="customer_manager")
@Data
public class CustomerManager implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 客户经理名字
     */
    private String name;

    /**
     * 归属县局
     */
    private String addr;

    /**
     * 归属中心
     */
    private String center;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}