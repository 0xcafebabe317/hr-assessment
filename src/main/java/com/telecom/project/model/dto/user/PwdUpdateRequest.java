package com.telecom.project.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年11月14 16:41
 **/
@Data
public class PwdUpdateRequest implements Serializable {

    private String newPwd;

    private String newCheckPwd;
}
