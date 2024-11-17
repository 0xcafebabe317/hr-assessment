package com.telecom.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.telecom.project.service.CustomerManagerService;
import com.telecom.project.model.entity.CustomerManager;
import com.telecom.project.mapper.CustomerManagerMapper;
import org.springframework.stereotype.Service;

/**
* @author tiscy
* @description 针对表【customer_manager(客户经理表)】的数据库操作Service实现
* @createDate 2024-10-31 18:32:59
*/
@Service
public class CustomerManagerServiceImpl extends ServiceImpl<CustomerManagerMapper, CustomerManager>
    implements CustomerManagerService {

}




