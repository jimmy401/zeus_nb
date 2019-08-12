<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
    <title>用户管理</title>
    <meta charset="utf-8">
    <link rel="stylesheet" href="newcss/easyui.css"/>
    <link rel="stylesheet" href="newcss/color.css"/>
    <link rel="stylesheet" href="newcss/icon.css"/>
    <link rel="stylesheet" href="newcss/custom.css"/>

    <script type="text/javascript" src="newjs/jquery.min.js"></script>
    <script type="text/javascript" src="newjs/jquery.easyui.min-1.8.2.js"></script>
    <script type="text/javascript" src="newjs/jquery.extend.js"></script>
    <script type="text/javascript" src="newjs/usermanager.js"></script>
</head>
<body>
<%@include file="header.jsp"%>
<%@include file="leftside.jsp"%>
    <div class="mainContent">
        <div style="float: right;margin-bottom: 10px;">
        <input id="uid" class="easyui-textbox" label="账号:" labelPosition="left" style="width:300px;">
        <a id="search_btn" href="javascript:void(0)" class="easyui-linkbutton"
           data-options="iconCls:'icon-search'" style="width:80px">搜索</a>
        </div>
        <table id="user_manager_grid">
        </table>

        <input id = "h_user_type" value="${user.userType}" type="hidden"/>
        <div id="user_manager_grid_toolbar">
            <a id="a_ok" href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-edit" plain="true"
               onclick="openOkDialog()">审核通过</a>
            <a id="a_no" href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-edit" plain="true"
               onclick="openNoDialog()">审核拒绝</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-edit" plain="true"
               onclick="openEditDialog()">编辑</a>
            <a id="a_delete" href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-remove" plain="true"
               onclick="openDeleteDialog()">删除</a>
        </div>

        <div id="edit_dialog" class="easyui-dialog" style="width: 420px; height: 350px; padding: 10px 20px"
             closed="true"
             buttons="#editdlg-buttons">
            <form id="edit_form" method="post" novalidate>
                <div>
                    <label> 用户账号:</label>
                    <div>
                        <input id="edit_uid" name="uid" readonly="readonly" disabled="disabled"/>
                    </div>
                </div>
                <div>
                    <label> 密码:</label>
                    <div>
                        <input id="edit_passwd" name="" required="true"/>
                    </div>
                </div>
                <div>
                    <label> 邮箱:</label>
                    <div>
                        <input id="edit_email" name="email" required="true"/>
                    </div>
                </div>
                <div>
                    <label> 手机号:</label>
                    <div>
                        <input id="edit_phone" name="phone" required="true"/>
                    </div>
                </div>
            </form>
        </div>
        <div id="editdlg-buttons">
            <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-ok" onclick="saveEdit()"> 保存</a>
            <a href="javascript:void(0)" class="easyui-linkbutton" iconcls="icon-cancel"
               onclick="javascript:$('#edit_dialog').dialog('close')">取消</a>
        </div>
    </div>
</body>
</html>
