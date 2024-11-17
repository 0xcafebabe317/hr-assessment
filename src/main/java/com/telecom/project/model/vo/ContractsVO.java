package com.telecom.project.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: Toys
 * @date: 2024年11月01 13:47
 **/
@Data
public class ContractsVO implements Serializable {

    /**
     * 细则id
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


    private static final long serialVersionUID = 1L;
}
