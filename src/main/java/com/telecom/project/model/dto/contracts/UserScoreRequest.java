package com.telecom.project.model.dto.contracts;

import com.telecom.project.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年12月17 19:39
 **/

@Data
public class UserScoreRequest extends PageRequest implements Serializable {

    private String searchText;
}
