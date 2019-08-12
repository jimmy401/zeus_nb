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
    <script type="text/javascript" src="newjs/jquery.easyui.min-1.8.2.js"></script>
    <script type="text/javascript" src="newjs/jquery.extend.js"></script>
    <script type="text/javascript" src="newjs/developcenter.js"></script>

    <script src="codemirror/lib/codemirror.js"></script>
    <link rel="stylesheet" href="codemirror/lib/codemirror.css">
    <script src="codemirror/mode/javascript/javascript.js"></script>
</head>
<body>
<div id="body" class="page" style="position: relative;">
    <%@include file="header.jsp" %>
    <%@include file="leftside.jsp" %>
    <div class="mainContent">
        <div class="easyui-layout" style="width:1100px;height:350px;">
            <div data-options="region:'west',split:true" style="width:200px;">
                <div class="easyui-accordion" style="width:200px;height: 500px;">
                    <div title="我的文档" style="overflow:auto;padding:10px;">
                        <ul id="personal_files" class="easyui-tree">
                        </ul>
                        <div id="rc_follow_menu" class="easyui-menu" style="width:120px;">
                            <div id="new_folder" onclick="newFolder()">新建文件夹</div>
                            <div id="new_hive" onclick="newHiveFile()">新建hive</div>
                            <div id="new_shell" onclick="newShellFile()">新建shell</div>
                            <div id="new_file" onclick="newFile()">新建文件</div>
                            <div id="rename_file" onclick="rename()">重命名</div>
                            <div id="open_file" onclick="openFile()">打开</div>
                            <div id="delete_file" onclick="deleteFile()">删除</div>
                        </div>
                    </div>
                    <div title="公共文档" style="padding:10px 0;">
                        <ul id="public_files" class="easyui-tree" data-options="animate:true,dnd:true">
                        </ul>
                    </div>
                </div>
            </div>
            <div data-options="region:'center',iconCls:'icon-ok'">
                <div id="action_div">
                    <a href="#" class="easyui-linkbutton" data-options="plain:true,iconCls:'icon-cancel'">Cancel</a>
                    <a href="#" class="easyui-linkbutton" data-options="plain:true,iconCls:'icon-reload'">Refresh</a>
                    <a href="#" class="easyui-linkbutton" data-options="plain:true,iconCls:'icon-search'">Search</a>
                </div>
                <div id="tt" class="easyui-tabs">
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
