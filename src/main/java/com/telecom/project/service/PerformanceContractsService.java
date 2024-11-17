package com.telecom.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.telecom.project.common.IdRequest;
import com.telecom.project.common.PageRequest;
import com.telecom.project.model.dto.contracts.*;
import com.telecom.project.model.entity.PerformanceContracts;
import com.baomidou.mybatisplus.extension.service.IService;
import com.telecom.project.model.vo.ContractsVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
* @author tiscy
* @description 针对表【performance_contracts(业绩合同表)】的数据库操作Service
* @createDate 2024-10-30 12:13:12
*/
public interface PerformanceContractsService extends IService<PerformanceContracts> {

    List<String> getAssessedUnit();

    boolean addContracts(PerformanceContracts performanceContracts);

    List<String> getAssessedCenter();

    List<String> getCustomerManager(CustomerRequest customerRequest);

    List<String> getBuManager(BuRequest buRequest);

    List<PerformanceContracts> getContracts(ContractsRequest contractsRequest);

    Page<PerformanceContracts> getContractsScore(PageRequest pageRequest,HttpServletRequest request);

    boolean score(ScoreRequest scoreRequest,HttpServletRequest request);

    boolean saveResult(ScoreRequest scoreRequest, HttpServletRequest request);

    Map<Long, Double> getTempScore(TempScoreRequest tempScoreRequest);

    boolean updateContract(UpdateRequest updateRequest);

    boolean deleteContract(IdRequest idRequest);
}
