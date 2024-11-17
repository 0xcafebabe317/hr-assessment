package com.telecom.project.service;

import com.telecom.project.model.vo.ExcelVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: Toys
 * @date: 2024年11月01 15:53
 **/
public interface HrService {
    boolean publish(HttpServletRequest request);

    boolean lock(HttpServletRequest request);

    boolean remindAndSend();

    List<String> getUnscoreDepts();

    List<ExcelVO> getAllContracts();
}
