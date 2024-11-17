package com.telecom.project.model.dto.contracts;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年10月31 18:31
 **/
@Data
public class CustomerRequest implements Serializable {

    /**
     * 单位
     */
    private String unit;

    /**
     * 中心
     */
    private String center;
}
