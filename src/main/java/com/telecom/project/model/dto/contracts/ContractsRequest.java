package com.telecom.project.model.dto.contracts;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年10月31 23:05
 **/
@Data
public class ContractsRequest implements Serializable {

    /**
     * 被考核单位
     */
    private String assessedUnit;

    /**
     * 被考核中心
     */
    private String assessedCenter;

    /**
     * 被考核人
     */
    private String assessedPeople;
}
