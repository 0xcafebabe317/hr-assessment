package com.telecom.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 业绩合同表
 * @TableName performance_contracts
 */
@TableName(value ="performance_contracts")
@Data
public class PerformanceContracts implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 大类名称
     */
    private String categories;

    /**
     * 小类名称
     */
    private String sub_categories;

    /**
     * 指标
     */
    private String indicators;

    /**
     * 考核部门
     */
    private String assessment_dept;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 记分方法
     */
    private String scoring_method;

    /**
     * 考核周期
     */
    private String assessment_cycle;

    /**
     * 被考核单位
     */
    private String assessed_unit;

    /**
     * 被考核中心
     */
    private String assessed_center;

    /**
     * 被考核人
     */
    private String assessed_people;

    /**
     * 其他
     */
    private String other;

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