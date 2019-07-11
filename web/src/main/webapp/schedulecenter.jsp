<%@ page import="static com.taobao.zeus.web.FileUploadServlet.hdfsLibPath" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <title>调度中心</title>
    <meta charset="utf-8">

    <link rel="stylesheet" href="newcss/easyui.css"/>
    <link rel="stylesheet" href="newcss/color.css"/>
    <link rel="stylesheet" href="newcss/icon.css"/>
    <link rel="stylesheet" href="newcss/custom.css"/>

    <script type="text/javascript" src="newjs/jquery.min.js"></script>
    <script type="text/javascript" src="newjs/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="newjs/jquery.extend.js"></script>
    <script type="text/javascript" src="newjs/schedulecenter.js"></script>
    <script type="text/javascript" src="newjs/echarts.js"></script>
</head>
<body>
<div id="body" class="page" style="position: relative;">
    <%@include file="header.jsp" %>
    <%@include file="leftside.jsp" %>
    <div class="mainContent">
        <div class="easyui-layout" style="width:1100px;height:700px;">
            <div data-options="region:'west',split:true" style="width:200px;">
                <div title="所有的调度任务" data-options="collapsed:false,collapsible:true" style="padding:10px;">
                    <input id="all_job_search" class="easyui-searchbox" prompt="id 或者 名称 匹配搜索，使用空格可匹配多个"
                           style="width:300px;height:25px;">
                    <ul id="all_jobs" class="easyui-tree">
                    </ul>

                    <div id="rc_follow_menu" class="easyui-menu" style="width:120px;">
                        <div onclick="followWith()">关注(接收报警)</div>
                    </div>
                    <div id="rc_unfollow_menu" class="easyui-menu" style="width:120px;">
                        <div onclick="unFollowWith()">取消关注</div>
                    </div>

                </div>
            </div>
            <div id="region_east" data-options="region:'east',split:true" style="width:100px;">
                <div id="group_buttons">
                    <div><a id="wholeViewButton" href="javascript:void(0)" class="easyui-linkbutton"
                            style="width:80px;" onclick="openWholeVeiw()">任务总览</a></div>
                    <div><a id="autoJobsButton" href="javascript:void(0)" class="easyui-linkbutton"
                            style="width:80px;margin-top: 5px;" onclick="openAutoJobs()">自动任务</a></div>
                    <div><a id="handleJobsButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="openHandleJobs()">手动任务</a></div>
                    <div><a id="addGroupButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="addGroup()">添加组</a></div>
                    <div><a id="addJobButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="addJob()">添加任务</a></div>
                    <div><a id="editGroupButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="editGroup()">编辑</a></div>
                    <div><a id="deleteGroupButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="deleteGroup()">删除</a></div>
                    <div><a id="configGroupAdministorButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="configGroupAdministor()">管理员</a></div>
                </div>
                <div id="job_buttons">
                    <div><a id="openRunLogsButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="openRunLogs()">运行日志</a></div>
                    <div><a id="openRelyMapButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="openRelyMap()">依赖图</a></div>
                    <div><a id="editJobButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="editJob()">编辑</a></div>
                    <div><a id="handleRunButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="handleRun()">手动执行</a></div>
                    <div><a id="handleRecoverButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="handleRecover()">手动恢复</a></div>
                    <div><a id="openOrCloseButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="openOrClose()">开启/关闭</a></div>
                    <div><a id="deleteJobButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="deleteJob()">删除</a></div>
                    <div><a id="configJobOwnerButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="configJobOwner()">所有人</a></div>
                    <div><a id="configJobAdminButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="configJobAdmin()">管理员</a></div>
                    <div><a id="configJobContactButton" href="javascript:void(0)" class="easyui-linkbutton "
                            style="width:80px;margin-top: 5px;" onclick="configJobContact()">联系人</a></div>
                </div>
            </div>
            <div data-options="region:'center',iconCls:'icon-ok'">
                <div id="node_info_panel">
                    <div id="job_schedule_panel" class="easyui-panel" style="padding:5px;">
                        <div id="p1" class="easyui-panel" title="任务信息" style="padding:10px;" data-options="collapsible:true">
                            <div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">id:</label>
                                    <label id="job_id"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">任务类型:</label>
                                    <label id="job_type" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">名称:</label>
                                    <label id="job_name"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">调度类型:</label>
                                    <label id="job_schedule_type" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">所有人:</label>
                                    <label id="job_owner"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">定时表达式:</label>
                                    <label id="job_cron" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">描述:</label>
                                    <label id="job_desc"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">自动调度:</label>
                                    <label id="job_open_status" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">重要联系人:</label>
                                    <label id="job_key_contact"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">任务优先级:</label>
                                    <label id="job_priority_level" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">关注人员:</label>
                                    <label id="job_notice_person"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">失败重试次数:</label>
                                    <label id="job_retry_time" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">管理员:</label>
                                    <label id="job_administrator"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">重试时间间隔:</label>
                                    <label id="job_retry_time_span" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">host组id:</label>
                                    <label id="job_host_group_id"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">预计时长:</label>
                                    <label id="job_run_time_span" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;">
                                    <label style="display:inline-block;width: 100px;">host组名:</label>
                                    <label id="job_host_group_name"></label></div>
                            </div>
                        </div>

                        <div id="p2" class="easyui-panel" title="配置项信息" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_config"></label>
                            </div>
                        </div>
                        <div id="p3" class="easyui-panel" title="脚本" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_script"></label>
                            </div>
                        </div>
                        <div id="p4" class="easyui-panel" title="资源信息" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_resource"></label>
                            </div>
                        </div>
                        <div id="p5" class="easyui-panel" title="继承的配置项信息" style="padding:10px;" data-options="collapsible:true">
                            <div>
                                <label id="job_inherited_config"></label>
                            </div>
                        </div>
                        <div id="p6" class="easyui-panel" title="继承的资源信息" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_inherited_resource"></label>
                            </div>
                        </div>
                    </div>

                    <div id="job_depend_panel" class="easyui-panel" style="padding:5px;">
                        <div id="p21" class="easyui-panel" title="任务信息" style="padding:10px;" data-options="collapsible:true">
                            <div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">id:</label>
                                    <label id="job_depend_id"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">任务类型:</label>
                                    <label id="job_depend_type" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">名称:</label>
                                    <label id="job_depend_name"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">调度类型:</label>
                                    <label id="job_depend_schedule_type" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">所有人:</label>
                                    <label id="job_depend_owner"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">自动调度:</label>
                                    <label id="job_depend_open_status" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">描述:</label>
                                    <label id="job_depend_desc"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">依赖任务:</label>
                                    <label id="job_depend_jobs" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">重要联系人:</label>
                                    <label id="job_depend_key_contact"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">依赖周期:</label>
                                    <label id="job_depend_span" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">关注人员:</label>
                                    <label id="job_depend_notice_person"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">任务优先级:</label>
                                    <label id="job_depend_priority_level" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">管理员:</label>
                                    <label id="job_depend_administrator"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">失败重试次数:</label>
                                    <label id="job_depend_retry_time" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">host组id:</label>
                                    <label id="job_depend_host_group_id"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">重试时间间隔:</label>
                                    <label id="job_depend_retry_time_span" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;position: relative;">
                                    <label style="display:inline-block;width: 100px;">host组名:</label>
                                    <label id="job_depend_host_group_name"></label>
                                    <label style="display:inline-block;width: 100px;position: absolute;left: 400px;">预计时长:</label>
                                    <label id="job_depend_run_time_span" style="display:inline-block;width: 100px;position: absolute;left: 500px;"></label></div>
                            </div>
                        </div>

                        <div id="p22" class="easyui-panel" title="配置项信息" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_depend_config"></label>
                            </div>
                        </div>
                        <div id="p23" class="easyui-panel" title="脚本" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_depend_script"></label>
                            </div>
                        </div>
                        <div id="p24" class="easyui-panel" title="资源信息" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_depend_resource"></label>
                            </div>
                        </div>
                        <div id="p25" class="easyui-panel" title="继承的配置项信息" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_depend_inherited_config"></label>
                            </div>
                        </div>
                        <div id="p26" class="easyui-panel" title="继承的资源信息" style="padding:10px;"
                             data-options="collapsible:true">
                            <div>
                                <label id="job_depend_inherited_resource"></label>
                            </div>
                        </div>
                    </div>

                    <div id="group_panel" class="easyui-panel">
                        <div id="p7" class="easyui-panel" title="组信息"
                             data-options="collapsible:true">
                            <div>
                                <div style="margin-left: 10px;margin-top: 5px;"><label style="display:inline-block;width: 100px;">id:</label><label id="group_id"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;"><label style="display:inline-block;width: 100px;">名称:</label><label id="group_name"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;"><label style="display:inline-block;width: 100px;">所有人:</label><label id="group_owner"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;"><label style="display:inline-block;width: 100px;">描述:</label><label id="group_desc"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;"><label style="display:inline-block;width: 100px;">关注人员:</label><label
                                        id="group_notice_person"></label></div>
                                <div style="margin-left: 10px;margin-top: 5px;"><label style="display:inline-block;width: 100px;">管理员:</label><label
                                        id="group_administrator"></label></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div id="allDialog">
            <div id="whole_view_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#whole_view-buttons" data-options="modal:true">
                <form id="whole_view_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div id="whole_view_content_panel">
                        开始日期：
                        <input id="whole_view_start_date" type="text" class="easyui-datebox" required="required">
                        结束日期：
                        <input id="whole_view_end_date" type="text" class="easyui-datebox" required="required">
                        <a id="whole_view_search" href="javascript:void(0)" class="easyui-linkbutton"
                           data-options="iconCls:'icon-search'" onclick="searchWholeView()">查询</a>
                        <table id="whole_view_table">
                        </table>
                    </div>
                </form>
            </div>
            <div id="whole_view-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#whole_view_dialog').dialog('close')">取消</a>
            </div>

            <div id="job_rely_dialog" class="easyui-dialog" style="width: 800px; height: 500px; padding: 10px 20px"
                 closed="true" buttons="#job_rely-buttons" data-options="modal:true">
                <div>任务依赖图：</div>
                <div id="job_dependee" style="width: 600px;height:300px;"></div>
                任务被依赖图：
                <div style="margin:0;padding:0; width:735px;height:0.5px;background-color:#333;overflow:hidden;"></div>
                <div id="job_depender" style="width: 600px;height:300px;"></div>
            </div>
            <div id="job_rely-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#job_rely_dialog').dialog('close')">取消</a>
            </div>

            <div id="add_group_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#add_group-buttons" data-options="modal:true">
                <form id="add_group_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <label>组名称:</label> <input id="add_group_name" class="easyui-textbox">
                    </div>

                    <div>
                        <label>组类型:</label> <select id="add_group_type" class="easyui-combobox" style="width:200px;">
                        <option value="0">大目录</option>
                        <option value="1">小目录</option>
                    </select>
                    </div>
                </form>
            </div>
            <div id="add_group-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="addGroupConfirmed()">
                    确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#add_group_dialog').dialog('close')">取消</a>
            </div>

            <div id="edit_group_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#edit_group-buttons" data-options="modal:true">
                <form id="edit_group_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <label>名称:</label> <input id="edit_group_name" class="easyui-textbox">
                    </div>

                    <div>
                        <label>描述:</label> <input id="edit_group_desc" class="easyui-textbox">
                    </div>
                    <div>
                        <label>配置项信息:</label> <input id="edit_group_config_info" class="easyui-textbox">
                    </div>

                    <div>
                        <label>资源信息:</label> <input id="edit_group_resource_info" class="easyui-textbox">
                    </div>
                </form>
            </div>
            <div id="edit_group-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="editGroupConfirmed()">
                    保存</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#edit_group_dialog').dialog('close')">取消</a>
            </div>

            <div id="delete_group_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#delete_group-buttons" data-options="modal:true">
                <form id="delete_group_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <label>你确认删除此组:</label> <input id="delete_group_name" class="easyui-textbox">
                    </div>
                </form>
            </div>
            <div id="delete_group-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok"
                   onclick="deleteGroupConfirmed()"> 保存</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#delete_group_dialog').dialog('close')">取消</a>
            </div>

            <div id="config_admins_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#config_admins-buttons" data-options="modal:true">
                <form id="config_admins_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <input type="text" id="config_admins" class="easyui-textbox">
                    </div>
                </form>
            </div>
            <div id="config_admins-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="addGroupAdmins()">
                    添加管理员</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="deleteGroupAdmins()">
                    删除管理员</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="transferAdmin()">
                    转让所有权</a>
            </div>

            <div id="add_admins_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#add_admins-buttons" data-options="modal:true">
                <form id="add_admins_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <div class="easyui-panel" style="width:100%;max-width:400px;padding:30px 60px;">
                            <div style="margin-bottom:20px">
                                <input id="add_admins" class="easyui-combobox" style="width:100%;" data-options="
                                    valueField: 'uid',textField: 'name',label: '选择用户:',labelPosition: 'left'">
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div id="add_admins-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok"
                   onclick="addGroupAdminsConfirm()"> 添加</a>
            </div>

            <div id="delete_admins_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#delete_admins-buttons" data-options="modal:true">
                <form id="delete_admins_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <div class="easyui-panel" style="width:100%;max-width:400px;padding:30px 60px;">
                            <div style="margin-bottom:20px">
                                <input id="delete_admins" class="easyui-combobox" style="width:100%;" data-options="
                                    valueField: 'uid',textField: 'name',label: '选择用户:',labelPosition: 'left'">
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div id="delete_admins-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok"
                   onclick="deleteGroupAdminsConfirm()"> 添加</a>
            </div>

            <div id="transfer_admins_dialog" class="easyui-dialog"
                 style="width: 420px; height: 320px; padding: 10px 20px" closed="true"
                 buttons="#transfer_admins-buttons" data-options="modal:true">
                <form id="transfer_admins_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <div class="easyui-panel" style="width:100%;max-width:400px;padding:30px 60px;">
                            <div style="margin-bottom:20px">
                                <input id="transfer_admins" class="easyui-combobox" style="width:100%;" data-options="
                                    valueField: 'uid',textField: 'name',label: '选择组管理员:',labelPosition: 'left'">
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div id="transfer_admins-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok"
                   onclick="transferGroupAdminsConfirm()"> 添加</a>
            </div>

            <div id="add_job_dialog" class="easyui-dialog" style="width: 420px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#add_job-buttons" data-options="modal:true">
                <form id="add_job_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <div class="easyui-panel" style="width:100%;max-width:400px;padding:30px 60px;">
                            <div>
                                <label>任务名称:</label> <input id="add_job_name" class="easyui-textbox">
                            </div>
                            <div>
                                <label>任务类型:</label> <select id="add_job_type" class="easyui-combobox"
                                                             style="width:200px;">
                                <option value="0">MapReduce程序</option>
                                <option value="1">shell脚本</option>
                                <option value="1">hive脚本</option>
                            </select>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div id="add_job-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="addJobConfirm()">
                    保存</a>
            </div>

            <div id="run_log_dialog" class="easyui-dialog" style="width: 1200px; height: 500px; padding: 10px 20px"
                 closed="true" buttons="#run_log-buttons" data-options="modal:true">
                <form id="run_log_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div id="run_log_content_panel">
                        <table id="run_log_table">
                        </table>
                    </div>
                </form>
            </div>
            <div id="run_log-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="refreshJobLog()">
                    刷新</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#run_log_dialog').dialog('close')">取消</a>
            </div>

            <div id="job_log_dialog" class="easyui-dialog" style="width: 600px; height: 500px; padding: 10px 20px"
                 closed="true" data-options="modal:true">
                <form id="job_log_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <div id="job_log_label"></div>
                    </div>
                </form>
            </div>

            <div id="edit_schedule_job_dialog" class="easyui-dialog" style="width: 900px; height: 500px; padding: 10px 20px"
                 closed="true" buttons="#edit_job-buttons" data-options="modal:true">
                <form id="edit_job_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <input id="edit_job_owner" type="hidden" name="owner"/>
                    <input id="edit_group_id" type="hidden" name="groupId"/>
                    <input id="edit_run_type" type="hidden" name="runType"/>
                    <input id="edit_cycle" type="hidden" name="cycle"/>
                    <input id="edit_auto" type="hidden" name="auto"/>
                    <input id="edit_offset" type="hidden" name="offset"/>
                    <input id="edit_timezone" type="hidden" name="timezone"/>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">名称:</label>
                        <input id="edit_job_name" name="name" style="width: 300px;"/>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;"> 调度类型:</label>
                        <select id="edit_schedule_type" name="scheduleType" style="display:inline-block;width: 100px;position: absolute;left: 550px;">
                            <option value="0">定时调度</option>
                            <option value="1">依赖调度</option>
                        </select>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;"> 失败重试次数:</label>
                        <select id="edit_fail_retry_times" name="rollBackTimes">
                            <option value="0">0</option>
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option value="3">3</option>
                            <option value="4">4</option>
                        </select>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;">定时表达式:</label>
                        <input id="edit_cron_string" name="cronExpression" style="display:inline-block;width: 100px;position: absolute;left: 550px;"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;"> 重试间隔:</label>
                        <select id="edit_fail_retry_span" name="rollBackWaitTime">
                            <option value="1">1</option>
                            <option value="10">10</option>
                            <option value="30">30</option>
                            <option value="60">60</option>
                            <option value="120">120</option>
                        </select>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;">host组id:</label>
                        <input id="edit_host_group_id" name="hostGroupId" onclick="openHostGroupInfo($(this))"
                               style="display:inline-block;width: 100px;position: absolute;left: 550px;"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;"> 任务优先级:</label>
                        <select id="edit_priority" name="runPriorityLevel">
                            <option value="3">high</option>
                            <option value="2">medium</option>
                            <option value="1">low</option>
                        </select>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;"> 脚本是否可见:</label>
                        <select id="edit_script_visible" name="zeusSecretScript" style="display:inline-block;width: 100px;position: absolute;left: 550px;">
                            <option value="false">可见</option>
                            <option value="true">不可见</option>
                        </select>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">描述:</label> <input id="edit_desc" name="descr" style="width: 300px;"/>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;">预计时长:</label>
                        <input id="edit_run_time_span" name="cron" style="display:inline-block;width: 100px;position: absolute;left: 550px;"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">配置项信息:</label>
                        <input id="edit_job_config" name="" style="width: 100px;"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">脚本:</label>
                        <input id="edit_job_script" name="script" class="easyui-textbox" labelPosition="top" multiline="true" style="width:100%;height:220px">
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">资源信息:</label>
                        <input id="edit_resource" name="resources" class="easyui-textbox" multiline="true" style="width:100%;height:60px"/>
                    </div>
                </form>
            </div>
            <div id="edit_job-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="uploadResourceDialog()">
                    上传资源文件</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="editJobConfirm()">
                    保存</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#edit_schedule_job_dialog').dialog('close')">取消</a>
            </div>

            <input id="hdfsLibPath" type="hidden" value="<%=hdfsLibPath%>"/>
            <div id="upload_resource_dialog" class="easyui-dialog" style="width: 550px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#upload_resource_dialog-buttons">
                <form id="uploadForm" enctype="multipart/form-data">
                    <input id="resource_file" type="file" name="file"/>
                </form>
            </div>
            <div id="upload_resource_dialog-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel" onclick="resetResource()">
                    重置</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="uploadResource()">上传</a>
            </div>

            <div id="host_group_dialog" class="easyui-dialog" style="width: 550px; height: 320px; padding: 10px 20px"
                 closed="true" buttons="#host_group_dialog-buttons">
                <table id="host_group_grid">
                </table>
            </div>
            <div id="host_group_dialog-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="selectHostGroupConfirm()">
                    确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#host_group_dialog').dialog('close')">取消</a>
            </div>


            <div id="edit_depend_job_dialog" class="easyui-dialog" style="width: 900px; height: 500px; padding: 10px 20px"
                 closed="true" buttons="#edit_depend_job-buttons" data-options="modal:true">
                <form id="edit_depend_job_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <input id="edit_depend_job_owner" type="hidden" name="owner"/>
                    <input id="edit_depend_group_id" type="hidden" name="groupId"/>
                    <input id="edit_depend_run_type" type="hidden" name="runType"/>
                    <input id="edit_depend_auto" type="hidden" name="auto"/>
                    <input id="edit_depend_offset" type="hidden" name="offset"/>
                    <input id="edit_depend_timezone" type="hidden" name="timezone"/>

                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">名称:</label>
                        <input id="edit_depend_job_name" name="name" style="width: 300px;"/>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;"> 调度类型:</label>
                        <select id="edit_depend_schedule_type" name="scheduleType" style="display:inline-block;width: 100px;position: absolute;left: 550px;">
                            <option value="0">定时调度</option>
                            <option value="1">依赖调度</option>
                        </select>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;"> 失败重试次数:</label>
                        <select id="edit_depend_fail_retry_times" name="rollBackTimes" style="width: 300px;">
                            <option value="0">0</option>
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option value="3">3</option>
                            <option value="4">4</option>
                        </select>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;">依赖任务:</label>
                        <input id="edit_depend_jobs" name="dependencies" style="display:inline-block;width: 100px;position: absolute;left: 550px;" onclick="openDependDialog()"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;"> 重试间隔:</label>
                        <select id="edit_depend_fail_retry_span" name="rollBackWaitTime" style="width: 300px;">
                            <option value="1">1</option>
                            <option value="10">10</option>
                            <option value="30">30</option>
                            <option value="60">60</option>
                            <option value="120">120</option>
                        </select>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;"> 依赖周期:</label>
                        <select id="edit_depend_cycle" name="dependCycle" style="display:inline-block;width: 100px;position: absolute;left: 550px;">
                            <option value="同一天">同一天</option>
                            <option value="无限制">无限制</option>
                        </select>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;"> 任务优先级:</label>
                        <select id="edit_depend_priority" name="runPriorityLevel" style="width: 300px;">
                            <option value="3">high</option>
                            <option value="2">medium</option>
                            <option value="1">low</option>
                        </select>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;">host组id:</label>
                        <input id="edit_depend_host_group_id" name="hostGroupId" style="display:inline-block;width: 100px;position: absolute;left: 550px;"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;"> 脚本是否可见:</label>
                        <select id="edit_depend_script_visible" name="zeusSecretScript" style="width: 300px;">
                            <option value="false">可见</option>
                            <option value="true">不可见</option>
                        </select>
                        <label style="display:inline-block;width: 100px;position: absolute;left: 450px;">预计时长:</label>
                        <input id="edit_depend_run_time_span" name="cron" style="display:inline-block;width: 100px;position: absolute;left: 550px;"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">描述:</label>
                        <input id="edit_depend_desc" name="descr" style="width: 100px;"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">配置项信息:</label>
                        <input id="edit_depend_job_config" name="" style="width: 100px;"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">脚本:</label>
                        <input id="edit_depend_job_script" name="script" class="easyui-textbox" labelPosition="top" multiline="true" style="width:100%;height:220px"/>
                    </div>
                    <div style="margin-top: 10px;position: relative;">
                        <label style="display:inline-block;width: 100px;">资源信息:</label>
                        <input id="edit_depend_resource" name="resources" class="easyui-textbox" multiline="true" style="width:100%;height:60px"/>
                    </div>
                </form>
            </div>
            <div id="edit_depend_job-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="editDependJobConfirm()">
                    保存</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#edit_depend_job_dialog').dialog('close')">取消</a>
            </div>

            <div id="job_depend_dialog" class="easyui-dialog" style="width: 900px; height: 500px; padding: 10px 20px"
                 closed="true" buttons="#job_depend-buttons" data-options="modal:true">
                <div style="position: relative;">
                    <div style="position: absolute;top:10px;">
                        <div style="margin-bottom: 10px;">
                            <input id="search_job_name" class="easyui-textbox" data-options="prompt:'job name or id',iconCls:'icon-search'"
                                   style="width:300px;">
                        </div>
                        <div>所有任务列表 (双击加入依赖列表)</div>
                        <div id="all_job_info" class="easyui-datalist" style="width:400px;height:250px;margin-top:20px;">
                        </div>
                        <div style="position: absolute;left: 420px;top:40px;">
                            <div>依赖的任务列表 (双击从依赖列表移除)</div>
                            <div id="rely_job_info" class="easyui-datalist" style="width:400px;height:250px">
                            </div>
                        </div>
                    </div>
                    <div id="job_depend-buttons">
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="okJobDepend()">
                            确认</a>
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                           onclick="javascript:$('#job_depend_dialog').dialog('close')">取消</a>
                    </div>

                </div>

            </div>

            <div id="handle_run_dialog" class="easyui-dialog" style="width: 420px; height: 220px; padding: 10px 20px"
                 closed="true" buttons="#handle_run-buttons" data-options="modal:true">
                <form id="handle_run_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <label>选择实例版本:</label>
                        <input id="handle_run_actions" name="handleRunActions" style="width: 200px;">
                    </div>
                </form>
            </div>
            <div id="handle_run-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="handleRunConfirmed()">
                    确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#handle_run_dialog').dialog('close')">取消</a>
            </div>

            <div id="handle_recover_dialog" class="easyui-dialog" style="width: 420px; height: 220px; padding: 10px 20px"
                 closed="true" buttons="#handle_recover-buttons" data-options="modal:true">
                <form id="handle_recover_form" class="easyui-form" method="post" data-options="novalidate:true">
                    <div>
                        <label>选择实例版本:</label>
                        <input id="handle_recover_actions" name="handleRecoverActions" style="width: 200px;">
                    </div>
                </form>
            </div>
            <div id="handle_recover-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="handleRecoverConfirmed()">
                    确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#handle_recover_dialog').dialog('close')">取消</a>
            </div>

            <div id="job_owner_dialog" class="easyui-dialog" style="width: 450px; height: 200px; padding: 10px 20px"
                 closed="true" buttons="#job_owner-buttons" data-options="modal:true">
                <input id="job_owners" class="easyui-combobox" style="width:300px;" data-options="
                                    valueField: 'uid',textField: 'name',label: '选择所有人:',labelPosition: 'left'">
            </div>
            <div id="job_owner-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="updateOwner()">
                    确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#job_owner_dialog').dialog('close')">取消</a>
            </div>

            <div id="config_job_admin_dialog" class="easyui-dialog" style="width: 450px; height: 400px; padding: 10px 20px"
                 closed="true" buttons="#config_job_admin-buttons" data-options="modal:true">
                    <table id="job_admin_grid" class="easyui-datagrid">
                    </table>
                    <div id="config_job_admin_grid-toolbar">
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-add" plain="true" onclick="addJobAdminDialog()">新增</a>
                        <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-remove" plain="true" onclick="deleteJobAdminDialog()">删除</a>
                    </div>
            </div>
            <div id="config_job_admin-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok"
                   onclick="configJobConfirm()">确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#config_job_admin_dialog').dialog('close')">取消</a>
            </div>
            <div id="add_job_admin_dialog" class="easyui-dialog" style="width: 450px; height: 200px; padding: 10px 20px"
                 closed="true" buttons="#add_job_admin_dialog-buttons" data-options="modal:true">
                <input id="add_job_admins" class="easyui-combobox" style="width:300px;" data-options="
                                    valueField: 'uid',textField: 'name',label: '选择管理员:',labelPosition: 'left'">
            </div>
            <div id="add_job_admin_dialog-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="addJobAdmin()">
                    确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#add_job_admin_dialog').dialog('close')">取消</a>
            </div>

            <div id="config_job_contact_dialog" class="easyui-dialog" style="width: 450px; height: 400px; padding: 10px 20px"
                 closed="true" buttons="#config_job_contact-buttons" data-options="modal:true">
                <table id="job_contact_grid" class="easyui-datagrid">
                </table>
                <div id="config_job_contact_grid-toolbar">
                    <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-add" plain="true" onclick="addJobContactDialog()">新增</a>
                    <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-remove" plain="true" onclick="deleteJobContactDialog()">删除</a>
                </div>
            </div>
            <div id="config_job_contact-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#config_job_contact_dialog').dialog('close')">关闭</a>
            </div>
            <div id="add_job_contact_dialog" class="easyui-dialog" style="width: 450px; height: 200px; padding: 10px 20px"
                 closed="true" buttons="#add_job_contact_dialog-buttons" data-options="modal:true">
                <input id="add_job_contact" class="easyui-combobox" style="width:300px;" data-options="
                                    valueField: 'uid',textField: 'name',label: '选择联系人:',labelPosition: 'left'">
            </div>
            <div id="add_job_contact_dialog-buttons">
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="addJobContact()">
                    确定</a>
                <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
                   onclick="javascript:$('#add_job_contact_dialog').dialog('close')">取消</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
