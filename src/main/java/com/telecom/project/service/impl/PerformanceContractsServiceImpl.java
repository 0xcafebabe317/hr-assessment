package com.telecom.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.Query;
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
import com.telecom.project.model.vo.ExcelVO;
import com.telecom.project.service.*;
import com.telecom.project.mapper.PerformanceContractsMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.telecom.project.utils.DateUtil.getCurrentDateAsDate;
import static com.telecom.project.utils.ExcelUtil.convertToExcelVO;

/**
 * @author tiscy
 * @description 针对表【performance_contracts(业绩合同表)】的数据库操作Service实现
 * @createDate 2024-10-30 12:13:12
 */
@Service
public class PerformanceContractsServiceImpl extends ServiceImpl<PerformanceContractsMapper, PerformanceContracts>
        implements PerformanceContractsService {

    @Resource
    private ConfirmService confirmService;

    @Resource
    private PublicityService publicityService;

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
        if (weight != null) {
            if (weight <= 0 || weight >= 100) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "权重范围应在1～99");
            }
            pc.setWeight(weight);
        }

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
        QueryWrapper<PerformanceContracts> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_dept", assessment_dept)
                .eq("scoring_method", scoring_method)
                .eq("assessed_people", assessed_people);
        List<PerformanceContracts> list = this.list(wrapper);
        if (list.size() > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该条数据已经存在！");
        }
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
            wrapper.eq("assessed_unit", assessedUnit);
            wrapper.eq("assessed_center", assessedCenter);
            wrapper.eq("assessed_people", assessedPeople);
            return this.list(wrapper);
        }
    }

    @Override
    public Page<PerformanceContracts> getContractsScore(UserScoreRequest userScoreRequest, HttpServletRequest request) {
        long current = userScoreRequest.getCurrent();
        long pageSize = userScoreRequest.getPageSize();
        String searchText = userScoreRequest.getSearchText();

        User loginUser = userService.getLoginUser(request);
        String userDept = loginUser.getUserDept();
        String userRole = loginUser.getUserRole();
        // 确定是打分人
        if (!userRole.equals("score") && !userRole.equals("hr")) {
            return null;
        }


        // 本部门要打分的细则
        QueryWrapper<PerformanceContracts> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_dept", userDept)
                .and(w -> w.like("assessed_people", searchText)
                        .or()
                        .like("assessed_unit", searchText)
                        .or()
                        .like("assessed_center", searchText));

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

    @Override
    public Page<PerformanceContracts> getPublicResult(PageRequest pageRequest, HttpServletRequest request) {
        String date = getCurrentDateAsDate();
        // 查询是否公示
        QueryWrapper<Publicity> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        Publicity one = publicityService.getOne(wrapper);
        if (one == null || one.getIsPublic() != 1L) {
            return null;
        }

        long current = pageRequest.getCurrent();
        long pageSize = pageRequest.getPageSize();

        User loginUser = userService.getLoginUser(request);
        String userDept = loginUser.getUserDept();
        String userName = loginUser.getUserName();


        QueryWrapper<PerformanceContracts> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("assessed_unit", userDept);
        wrapper1.eq("assessed_people", userName);
        List<PerformanceContracts> list = this.list(wrapper1);

        // 本部门要打分的id集合
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
    public boolean confirm(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String userDept = loginUser.getUserDept();
        String userName = loginUser.getUserName();
        String date = getCurrentDateAsDate();
        QueryWrapper<Confirm> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        wrapper.eq("unit", userDept);
        wrapper.eq("name", userName);
        Confirm one = confirmService.getOne(wrapper);
        if (one == null) {
            Confirm confirm = new Confirm();
            confirm.setName(userName);
            confirm.setUnit(userDept);
            confirm.setAssessment_time(date);
            confirm.setIsConfirm(1);
            return confirmService.save(confirm);
        }
        return false;
    }

    @Override
    public boolean isConfirm(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String userDept = loginUser.getUserDept();
        String userName = loginUser.getUserName();
        String date = getCurrentDateAsDate();
        QueryWrapper<Confirm> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        wrapper.eq("unit", userDept);
        wrapper.eq("name", userName);
        Confirm one = confirmService.getOne(wrapper);
        if (one == null) {
            return false;
        }
        if (one.getIsConfirm() == 1) {
            return true;
        }
        return false;
    }

    @Override
    public boolean dispute(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String userDept = loginUser.getUserDept();
        String userName = loginUser.getUserName();
        String date = getCurrentDateAsDate();
        QueryWrapper<Confirm> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        wrapper.eq("unit", userDept);
        wrapper.eq("name", userName);
        Confirm one = confirmService.getOne(wrapper);
        if (one == null) {
            Confirm confirm = new Confirm();
            confirm.setName(userName);
            confirm.setUnit(userDept);
            confirm.setAssessment_time(date);
            confirm.setIsDispute(1);
            return confirmService.save(confirm);
        }
        return false;
    }

    @Override
    public boolean isDispute(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String userDept = loginUser.getUserDept();
        String userName = loginUser.getUserName();
        String date = getCurrentDateAsDate();
        QueryWrapper<Confirm> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        wrapper.eq("unit", userDept);
        wrapper.eq("name", userName);
        Confirm one = confirmService.getOne(wrapper);
        if (one == null) {
            return false;
        }
        if (one.getIsDispute() == 1) {
            return true;
        }
        return false;
    }

    @Override
    public List<ExcelVO> getAllContracts(HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        String userDept = loginUser.getUserDept();
        // 本月时间
        String date = getCurrentDateAsDate();

        // 查询当月的合同评分记录
        QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_time", date);
        List<ContractsScore> list = contractsScoreService.list(wrapper);

        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当月没有可导出的业绩合同");
        }

        // 已锁定条数
        QueryWrapper<ContractsScore> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("assessment_time", date);

        // 获取所有评分记录
        List<ContractsScore> resList = contractsScoreService.list(wrapper1);
        List<Long> ids = resList.stream().map(ContractsScore::getContract_id).collect(Collectors.toList());

        // 根据合同 ID 获取 PerformanceContracts 数据
        QueryWrapper<PerformanceContracts> wrapper2 = new QueryWrapper<>();
        wrapper2.in("id", ids);
        wrapper2.eq("assessment_dept", userDept);

        List<PerformanceContracts> performanceContracts = this.list(wrapper2);

        // 将 ContractsScore 和 PerformanceContracts 转换成 ExcelVO
        List<ExcelVO> excelData = convertToExcelVO(resList, performanceContracts);

        return excelData;
    }

    @Override
    @Transactional
    public boolean saveRes(MultipartFile multipartFile) throws IOException {
        // 校验文件类型和大小
        if (multipartFile.isEmpty() || !multipartFile.getOriginalFilename().endsWith(".xlsx")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传Excel文件");
        }
        // 创建临时文件夹
        String tempDir = System.getProperty("user.dir") + File.separator + "excel_temp_" + System.currentTimeMillis();
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            // 将文件保存到临时文件夹中
            Path filePath = Paths.get(tempDir, multipartFile.getOriginalFilename());
            multipartFile.transferTo(filePath.toFile());

            // 存储子表数据：子表名 -> 子表内容
            Map<String, List<List<String>>> sheetDataMap = new HashMap<>();

            // 使用保存后的文件进行读取
            try (Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath.toFile()))) {
                int numberOfSheets = workbook.getNumberOfSheets();

                // 遍历每个子表（Sheet）
                for (int i = 0; i < numberOfSheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    String sheetName = sheet.getSheetName(); // 获取子表名称
                    // 存储当前子表的数据
                    List<List<String>> sheetContent = new ArrayList<>();

                    // 遍历行
                    for (Row row : sheet) {
                        List<String> rowData = new ArrayList<>();

                        // 遍历单元格
                        for (Cell cell : row) {
                            // 根据单元格类型获取内容
                            switch (cell.getCellType()) {
                                case STRING:
                                    rowData.add(cell.getStringCellValue());
                                    break;
                                case NUMERIC:
                                    rowData.add(String.valueOf(cell.getNumericCellValue()));
                                    break;
                                default:
                                    rowData.add(""); // 空单元格
                            }
                        }
                        sheetContent.add(rowData);
                    }
                    // 将当前子表的内容存入 Map
                    sheetDataMap.put(sheetName, sheetContent);
                }
            }
            return this.saveRes2Database(sheetDataMap);

        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据保存失败");
        } finally {
            deleteDirectory(dir);
        }
    }

    @Override
    public double getTotal(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<PerformanceContracts> wrapper = new QueryWrapper<>();
        wrapper.eq("assessed_people", loginUser.getUserName());
        List<PerformanceContracts> list = this.list(wrapper);
        List<Long> ids = list.stream().map(item -> item.getId()).collect(Collectors.toList());
        QueryWrapper<ContractsScore> wrapper1 = new QueryWrapper<>();
        wrapper1.in("contract_id", ids);
        List<ContractsScore> list1 = contractsScoreService.list(wrapper1);
        double totalScore = list1.stream()
                .map(item -> item.getScore()) // 获取每个 item 的 score
                .reduce(0.0, Double::sum);    // 累加所有 score 值，初始值为 0
        return totalScore;
    }

    /**
     * 根据名字获取总分
     *
     * @param name
     * @return
     */
    public double getScoreByName(String name) {
        QueryWrapper<PerformanceContracts> wrapper = new QueryWrapper<>();
        wrapper.eq("assessed_people", name);
        List<PerformanceContracts> list = this.list(wrapper);
        List<Long> ids = list.stream().map(item -> item.getId()).collect(Collectors.toList());
        QueryWrapper<ContractsScore> wrapper1 = new QueryWrapper<>();
        wrapper1.in("contract_id", ids);
        List<ContractsScore> list1 = contractsScoreService.list(wrapper1);
        double totalScore = list1.stream()
                .map(item -> item.getScore()) // 获取每个 item 的 score
                .reduce(0.0, Double::sum);    // 累加所有 score 值，初始值为 0
        return totalScore;
    }

    private boolean saveRes2Database(Map<String, List<List<String>>> sheetDataMap) {
        try {
            // 遍历每个子表
            for (Map.Entry<String, List<List<String>>> entry : sheetDataMap.entrySet()) {
                List<List<String>> sheetData = entry.getValue(); // 子表数据

                // 批量操作集合
                Map<Long, Double> scores = new HashMap<>();

                // 遍历当前子表的每一行，跳过第一行（表头）
                for (int rowIndex = 1; rowIndex < sheetData.size(); rowIndex++) {
                    List<String> row = sheetData.get(rowIndex);

                    // 数据提取（允许空值）
                    String column1 = row.size() > 2 ? row.get(2) : null; // 指标
                    String column2 = row.size() > 3 ? row.get(3) : null; // 考核部门
                    String column3 = row.size() > 7 ? row.get(7) : null; // 被考核单位
                    String column4 = row.size() > 8 ? row.get(8) : null; // 被考核中心
                    String column5 = row.size() > 9 ? row.get(9) : null; // 被考核人
                    String scoreStr = row.size() > 11 ? row.get(11) : "0"; // 得分（默认0）

                    double score;
                    try {
                        score = Double.parseDouble(scoreStr); // 转换得分
                    } catch (NumberFormatException e) {
                        score = 0.0; // 解析失败时默认得分为 0
                    }

                    // 查询 PerformanceContracts 表
                    QueryWrapper<PerformanceContracts> wrapper = new QueryWrapper<>();
                    wrapper.eq("indicators", column1)
                            .eq("assessment_dept", column2)
                            .eq("assessed_unit", column3)
                            .eq("assessed_center", column4)
                            .eq("assessed_people", column5);

                    PerformanceContracts one = this.getOne(wrapper);
                    if (one != null) { // 如果找到对应记录
                        Long id = one.getId();
                        scores.put(id, score); // 保存到批量操作集合
                    } else {
                        // 日志记录或特殊处理：无法找到匹配的合同
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "未发布该合同！");
                    }
                }

                String date = getCurrentDateAsDate(); // 获取当前日期作为字符串

                // 批量更新操作
                for (Map.Entry<Long, Double> each : scores.entrySet()) {
                    Long id = each.getKey();
                    Double score = each.getValue();

                    // 查询 ContractsScore 表
                    QueryWrapper<ContractsScore> wrapper = new QueryWrapper<>();
                    wrapper.eq("contract_id", id)
                            .eq("assessment_time", date);

                    ContractsScore contractScore = contractsScoreService.getOne(wrapper);
                    if (contractScore != null) {
                        contractScore.setScore(score); // 更新得分
                        contractsScoreService.updateById(contractScore);
                    }
                }
                System.out.println(entry.getKey() + "已保存");
            }
        } catch (Exception e) {
            // 记录错误日志以便调试
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "批量评分失败!");
        }
        return true; // 返回成功
    }


    /**
     * 删除文件夹及内容
     *
     * @param dir
     * @throws IOException
     */
    private void deleteDirectory(File dir) throws IOException {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteDirectory(file);
                    }
                }
            }
            Files.delete(dir.toPath());
        }
    }

}




