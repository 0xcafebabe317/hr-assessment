package com.telecom.project.model.dto.contracts;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.Map;

/**
 * @author: Toys
 * @date: 2024年11月01 17:54
 **/
@Data
public class ScoreRequest implements Serializable {

    private Map<Long,Double> result;

}
