<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <title>开发中心</title>
    <meta charset="utf-8">

    <link rel="stylesheet" href="newcss/easyui.css"/>
    <link rel="stylesheet" href="newcss/color.css"/>
    <link rel="stylesheet" href="newcss/icon.css"/>
    <link rel="stylesheet" href="newcss/custom.css"/>

    <script type="text/javascript" src="newjs/jquery.min.js"></script>
    <script type="text/javascript" src="newjs/jquery.easyui.min.js"></script>
    <script type="text/javascript">
        $(function () {
            initialTree();
            $('#btnSearch').bind('click', function () {
                $('#initgrid').datagrid('load', {
                    table_name: $('#searchTableName').val(),
                    db_name: $('#searchDbName').val()
                });
            });
        });

        function initialTree() {
            var postData = {};

            $.ajax({
                type: "POST",
                url: "tree/my_tree_data",
                contentType: "application/json",
                data: JSON.stringify(postData),
                dataType: "json",
                success: function (ret) {
                    if (ret != null) {
                        if (ret.msg == "success") {
                            $('#myjobs').tree({
                                data: ret.data,
                                onClick: function (node) {
                                    getNodeInfo(node);
                                }
                            });
                        } else {
                            $.messager.alert('警告', '加载我的调度任务节点失败', 'info');
                        }
                    }
                }
            });

            $.ajax({
                type: "POST",
                url: "tree/all_tree_data",
                contentType: "application/json",
                data: JSON.stringify(postData),
                dataType: "json",
                success: function (ret) {
                    if (ret != null) {
                        if (ret.msg == "success") {
                            $('#alljobs').tree({
                                data: ret.data,
                                onClick: function (node) {
                                    alert(node.text);
                                }
                            });
                        } else {
                            $.messager.alert('警告', '加载所有的调度任务节点失败', 'info');
                        }
                    }
                }
            });
        }

        function getNodeInfo(node) {
            if (node.attributes.job == "true") {
                var postData = {
                    "jobId": node.id
                };
                $.ajax({
                    type: "POST",
                    url: "job/get_upstream_job",
                    contentType: "application/json",
                    data: JSON.stringify(postData),
                    dataType: "json",
                    success: function (ret) {
                        if (ret != null) {
                            if (ret.msg == "success") {
                                bindJobContent(ret.data);
                            } else {
                                $.messager.alert('警告', '加载我的任务节点信息失败', 'info');
                            }
                        }
                    }
                });
            } else {
                var postData = {
                    "groupId": node.id
                };
                $.ajax({
                    type: "POST",
                    url: "group/get_upstream_group",
                    contentType: "application/json",
                    data: JSON.stringify(postData),
                    dataType: "json",
                    success: function (ret) {
                        if (ret != null) {
                            if (ret.msg == "success") {
                                bindGroupContent(ret.data);
                            } else {
                                $.messager.alert('警告', '加载我的组节点信息失败', 'info');
                            }
                        }
                    }
                });
            }

            controlButtons(node);
        }

        function controlButtons(node) {
            if (node.attributes.job = "true") {
                $("group_buttons").hide();
                $("job_buttons").show();
            } else {
                $("job_buttons").hide();
                $("group_buttons").show();
                if (node.attributes.directory = true) {
                    $("addGroupButton").linkbutton('enable');
                    $("addJobButton").linkbutton('disable');
                } else {
                    $("addJobButton").linkbutton('enable');
                    $("addGroupButton").linkbutton('disable');
                }
            }
        }

        function bindJobContent(jobData) {
            $("group_panel").hide();

            if (jobScheduleType.jobScheduleType = '定时调度') {
                $("job_schedule_panel").show();
                $("job_depend_panel").hide();

                $("job_id").val(jobData.id);
                $("job_type").val(jobData.jobRunType);
                $("job_name").val(jobData.name);
                $("job_schedule_type").val(jobData.jobScheduleType);
                $("job_owner").val(jobData.owner);
                $("job_cron").val(jobData.cronExpression);
                $("job_desc").val(jobData.desc);
                $("job_open_status").val(jobData.status);
                $("job_key_contact").val(jobData.importantContacts);
                $("job_priority_level").val(jobData.runPriorityLevel);
                $("job_notice_person").val(jobData.follows);
                $("job_retry_time").val(jobData.rollBackTimes);
                $("job_administrator").val(jobData.admins);
                $("job_retry_time_span").val(jobData.rollBackWaitTime);
                $("job_host_group_id").val(jobData.hostGroupId);
                $("job_run_time_span").val(jobData.maxTime);
                $("job_host_group_name").val(jobData.hostGroupId);

                //TODO
                //配置项信息
                //资源信息
                //继承的配置项信息
                //继承的资源信息
            } else if (jobScheduleType.jobScheduleType = '依赖调度') {
                $("job_schedule_panel").hide();
                $("job_depend_panel").show();

                $("job_depend_id").val(jobData.id);
                $("job_depend_type").val(jobData.jobRunType);
                $("job_depend_name").val(jobData.name);
                $("job_depend_schedule_type").val(jobData.jobScheduleType);
                $("job_depend_owner").val(jobData.owner);
                $("job_depend_open_status").val(jobData.status);
                $("job_depend_desc").val(jobData.desc);
                $("job_depend_jobs").val(jobData.dependencies);
                $("job_depend_key_contact").val(jobData.importantContacts);
                $("job_depend_span").val(jobData.jobCycle);
                $("job_depend_notice_person").val(jobData.follows);
                $("job_depend_priority_level").val(jobData.runPriorityLevel);
                $("job_depend_administrator").val(jobData.admins);
                $("job_depend_retry_time").val(jobData.rollBackTimes);
                $("job_depend_host_group_id").val(jobData.hostGroupId);
                $("job_depend_retry_time_span").val(jobData.rollBackWaitTime);
                $("job_depend_host_group_name").val(jobData.hostGroupId);
                $("job_depend_run_time_span").val(jobData.maxTime);

                //TODO
                //配置项信息
                //资源信息
                //继承的配置项信息
                //继承的资源信息
            }
        }

        function bindGroupContent(groupData) {
            $("group_panel").show();
            $("job_schedule_panel").hide();
            $("job_depend_panel").hide();

            $("group_id").val(groupData.id);
            $("group_name").val(groupData.name);
            $("group_owner").val(groupData.ownerName);
            $("group_desc").val(groupData.desc);
            $("group_notice_person").val(groupData.follows);
            $("group_administrator").val(groupData.admins);
            //TODO
            //配置项信息
            //资源信息
            //继承的配置项信息
            //继承的资源信息
        }

        function openWholeVeiw(){
            $("node_info_panel").hide();
            $("whole_view_content_panel").show();
            initWholeViewContent();
        }

        function initWholeViewContent(){
            var node =$('#myjobs').tree('getSelected');
            var groupId = node.attributes.id;

            $("#whole_view_table").datagrid({
                title : '任务总览',
                iconCls : 'icon-ok',
                width : '100%',
                height : 700,
                animate : true,
                collapsible : true,
                fitcolumns : true,
                singleSelect : true,
                pagination : true,
                striped:true,
                pageList : [ 20, 40, 60, 100, 1000 ],
                url : "job/get_job_status_by_groupId",
                queryParams : {
                    groupId : groupId
                },
                idField : 'id',
                showFooter : false,
                columns : [ [ {
                    field : 'id',
                    title : '实例Id',
                    width : 400,
                    align : 'center'
                }, {
                    field : 'toJobId',
                    title : '任务Id',
                    width : 200,
                    align : 'center'
                }, {
                    field : 'name',
                    title : '任务名称',
                    width : 200,
                    align : 'center'
                }, {
                    field : 'status',
                    title : '执行状态',
                    width : 200,
                    align : 'center'
                }, {
                    field : 'jobCycle',
                    title : '依赖状态',
                    width : 200,
                    align : 'center',
                    formatter : function(value, row, index) {
                        if (value.indexOf('未执行')!=-1) {
                            var s = "<label style='color:red;'>" + value + "</label>";
                            return s;
                        } else {
                            return '';
                        }
                    }
                }, {
                    field : 'lastStatus',
                    title : '上一次任务情况',
                    width : 200,
                    align : 'center'
                } ] ],
                onLoadError : function() {
                    $.messager.alert('警告', '加载任务总览数据出错', 'info');
                }
            });
        }

        function searchWholeView(){
            var startDate =$("whole_view_start_date").getValue();
            var endDate = $("whole_view_end_date").getValue();
            var node =$('#myjobs').tree('getSelected');
            var groupId = node.attributes.id;

            $('#whole_view_table').datagrid('load', {
                groupId : groupId,
                startDate:startDate,
                endDate:endDate
            });
        }

        function addGroup(){
            $('#add_group_dialog').dialog('open').dialog('setTitle', '新建组');
        }

        function addGroupConfirmed(){
            var groupName=$('#add_group_name').val();
            var groupType = $('#add_group_type').combobox('getValue');
        }

    </script>
</head>
<body>
<div id="body" class="page" style="position: relative;">
    <%@include file="header.jsp" %>
    <%@include file="leftside.jsp" %>
    <div class="mainContent">
        <div class="easyui-layout" style="width:700px;height:350px;">
            <div data-options="region:'west',split:true" title="West" style="width:100px;">
                <div class="easyui-accordion" style="width:500px;height:300px;">
                    <div title="我的调度任务" data-options="iconCls:'icon-search',collapsed:false,collapsible:false"
                         style="padding:10px;">
                        <input id="my_job_search" class="easyui-searchbox" prompt="id 或者 名称 匹配搜索，使用空格可匹配多个"
                               style="width:300px;height:25px;">
                    </div>
                    <div title="TreeMenu" style="padding:10px;">
                        <ul id="myjobs" class="easyui-tree">
                        </ul>
                    </div>
                    <div title="所有的调度任务" data-options="iconCls:'icon-search',collapsed:false,collapsible:false"
                         style="padding:10px;">
                        <input id="all_job_search" class="easyui-searchbox" prompt="id 或者 名称 匹配搜索，使用空格可匹配多个"
                               style="width:300px;height:25px;">
                    </div>
                    <div title="TreeMenu" style="padding:10px;">
                        <ul id="alljobs" class="easyui-tree">
                        </ul>
                    </div>
                </div>
            </div>
            <div data-options="region:'east',split:true" title="East" style="width:100px;">
                <div id="group_buttons">
                    <div><a id="wholeViewButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="openWholeVeiw()">任务总览</a></div>
                    <div><a id="autoJobsButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="openAutoJobs()">自动任务</a></div>
                    <div><a id="handleJobsButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="openHandleJobs()">手动任务</a></div>
                    <div><a id="addGroupButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="addGroup()">添加组</a></div>
                    <div><a id="addJobButton" href="javascript:void(0)" class="easyui-linkbutton c4" style="width:120px"
                            onclick="addJob()">添加任务</a></div>
                    <div><a id="editGroupButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="editGroup()">编辑</a></div>
                    <div><a id="deleteGroupButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="deleteGroup()">删除</a></div>
                    <div><a id="configGroupAdministorButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="configGroupAdministor()">配置管理员</a></div>
                </div>
                <div id="job_buttons">
                    <div><a id="openRunLogsButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="openRunLogs()">运行日志</a></div>
                    <div><a id="openRelyMapButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="openRelyMap()">依赖图</a></div>
                    <div><a id="editJobButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="editJob()">编辑</a></div>
                    <div><a id="handleRunButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="handleRun()">手动执行</a></div>
                    <div><a id="handleRecoverButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="handleRecover()">手动恢复</a></div>
                    <div><a id="openOrCloseButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="openOrClose()">开启/关闭</a></div>
                    <div><a id="deleteJobButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="deleteJob()">删除</a></div>
                    <div><a id="configJobAdministor" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="configJobAdministor()">配置管理员</a></div>
                    <div><a id="configJobAdministorButton" href="javascript:void(0)" class="easyui-linkbutton c4"
                            style="width:120px" onclick="configJobAdministor()">配置重要联系人</a></div>
                </div>
            </div>
            <div data-options="region:'center',title:'Main Title',iconCls:'icon-ok'">
                <div id="node_info_panel">
                    <div id="job_schedule_panel" class="easyui-panel" style="height:350px;padding:5px;">
                        <div id="p1" class="easyui-panel" title="任务信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <div><label>id:</label><input type="text" id="job_id"/></div>
                                <div><label>任务类型:</label><input type="text" id="job_type"/></div>
                                <div><label>名称:</label><input type="text" id="job_name"/></div>
                                <div><label>调度类型:</label><input type="text" id="job_schedule_type"/></div>
                                <div><label>所有人:</label><input type="text" id="job_owner"/></div>
                                <div><label>定时表达式:</label><input type="text" id="job_cron"/></div>
                                <div><label>描述:</label><input type="text" id="job_desc"/></div>
                                <div><label>自动调度:</label><input type="text" id="job_open_status"/></div>
                                <div><label>重要联系人:</label><input type="text" id="job_key_contact"/></div>
                                <div><label>任务优先级:</label><input type="text" id="job_priority_level"/></div>
                                <div><label>关注人员:</label><input type="text" id="job_notice_person"/></div>
                                <div><label>失败重试次数:</label><input type="text" id="job_retry_time"/></div>
                                <div><label>管理员:</label><input type="text" id="job_administrator"/></div>
                                <div><label>重试时间间隔:</label><input type="text" id="job_retry_time_span"/></div>
                                <div><label>host组id:</label><input type="text" id="job_host_group_id"/></div>
                                <div><label>预计时长:</label><input type="text" id="job_run_time_span"/></div>
                                <div><label>host组名:</label><input type="text" id="job_host_group_name"/></div>
                            </div>
                        </div>

                        <div id="p2" class="easyui-panel" title="配置项信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_config"/>
                            </div>
                        </div>
                        <div id="p3" class="easyui-panel" title="脚本" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_script"/>
                            </div>
                        </div>
                        <div id="p4" class="easyui-panel" title="资源信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_resource"/>
                            </div>
                        </div>
                        <div id="p5" class="easyui-panel" title="继承的配置项信息"
                             style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_inherited _config"/>
                            </div>
                        </div>
                        <div id="p6" class="easyui-panel" title="继承的资源信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_inherited _resource"/>
                            </div>
                        </div>
                    </div>

                    <div id="job_depend_panel" class="easyui-panel" style="height:350px;padding:5px;">
                        <div id="p21" class="easyui-panel" title="任务信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <div><label>id:</label><input type="text" id="job_depend_id"/></div>
                                <div><label>任务类型:</label><input type="text" id="job_depend_type"/></div>
                                <div><label>名称:</label><input type="text" id="job_depend_name"/></div>
                                <div><label>调度类型:</label><input type="text" id="job_depend_schedule_type"/></div>
                                <div><label>所有人:</label><input type="text" id="job_depend_owner"/></div>
                                <div><label>自动调度:</label><input type="text" id="job_depend_open_status"/></div>
                                <div><label>描述:</label><input type="text" id="job_depend_desc"/></div>
                                <div><label>依赖任务:</label><input type="text" id="job_depend_jobs"/></div>
                                <div><label>重要联系人:</label><input type="text" id="job_depend_key_contact"/></div>
                                <div><label>依赖周期:</label><input type="text" id="job_depend_span"/></div>
                                <div><label>关注人员:</label><input type="text" id="job_depend_notice_person"/></div>
                                <div><label>任务优先级:</label><input type="text" id="job_depend_priority_level"/></div>
                                <div><label>管理员:</label><input type="text" id="job_depend_administrator"/></div>
                                <div><label>失败重试次数:</label><input type="text" id="job_depend_retry_time"/></div>
                                <div><label>host组id:</label><input type="text" id="job_depend_host_group_id"/></div>
                                <div><label>重试时间间隔:</label><input type="text" id="job_depend_retry_time_span"/></div>
                                <div><label>host组名:</label><input type="text" id="job_depend_host_group_name"/></div>
                                <div><label>预计时长:</label><input type="text" id="job_depend_run_time_span"/></div>
                            </div>
                        </div>

                        <div id="p22" class="easyui-panel" title="配置项信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_depend_config"/>
                            </div>
                        </div>
                        <div id="p23" class="easyui-panel" title="脚本" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_depend_script"/>
                            </div>
                        </div>
                        <div id="p24" class="easyui-panel" title="资源信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_depend_resource"/>
                            </div>
                        </div>
                        <div id="p25" class="easyui-panel" title="继承的配置项信息"
                             style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_depend_inherited _config"/>
                            </div>
                        </div>
                        <div id="p26" class="easyui-panel" title="继承的资源信息"
                             style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="job_depend_inherited _resource"/>
                            </div>
                        </div>
                    </div>

                    <div id="group_panel" class="easyui-panel" style="height:350px;padding:5px;">
                        <div id="p7" class="easyui-panel" title="组信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <div><label>id:</label><input type="text" id="group_id"/></div>
                                <div><label>名称:</label><input type="text" id="group_name"/></div>
                                <div><label>所有人:</label><input type="text" id="group_owner"/></div>
                                <div><label>描述:</label><input type="text" id="group_desc"/></div>
                                <div><label>关注人员:</label><input type="text" id="group_notice_person"/></div>
                                <div><label>管理员:</label><input type="text" id="group_administrator"/></div>
                            </div>
                        </div>

                        <div id="p8" class="easyui-panel" title="配置项信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="group_config"/>
                            </div>
                        </div>
                        <div id="p9" class="easyui-panel" title="资源信息" style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="group_resource"/>
                            </div>
                        </div>
                        <div id="p10" class="easyui-panel" title="继承的配置项信息"
                             style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="group_inherited _config"/>
                            </div>
                        </div>
                        <div id="p11" class="easyui-panel" title="继承的资源信息"
                             style="width:600px;height:200px;padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <input type="text" id="group_inherited _resource"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div id="whole_view_content_panel">
                    开始日期：
                    <input id="whole_view_start_date" type="text" class="easyui-datebox" required="required">
                    结束日期：
                    <input id="whole_view_end_date" type="text" class="easyui-datebox" required="required">
                    <a id="whole_view_search" href="javascript:void(0)" class="easyui-linkbutton" data-options="iconCls:'icon-search'" onclick="searchWholeView()">查询</a>
                    <table id="whole_view_table">

                    </table>

                </div>
            </div>
        </div>
        <div id="allDialog">
            <div id="add_group_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px" closed="true" buttons="#add_group-buttons">
                <form id="add_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div >
                        <label>组名称:</label> <input id="add_group_name" class="easyui-textbox">
                    </div>

                    <div >
                        <label>组类型:</label> <select id="add_group_type" class="easyui-combobox" style="width:200px;">
                        <option value="0">大目录</option>
                        <option value="1">小目录</option>
                    </select>
                    </div>
                </form>
            </div>
            <div id="add_group-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="addGroupConfirmed()"> 确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton"  iconcls="icon-cancel" onclick="javascript:$('#add_group_dialog').dialog('close')">取消</a>
            </div>
        </div>
    </div>
</div>

</body>
</html>
