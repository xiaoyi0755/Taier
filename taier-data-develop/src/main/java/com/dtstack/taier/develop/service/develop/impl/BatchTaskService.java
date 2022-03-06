/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.taier.develop.service.develop.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dtstack.dtcenter.loader.client.ClientCache;
import com.dtstack.dtcenter.loader.client.IClient;
import com.dtstack.dtcenter.loader.client.IKerberos;
import com.dtstack.dtcenter.loader.dto.ColumnMetaDTO;
import com.dtstack.dtcenter.loader.dto.SqlQueryDTO;
import com.dtstack.dtcenter.loader.dto.source.ISourceDTO;
import com.dtstack.dtcenter.loader.source.DataSourceType;
import com.dtstack.taier.common.constant.PatternConstant;
import com.dtstack.taier.common.enums.CatalogueType;
import com.dtstack.taier.common.enums.Deleted;
import com.dtstack.taier.common.enums.DependencyType;
import com.dtstack.taier.common.enums.DictType;
import com.dtstack.taier.common.enums.EComponentType;
import com.dtstack.taier.common.enums.EDeployType;
import com.dtstack.taier.common.enums.EParamType;
import com.dtstack.taier.common.enums.EScheduleJobType;
import com.dtstack.taier.common.enums.EScheduleStatus;
import com.dtstack.taier.common.enums.ESubmitStatus;
import com.dtstack.taier.common.enums.FuncType;
import com.dtstack.taier.common.enums.MultiEngineType;
import com.dtstack.taier.common.enums.PublishTaskStatusEnum;
import com.dtstack.taier.common.enums.ReadWriteLockType;
import com.dtstack.taier.common.enums.ResourceRefType;
import com.dtstack.taier.common.enums.TaskLockStatus;
import com.dtstack.taier.common.env.EnvironmentContext;
import com.dtstack.taier.common.exception.DtCenterDefException;
import com.dtstack.taier.common.exception.ErrorCode;
import com.dtstack.taier.common.exception.RdosDefineException;
import com.dtstack.taier.common.kerberos.KerberosConfigVerify;
import com.dtstack.taier.common.util.Base64Util;
import com.dtstack.taier.common.util.DataFilter;
import com.dtstack.taier.common.util.JsonUtils;
import com.dtstack.taier.common.util.PublicUtil;
import com.dtstack.taier.common.util.Strings;
import com.dtstack.taier.dao.domain.BatchCatalogue;
import com.dtstack.taier.dao.domain.BatchDataSource;
import com.dtstack.taier.dao.domain.BatchReadWriteLock;
import com.dtstack.taier.dao.domain.BatchResource;
import com.dtstack.taier.dao.domain.BatchSysParameter;
import com.dtstack.taier.dao.domain.Task;
import com.dtstack.taier.dao.domain.BatchTaskParam;
import com.dtstack.taier.dao.domain.BatchTaskResource;
import com.dtstack.taier.dao.domain.BatchTaskTask;
import com.dtstack.taier.dao.domain.BatchTaskVersion;
import com.dtstack.taier.dao.domain.Component;
import com.dtstack.taier.dao.domain.Dict;
import com.dtstack.taier.dao.domain.ScheduleTaskShade;
import com.dtstack.taier.dao.domain.Task;
import com.dtstack.taier.dao.domain.TaskParamTemplate;
import com.dtstack.taier.dao.domain.TaskVersion;
import com.dtstack.taier.dao.domain.Tenant;
import com.dtstack.taier.dao.domain.User;
import com.dtstack.taier.dao.dto.BatchTaskDTO;
import com.dtstack.taier.dao.dto.BatchTaskVersionDetailDTO;
import com.dtstack.taier.dao.dto.UserDTO;
import com.dtstack.taier.dao.mapper.DevelopReadWriteLockDao;
import com.dtstack.taier.dao.mapper.DevelopTaskDao;
import com.dtstack.taier.dao.pager.PageQuery;
import com.dtstack.taier.dao.pager.Sort;
import com.dtstack.taier.develop.common.template.Reader;
import com.dtstack.taier.develop.common.template.Setting;
import com.dtstack.taier.develop.common.template.Writer;
import com.dtstack.taier.develop.dto.devlop.BatchTaskBatchVO;
import com.dtstack.taier.develop.dto.devlop.CheckSyntaxResult;
import com.dtstack.taier.develop.dto.devlop.ReadWriteLockVO;
import com.dtstack.taier.develop.dto.devlop.TaskCatalogueVO;
import com.dtstack.taier.develop.dto.devlop.TaskCheckResultVO;
import com.dtstack.taier.develop.dto.devlop.TaskGetNotDeleteVO;
import com.dtstack.taier.develop.dto.devlop.TaskResourceParam;
import com.dtstack.taier.develop.dto.devlop.TaskVO;
import com.dtstack.taier.develop.enums.develop.EDataSyncJobType;
import com.dtstack.taier.develop.enums.develop.FlinkVersion;
import com.dtstack.taier.develop.enums.develop.SourceDTOType;
import com.dtstack.taier.develop.enums.develop.SyncContentEnum;
import com.dtstack.taier.develop.enums.develop.SyncModel;
import com.dtstack.taier.develop.enums.develop.TaskCreateModelType;
import com.dtstack.taier.develop.enums.develop.TaskOperateType;
import com.dtstack.taier.develop.enums.develop.TaskSubmitStatusEnum;
import com.dtstack.taier.develop.mapstruct.vo.TaskMapstructTransfer;
import com.dtstack.taier.develop.parser.ESchedulePeriodType;
import com.dtstack.taier.develop.parser.ScheduleCron;
import com.dtstack.taier.develop.parser.ScheduleFactory;
import com.dtstack.taier.develop.service.console.TenantService;
import com.dtstack.taier.develop.service.datasource.impl.DatasourceService;
import com.dtstack.taier.develop.service.schedule.TaskService;
import com.dtstack.taier.develop.service.task.TaskParamTemplateService;
import com.dtstack.taier.develop.service.template.DaJobCheck;
import com.dtstack.taier.develop.service.template.DefaultSetting;
import com.dtstack.taier.develop.service.template.FlinkxJobTemplate;
import com.dtstack.taier.develop.service.template.Restoration;
import com.dtstack.taier.develop.service.template.bulider.nameMapping.NameMappingBuilder;
import com.dtstack.taier.develop.service.template.bulider.nameMapping.NameMappingBuilderFactory;
import com.dtstack.taier.develop.service.template.bulider.reader.DaReaderBuilder;
import com.dtstack.taier.develop.service.template.bulider.reader.DaReaderBuilderFactory;
import com.dtstack.taier.develop.service.template.bulider.writer.DaWriterBuilder;
import com.dtstack.taier.develop.service.template.bulider.writer.DaWriterBuilderFactory;
import com.dtstack.taier.develop.service.user.UserService;
import com.dtstack.taier.develop.utils.TaskStatusCheckUtil;
import com.dtstack.taier.develop.utils.TaskUtils;
import com.dtstack.taier.develop.utils.TimeUtil;
import com.dtstack.taier.develop.utils.develop.common.enums.Constant;
import com.dtstack.taier.develop.utils.develop.sync.job.PluginName;
import com.dtstack.taier.develop.utils.develop.sync.job.SyncJobCheck;
import com.dtstack.taier.develop.vo.develop.query.AllProductGlobalSearchVO;
import com.dtstack.taier.develop.vo.develop.result.BatchAllProductGlobalReturnVO;
import com.dtstack.taier.develop.vo.develop.result.BatchTaskGetComponentVersionResultVO;
import com.dtstack.taier.pluginapi.util.MathUtil;
import com.dtstack.taier.scheduler.dto.schedule.SavaTaskDTO;
import com.dtstack.taier.scheduler.dto.schedule.ScheduleTaskShadeDTO;
import com.dtstack.taier.scheduler.impl.pojo.ParamTaskAction;
import com.dtstack.taier.scheduler.service.ClusterService;
import com.dtstack.taier.scheduler.service.ComponentService;
import com.dtstack.taier.scheduler.service.ScheduleDictService;
import com.dtstack.taier.scheduler.vo.ScheduleTaskVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dtstack.taier.develop.utils.develop.common.enums.Constant.CREATE_MODEL_GUIDE;
import static com.dtstack.taier.develop.utils.develop.common.enums.Constant.CREATE_MODEL_TEMPLATE;


/**
 * company: www.dtstack.com
 * author: toutian
 * create: 2017/5/4
 */
@Service
public class BatchTaskService {

    public static Logger LOGGER = LoggerFactory.getLogger(BatchTaskService.class);
    private static final ObjectMapper objMapper = new ObjectMapper();

    @Resource(name = "batchJobParamReplace")
    private JobParamReplace jobParamReplace;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private BatchTaskParamShadeService batchTaskParamShadeService;

    @Autowired
    private DevelopTaskDao developTaskDao;

    @Autowired
    private TaskParamTemplateService taskParamTemplateService;

    @Autowired
    private BatchTaskResourceService batchTaskResourceService;

    @Autowired
    private BatchTaskParamService batchTaskParamService;

    @Autowired
    private BatchTaskTaskService batchTaskTaskService;

    @Autowired
    private DatasourceService dataSourceService;

    @Autowired
    private BatchCatalogueService batchCatalogueService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskVersionService taskVersionService;

    @Autowired
    private BatchSysParamService batchSysParamService;

    @Autowired
    private BatchResourceService batchResourceService;

    @Autowired
    private BatchFunctionService batchFunctionService;

    @Autowired
    private ScheduleDictService dictService;

    @Autowired
    private BatchSqlExeService batchSqlExeService;

    @Autowired
    private BatchTaskResourceShadeService batchTaskResourceShadeService;

    @Autowired
    private BatchJobService batchJobService;

    @Autowired
    private HadoopJobExeService hadoopJobExeService;

    @Autowired
    private DaReaderBuilderFactory daReaderBuilderFactory;
    @Autowired
    private NameMappingBuilderFactory nameMappingBuilderFactory;

    @Autowired
    private DaWriterBuilderFactory daWriterBuilderFactory;

    @Autowired
    private EnvironmentContext environmentContext;

    @Autowired
    private ComponentService componentService;

    private static final String KEY = "key";

    private static final String TYPE = "type";

    private static final String COLUMN = "column";

    private static final String KERBEROS_CONFIG = "kerberosConfig";
    private static final String DEFAULT_BATCH_SCHEDULE_CONF = "{\"selfReliance\":false, \"min\":0,\"hour\":0,\"periodType\":\"2\",\"beginDate\":\"%s\",\"endDate\":\"%s\",\"isFailRetry\":true,\"maxRetryNum\":\"3\"}";
    private static final String DEFAULT_STREAM_SCHEDULE_CONF = "{\"isFailRetry\":false,\"beginDate\":\"%s\",\"endDate\":\"%s\",\"periodType\":\"5\",\"maxRetryNum\":\"3\",\"submitExpiredUnit\":\"1\",\"submitExpired\":\"3\",\"retryInterval\":\"3\",\"retryIntervalUnit\":\"1\"}";

    @Autowired
    private ClusterService clusterService;

    /**
     * kerberos认证文件在 ftp上的相对路径
     */
    private static final String KERBEROS_DIR = "kerberosDir";

    /**
     * Kerberos 文件上传的时间戳
     */
    private static final String KERBEROS_FILE_TIMESTAMP = "kerberosFileTimestamp";

    private static Map<Integer, List<Pair<String, String>>> jobSupportTypeMap = Maps.newHashMap();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_SCHEDULE_CONF = "{\"selfReliance\":0, \"min\":0,\"hour\":0,\"periodType\":\"2\",\"beginDate\":\"2001-01-01\",\"endDate\":\"2121-01-01\",\"isFailRetry\":true,\"maxRetryNum\":\"3\"}";

    private static final Integer DEFAULT_SCHEDULE_PERIOD = ESchedulePeriodType.DAY.getVal();

    private static final String CMD_OPTS = "--cmd-opts";

    private static final String OPERATE_MODEL = "operateModel";

    private static final Integer IS_FILE = 1;


    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final Integer INIT_LOCK_VERSION = 0;


    public static final String HADOOP_CONFIG = "hadoopConfig";

    @Autowired
    private ReadWriteLockService readWriteLockService;

    private static final String TASK_PATTERN = "[\\u4e00-\\u9fa5_a-z0-9A-Z-]+";

    @PostConstruct
    public void init() {
        //初始化可以支持的任务类型
        final List<Dict> yarn = dictService.listByDictType(DictType.DATA_DEVELOP_SUPPORT_TASK_TYPE);
        final List<Pair<String, String>> yarnSupportType = yarn.stream().map(dict -> Pair.of(dict.getDictValue(), dict.getDictDesc())).collect(Collectors.toList());
        jobSupportTypeMap.put(EDeployType.YARN.getType(), yarnSupportType);
    }

    /**
     * 按id查询任务详情
     *
     * @param taskVO
     * @return
     */
    public TaskVO getTaskById(TaskVO taskVO) {
        Task task = getOne(taskVO.getId());
        TaskMapstructTransfer.INSTANCE.taskToTaskVO(task, taskVO);

        // sqlText 解密处理
        String sqlText = Base64Util.baseDecode(taskVO.getSqlText());
        //如果sqlText不为空进行解析
        if (StringUtils.isNotBlank(sqlText)) {
            JSONObject jsonObject = JSONObject.parseObject(sqlText);
            taskVO.setSqlText(formatSqlText(taskVO.getCreateModel(), jsonObject));
        }
      /*  Set<Long> userIds = new HashSet<>();
        userIds.add(task.getCreateUserId());
        userIds.add(task.getModifyUserId());
        userIds.add(task.getOwnerUserId());
        Map<Long, User> userMap = userService.getUserMap(userIds);
        User createUser = userMap.get(task.getCreateUserId());
        if (createUser != null) {
            taskVO.setCreateUser(createUser);
            taskVO.setCreateUserName(createUser.getUserName());
        }
        User modifyUser = userMap.get(task.getModifyUserId());
        if (modifyUser != null) {
            taskVO.setModifyUserName(modifyUser.getUserName());
        }

        User ownerUser = userMap.get(task.getOwnerUserId());
        if (ownerUser != null) {
            taskVO.setOwnerUser(ownerUser);
        }
        PageQuery query = new PageQuery(1, 5, "rb.gmt_create", Sort.DESC.name());
        List<DataSyncTaskVersionVO> dataSyncTaskVersions = dataSyncTaskVersionService.listByTaskId(taskVO.getId(), query);
        Set<Long> createUserIds = dataSyncTaskVersions.stream().map(DataSyncTaskVersionVO::getCreateUserId).collect(Collectors.toSet());
        Map<Long, User> createUserMap = userService.getUserMap(createUserIds);
        dataSyncTaskVersions.stream()
                .map(ver -> {
                    if (StringUtils.isNotBlank(ver.getOriginSql())) {
                        ver.setSqlText(ver.getOriginSql());
                    }
                    ver.setUserName(createUserMap.get(ver.getCreateUserId()).getUserName());
                    return ver;
                }).collect(Collectors.toList());
        streamTaskVO.setTaskVersions(dataSyncTaskVersions);
        if (StringUtils.isNotEmpty(streamTaskVO.getSqlText())) {
            streamTaskVO.setSqlText(JsonUtils.formatJSON(DataFilter.passwordFilter(streamTaskVO.getSqlText())));
        }
        if (!Objects.equals(EDataSyncJobType.BATCH_SYNC.getVal(), taskVO.getTaskType())) {
            for (DataSyncTaskVersionVO taskVersion : taskVO.getTaskVersions()) {
                if (taskVersion.getSqlText() != null) {
                    taskVersion.setSqlText(JsonUtils.formatJSON(DataFilter.passwordFilter(Base64Util.baseDecode(taskVersion.getSqlText()))));
                }
            }
        }*/
        taskVO.setSourceMap(JSON.parseObject(taskVO.getSourceStr(), Map.class));
        taskVO.setTargetMap(JSON.parseObject(taskVO.getTargetStr(), Map.class));
        taskVO.setSettingMap(JSON.parseObject(taskVO.getSettingStr(), Map.class));
        setTaskVariables(taskVO, taskVO.getId());
//        DataSyncTaskDirtyDataManage oneByTaskId = taskDirtyDataManageService.getOneByTaskId(task.getId(), task.getProjectId());
//        streamTaskVO.setDataSyncTaskDirtyDataManageVO(DataSyncTaskDirtyDataManageTransfer.INSTANCE.taskDirtyDataManageToStreamTaskDirtyDataManageVO(oneByTaskId));
//        streamTaskVO.setIsDirtyDataManage(streamTaskVO.getDataSyncTaskDirtyDataManageVO() == null ? false : true);
        return taskVO;
    }


    /**
     * 数据开发-根据任务id，查询详情
     *
     * @return
     * @author toutian
     */
//    public BatchTaskBatchVO getTaskById(final ScheduleTaskVO scheduleTaskVO) {
//        final Task task = this.developTaskDao.getOne(scheduleTaskVO.getId());
//        if (task == null) {
//            throw new RdosDefineException(ErrorCode.CAN_NOT_FIND_TASK);
//        }
//
//        final List<BatchResource> resources = this.batchTaskResourceService.getResources(scheduleTaskVO.getId(), ResourceRefType.MAIN_RES.getType());
//        final List<BatchResource> refResourceIdList = this.batchTaskResourceService.getResources(scheduleTaskVO.getId(), ResourceRefType.DEPENDENCY_RES.getType());
//
//        final BatchTaskBatchVO taskVO = new BatchTaskBatchVO(this.batchTaskTaskService.getForefathers(task));
//        taskVO.setVersion(task.getVersion());
//        if (task.getTaskType().intValue() == EScheduleJobType.SYNC.getVal().intValue()) {  //同步任务类型
//            final String taskJson = Base64Util.baseDecode(task.getSqlText());
//            if (StringUtils.isBlank(taskJson)) {
//                taskVO.setCreateModel(CREATE_MODEL_GUIDE);  //向导模式存在为空的情况
//                taskVO.setSqlText("");
//            } else {
//                final JSONObject obj = JSON.parseObject(taskJson);
//                taskVO.setCreateModel(obj.get("createModel") == null ? CREATE_MODEL_GUIDE : Integer.parseInt(String.valueOf(obj.get("createModel"))));
//                formatSqlText(taskVO, obj);
//            }
//        }
//
//        this.setTaskOperatorModelAndOptions(taskVO, task);
//        if (task.getFlowId() != null && task.getFlowId() > 0) {
//            taskVO.setFlowId(task.getFlowId());
//            final Task flow = this.developTaskDao.getOne(task.getFlowId());
//            if (flow != null) {
//                taskVO.setFlowName(flow.getName());
//            }
//        }
//
//        final BatchCatalogue catalogue = batchCatalogueService.getOne(task.getNodePid());
//        if (catalogue != null) {
//            taskVO.setNodePName(catalogue.getNodeName());
//        }
//
//        taskVO.setResourceList(resources);
//        taskVO.setRefResourceList(refResourceIdList);
//
//        PageQuery pageQuery = new PageQuery(1, 5, "gmt_create", Sort.DESC.name());
//        List<BatchTaskVersionDetailDTO> taskVersions = batchTaskVersionService.listByTaskId(scheduleTaskVO.getId(), pageQuery).stream()
//                .map(ver -> {
//                    if (StringUtils.isNotBlank(ver.getOriginSql())) {
//                        if (task.getTaskType().intValue() == EScheduleJobType.SYNC.getVal().intValue()) {
//                            ver.setSqlText(ver.getSqlText());
//                        } else {
//                            ver.setSqlText(ver.getOriginSql());
//                        }
//
//                    }
//                    // 填充用户名称
//                    ver.setUserName(userService.getUserName(ver.getCreateUserId()));
//                    return ver;
//                }).collect(Collectors.toList());
//        taskVO.setTaskVersions(taskVersions);
//
//        // 密码脱敏 --2019/10/25 茂茂-- 同步任务 密码脱敏 仅 向导模式 修改成 全部模式
//        if (task.getTaskType().intValue() == EScheduleJobType.SYNC.getVal().intValue()) {
//            try {
//                taskVO.setSqlText(JsonUtils.formatJSON(DataFilter.passwordFilter(taskVO.getSqlText())));
//            }catch (final Exception e){
//                LOGGER.error("同步任务json解析失败 taskId = {}", task.getId(), e);
//                taskVO.setSqlText(DataFilter.passwordFilter(taskVO.getSqlText()));
//            }
//
//            for (BatchTaskVersionDetailDTO taskVersion : taskVO.getTaskVersions()) {
//                try {
//                    taskVersion.setSqlText(JsonUtils.formatJSON(DataFilter.passwordFilter(taskVersion.getSqlText())));
//                }catch (final Exception e){
//                    LOGGER.error("同步任务json解析失败 taskVersionId = {}", taskVersion.getId(), e);
//                    taskVersion.setSqlText(DataFilter.passwordFilter(taskVersion.getSqlText()));
//                }
//            }
//        }
//
//        final ReadWriteLockVO readWriteLockVO = this.readWriteLockService.getDetail(
//                scheduleTaskVO.getTenantId(), task.getId(),
//                ReadWriteLockType.BATCH_TASK, scheduleTaskVO.getUserId(),
//                task.getModifyUserId(),
//                task.getGmtModified());
//        taskVO.setReadWriteLockVO(readWriteLockVO);
//        taskVO.setUserId(scheduleTaskVO.getUserId());
//        setTaskVariables(taskVO, scheduleTaskVO.getId());
//        final List<Long> userIds = new ArrayList<>();
//        userIds.add(task.getCreateUserId());
//        final Map<Long, User> userMap = userService.getUserMap(userIds);
//        buildUserDTOInfo(userMap,taskVO);
//        return taskVO;
//    }

    private void setTaskOperatorModelAndOptions(final ScheduleTaskVO taskVO, final Task task) {
        if (task.getTaskType().equals(EScheduleJobType.SPARK.getVal())) {
            if (StringUtils.isBlank(task.getExeArgs())) {
                //  兼容之前v3.3及以前生成的task
                taskVO.setOperateModel(TaskOperateType.RESOURCE.getType());
            } else {
                JSONObject exeArgsJson;
                try {
                    exeArgsJson = JSON.parseObject(task.getExeArgs());
                } catch (final Exception e) {
                    // 兼容v3.3之前
                    exeArgsJson = new JSONObject();
                    exeArgsJson.put(CMD_OPTS, task.getExeArgs());
                    exeArgsJson.put(OPERATE_MODEL, TaskOperateType.RESOURCE.getType());
                }
                taskVO.setOptions(exeArgsJson.getString(CMD_OPTS));
                taskVO.setOperateModel(exeArgsJson.getIntValue(OPERATE_MODEL));
            }
        }
    }

    private void setTaskVariables(TaskVO taskVO, final Long taskId) {
        final List<BatchTaskParam> taskParams = this.batchTaskParamService.getTaskParam(taskId);
        final List<Map> mapParams = new ArrayList<>();
        if (taskParams != null) {
            for (final BatchTaskParam taskParam : taskParams) {
                final Map map = new HashMap();
                map.put("type", taskParam.getType());
                map.put("paramName", taskParam.getParamName());
                map.put("paramCommand", taskParam.getParamCommand());
                mapParams.add(map);
            }
        }
        taskVO.setTaskVariables(mapParams);
    }
    private void setTaskVariables(final ScheduleTaskVO taskVO, final Long taskId) {
        final List<BatchTaskParam> taskParams = this.batchTaskParamService.getTaskParam(taskId);
        final List<Map> mapParams = new ArrayList<>();
        if (taskParams != null) {
            for (final BatchTaskParam taskParam : taskParams) {
                final Map map = new HashMap();
                map.put("type", taskParam.getType());
                map.put("paramName", taskParam.getParamName());
                map.put("paramCommand", taskParam.getParamCommand());
                mapParams.add(map);
            }
        }
        taskVO.setTaskVariables(mapParams);
    }

    /**
     * 数据开发-检查task与依赖的task是否有构成有向环
     *
     * @author toutian
     */
    public Task checkIsLoop(Long taskId,
                                 Long dependencyTaskId) {

        HashSet<Long> node = Sets.newHashSet(taskId);

        Long loopTaskId = isHasLoop(dependencyTaskId, node);
        if (loopTaskId == 0L) {
            return null;
        }
        return developTaskDao.getOne(loopTaskId);
    }

    public Long isHasLoop(final Long parentTaskId, final HashSet<Long> node) {
        HashSet<Long> loopNode = new HashSet<>(node.size() + 1);
        loopNode.addAll(node);
        loopNode.add(parentTaskId);
        //出现闭环则返回
        if (loopNode.size() == node.size()) {
            return parentTaskId;
        }

        List<BatchTaskTask> taskTasks = batchTaskTaskService.getAllParentTask(parentTaskId);
        if (CollectionUtils.isEmpty(taskTasks)) {
            return 0L;
        }
        for (BatchTaskTask subTask : taskTasks) {
            Long loopTaskId = isHasLoop(subTask.getParentTaskId(), loopNode);
            if (loopTaskId != 0L) {
                return loopTaskId;
            }
        }
        return 0L;
    }


    /**
     * 判断是否成环
     *
     * @param nodeMap 任务完整依赖关系  key：节点  value 节点的所有父节点
     * @return
     */
    public void checkIsLoopByList(Map<Long, List<Long>> nodeMap) {
        if (MapUtils.isEmpty(nodeMap)) {
            return;
        }
        for (Map.Entry<Long, List<Long>> entry : nodeMap.entrySet()) {
            mapDfs(entry.getKey(), new HashSet(), nodeMap);
        }
    }

    /**
     * 图深度遍历
     *
     * @param taskId  任务ID
     * @param set     已经遍历过的节点
     * @param nodeMap 任务完整依赖关系  key：节点  value 节点的所有父节点
     */
    private void mapDfs(Long taskId, HashSet<Long> set, Map<Long, List<Long>> nodeMap) {
        HashSet<Long> node = new HashSet<>(set);
        // 判断该节点是否以及存在，如果存在，则证明成环了
        if (set.contains(taskId)) {
            Task task = developTaskDao.getOne(taskId);
            if (Objects.nonNull(task)) {
                throw new RdosDefineException(String.format("%s任务发生依赖闭环", task.getName()));
            }
        }
        node.add(taskId);
        for (Long j : nodeMap.get(taskId)){
            mapDfs(j, node, nodeMap);
        }
    }


    public void buildUserDTOInfo(final Map<Long, User> userMap, final ScheduleTaskVO vo) {
        if (Objects.nonNull(vo.getCreateUserId())) {
            User createUser = userMap.get(vo.getCreateUserId());
            UserDTO dto = new UserDTO();
            BeanUtils.copyProperties(createUser, dto);
            vo.setCreateUser(dto);
            if (vo.getCreateUserId().equals(vo.getModifyUserId())) {
                vo.setModifyUser(dto);
            } else {
                UserDTO modifyDto = new UserDTO();
                BeanUtils.copyProperties(userMap.getOrDefault(vo.getModifyUserId(),new User()),modifyDto);
                vo.setModifyUser(modifyDto);
            }
        }
    }

    /**
     * 任务发布
     * @param tenantId
     * @param id
     * @param userId
     * @param publishDesc
     * @param isRoot
     * @param ignoreCheck 是否忽略语法校验
     * @return
     * @throws Exception
     */
    @Transactional
    public TaskCheckResultVO publishTask(Long tenantId, Long id, Long userId, String publishDesc, Boolean isRoot, Boolean ignoreCheck) {
        Task task = developTaskDao.getOne(id);
        if (task == null) {
            throw new RdosDefineException(ErrorCode.CAN_NOT_FIND_TASK);
        }
        return publishBatchTaskInfo(task, tenantId, userId, publishDesc, isRoot, ignoreCheck);
    }
    /**
     * 根据任务状态
     * 判断当前任务是否可以启动
     *
     * @param task
     * @return
     */
    public boolean checkTaskCanRunByStatus(Task task) {
        Integer taskStatus;
        try {
            //todo 有问题
            taskStatus = task.getScheduleStatus();
        } catch (Exception e) {
            LOGGER.error("从Engine查询任务状态异常,{}", e.getMessage(), e);
            throw new RdosDefineException(String.format("从Engine查询任务状态异常,Caused by: %s", e.getMessage()), e);
        }
        return taskStatus == null || TaskStatusCheckUtil.CAN_RUN_STATUS.contains(taskStatus);
    }
    @Transactional
    public TaskCheckResultVO publishTask(Long id, Long userId, String publishDesc, String componentVersion) {
        if (StringUtils.isBlank(publishDesc)) {
            throw new RdosDefineException("(提交描述不能为空)", ErrorCode.INVALID_PARAMETERS);
        }
        Task task = getOne(id);
        if (StringUtils.isNotBlank(componentVersion) && !StringUtils.equals(componentVersion, task.getComponentVersion())) {
            throw new RdosDefineException("flink版本更新，请重新确认并保存后再提交");
        }
        if (Objects.equals(task.getTaskType(), EDataSyncJobType.DATA_ACQUISITION.getVal())) {
            if (!checkTaskCanRunByStatus(task)) {
                throw new RdosDefineException("任务状态未提交发布");
            }
            return publishTaskInfo(task, userId, publishDesc);
        } else if (Objects.equals(task.getTaskType(), EDataSyncJobType.SYNC.getVal())) {
            JSONObject scheduleConf = JSON.parseObject(task.getScheduleConf());
            //判断自定义调度是否合法
//            checkCronValid(scheduleConf);
            return publishTaskInfo(task, userId, publishDesc);
        }
        TaskCheckResultVO checkResultVO = new TaskCheckResultVO();
        checkResultVO.setErrorSign(PublishTaskStatusEnum.NOMAL.getType());
        return checkResultVO;
    }

//
//    /**
//     * 自定义调度周期，判断是否合法，并且调度间隔不能小于5分钟
//     *
//     * @param scheduleConf
//     */
//    private void checkCronValid(JSONObject scheduleConf) {
//        Integer periodType = scheduleConf.getInteger("periodType");
//        if (periodType == ESchedulePeriodType.CRON.getVal()) {
//            String cron = scheduleConf.getString("cron");
//            CronExceptionVO cronExceptionVO = scheduleTaskShadeService.checkCronExpression(cron, MIN_PERIOD);
//            if (cronExceptionVO != null && StringUtils.isNotEmpty(cronExceptionVO.getErrMessage())) {
//                if (cronExceptionVO.getErrCode() == 1) {
//                    throw new RdosDefineException("请填写合法的Cron表达式！");
//                } else if (cronExceptionVO.getErrCode() == 2) {
//                    throw new RdosDefineException("调度周期间隔不可小于5分钟！");
//                } else {
//                    throw new RdosDefineException(cronExceptionVO.getErrMessage());
//                }
//            }
//        }
//    }

    /**
     * 批量发布任务至engine
     *
     * @param userId      用户id
     * @param publishDesc 发布描述
     * @return 发布结果
     */
    public TaskCheckResultVO publishTaskInfo(Task task, Long userId, String publishDesc) {
//        User user = userService.getUser(task.getOwnerUserId());
//        if (user == null) {
//            throw new RdosDefineException(String.format("%s任务责任人在数栈中不存在", task.getName()));
//        }

//        List<Integer> roleList = roleUserService.listRoleValueByUserIdAndProjectId(task.getOwnerUserId(), task.getProjectId(), task.getDtuicTenantId());
//        if (CollectionUtils.isEmpty(roleList)) {
//            Boolean isRootByDtUserId = DtUicUserConnect.getIsRootByDtUserId(environmentContext.getSdkToken(), environmentContext.getDtUicUrl(), user.getDtuicUserId());
//            if (isRootByDtUserId == null || !isRootByDtUserId) {
//                throw new RdosDefineException("任务责任人在项目中不存在");
//            }
//        }
        TaskCheckResultVO checkResultVO = new TaskCheckResultVO();
        checkResultVO.setErrorSign(PublishTaskStatusEnum.NOMAL.getType());
        // 检查任务是否可以发布并记录版本信息
        TaskCheckResultVO<TaskVersion> resultVO = checkTaskAndSaveVersion(task, userId, publishDesc);
        if (!PublishTaskStatusEnum.NOMAL.getType().equals(resultVO.getErrorSign())) {
            return resultVO;
        }
        try {
//            //检查并创建脏数据表
//            if (!Objects.equals(task.getTaskType(), EDataSyncJobType.BATCH_SYNC.getVal())) {
//                DataSyncTaskDirtyDataManage byTaskId = taskDirtyDataManageService.getOneByTaskId(task.getId(), MetaObjectHolder.projectId());
//                if (byTaskId != null&& Objects.equals(byTaskId.getOutputType(), TaskDirtyOutPutTypeEnum.JDBC.getValue())) {
//                    String table = byTaskId.getTableName();
//                    if (StringUtils.isNotBlank(table) && !Objects.equals(table, taskDirtyDataManageService.getDefaultDirtyDataTableName(task.getName()))) {
//                        taskDirtyDataManageShadeService.createTable(byTaskId.getSourceId(), table);
//                    } else {
//                        String tableName = taskDirtyDataManageService.getDefaultDirtyDataTableName(task.getName());
//                        taskDirtyDataManageShadeService.createTable(byTaskId.getSourceId(), tableName);
//                    }
//                }
//            }
            // 构建要发布的任务列表
            ScheduleTaskShadeDTO scheduleTasks = buildScheduleTaskShadeDTO(task,resultVO.getData());
//            taskDirtyDataManageShadeService.syncDirtyDataManage(task.getDtuicTenantId(),task.getId(),task.getProjectId(),task.getTaskType());

            // 提交任务参数信息并保存任务记录和更新任务状态
            sendTaskStartTrigger(task.getId(), userId,scheduleTasks);
            if (!Objects.equals(TaskSubmitStatusEnum.SUBMITTED, task.getSubmitStatus())) {
                developTaskDao.updateSubmitStatus(task.getId(), TaskSubmitStatusEnum.SUBMITTED.getStatus(), null);
            }
            SavaTaskDTO savaTaskDTO = new SavaTaskDTO();
            savaTaskDTO.setScheduleTaskShadeDTO(scheduleTasks);
            //todo 依赖关系没处理
//            savaTaskDTO.setParentTaskIdList(parentTaskIds);
            this.taskService.saveTask(savaTaskDTO);
        } catch (Exception e) {
            LOGGER.error("send task error {} ", task.getId(), e);
            throw new RdosDefineException(String.format("任务提交异常：%s", e.getMessage()), e);
        }
        LOGGER.info("待发布任务参数提交完毕");
        return checkResultVO;
    }

    /**
     * 发送task 执行任务全部信息
     */
    public void sendTaskStartTrigger(Long taskId, Long userId,ScheduleTaskShadeDTO scheduleTasks) throws Exception {
        Task task = developTaskDao.getOne(taskId);
        if (task == null) {
            throw new RdosDefineException("can not find task by id:" + taskId);
        }
        String extroInfo = getExtraInfo(task, userId);
        if(Objects.equals(task.getTaskType(),EDataSyncJobType.DATA_ACQUISITION.getVal())){
            ParamTaskAction paramTaskAction = new ParamTaskAction();
            paramTaskAction.setJobId(task.getJobId());
            paramTaskAction.setIsRestart(0);
            scheduleTasks.setExtraInfo(extroInfo);
            if (!scheduleTasks.getScheduleConf().contains("periodType")) {
                JSONObject scheduleConf = JSONObject.parseObject(scheduleTasks.getScheduleConf());
                scheduleConf.put("periodType", ESchedulePeriodType.DAY.getVal());
                scheduleTasks.setScheduleConf(JSON.toJSONString(scheduleConf));
            }
            paramTaskAction.setBatchTask(scheduleTasks);
            //todo
//            actionServiceClient.addOrUpdateJob(paramTaskAction);
        }
        //todo
//        this.scheduleTaskShadeService.info(taskId, AppType.DATASYNC.getType(), extroInfo);
    }



    /**
     * 初始化engine info接口extroInfo信息
     *
     * @param task
     * @param userId
     * @return info信息
     * @throws Exception
     */
    private String getExtraInfo(Task task, Long userId) throws Exception {
        String extroInfo = "";
        Long taskId = task.getId();
        // 跨项目的时候 需要依赖 task的project

        final Map<String, Object> actionParam = new HashMap<>(10);
        // todo 离线用到
        List<BatchTaskParam> taskParam = batchTaskParamService.getTaskParam(task.getId());
//        taskParam.forEach(paramShade -> paramShade.setType(EParamType.getEngineTypeByType(paramShade.getType())));
        hadoopJobExeService.readyForTaskStartTrigger(actionParam, task.getTenantId(), task);
        JSONObject confProp = new JSONObject();
//        buildTaskDirtyDataManageArgs(task.getId(), task.getProjectId(), confProp, task.getName());
        actionParam.put("confProp", JSON.toJSONString(confProp));

        actionParam.put("taskId", taskId);
//        actionParam.put("engineType", EngineType.Flink.getEngineName());
        actionParam.put("taskType", EDataSyncJobType.getEngineJobType(task.getTaskType()));
        actionParam.put("name", task.getName());
        actionParam.put("computeType", task.getComputeType());
        actionParam.put("tenantId", task.getTenantId());
        actionParam.put("isFailRetry", false);
        actionParam.put("maxRetryNum", 0);
        actionParam.put("multiEngineType", MultiEngineType.HADOOP.getType());
        actionParam.put("taskParamsToReplace", JSON.toJSONString(taskParam));
        actionParam.put("userId", userId);

        // 出错重试配置,兼容之前的任务，没有这个参数则默认重试
        final JSONObject scheduleConf = JSON.parseObject(task.getScheduleConf());
        if (scheduleConf != null && scheduleConf.containsKey("isFailRetry")) {
            actionParam.put("isFailRetry", scheduleConf.getBooleanValue("isFailRetry"));
            if (scheduleConf.getBooleanValue("isFailRetry")) {
                final int maxRetryNum = scheduleConf.getIntValue("maxRetryNum") == 0 ? 3 : scheduleConf.getIntValue("maxRetryNum");
                actionParam.put("maxRetryNum", maxRetryNum);
            } else {
                actionParam.put("maxRetryNum", 0);
            }
        }
        extroInfo = objMapper.writeValueAsString(actionParam);
        extroInfo = extroInfo.replaceAll("\r\n", System.getProperty("line.separator"));
        return extroInfo;
    }
    private boolean isRestore(String job) {
        JSONObject jobJson = JSONObject.parseObject(job);
        Object isRestore = JSONPath.eval(jobJson, "$.job.setting.restore.isRestore");
        return BooleanUtils.toBoolean(String.valueOf(null == isRestore ? "true" : isRestore));
    }

    /**
     * 构建一个要发布到engine的任务DTO {@link ScheduleTaskShadeDTO}
     *
     * @param task 要发布的任务集合
     * @return 调度任务DTO
     */
    private ScheduleTaskShadeDTO buildScheduleTaskShadeDTO(final Task task,TaskVersion taskVersion) {
        if (task.getId() <= 0) {
            //只有异常情况才会走到该逻辑
            throw new RdosDefineException("task id can't be 0", ErrorCode.SERVER_EXCEPTION);
        }
        //保存batch_task_shade
        final ScheduleTaskShadeDTO scheduleTaskShadeDTO = new ScheduleTaskShadeDTO();
        BeanUtils.copyProperties(task, scheduleTaskShadeDTO);
        scheduleTaskShadeDTO.setTaskId(task.getId());
//        scheduleTaskShadeDTO.setAppType(AppType.DATASYNC.getType());
//        scheduleTaskShadeDTO.setEngineType(EngineType.Flink.getVal());
        scheduleTaskShadeDTO.setTenantId(scheduleTaskShadeDTO.getTenantId());
//        scheduleTaskShadeDTO.setSubmitStatus(ESubmitStatus.SUBMIT.getStatus());
        scheduleTaskShadeDTO.setVersionId(Math.toIntExact(taskVersion.getId()));
//        scheduleTaskShadeDTO.setVersion(Math.toIntExact(taskVersion.getId()));
        if(Objects.equals(task.getTaskType(), EDataSyncJobType.SYNC.getVal()) && StringUtils.isNotEmpty(task.getScheduleConf())) {
            JSONObject scheduleConfig = JSONObject.parseObject(task.getScheduleConf());
            if (scheduleConfig != null) {
//                scheduleTaskShadeDTO.setIsExpire(scheduleConfig.getBooleanValue("isExpire") ? 1 : 0);
                scheduleTaskShadeDTO.setPeriodType(scheduleConfig.getInteger("periodType"));
            } else {
//                scheduleTaskShadeDTO.setIsExpire(0);
                scheduleTaskShadeDTO.setPeriodType(2);
            }
//            scheduleTaskShadeDTO.setProjectScheduleStatus(projectService.getProjectByProjectId(task.getProjectId()).getScheduleStatus());
//            if (Objects.equals(EDataSyncJobType.BATCH_SYNC.getVal(),task.getTaskType())) {
                //批量同步任务外层壳不开启调度
//                scheduleTaskShadeDTO.setProjectScheduleStatus(EProjectScheduleStatus.PAUSE.getStatus());
//            }else {
//                scheduleTaskShadeDTO.setProjectScheduleStatus(projectService.getProjectByProjectId(task.getProjectId()).getScheduleStatus());
//            }
//            if (task.getGroupId() != null && task.getGroupId() > 0) {
//                // 认为是批量同步任务
//                scheduleTaskShadeDTO.setFlowId(task.getGroupId());
//            }
        }
//        else if(Objects.equals(task.getTaskType(), EDataSyncJobType.DATA_ACQUISITION.getVal())){
//            JSONObject sourceJson = JSONObject.parseObject(task.getSourceStr());
//            if (sourceJson == null) { //脚本模式没有这个字段 默认传数据同步
//                scheduleTaskShadeDTO.setBusinessType(SyncContentEnum.DATA_SYNC.getType().toString());
//            } else {
//                Integer syncContent = sourceJson.getInteger("syncContent");
//                scheduleTaskShadeDTO.setBusinessType(syncContent.toString());
//            }
//        }
        return scheduleTaskShadeDTO;
    }

    /**
     * 批量发布任务至engine
     * @param publishTask 要发布的task集合
     * @param tenantId 项目id
     * @param userId 用户id
     * @param publishDesc 发布描述
     * @param isRoot 是否是管理员
     * @param ignoreCheck 忽略检查
     * @return 发布结果
     */
    public TaskCheckResultVO publishBatchTaskInfo(Task publishTask, Long tenantId, Long userId, String publishDesc, Boolean isRoot, Boolean ignoreCheck) {
        //判断任务责任人是否存在 如果任务责任人不存在或无权限 不允许提交
        User user = userService.getById(publishTask.getCreateUserId());
        if (user == null){
            throw new RdosDefineException(String.format("%s任务责任人在数栈中不存在", publishTask.getName()));
        }

        TaskCheckResultVO checkResultVO = new TaskCheckResultVO();
        checkResultVO.setErrorSign(PublishTaskStatusEnum.NOMAL.getType());

        // 检查任务是否可以发布并记录版本信息
        TaskCheckResultVO resultVO = checkTaskAndSaveVersion(publishTask, tenantId, userId, publishDesc, isRoot, ignoreCheck);
        if (!PublishTaskStatusEnum.NOMAL.getType().equals(resultVO.getErrorSign())){
            //做一下优化 如果是工作流任务的话 把任务名称打印出来
            if (publishTask.getFlowId()>0){
                resultVO.setErrorMessage(String.format("任务:%s提交失败，原因是:%s", publishTask.getName(), resultVO.getErrorMessage()));
            }
            return resultVO;
        }

        // 发布任务中所有的依赖关系
        List<Long> parentTaskIds = Lists.newArrayList();
        ScheduleTaskShadeDTO scheduleTaskShadeDTO = buildScheduleTaskShadeDTO(publishTask, parentTaskIds);

        // 提交任务参数信息并保存任务记录和更新任务状态
        try {
            Task task = getOneWithError(scheduleTaskShadeDTO.getTaskId());
            String extraInfo = this.batchJobService.getExtraInfo(task, userId, null);
            scheduleTaskShadeDTO.setExtraInfo(extraInfo);
            // 无异常保存一条任务记录并更新任务状态
            saveRecordAndUpdateSubmitStatus(task, tenantId, userId, TaskOperateType.COMMIT.getType(), ESubmitStatus.SUBMIT.getStatus());
        } catch (Exception e) {
            LOGGER.error("send task error {} ", scheduleTaskShadeDTO.getTaskId(), e);
            throw new RdosDefineException(String.format("任务提交异常：%s", e.getMessage()), e);
        }

        SavaTaskDTO savaTaskDTO = new SavaTaskDTO();
        savaTaskDTO.setScheduleTaskShadeDTO(scheduleTaskShadeDTO);
        savaTaskDTO.setParentTaskIdList(parentTaskIds);
        // 批量发布任务
        this.taskService.saveTask(savaTaskDTO);

        LOGGER.info("待发布任务参数提交完毕");
        return checkResultVO;
    }

    /**
     * 保存一条任务记录并更新任务状态
     * @param task 任务信息
     * @param tenantId 租户id
     * @param userId 用户id
     * @param taskOperateType 任务操作类型 @{@link TaskOperateType}
     * @param submitStatus 发布状态 {@link ESubmitStatus}
     */
    private void saveRecordAndUpdateSubmitStatus(Task task, Long tenantId, Long userId, Integer taskOperateType, Integer submitStatus) {
        this.updateSubmitStatus(tenantId, task.getId(), submitStatus);
    }
    /**
     * 检查要发布的任务并保存版本信息
     *
     * @param task        任务信息
     * @param userId      用户id
     * @param publishDesc 发布描述
     * @return 检查结果
     */
    private TaskCheckResultVO checkTaskAndSaveVersion(Task task, Long userId, String publishDesc) {
        TaskCheckResultVO checkVo = new TaskCheckResultVO();
        checkVo.setErrorSign(PublishTaskStatusEnum.NOMAL.getType());
        TaskVersion taskVersion ;
        if (StringUtils.isBlank(task.getSqlText())) {
            throw new RdosDefineException(task.getName() + "任务配置信息为空", ErrorCode.TASK_CAN_NOT_SUBMIT);
        }
        checkTaskCanSubmit(task);
        taskVersion = saveTaskVersion(task, userId, publishDesc, true);
        checkVo.setData(taskVersion);
        return checkVo;
    }
    /**
     * 保存任务版本信息
     *
     * @param task
     * @param userId
     * @param publishDesc
     */
    private TaskVersion saveTaskVersion(Task task, Long userId, String publishDesc,Boolean isCheckFormat) {
        TaskVersion taskVersion = new TaskVersion();
        taskVersion.setCreateUserId(userId);
        if (StringUtils.isNotBlank(task.getSqlText())) {
            final JSONObject jsonTask = JSON.parseObject(Base64Util.baseDecode(task.getSqlText()));
            Integer createModelType = Integer.valueOf(jsonTask.getString("createModel"));
            JSONObject job = jsonTask.getJSONObject("job");
            if (Objects.isNull(job)) {
                throw new RdosDefineException(String.format("数据同步任务：%s 未配置", task.getName()));
            }
            // 检测job格式
            if(BooleanUtils.isTrue(isCheckFormat)){
                DaJobCheck.checkJobFormat(job.toJSONString(), createModelType);
            }
            taskVersion.setSqlText(jsonTask.toJSONString());
            taskVersion.setOriginSql(task.getSqlText());
        }else {
            taskVersion.setSqlText(StringUtils.EMPTY);
            taskVersion.setOriginSql(StringUtils.EMPTY);
        }
        taskVersion.setTaskId(task.getId());
        taskVersion.setVersion(task.getVersion());
        taskVersion.setTaskParams(task.getTaskParams());
        taskVersion.setPublishDesc(publishDesc);
        taskVersion.setExeArgs(task.getExeArgs());
        taskVersion.setSourceStr(task.getSourceStr());
        taskVersion.setTargetStr(task.getTargetStr());
        taskVersion.setSettingStr(task.getSettingStr());
        taskVersion.setModifyUserId(task.getModifyUserId());
        taskVersion.setTaskDesc(task.getTaskDesc());
        taskVersion.setCreateModel(task.getCreateModel());
        taskVersion.setComponentVersion(task.getComponentVersion());
        taskVersion.setScheduleConf(task.getScheduleConf());
        taskVersion.setPeriodType(task.getPeriodType());
        taskVersion.setScheduleStatus(task.getScheduleStatus());
        taskVersionService.insert(taskVersion);
        return taskVersion;
    }

    /**
     * 检查要发布的任务并保存版本信息
     *
     * @param task 任务信息
     * @param tenantId 项目id
     * @param userId 用户id
     * @param publishDesc 发布描述
     * @param isRoot 是否是管理员
     * @param ignoreCheck 是否忽略检查
     * @return 检查结果
     */
    private TaskCheckResultVO checkTaskAndSaveVersion(Task task, Long tenantId, Long userId, String publishDesc, Boolean isRoot, Boolean ignoreCheck) {
        TaskCheckResultVO checkVo  = new TaskCheckResultVO();
        checkVo.setErrorSign(PublishTaskStatusEnum.NOMAL.getType());
        checkTaskCanSubmit(task);

        task.setSubmitStatus(ESubmitStatus.SUBMIT.getStatus());
        task.setGmtModified(Timestamp.valueOf(LocalDateTime.now()));

        final TaskVersion version = new TaskVersion();
        version.setCreateUserId(userId);

        String versionSqlText = StringUtils.EMPTY;

        if (EScheduleJobType.SPARK_SQL.getVal().intValue() == task.getTaskType().intValue()) {
            // 语法检测
            List<BatchTaskParam> taskParamsToReplace = batchTaskParamService.getTaskParam(task.getId());
            versionSqlText = this.jobParamReplace.paramReplace(task.getSqlText(), taskParamsToReplace, this.sdf.format(new Date()));
            //避免重复校验
            CheckSyntaxResult syntaxResult = batchSqlExeService.processSqlText(task.getTenantId(), task.getTaskType(), versionSqlText);
            if (!syntaxResult.getCheckResult()){
                checkVo.setErrorSign(PublishTaskStatusEnum.CHECKSYNTAXERROR.getType());
                checkVo.setErrorMessage(syntaxResult.getMessage());
                return checkVo;
            }

        } else if (EScheduleJobType.SYNC.getVal().intValue() == task.getTaskType().intValue()) {
            if (StringUtils.isNotEmpty(task.getSqlText())) {
                final JSONObject jsonTask = JSON.parseObject(Base64Util.baseDecode(task.getSqlText()));

                Integer createModelType = Integer.valueOf(jsonTask.getString("createModel"));
                JSONObject job = jsonTask.getJSONObject("job");
                if (Objects.isNull(job)) {
                    throw new RdosDefineException(String.format("数据同步任务：%s 未配置", task.getName()));
                }
                // 检测job格式
                SyncJobCheck.checkJobFormat(job.toJSONString(), createModelType);
                versionSqlText = jsonTask.getString("job");
            }
        }

        version.setSqlText(versionSqlText);
        version.setOriginSql(task.getSqlText());
        version.setTenantId(task.getTenantId());
        version.setTaskId(task.getId());
        //任务的版本号
        version.setVersion(task.getVersion());
        version.setTaskParams(task.getTaskParams());
        version.setScheduleConf(task.getScheduleConf());
        version.setScheduleStatus(task.getScheduleStatus());
        version.setGmtModified(task.getGmtModified());

        String dependencyTaskIds = StringUtils.EMPTY;
        final List<BatchTaskTask> taskTasks = this.batchTaskTaskService.getAllParentTask(task.getId());
        if (CollectionUtils.isNotEmpty(taskTasks)) {
            List<Map<String, Object>> parentTasks = taskTasks.stream().map(taskTask -> {
                Map<String, Object> map = Maps.newHashMap();
                map.put("parentTaskId", taskTask.getParentTaskId());
                return map;
            }).collect(Collectors.toList());
            dependencyTaskIds = JSON.toJSONString(parentTasks);
        }
//todo
//        version.setDependencyTaskIds(dependencyTaskIds);
        version.setPublishDesc(null == publishDesc ? "" : publishDesc);
        // 插入一条记录信息
        taskVersionService.insert(version);
        task.setVersion(version.getId().intValue());
        return checkVo;
    }

    /**
     * 构建一个要发布到engine的任务DTO {@link ScheduleTaskShadeDTO}
     * @param task 要发布的任务集合
     * @param parentTaskIds 父任务的id
     * @return 调度任务DTO
     */
    private ScheduleTaskShadeDTO buildScheduleTaskShadeDTO(final Task task, List<Long> parentTaskIds) {
        if (task.getId() <= 0) {
            //只有异常情况才会走到该逻辑
            throw new RdosDefineException("task id can't be 0", ErrorCode.SERVER_EXCEPTION);
        }

        final long taskId = task.getId();
        //清空任务关联的batch_task_param, task_resource, task_task 表信息
        this.batchTaskParamShadeService.clearDataByTaskId(taskId);
        this.batchTaskResourceShadeService.clearDataByTaskId(taskId);

        final List<BatchTaskParam> batchTaskParamList = this.batchTaskParamService.getTaskParam(task.getId());
        //查询出任务所有的关联的资源(运行主体资源和依赖引用资源)
        final List<BatchTaskResource> batchTaskResourceList = this.batchTaskResourceService.getTaskResources(task.getId(), null);
        List<Long> parentTaskList = this.batchTaskTaskService.getAllParentTaskId(task.getId());
        parentTaskIds.addAll(parentTaskList);

        if (!CollectionUtils.isEmpty(batchTaskResourceList)) {
            this.batchTaskResourceShadeService.saveTaskResource(batchTaskResourceList);
        }
        //保存batch_task_shade
        final ScheduleTaskShadeDTO scheduleTaskShadeDTO = new ScheduleTaskShadeDTO();
        BeanUtils.copyProperties(task, scheduleTaskShadeDTO);
        scheduleTaskShadeDTO.setTaskId(task.getId());
        scheduleTaskShadeDTO.setScheduleStatus(EScheduleStatus.NORMAL.getVal());

        if (!CollectionUtils.isEmpty(batchTaskParamList)) {
            this.batchTaskParamShadeService.saveTaskParam(batchTaskParamList);
        }else{
            scheduleTaskShadeDTO.setTaskParams("");
        }

        return scheduleTaskShadeDTO;
    }

    public List<BatchTaskVersionDetailDTO> getTaskVersionRecord(Long taskId, Integer pageSize, Integer pageNo) {
        if (pageNo == null) {
            pageNo = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        PageQuery pageQuery = new PageQuery(pageNo, pageSize, "gmt_create", Sort.DESC.name());
        List<BatchTaskVersionDetailDTO> res = taskVersionService.listByTaskId(taskId, pageQuery);
        for (BatchTaskVersionDetailDTO detail : res) {
            detail.setUserName(userService.getUserName(detail.getCreateUserId()));
        }
        return res;
    }

    public BatchTaskVersionDetailDTO taskVersionScheduleConf(Long versionId) {
        BatchTaskVersionDetailDTO taskVersion = taskVersionService.getByVersionId(versionId);
        if (taskVersion == null) {
            return null;
        }
        taskVersion.setUserName(userService.getUserName(taskVersion.getCreateUserId()));
        if (StringUtils.isNotBlank(taskVersion.getDependencyTaskIds())) {
            List<Map<String, Object>> dependencyTasks = getDependencyTasks(taskVersion.getDependencyTaskIds());
            JSONObject taskParams = new JSONObject();
            int i = 1;
            for (Map<String, Object> dependencyTask : dependencyTasks) {
                ScheduleTaskShade taskShade = taskService.findTaskByTaskId(MathUtil.getLongVal(dependencyTask.get("parentTaskId")));
                if (taskShade != null) {
                    JSONObject taskParam = new JSONObject();
                    taskParam.put("taskName", taskShade.getName());
                    taskParam.put("tenantName", tenantService.getTenantById(taskShade.getTenantId()).getTenantName());
                    taskParams.put("task" + i++, taskParam);
                }
            }
            taskVersion.setDependencyTasks(taskParams);
        }
        return taskVersion;
    }


    @Transactional(rollbackFor = Exception.class)
    public TaskVO getTaskVOAndAddOrUpdateTask(TaskResourceParam taskResourceParam) {
        TaskVO task = addOrUpdateTask(taskResourceParam);
        return this.getTaskById(task);
    }

    public TaskVO addOrUpdateTask(TaskResourceParam taskResourceParam) {
        // 校验任务信息,主资源不能为空
//        checkTaskParam(taskResourceParam);
        TaskVO taskVO = TaskMapstructTransfer.INSTANCE.TaskResourceParamToTaskVO(taskResourceParam);
        if (taskResourceParam.getUpdateSource()) {
            taskVO.setSourceStr(JSON.toJSONString(taskResourceParam.getSourceMap()));
            taskVO.setTargetStr(JSON.toJSONString(taskResourceParam.getTargetMap()));
            taskVO.setSettingStr(JSON.toJSONString(taskResourceParam.getSettingMap()));
        } else {
            Task task = getOne(taskVO.getId());
            taskVO.setSourceStr(task.getSourceStr());
            taskVO.setTargetStr(task.getTargetStr());
            taskVO.setSettingStr(task.getSettingStr());
        }
        //检查密码回填操作
        this.checkFillPassword(taskResourceParam);
        setSqlTextByCreateModel(taskResourceParam, taskVO);
        taskVO.setModifyUserId(taskResourceParam.getUserId());
//        taskVO.setEngineType(EngineType.Flink.getVal());
        //任务中引用的数据源id，不从指定 offset 重跑保存的时候 sourceId 从任务中解析，从 offset 重跑沿用之前任务关联的 sourceId
//        List<Long> dataSourceIds = getDataSourceIds(taskResourceParam);
        taskVO = (TaskVO) updateTask(taskVO, true);
        return taskVO;
    }

    /**
     * 新增/更新任务
     * 内部使用 不对外提供
     *
     * @param taskVO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Task updateTask(TaskVO taskVO,Boolean taskParam) {

        if (taskVO.getName() == null) {
            throw new RdosDefineException("名称不能为空", ErrorCode.INVALID_PARAMETERS);
        }

        taskVO.setGmtModified(Timestamp.valueOf(LocalDateTime.now()));
        Task task = developTaskDao.getByName(taskVO.getName(), taskVO.getTenantId());

        if (taskVO.getId() > 0) {//update
            if (task != null && task.getName().equals(taskVO.getName()) && !task.getId().equals(taskVO.getId())) {
                throw new RdosDefineException(ErrorCode.NAME_ALREADY_EXIST);
            }
            batchTaskParamService.checkParams(taskVO.getSqlText(), taskVO.getTaskVariables());
            updateTask(taskVO);
        } else {
            if (task != null) {
                throw new RdosDefineException(ErrorCode.NAME_ALREADY_EXIST);
            }
            addTask(taskVO);
        }
        if(BooleanUtils.isTrue(taskParam)){
            batchTaskParamService.addOrUpdateTaskParam(taskVO.getTaskVariables(),taskVO.getId());
        }


        if (!taskVO.getUpdateSource()) {
            return taskVO;
        }
//        updateTaskRefRelation(taskVO, dataSourceIds);

        return taskVO;
    }
    /**
     * 新增任务
     *
     * @param taskVO
     */
    private void addTask(TaskVO taskVO) {
        taskVO.setGmtCreate(Timestamp.valueOf(LocalDateTime.now()));
//        taskVO.setJobId(SdkMessageCheckUtil.checkGenerateUniqueSign(actionServiceClient.generateUniqueSign()));
        taskVO.setTaskParams(taskVO.getTaskParams() == null ?taskParamTemplateService.getTaskParamTemplate(taskVO.getComponentVersion(),taskVO.getTaskType()).getParams():taskVO.getTaskParams());
        taskVO.setTenantId(taskVO.getTenantId());
//        taskVO.setOwnerUserId(taskVO.getUserId());
        taskVO.setScheduleStatus(EScheduleStatus.NORMAL.getVal());
        if (Objects.equals(taskVO.getTaskType(), EDataSyncJobType.DATA_ACQUISITION.getVal())) {
            taskVO.setScheduleConf(String.format(DEFAULT_STREAM_SCHEDULE_CONF, TimeUtil.getDate(new Date(),0),TimeUtil.getDate(new Date(),100) ));
        } else if (Objects.equals(taskVO.getTaskType(), EDataSyncJobType.SYNC.getVal())) {
            taskVO.setScheduleConf(String.format(DEFAULT_BATCH_SCHEDULE_CONF, TimeUtil.getDate(new Date(), 0), TimeUtil.getDate(new Date(), 100)));
        }

        if (StringUtils.isBlank(taskVO.getSqlText())) {
            taskVO.setSqlText("");
        }
        try {
            developTaskDao.insert(taskVO);
        } catch (Exception e) {
            if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("Duplicate entry")) {
                throw new RdosDefineException("(新建任务失败，该项目下任务名称已经存在)", ErrorCode.NAME_ALREADY_EXIST);
            }
            throw new RdosDefineException("新建任务失败", e);
        }
    }
    /**
     * 转化环境参数，不同版本之间切换需要刷新环境参数信息
     *
     * @param before       转化前的 flink 版本
     * @param after        转化后的 flink 版本
     * @param paramsBefore 环境参数
     * @return 转化后的环境参数
     */
    public String convertParams(FlinkVersion before, FlinkVersion after, String paramsBefore,Integer taskType) {
        // 版本一致不需要进行转换
        if (before.equals(after)) {
            return paramsBefore;
        }
        return taskParamTemplateService.getTaskParamTemplate(after.getType(),taskType).getParams();
    }

    /**
     * 修改任务
     *
     * @param taskVO
     */
    private void updateTask(TaskVO taskVO) {
        Task specialTask = developTaskDao.getOne(taskVO.getId());
        if (specialTask == null) {
            throw new RdosDefineException(ErrorCode.CAN_NOT_FIND_TASK);
        }
        // 转换环境参数
        String convertParams = convertParams(FlinkVersion.getVersion(specialTask.getComponentVersion()),
                FlinkVersion.getVersion(taskVO.getComponentVersion()),
                taskVO.getTaskParams(),taskVO.getTaskType());
        taskVO.setTaskParams(convertParams);

        //由于密码脱敏，脚本模式保存时密码变成"******"，进行按照原储存信息进行还原，依据是url+username
        if (CREATE_MODEL_TEMPLATE == specialTask.getCreateModel() && Objects.equals(specialTask.getTaskType(), EDataSyncJobType.DATA_ACQUISITION.getVal())) {
            String sqlText = TaskUtils.resumeTemplatePwd(taskVO.getSqlText(), specialTask);
            taskVO.setSqlText(sqlText);
        }
        Task specialTask1 = new Task();

        TaskMapstructTransfer.INSTANCE.taskVOTOTask(taskVO, specialTask1);
        Integer update = developTaskDao.update(specialTask1);
        if (update == 1) {
            taskVO.setVersion(taskVO.getVersion() + 1);
        }
    }
    /**
     * 实时采集任务根据操作模式生成sqlText
     *
     * @param taskResourceParam
     * @param task
     */
    private void setSqlTextByCreateModel(TaskResourceParam taskResourceParam, Task task) {
        if (taskResourceParam.getSourceMap() != null && DataSourceType.Polardb_For_MySQL.getVal().equals(MapUtils.getInteger(taskResourceParam.getSourceMap(), "type"))) {
            taskResourceParam.getSourceMap().put("type", DataSourceType.MySQL.getVal());
        }
        String sqlText = taskResourceParam.getSqlText();
        Integer createModel = taskResourceParam.getCreateModel();
        if (CREATE_MODEL_TEMPLATE==createModel) {
            JSONObject sql = new JSONObject(2);
            sql.put("job", sqlText);
            sql.put("createModel", CREATE_MODEL_TEMPLATE);
            if (Objects.equals(taskResourceParam.getTaskType(), EDataSyncJobType.SYNC.getVal())) {
                batchTaskParamService.checkParams(sql.toJSONString(), taskResourceParam.getTaskVariables());
            }
            task.setSqlText(Base64Util.baseEncode(sql.toJSONString()));
        } else if (CREATE_MODEL_GUIDE==createModel) {
            String daSqlText;
            if (taskResourceParam.isPreSave()) {
                //发布过之后，校验 输入输出数据源是否改变
//                checkVersion(streamTask, taskResourceParam);
                // todo 要优化写法
                TaskUtils.dealWithTaskParam(taskResourceParam);
                daSqlText = getDASqlText(taskResourceParam);
                task.setSqlText(daSqlText);
            }
        } else {
            throw new RdosDefineException("createModel incorrect parameter", ErrorCode.INVALID_PARAMETERS);
        }
    }


    /**
     * 获取sqlText
     *
     * @param param
     * @return
     */
    public String getDASqlText(TaskResourceParam param) {
        try {
            //格式化入参(前端会在sourceMap传入很多无效参数，需要格式化入参获取真正需要的参数)
            int sourceType = Integer.parseInt(String.valueOf(param.getSourceMap().get("type")));
            DataSourceType dataSourceType = DataSourceType.getSourceType(sourceType);
            DaReaderBuilder daReaderBuilder = daReaderBuilderFactory.getDaReaderBuilder(dataSourceType);
            if (daReaderBuilder == null) {
                throw new RdosDefineException(ErrorCode.SOURCE_CAN_NOT_AS_INPUT);
            }
            //来源集合
            Map<String, Object> sourceMap = daReaderBuilder.getParserSourceMap(param.getSourceMap());
            param.setSourceMap(sourceMap);
            //前端入参 需要保存
            Map<String, Object> sourceParamMap = new HashMap<>(sourceMap);
            Reader reader = daReaderBuilder.daReaderBuild(param);

            //目标集合
            Map<String, Object> targetMap = param.getTargetMap();
            //流控、错误集合
            Map<String, Object> settingMap = param.getSettingMap();
            Writer writer = null;
            Setting setting = null;
            Restoration restoration = null;
            JSONObject nameMappingJson = null;
            Integer targetType = Integer.parseInt(String.valueOf(targetMap.get("type")));
            DataSourceType targetDataSourceType = DataSourceType.getSourceType(targetType);
            DaWriterBuilder daWriterBuilder = daWriterBuilderFactory.getDaWriterBuilder(targetDataSourceType);
            writer = daWriterBuilder.daWriterBuild(param);
            setting = PublicUtil.objectToObject(settingMap, DefaultSetting.class);

            NameMappingBuilder mysqlNameMappingBuilder = nameMappingBuilderFactory.getDaReaderBuilder(dataSourceType);
            if (mysqlNameMappingBuilder != null) {
                nameMappingJson = mysqlNameMappingBuilder.daReaderBuild(param);
            }

            //转脚本模式直接返回
            if (CREATE_MODEL_TEMPLATE == param.getCreateModel()) {
                String jobText = getJobText(putDefaultEmptyValueForReader(sourceType, reader),
                        putDefaultEmptyValueForWriter(targetType, writer), putDefaultEmptyValueForSetting(setting), nameMappingJson, restoration, param);
                return jobText;
            }

            //获得数据同步job.xml的配置
            String jobXml = getJobText(reader, writer, setting, nameMappingJson, restoration, param);
            String parserXml = getParserText(sourceParamMap, targetMap, settingMap);

            JSONObject sql = new JSONObject(3);
            sql.put("job", jobXml);
            sql.put("parser", parserXml);
            sql.put("createModel", CREATE_MODEL_GUIDE);
            return Base64Util.baseEncode(sql.toJSONString());
        } catch (Exception e) {
            throw new RdosDefineException("解析实时采集任务失败: " + e.getMessage(), ErrorCode.SERVER_EXCEPTION, e);
        }
    }

    private String getParserText(final Map<String, Object> sourceParamMap,
                                 final Map<String, Object> targetMap,
                                 final Map<String, Object> settingMap) {
        JSONObject parser = new JSONObject(4);
        parser.put("sourceMap", sourceParamMap);
        parser.put("targetMap", getTargetMap(targetMap));
        parser.put("setting", settingMap);
        return parser.toJSONString();
    }

    public Map<String, Object> getTargetMap(Map<String, Object> targetMap) {
        Map<String, Object> map = new HashMap<>(4);
        map.put("type", targetMap.get("type"));
        map.put("sourceId", targetMap.get("sourceId"));
        map.put("name", targetMap.get("name"));
        return map;
    }

    private Setting putDefaultEmptyValueForSetting(Setting setting) {
        DefaultSetting defaultSetting = (DefaultSetting) setting;
        defaultSetting.setSpeed(-1.0d);
        return defaultSetting;
    }
    private Reader putDefaultEmptyValueForReader(Integer sourceType, Reader reader) {
       /* if (SupportDataSourcesService.REALTIME_SCRIPT_MODE_SOURCE_TYPES.contains(sourceType)) {
            return reader;
        }*/
//        throw new DataSyncAppDefineException("输入源类型暂不支持");
        return reader;
    }

    private Writer putDefaultEmptyValueForWriter(int sourceType, Writer writer) {
       /* if (SupportDataSourcesService.REALTIME_SCRIPT_MODE_TARGET_TYPES.contains(sourceType)) {
            return writer;
        }*/
//        throw new DataSyncAppDefineException("输出源类型暂不支持");
        return writer;
    }

    /**
     * @author toutian
     */
    private String getJobText(final Reader reader,
                              final Writer writer, final Setting setting, final JSONObject nameMapping,final Restoration restoration,final TaskResourceParam param) {
        FlinkxJobTemplate flinkxJobTemplate = new FlinkxJobTemplate() {
            @Override
            public Setting newSetting() {
                return setting;
            }

            @Override
            public JSONObject nameMapping() {
                return nameMapping;
            }

            @Override
            public Restoration restoration() {
                return restoration;
            }

            @Override
            public Reader newReader() {
                return reader;
            }

            @Override
            public Writer newWrite() {
                return writer;
            }
        };
        return flinkxJobTemplate.toJobJsonString(param);
    }
//    /**
//     * 数据开发-新建/更新 任务
//     *
//     * @param param 任务
//     * @return
//     * @throws NoSuchFieldException
//     * @throws IllegalAccessException
//     * @author toutian
//     */
//    @Transactional(rollbackFor = Exception.class)
//    public TaskCatalogueVO addOrUpdateTask(final TaskResourceParam param) {
//        //检查密码回填操作
//        this.checkFillPassword(param);
//        //数据预处理 主要是数据同步任务 生成sqlText
//        this.checkBeforeUpdateTask(param);
//        if (StringUtils.isNotBlank(param.getScheduleConf())) {
//            //处理调度配置
//            JSONObject schduleConf = JSON.parseObject(param.getScheduleConf());
//            if (schduleConf.get("isExpire") != null && "false".equals(schduleConf.get("isExpire").toString())) {
//                schduleConf.replace("isLastInstance", true);
//                param.setScheduleConf(schduleConf.toString());
//            }
//            param.setPeriodType(schduleConf.getInteger("periodType"));
//        }
//        if (param.getId() > 0 && param.getTaskType().equals(EScheduleJobType.WORK_FLOW.getVal())) {
//            //更新子任务间的依赖关系
//            final String sqlText = param.getSqlText();
//            if (StringUtils.isNotBlank(sqlText)) {
//                final Map<Long, List<Long>> relations = this.parseTaskRelationsFromSqlText(sqlText);
//                // 判断任务依赖是否成环
//                if (MapUtils.isNotEmpty(relations)) {
//                    checkIsLoopByList(relations);
//                }
//                for (final Map.Entry<Long, List<Long>> entry : relations.entrySet()) {
//                    List<Task> dependencyTasks = getTaskByIds(entry.getValue());
//                    dependencyTasks.stream().forEach(task -> {
//                        task.setTenantId(param.getTenantId());
//                    });
//                    batchTaskTaskService.addOrUpdateTaskTask(entry.getKey(), dependencyTasks);
//                }
//            }
//        }
//
//        BatchTaskBatchVO task = PublicUtil.objectToObject(param, BatchTaskBatchVO.class);
//        task.setModifyUserId(param.getUserId());
//        task.setVersion(Objects.isNull(param.getVersion()) ? 0 : param.getVersion());
//        task.parsePeriodType();
//        task = this.updateTask(task, param.getEditBaseInfo());
//        TaskCatalogueVO taskCatalogueVO = new TaskCatalogueVO(task, task.getNodePid());
//
//        // 强行置为更新
//        taskCatalogueVO.getReadWriteLockVO().setResult(TaskLockStatus.TO_UPDATE.getVal());
//
//        //更新 关联资源
//        if (param.getResourceIdList() != null) {
//            final Map<String, Object> params = Maps.newHashMap();
//            params.put("id", task.getId());
//            params.put("resources", param.getResourceIdList());
//            params.put("createUserId", task.getCreateUserId());
//            this.updateTaskResource(params);
//        }
//
//        if (param.getRefResourceIdList() != null) {
//            final Map<String, Object> params = Maps.newHashMap();
//            params.put("id", task.getId());
//            params.put("refResource", param.getRefResourceIdList());
//            params.put("createUserId", task.getCreateUserId());
//            this.updateTaskRefResource(params);
//        }
//
//        final User user = userService.getById(task.getModifyUserId());
//        if (user != null) {
//            taskCatalogueVO.setCreateUser(user.getUserName());
//        }
//        final List<Task> dependencyTasks = param.getDependencyTasks();
//        if (dependencyTasks != null) {
//            this.batchTaskTaskService.addOrUpdateTaskTask(task.getId(), dependencyTasks);
//            taskCatalogueVO.setDependencyTasks(dependencyTasks);
//        }
//
//        String createUserName = userService.getUserName(task.getCreateUserId());
//        taskCatalogueVO.setCreateUser(createUserName);
//        taskCatalogueVO.setCatalogueType(CatalogueType.TASK_DEVELOP.getType());
//
//        return taskCatalogueVO;
//    }

    /**
     * 密码回填检查方法
     **/
    private void checkFillPassword(final TaskResourceParam param) {
        // 单独对同步任务中密码进行补全处理 将未变更的 ****** 填充为原密码信息 --2019/10/25 茂茂--
        if (param.getId() > 0 && EScheduleJobType.SYNC.getVal().equals(param.getTaskType())) {
            final String context = param.getSqlText();
            if (null == context) {
                return;
            }
            //1、检查上送字段是否存在需要处理的密码，不存在直接跳过
            final Pattern pattern = Pattern.compile(PatternConstant.PASSWORD_FIELD_REGEX, Pattern.CASE_INSENSITIVE);
            final Matcher matcher = pattern.matcher(context);
            if (matcher.find()) {
                LOGGER.info("当前上送信息存在隐藏密码字段，准备执行旧密码回填操作");
                //2、查询旧数据信息，保存成结构数据，待数据解析补充
                final Task task = this.developTaskDao.getOne(param.getId());
                if (Objects.nonNull(task)) {
                    final String sqlText = task.getSqlText();
                    if (StringUtils.isNotEmpty(sqlText)) {
                        final JSONObject oldData = JSON.parseObject(Base64Util.baseDecode(sqlText));
                        //3、处理新上送的数据，替换未变更的密码信息
                        final JSONObject newData = JSON.parseObject(context);
                        //值并行处理 -- 固定接口直接写死job的值处理密码问题
                        this.fillPassword(newData, oldData.getJSONObject("job"));
                        param.setSqlText(newData.toJSONString());
                    }
                }
            }
        }
    }

    /**
     * 填充密文密码信息
     */
    private void fillPassword(final Object newData, final Object oldData) {
        if (null == newData || null == oldData) {
            return;
        }
        if (newData instanceof JSONObject && oldData instanceof JSONObject) {
            final Set<Map.Entry<String, Object>> entrySet = ((JSONObject) newData).entrySet();
            for (final Map.Entry<String, Object> entry : entrySet) {
                final String key = entry.getKey();
                final Object value = entry.getValue();
                final Object oldValue = ((JSONObject) oldData).get(key);
                if (StringUtils.isBlank(key) || null == value || null == oldValue) {
                    continue;
                }
                if (DataFilter.PASSWORD_KEYS.contains(key.toLowerCase())
                        && "******".equals(value)) {
                    entry.setValue(oldValue);
                } else {
                    this.fillPassword(value, oldValue);
                }
            }
        } else if (newData instanceof JSONArray && oldData instanceof JSONArray) {
            final JSONArray newArr = (JSONArray) newData;
            final JSONArray oldArr = (JSONArray) oldData;
            for (int i = 0; i < newArr.size(); i++) {
                if (oldArr.size() > i) {
                    this.fillPassword(newArr.get(i), oldArr.get(i));
                }
            }
        }
    }

    /**
     * 任务保存之前的一些参数校验并返回engineType
     *
     * @param param
     * @return
     */
    private void checkBeforeUpdateTask(TaskResourceParam param) {
        if (EScheduleJobType.SYNC.getVal().equals(param.getTaskType())) {
            operateSyncTask(param);
            return;
        }
        if (CollectionUtils.isNotEmpty(param.getResourceIdList())) {
            throw new RdosDefineException("该任务不能添加资源.", ErrorCode.INVALID_PARAMETERS);
        }
    }

    /**
     * 处理数据同步任务
     * @param param
     * @return
     */
    private void operateSyncTask(TaskResourceParam param) {
        Map<String, Object> sourceMap = param.getSourceMap();
        Map<String, Object> settingMap = param.getSettingMap();
        //下面代码 是为了 拿到断点续传在字段列表的第几位
        if (sourceMap != null && settingMap != null) {
            Object column = sourceMap.get("column");
            Integer restoreColumnIndex = 0;
            if (column != null) {
                JSONArray colums = JSONArray.parseArray(JSONObject.toJSONString(sourceMap.get("column")));
                for (int i = 0; i < colums.size(); i++) {
                    if (Objects.equals(colums.getJSONObject(i).getString("key"), settingMap.get("restoreColumnName"))) {
                        restoreColumnIndex = i;
                        break;
                    }
                }
            }
            settingMap.put("restoreColumnIndex", restoreColumnIndex);
            param.setSettingMap(settingMap);
        }
        LOGGER.info("addOrUpdateTask with createModel {}", param.getCreateModel());

        if (param.getEditBaseInfo()) {
            // 右键编辑 处理增量标识
            operateIncreCol(param);
        } else {
            JSONObject sql = new JSONObject();
            if (param.getCreateModel() == TaskCreateModelType.TEMPLATE.getType()) {
                sql.put("job", param.getSqlText());
                this.batchTaskParamService.checkParams(sql.toJSONString(), param.getTaskVariables());
            } else if ((param.isPreSave() || param.getId() == 0) && param.getCreateModel() == TaskCreateModelType.GUIDE.getType()) {
                if (param.getId() != 0) {
                    String sqlText = this.dataSourceService.getSyncSql(param, false);
                    sql = JSON.parseObject(sqlText);
                }
            }
            sql.put("createModel", param.getCreateModel());
            sql.put("syncModel", param.getSyncModel());
            param.setSqlText(sql.toJSONString());
        }
        if (param.getSqlText() != null) {
            this.checkIncreSyncTask(param);
            param.setSqlText(Base64Util.baseEncode(param.getSqlText()));
        }
    }


    /**
     * 处理增量标识  主要处理两部分 1 处理增量标识字段  2.处理调度依赖
     * @param param
     */
    private void operateIncreCol(TaskResourceParam param) {
        final Task task = this.developTaskDao.getOne(param.getId());
        if (StringUtils.isNotEmpty(task.getSqlText())) {
            final JSONObject json = JSON.parseObject(Base64Util.baseDecode(task.getSqlText()));
            json.put("syncModel", param.getSyncModel());
            //处理增量标示
            operateIncreamColumn(json,param.getSyncModel());
            param.setSqlText(json.toJSONString());
        }

        JSONObject scheduleConf = JSON.parseObject(task.getScheduleConf());
        Integer selfReliance = scheduleConf.getInteger("selfReliance");
        if (param.getSyncModel() == SyncModel.HAS_INCRE_COL.getModel() &&
                !DependencyType.SELF_DEPENDENCY_SUCCESS.getType().equals(selfReliance)
                && !DependencyType.SELF_DEPENDENCY_END.getType().equals(selfReliance)) {
            scheduleConf.put("selfReliance", DependencyType.SELF_DEPENDENCY_END.getType());
            param.setScheduleConf(scheduleConf.toJSONString());
        }
    }

    /**
     * 向导模式转模版
     * @param param
     * @return
     * @throws Exception
     */
    @Transactional
    public TaskCatalogueVO guideToTemplate(final TaskResourceParam param) {
        final Task task = this.developTaskDao.getOne(param.getId());
        TaskVO taskVO = new TaskVO();
        taskVO.setId(param.getId());
        taskVO.setName(task.getName());
        taskVO.setVersion(param.getVersion());
        taskVO.setCreateUserId(param.getUserId());
        taskVO.setNodePid(task.getNodePid());
//        taskVO.setReadWriteLockVO(param.getReadWriteLockVO());
//        taskVO.setLockVersion(param.getLockVersion());
        taskVO.setTenantId(param.getTenantId());
        final JSONObject sqlJson = JSON.parseObject(Base64Util.baseDecode(task.getSqlText()));
        sqlJson.put("createModel", TaskCreateModelType.TEMPLATE.getType());

        taskVO.setSqlText(Base64Util.baseEncode(sqlJson.toJSONString()));
        this.updateTask(taskVO, true);
        final TaskCatalogueVO taskCatalogueVO = new TaskCatalogueVO(param, taskVO.getNodePid());
        return taskCatalogueVO;
    }

    /**
     * 判断任务是否可以配置增量标识
     */
    public boolean canSetIncreConf(Long taskId) {
        final Task task = this.getBatchTaskById(taskId);
        if (task == null) {
            throw new RdosDefineException(ErrorCode.DATA_NOT_FIND);
        }

        if (!EScheduleJobType.SYNC.getVal().equals(task.getTaskType())) {
            return false;
        }

        // 增量同步任务不能在工作流中运行
        if (task.getFlowId() != 0) {
            return false;
        }

        if (StringUtils.isEmpty(task.getSqlText())) {
            throw new RdosDefineException("同步任务未配置数据源");
        }

        try {
            final JSONObject json = JSON.parseObject(Base64Util.baseDecode(task.getSqlText()));
            this.checkSyncJobContent(json.getJSONObject("job"), false);
        } catch (final RdosDefineException e) {
            return false;
        }

        return true;
    }

    /**
     * 检查增量同步任务配置
     *
     * @param param
     */
    private void checkIncreSyncTask(final TaskResourceParam param) {
        final JSONObject taskJson = JSON.parseObject(param.getSqlText());
        if (!taskJson.containsKey("syncModel") || SyncModel.NO_INCRE_COL.getModel() == taskJson.getInteger("syncModel")) {
            return;
        }

        if (param.getFlowId() != 0) {
            throw new RdosDefineException("增量同步任务不能在工作流中运行", ErrorCode.INVALID_PARAMETERS);
        }

        this.checkSyncJobContent(taskJson.getJSONObject("job"), true);
    }

    public void checkSyncJobContent(final JSONObject jobJson, final boolean checkIncreCol) {
        if (jobJson == null) {
            return;
        }

        String readerPlugin = JSONPath.eval(jobJson, "$.job.content[0].reader.name").toString();
        String writerPlugin = JSONPath.eval(jobJson, "$.job.content[0].writer.name").toString();

        if (!PluginName.RDB_READER.contains(readerPlugin)) {
            throw new RdosDefineException("增量同步任务只支持从关系型数据库读取", ErrorCode.INVALID_PARAMETERS);
        }

        if (!PluginName.HDFS_W.equals(writerPlugin)) {
            throw new RdosDefineException("增量同步任务只支持写入hive和hdfs", ErrorCode.INVALID_PARAMETERS);
        }

        if (!checkIncreCol) {
            return;
        }

        String increColumn = (String) JSONPath.eval(jobJson, "$.job.content[0].reader.parameter.increColumn");
        if (StringUtils.isEmpty(increColumn)) {
            throw new RdosDefineException("增量同步任务必须配置增量字段", ErrorCode.INVALID_PARAMETERS);
        }
    }

    private String createAnnotationText(final TaskVO task) {
        if (StringUtils.isNotBlank(task.getSqlText())) {
            return task.getSqlText();
        }
        final String ENTER = "\n";
        final String NOTE_SIGN;
        String type = EScheduleJobType.getByTaskType(task.getTaskType()).getName();
        final StringBuilder sb = new StringBuilder();

        // 需要代码注释模版的任务类型
        Set<Integer> shouldNoteSqlTypes = Sets.newHashSet(EScheduleJobType.SPARK_SQL.getVal());

        if (shouldNoteSqlTypes.contains(task.getTaskType())) {
            NOTE_SIGN = "-- ";
        } else {
            sb.append(StringUtils.isBlank(task.getSqlText()) ? "" : task.getSqlText());
            return sb.toString();
        }
        //包括任务名称、任务类型、作者、创建时间、描述；
        sb.append(NOTE_SIGN).append("name ").append(task.getName()).append(ENTER);
        sb.append(NOTE_SIGN).append("type ").append(type).append(ENTER);
        sb.append(NOTE_SIGN).append("author ").append(userService.getUserName(task.getCreateUserId())).append(ENTER);
        final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sb.append(NOTE_SIGN).append("create time ").append(sdf.format(task.getGmtCreate())).append(ENTER);
        sb.append(NOTE_SIGN).append("desc ").append(StringUtils.isBlank(task.getTaskDesc()) ? "" : task.getTaskDesc().replace(ENTER, " ")).append(ENTER);
        sb.append(StringUtils.isBlank(task.getSqlText()) ? "" : task.getSqlText());

        return sb.toString();
    }


    /**
     * 解析子任务依赖关系
     *
     * @param sqlText
     * @return key-taskId, value-parentIdList
     */
    public Map<Long, List<Long>> parseTaskRelationsFromSqlText(final String sqlText) {
        if (StringUtils.isNotBlank(sqlText)) {
            final JSONArray array = JSON.parseArray(sqlText);
            final Map<Long, List<Long>> relations = Maps.newHashMap();
            for (int i = 0; i < array.size(); i++) {
                final JSONObject object = array.getJSONObject(i);
                final JSONObject source = object.getJSONObject("source");
                final JSONObject target = object.getJSONObject("target");
                if (source != null && target != null) {
                    final long parentId = source.getJSONObject("data").getLong("id");
                    final long targetId = target.getJSONObject("data").getLong("id");
                    if (relations.containsKey(targetId)) {
                        relations.get(targetId).add(parentId);
                    } else {
                        relations.put(targetId, Lists.newArrayList(parentId));
                    }
                } else if(object.getJSONObject("data") != null) {
                    if (!relations.containsKey(object.getJSONObject("data").getLong("id"))) {
                        relations.put(object.getJSONObject("data").getLong("id"), Lists.newArrayList());
                    }
                }
            }
            return relations;
        } else {
            throw new RdosDefineException("该工作流不存在子任务");
        }
    }

//
//    /**
//     * 新增/更新任务
//     *
//     * @param task
//     * @param isEditBaseInfo 如果是右键编辑的情况则不更新任务参数
//     * @return
//     */
//    @Transactional
//    public TaskVO updateTask(final TaskVO taskVO, final Boolean isEditBaseInfo) {
//
//        if (taskVO.getName() == null) {
//            throw new RdosDefineException("任务名称不能为空.", ErrorCode.INVALID_PARAMETERS);
//        }
//
//        if (!PublicUtil.matcher(taskVO.getName(), TASK_PATTERN)) {
//            throw new RdosDefineException("名称只能由字母、数据、中文、下划线组成", ErrorCode.INVALID_PARAMETERS);
//        }
//
//
//        taskVO.setGmtModified(Timestamp.valueOf(LocalDateTime.now()));
//        Task task = this.developTaskDao.getByName(taskVO.getName(), taskVO.getTenantId());
//
//        boolean isAdd = false;
//        if (task.getId() > 0) {//update
//            Task specialTask = getOneWithError(taskVO.getId());
//            if (task.getTaskType() == null) {
//                task.setTaskType(specialTask.getTaskType());
//            }
//            String oriTaskName = specialTask.getName();
//            if (task != null && !task.getId().equals(task.getId())) {
//                throw new RdosDefineException(ErrorCode.NAME_ALREADY_EXIST);
//            }
//
//            //如果是工作流任务 更新父节点调度类型时，需要同样更新子节点
//            if (EScheduleJobType.WORK_FLOW.getVal().equals(task.getTaskType()) && task.getFlowId() == 0 && StringUtils.isNotEmpty(task.getScheduleConf())){
//                updateSonTaskPeriodType(task.getId(),task.getPeriodType(),task.getScheduleConf());
//            }
//            if (!oriTaskName.equals(task.getName())) {//修改名字需要同步到taskShade
//                this.taskService.updateTaskName(task.getId(), task.getName());
//            }
//            LOGGER.info("success update task, taskId:{}", task.getId());
//
//        } else {
//            if (task != null) {
//                throw new RdosDefineException(ErrorCode.NAME_ALREADY_EXIST);
//            }
//            //初始化task的一些属性
//            isAdd = initTaskInfo(taskVO);
//            Task insertTask = new Task();
//            BeanUtils.copyProperties(task, insertTask);
//            //如果是工作流获取父任务的锁 用来保证父任务一定会更新成功 这里有并发问题 如果同时对一个工作流添加子任务 会丢失
//            if (task.getFlowId()>0){
//                Task parentTask = developTaskDao.getOne(task.getFlowId());
//                BatchReadWriteLock readWriteLock = developReadWriteLockDao.getByTenantIdAndRelationIdAndType(0L, parentTask.getId(), ReadWriteLockType.BATCH_TASK.name());
//                if (readWriteLock == null) {
//                    throw new RdosDefineException("父任务锁不存在");
//                }
////                if (!readWriteLock.getVersion().equals(task.getParentReadWriteLockVersion())) {
////                    throw new RdosDefineException("当前任务已被修改，请重新打开任务后再次提交");
////                }
//            }
//            developTaskDao.insert(insertTask);
////            task.set(insertTask.getId());
//            task.setId(insertTask.getId());
//
////            parseCreateTaskExeArgs(task);
////
////            //新增锁
////            ReadWriteLockVO readWriteLockVO = this.readWriteLockService.getLock(
////                    task.getTenantId(),
////                    task.getUserId(),
////                    ReadWriteLockType.BATCH_TASK.name(),
////                    task.getId(),
////                     null);
////            task.setReadWriteLockVO(readWriteLockVO);
//            LOGGER.info("success insert task, taskId:{}", task.getId());
//        }
//
//        // 右键编辑时会调用另一个接口
//        if (BooleanUtils.isNotTrue(isEditBaseInfo)) {
//            if (!EScheduleJobType.WORK_FLOW.getVal().equals(task.getTaskType())  &&
//                    !EScheduleJobType.VIRTUAL.getVal().equals(task.getTaskType())) {
//                //新增加不校验自定义参数
//                if (!isAdd) {
//                    this.batchTaskParamService.checkParams(task.getSqlText(), taskVO.getTaskVariables());
//                }
//            }
//            this.batchTaskParamService.addOrUpdateTaskParam(task);
//        }
//
//        final BatchTaskBatchVO batchTaskBatchVO = new BatchTaskBatchVO(task);
////        batchTaskBatchVO.setReadWriteLockVO(task.getReadWriteLockVO());
//        batchTaskBatchVO.setVersion(task.getVersion());
//        return batchTaskBatchVO;
//    }

    /**
     *初始化 task的一些基本属性
     *
     * @param task
     * @return
     */
    private boolean initTaskInfo(TaskVO task) {
        if (StringUtils.isBlank(task.getTaskDesc())) {
            task.setTaskDesc("");
        }
        if (StringUtils.isBlank(task.getTaskParams())) {
            task.setTaskParams("");
        }
//
//        if (StringUtils.isBlank(task.getMainClass())) {
//            task.setMainClass("");
//        }

        if (StringUtils.isBlank(task.getScheduleConf())) {
            task.setScheduleConf(DEFAULT_SCHEDULE_CONF);
        } else {
            final JSONObject scheduleConf = JSON.parseObject(task.getScheduleConf());
            final String beginDate = scheduleConf.getString("beginDate");
            if (StringUtils.isBlank(beginDate) || "null".equalsIgnoreCase(beginDate)) {
                throw new RdosDefineException("生效日期起至时间不能为空");
            }
            final String endDate = scheduleConf.getString("endDate");
            if (StringUtils.isBlank(endDate) || "null".equalsIgnoreCase(endDate)) {
                throw new RdosDefineException("生效日期结束时间不能为空");
            }
        }

        if (task.getVersion() == null) {
            task.setVersion(0);
        }

        if (task.getCreateUserId() == null) {
            task.setCreateUserId(task.getCreateUserId());
        }
        task.setGmtCreate(task.getGmtModified());
        // 增加注释
        task.setSqlText(this.createAnnotationText(task));
        task.setSubmitStatus(ESubmitStatus.UNSUBMIT.getStatus());
        task.setTaskParams(getDefaultTaskParam(task.getTenantId(), task.getTaskType()));
        task.setScheduleStatus(EScheduleStatus.NORMAL.getVal());
        task.setPeriodType(DEFAULT_SCHEDULE_PERIOD);
        String scConf = DEFAULT_SCHEDULE_CONF;
        int period = DEFAULT_SCHEDULE_PERIOD;
        if (task.getFlowId() != null && task.getFlowId() > 0) {
            final Task flow = this.developTaskDao.getOne(task.getFlowId());
            if (flow != null) {
                scConf = flow.getScheduleConf();
                final ScheduleCron scheduleCron;
                try {
                    scheduleCron = ScheduleFactory.parseFromJson(scConf);
                } catch (Exception e) {
                    throw new RdosDefineException(e.getMessage(), e);
                }
                period = scheduleCron.getPeriodType();
            }
            task.setScheduleConf(scConf);
        }
        task.setPeriodType(period);
        if(Objects.isNull(task.getFlowId())){
            task.setFlowId(0L);
        }
        return true;
    }

    private Integer getLockVersion(final BatchTaskBatchVO task) {
        Integer lockVersion;
        //仅更新名字时readWriteLock可能为空
        if (task.getReadWriteLockVO() == null) {
            BatchReadWriteLock lock = readWriteLockService.getReadWriteLock(0L, task.getId(), ReadWriteLockType.BATCH_TASK.name());
            if (lock.getModifyUserId().equals(task.getUserId())) {
                lockVersion = lock.getVersion();
            } else {
                lockVersion = INIT_LOCK_VERSION;
            }
        } else {
            lockVersion = task.getReadWriteLockVO().getVersion();
        }
        return lockVersion;
    }
    /**
     * 向导模式下的需要json格式化
     *
     * @param createModel
     * @param obj
     */
    private String formatSqlText(Integer createModel, JSONObject obj) {
        if (obj == null) {
            return "";
        }
        if (obj.get("job") != null && CREATE_MODEL_GUIDE ==createModel) {
            return JSON.toJSONString(JSONObject.parseObject(DataFilter.passwordFilter(obj.get("job").toString())), SerializerFeature.PrettyFormat);
        } else if (obj.get("job") != null && CREATE_MODEL_TEMPLATE ==createModel) {
            return String.valueOf(obj.get("job"));
        } else {
            return "";
        }
    }

    /**
     * 向导模式下的数据同步需要json格式化
     *
     * @param taskVO
     * @param obj
     */
    private void formatSqlText(final ScheduleTaskVO taskVO, final JSONObject obj) {
        taskVO.setCreateModel(obj.get("createModel") == null ? CREATE_MODEL_GUIDE : Integer.parseInt(String.valueOf(obj.get("createModel"))));
        if (obj.get("job") != null && CREATE_MODEL_GUIDE == taskVO.getCreateModel()) {
            final Map<String, String> map;
            final String sqlText;
            try {
                map = (Map<String, String>) objectMapper.readValue(String.valueOf(obj.get("job")), Object.class);
                sqlText = JsonUtils.formatJSON(map);
            } catch (final IOException e) {
                throw new RdosDefineException("sqlText的json格式化失败", e);
            }
            taskVO.setSqlText(sqlText);
        } else if (obj.get("job") != null && CREATE_MODEL_TEMPLATE == taskVO.getCreateModel()) {
            taskVO.setSqlText(String.valueOf(obj.get("job")));
        } else {
            taskVO.setSqlText("");
        }

        if (obj.get("syncModel") != null) {
            taskVO.setSyncModel(obj.getInteger("syncModel"));
            if (taskVO.getSyncModel() == SyncModel.HAS_INCRE_COL.getModel()) {
                final Object increCol = JSONPath.eval(obj.getJSONObject("parser"), "$.sourceMap.increColumn");
                if (increCol != null) {
                    taskVO.setIncreColumn(increCol.toString());
                }
            }
        }
    }

    /**
     * 更新任务主资源
     *
     * @param taskResourceMap
     * @return
     */
    @Transactional
    public void updateTaskResource(final Map<String, Object> taskResourceMap) {

        Preconditions.checkState(taskResourceMap.containsKey("id"), "need param of id");
        Preconditions.checkState(taskResourceMap.containsKey("resources"), "need param of resources");
        Preconditions.checkState(taskResourceMap.containsKey("tenantId"), "need param of tenantId");
        Preconditions.checkState(taskResourceMap.containsKey("createUserId"), "need param of createUserId");

        final Long id = MathUtil.getLongVal(taskResourceMap.get("id"));
        final List<Object> oriResourceList = (List<Object>) taskResourceMap.get("resources");

        final Task task = this.developTaskDao.getOne(id);
        Preconditions.checkNotNull(task, "can not find task by id " + id);

        //删除旧的资源
        batchTaskResourceService.deleteByTaskId(task.getId(), ResourceRefType.MAIN_RES.getType());

        //添加新的资源
        if (CollectionUtils.isNotEmpty(oriResourceList)) {
            List<Long> resourceIdList = Lists.newArrayList();
            oriResourceList.forEach(tmpId -> resourceIdList.add(MathUtil.getLongVal(tmpId)));
            batchTaskResourceService.save(task, resourceIdList, ResourceRefType.MAIN_RES.getType());
        }

    }

    /**
     * 更新任务引用资源
     *
     * @param taskResourceMap
     * @return
     */
    @Transactional
    public void updateTaskRefResource(final Map<String, Object> taskResourceMap) {

        Preconditions.checkState(taskResourceMap.containsKey("id"), "need param of id");
        Preconditions.checkState(taskResourceMap.containsKey("tenantId"), "need param of tenantId");
        Preconditions.checkState(taskResourceMap.containsKey("createUserId"), "need param of createUserId");

        final Long id = MathUtil.getLongVal(taskResourceMap.get("id"));
        final List<Object> refResourceList = (List<Object>) taskResourceMap.get("refResource");

        final Task task = getOneWithError(id);

        //删除旧的资源
        batchTaskResourceService.deleteByTaskId(task.getId(), ResourceRefType.DEPENDENCY_RES.getType());

        //添加新的关联资源
        if (CollectionUtils.isNotEmpty(refResourceList)) {
            final List<Long> refResourceIdList = Lists.newArrayList();
            refResourceList.forEach(tmpId -> refResourceIdList.add(MathUtil.getLongVal(tmpId)));
            this.batchTaskResourceService.save(task, refResourceIdList, ResourceRefType.DEPENDENCY_RES.getType());
        }
    }

    /**
     * 获取任务的默认参数
     *
     * @param tenantId 租户ID
     * @param taskType 任务类型
     * @return
     */
    private String getDefaultTaskParam(Long tenantId, Integer taskType) {
        EScheduleJobType eScheduleJobType = EScheduleJobType.getByTaskType(taskType);
        List<Component> componentList = componentService.listComponentsByComponentType(tenantId, eScheduleJobType.getComponentType().getTypeCode());
        if (CollectionUtils.isEmpty(componentList)){
            return Strings.EMPTY_STRING;
        }
        // todo 后续多版本再进行扩展
        String version = componentList.get(0).getVersionName();
        TaskParamTemplate taskParamTemplate = taskParamTemplateService.getTaskParamTemplate(version, taskType);
        return Objects.isNull(taskParamTemplate) ? Strings.EMPTY_STRING : taskParamTemplate.getParams();
    }

    /**
     * 数据开发-删除任务
     *
     * @param taskId    任务id
     * @param tenantId 项目id
     * @param userId    用户id
     * @return
     * @author toutian
     */
    @Transactional
    public Long deleteTask(Long taskId, Long tenantId, Long userId, String sqlText) {

        final Task task = this.developTaskDao.getOne(taskId);
        if (task == null) {
            throw new RdosDefineException(ErrorCode.CAN_NOT_FIND_TASK);
        }
        // 判断该任务是否有子任务(调用engine接口) 工作流不需要判断
        if (task.getFlowId() == 0) {
            List<TaskGetNotDeleteVO> notDeleteTaskVOS = getChildTasks(taskId);
            if (CollectionUtils.isNotEmpty(notDeleteTaskVOS)) {
                throw new RdosDefineException("(当前任务被其他任务依赖)", ErrorCode.CAN_NOT_DELETE_TASK);
            }
        }

        final ScheduleTaskShade dbTask = this.taskService.findTaskByTaskId(taskId);
        if (task.getFlowId() == 0 && Objects.nonNull(dbTask) &&
        task.getScheduleStatus().intValue() == EScheduleStatus.NORMAL.getVal().intValue()){
            throw new RdosDefineException("(当前任务未被冻结)", ErrorCode.CAN_NOT_DELETE_TASK);
        }

        if (task.getTaskType().intValue() == EScheduleJobType.WORK_FLOW.getVal()) {
            final List<Task> batchTasks = this.getFlowWorkSubTasks(taskId);
            //删除所有子任务相关
            batchTasks.forEach(task1 -> this.deleteTaskInfos(task1.getId(), tenantId, userId));
        }

        //删除工作流中的子任务同时删除被依赖的关系
        if (task.getFlowId() > 0) {
            this.batchTaskTaskService.deleteTaskTaskByParentId(task.getId());
        }

        if (StringUtils.isNotBlank(sqlText)) {
            final Task batchTaskBean=new Task();
            batchTaskBean.setId(task.getFlowId());
            batchTaskBean.setSqlText(sqlText);
            this.developTaskDao.updateSqlText(batchTaskBean);
            LOGGER.info("sqlText 修改成功");
        } else {
            LOGGER.error("deleteTask sqlText is null");
        }
        //删除任务
        this.deleteTaskInfos(taskId, tenantId, userId);

        return taskId;
    }

    public void deleteTaskInfos(Long taskId, Long tenantId, Long userId) {
        //软删除任务记录
        this.developTaskDao.deleteById(taskId, Timestamp.valueOf(LocalDateTime.now()), tenantId, userId);
        //删除任务的依赖关系
        this.batchTaskTaskService.deleteTaskTaskByTaskId(taskId);
        //删除关联的函数资源
        this.batchTaskResourceService.deleteTaskResource(taskId);
        this.batchTaskResourceShadeService.deleteByTaskId(taskId);
        //删除关联的参数表信息
        this.batchTaskParamService.deleteTaskParam(taskId);
        //删除发布相关的数据
        this.taskService.deleteTask(taskId, userId);
    }


    public Task getBatchTaskById(final long taskId) {
        return this.developTaskDao.getOne(taskId);
    }


    public List<Task> getTaskByIds(final List<Long> taskIdArray) {
        if (CollectionUtils.isEmpty(taskIdArray)) {
            return ListUtils.EMPTY_LIST;
        }
        return this.developTaskDao.listByIds(taskIdArray);
    }


    /**
     * 判断任务是否可以发布
     * 当前只对sql任务做判断--不允许提交空的sql任务
     *
     * @return
     */
    private boolean checkTaskCanSubmit(final Task task) {
        if (task.getTaskType().equals(EScheduleJobType.SPARK_SQL.getVal()) && StringUtils.isEmpty(task.getSqlText())) {
            throw new RdosDefineException(task.getName() + "任务的SQL为空", ErrorCode.TASK_CAN_NOT_SUBMIT);
        } else if (task.getTaskType().equals(EScheduleJobType.SYNC.getVal())) {
            if (StringUtils.isBlank(task.getSqlText())) {
                throw new RdosDefineException(task.getName() + "任务配置信息为空", ErrorCode.TASK_CAN_NOT_SUBMIT);
            }
            final String sqlText = Base64Util.baseDecode(task.getSqlText());
            final JSONObject jsonObject = JSON.parseObject(sqlText);
            if (jsonObject.containsKey("parser")) {
                final JSONObject parser = jsonObject.getJSONObject("parser");
                if (parser.containsKey("targetMap")) {
                    dataSourceService.checkConnectionById(parser.getJSONObject("targetMap").getLong("sourceId"));
                }
                if (parser.containsKey("sourceMap")) {
                    final JSONObject sourceMap = parser.getJSONObject("sourceMap");
                    if (sourceMap.containsKey("sourceList")) {
                        final JSONArray sourceList = sourceMap.getJSONArray("sourceList");
                        for (final Object o : sourceList) {
                            final JSONObject source = (JSONObject) o;
                            dataSourceService.checkConnectionById(source.getLong("sourceId"));
                        }
                    } else {
                        dataSourceService.checkConnectionById(parser.getJSONObject("sourceMap").getLong("sourceId"));
                    }
                }
            }
        }
        return true;
    }


//    public TaskCatalogueVO forceUpdate(final TaskResourceParam param) {
//        return addOrUpdateTask(param);
//    }

    /**
     * 数据开发-获取所有系统参数
     */
    public Collection<BatchSysParameter> getSysParams() {
        return this.batchSysParamService.listSystemParam();
    }


    /**
     * 新增离线任务/脚本/资源/自定义脚本，校验名称
     *
     * @param name
     * @param type
     * @param pid
     * @param isFile
     * @param tenantId
     */
    public void checkName(String name, String type, Integer pid, Integer isFile, Long tenantId) {
        if (StringUtils.isBlank(name)) {
            return;
        }
        if (!isFile.equals(IS_FILE)) {
            BatchCatalogue batchCatalogue = batchCatalogueService.getByPidAndName(tenantId, pid.longValue(), name);
            if (batchCatalogue != null) {
                throw new RdosDefineException("文件夹已存在", ErrorCode.NAME_ALREADY_EXIST);
            }
        } else {
            final Object obj;
            if (type.equals(CatalogueType.TASK_DEVELOP.name())) {
                obj = this.developTaskDao.getByName(name, tenantId);
            } else if (type.equals(CatalogueType.RESOURCE_MANAGER.name())) {
                obj = batchResourceService.listByNameAndTenantId(tenantId, name);
            } else if (type.equals(CatalogueType.CUSTOM_FUNCTION.name())) {
                obj = batchFunctionService.listByNameAndTenantId(tenantId, name, FuncType.CUSTOM.getType());
            } else if (type.equals(CatalogueType.PROCEDURE_FUNCTION.name())) {
                obj = batchFunctionService.listByNameAndTenantId(tenantId, name, FuncType.PROCEDURE.getType());
            } else if (type.equals(CatalogueType.SYSTEM_FUNCTION.name())) {
                throw new RdosDefineException("不能添加系统函数");
            } else {
                throw new RdosDefineException(ErrorCode.INVALID_PARAMETERS);
            }

            if (obj instanceof Task) {
                if (obj != null) {
                    throw new RdosDefineException(ErrorCode.NAME_ALREADY_EXIST);
                }
            } else if (obj instanceof List) {
                if (CollectionUtils.isNotEmpty((List) obj)) {
                    throw new RdosDefineException(ErrorCode.NAME_ALREADY_EXIST);
                }
            }
        }
    }

    /**
     * 获取任务流下的所有子任务
     *
     * @param taskId
     * @return
     */
    public List<Task> getFlowWorkSubTasks(final Long taskId) {
        BatchTaskDTO batchTaskDTO = new BatchTaskDTO();
        batchTaskDTO.setIsDeleted(Deleted.NORMAL.getStatus());
        batchTaskDTO.setFlowId(taskId);
        PageQuery<BatchTaskDTO> pageQuery = new PageQuery<>(batchTaskDTO);
        List<Task> batchTasks = this.developTaskDao.generalQuery(pageQuery);
        return batchTasks;
    }

    public Task getByName(String name, Long tenantId) {
        return this.developTaskDao.getByName(name, tenantId);
    }

    private Integer updateSubmitStatus(final Long tenantId, final Long taskId, final Integer submitStatus) {
        return this.developTaskDao.updateSubmitStatus(tenantId, taskId, submitStatus, Timestamp.valueOf(LocalDateTime.now()));
    }

    /**
     * 根据ID查询信息
     *  如果不存在，则抛异常
     *
     * @param taskId 任务ID
     * @return
     */
    public Task getOneWithError(final Long taskId) {
        Task one = getOne(taskId);
        if (Objects.isNull(one)) {
            throw new RdosDefineException(ErrorCode.CAN_NOT_FIND_TASK);
        }
        return one;
    }

//    /**
//     * 根据ID查询信息
//     *
//     * @param taskId 任务ID
//     * @return
//     */
//    public Task getOne(Long taskId) {
//        return this.developTaskDao.getOne(taskId);
//    }

    /**
     * 根据ID查询信息
     *
     * @param taskId 任务ID
     * @return
     */
    public Task getOne(Long taskId) {
        return developTaskDao.getOne(taskId);
    }

    /**
     *根据父任务Id  更新调度类型  调度配置
     */
    private void updateSonTaskPeriodType(Long flowId,Integer periodType,String scheduleConf){
        JSONObject scheduleJson = JSON.parseObject(scheduleConf);
        scheduleJson.put("selfReliance", 0);
        //工作流配置的自动取消不同步子任务
        scheduleJson.remove("isExpire");
        //为什么不toJsonString 是为了兼容历史数据
        developTaskDao.updateScheduleConf(flowId,periodType,scheduleJson.toString());
    }

    /**
     * 操作增量标示  根据选择 删除或新增
     * @param sqlText  任务的内容
     * @param isIncream
     */
    private void operateIncreamColumn(JSONObject sqlText , Integer isIncream) {
        if (sqlText.containsKey("job")) {
            //获取前端展示的任务内容 忽略额外信息
            JSONObject taskText = sqlText.getJSONObject("job");
            //获取嵌套内容
            JSONObject jobInfo = taskText.getJSONObject("job");
            JSONObject readerParameter = jobInfo.getJSONArray("content").getJSONObject(0).getJSONObject("reader").getJSONObject("parameter");
            if (SyncModel.NO_INCRE_COL.getModel() == isIncream) {
                if (readerParameter.containsKey("increColumn")) {
                    readerParameter.remove("increColumn");
                }
            }
            taskText.put("job", jobInfo);
            sqlText.put("job", taskText);
        }
    }

    /**
     * 根据dependencyTaskIds解析依赖的任务
     * @param dependencyTaskIds
     * @return
     */
    private List<Map<String, Object>> getDependencyTasks(String dependencyTaskIds) {
        try {
            return JSON.parseObject(dependencyTaskIds, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return Arrays.stream(dependencyTaskIds
                    .split(","))
                    .map(taskId -> {
                        Map<String, Object> map = Maps.newHashMap();
                        map.put("parentTaskId", taskId);
                        return map;
                    }).collect(Collectors.toList());
        }
    }

    /**
     * 获取当前任务的下游任务
     * @param taskId
     * @return
     */
    public List<TaskGetNotDeleteVO> getChildTasks(Long taskId) {
        List<ScheduleTaskShade> notDeleteTaskVOs = taskService.listRelyCurrentTask(taskId);
        return notDeleteTaskVOs.stream()
                .map(taskShade ->{
                    TaskGetNotDeleteVO deleteVO = new TaskGetNotDeleteVO();
                    deleteVO.setTaskId(taskShade.getTaskId());
                    deleteVO.setName(taskShade.getName());
                    return deleteVO;
                }).collect(Collectors.toList());
    }

    /**
     * 获取组件版本
     * @param tenantId
     * @param taskType
     * @return
     */
    public List<BatchTaskGetComponentVersionResultVO> getComponentVersionByTaskType(Long tenantId, Integer taskType) {
        List<Component> components = componentService.getComponentVersionByEngineType(tenantId, taskType);
        List<BatchTaskGetComponentVersionResultVO> componentVersionResultVOS = Lists.newArrayList();
        for (Component component : components) {
            BatchTaskGetComponentVersionResultVO resultVO = new BatchTaskGetComponentVersionResultVO();
            resultVO.setComponentVersion(component.getVersionValue());
            resultVO.setDefault(component.getIsDefault());
            componentVersionResultVOS.add(resultVO);
        }
        componentVersionResultVOS.sort(sortComponentVersion());
        return componentVersionResultVOS;
    }

    /**
     * 版本号排序，按照版本号新旧排序
     * @return
     */
    private Comparator<BatchTaskGetComponentVersionResultVO> sortComponentVersion() {
        return (o1, o2) -> {
            String[] version1 = o1.getComponentVersion().split("\\.");
            String[] version2 = o2.getComponentVersion().split("\\.");
            if (version1.length > 0 && version2.length > 0) {
                for (int i = 0; i < version1.length; i++) {
                    try {
                        if (Integer.parseInt(version1[i]) > Integer.parseInt(version2[i])) {
                            return -1;
                        } else if (Integer.parseInt(version1[i]) < Integer.parseInt(version2[i])) {
                            return 1;
                        } else {
                            continue;
                        }
                    } catch (Exception e) {
                        LOGGER.info("hadoop版本号：{}, {}", o1, o2, e);
                    }
                }
            }
            return o2.getComponentVersion().compareTo(o1.getComponentVersion());
        };
    }


    /**
     * 根据 租户、目录id 查询任务列表
     * @param tenantId
     * @param nodePid
     * @return
     */
    public List<Task> listBatchTaskByNodePid(Long tenantId, Long nodePid) {
        return developTaskDao.listBatchTaskByNodePid(tenantId, nodePid);
    }

    /**
     * 根据 租户、目录id 查询任务列表
     * 此方法适合目录信息查询，与上面方法的区别是不返回SqlText等无用的大数据字段
     * @param tenantId
     * @param nodePid
     * @return
     */
    public List<Task> catalogueListBatchTaskByNodePid(Long tenantId, Long nodePid) {
        return developTaskDao.catalogueListBatchTaskByNodePid(tenantId, nodePid);
    }

    public JSONObject trace(final Long taskId) {
        String sqlText = null;
        final Task task = this.getBatchTaskById(taskId);

        if (task == null) {
            throw new RdosDefineException(ErrorCode.CAN_NOT_FIND_TASK);
        } else {
            sqlText = task.getSqlText();
        }

        final String sql = Base64Util.baseDecode(sqlText);
        if (StringUtils.isBlank(sql)) {
            return null;
        }

        final JSONObject sqlJson = JSON.parseObject(sql);
        JSONObject parserJson = sqlJson.getJSONObject("parser");
        if (parserJson != null) {
            parserJson = this.checkTrace(parserJson);
            parserJson.put("sqlText", sqlJson.getString("job"));
            parserJson.put("syncMode", sqlJson.get("syncMode"));
            parserJson.put("taskId", taskId);
        }
        return parserJson;
    }


    private JSONObject checkTrace(final JSONObject jsonObject) {
        final JSONObject keymap = jsonObject.getJSONObject("keymap");
        final JSONArray source = keymap.getJSONArray("source");
        final JSONArray target = keymap.getJSONArray("target");
        final JSONObject sourceMap = jsonObject.getJSONObject("sourceMap");
        final Integer fromId = (Integer) sourceMap.get("sourceId");
        final JSONObject targetMap = jsonObject.getJSONObject("targetMap");
        final Integer toId = (Integer) targetMap.get("sourceId");
        final JSONObject sourceType = sourceMap.getJSONObject("type");
        final List<String> sourceTables = this.getTables(sourceType);
        final JSONObject targetType = targetMap.getJSONObject("type");
        final List<String> targetTables = this.getTables(targetType);
        final BatchDataSource fromDs = dataSourceService.getOne(fromId.longValue());
        final BatchDataSource toDs = dataSourceService.getOne(toId.longValue());

        int fromSourceType = DataSourceType.getSourceType(fromDs.getType()).getVal();
        int toSourceType = DataSourceType.getSourceType(toDs.getType()).getVal();
        if (DataSourceType.HBASE.getVal() == fromSourceType || DataSourceType.HBASE.getVal() == toSourceType) {
            return jsonObject;
        }

        // 处理分库分表的信息
        this.addSourceList(sourceMap);

        if (CollectionUtils.isNotEmpty(sourceTables)) {
            getMetaDataColumns(sourceMap, sourceTables, fromDs);
        }

        if (CollectionUtils.isNotEmpty(targetTables)) {
            getMetaDataColumns(targetMap, targetTables, toDs);
        }
        //因为下面要对keyMap中target中的字段类型进行更新 所以遍历一次目标map 拿出字段和类型的映射
        Map<String,String> newTargetColumnTypeMap = targetMap.getJSONArray(COLUMN)
                .stream().map(column -> (JSONObject)column)
                .collect(Collectors.toMap(column -> column.getString(KEY), column -> column.getString(TYPE)));


        final Collection<BatchSysParameter> sysParams = this.getSysParams();

        final JSONArray newSource = new JSONArray();
        final JSONArray newTarget = new JSONArray();
        for (int i = 0; i < source.size(); ++i) {
            boolean srcTag = true;
            final JSONArray srcColumns = sourceMap.getJSONArray("column");
            if (CollectionUtils.isNotEmpty(sourceTables)) {
                int j = 0;
                final String srcColName;
                String colValue = "";
                if (!(source.get(i) instanceof JSONObject)) {
                    srcColName = source.getString(i);
                } else {
                    //source 可能含有系统变量
                    srcColName = source.getJSONObject(i).getString("key");
                    colValue = source.getJSONObject(i).getString("value");
                }

                //srcColumns 源表中的字段
                for (; j < srcColumns.size(); ++j) {
                    final JSONObject srcColumn = srcColumns.getJSONObject(j);
                    if (srcColumn.getString("key").equals(srcColName)) {
                        break;
                    }
                }
                boolean isSysParam = false;
                for (final BatchSysParameter sysParam : sysParams) {
                    if (sysParam.strIsSysParam(colValue)) {
                        isSysParam = true;
                        break;
                    }
                }
                // 没有系统变量 还需要判断是否有自定义变量
                if(!isSysParam){
                    isSysParam = StringUtils.isNotBlank(colValue);
                }
                //兼容系统变量
                if (isSysParam) {
                    boolean hasThisKey = false;
                    for (int k = 0; k < srcColumns.size(); ++k) {
                        final JSONObject srcColumn = srcColumns.getJSONObject(k);
                        if (srcColumn.getString("key").equals(srcColName)) {
                            hasThisKey = true;
                            break;
                        }

                    }
                    if (!hasThisKey) {
                        //创建出系统变量colume
                        final JSONObject jsonColumn = new JSONObject();
                        jsonColumn.put("key", srcColName);
                        jsonColumn.put("value", colValue);
                        jsonColumn.put("type",source.getJSONObject(i).getString("type"));
                        jsonColumn.put("format",source.getJSONObject(i).getString("format"));
                        srcColumns.add(jsonColumn);
                    }
                }
                if (j == srcColumns.size() && !isSysParam) {
                    srcTag = false;
                }
            }

            boolean destTag = true;
            final JSONArray destColumns = targetMap.getJSONArray("column");
            if (CollectionUtils.isNotEmpty(targetTables)) {
                int k = 0;
                final String destColName;
                if (!(target.get(i) instanceof JSONObject)) {
                    destColName = target.getString(i);
                } else {
                    destColName = target.getJSONObject(i).getString("key");
                    //更新dest表中字段类型
                    final String newType = newTargetColumnTypeMap.get(destColName);
                    if (StringUtils.isNotEmpty(newType)){
                        target.getJSONObject(i).put("type",newType);
                    }
                }
                for (; k < destColumns.size(); ++k) {
                    final JSONObject destColumn = destColumns.getJSONObject(k);
                    if (destColumn.getString("key").equals(destColName)) {
                        break;
                    }
                }

                if (k == destColumns.size()) {
                    destTag = false;
                }
            }

            if (srcTag && destTag) {
                newSource.add(source.get(i));
                newTarget.add(target.get(i));
            }
        }

        keymap.put("source", newSource);
        keymap.put("target", newTarget);

        return jsonObject;
    }


    private List<String> getTables(final Map<String, Object> map) {
        final List<String> tables = new ArrayList<>();
        if (map.get("table") instanceof String) {
            tables.add(map.get("table").toString());
        } else {
            final List<String> tableList = (List<String>) map.get("table");
            if (CollectionUtils.isNotEmpty(tableList)) {
                tables.addAll((List<String>) map.get("table"));
            }
        }

        return tables;
    }

    private void addSourceList(final JSONObject sourceMap) {
        if (sourceMap.containsKey("sourceList")) {
            return;
        }

        if (!DataSourceType.MySQL.getVal().equals(sourceMap.getJSONObject("type").getInteger("type"))) {
            return;
        }

        final JSONArray sourceList = new JSONArray();
        final JSONObject source = new JSONObject();
        source.put("sourceId", sourceMap.get("sourceId"));
        source.put("name", sourceMap.getString("name"));
        source.put("type", sourceMap.getJSONObject("type").getInteger("type"));
        source.put("tables", Arrays.asList(sourceMap.getJSONObject("type").getString("table")));
        sourceList.add(source);

        sourceMap.put("sourceList", sourceList);
    }

    /**
     * 刷新sourceMap中的字段信息
     * 这个方法做了3个事情
     * 1.拿到sourceMap的中的原字段信息
     * 2.拿到对应表的 元数据最新字段信息
     * 3.和原字段信息进行匹配，
     * 如果原字段中的某个字段 不在最新字段中 那就忽略大小写再匹配一次，如果能匹配到就用原字段信息
     * 原因是 Hive执行alter语句增加字段会把源信息所有字段变小写  导致前端映射关系丢失 这里做一下处理
     * @param sourceMap
     * @param sourceTables
     * @param fromDs
     */
    private void getMetaDataColumns(JSONObject sourceMap, List<String> sourceTables, BatchDataSource fromDs) {
        JSONArray srcColumns = new JSONArray();
        List<JSONObject> custColumns = new ArrayList<>();
        List<String> allOldColumnsName = new ArrayList<>();
        Map<String,String> newNameToOldName = new HashMap<>();
        //对原有的字段进行处理 处理方式看方法注释
        getAllTypeColumnsMap(sourceMap, custColumns, allOldColumnsName, newNameToOldName);
        //获取原有字段
        JSONArray sourceColumns = sourceMap.getJSONArray(COLUMN);
        try {
            //获取一下schema
            String schema = sourceMap.getString("schema");
            List<JSONObject> tableColumns = getTableColumnIncludePart(fromDs, sourceTables.get(0), true, schema);
            for (JSONObject tableColumn : tableColumns) {
                String columnName = tableColumn.getString(KEY);
                //获取前端需要的真正的字段名称
                columnName = getRealColumnName(allOldColumnsName, newNameToOldName, columnName);

                String columnType = tableColumn.getString(TYPE);
                JSONObject jsonColumn = new JSONObject();
                jsonColumn.put(KEY, columnName);
                jsonColumn.put(TYPE, columnType);
                if (StringUtils.isNotEmpty(tableColumn.getString("isPart"))) {
                    jsonColumn.put("isPart", tableColumn.get("isPart"));
                }

                if (!(sourceColumns.get(0) instanceof String)) {
                    //这个是兼容原来的desc table 出来的结果 因为desc出来的不仅仅是字段名
                    for (int i = 0; i < sourceColumns.size(); i++) {
                        final JSONObject item = sourceColumns.getJSONObject(i);
                        if (item.get(KEY).equals(columnName) && item.containsKey("format")) {
                            jsonColumn.put("format", item.getString("format"));
                            break;
                        }
                    }
                }
                srcColumns.add(jsonColumn);
            }
        } catch (Exception ignore) {
            LOGGER.error("数据同步获取表字段异常 : {}", ignore.getMessage(), ignore);
            srcColumns = sourceColumns;
        }
        if (CollectionUtils.isNotEmpty(custColumns)) {
            srcColumns.addAll(custColumns);
        }
        sourceMap.put(COLUMN, srcColumns);
    }

    /**
     * 拿到真实的字段名
     * 判断 如果
     * @param allOldColumnsName  原有的所有字段的字段名
     * @param newNameToOldName   key是原有字段名的小写  value是原有字段名
     * @param columnName  元数据字段名
     * @return
     */
    private String getRealColumnName(List<String> allOldColumnsName, Map<String, String> newNameToOldName, String columnName) {
        if (allOldColumnsName.contains(columnName)){
            //认为字段名没有改动 直接返回
            return columnName;
        }

        String oldColumnName = newNameToOldName.get(columnName);
        if (StringUtils.isBlank(oldColumnName)){
            //认为字段名没有从大写变小写
            return columnName;
        }
        //字段名大写变小写了 所以返回原有字段名 保证前端映射无问题
        return oldColumnName;
    }

    /**
     * 这个方法 是对老数据中的字段做一下处理 会出来3个集合
     * @param sourceMap 源信息
     * @param custColumns 用户自定义字段
     * @param allOldColumnsName  老字段名称集合
     * @param newNameToOldName  新字段名称和老字段名字对应集合  key：字段名小写  value 原字段名 用处hive增加字段 字段名全小写导致对应关系丢失
     */
    private void getAllTypeColumnsMap(JSONObject sourceMap,List<JSONObject> custColumns,List<String> allOldColumnsName,Map<String,String> newNameToOldName ){
        JSONArray sourceColumns = sourceMap.getJSONArray(COLUMN);
        if (sourceColumns == null){
            return;
        }
        for (int i = 0; i < sourceColumns.size(); ++i) {
            JSONObject column = sourceColumns.getJSONObject(i);
            if (column.containsKey("value")) {
                custColumns.add(column);
                continue;
            }
            String key = column.getString(KEY);
            if (StringUtils.isBlank(key)){
                continue;
            }
            allOldColumnsName.add(key);
            newNameToOldName.put(key.toLowerCase(),key);
        }

    }

    /**
     * 查询表所属字段 可以选择是否需要分区字段
     * @param source
     * @param tableName
     * @param part 是否需要分区字段
     * @return
     * @throws Exception
     */
    private List<JSONObject> getTableColumnIncludePart(BatchDataSource source, String tableName, Boolean part, String schema)  {
        try {
            if (source == null) {
                throw new RdosDefineException(ErrorCode.CAN_NOT_FIND_DATA_SOURCE);
            }
            if (part ==null){
                part = false;
            }
            JSONObject dataJson = JSONObject.parseObject(source.getDataJson());
            Map<String, Object> kerberosConfig = fillKerberosConfig(source.getId());
            IClient iClient = ClientCache.getClient(source.getType());
            SqlQueryDTO sqlQueryDTO = SqlQueryDTO.builder()
                    .tableName(tableName)
                    .schema(schema)
                    .filterPartitionColumns(part)
                    .build();
            ISourceDTO iSourceDTO = SourceDTOType.getSourceDTO(dataJson, source.getType(), kerberosConfig, Maps.newHashMap());
            List<ColumnMetaDTO> columnMetaData = iClient.getColumnMetaData(iSourceDTO, sqlQueryDTO);
            List<JSONObject> list = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(columnMetaData)) {
                for (ColumnMetaDTO columnMetaDTO : columnMetaData) {
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(columnMetaDTO));
                    jsonObject.put("isPart",columnMetaDTO.getPart());
                    list.add(jsonObject);
                }
            }
            return list;
        } catch (DtCenterDefException e) {
            throw e;
        } catch (Exception e) {
            throw new RdosDefineException(ErrorCode.GET_COLUMN_ERROR, e);
        }

    }

    /**
     * 下载检查kerberos配置
     *
     * @param sourceId
     * @return 返回该数据源的完整kerberos配置
     */
    public Map<String, Object> fillKerberosConfig(Long sourceId) {
        BatchDataSource source = dataSourceService.getOne(sourceId);
        Long tenantId = tenantService.getDtTenantId(source.getTenantId());
        JSONObject dataJson = JSON.parseObject(source.getDataJson());
        JSONObject kerberosConfig = dataJson.getJSONObject(KERBEROS_CONFIG);
        if (MapUtils.isNotEmpty(kerberosConfig)) {
            String localKerberosConf = getLocalKerberosConf(sourceId);
            downloadKerberosFromSftp(kerberosConfig.getString(KERBEROS_DIR), localKerberosConf, tenantId, dataJson.getTimestamp(KERBEROS_FILE_TIMESTAMP));
            return handleKerberos(source.getType(), kerberosConfig, localKerberosConf);
        }
        return new HashMap<>();
    }

    private String getLocalKerberosConf(Long sourceId) {
        String key = getSourceKey(sourceId);
        return environmentContext.getKerberosLocalPath() + File.separator + key;
    }

    private String getSourceKey(Long sourceId) {
        return Optional.ofNullable(sourceId).orElse(0L).toString();
    }

    private void downloadKerberosFromSftp(String kerberosFile, String localKerberosConf, Long tenantId, Timestamp kerberosFileTimestamp) {
        //需要读取配置文件
        Map<String, String> sftpMap =  clusterService.getComponentByTenantId(tenantId,EComponentType.SFTP.getTypeCode(), false,Map.class,null);
        try {
            KerberosConfigVerify.downloadKerberosFromSftp(kerberosFile, localKerberosConf, sftpMap, kerberosFileTimestamp);
        } catch (Exception e) {
            //允许下载失败
            LOGGER.info("download kerberosFile failed {}",kerberosFile, e);
        }
    }


    /**
     * kerberos配置预处理、替换相对路径为绝对路径等操作
     *
     * @param sourceType
     * @param kerberosMap
     * @param localKerberosConf
     * @return
     */
    private Map<String, Object> handleKerberos (Integer sourceType, Map<String, Object> kerberosMap, String localKerberosConf) {
        IKerberos kerberos = ClientCache.getKerberos(sourceType);
        HashMap<String, Object> tmpKerberosConfig = new HashMap<>(kerberosMap);
        try {
            kerberos.prepareKerberosForConnect(tmpKerberosConfig, localKerberosConf);
        } catch (Exception e) {
            throw new RdosDefineException("common-loader中kerberos配置文件处理失败", e);
        }
        return tmpKerberosConfig;
    }

    /**
     * 查找所有产品提交的任务
     *
     * @param searchVO
     * @return
     */
    public List<BatchAllProductGlobalReturnVO> allProductGlobalSearch(AllProductGlobalSearchVO searchVO) {
        Task task = developTaskDao.getOne(searchVO.getTaskId());

        if (task == null) {
            throw new RdosDefineException(ErrorCode.CAN_NOT_FIND_TASK);
        }
        if (task.getTaskType().intValue() == EScheduleJobType.VIRTUAL.getVal().intValue()) {
            throw new RdosDefineException(ErrorCode.VIRTUAL_TASK_UNSUPPORTED_OPERATION);
        }

        // 过滤掉已经依赖的任务
        final List<BatchTaskTask> taskTasks = this.batchTaskTaskService.getByParentTaskId(searchVO.getTaskId());
        final List<Long> excludeIds = new ArrayList<>(taskTasks.size());
        excludeIds.add(searchVO.getTaskId());
        taskTasks.forEach(taskTask -> excludeIds.add(taskTask.getTaskId()));

        List<ScheduleTaskShade> scheduleTaskShadeList = taskService.findTaskByTaskName(searchVO.getTaskName(), searchVO.getSelectTenantId(), searchVO.getUserId());
        List<ScheduleTaskShade> filterTask = scheduleTaskShadeList.stream().filter(scheduleTask -> !excludeIds.contains(scheduleTask.getTaskId())).collect(Collectors.toList());
        Map<Long, Tenant> tenantMap = tenantService.listAllTenant().stream().collect(Collectors.toMap(Tenant::getId, g -> (g)));

        List<BatchAllProductGlobalReturnVO> voList = Lists.newArrayList();
        for (ScheduleTaskShade scheduleTaskShade : filterTask) {
            BatchAllProductGlobalReturnVO vo = new BatchAllProductGlobalReturnVO();
            vo.setTaskId(scheduleTaskShade.getTaskId());
            vo.setTaskName(scheduleTaskShade.getName());

            Tenant tenant = tenantMap.get(scheduleTaskShade.getTenantId());

            if (tenant != null) {
                vo.setTenantId(tenant.getId());
                vo.setTenantName(tenant.getTenantName());
            }

            voList.add(vo);
        }
        return voList;
    }

    /**
     * 冻结任务
     *
     * @param taskId         任务编号
     * @param scheduleStatus 调度状态
     * @param userId         用户ID
     */
    public void frozenTask(Long taskId, Integer scheduleStatus, Long userId) {
        Task task = getOneWithError(taskId);
        EScheduleStatus targetStatus = EScheduleStatus.getStatus(scheduleStatus);
        if (Objects.isNull(targetStatus)) {
            throw new RdosDefineException("任务状态参数非法", ErrorCode.INVALID_PARAMETERS);
        }
        task.setModifyUserId(userId);
        task.setScheduleStatus(scheduleStatus);
        developTaskDao.update(task);
        taskService.frozenTask(Lists.newArrayList(taskId), scheduleStatus);
    }

}