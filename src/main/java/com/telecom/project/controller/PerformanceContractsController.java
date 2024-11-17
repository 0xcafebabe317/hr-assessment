package com.telecom.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.telecom.project.common.*;
import com.telecom.project.exception.BusinessException;
import com.telecom.project.model.dto.contracts.*;
import com.telecom.project.model.entity.PerformanceContracts;

import com.telecom.project.service.PerformanceContractsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author: Toys
 * @date: 2024年10月30 12:20
 **/
@RestController
@RequestMapping("performance_contracts")
@Slf4j
public class PerformanceContractsController {

    @Resource
    private PerformanceContractsService performanceContractsService;


    /**
     * 获取被考核单位
     *
     * @return
     */
    @RequestMapping("get/assessed_unit")
    public BaseResponse<List<String>> getAssessedUnit() {
        List<String> res = performanceContractsService.getAssessedUnit();
        return ResultUtils.success(res);
    }

    /**
     * 添加考核合同
     *
     * @param performanceContracts
     * @return
     */
    @RequestMapping("add/assessed/detail")
    public BaseResponse<Boolean> addAssessed(@RequestBody PerformanceContracts performanceContracts) {
        if (performanceContracts == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        boolean res = performanceContractsService.addContracts(performanceContracts);
        return ResultUtils.success(res);
    }

    /**
     * 获取被考核中心
     *
     * @return
     */
    @RequestMapping("get/assessed_center")
    public BaseResponse<List<String>> getAssessedCenter() {
        List<String> res = performanceContractsService.getAssessedCenter();
        return ResultUtils.success(res);
    }

    /**
     * 获取8个县局的政企中心下的客户经理和负责人
     *
     * @param customerRequest
     * @return
     */
    @RequestMapping("get/customer_manager")
    public BaseResponse<List<String>> getCustomerManager(@RequestBody CustomerRequest customerRequest) {
        List<String> res = performanceContractsService.getCustomerManager(customerRequest);
        if (res.size() == 0){
            res.add("无");
        }
        return ResultUtils.success(res);
    }

    /**
     * 获取bu和客户经理
     *
     * @param buRequest
     * @return
     */
    @RequestMapping("get/bu/manager")
    public BaseResponse<List<String>> getBuManager(@RequestBody BuRequest buRequest) {
        List<String> res = performanceContractsService.getBuManager(buRequest);
        return ResultUtils.success(res);
    }

    /**
     * 获取业绩合同详细信息
     *
     * @param contractsRequest
     * @return
     */
    @RequestMapping("get/contracts/detail")
    public BaseResponse<List<PerformanceContracts>> getContracts(@RequestBody ContractsRequest contractsRequest) {
        List<PerformanceContracts> res = performanceContractsService.getContracts(contractsRequest);
        return ResultUtils.success(res);
    }


    /**
     * 获取打分页面
     *
     * @param pageRequest
     * @param request
     * @return
     */
    @RequestMapping("get/contracts/score")
    public BaseResponse<Page<PerformanceContracts>> getContractsScore(@RequestBody PageRequest pageRequest, HttpServletRequest request) {
        Page<PerformanceContracts> res = performanceContractsService.getContractsScore(pageRequest, request);
        return ResultUtils.success(res);
    }

    /**
     * 暂存打分结果
     *
     * @return
     */
    @RequestMapping("temporary/storage")
    public BaseResponse<Boolean> temporaryStorage(@RequestBody ScoreRequest scoreRequest, HttpServletRequest request) {
        boolean res = performanceContractsService.score(scoreRequest, request);
        return ResultUtils.success(res);
    }

    /**
     * 提交打分结果
     *
     * @return
     */
    @RequestMapping("save/result")
    public BaseResponse<Boolean> saveResult(@RequestBody ScoreRequest scoreRequest, HttpServletRequest request) {
        boolean res = performanceContractsService.saveResult(scoreRequest, request);
        return ResultUtils.success(res);
    }

    /**
     * 获取暂存的分数列表
     *
     * @param tempScoreRequest
     * @return
     */
    @RequestMapping("get/temp/score")
    public BaseResponse<Map<Long, Double>> getTempScore(@RequestBody TempScoreRequest tempScoreRequest) {
        Map<Long, Double> res = performanceContractsService.getTempScore(tempScoreRequest);
        return ResultUtils.success(res);
    }

    @RequestMapping("/update/contract")
    public BaseResponse<Boolean> updateContract(@RequestBody UpdateRequest updateRequest) {
        if (updateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        boolean res = performanceContractsService.updateContract(updateRequest);
        return ResultUtils.success(res);
    }

    @RequestMapping("/delete/contract")
    public BaseResponse<Boolean> DeleteContract(@RequestBody IdRequest idRequest) {
        if (idRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        boolean res = performanceContractsService.deleteContract(idRequest);
        return ResultUtils.success(res);
    }


}
