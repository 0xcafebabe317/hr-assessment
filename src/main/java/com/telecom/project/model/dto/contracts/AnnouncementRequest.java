package com.telecom.project.model.dto.contracts;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Toys
 * @date: 2024年11月12 17:51
 **/
@Data
public class AnnouncementRequest implements Serializable {

    private String content;
}
