package com.telecom.project.model.dto.contracts;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年10月31 19:50
 **/
@Data
public class BuRequest implements Serializable {

    /**
     * bu名称
     */
    private String bu;


    /**
     * 权限：客户经理、负责人
     */
    private String role;
}
