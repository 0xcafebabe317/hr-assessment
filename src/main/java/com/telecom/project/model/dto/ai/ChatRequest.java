package com.telecom.project.model.dto.ai;


import lombok.Data;

import java.io.Serializable;

@Data
public class ChatRequest implements Serializable {

    private String message;

}
