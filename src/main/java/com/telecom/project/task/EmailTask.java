package com.telecom.project.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.telecom.project.common.ErrorCode;
import com.telecom.project.exception.BusinessException;
import com.telecom.project.manage.mail.MailService;
import com.telecom.project.model.entity.User;
import com.telecom.project.service.HrService;
import com.telecom.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.telecom.project.utils.DateUtil.getCurrentDateAsDate;

/**
 * @author: Toys
 * @date: 2024年11月27 11:58
 **/
@Component
@Slf4j
public class EmailTask {

    @Resource
    private MailService mailService;

    @Resource
    private HrService hrService;

    @Resource
    private UserService userService;

    /**
     * 如果未评分每天提醒，每天早上9：30执行
     */
    @Scheduled(cron = "0 30 9 * * ?")
    public void remindScore() {
        List<String> unscoreDeptsForEmail = hrService.getUnscoreDeptsForEmail();
        // 如果所有部门都已经打分，则不执行
        if (unscoreDeptsForEmail.isEmpty()) {
            return;
        }
        // 需要发送的邮箱
        List<String> mailList = new ArrayList<>();
        for (String dept : unscoreDeptsForEmail) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("userDept", dept);
            userQueryWrapper.eq("userRole", "score");
            User one = userService.getOne(userQueryWrapper);
            if ((one == null) || StringUtils.isBlank(one.getEmail())) {
                log.warn("用户部门：{}，评分用户不存在或邮箱为空，跳过发送", dept);
                continue; // 跳过当前用户
            }
            mailList.add(one.getEmail());
        }
        // 发邮件提醒
        if (mailList.isEmpty()) {
            log.info("没有需要提醒的邮箱，邮件发送任务结束");
            return;
        }
        String date = getCurrentDateAsDate();
        String subject = "【" + date + "业绩考核评分提醒】";
        String content = "请于" + date + "18日下午18:30之前前往业绩考核评分系统进行评分，过时将会锁定评分，待公示期后前往人力资源部进行补评。评分地址：118.25.230.183";
        for (String mail : mailList) {
            mailService.sendSimpleMail(mail, subject, content);
        }
        log.info("{}提醒邮件已成功发送", date);
    }


    /**
     * 每月15日上午9：30开始发布评分表
     */
    @Scheduled(cron = "0 30 9 15 * ?")
    public void pubScoreTable() {
        boolean lock = hrService.publish();
        String date = getCurrentDateAsDate();
        if (lock) {
            log.info(date + "评分表已发布！");
        }
    }

    /**
     * 每月18日下午18：30锁定评分表
     */
    @Scheduled(cron = "0 30 18 18 * ?")
    public void lockScore() {
        boolean lock = hrService.lock();
        String date = getCurrentDateAsDate();
        if (lock) {
            log.info(date + "评分表已锁定！");
        }
    }

    /**
     * 每月16日早上9：30开始公示
     */
    @Scheduled(cron = "0 30 9 19 * ?")
    public void publicRes() {
        String date = getCurrentDateAsDate();
        boolean b = hrService.AutoPublicRes();
        if (b) {
            log.info(date + "评分结果已经开始公示！");
        }
    }

    /**
     * 每月18日下午18：30结束公示
     */
    @Scheduled(cron = "0 30 18 21 * ?")
    public void OverPubRes() {
        String date = getCurrentDateAsDate();
        boolean b = hrService.AutoUnPublicRes();
        if (b) {
            log.info(date + "评分结果已经结束公示！");
        }
    }

    /**
     * 每月22日9:30-23日18:30调整
     */
    @Scheduled(cron = "0 30 9 22 * ?")
    public void adjust() {
        String date = getCurrentDateAsDate();
        boolean b = hrService.adjust();
        if (b) {
            log.info(date + "业绩合同开始调整流程");
        }
    }

    /**
     * 每月22日9:30-23日18:30调整
     */
    @Scheduled(cron = "0 30 18 23 * ?")
    public void overAdjust() {
        String date = getCurrentDateAsDate();
        boolean b = hrService.overAdjust();
        if (b) {
            log.info(date + "业绩合同结束调整流程！");
        }
    }


    /**
     * 每月23日18：:30冻结，冻结后人力不可修改评分
     */
    @Scheduled(cron = "0 30 18 23 * ?")
    public void freeze() {
        String date = getCurrentDateAsDate();
        boolean b = hrService.AutoFreezen();
        if (b) {
            log.info(date + "评分流程已冻结，不可修改！");
        }
    }


}
