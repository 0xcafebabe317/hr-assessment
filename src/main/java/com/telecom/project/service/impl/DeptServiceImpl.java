package com.telecom.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.telecom.project.model.entity.Dept;
import com.telecom.project.service.DeptService;
import com.telecom.project.mapper.DeptMapper;
import org.springframework.stereotype.Service;

/**
* @author tiscy
* @description 针对表【dept(部门表)】的数据库操作Service实现
* @createDate 2024-10-31 01:23:57
*/
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept>
    implements DeptService{

}




