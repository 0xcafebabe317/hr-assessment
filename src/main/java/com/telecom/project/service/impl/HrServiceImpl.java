package com.telecom.project.service.impl;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.telecom.project.common.ErrorCode;
import com.telecom.project.common.PageRequest;
import com.telecom.project.common.ResultUtils;
import com.telecom.project.exception.BusinessException;
import com.telecom.project.manage.mail.MailService;
import com.telecom.project.mapper.ContractsScoreMapper;
import com.telecom.project.model.dto.hr.ArgueScoreRequest;
import com.telecom.project.model.dto.hr.ScorePageRequest;
import com.telecom.project.model.entity.*;
import com.telecom.project.model.vo.ExcelVO;
import com.telecom.project.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.*;
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
    private ContractsScoreMapper contractsScoreMapper;

    @Resource
    private ConfirmService confirmService;

    @Resource
    private PublicityService publicityService;

    @Resource
    private ContractsScoreService contractsScoreService;

    @Resource
    private UserService userService;

    @Resource
    private PerformanceContractsService performanceContractsService;

    @Resource
    private MailService mailService;


    @Override
    @Transactional
    public boolean publish() {
        // 本月时间 例如 2024年11月
        String currentDate = getCurrentDateAsDate();
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", currentDate);
        long count = contractsScoreService.count(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "本月已经启动业绩合同评分流程!");
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
            // 初始化公示表
            Publicity publicity = new Publicity();
            QueryWrapper<Publicity> publicityQueryWrapper = new QueryWrapper<>();
            publicityQueryWrapper.eq("assessment_time", currentDate);
            Publicity one = publicityService.getOne(publicityQueryWrapper);
            if (one == null) {
                publicity.setAssessment_time(currentDate);
                publicityService.save(publicity);
            }
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            wrapper.eq("userRole", "score");

            List<User> scoreUserList = userService.list(userWrapper);

            List<User> validEmailUsers = scoreUserList.stream()
                    .filter(i -> StringUtils.isNotBlank(i.getEmail()) && isValidEmail(i.getEmail()))
                    .collect(Collectors.toList());

            //发送邮件
            // 标题
            String date = getCurrentDateAsDate();
            String subject = date + "业绩合同评分系统开放提醒";
            // 内容
            String content = "【人力资源部】已经发布了" + date + "业绩合同评分表，请尽快前往业绩评分系统进行评分，评分地址：118.25.230.183";

            List<String> collect = validEmailUsers.stream().map(User::getEmail).collect(Collectors.toList());
            for (String email : collect) {
                mailService.sendSimpleMail(email, subject, content);
            }
        }
        return b;
    }

    @Override
    public boolean lock() {
        String currentDate = getCurrentDateAsDate();
        return contractsScoreMapper.lockRes(currentDate);
    }

    @Override
    public boolean remindAndSend() {
        // 查询还未打分的细则id
        String date = getCurrentDateAsDate();
        QueryWrapper<ContractsScore> scoreWrapper = new QueryWrapper<>();
        scoreWrapper.eq("assessment_time", date);
        scoreWrapper.eq("is_lock", 0);
        List<ContractsScore> contractsScores = contractsScoreService.list(scoreWrapper);
        // 还未提交的合同的id集合
        List<Long> collect = contractsScores.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());
        if (collect.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "所有评分均已提交!");
        }
        // 根据id集合查询PerformanceContracts
        QueryWrapper<PerformanceContracts> contractsWrapper = new QueryWrapper<>();
        contractsWrapper.in("id", collect);
        List<PerformanceContracts> performanceContracts = performanceContractsService.list(contractsWrapper);

        // 根据assessment_dept分组，并统计每个部门的数量
        Map<String, Long> assessmentDeptCount = performanceContracts.stream()
                .collect(Collectors.groupingBy(PerformanceContracts::getAssessment_dept, Collectors.counting()));

        Set<String> depts = assessmentDeptCount.keySet();
        // 发邮箱给打分部门
        QueryWrapper<User> scoreDeptWrapper = new QueryWrapper<>();
        String scoreSubject = date + "业绩合同评分提醒";
        // 内容
        String scoreContent = "根据《关于印发中国电信林芝分公司绩效管理办法（2024版）的通知》（中国电信林芝【2024】101号）规定，请各部门负责人于12月25日13：00前完成11月单位、部门业绩打分，并邮件至人力资源部，逾期造成业绩考核未完成的部门将进行通报，扣除相关部门负责人业绩得分，感谢配合。\n" +
                "\n" +
                "本月打分线上线下并行\n" +
                "地址：http://118.25.230.183\n" +
                "账号：手机号（可申请）\n" +
                "初始密码：12345678\n" +
                "登录后请在右上角选择修改密码\n" +
                "如果有操作上的问题，联系 唐玮志：13308941203";
        // 打分部门
        scoreDeptWrapper.eq("userRole", "score");
        scoreDeptWrapper.in("userDept", depts);
        List<User> users = userService.list(scoreDeptWrapper);
        List<String> scoreEmails = users.stream()
                .map(User::getEmail)
                .filter(Objects::nonNull) // 过滤掉 email 为 null 的数据
                .collect(Collectors.toList());

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
        scoreWrapper.eq("assessment_time", date);
        scoreWrapper.eq("is_lock", 0);
        List<ContractsScore> contractsScores = contractsScoreService.list(scoreWrapper);
        // 还未提交的合同的id集合
        List<Long> collect = contractsScores.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());
        if (collect.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "所有评分均已提交或还未启动评分流程!");
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
    public List<ExcelVO> getAllContracts(String yearmonth) {

        // 计算各县局局长的评分并且补充到其他三个中心的评分里面
        QueryWrapper<PerformanceContracts> wrapperpc = new QueryWrapper<>();
        wrapperpc.eq("assessed_center", "CEO");
        List<PerformanceContracts> ceos = performanceContractsService.list(wrapperpc);
        // <巴宜区,[1，2，,3，,4]>  哪一个区的所有id
        // 创建一个 Map 用于存储分组结果
        Map<String, List<Long>> map = ceos.stream()
                .collect(Collectors.groupingBy(
                        PerformanceContracts::getAssessed_unit, // 根据 assessed_unit 字段分组
                        Collectors.mapping(PerformanceContracts::getId, Collectors.toList()) // 提取 id 并收集为 List
                ));

        // CEO总分 <巴宜区,88.8>
        Map<String, Double> ceoScore = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : map.entrySet()) {
            // 对应区县的名称和 CEO的考核细则
            String assessedUnit = entry.getKey(); // 获取 assessed_unit
            List<Long> ids = entry.getValue();   // 获取对应的 id 列表
            QueryWrapper<ContractsScore> wrappercs = new QueryWrapper<>();
            wrappercs.in("contract_id", ids);
            wrappercs.eq("assessment_time",yearmonth);
            List<ContractsScore> list = contractsScoreService.list(wrappercs);
            double totalScore = list.stream()
                    .mapToDouble(ContractsScore::getScore) // 映射为 double
                    .sum(); // 求和
            ceoScore.put(assessedUnit, totalScore);
        }
        // 政企、公众 30%  综维 20%
        // 政企
        QueryWrapper<PerformanceContracts> wrapper3 = new QueryWrapper<>();
        wrapper3.eq("assessed_center", "政企中心");
        wrapper3.eq("indicators", "县（支）局业绩得分");
        List<PerformanceContracts> listPC = performanceContractsService.list(wrapper3);
        // <巴宜区,2>
        Map<Long, String> map1 = listPC.stream()
                .collect(Collectors.toMap(
                        PerformanceContracts::getId,           // 以 id 作为键
                        PerformanceContracts::getAssessed_unit, // 以 assessed_unit 作为值
                        (existing, replacement) -> replacement // 如果有重复键，保留最新的值
                ));

        QueryWrapper<ContractsScore> wrapperCS = new QueryWrapper<>();
        Set<Long> idsT = map1.keySet();
        wrapperCS.in("contract_id", idsT);
        wrapperCS.eq("assessment_time",yearmonth);
        List<ContractsScore> listCS = contractsScoreService.list(wrapperCS);
        for (ContractsScore item : listCS) {
            Long contractId = item.getContract_id();
            String unit = map1.get(contractId);
            // 从ceoScore找到ceo的分
            Double aDouble = ceoScore.get(unit);
            // 政企30%
            double resScore = Math.round(aDouble * 0.3 * 100) / 100.0; // 保留两位小数
            item.setScore(resScore);
            contractsScoreService.updateById(item);
        }

        // 公众
        QueryWrapper<PerformanceContracts> wrapper4 = new QueryWrapper<>();
        wrapper4.eq("assessed_center", "公众商客");
        wrapper4.eq("indicators", "县（支）局业绩得分");
        List<PerformanceContracts> list2 = performanceContractsService.list(wrapper4);
        // <2，巴宜区>
        Map<Long, String> map2 = list2.stream()
                .collect(Collectors.toMap(
                        PerformanceContracts::getId,           // 以 id 作为键
                        PerformanceContracts::getAssessed_unit, // 以 assessed_unit 作为值
                        (existing, replacement) -> replacement // 如果有重复键，保留最新的值
                ));

        QueryWrapper<ContractsScore> wrapper5 = new QueryWrapper<>();
        Set<Long> ids1 = map2.keySet();
        wrapper5.in("contract_id", ids1);
        wrapper5.eq("assessment_time",yearmonth);
        List<ContractsScore> list3 = contractsScoreService.list(wrapper5);
        for (ContractsScore item : list3) {
            Long contractId = item.getContract_id();
            String unit = map2.get(contractId);
            // 从ceoScore找到ceo的分
            Double aDouble = ceoScore.get(unit);
            // 公众30%
            double resScore = Math.round(aDouble * 0.3 * 100) / 100.0; // 保留两位小数
            item.setScore(resScore);
            contractsScoreService.updateById(item);
        }

        // 综维
        QueryWrapper<PerformanceContracts> wrapper6 = new QueryWrapper<>();
        wrapper6.eq("assessed_center", "综维中心");
        wrapper6.eq("indicators", "县（支）局业绩得分");
        List<PerformanceContracts> list4 = performanceContractsService.list(wrapper6);
        // <2,巴宜区>
        Map<Long, String> map3 = list4.stream()
                .collect(Collectors.toMap(
                        PerformanceContracts::getId,           // 以 id 作为键
                        PerformanceContracts::getAssessed_unit, // 以 assessed_unit 作为值
                        (existing, replacement) -> replacement // 如果有重复键，保留最新的值
                ));

        QueryWrapper<ContractsScore> wrapper7 = new QueryWrapper<>();
        Set<Long> ids3 = map3.keySet();
        wrapper7.in("contract_id", ids3);
        wrapper7.eq("assessment_time",yearmonth);
        List<ContractsScore> list5 = contractsScoreService.list(wrapper7);
        for (ContractsScore item : list5) {
            Long contractId = item.getContract_id();
            String unit = map3.get(contractId);
            // 从ceoScore找到ceo的分
            Double aDouble = ceoScore.get(unit);
            // 综维20%
            double resScore = Math.round(aDouble * 0.2 * 100) / 100.0; // 保留两位小数
            item.setScore(resScore);
            contractsScoreService.updateById(item);
        }

        // 查询当月的合同评分记录
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", yearmonth);
        List<ContractsScore> list = contractsScoreService.list(wrapper);

        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当月没有可导出的业绩合同");
        }

        // 总条数
        long allCount = contractsScoreService.count(wrapper);

        // 已锁定条数
        QueryWrapper<ContractsScore> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("assessment_time", yearmonth);
        // wrapper1.eq("is_lock", 1);
        long lockCount = contractsScoreService.count(wrapper1);

//        if (allCount != lockCount) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "还有部门未完成评分提交");
//        }

        // 获取所有评分记录
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

    @Override
    public Page<PerformanceContracts> getContractsScore(ScorePageRequest scorePageRequest, HttpServletRequest request) {
        String date = getCurrentDateAsDate();
        QueryWrapper<Publicity> pubWrapper = new QueryWrapper<>();
        pubWrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(pubWrapper);
        if ((one.getIsFreeze() != 0) || (one.getIsAdjust() == 0)) {
            return null;
        }
        long current = scorePageRequest.getCurrent();
        long pageSize = scorePageRequest.getPageSize();
        String searchText = scorePageRequest.getSearchText();

        User loginUser = userService.getLoginUser(request);
        String userRole = loginUser.getUserRole();
        // 确定是hr
        if (!userRole.equals("hr")) {
            return null;
        }
        QueryWrapper<PerformanceContracts> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(searchText)) {
            wrapper.like("assessed_unit", searchText)
                    .or()
                    .like("assessed_center", searchText)
                    .or()
                    .like("assessed_people", searchText);
        }
        List<PerformanceContracts> list = performanceContractsService.list(wrapper);

        List<Long> ids = list.stream().map(PerformanceContracts::getId).collect(Collectors.toList());

        QueryWrapper<ContractsScore> scoreQueryWrapper = new QueryWrapper<>();
        // 在打分表中去中id相同并且未锁定的打分合同
        scoreQueryWrapper.in("contract_id", ids) // contract_id 在 ids 列表中
                .eq("is_lock", 1);

        // 得到打分合同
        List<ContractsScore> contractsScores = contractsScoreService.list(scoreQueryWrapper);

        // 打分合同中的合同id集合，这就是全部要返回的id集合
        List<Long> contractsScoresIds = contractsScores.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());

        // 最终业绩合同
        if (!contractsScoresIds.isEmpty()) {
            List<PerformanceContracts> performanceContractsList = performanceContractsService.listByIds(contractsScoresIds);

            // 按 categories 和 sub_categories 排序
            performanceContractsList.sort(Comparator.comparing(PerformanceContracts::getCategories)
                    .thenComparing(PerformanceContracts::getSub_categories));

            // 处理分页
            Page<PerformanceContracts> page = new Page<>(current, pageSize, performanceContractsList.size());

            // 计算分页的开始和结束索引
            int startIndex = (int) ((current - 1) * pageSize);
            int endIndex = Math.min(startIndex + (int) pageSize, performanceContractsList.size());

            // 设置当前页记录
            page.setRecords(performanceContractsList.subList(startIndex, endIndex));

            return page;
        }

        return null;
    }

    /**
     * 修改争议评分
     *
     * @param argueScoreRequest
     * @return
     */
    @Override
    public boolean updateScore(ArgueScoreRequest argueScoreRequest) {

        String date = getCurrentDateAsDate();
        QueryWrapper<Publicity> pubWrapper = new QueryWrapper<>();
        pubWrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(pubWrapper);
        if (one == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "本月还未发布业绩合同!");
        }
        // 还未处于修改阶段
        if (one.getIsAdjust() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前时间未处于调整阶段！");
        }
        Long id = argueScoreRequest.getId();
        Double score = argueScoreRequest.getScore();

        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", id);
        wrapper.eq("assessment_time", date);
        ContractsScore byId = contractsScoreService.getOne(wrapper);
        byId.setScore(score);
        boolean b = contractsScoreService.updateById(byId);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改失败");
        }
        return true;
    }

    @Override
    @Transactional
    public boolean publicRes() {
        String date = getCurrentDateAsDate();
        // 将所有打分表锁定
        contractsScoreMapper.lockRes(date);
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "本月尚未发布考评！");
        }
        if (one.getIsPublic() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前正处于公示期！");
        }
        one.setIsPublic(1);
        // 存储确认结果表
        QueryWrapper<PerformanceContracts> wrapper1 = new QueryWrapper<>();
        wrapper1.select("assessed_unit", "assessed_people");
        List<PerformanceContracts> list = performanceContractsService.list(wrapper1);

        // 使用 Set 去重
        Set<String> uniqueKeys = new HashSet<>();
        List<PerformanceContracts> uniqueList = new ArrayList<>();
        for (PerformanceContracts pc : list) {
            // 定义唯一性条件，这里是 unit 和 people 的组合
            String uniqueKey = pc.getAssessed_unit() + "_" + pc.getAssessed_people();
            if (uniqueKeys.add(uniqueKey)) { // 如果添加成功，说明是第一次出现
                uniqueList.add(pc);
            }
        }

        // 将去重后的列表转换为 Confirm 列表
        List<Confirm> confirms = new ArrayList<>();
        for (PerformanceContracts pc : uniqueList) {
            Confirm confirm = new Confirm();
            confirm.setName(pc.getAssessed_people());
            confirm.setAssessment_time(date);
            confirm.setUnit(pc.getAssessed_unit());
            confirms.add(confirm);
        }

        boolean b = confirmService.saveBatch(confirms);
        if (!b) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公示确认表保存失败！");
        }
        return publicityService.updateById(one);
    }

    @Override
    public boolean unPublicRes() {
        String date = getCurrentDateAsDate();
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "本月尚未发布考评！");
        }
        if (one.getIsPublic() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "还未开启公示或公示已结束");
        }
        one.setIsPublic(0);

        return publicityService.updateById(one);
    }

    @Override
    public boolean freeze() {
        String date = getCurrentDateAsDate();
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "本月尚未发布考评！");
        }
        one.setIsFreeze(1);
        return publicityService.updateById(one);
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

    @Override
    public List<String> getUnscoreDeptsForEmail() {
        // 查询还未打分的细则id
        QueryWrapper<ContractsScore> scoreWrapper = new QueryWrapper<>();
        String date = getCurrentDateAsDate();
        scoreWrapper.eq("assessment_time", date);
        scoreWrapper.eq("is_lock", 0);
        List<ContractsScore> contractsScores = contractsScoreService.list(scoreWrapper);
        // 还未提交的合同的id集合
        List<Long> collect = contractsScores.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return new ArrayList<>();
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
    @Transactional
    public boolean AutoPublicRes() {
        String date = getCurrentDateAsDate();
        // 将所有打分表锁定
        contractsScoreMapper.lockRes(date);
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null) {
            return false;
        }
        if (one.getIsPublic() == 1) {
            return false;
        }
        one.setIsPublic(1);
        // 存储确认结果表
        QueryWrapper<PerformanceContracts> wrapper1 = new QueryWrapper<>();
        wrapper1.select("assessed_unit", "assessed_people");
        List<PerformanceContracts> list = performanceContractsService.list(wrapper1);

        // 使用 Set 去重
        Set<String> uniqueKeys = new HashSet<>();
        List<PerformanceContracts> uniqueList = new ArrayList<>();
        for (PerformanceContracts pc : list) {
            // 定义唯一性条件，这里是 unit 和 people 的组合
            String uniqueKey = pc.getAssessed_unit() + "_" + pc.getAssessed_people();
            if (uniqueKeys.add(uniqueKey)) { // 如果添加成功，说明是第一次出现
                uniqueList.add(pc);
            }
        }

        // 将去重后的列表转换为 Confirm 列表
        List<Confirm> confirms = new ArrayList<>();
        for (PerformanceContracts pc : uniqueList) {
            Confirm confirm = new Confirm();
            confirm.setName(pc.getAssessed_people());
            confirm.setAssessment_time(date);
            confirm.setUnit(pc.getAssessed_unit());
            confirms.add(confirm);
        }

        boolean b = confirmService.saveBatch(confirms);
        if (!b) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公示确认表保存失败！");
        }
        return publicityService.updateById(one);
    }

    /**
     * 自动结束公示
     *
     * @return
     */
    @Override
    public boolean AutoUnPublicRes() {
        String date = getCurrentDateAsDate();
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null) {
            return false;
        }
        if (one.getIsPublic() == 0) {
            return false;
        }
        one.setIsPublic(0);
        return publicityService.updateById(one);
    }

    /**
     * 冻结，流程结束
     *
     * @return
     */
    @Override
    public boolean AutoFreezen() {
        String date = getCurrentDateAsDate();
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null) {
            return false;
        }
        one.setIsFreeze(1);
        return publicityService.updateById(one);
    }

    /**
     * 调整阶段
     *
     * @return
     */
    @Override
    public boolean adjust() {
        String date = getCurrentDateAsDate();
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null) {
            return false;
        }
        one.setIsAdjust(1);
        return publicityService.updateById(one);
    }

    /**
     * 调整结束
     *
     * @return
     */
    @Override
    public boolean overAdjust() {
        String date = getCurrentDateAsDate();
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null) {
            return false;
        }
        one.setIsAdjust(0);
        return publicityService.updateById(one);
    }

    @Override
    public Map<String, String> getDisList() {
        String date = getCurrentDateAsDate();
        QueryWrapper<Confirm> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        wrapper.eq("isDispute", 1);
        Map<String, String> res = new HashMap<>();
        List<Confirm> list = confirmService.list(wrapper);
        if (list.size() == 0) {
            return res;
        }
        for (Confirm confirm : list) {
            res.put(confirm.getUnit(), confirm.getName());
        }
        return res;
    }
}
