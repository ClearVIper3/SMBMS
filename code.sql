-- auto-generated definition
create table smbms_address
(
    id           bigint      null,
    contact      varchar(15) null,
    addressDesc  varchar(50) null,
    postCode     varchar(15) null,
    tel          varchar(20) null,
    createdBy    bigint      null,
    creationDate datetime    null,
    modifyBy     bigint      null,
    modifyDate   datetime    null,
    userId       bigint      null
);

-- auto-generated definition
create table smbms_bill
(
    id           bigint auto_increment comment 'id'
        primary key,
    billCode     varchar(30)    null comment '账单编码',
    productName  varchar(20)    null comment '商品名称',
    productDesc  varchar(50)    null comment '商品描述',
    productUnit  varchar(60)    null comment '商品数量',
    productCount decimal(20, 2) null comment '总金额',
    totalPrice   decimal(20, 2) null comment '是否支付',
    isPayment    int            null comment '供应商ID',
    createdBy    bigint         null comment '创建者',
    creationDate datetime       null comment '创建时间',
    modifyBy     bigint         null comment '更新者',
    modifyDate   datetime       null comment '更新时间',
    providerId   bigint         null comment '供应商id'
)
    charset = utf8mb3;

-- auto-generated definition
create table smbms_provider
(
    id           bigint auto_increment comment 'id'
        primary key,
    proCode      varchar(15) null comment '供应商编码',
    proName      varchar(15) null comment '供应商名称',
    proDesc      varchar(50) null comment '供应商描述',
    proContact   varchar(15) null comment '供应商联系人',
    proPhone     varchar(20) null comment '供应商电话',
    userAddress  varchar(30) null comment '供应商地址',
    userFax      varchar(20) null comment '供应商传真',
    createdBy    bigint      null comment '创建者',
    creationDate datetime    null comment '创建时间',
    modifyBy     bigint      null comment '更新者',
    modifyDate   datetime    null comment '更新时间'
)
    charset = utf8mb3;

-- auto-generated definition
create table smbms_role
(
    id           bigint auto_increment comment 'id'
        primary key,
    roleCode     varchar(30) null comment '角色编码',
    roleName     varchar(15) null comment '角色名称',
    createdBy    bigint      null comment '创建者',
    creationDate datetime    null comment '创建时间',
    modifyBy     bigint      null comment '更新者',
    modifyDate   datetime    null comment '更新时间'
)
    charset = utf8mb3;

-- auto-generated definition
create table smbms_user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userCode     varchar(15) null comment '用户编码',
    userName     varchar(15) null comment '用户名字',
    userPassword varchar(20) null comment '用户密码',
    gender       int         null comment '性别',
    birthday     date        null comment '出生日期',
    phone        varchar(20) null comment '电话',
    address      varchar(30) null comment '地址',
    userRole     bigint      null comment '用户角色',
    createdBy    bigint      null comment '创建者',
    creationDate datetime    null comment '创建时间',
    modifyBy     bigint      null comment '更新者',
    modifyDate   datetime    null comment '更新时间'
)
    charset = utf8mb3;

