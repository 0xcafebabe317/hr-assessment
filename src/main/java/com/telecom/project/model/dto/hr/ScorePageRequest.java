package com.telecom.project.model.dto.hr;

import com.telecom.project.constant.CommonConstant;
import lombok.Data;

/**
 * @author: Toys
 * @date: 2024年11月20 17:59
 **/
@Data
public class ScorePageRequest {

    /**
     * 搜索内容
     */
    private String searchText;
    /**
     * 当前页号
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
