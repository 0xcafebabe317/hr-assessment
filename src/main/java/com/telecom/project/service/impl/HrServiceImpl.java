package com.telecom.project.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.telecom.project.common.ErrorCode;
import com.telecom.project.common.ResultUtils;
import com.telecom.project.exception.BusinessException;
import com.telecom.project.manage.mail.MailService;
import com.telecom.project.model.entity.ContractsScore;
import com.telecom.project.model.entity.PerformanceContracts;
import com.telecom.project.model.entity.User;
import com.telecom.project.model.vo.ExcelVO;
import com.telecom.project.service.ContractsScoreService;
import com.telecom.project.service.HrService;
import com.telecom.project.service.PerformanceContractsService;
import com.telecom.project.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.telecom.project.utils.DateUtil.getCurrentDateAsDate;
import static com.telecom.project.utils.ExcelUtil.convertToExcelVO;

/**
 * @author: Toys
 * @date: 2024年11月01 15:53
 **/
@Service
public class HrServiceImpl implements HrService {

    @Resource
    private ContractsScoreService contractsScoreService;

    @Resource
    private UserService userService;

    @Resource
    private PerformanceContractsService performanceContractsService;

    @Resource
    private MailService mailService;


    @Override
    public boolean publish(HttpServletRequest request) {
        // 本月时间 例如 2024年11月
        String currentDate = getCurrentDateAsDate();
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", currentDate);
        long count = contractsScoreService.count(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当月已经发布过业绩合同!");
        }
        List<PerformanceContracts> list = performanceContractsService.list();
        List<ContractsScore> res = new ArrayList<>();
        for (PerformanceContracts performanceContracts : list) {
            ContractsScore contractsScore = new ContractsScore();
            contractsScore.setContract_id(performanceContracts.getId());
            contractsScore.setAssessment_time(currentDate);
            res.add(contractsScore);
        }
        boolean b = contractsScoreService.saveBatch(res);
        if (b) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            wrapper.eq("userRole", "score");

            List<User> scoreUserList = userService.list(userWrapper);

            List<User> validEmailUsers = scoreUserList.stream()
                    .filter(i -> StringUtils.isNotBlank(i.getEmail()) && isValidEmail(i.getEmail()))
                    .collect(Collectors.toList());

            // 发件人
            User loginUser = userService.getLoginUser(request);
            String userName = loginUser.getUserName();
            //发送邮件
            // 标题
            String date = getCurrentDateAsDate();
            String subject = date + "业绩合同评分系统开放提醒";
            // 内容
            String content = "人力资源部【" + userName + "】已经发布了" + date + "业绩合同评分表，请尽快前往评分系统进行评分。";

            List<String> collect = validEmailUsers.stream().map(User::getEmail).collect(Collectors.toList());
            for (String email : collect) {
                mailService.sendSimpleMail(email, subject, content);
            }
        }
        return b;
    }

    @Override
    public boolean lock(HttpServletRequest request) {
        String currentDate = getCurrentDateAsDate();
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", currentDate);
        wrapper.select("contract_id");
        List<ContractsScore> list = contractsScoreService.list(wrapper);
        List<Long> ids = list.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());
        QueryWrapper<PerformanceContracts> wrapper1 = new QueryWrapper<>();
        wrapper1.in("id",ids);
        // to do
        return true;
    }

    @Override
    public boolean remindAndSend() {
        // 查询还未打分的细则id
        String date = getCurrentDateAsDate();
        QueryWrapper<ContractsScore> scoreWrapper = new QueryWrapper<>();
        scoreWrapper.eq("assessment_time",date);
        scoreWrapper.eq("is_lock",0);
        List<ContractsScore> contractsScores = contractsScoreService.list(scoreWrapper);
        // 还未提交的合同的id集合
        List<Long> collect = contractsScores.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());
        if (collect.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"所有评分均已提交!");
        }
        // 根据id集合查询PerformanceContracts
        QueryWrapper<PerformanceContracts> contractsWrapper = new QueryWrapper<>();
        contractsWrapper.in("id", collect);
        List<PerformanceContracts> performanceContracts = performanceContractsService.list(contractsWrapper);

        // 根据assessment_dept分组，并统计每个部门的数量
        Map<String, Long> assessmentDeptCount = performanceContracts.stream()
                .collect(Collectors.groupingBy(PerformanceContracts::getAssessment_dept, Collectors.counting()));

        Set<String> depts = assessmentDeptCount.keySet();
 //       String deptsStr = String.join("、", depts);

//        //给人力发送邮件
//        // 标题
//        String hrSubject = date + "业绩合同未提交评分名单";
//        // 内容
//        String hrContent = "以下部门还未进行评分提交：【"+deptsStr+"】，已向对应打分负责人发送邮箱提醒。";
//        // 人力
//        QueryWrapper<User> hrWrapper = new QueryWrapper<>();
//        hrWrapper.eq("userRole","hr");
//        List<User> hrList = userService.list(hrWrapper);
//        List<String> hrEmails = hrList.stream().map(User::getEmail).collect(Collectors.toList());
//        for (String email : hrEmails) {
//            mailService.sendSimpleMail(email, hrSubject, hrContent);
//        }
        // 发邮箱给打分部门
        QueryWrapper<User> scoreDeptWrapper = new QueryWrapper<>();
        String scoreSubject = date + "业绩合同评分提醒";
        // 内容
        String scoreContent = "请尽快前往业绩合同评分系统进行评分提交。";
        // 打分部门
        scoreDeptWrapper.eq("userRole","score");
        scoreDeptWrapper.in("userDept",depts);
        List<User> users = userService.list(scoreDeptWrapper);
        List<String> scoreEmails = users.stream().map(User::getEmail).collect(Collectors.toList());
        for (String email : scoreEmails) {
            mailService.sendSimpleMail(email, scoreSubject, scoreContent);
        }
        return true;
    }

    @Override
    public List<String> getUnscoreDepts() {
        // 查询还未打分的细则id
        QueryWrapper<ContractsScore> scoreWrapper = new QueryWrapper<>();
        String date = getCurrentDateAsDate();
        scoreWrapper.eq("assessment_time",date);
        scoreWrapper.eq("is_lock",0);
        List<ContractsScore> contractsScores = contractsScoreService.list(scoreWrapper);
        // 还未提交的合同的id集合
        List<Long> collect = contractsScores.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());
        if (collect.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"所有评分均已提交!");
        }
        // 根据id集合查询PerformanceContracts
        QueryWrapper<PerformanceContracts> contractsWrapper = new QueryWrapper<>();
        contractsWrapper.in("id", collect);
        List<PerformanceContracts> performanceContracts = performanceContractsService.list(contractsWrapper);

        // 根据assessment_dept分组，并统计每个部门的数量
        Map<String, Long> assessmentDeptCount = performanceContracts.stream()
                .collect(Collectors.groupingBy(PerformanceContracts::getAssessment_dept, Collectors.counting()));

        Set<String> depts = assessmentDeptCount.keySet();
        List<String> res = new ArrayList<>(depts);
        return res;
    }

    @Override
    public List<ExcelVO> getAllContracts() {
        // 本月时间
        String date = getCurrentDateAsDate();

        // 查询当月的合同评分记录
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        List<ContractsScore> list = contractsScoreService.list(wrapper);

        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当月没有可导出的业绩合同");
        }

        // 总条数
        long allCount = contractsScoreService.count(wrapper);

        // 已锁定条数
        QueryWrapper<ContractsScore> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("assessment_time", date);
        wrapper1.eq("is_lock", 1);
        long lockCount = contractsScoreService.count(wrapper1);

//        if (allCount != lockCount) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "还有部门未完成评分提交");
//        }

        // 获取已锁定的评分记录
        List<ContractsScore> resList = contractsScoreService.list(wrapper1);
        List<Long> ids = resList.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());

        // 根据合同 ID 获取 PerformanceContracts 数据
        QueryWrapper<PerformanceContracts> wrapper2 = new QueryWrapper<>();
        wrapper2.in("id", ids);
        List<PerformanceContracts> performanceContracts = performanceContractsService.list(wrapper2);

        // 将 ContractsScore 和 PerformanceContracts 转换成 ExcelVO
        List<ExcelVO> excelData = convertToExcelVO(resList, performanceContracts);

        return excelData;
    }



    /**
     * 邮箱合法性校验
     *
     * @param email
     * @return
     */
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
