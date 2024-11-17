package com.telecom.project.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年11月05 10:16
 **/
@Data
public class ExcelVO implements Serializable {
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
     * 得分
     */
    private Double score;

    /**
     * 考核时间
     */
    private String assessment_time;

    /**
     * 评分人
     */
    private String assessment_people;

}
