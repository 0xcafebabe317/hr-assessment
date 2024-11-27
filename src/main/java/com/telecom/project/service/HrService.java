package com.telecom.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.telecom.project.common.PageRequest;
import com.telecom.project.model.dto.hr.ArgueScoreRequest;
import com.telecom.project.model.dto.hr.ScorePageRequest;
import com.telecom.project.model.entity.PerformanceContracts;
import com.telecom.project.model.vo.ExcelVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: Toys
 * @date: 2024年11月01 15:53
 **/
public interface HrService {
    boolean publish();

    boolean lock();

    boolean remindAndSend();

    List<String> getUnscoreDepts();

    List<ExcelVO> getAllContracts();

    Page<PerformanceContracts> getContractsScore(ScorePageRequest scorepageRequest, HttpServletRequest request);

    boolean updateScore(ArgueScoreRequest argueScoreRequest);

    boolean publicRes();

    boolean unPublicRes();

    boolean freeze();

    List<String> getUnscoreDeptsForEmail();

    boolean AutoPublicRes();

    boolean AutoUnPublicRes();

    boolean AutoFreezen();

    boolean adjust();

    boolean overAdjust();
}
