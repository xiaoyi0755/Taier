CREATE TABLE `schedule_plugin_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `plugin_key` varchar(255) NOT NULL COMMENT '插件配置信息md5值',
  `plugin_info` text NOT NULL COMMENT '插件信息',
  `type` tinyint(2) NOT NULL COMMENT '类型 0:默认插件, 1:动态插件(暂时数据库只存动态插件)',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_plugin_id` (`plugin_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `schedule_engine_job_checkpoint` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(64) NOT NULL COMMENT '任务id',
  `task_engine_id` varchar(64) NOT NULL COMMENT '任务对于的引擎id',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `checkpoint_id` varchar(64) DEFAULT NULL COMMENT '检查点id',
  `checkpoint_trigger` timestamp NULL DEFAULT NULL COMMENT 'checkpoint触发时间',
  `checkpoint_savepath` varchar(128) DEFAULT NULL COMMENT 'checkpoint存储路径',
  `checkpoint_counts` varchar(128) DEFAULT NULL COMMENT 'checkpoint信息中的counts指标',
  PRIMARY KEY (`id`),
  UNIQUE KEY `taskid_checkpoint` (`task_id`,`checkpoint_id`) COMMENT 'taskid和checkpoint组成的唯一索引',
  KEY `idx_task_engine_id` (`task_engine_id`) COMMENT '任务的引擎id'
) ENGINE=InnoDB AUTO_INCREMENT=26474 DEFAULT CHARSET=utf8;

CREATE TABLE `schedule_engine_job_cache` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` varchar(256) NOT NULL COMMENT '任务id',
  `job_name` VARCHAR(256) DEFAULT NULL COMMENT '任务名称',
  `engine_type` varchar(256) NOT NULL COMMENT '任务的执行引擎类型',
  `compute_type` tinyint(2) NOT NULL COMMENT '计算类型stream/batch',
  `stage` tinyint(2) NOT NULL COMMENT '处于master等待队列：1 还是exe等待队列 2',
  `job_info` longtext NOT NULL COMMENT 'job信息',
  `node_address` varchar(256) DEFAULT NULL COMMENT '节点地址',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `job_priority` BIGINT(20) DEFAULT NULL COMMENT '任务优先级',
  `job_resource` VARCHAR(256) DEFAULT NULL COMMENT 'job的计算引擎资源类型',
  `is_failover` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0：不是，1：由故障恢复来的任务',
  `wait_reason` text DEFAULT NULL COMMENT '任务等待原因',
  PRIMARY KEY (`id`),
  unique KEY `index_job_id` (`job_id`(128))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

CREATE TABLE `schedule_plugin_job_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `job_id` varchar(255) NOT NULL COMMENT '任务id',
  `job_info` LONGTEXT NOT NULL COMMENT '任务信息',
  `log_info` text COMMENT '任务信息',
  `status` tinyint(2) NOT NULL COMMENT '任务状态',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_job_id` (`job_id`(128)),
  KEY idx_gmt_modified (`gmt_modified`) COMMENT '修改时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `schedule_engine_unique_sign` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `unique_sign` varchar(255) NOT NULL COMMENT '唯一标识',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_unique_sign` (`unique_sign`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 重试记录表
CREATE TABLE `schedule_engine_job_retry` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '任务状态 UNSUBMIT(0),CREATED(1),SCHEDULED(2),DEPLOYING(3),RUNNING(4),FINISHED(5),CANCELING(6),CANCELED(7),FAILED(8)',
  `job_id` varchar(256) NOT NULL COMMENT '离线任务id',
  `engine_job_id` varchar(256) DEFAULT NULL COMMENT '离线任务计算引擎id',
  `application_id` varchar(256) DEFAULT NULL COMMENT '独立运行的任务需要记录额外的id',
  `exec_start_time` datetime DEFAULT NULL COMMENT '执行开始时间',
  `exec_end_time` datetime DEFAULT NULL COMMENT '执行结束时间',
  `retry_num` int(10) NOT NULL DEFAULT '0' COMMENT '执行时，重试的次数',
  `log_info` mediumtext COMMENT '错误信息',
  `engine_log` longtext COMMENT '引擎错误信息',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `retry_task_params` text DEFAULT NULL COMMENT '重试任务参数',
  PRIMARY KEY (`id`),
  KEY `idx_job_id` (`job_id`) COMMENT '任务实例 id'
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE `schedule_engine_job_stop_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` varchar(256) NOT NULL COMMENT '任务id',
  `task_type` int(10) DEFAULT NULL COMMENT '任务类型',
  `engine_type` varchar(256) DEFAULT NULL COMMENT '任务的执行引擎类型',
  `compute_type` tinyint(2) DEFAULT NULL COMMENT '计算类型stream/batch',
  `job_resource` VARCHAR(256) DEFAULT NULL COMMENT 'job的计算引擎资源类型',
  `version` int(10) DEFAULT '0' COMMENT '版本号',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `operator_expired` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作过期时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `schedule_node_machine` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(64) NOT NULL COMMENT 'master主机ip',
  `port` int(11) NOT NULL COMMENT 'master主机端口',
  `machine_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0 master,1 slave',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `app_type` varchar(64) NOT NULL DEFAULT 'web' COMMENT 'web,engine',
  `deploy_info` varchar(256) DEFAULT NULL COMMENT 'flink,spark对应的部署模式',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_machine` (`ip`,`port`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;




CREATE TABLE `console_cluster` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_name` varchar(128) NOT NULL COMMENT '集群名称',
  `hadoop_version` varchar(24) NOT NULL COMMENT 'hadoop版本',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx` (`cluster_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `console_engine` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL COMMENT '集群id',
  `engine_name` varchar(24) NOT NULL COMMENT '引擎名称',
  `engine_type` tinyint(4) NOT NULL COMMENT '引擎类型',
  `total_node` int(11) NOT NULL COMMENT '节点数',
  `total_memory` int(11) NOT NULL COMMENT '总内存',
  `total_core` int(11) NOT NULL COMMENT '总核数',
  `sync_type` tinyint(1) NULL COMMENT '获取元数据组件类型',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `index_cluster_engineType` (`cluster_id`, `engine_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `console_component` (
 `id` int(11) NOT NULL AUTO_INCREMENT,
 `engine_id` int(11) NOT NULL COMMENT '引擎id',
 `component_name` varchar(24) NOT NULL COMMENT '组件名称',
 `component_type_code` tinyint(1) NOT NULL COMMENT '组件类型',
 `component_config` text NOT NULL COMMENT '组件配置',
 `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
 `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
 `hadoop_version` varchar(25) DEFAULT '' COMMENT '组件hadoop版本',
 `upload_file_name` varchar(50) DEFAULT '' COMMENT '上传文件zip名称',
 `component_template` text COMMENT '前端展示模版json',
 `kerberos_file_name` varchar(50) DEFAULT '' COMMENT '上传kerberos文件zip名称',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `index_component`(`engine_id`, `component_type_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

CREATE TABLE `console_dtuic_tenant` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dt_uic_tenant_id` int(11) NOT NULL COMMENT 'uic租户id',
  `tenant_name` varchar(256) NOT NULL COMMENT '用户名称',
  `tenant_desc` varchar(256) DEFAULT '' COMMENT '租户描述',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `console_engine_tenant` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` int(11) NOT NULL COMMENT '租户id',
  `engine_id` int(11) NOT NULL COMMENT '引擎id',
  `queue_id` int(11) NULL COMMENT '队列id',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `console_queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `engine_id` int(11) NOT NULL COMMENT '引擎id',
  `queue_name` varchar(128) NOT NULL COMMENT '队列名称',
  `capacity` varchar(24) NOT NULL COMMENT '最小容量',
  `max_capacity` varchar(24) NOT NULL COMMENT '最大容量',
  `queue_state` varchar(24) NOT NULL COMMENT '运行状态',
  `parent_queue_id` int(11) NOT NULL COMMENT '父队列id',
  `queue_path` varchar(256) NOT NULL COMMENT '队列路径',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `console_kerberos` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `cluster_id` int(11) NOT NULL COMMENT '集群id',
    `open_kerberos` tinyint(1) NOT NULL COMMENT '是否开启kerberos配置',
    `name` varchar(100) NOT NULL COMMENT 'kerberos文件名称',
    `remote_path` varchar(200) NOT NULL COMMENT 'sftp存储路径',
    `principal` varchar(50) NOT NULL COMMENT 'principal',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
    `krb_name` varchar(26) DEFAULT NULL COMMENT 'krb5_conf名称',
    `component_type` int(11) DEFAULT NULL COMMENT '组件类型',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;


-- 账号表
CREATE TABLE `console_user`
(
  `id`                 int(11)      NOT NULL AUTO_INCREMENT,
  `dtuic_user_id`      int(11)      NOT NULL COMMENT 'dtuic userid',
  `user_name`          varchar(256) NOT NULL COMMENT '用户名称',
  `email`              varchar(256) NOT NULL COMMENT '用户手机号',
  `status`             tinyint(1)   NOT NULL DEFAULT '0' COMMENT '用户状态0：正常，1：禁用',
  `gmt_create`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified`       datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted`         tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `default_project_id` int(11)               DEFAULT NULL COMMENT '默认项目id',
  `phone_number`       varchar(256)          DEFAULT NULL COMMENT '用户手机号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_dtuic_user_id` (`dtuic_user_id`),
  KEY `index_user_name` (`user_name`(128))
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8;

CREATE TABLE `console_account`
(
  `id`             int(11)      NOT NULL AUTO_INCREMENT,
  `name`           varchar(24)  NOT NULL COMMENT '用户名',
  `password`       varchar(256) NOT NULL COMMENT '密码',
  `type`           tinyint(11)   NOT NULL COMMENT '账号类型',
  `gmt_create`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted`     tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `create_user_id` int(11)      NOT NULL,
  `modify_user_id` int(11)               DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `console_account_tenant`
(
  `id`             int(11)    NOT NULL AUTO_INCREMENT,
  `account_id`     int(11)    NOT NULL COMMENT '数据库账号',
  `user_id`        int(11)    NOT NULL COMMENT '数栈绑定用户',
  `tenant_id`      int(11)    NOT NULL COMMENT '数栈绑定租户',
  `gmt_create`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted`     tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `create_user_id` int(11)    NOT NULL,
  `modify_user_id` int(11)             DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8;






CREATE TABLE `schedule_task_shade`
(
  `id`                      int(11)      NOT NULL AUTO_INCREMENT,
  `tenant_id`               int(11)      NOT NULL DEFAULT '-1' COMMENT '租户id',
  `project_id`              int(11)      NOT NULL DEFAULT '-1' COMMENT '项目id',
  `dtuic_tenant_id`         int(11)      NOT NULL DEFAULT '-1' COMMENT 'uic租户id',
  `app_type`                int(11)      NOT NULL DEFAULT '0' COMMENT 'RDOS(1), DQ(2), API(3), TAG(4), MAP(5), CONSOLE(6), STREAM(7), DATASCIENCE(8)',
  `node_pid`                int(11)      NOT NULL DEFAULT '0' COMMENT '父文件夹id',
  `name`                    varchar(256) NOT NULL COMMENT '任务名称',
  `task_type`               tinyint(1)   NOT NULL COMMENT '任务类型 -1:虚节点, 0:sparksql, 1:spark, 2:数据同步, 3:pyspark, 4:R, 5:深度学习, 6:python, 7:shell, 8:机器学习, 9:hadoopMR, 10:工作流, 12:carbonSQL, 13:notebook, 14:算法实验, 15:libra sql, 16:kylin, 17:hiveSQL',
  `engine_type`             tinyint(1)   NOT NULL COMMENT '执行引擎类型 0:flink, 1:spark, 2:datax, 3:learning, 4:shell, 5:python2, 6:dtyarnshell, 7:python3, 8:hadoop, 9:carbon, 10:postgresql, 11:kylin, 12:hive',
  `compute_type`            tinyint(1)   NOT NULL COMMENT '计算类型 0实时，1 离线',
  `sql_text`                LONGTEXT     NOT NULL COMMENT 'sql 文本',
  `task_params`             text         NOT NULL COMMENT '任务参数',
  `task_id`                 int(11)      NOT NULL COMMENT '任务id',
  `schedule_conf`           varchar(512) NOT NULL COMMENT '调度配置 json格式',
  `period_type`             tinyint(2) COMMENT '周期类型',
  `schedule_status`         tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0未开始,1正常调度,2暂停',
  `project_schedule_status` tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0正常, 1:停止',
  `submit_status`           tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0未提交,1已提交',
  `gmt_create`              datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified`            datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `modify_user_id`          int(11)      NOT NULL COMMENT '最后修改task的用户',
  `create_user_id`          int(11)      NOT NULL COMMENT '新建task的用户',
  `owner_user_id`           int(11)      NOT NULL COMMENT '负责人id',
  `version_id`              int(11)      NOT NULL DEFAULT '0' COMMENT 'task版本',
  `is_deleted`              tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `task_desc`               varchar(256) NOT NULL             COMMENT '任务描述',
  `main_class`              varchar(256) NOT NULL             COMMENT 'Jar包的入口函数',
  `exe_args`                text                  DEFAULT NULL COMMENT '额外参数',
  `flow_id`                 INT(11)      NOT NULL DEFAULT '0' COMMENT '工作流id',
  `is_publish_to_produce`   tinyint(1)   NOT NULL DEFAULT '0' COMMENT '是否发布到生产环境：0-否，1-是',
  `extra_info`              mediumtext                  DEFAULT NULL COMMENT '存储task运行时所需的额外信息',
  PRIMARY KEY (`id`),
  KEY `index_name` (`project_id`, `name`(128)),
  UNIQUE KEY `index_task_id` (`task_id`,`app_type`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8;

CREATE TABLE `schedule_task_task_shade`
(
  `id`              int(11)    NOT NULL AUTO_INCREMENT,
  `tenant_id`       int(11)    NOT NULL COMMENT '租户id',
  `project_id`      int(11)    NOT NULL COMMENT '项目id',
  `dtuic_tenant_id` int(11)    NOT NULL DEFAULT '-1' COMMENT 'uic租户id',
  `app_type`        int(11)    NOT NULL DEFAULT '0' COMMENT 'RDOS(1), DQ(2), API(3), TAG(4), MAP(5), CONSOLE(6), STREAM(7), DATASCIENCE(8)',
  `task_id`         int(11)    NOT NULL COMMENT 'batch 任务id',
  `parent_task_id`  int(11)             DEFAULT NULL COMMENT '对应batch任务父节点的id',
  `gmt_create`      datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified`    datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_batch_task_task` (`task_id`, `parent_task_id`, `project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `schedule_job`
(
  `id`              int(11)      NOT NULL AUTO_INCREMENT,
  `tenant_id`       int(11)      NOT NULL COMMENT '租户id',
  `project_id`      int(11)      NOT NULL COMMENT '项目id',
  `dtuic_tenant_id` int(11)      NOT NULL DEFAULT '-1' COMMENT 'uic租户id',
  `app_type`        int(11)      NOT NULL DEFAULT '0' COMMENT 'RDOS(1), DQ(2), API(3), TAG(4), MAP(5), CONSOLE(6), STREAM(7), DATASCIENCE(8)',
  `job_id`          varchar(256) NOT NULL COMMENT '工作任务id',
  `job_key`         varchar(256) NOT NULL DEFAULT '' COMMENT '工作任务key',
  `job_name`        VARCHAR(256) NOT NULL DEFAULT '' COMMENT '工作任务名称',
  `task_id`         int(11)      NOT NULL COMMENT '任务id',
  `gmt_create`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user_id`  int(11)      NOT NULL COMMENT '发起操作的用户',
  `is_deleted`      tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `type`            tinyint(1)   NOT NULL DEFAULT '2' COMMENT '0正常调度 1补数据 2临时运行',
  `is_restart`      tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0：非重启任务, 1：重启任务',
  `business_date`   varchar(64) NOT NULL COMMENT '业务日期 yyyyMMddHHmmss',
  `cyc_time`        varchar(64) NOT NULL COMMENT '调度时间 yyyyMMddHHmmss',
  `dependency_type` tinyint(2)   NOT NULL DEFAULT 0 COMMENT '依赖类型',
  `flow_job_id`     VARCHAR(256)          DEFAULT '0' NOT NULL COMMENT '工作流实例id',
  `period_type`     tinyint(2)            DEFAULT NULL COMMENT '周期类型',
  `status`          tinyint(1)   NOT NULL DEFAULT '0' COMMENT '任务状态 UNSUBMIT(0),CREATED(1),SCHEDULED(2),DEPLOYING(3),RUNNING(4),FINISHED(5),CANCELING(6),CANCELED(7),FAILED(8)',
  `task_type`       tinyint(1)   NOT NULL COMMENT '任务类型 -1:虚节点, 0:sparksql, 1:spark, 2:数据同步, 3:pyspark, 4:R, 5:深度学习, 6:python, 7:shell, 8:机器学习, 9:hadoopMR, 10:工作流, 12:carbonSQL, 13:notebook, 14:算法实验, 15:libra sql, 16:kylin, 17:hiveSQL',
  `fill_id`         int(11)               DEFAULT 0 COMMENT '补数据id，默认为0',
  `exec_start_time` datetime              DEFAULT NULL COMMENT '执行开始时间',
  `exec_end_time`   datetime              DEFAULT NULL COMMENT '执行结束时间',
  `exec_time`       int(11)               DEFAULT '0' COMMENT '执行时间',
  `submit_time`     datetime              DEFAULT NULL COMMENT '提交时间',
  `max_retry_num`   int(10)      NOT NULL DEFAULT '0' COMMENT '最大重试次数',
  `retry_num`       int(10)      NOT NULL DEFAULT '0' COMMENT '执行时，重试的次数',
  `log_info`        mediumtext                        COMMENT '错误信息',
  `node_address`    varchar(64)          DEFAULT NULL COMMENT '节点地址',
  `version_id`      int(10)               DEFAULT '0' COMMENT '任务运行时候版本号',
  `next_cyc_time`   varchar(64)          DEFAULT NULL COMMENT '下一次调度时间 yyyyMMddHHmmss',
  `engine_job_id`   varchar(256) DEFAULT NULL COMMENT '离线任务计算引擎id',
  `application_id`  varchar(256) DEFAULT NULL COMMENT '独立运行的任务需要记录额外的id',
  `engine_log`      longtext                  COMMENT '引擎错误信息',
  `plugin_info_id`  int(11)      DEFAULT NULL COMMENT '插件信息',
  `retry_task_params` text       DEFAULT NULL COMMENT '重试任务参数',
  `compute_type`    tinyint(1)   NOT NULL DEFAULT '1' COMMENT '计算类型STREAM(0), BATCH(1)',
  `phase_status`    tinyint(1) NOT NULL DEFAULT '0' COMMENT '运行状态: CREATE(0):创建,JOIN_THE_TEAM(1):入队,LEAVE_THE_TEAM(2):出队',
  `job_graph`       TEXT DEFAULT NULL COMMENT 'jobGraph构建json',
  `submit_user_name` VARCHAR(20) DEFAULT NULL COMMENT '任务提交用户名',
  PRIMARY KEY (`id`),
  KEY `index_task_id` (`task_id`),
  UNIQUE KEY `index_job_id` (`job_id`(128),`is_deleted`),
  KEY `index_fill_id` (`fill_id`),
  KEY `index_project_id` (`project_id`),
  UNIQUE KEY `idx_jobKey` (`job_key`(128)),
  KEY `idx_name_type` (`job_name`(128), `type`),
  KEY `index_engine_job_id` (`engine_job_id`(128)),
  KEY `index_status` (`status`),
  KEY `index_gmt_modified` (`gmt_modified`),
  KEY `idx_cyctime` (`cyc_time`),
  KEY `idx_exec_start_time` (`exec_start_time`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8;

CREATE TABLE `schedule_job_job`
(
  `id`              int(11)      NOT NULL AUTO_INCREMENT,
  `tenant_id`       int(11)      NOT NULL COMMENT '租户id',
  `project_id`      int(11)      NOT NULL COMMENT '项目id',
  `dtuic_tenant_id` int(11)      NOT NULL DEFAULT '-1' COMMENT 'uic租户id',
  `app_type`        int(11)      NOT NULL DEFAULT '0' COMMENT 'RDOS(1), DQ(2), API(3), TAG(4), MAP(5), CONSOLE(6), STREAM(7), DATASCIENCE(8)',
  `job_key`         VARCHAR(256) NOT NULL COMMENT 'batch 任务key',
  `parent_job_key`  VARCHAR(256)          DEFAULT NULL COMMENT '对应batch任务父节点的key',
  `gmt_create`      datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted`      tinyint(1)   NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_job_parentJobKey` (`job_key`(255), `parent_job_key`(255)),
  KEY `idx_job_jobKey`(`parent_job_key`(128)) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE `schedule_fill_data_job`
(
  `id`              int(11)     NOT NULL AUTO_INCREMENT,
  `tenant_id`       int(11)     NOT NULL COMMENT '租户id',
  `project_id`      int(11)     NOT NULL COMMENT '项目id',
  `dtuic_tenant_id` int(11)     NOT NULL DEFAULT '-1' COMMENT 'uic租户id',
  `app_type`        int(11)     NOT NULL DEFAULT '0' COMMENT 'RDOS(1), DQ(2), API(3), TAG(4), MAP(5), CONSOLE(6), STREAM(7), DATASCIENCE(8)',
  `job_name`        VARCHAR(64) NOT NULL DEFAULT '' COMMENT '补数据任务名称',
  `run_day`         VARCHAR(64) NOT NULL COMMENT '补数据运行日期yyyy-MM-dd',
  `from_day`        VARCHAR(64) COMMENT '补数据开始业务日期yyyy-MM-dd',
  `to_day`          VARCHAR(64) COMMENT '补数据结束业务日期yyyy-MM-dd',
  `gmt_create`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user_id`  int(11)     NOT NULL COMMENT '发起操作的用户',
  `is_deleted`      tinyint(1)  NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_task_id` (`tenant_id`, `project_id`, `job_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 0
  DEFAULT CHARSET = utf8;


CREATE TABLE `schedule_job_graph_trigger`
(
  `id`           int(11)    NOT NULL AUTO_INCREMENT,
  `trigger_type` tinyint(3) NOT NULL COMMENT '0:正常调度 1补数据',
  `trigger_time` datetime   NOT NULL COMMENT '调度时间',
  `gmt_create`   datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted`   int(10)    NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_trigger_time` (`trigger_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `console_tenant_resource` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` int(11) NOT NULL COMMENT '租户id',
  `dt_uic_tenant_id` int(11) NOT NULL COMMENT 'uic租户id',
  `task_type` tinyint(2) NOT NULL COMMENT '任务类型',
  `engine_type` varchar(256) NOT NULL COMMENT '任务类型名称',
  `resource_limit` text NOT NULL COMMENT '资源限制',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int(10) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_uic_tenantid_tasktype` (`dt_uic_tenant_id`,`task_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8 COMMENT='租户资源限制表';

CREATE TABLE `schedule_task_commit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` int(11) NOT NULL COMMENT '任务id',
  `app_type` int(11) NOT NULL DEFAULT '0' COMMENT 'RDOS(1), DQ(2), API(3), TAG(4), MAP(5), CONSOLE(6), STREAM(7), DATASCIENCE(8)',
  `commit_id` varchar(128) NOT NULL COMMENT '提交id',
  `task_json` text COMMENT '额外参数',
  `extra_info` mediumtext COMMENT '存储task运行时所需的额外信息',
  `is_commit` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否提交：0未提交 1已提交',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '过期策略：0永不过期 1过期取消',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_job_id` (`commit_id`(128),`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dt_alert_gate` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `alert_gate_name` varchar(32) DEFAULT NULL,
  `alert_gate_type` smallint(2) DEFAULT NULL,
  `alert_gate_code` varchar(16) DEFAULT NULL,
  `alert_gate_status` smallint(2) DEFAULT NULL,
  `alert_gate_json` varchar(1024) DEFAULT NULL,
  `is_deleted` smallint(2) DEFAULT NULL,
  `gmt_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `alert_gate_source` varchar(32) DEFAULT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `dt_alert_template` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `alert_template_name` varchar(32) DEFAULT NULL,
  `alert_template_type` smallint(2) DEFAULT NULL,
  `alert_template_status` smallint(2) DEFAULT NULL,
  `alert_template` text,
  `is_deleted` smallint(2) DEFAULT NULL,
  `gmt_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `alert_gate_source` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `dt_cluster_alert` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) unsigned NOT NULL,
  `alert_id` int(11) unsigned NOT NULL,
  `is_default` tinyint(3) NOT NULL,
  `gmt_created` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `dt_notify_record_content` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` int(11) NOT NULL DEFAULT '0' COMMENT '租户id',
  `project_id` int(11) NOT NULL DEFAULT '0' COMMENT '项目id',
  `app_type` int(11) NOT NULL COMMENT '应用类型，1：RDOS, 2:数据质量, 3:数据API ,4: 标签工程 ,5:数据地图',
  `content` text NOT NULL COMMENT '内容文本',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '触发类型',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='消息记录内容';

CREATE TABLE `dt_notify_record_read` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` int(11) NOT NULL DEFAULT '0' COMMENT '租户id',
  `project_id` int(11) DEFAULT '0' COMMENT '项目id',
  `app_type` int(11) NOT NULL COMMENT '应用类型，1：RDOS, 2:数据质量, 3:数据API ,4: 标签工程 ,5:数据地图',
  `notify_record_id` int(11) NOT NULL DEFAULT '0' COMMENT '通知记录id',
  `content_id` int(11) NOT NULL DEFAULT '0' COMMENT '内容文本id',
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT '接收人id',
  `read_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0:未读 1:已读',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_app_type_notify_record_id` (`app_type`,`notify_record_id`),
  KEY `idx_app_type_user_id` (`app_type`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='消息记录读状态';

CREATE TABLE `dt_notify_send_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tenant_id` int(11) NOT NULL DEFAULT '0' COMMENT '租户id',
  `project_id` int(11) DEFAULT '0' COMMENT '项目id',
  `app_type` int(11) NOT NULL COMMENT '应用类型，1：RDOS, 2:数据质量, 3:数据API ,4: 标签工程 ,5:数据地图',
  `notify_record_id` int(11) NOT NULL COMMENT '通知记录id',
  `content_id` int(11) NOT NULL DEFAULT '0' COMMENT '内容文本id',
  `user_id` int(11) NOT NULL COMMENT '发送的用户id',
  `send_type` tinyint(1) NOT NULL COMMENT '1：邮件，2: 短信，3: 微信，4: 钉钉',
  `send_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0:准备发送 1:发送成功 2:发送失败',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='通知记录表';

CREATE TABLE `console_security_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL COMMENT '当前app下的用户租户id',
  `operator` varchar(100) NOT NULL COMMENT '操作人',
  `operator_id` bigint(20) NOT NULL COMMENT '操作人在对应app下的用户id',
  `app_tag` varchar(45) NOT NULL COMMENT 'App类型标示',
  `action` varchar(200) NOT NULL COMMENT '执行的动作',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `operation` varchar(10) NOT NULL COMMENT '当前操作',
  `operation_object` varchar(200) NOT NULL COMMENT '操作对象',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE `task_param_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '0正常 1逻辑删除',
  `compute_type` int(11) DEFAULT NULL,
  `engine_type` int(11) DEFAULT NULL,
  `task_type` int(11) DEFAULT '0' COMMENT '默认0-任务类型',
  `params` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_compute_engine_task` (`compute_type`,`engine_type`,`task_type`,`is_deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;




