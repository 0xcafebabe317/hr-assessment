package com.telecom.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.telecom.project.annotation.AuthCheck;
import com.telecom.project.common.BaseResponse;
import com.telecom.project.common.ErrorCode;
import com.telecom.project.common.ResultUtils;
import com.telecom.project.exception.BusinessException;
import com.telecom.project.model.dto.contracts.AnnouncementRequest;
import com.telecom.project.model.dto.hr.ArgueScoreRequest;
import com.telecom.project.model.dto.hr.ScorePageRequest;
import com.telecom.project.model.dto.hr.YearMonthRequest;
import com.telecom.project.model.entity.Announcement;
import com.telecom.project.model.entity.PerformanceContracts;
import com.telecom.project.model.entity.Publicity;
import com.telecom.project.service.AnnouncementService;
import com.telecom.project.service.HrService;
import com.telecom.project.service.PublicityService;
import com.telecom.project.utils.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.telecom.project.utils.DateUtil.getCurrentDateAsDate;

/**
 * @author: Toys
 * 人力操作
 * @date: 2024年11月01 15:22
 **/
@RestController
@RequestMapping("hr")
@Slf4j
public class HrController {

    @Resource
    private HrService hrService;

    @Resource
    private PublicityService publicityService;

    @Resource
    private AnnouncementService announcementService;


    /**
     * 发布当月打分表
     *
     * @return
     */
    @RequestMapping("/publish")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> publish() {
        boolean res = hrService.publish();
        return ResultUtils.success(res);
    }

    /**
     * 锁定评分表
     *
     * @return
     */
    @RequestMapping("/lock")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> lock() {
        boolean res = hrService.lock();
        return ResultUtils.success(res);
    }

    /**
     * 还未给打分的人并且发送邮件提醒
     *
     * @return
     */
    @RequestMapping("/remind/and/output")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> remind() {
        boolean res = hrService.remindAndSend();
        return ResultUtils.success(res);
    }

    /**
     * 查看还未打分部门的名单
     *
     * @return
     */
    @RequestMapping("/unscore/dept")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<List<String>> getUnscoreDepts() {
        List<String> res = hrService.getUnscoreDepts();
        return ResultUtils.success(res);
    }

    /**
     * 导出业绩合同excel
     *
     * @param response
     * @throws IOException
     */
    @RequestMapping("export/excel")
    @AuthCheck(mustRole = "hr")
    public void exportExcel(
            @RequestBody YearMonthRequest yearMonth, // 接收 "2024年1月" 这样的字符串
            HttpServletResponse response
    ) throws IOException {

        String yearmonth = yearMonth.getYearmonth();
        // 检查参数格式是否正确
        if (!yearmonth.matches("\\d{4}年\\d{1,2}月")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数格式不正确，应为 'YYYY年M月'！");
        }
        // 查询条件：根据年份和月份筛选
        QueryWrapper<Publicity> pubWrapper = new QueryWrapper<>();
        pubWrapper.eq("assessment_time", yearmonth);
        Publicity one = publicityService.getOne(pubWrapper);

        // 检查流程是否已冻结
        if (one == null || one.getIsFreeze() == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前评分流程尚未开始或尚未结束，请于本月23日18:30后导出结果！");
        }

        // 设置文件响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=result_" + yearMonth + ".xlsx");

        // 获取数据并写入 Excel
        try (ByteArrayOutputStream outputStream = ExcelUtil.exportUsersToExcel(hrService.getAllContracts(yearmonth));
             ServletOutputStream responseStream = response.getOutputStream()) {
            responseStream.write(outputStream.toByteArray());
            responseStream.flush();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "导出失败，请重试！");
        }
    }



    /**
     * 发布公告
     *
     * @param announcementRequest
     * @return
     */
    @RequestMapping("publish/announcement")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> publishAnnouncement(@RequestBody AnnouncementRequest announcementRequest) {
        String content = announcementRequest.getContent();
        Announcement announcement = new Announcement();
        announcement.setContent(content);
        boolean save = announcementService.save(announcement);
        return ResultUtils.success(save);
    }

    /**
     * 获取公告内容
     *
     * @return
     */
    @RequestMapping("get/announcement")
    public BaseResponse<Announcement> getAnnouncement() {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("createTime");  // 根据插入时间降序排列
        List<Announcement> announcements = announcementService.list(wrapper);  // 获取公告列表
        if (announcements != null && !announcements.isEmpty()) {
            // 获取最新的公告（列表中的第一条）
            Announcement latestAnnouncement = announcements.get(0);
            return ResultUtils.success(latestAnnouncement);  // 返回最新的公告
        }
        return ResultUtils.success(new Announcement());  // 如果没有公告，返回空公告
    }


    /**
     * 获取所有打分页面
     *
     * @param scorepageRequest
     * @param request
     * @return
     */
    @RequestMapping("get/contracts/score")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Page<PerformanceContracts>> getContractsScore(@RequestBody ScorePageRequest scorepageRequest, HttpServletRequest request) {
        Page<PerformanceContracts> res = hrService.getContractsScore(scorepageRequest, request);
        return ResultUtils.success(res);
    }

    /**
     * 修改争议评分
     *
     * @param argueScoreRequest
     * @return
     */
    @RequestMapping("argue/score")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> ScoreArgue(@RequestBody ArgueScoreRequest argueScoreRequest) {
        if (argueScoreRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "未修改数据");
        }
        boolean res = hrService.updateScore(argueScoreRequest);
        return ResultUtils.success(res);
    }

    /**
     * 公示结果
     *
     * @return
     */
    @RequestMapping("/public")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> Public() {
        boolean res = hrService.publicRes();
        return ResultUtils.success(res);
    }

    /**
     * 结束公示
     *
     * @return
     */
    @RequestMapping("/unPublic")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> unPublic() {
        boolean res = hrService.unPublicRes();
        return ResultUtils.success(res);
    }

    /**
     * 冻结
     *
     * @return
     */
    @RequestMapping("/freeze")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> freeze() {
        boolean res = hrService.freeze();
        return ResultUtils.success(res);
    }

    /**
     * 争议名单
     * @return
     */
    @RequestMapping("/get/dispute/list")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Map<String,String>> getDisputeList() {
        Map<String,String> res =  hrService.getDisList();
        return ResultUtils.success(res);
    }

    /**
     * 获得总分
     * @return
     */
    @RequestMapping("/get/all/score")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Map<String,String>> getAllScore() {
        Map<String,String> res =  hrService.getDisList();
        return ResultUtils.success(res);
    }




}
