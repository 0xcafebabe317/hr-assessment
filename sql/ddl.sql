-- 创建库
create database if not exists Hr_assessment;

-- 切换库
use Hr_assessment;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userName     varchar(256)                           null comment '用户姓名',
    userAccount  varchar(256)                           not null comment '账号',
    userDept     varchar(256)                           not null comment '用户部门',
    userRole     varchar(256) default 'user'            not null comment '用户权限：hr/score/user',
    userPassword varchar(512)                           not null comment '密码',
    email        varchar(256)                           null comment '邮箱',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    constraint uni_userAccount
        unique (userAccount)
) comment '用户';

-- 业绩合同表
create table if not exists performance_contracts

(
    id               bigint auto_increment comment 'id' primary key,
    categories       varchar(256)                       not null comment '大类名称',
    sub_categories   varchar(256)                       not null comment '小类名称',
    indicators       varchar(256)                       not null comment '指标',
    assessment_dept  varchar(256)                       not null comment '考核部门',
    weight           int                                not null comment '权重',
    scoring_method   varchar(512)                       not null comment '记分方法',
    assessment_cycle varchar(256)                       not null comment '考核周期',
    assessed_unit    varchar(256)                       not null comment '被考核单位',
    assessed_center  varchar(256)                       not null comment '被考核中心',
    assessed_people  varchar(256)                       not null comment '被考核人',
    other            varchar(256)                       null comment '其他',
    createTime       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint  default 0                 not null comment '是否删除'
) comment '业绩合同表';

-- 部门表
create table if not exists dept
(
    id        bigint auto_increment comment 'id' primary key,
    dept_name varchar(256) not null comment '部门名称'
) comment '部门表';

-- 中心表
create table if not exists center
(
    id          bigint auto_increment comment 'id' primary key,
    center_name varchar(256) not null comment '中心名称'
) comment '中心表';

-- 客户经理表
create table if not exists customer_manager
(
    id     bigint auto_increment comment 'id' primary key,
    name   varchar(256) not null comment '客户经理名字',
    addr   varchar(256) not null comment '归属县局',
    center varchar(256) not null comment '归属中心'
) comment '客户经理表';


-- bu及客户经理表
create table if not exists bu_manager
(
    id   bigint auto_increment comment 'id' primary key,
    name varchar(256) not null comment '名字',
    addr varchar(256) not null comment '归属bu',
    role varchar(256) not null comment '角色:负责人/客户经理'
) comment 'bu及客户经理表';

-- 打分表
create table if not exists contracts_score

(
    id                bigint auto_increment comment 'id' primary key,
    contract_id       bigint                                  not null comment '合同id',
    score             double(10, 2) default 0                 null comment '得分',
    assessment_time   varchar(256)                            not null comment '考核时间',
    assessment_people varchar(256)                            null comment '评分人',
    is_lock           tinyint       default 0                 not null comment '是否锁定',
    createTime        datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime        datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete          tinyint       default 0                 not null comment '是否删除'
) comment '打分表';


-- 公告表
create table if not exists announcement
(
    id         bigint auto_increment comment 'id' primary key,
    content    varchar(2048) default '暂无公告'        not null comment '公告内容',
    createTime datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint       default 0                 not null comment '是否删除'
) comment '公告表';

-- 功能表
create table if not exists publicity
(
    id              bigint auto_increment comment 'id' primary key,
    assessment_time varchar(256)                       not null comment '考核时间',
    isPublic        tinyint  default 0                 not null comment '是否公示：0-不 1-是 ',
    isFreeze        tinyint  default 0                 not null comment '是否冻结',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除'
) comment '功能表';

-- 公示确认争议表
create table if not exists confirm
(
    id              bigint auto_increment comment 'id' primary key,
    assessment_time varchar(256)                       not null comment '考核时间',
    unit            varchar(256)                       not null comment '被考核单位',
    name            varchar(256)                       not null comment '被考核人',
    isConfirm       tinyint  default 0                 not null comment '是否确认：0-没有 1-确认 ',
    isDispute       tinyint  default 0                 not null comment '是否有争议：0-没有 1-有',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint  default 0                 not null comment '是否删除'
) comment '公示确认争议表';