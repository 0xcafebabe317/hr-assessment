package com.telecom.project.model.dto.contracts;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年11月03 14:52
 **/
@Data
public class UpdateRequest implements Serializable {
    /**
     * id
     */
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

}
