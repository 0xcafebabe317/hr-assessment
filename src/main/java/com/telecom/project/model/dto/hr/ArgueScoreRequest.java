package com.telecom.project.model.dto.hr;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年11月20 17:11
 **/
@Data
public class ArgueScoreRequest implements Serializable {
    private Long id;
    private Double score;
}
