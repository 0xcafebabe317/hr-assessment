package com.telecom.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.telecom.project.common.ErrorCode;
import com.telecom.project.common.IdRequest;
import com.telecom.project.common.PageRequest;
import com.telecom.project.exception.BusinessException;
import com.telecom.project.model.dto.contracts.*;
import com.telecom.project.model.entity.*;
import com.telecom.project.model.vo.ContractsVO;
import com.telecom.project.service.*;
import com.telecom.project.mapper.PerformanceContractsMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.telecom.project.utils.DateUtil.getCurrentDateAsDate;

/**
 * @author tiscy
 * @description 针对表【performance_contracts(业绩合同表)】的数据库操作Service实现
 * @createDate 2024-10-30 12:13:12
 */
@Service
public class PerformanceContractsServiceImpl extends ServiceImpl<PerformanceContractsMapper, PerformanceContracts>
        implements PerformanceContractsService {

    @Resource
    private UserService userService;

    @Resource
    private DeptService deptService;

    @Resource
    private CenterService centerService;

    @Resource
    private CustomerManagerService customerManagerService;

    @Resource
    private BuManagerService buManagerService;

    @Resource
    private ContractsScoreService contractsScoreService;

    @Override
    public List<String> getAssessedUnit() {
        List<Dept> list = deptService.list();
        return list.stream().map(Dept::getDept_name).collect(Collectors.toList());
    }

    @Override
    public boolean addContracts(PerformanceContracts performanceContracts) {
        PerformanceContracts pc = new PerformanceContracts();
        // 大类
        String categories = performanceContracts.getCategories();
        pc.setCategories(categories);
        // 小类
        String sub_categories = performanceContracts.getSub_categories();
        pc.setSub_categories(sub_categories);
        // 指标
        String indicators = performanceContracts.getIndicators();
        pc.setIndicators(indicators);
        // 考核部门
        String assessment_dept = performanceContracts.getAssessment_dept();
        pc.setAssessment_dept(assessment_dept);
        // 权重
        Integer weight = performanceContracts.getWeight();
        if (weight <= 0 || weight >= 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权重范围应在1～99");
        }
        pc.setWeight(weight);
        // 记分方法
        String scoring_method = performanceContracts.getScoring_method();
        pc.setScoring_method(scoring_method);
        // 考核周期
        String assessment_cycle = performanceContracts.getAssessment_cycle();
        pc.setAssessment_cycle(assessment_cycle);
        // 被考核单位
        String assessed_unit = performanceContracts.getAssessed_unit();
        pc.setAssessed_unit(assessed_unit);
        // 被考核中心
        String assessed_center = performanceContracts.getAssessed_center();
        pc.setAssessed_center(assessed_center);
        // 被考核人
        String assessed_people = performanceContracts.getAssessed_people();
        pc.setAssessed_people(assessed_people);
        // 其他
        String other = performanceContracts.getOther();
        pc.setOther(other);
        return this.save(pc);
    }

    @Override
    public List<String> getAssessedCenter() {
        List<Center> list = centerService.list();
        return list.stream().map(Center::getCenter_name).collect(Collectors.toList());
    }

    @Override
    public List<String> getCustomerManager(CustomerRequest customerRequest) {
        String unit = customerRequest.getUnit();
        String center = customerRequest.getCenter();
        QueryWrapper<CustomerManager> wrapper = new QueryWrapper<>();
        wrapper.eq("addr", unit);
        wrapper.eq("center", center);
        List<CustomerManager> list = customerManagerService.list(wrapper);
        return list.stream().map(CustomerManager::getName).collect(Collectors.toList());
    }

    @Override
    public List<String> getBuManager(BuRequest buRequest) {
        String bu = buRequest.getBu();
        String role = buRequest.getRole();
        QueryWrapper<BuManager> wrapper = new QueryWrapper<>();
        wrapper.eq("addr", bu);
        wrapper.eq("role", role);
        List<BuManager> list = buManagerService.list(wrapper);
        return list.stream().map(BuManager::getName).collect(Collectors.toList());
    }

    @Override
    public List<PerformanceContracts> getContracts(ContractsRequest contractsRequest) {
        String assessedUnit = contractsRequest.getAssessedUnit();
        String assessedCenter = contractsRequest.getAssessedCenter();
        String assessedPeople = contractsRequest.getAssessedPeople();
        QueryWrapper<PerformanceContracts> wrapper = new QueryWrapper<>();
        // 支撑部门
        if (StringUtils.containsAny(assessedUnit, "人力部", "安保部", "工会", "财务部", "党群工作部", "纪委办公室", "综合办公室", "云网运营部", "云网发展部", "云中台", "全渠道", "客户服务部", "政企客户部", "市场部")) {
            wrapper.eq("assessed_unit", assessedUnit);
            return this.list(wrapper);
            // 县局
        } else if (StringUtils.containsAny(assessedUnit, "巴宜区", "帮宗", "米林", "墨脱", "波密", "工布江达", "察隅", "朗县")) {
            wrapper.eq("assessed_unit", assessedUnit);
            wrapper.eq("assessed_center", assessedCenter);
            wrapper.eq("assessed_people", assessedPeople);
            List<PerformanceContracts> list = this.list(wrapper);
            return list;
            // bu
        } else {
            wrapper.eq("assessed_center", assessedUnit);
            wrapper.eq("assessed_center", assessedCenter);
            wrapper.eq("assessed_people", assessedPeople);
            return this.list(wrapper);
        }
    }

    @Override
    public Page<PerformanceContracts> getContractsScore(PageRequest pageRequest, HttpServletRequest request) {
        long current = pageRequest.getCurrent();
        long pageSize = pageRequest.getPageSize();

        User loginUser = userService.getLoginUser(request);
        String userDept = loginUser.getUserDept();
        String userRole = loginUser.getUserRole();
        // 确定是打分人
        if (!userRole.equals("score")) {
            return null;
        }

        // 本部门要打分的细则
        QueryWrapper<PerformanceContracts> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_dept", userDept);
        List<PerformanceContracts> list = this.list(wrapper);

        // 本部门要打分的id集合
        List<Long> ids = list.stream().map(PerformanceContracts::getId).collect(Collectors.toList());

        QueryWrapper<ContractsScore> scoreQueryWrapper = new QueryWrapper<>();
        // 在打分表中去中id相同并且未锁定的打分合同
        scoreQueryWrapper.in("contract_id", ids) // contract_id 在 ids 列表中
                .eq("is_lock", 0);

        // 得到打分合同
        List<ContractsScore> contractsScores = contractsScoreService.list(scoreQueryWrapper);

        // 打分合同中的合同id集合，这就是全部要返回的id集合
        List<Long> contractsScoresIds = contractsScores.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());

        // 最终业绩合同
        if (!contractsScoresIds.isEmpty()) {
            List<PerformanceContracts> performanceContractsList = this.listByIds(contractsScoresIds);

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


    @Override
    public boolean score(ScoreRequest scoreRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String userName = loginUser.getUserName();
        Map<Long, Double> result = scoreRequest.getResult();
        if (result.size() == 0) {
            return true;
        }
        // 获取所有打分的id
        Set<Long> ids = result.keySet();
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.in("contract_id", ids); // 根据ids查找

        // 查询ContractsScore
        List<ContractsScore> scores = contractsScoreService.list(wrapper);
        // 遍历并赋值
        for (ContractsScore score : scores) {
            Double newScore = result.get(score.getContract_id());
            if (newScore != null) {
                score.setScore(newScore); // 假设ContractsScore有setScore方法
                score.setAssessment_people(userName);
            }
        }
        // 批量更新
        return contractsScoreService.updateBatchById(scores);
    }

    @Override
    public boolean saveResult(ScoreRequest scoreRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String userName = loginUser.getUserName();
        Map<Long, Double> result = scoreRequest.getResult();
        if (result.size() == 0) {
            return true;
        }
        // 获取所有打分的id
        Set<Long> ids = result.keySet();
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.in("contract_id", ids); // 根据ids查找

        // 查询ContractsScore
        List<ContractsScore> scores = contractsScoreService.list(wrapper);
        // 遍历并赋值
        for (ContractsScore score : scores) {
            Double newScore = result.get(score.getContract_id());
            if (newScore != null) {
                score.setScore(newScore);
                score.setAssessment_people(userName);
                score.setIs_lock(1);
            }
        }
        // 批量更新
        return contractsScoreService.updateBatchById(scores);
    }

    @Override
    public Map<Long, Double> getTempScore(TempScoreRequest tempScoreRequest) {
        List<Long> ids = tempScoreRequest.getIds();
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.in("contract_id", ids);
        List<ContractsScore> list = contractsScoreService.list(wrapper);
        Map<Long, Double> map = new HashMap<>();
        for (ContractsScore contractsScore : list) {
            map.put(contractsScore.getContract_id(), contractsScore.getScore());
        }
        return map;
    }

    @Override
    public boolean updateContract(UpdateRequest updateRequest) {
        // 更新的合同Id
        Long id = updateRequest.getId();
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();

        // 当月时间
        String date = getCurrentDateAsDate();
        wrapper.eq("contract_id", id);
        wrapper.eq("assessment_time", date);
        ContractsScore one = contractsScoreService.getOne(wrapper);
        if (one != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前业绩合同正在进行打分，无法修改");
        }
        PerformanceContracts performanceContracts = new PerformanceContracts();
        BeanUtils.copyProperties(updateRequest, performanceContracts);
        boolean b = this.updateById(performanceContracts);
        if (!b) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新失败!");
        }
        return b;
    }

    @Override
    public boolean deleteContract(IdRequest idRequest) {
        // 删除合同的id
        Long id = idRequest.getId();
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();

        // 当月时间
        String date = getCurrentDateAsDate();
        wrapper.eq("contract_id", id);
        wrapper.eq("assessment_time", date);
        ContractsScore one = contractsScoreService.getOne(wrapper);
        if (one != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前业绩合同正在进行打分，无法删除");
        }
        boolean res = this.removeById(id);
        if (!res) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败!");
        }
        return res;
    }


}




