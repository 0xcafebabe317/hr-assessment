package com.telecom.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.telecom.project.annotation.AuthCheck;
import com.telecom.project.common.BaseResponse;
import com.telecom.project.common.ResultUtils;
import com.telecom.project.model.dto.contracts.AnnouncementRequest;
import com.telecom.project.model.entity.Announcement;
import com.telecom.project.service.AnnouncementService;
import com.telecom.project.service.HrService;
import com.telecom.project.utils.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
    private AnnouncementService announcementService;


    /**
     * 发布当月打分表
     *
     * @return
     */
    @RequestMapping("/publish")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> publish(HttpServletRequest request) {
        boolean res = hrService.publish(request);
        return ResultUtils.success(res);
    }

    /**
     * 计算打分表
     *
     * @return
     */
    @RequestMapping("/lock")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> lock(HttpServletRequest request) {
        boolean res = hrService.lock(request);
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
     * @param response
     * @throws IOException
     */
    @RequestMapping("export/excel")
    @AuthCheck(mustRole = "hr")
    public void exportExcel(HttpServletResponse response) throws IOException {
        // 设置文件响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=contracts.xlsx");

        // 获取数据并写入 Excel
        ByteArrayOutputStream outputStream = ExcelUtil.exportUsersToExcel(hrService.getAllContracts());
        response.getOutputStream().write(outputStream.toByteArray());
    }


    @RequestMapping("publish/announcement")
    @AuthCheck(mustRole = "hr")
    public BaseResponse<Boolean> publishAnnouncement (@RequestBody AnnouncementRequest announcementRequest)  {
        String content = announcementRequest.getContent();
        Announcement announcement = new Announcement();
        announcement.setContent(content);
        boolean save = announcementService.save(announcement);
        return ResultUtils.success(save);
    }

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


}
