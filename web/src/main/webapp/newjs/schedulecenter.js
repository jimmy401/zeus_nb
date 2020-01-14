$(function () {
    initialTree();
    $('#btnSearch').bind('click', function () {
        $('#initgrid').datagrid('load', {
            table_name: $('#searchTableName').val(),
            db_name: $('#searchDbName').val()
        });
    });
});

var activeNode;

function initialTree() {
    if (activeNode == null) {
        $('#group_buttons').hide();
        $('#job_buttons').hide();
        $('#job_schedule_panel').hide();
        $('#job_depend_panel').hide();
        $('#group_panel').hide();
    }

    var postData = {};

    $.ajax({
        type: "GET",
        url: "tree/all_tree_data",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#all_jobs').tree({
                        data: ret.data,
                        onClick: function (node) {
                            activeNode = node;
                            getNodeInfo(node);
                        },
                        onContextMenu: function(e,node){
                            e.preventDefault();
                            $(this).tree('select',node.target);

                            var postData = {
                                "jobId": activeNode.id
                            };
                            $.ajax({
                                type: "GET",
                                url: "job/follow_status",
                                data: postData,
                                success: function (ret) {
                                    if (ret != null) {
                                        if (ret.msg == "success") {
                                            if (ret.data)
                                            {
                                                $('#rc_unfollow_menu').menu('show',{
                                                left: e.pageX,
                                                top: e.pageY
                                            });
                                            }else{
                                                $('#rc_follow_menu').menu('show',{
                                                    left: e.pageX,
                                                    top: e.pageY
                                                });
                                            }
                                        } else {
                                            $.messager.alert('警告', '获取组节点信息失败', 'info');
                                        }
                                    }
                                }
                            });
                        }
                    });
                } else {
                    $.messager.alert('警告', '加载所有的调度任务节点失败', 'info');
                }
            }
        }
    });
}
var activeNodeInfo;
function getNodeInfo(node) {
    if (node.attributes.group) {
        $('#group_buttons').show();
        $('#job_buttons').hide();
        $('#job_schedule_panel').hide();
        $('#job_depend_panel').hide();
        $('#group_panel').show();
    } else {
        $('#group_buttons').hide();
        $('#job_buttons').show();
        $('#job_schedule_panel').show();
        $('#job_depend_panel').show();
        $('#group_panel').hide();
    }
    if (node.attributes.directory) {
        $('#addJobButton').linkbutton('disable');
        $('#addGroupButton').linkbutton('enable');

    } else {
        $('#addJobButton').linkbutton('enable');
        $('#addGroupButton').linkbutton('disable');
    }
    if (node.attributes.group) {
        var postData = {
            "groupId": node.id
        };
        $.ajax({
            type: "GET",
            url: "group/get_upstream_group",
            data: postData,
            success: function (ret) {
                if (ret != null) {
                    if (ret.msg == "success") {
                        activeNodeInfo=ret.data;
                        bindGroupContent(ret.data);
                    } else {
                        $.messager.alert('警告', '获取组节点信息失败', 'info');
                    }
                }
            }
        });
    } else {
        var postData = {
            "jobId": node.id
        };
        $.ajax({
            type: "GET",
            url: "job/get_upstream_job",
            data: postData,
            success: function (ret) {
                if (ret != null) {
                    if (ret.msg == "success") {
                        activeNodeInfo=ret.data;
                        bindJobContent(ret.data);
                    } else {
                        $.messager.alert('警告', '获取任务节点信息失败', 'info');
                    }
                }
            }
        });
    }

}

function bindJobContent(jobData) {
    if (jobData.jobScheduleType == '定时调度') {
        $("#job_schedule_panel").show();
        $("#job_depend_panel").hide();

        $("#job_id").html(jobData.id);
        $("#job_type").html(jobData.jobRunType);
        $("#job_name").html(jobData.name);
        $("#job_schedule_type").html(jobData.jobScheduleType);
        $("#job_owner").html(jobData.owner);
        $("#job_cron").html(jobData.cronExpression);
        $("#job_desc").html(jobData.desc);
        $("#job_open_status").html(jobData.auto?'打开':'关闭');
        $("#job_key_contact").html(jobData.importantContacts);
        $("#job_priority_level").html(jobData.runPriorityLevel);
        $("#job_notice_person").html(jobData.follows);
        $("#job_retry_time").html(jobData.rollBackTimes);
        $("#job_administrator").html(jobData.admins);
        $("#job_retry_time_span").html(jobData.rollBackWaitTime);
        $("#job_host_group_id").html(jobData.hostGroupId);
        $("#job_run_time_span").html(jobData.maxTime);


        $("#job_script").html(jobData.script);
        var resources = "";
        if (jobData.localResources.length > 0) {
            for (var i = 0; i < jobData.localResources.length; i++) {
                resources += "\n" + jobData.localResources[i].name;
            }
        }
        $("#job_resource").html(resources);

        var postData = {
            "id": jobData.hostGroupId
        };
        $.ajax({
            type: "GET",
            url: "job/get_host_group_name_by_id",
            data: postData,
            success: function (ret) {
                if (ret != null) {
                    if (ret.msg == "success") {
                        $("#job_host_group_name").html(ret.data);
                    } else {
                        $.messager.alert('警告', '获取任务组名字失败', 'info');
                    }
                }
            }
        });
        //TODO
        //配置项信息
        //资源信息
        //继承的配置项信息
        //继承的资源信息
    } else {
        $("#job_schedule_panel").hide();
        $("#job_depend_panel").show();

        $("#job_depend_id").html(jobData.id);
        $("#job_depend_type").html(jobData.jobRunType);
        $("#job_depend_name").html(jobData.name);
        $("#job_depend_schedule_type").html(jobData.jobScheduleType);
        $("#job_depend_owner").html(jobData.owner);
        $("#job_depend_open_status").html(jobData.auto?'打开':'关闭');
        $("#job_depend_desc").html(jobData.desc);
        $("#job_depend_jobs").html('['+jobData.dependencies+']');
        $("#job_depend_key_contact").html(jobData.importantContacts);
        $("#job_depend_span").html(jobData.jobCycle);
        $("#job_depend_notice_person").html(jobData.follows);
        $("#job_depend_priority_level").html(jobData.runPriorityLevel);
        $("#job_depend_administrator").html(jobData.admins);
        $("#job_depend_retry_time").html(jobData.rollBackTimes);
        $("#job_depend_host_group_id").html(jobData.hostGroupId);
        $("#job_depend_retry_time_span").html(jobData.rollBackWaitTime);
        $("#job_depend_run_time_span").html(jobData.maxTime);

        $("#job_depend_script").html(jobData.script);
        var resources = "";
        if (jobData.localResources.length > 0) {
            for (var i = 0; i < jobData.localResources.length; i++) {
                resources += "\n" + jobData.localResources[i].name;
            }
        }
        $("#job_depend_resource").html(resources);

        //TODO
        //配置项信息
        //资源信息
        //继承的配置项信息
        //继承的资源信息

        var postData = {
            "id": jobData.hostGroupId
        };
        $.ajax({
            type: "GET",
            url: "job/get_host_group_name_by_id",
            data: postData,
            success: function (ret) {
                if (ret != null) {
                    if (ret.msg == "success") {
                        $("#job_depend_host_group_name").html(ret.data);
                    } else {
                        $.messager.alert('警告', '获取任务组名字失败', 'info');
                    }
                }
            }
        });
    }
}

function bindGroupContent(groupData) {
    $('#group_panel').show();
    $("#group_id").html(groupData.id);
    $("#group_name").html(groupData.name);
    $("#group_owner").html(groupData.ownerName);
    $("#group_desc").html(groupData.desc);
    $("#group_notice_person").html(groupData.follows);
    $("#group_administrator").html(groupData.admins);
    //TODO
    //配置项信息
    //资源信息
    //继承的配置项信息
    //继承的资源信息
}

function openWholeVeiw() {
    $('#whole_view_dialog').dialog('open').dialog('setTitle', '任务总览');
    initWholeViewContent();
}

function initWholeViewContent() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;

    $("#whole_view_table").datagrid({
        title: '任务总览',
        iconCls: 'icon-ok',
        width: '100%',
        height: 700,
        animate: true,
        collapsible: true,
        fitcolumns: true,
        singleSelect: true,
        pagination: true,
        striped: true,
        pageSize: 20,
        pageList: [20, 40, 60, 100, 1000],
        url: "job/get_job_status_by_groupId",
        queryParams: {
            groupId: groupId
        },
        idField: 'id',
        showFooter: false,
        columns: [[{
            field: 'id',
            title: '实例Id',
            width: 400,
            align: 'center'
        }, {
            field: 'jobId',
            title: '任务Id',
            width: 200,
            align: 'center'
        }, {
            field: 'name',
            title: '任务名称',
            width: 200,
            align: 'center'
        }, {
            field: 'status',
            title: '执行状态',
            width: 200,
            align: 'center'
        }, {
            field: 'jobCycle',
            title: '依赖状态',
            width: 200,
            align: 'center',
            formatter: function (value, row, index) {
                if (value.indexOf('未执行') != -1) {
                    var s = "<label style='color:red;'>" + value + "</label>";
                    return s;
                } else {
                    return '';
                }
            }
        }, {
            field: 'lastStatus',
            title: '上一次任务情况',
            width: 200,
            align: 'center'
        }]],
        onLoadError: function () {
            $.messager.alert('警告', '加载任务总览数据出错', 'info');
        }
    });
}

function searchWholeView() {
    var startDate = $("#whole_view_start_date").getValue();
    var endDate = $("#whole_view_end_date").getValue();
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;

    $('#whole_view_table').datagrid('load', {
        groupId: groupId,
        startDate: startDate,
        endDate: endDate
    });
}

function addGroup() {
    $('#add_group_dialog').dialog('open').dialog('setTitle', '新建组');
}

function addGroupConfirmed() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;

    var groupName = $('#add_group_name').val();
    var groupType = $('#add_group_type').combobox('getValue');

    var postData = {
        "groupName": groupName,
        "parentGroupId": groupId,
        isDirectory: groupType == 0 ? true : false
    };
    $.ajax({
        type: "GET",
        url: "group/create_group",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    initialTree();
                    $('#add_group_dialog').dialog('close');
                } else {
                    $.messager.alert('警告', '创建我的组节点信息失败', 'info');
                }
            }
        }
    });
}

function editGroup() {
    $('#edit_group_dialog').dialog('open').dialog('setTitle', '编辑组');
}

function editGroupConfirmed() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;
    var groupName = $('#edit_group_name').val();
    var groupDesc = $('#edit_group_desc').val();
    var groupConfigInfo = $('#edit_group_config_info').val();
    var groupResourceInfo = $('#edit_group_resource_info').val();

    var postData = {
        "groupId": groupId,
        "groupName": groupName,
        "groupDesc": groupDesc,
        "groupConfigInfo": groupConfigInfo,
        "groupResourceInfo": groupResourceInfo
    };
    $.ajax({
        type: "GET",
        url: "group/edit_group",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    initialTree();
                } else {
                    $.messager.alert('警告', '编辑我的组节点信息失败', 'info');
                }
            }
        }
    });
}

function deleteGroup() {
    var node = $('#all_jobs').tree('getSelected');
    var name = node.text;
    $('#delete_group_name').html(name);
    $('#delete_group_dialog').dialog('open').dialog('setTitle', '删除组');
}

function deleteGroupConfirmed() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;
    var postData = {
        "groupId": groupId
    };

    $.ajax({
        type: "GET",
        url: "group/delete_group",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#delete_group_dialog').dialog('close');
                    initialTree();
                } else {
                    $.messager.alert('警告', "删除失败，错误信息：" + ret.data, 'info');
                }
            }
        }
    });
}

function addJob() {
    $('#add_job_dialog').dialog('open').dialog('setTitle', '新建任务');
}

function addJobConfirm() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;

    var jobName = $('#add_job_name').val();
    var jobType = $('#add_job_type').combobox('getValue');

    var postData = {
        "jobName": jobName,
        "jobType": jobType,
        "parentGroupId": groupId
    };
    $.ajax({
        type: "GET",
        url: "job/create_job",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    initialTree();
                } else {
                    $.messager.alert('警告', '创建我的任务失败', 'info');
                }
            }
        }
    });
}

function configGroupAdministor() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;
    var postData = {
        "groupId": groupId
    };

    $.ajax({
        type: "GET",
        url: "group/get_group_admins_string",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#config_admins').val(ret.data);
                } else {
                    $.messager.alert('警告', '获取管理员信息失败', 'info');
                }
            }
        }
    });

    $('#config_admins_dialog').dialog('open').dialog('setTitle', '配置管理员');
}

function addGroupAdmins() {
    $('#add_admins').combobox('reload', 'group/get_group_admins')
    $('#add_admins_dialog').dialog('open').dialog('setTitle', '添加管理员');
}

function addGroupAdminsConfirm() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;
    var uid = $('#add_admins').combobox('getValue');
    var postData = {
        "groupId": groupId,
        "uid": uid
    };

    $.ajax({
        type: "GET",
        url: "group/add_group_admin",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    configGroupAdministor();
                } else {
                    $.messager.alert('警告', '添加管理员信息失败', 'info');
                }
            }
        }
    });
}

function deleteGroupAdmins() {
    $('#delete_admins').combobox('reload', 'group/get_group_admins')
    $('#delete_admins_dialog').dialog('open').dialog('setTitle', '删除管理员');
}

function deleteGroupAdminsConfirm() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;
    var uid = $('#delete_admins').combobox('getValue');
    var postData = {
        "groupId": groupId,
        "uid": uid
    };

    $.ajax({
        type: "GET",
        url: "group/delete_group_admin",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    configGroupAdministor();
                } else {
                    $.messager.alert('警告', '删除管理员信息失败', 'info');
                }
            }
        }
    });
}

function transferAdmin() {
    $('#transfer_admins').combobox('reload', 'group/get_group_admins')
    $('#transfer_admins_dialog').dialog('open').dialog('setTitle', '转让所有权');
}

function transferGroupAdminsConfirm() {
    var node = $('#all_jobs').tree('getSelected');
    var groupId = node.attributes.id;
    var uid = $('#transfer_admins').combobox('getValue');
    var postData = {
        "groupId": groupId,
        "uid": uid
    };

    $.ajax({
        type: "GET",
        url: "group/transfer_owner",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    configGroupAdministor();
                } else {
                    $.messager.alert('警告', '转让组所有权失败', 'info');
                }
            }
        }
    });
}

function openRunLogs() {
    $('#run_log_dialog').dialog('open').dialog('setTitle', '历史运行日志');
    initRunLogContent();
}

function initRunLogContent() {
    var node = $('#all_jobs').tree('getSelected');
    var jobId = node.attributes.id;

    $("#run_log_table").datagrid({
        title: '历史记录',
        iconCls: 'icon-ok',
        width: '100%',
        height: 700,
        animate: true,
        collapsible: true,
        fitcolumns: true,
        singleSelect: true,
        pagination: true,
        striped: true,
        pageSize: 20,
        pageList: [20, 40, 60, 100, 1000],
        url: "job/get_job_history_list",
        queryParams: {
            jobId: jobId
        },
        idField: 'id',
        showFooter: false,
        columns: [[{
            field: 'id',
            title: 'id',
            width: 50,
            align: 'center'
        },
            {
                field: 'actionId',
                title: '实例Id',
                width: 160,
                align: 'center'
            }, {
                field: 'jobId',
                title: '任务Id',
                width: 100,
                align: 'center'
            }, {
                field: 'startTimeStr',
                title: '开始时间',
                width: 150,
                align: 'center'
            }, {
                field: 'endTimeStr',
                title: '结束时间',
                width: 150,
                align: 'center'
            },
            {
                field: 'executeHost',
                title: '执行服务器',
                width: 110,
                align: 'center'
            },
            {
                field: 'status',
                title: '执行状态',
                width: 80,
                align: 'center'
            },
            {
                field: 'operator',
                title: '执行人',
                width: 80,
                align: 'center'
            },
            {
                field: 'triggerTypeStr',
                title: '触发类型',
                width: 100,
                align: 'center'
            }, {
                field: 'illustrate',
                title: '说明',
                width: 100,
                align: 'center'
            },
            {
                field: 'cycle',
                title: '任务周期',
                width: 100,
                align: 'center'
            },
            {
                field: 'instanceAction',
                title: '操作',
                width: 150,
                align: 'center',
                formatter: function (value, row, index) {
                    if (row.id) {
                        if (row.status == 'running') {
                            var s = "<label style='color:blue;cursor:pointer;' onclick=\"showRunLog('" + row.id + "')\">" + "查看日志" + "</label>";
                            s=s+"<label style='color:blue;cursor:pointer;margin-left: 10px;' onclick=\"cancelAction('" + row.id + "')\">" + "取消任务" + "</label>";
                            return s;
                        } else {
                            var s = "<label style='color:blue;cursor:pointer;' onclick=\"showRunLog('" + row.id + "')\">" + "查看日志" + "</label>";
                            return s;
                        }
                    } else {
                        return '';
                    }
                }
            }]],
        onLoadError: function () {
            $.messager.alert('警告', '加载任务日志数据出错', 'info');
        }
    });
}

function refreshJobLog() {
    var node = $('#all_jobs').tree('getSelected');
    var jobId = node.attributes.id;
    $('#run_log_table').datagrid('load', {
        jobId: jobId
    });
}

function showRunLog(id) {
    var postData = {
        "id": id
    };
    $.ajax({
        type: "GET",
        url: "job/get_job_history_by_id",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#job_log_label').html(ret.data.log.replace(/\n/g, '</br>'));
                    $('#job_log_dialog').dialog('open').dialog('setTitle', '任务id:' + ret.data.id + '-->实例id:' + ret.data.actionId);

                } else {
                    $.messager.alert('警告', '获取日志内容失败', 'info');
                }
            }
        }
    });
}

function cancelAction(id){
    $.messager.confirm('确认', '你确认取消该任务吗？任务id: ' + id + '?', function(r) {
        if (r) {
            var postData = {
                "id": id
            };
            $.ajax({
                type: "GET",
                url: "job/cancel",
                data: postData,
                success: function (ret) {
                    if (ret != null) {
                        if (ret.msg == "success") {

                        } else {
                            $.messager.alert('警告', '取消任务失败', 'info');
                        }
                    }
                }
            });
        }
    });
}

function openRelyMap(){
    $('#job_rely_dialog').dialog('open').dialog('setTitle', '任务依赖');
    var node = $('#all_jobs').tree('getSelected');
    var jobId = node.attributes.id;

    var postData = {
        "jobId": jobId
    };
    var dependeeChart = echarts.init(document.getElementById('job_dependee'));
    var dependerChart = echarts.init(document.getElementById('job_depender'));

    var option = {
        series : [
            {
                type: 'graph',
                layout: 'none',
                symbolSize: 20,
                roam: true,
                label: {
                    normal: {
                        show: true,
                        color:'#333',
                        fontSize:10
                    }
                },
                edgeSymbol: ['circle', 'arrow'],

                data: [],
                links: []
            }
        ]
    };

    dependeeChart.setOption(option);
    dependerChart.setOption(option);

    $.ajax({
        type: "GET",
        url: "tree/get_dependee_tree",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    dependeeChart.setOption({
                        series: [{
                            data:getRelationData(ret.data,-1),
                            links: getLinks(ret.data,-1)
                        }]
                    });


                    $.ajax({
                        type: "GET",
                        url: "tree/get_depender_tree",
                        data: postData,
                        success: function (ret) {
                            if (ret != null) {
                                if (ret.msg == "success") {
                                    dependerChart.setOption({
                                        series: [{
                                            data:getRelationData(ret.data,1),
                                            links: getLinks(ret.data,1)
                                        }]
                                    });
                                } else {
                                    $.messager.alert('警告', '获取依赖失败', 'info');
                                }
                            }
                        }
                    });
                } else {
                    $.messager.alert('警告', '获取依赖失败', 'info');
                }
            }
        }
    });

}

var dataNodes=[];
var tempNodes=[];
var floor = 0;
var level = 0;
var baseX=300;
var baseY=300;
var rootX = 0;
var rootY = 0;
//direction 子节点的发展方向
function getRelationData(data,direction){
    dataNodes=[];
    tempNodes=[];
    floor=0;
    level=0;
    baseX=300;
    baseY=300;
    getNodesData(data,floor,level,direction);

    return dataNodes;
}

function getNodesData(data,floor,level,direction){
    floor++;
    rootX = baseX + 50*floor;
    rootY = baseY + 30*level;
    if(data.name){
        dataNodes.push({name: data.name+'_'+data.id,x: rootX,y: rootY});
    }

    if (data.children) {
        if(direction<0){
            level--;
        }else{
            level++;
        }

        var i= -1 - data.children.length/2;
        for(var p in data.children){
            getNodesData(data.children[p],floor+i,level+Math.random()*0.5,direction);
            i++;
        }
    }

    return ;
}

var links=[];
function getLinks(data,direction){
    getNodeLinks(data,direction);
    return links;
}

function getNodeLinks(data,direction){
    if(direction<0){
        if (data.children) {
            for(var p in data.children){
                links.push({source: data.children[p].name+'_'+data.children[p].id,target: data.name+'_'+data.id});
                if (data.children[p].children) {
                    getNodeLinks(data.children[p],direction);
                }
            }
        }
    }else{
        if (data.children) {
            for(var p in data.children){
                links.push({source: data.name+'_'+data.id,target: data.children[p].name+'_'+data.children[p].id});
                if (data.children[p].children) {
                    getNodeLinks(data.children[p],direction);
                }
            }
        }
    }

}

function editJob() {
    var node = $('#all_jobs').tree('getSelected');
    var jobId = node.attributes.id;
    var scheduleType = node.scheduleType;
    //定时任务的情况下
    if (scheduleType == 0) {
        var postData = {
            "jobId": jobId
        };
        $.ajax({
            type: "GET",
            url: "job/get_job_by_id",
            data: postData,
            success: function (ret) {
                if (ret != null) {
                    if (ret.msg == "success") {
                        ret.data.zeusSecretScript = ret.data.zeusSecretScript?'true':'false';
                        ret.data.resources = parseResources(ret.data.resources);
                        $('#edit_job_form').form('load', ret.data);
                    } else {
                        $.messager.alert('警告', '获取任务信息失败', 'info');
                    }
                }
            }
        });
        $('#edit_schedule_job_dialog').dialog('open').dialog('setTitle', '编辑任务');
    }
    //依赖任务的情况下
    else if (scheduleType == 1) {
        var postData = {
            "jobId": jobId
        };
        $.ajax({
            type: "GET",
            url: "job/get_job_by_id",
            data: postData,
            success: function (ret) {
                if (ret != null) {
                    if (ret.msg == "success") {
                        ret.data.zeusSecretScript = ret.data.zeusSecretScript?'true':'false';
                        ret.data.resources = parseResources(ret.data.resources);
                        $('#edit_depend_job_form').form('load', ret.data);
                    } else {
                        $.messager.alert('警告', '获取任务信息失败', 'info');
                    }
                }
            }
        });
        $('#edit_depend_job_dialog').dialog('open').dialog('setTitle', '编辑任务');
    }
}

function parseResources(obj){
    obj = JSON.parse(obj);
    var ret = '';
    for(var p in obj){
        ret += '{"name":' +'"'+ obj[p].name+'"' + ',' + '"uri":' +'"'+ obj[p].uri+'"}' + '\n';
    }
    return ret;
}

function openHostGroupInfo(obj){
    $('#host_group_dialog').dialog('open').dialog('setTitle', 'host分组信息');
    $("#host_group_grid").datagrid({
        iconCls : 'icon-ok',
        width : 500,
        height : 300,
        animate : true,
        collapsible : true,
        singleSelect : true,
        striped:true,
        queryParams: {
            id: obj.val()
        },
        url : "job/get_host_group_by_id",
        idField : 'id',
        showFooter : false,
        columns : [ [ {
            field : 'id',
            title : 'id',
            width : 50,
            align : 'center'
        }, {
            field : 'name',
            title : '组名',
            width : 200,
            align : 'center'
        }, {
            field : 'description',
            title : '描述',
            width : 200,
            align : 'center'
        } ] ],
        onLoadError : function() {
            $.messager.alert('出错', '加载host组信息失败', 'error');
        }
    });
}

function selectHostGroupConfirm(){
    var row = $("#host_group_grid").datagrid("getSelected");
    $("#edit_host_group_id").val(row.id);
    $('#host_group_dialog').dialog('close');
}

function editJobConfirm() {
    var node = $('#all_jobs').tree('getSelected');
    var jobId = node.attributes.id;

    var name = $('#edit_job_name').val();
    var scheduleType = $('#edit_schedule_type').val();
    var failRetryTimes = $('#edit_fail_retry_times').val();
    var cron = $('#edit_cron_string').val();
    var retrySpan = $('#edit_fail_retry_span').val();
    var hostGroupId = $('#edit_host_group_id').val();
    var priority = $('#edit_priority').val();
    var scriptVisible = $('#edit_script_visible').val();
    var desc = $('#edit_desc').val();
    var runTimeSpan = $('#edit_run_time_span').val();
    var config = $('#edit_job_config').val();
    var script = $('#edit_job_script').textbox('getValue');
    var resource = $('#edit_resource').textbox('getValue');
    var owner = $('#edit_job_owner').val();
    var groupId=$('#edit_group_id').val();
    var runType = $('#edit_run_type').val();
    var cycle = $('#edit_cycle').val();
    var auto = $('#edit_auto').val();
    var offset = $('#edit_offset').val();
    var timezone =$('#edit_timezone').val();

    var postData = {
        "jobId": jobId,
        "name": name,
        "scheduleType": scheduleType,
        "failRetryTimes": failRetryTimes,
        "cron": cron,
        "retrySpan": retrySpan,
        "hostGroupId": hostGroupId,
        "priority": priority,
        "scriptVisible": scriptVisible,
        "desc": desc,
        "runTimeSpan": runTimeSpan,
        "config": config,
        "script": script,
        "resource": resource,
        "owner":owner,
        "groupId":groupId,
        "runType":runType,
        "cycle":cycle,
        "auto":auto,
        "offset":offset,
        "timezone":timezone
    };
    $.ajax({
        type: "POST",
        url: "job/update_job",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#edit_schedule_job_dialog').dialog('close');
                    getNodeInfo(activeNode);
                } else {
                    $.messager.alert('警告', '更新任务信息失败', 'info');
                }
            }
        }
    });
}

function uploadResourceDialog() {
    $('#upload_resource_dialog').dialog('open').dialog('setTitle', '上传资源文件');
}

function resetResource(){
    $('#resource_file').val('');
}

function uploadResource(){
    var newItem = '{"name":"' + getFileName($('#resource_file').val())+ '",' +'"uri":'+'"hdfs://' + $('#hdfsLibPath').val()
        +'/'+ getFileName($('#resource_file').val()) + '"}';
    var allItems = $('#edit_resource').textbox('getValue')+ '\n' + newItem;
    $('#edit_resource').textbox('setValue', allItems);
    $.ajax({
        type: "POST",
        url: "upload.do",
        cache: false,
        processData: false,
        contentType: false,
        data: new FormData($("#uploadForm")[0]),

        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {

                } else {
                    $.messager.alert('警告', '上传文件失败', 'info');
                }
            }
        }
    });
}

function getFileName(input){
var pos = input.lastIndexOf('\\');
return input.substring(pos+1);
}

function editDependJobConfirm() {
    var node = $('#all_jobs').tree('getSelected');
    var jobId = node.attributes.id;

    var name = $('#edit_depend_job_name').val();
    var scheduleType = $('#edit_depend_schedule_type').val();
    var failRetryTimes = $('#edit_depend_fail_retry_times').val();
    var cron = '';
    var retrySpan = $('#edit_depend_fail_retry_span').val();
    var hostGroupId = $('#edit_depend_host_group_id').val();
    var priority = $('#edit_depend_priority').val();
    var scriptVisible = $('#edit_depend_script_visible').val();
    var desc = $('#edit_depend_desc').val();
    var runTimeSpan = $('#edit_depend_run_time_span').val();
    var config = $('#edit_depend_job_config').val();
    var script = $('#edit_depend_job_script').textbox('getValue');
    var resource = $('#edit_depend_resource').textbox('getValue');
    var owner = $('#edit_depend_job_owner').val();
    var groupId=$('#edit_depend_group_id').val();
    var runType = $('#edit_depend_run_type').val();
    var cycle = $('#edit_depend_cycle').val();
    var auto = $('#edit_depend_auto').val();
    var offset = $('#edit_depend_offset').val();
    var timezone =$('#edit_depend_timezone').val();
    var dependencies = $('#edit_depend_jobs').val();

    var postData = {
        "jobId": jobId,
        "name": name,
        "scheduleType": scheduleType,
        "failRetryTimes": failRetryTimes,
        "cron": cron,
        "retrySpan": retrySpan,
        "hostGroupId": hostGroupId,
        "priority": priority,
        "scriptVisible": scriptVisible,
        "desc": desc,
        "runTimeSpan": runTimeSpan,
        "config": config,
        "script": script,
        "resource": resource,
        "owner":owner,
        "groupId":groupId,
        "runType":runType,
        "cycle":cycle,
        "auto":auto,
        "offset":offset,
        "timezone":timezone,
        "dependencies":dependencies
    };
    $.ajax({
        type: "POST",
        url: "job/update_job",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#edit_depend_job_dialog').dialog('close');
                    getNodeInfo(activeNode);
                } else {
                    $('#edit_depend_job_dialog').dialog('close');
                    getNodeInfo(activeNode);
                    $.messager.alert('警告', '更新任务信息失败', 'info');
                }
            }
        }
    });
}

function handleRun() {
    $('#handle_run_dialog').dialog('open').dialog('setTitle', '选择实例版本');
    var jobId = activeNode.id;
    $('#handle_run_actions').combobox({
        url: 'action/get_action_list_by_job_id',
        queryParams: {'jobId':jobId},
        valueField: 'id',
        textField: 'text'
    });
}

function handleRunConfirmed(){
    var actionId = $('#handle_run_actions').combobox('getText');
    var postData = {
        "actionId": actionId,
        "type":1,
    };

    $.ajax({
        type: "GET",
        url: "job/handle_run",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $.messager.show({
                        title:'成功',
                        msg:'手动执行任务开始',
                        timeout:2000,
                        showType:'slide',
                        top:'10px',
                        left:'500px'
                    });
                } else {
                    $.messager.alert('警告', '手动执行失败', 'info');
                }
            }
        }
    });
    $('#handle_run_dialog').dialog('close');
}

function openDependDialog(){
    var actionRow;
    $('#search_job_name').textbox('clear');
    $('#search_job_name').textbox({
        onChange:function (newValue, oldValue) {
            var url = 'job/get_job_like_name?jobName=' + newValue;
            $('#all_job_info').datalist({
                url: url,
                lines: true,
                onDblClickRow:function (index,row) {
                    actionRow = row;
                    $('#all_job_info').datalist('deleteRow',index);
                    actionRow.group="1";
                    $('#rely_job_info').datalist('appendRow',actionRow);

                }
            });
        }
    });

    var url = 'job/get_job_rely?jobId='+ activeNode.id;

    $('#rely_job_info').datalist({
        url: url,
        lines: true,
        rowStyler: function(index,row){
            if (row.group!=null){
                return 'background-color:#6293BB;color:#fff;';
            }
        },
        onDblClickRow:function (index,row) {
            actionRow = row;
            $('#rely_job_info').datalist('deleteRow',index);

            $('#all_job_info').datalist('appendRow',actionRow);
        }
    });

    $('#job_depend_dialog').dialog('open').dialog('setTitle', '任务依赖');
}

function okJobDepend(){
    var rows = $('#rely_job_info').datalist('getRows');
    var relyJobs='';
    if (rows.length > 0) {
        for (var i = 0; i < rows.length; i++) {
            if(relyJobs!=''){
                relyJobs = relyJobs + ',' +rows[i].value ;
            }else{
                relyJobs = rows[i].value;
            }
        }
    }
    $('#edit_depend_jobs').val(relyJobs);
    $('#job_depend_dialog').dialog('close');
}

function handleRecover() {
    $('#handle_recover_dialog').dialog('open').dialog('setTitle', '选择实例版本');
    var jobId = activeNode.id;
    $('#handle_recover_actions').combobox({
        url: 'action/get_action_list_by_job_id',
        queryParams: {'jobId':jobId},
        valueField: 'id',
        textField: 'text'
    });
}

function handleRecoverConfirmed(){
    var actionId = $('#handle_recover_actions').combobox('getText');
    var postData = {
        "actionId": actionId,
        "type":2,
    };

    $.ajax({
        type: "GET",
        url: "job/handle_run",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $.messager.show({
                        title:'成功',
                        msg:'手动恢复任务开始',
                        timeout:2000,
                        showType:'slide',
                        top:'10px',
                        left:'500px'
                    });
                } else {
                    $.messager.alert('警告', '手动恢复执行失败', 'info');
                }
            }
        }
    });
    $('#handle_recover_dialog').dialog('close');
}

function openOrClose() {
    var node = $('#all_jobs').tree('getSelected');
    var jobId = node.attributes.id;
    var scheduleType = node.scheduleType;
    var auto = false;
    //定时任务的情况下
    if (scheduleType == 0) {
        auto = $('#job_open_status').html()=='打开'?false:true;
    }else{
        auto = $('#job_depend_open_status').html()=='打开'?false:true;
    }
    var postData = {
        "jobId": jobId,
        "auto":auto,
    };

    $.ajax({
        type: "GET",
        url: "job/open_or_close",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "header") {
                    if(ret.data !=null && ret.data!=''){
                        $.messager.alert('警告', '上游有任务关闭，本任务不能开启！ ' + ret.data, 'info');
                    }
                }else if(ret.msg == "tail"){
                    if(ret.data !=null && ret.data!=''){
                        $.messager.alert('警告', '下游有任务开启，本任务不能关闭！ ' + ret.data, 'info');
                    }
                } else if (ret.msg == "success"){
                    getNodeInfo(activeNode);
                    $.messager.show({
                        title:'成功',
                        msg:'执行成功',
                        timeout:2000,
                        showType:'slide',
                        top:'10px',
                        left:'500px'
                    });
                }else {
                    $.messager.alert('警告', '打开/关闭 执行失败', 'info');
                }
            }
        }
    });
    $('#handle_recover_dialog').dialog('close');
}

function deleteJob() {
    $.messager.confirm('确认', '你确认删除该任务吗？任务: ' + activeNode.text + '?', function(r) {
        if (r) {
            var postData = {
                "jobId": activeNode.id
            };
            $.ajax({
                type: "GET",
                url: "job/delete",
                data: postData,
                success: function (ret) {
                    if (ret != null) {
                        if (ret.msg == "success") {

                        } else {
                            $.messager.alert('警告', '删除任务失败,' + ret.data, 'info');
                        }
                    }
                }
            });
        }
    });
}

function configJobOwner() {
    $('#job_owner_dialog').dialog('open').dialog('setTitle', '配置所有人');
    $('#job_owners').combobox('reload', 'user_manager/get_all_users');
    $('#job_owners').combobox('setValue', activeNodeInfo.owner);
}

function updateOwner() {
    var uid = $('#job_owners').combobox('getText');
    var postData = {
        "jobId": activeNode.id,
        "uid":uid
    };
    $.ajax({
        type: "GET",
        url: "job/transfer_owner",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#job_owner_dialog').dialog('close');
                    getNodeInfo(activeNode);
                } else {
                    $.messager.alert('警告', '更新所有人失败!');
                }
            }
        }
    });
}

function configJobAdmin() {
    $('#config_job_admin_dialog').dialog('open').dialog('setTitle', '配置管理员');

    var jobId = activeNode.id;
    $("#job_admin_grid").datagrid({
        title: '管理员',
        iconCls: 'icon-ok',
        width: '100%',
        height: 300,
        animate: true,
        collapsible: true,
        singleSelect: true,
        toolbar : "#config_job_admin_grid-toolbar",
        striped: true,
        url: "job/get_job_admins",
        queryParams: {
            jobId: jobId
        },
        idField: 'id',
        showFooter: false,
        columns: [[{
            field: 'uid',
            title: 'uid',
            width: 100,
            align: 'center'
        }, {
            field: 'name',
            title: '用户名',
            width: 200,
            align: 'center'
        }]],
        onLoadError: function () {
            $.messager.alert('警告', '加载管理员失败!', 'info');
        }
    });
}

function addJobAdminDialog(){
    $('#add_job_admin_dialog').dialog('open').dialog('setTitle', '选择用户');
    $('#add_job_admins').combobox('reload', 'user_manager/get_all_users');

}

function addJobAdmin(){
    var jobId = activeNode.id;
    var uid = $('#add_job_admins').combobox('getText');

    var postData = {
        "jobId": jobId,
        "uid":uid
    };
    $.ajax({
        type: "GET",
        url: "job/add_job_admin",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#add_job_admin_dialog').dialog('close');
                    $('#job_admin_grid').datagrid('load', {jobId : activeNode.id});
                } else {
                    $.messager.alert('警告', '添加管理员失败!');
                }
            }
        }
    });
}

function deleteJobAdminDialog() {
    var row = $('#job_admin_grid').datagrid('getSelected');

    if (row) {
        $.messager.confirm('确认', '删除: ' + row.uid + '?', function(r) {
            if (r) {
                $.get("job/remove_job_admin", {
                    jobId : activeNode.id,
                    uid:row.uid
                }, function(ret) {
                        if (ret.msg == "success") {
                            $('#job_admin_grid').datagrid('reload');
                        } else {
                            $.messager.alert('警告', '删除管理员失败!');
                        }
                }, 'json');
            }
        });
    }
}

function configJobConfirm(){
    $('#config_job_admin_dialog').dialog('close');
    getNodeInfo(activeNode);
}

function followWith() {
        var type=2;
        if(!activeNode.attributes.job){
            type=1;
        }
        var jobId = activeNode.id;
        var postData = {
            "type": type,
            "jobId":jobId
        };
        $.ajax({
            type: "GET",
            url: "job/follow",
            data: postData,
            success: function (ret) {
                if (ret != null) {
                    if (ret.msg == "success") {
                        getNodeInfo(activeNode);
                    } else {
                        $.messager.alert('警告', '添加关注失败!');
                    }
                }
            }
        });
}

function unFollowWith() {
        var type=2;
        if(!activeNode.attributes.job){
            type=1;
        }
        var jobId = activeNode.id;
        var postData = {
            "type": type,
            "jobId":jobId
        };
        $.ajax({
            type: "GET",
            url: "job/unfollow",
            data: postData,
            success: function (ret) {
                if (ret != null) {
                    if (ret.msg == "success") {
                        getNodeInfo(activeNode);
                    } else {
                        $.messager.alert('警告', '取消关注失败!');
                    }
                }
            }
        });
}

function configJobContact(){
    $('#config_job_contact_dialog').dialog('open').dialog('setTitle', '配置联系人');

    var jobId = activeNode.id;
    $("#job_contact_grid").datagrid({
        title: '联系人',
        iconCls: 'icon-ok',
        width: '100%',
        height: 300,
        animate: true,
        collapsible: true,
        singleSelect: true,
        toolbar : "#config_job_contact_grid-toolbar",
        striped: true,
        url: "job/get_imp_contacts_by_jobId",
        queryParams: {
            jobId: jobId
        },
        idField: 'id',
        showFooter: false,
        columns: [[{
            field: 'uid',
            title: 'uid',
            width: 100,
            align: 'center'
        }, {
            field: 'name',
            title: '用户名',
            width: 200,
            align: 'center'
        }]],
        onLoadError: function () {
            $.messager.alert('警告', '加载联系人失败!', 'info');
        }
    });
}

function addJobContactDialog(){
    $('#add_job_contact_dialog').dialog('open').dialog('setTitle', '选择用户');
    var jobId = activeNode.id;
    $('#add_job_contact').combobox({
        url: 'job/get_not_contacts_by_jobId',
        queryParams: {'jobId':jobId},
        valueField: 'uid',
        textField: 'name'
    });
}

function deleteJobContactDialog() {
    var row = $('#job_contact_grid').datagrid('getSelected');

    if (row) {
        $.messager.confirm('确认', '删除: ' + row.uid + '?', function(r) {
            if (r) {
                $.get("job/revoke_important", {
                    jobId : activeNode.id,
                    uid : row.uid
                }, function(ret) {
                    if (ret.msg == "success") {
                        $('#job_contact_grid').datagrid('reload');
                    } else {
                        $.messager.alert('警告', '删除联系人失败!');
                    }
                }, 'json');
            }
        });
    }
}

function addJobContact() {
    var uid = $('#add_job_contact').combobox('getValue');
    var jobId = activeNode.id;
    var postData = {
        "uid": uid,
        "jobId":jobId
    };
    $.ajax({
        type: "GET",
        url: "job/grant_important",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    getNodeInfo(activeNode);
                    $('#job_contact_grid').datagrid('reload');
                    $('#add_job_contact_dialog').dialog('close');
                } else {
                    $.messager.alert('警告', '添加联系人失败!');
                }
            }
        }
    });
}
