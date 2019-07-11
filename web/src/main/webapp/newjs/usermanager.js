$(function() {
    showAccordin();
    initialProgress();
    initialAuthority();
    $('#search_btn').bind('click', function() {
        $('#user_manager_grid').datagrid('load', {
            user : $('#uid').val()
        });
    });
});

function showAccordin() {
    var objId=$.Request("objId");
    $('#' + objId).addClass('leftbackground');
}

function initialAuthority(){
    var user=$('#h_user_type').val();
    if (user != 0) {
        $('#a_ok').hide();
        $('#a_no').hide();
        $('#a_delete').hide();
    }
}

function initialProgress() {
    $("#user_manager_grid").datagrid({
        title : '用户管理',
        iconCls : 'icon-ok',
        width : 1000,
        height : 500,
        animate : true,
        collapsible : true,
        singleSelect : true,
        pagination : true,
        striped:true,
        pageSize:20,
        pageList : [ 20, 40, 60, 100, 1000 ],
        toolbar : "#user_manager_grid_toolbar",
        url : "user_manager/get_all_users_by_page",
        idField : 'id',
        showFooter : false,
        columns : [ [ {
            field : 'uid',
            title : '用户账号',
            width : 100,
            align : 'center'
        }, {
            field : 'name',
            title : '用户姓名',
            width : 100,
            align : 'center'
        }, {
            field : 'isEffective',
            title : '用户状态',
            width : 100,
            align : 'center',
            formatter : function(value, row, index) {
                if (value&&value>0 ) {
                    return "审核通过";
                } else {
                    return '审核拒绝';
                }
            }

        }, {
            field : 'userType',
            title : '用户类型',
            width : 100,
            align : 'center',
            formatter : function(value, row, index) {
                if (value&&value>0 ) {
                    return "个人用户";
                } else {
                    return '组用户';
                }
            }
        }, {
            field : 'email',
            title : '邮箱',
            width : 150,
            align : 'center'
        }, {
            field : 'phone',
            title : '手机号码',
            width : 100,
            align : 'center'
        }, {
            field : 'description',
            title : '描述',
            width : 200,
            align : 'center'
        }, {
            field : 'modifiedTime',
            title : '更新日期',
            width : 150,
            align : 'center'
        } ] ],
        onLoadError : function() {
            $.messager.alert('出错', '加载用户失败', 'error');
        }
    });}

function openOkDialog() {
    var row = $('#user_manager_grid').datagrid('getSelected');

    if (row) {
        $.messager.confirm('确认', '审核通过: ' + row.uid + '?', function(r) {
            if (r) {
                $.post("user_manager/ok", {
                    uid : row.uid
                }, function(data) {
                    if (data != null) {
                        if (data.msg == "success") {
                            $('#user_manager_grid').datagrid('unselectAll');
                            $('#user_manager_grid').datagrid('reload');
                        } else {
                            $.messager.alert('出错', '审核失败', 'error');
                        }
                    }
                }, 'json');
            }
        });
    }
}

function openNoDialog() {
    var row = $('#user_manager_grid').datagrid('getSelected');

    if (row) {
        $.messager.confirm('确认', '审核拒绝: ' + row.uid + '?', function(r) {
            if (r) {
                $.post("user_manager/no", {
                    uid : row.uid
                }, function(data) {
                    if (data != null) {
                        if (data.msg == "success") {
                            $('#user_manager_grid').datagrid('unselectAll');
                            $('#user_manager_grid').datagrid('reload');
                        } else {
                            $.messager.alert('出错', '审核失败', 'error');
                        }
                    }
                }, 'json');
            }
        });
    }
}
function openEditDialog() {
    var row = $('#user_manager_grid').datagrid('getSelected');
    if (row) {
        $('#edit_dialog').dialog('open').dialog('setTitle', '编辑');

        $('#edit_form').form('load', row);
    }
}

function saveEdit() {
    var ret = $('#edit_form').form('validate');
    if (ret) {
        var uid = $('#edit_uid').val();
        var passwd = $('#edit_passwd').val();
        var email = $('#edit_email').val();
        var phone = $('#edit_phone').val();

        var postData = {
            "uid" : uid,
            "passwd" : passwd,
            "email" : email,
            "phone" : phone
        };

        $.ajax({
            type : "POST",
            url : "user_manager/edit_user",
            data : postData,
            dataType : "json",
            success : function(data) {
                if (data != null) {
                    if (data.msg == "success") {
                        $('#edit_dialog').dialog('close');
                        $('#user_manager_grid').datagrid('unselectAll');
                        $('#user_manager_grid').datagrid('reload');
                    } else {
                        $.messager.alert('出错', '编辑失败', 'error');
                    }
                }
            }
        });
    } else {
        return false;
    }
    return false;
}

function openDeleteDialog() {
    var row = $('#user_manager_grid').datagrid('getSelected');

    if (row) {
        $.messager.confirm('确认', '删除: ' + row.uid + '?', function(r) {
            if (r) {
                $.post("user_manager/delete", {
                    uid : row.uid
                }, function(data) {
                    if (data != null) {
                        if (data.msg == "success") {
                            $('#user_manager_grid').datagrid('unselectAll');
                            $('#user_manager_grid').datagrid('reload');
                        } else {
                            $.messager.alert('出错', '删除失败', 'error');
                        }
                    }
                }, 'json');
            }
        });
    }
}