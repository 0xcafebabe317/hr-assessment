package com.telecom.project.model.dto.contracts;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Toys
 * @date: 2024年11月02 01:50
 **/
@Data
public class TempScoreRequest implements Serializable {

    /**
     * 所有暂存打分的id集合
     */
    private List<Long> ids;
}
