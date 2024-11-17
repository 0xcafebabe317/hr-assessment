package com.telecom.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.telecom.project.model.entity.BuManager;
import com.telecom.project.service.BuManagerService;
import com.telecom.project.mapper.BuManagerMapper;
import org.springframework.stereotype.Service;

/**
* @author tiscy
* @description 针对表【bu_manager(bu及客户经理表)】的数据库操作Service实现
* @createDate 2024-10-31 19:48:43
*/
@Service
public class BuManagerServiceImpl extends ServiceImpl<BuManagerMapper, BuManager>
    implements BuManagerService{

}




