CREATE TABLE `rdos_action_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action_type` tinyint(1) NOT NULL comment '操作类型 0启动 1停止',
  `status` tinyint(1) NOT NULL default 0 comment '操作状态 0 未开始，1操作成功 2操作失败',
  `task_id` varchar(256) NOT NULL comment '任务id',
  `is_restoration` tinyint(1) NOT NULL default 0 comment '0不恢复 1恢复',
  `gmt_create` datetime NOT NULL comment '新增时间',
  `gmt_modified` datetime NOT NULL comment '修改时间',
  `create_user_id` int(11) NOT NULL comment '发起操作的用户',
  `tenant_id` int(11) NOT NULL comment '租户id',
  `is_deleted` tinyint(1) NOT NULL default 0 comment '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  KEY `index_create_time` (`create_time`),
  KEY `index_task_id` (`task_id`,`is_deleted`),
  KEY `index_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8

CREATE TABLE `rdos_node_machine` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(64) NOT NULL comment 'master主机ip',
  `port` int(11) NOT NULL comment 'master主机端口',
  `machine_type` tinyint(1) NOT NULL default 0 comment '0 master,1 slave',
  `is_deleted` tinyint(1) NOT NULL default 0 comment '0正常 1逻辑删除',
  `gmt_create` datetime NOT NULL comment '新增时间',
  `gmt_modified` datetime NOT NULL comment '修改时间',
  PRIMARY KEY (`id`),
  KEY `index_machine_type` (`machine_type`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8


CREATE TABLE `rdos_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(256) NOT NULL comment '任务id',
  `engine_task_id` varchar(256) comment '执行引擎任务id',
  `name` varchar(256) NOT NULL comment '任务名称',
  `status` tinyint(1) NOT NULL comment '任务状态 0提交，1运行，2取消，3停止，4失败',
  `task_type` tinyint(1) NOT NULL comment '任务类型 0 sql，1 mr',
  `compute_type` tinyint(1) NOT NULL comment '计算类型 0实时，1 离线',
  `store_location` varchar(256) NOT NULL comment '存储位置',
  `sql_text` text NOT NULL comment 'sql 文本',
  `task_params` text NOT NULL comment '任务参数',
  `gmt_create` datetime NOT NULL comment '新增时间',
  `gmt_modified` datetime NOT NULL comment '修改时间',
  `modify_user_id` int(11) NOT NULL comment '最后修改task的用户',
  `create_user_id` int(11) NOT NULL comment '新建task的用户',
  `tenant_id` int(11) NOT NULL comment '租户id',
  `version` int(11) NOT NULL default 0 comment 'task版本',
  `is_deleted` tinyint(1) NOT NULL default 0 comment '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_name` (`name`),
  KEY `index_task_id` (`task_id`,`is_deleted`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8