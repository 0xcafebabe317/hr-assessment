package com.telecom.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.telecom.project.model.entity.Announcement;
import com.telecom.project.service.AnnouncementService;
import com.telecom.project.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

/**
* @author tiscy
* @description 针对表【announcement(评分结果表)】的数据库操作Service实现
* @createDate 2024-11-12 17:48:13
*/
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
    implements AnnouncementService{

}




