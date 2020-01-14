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
    <script src="codemirror/mode/shell/shell.js"></script>
    <script src="codemirror/mode/sql/sql.js"></script>
</head>
<body>
<div id="body" class="page" style="position: relative;">
    <%@include file="header.jsp" %>
    <%@include file="leftside.jsp" %>
    <div class="mainContent">
        <div class="easyui-layout" style="width:1100px;height:500px;">
            <div data-options="region:'west',split:true" style="width:200px;">
                <div class="easyui-accordion" data-options="fit:true">
                    <div title="我的文档" style="overflow:auto;padding:10px;height: 500px;">
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
                </div>
            </div>
            <div data-options="region:'center',iconCls:'icon-ok'">
                <div id="action_div">
                    <a href="#" onclick="runCode()" class="easyui-linkbutton" data-options="plain:true,iconCls:'icon-ok'">运行</a>
                    <a href="#" onclick="runSelected()" class="easyui-linkbutton" data-options="plain:true,iconCls:'icon-reload'">运行选中代码</a>
                    <a href="#" onclick="uploadResources()" class="easyui-linkbutton" data-options="plain:true,iconCls:'icon-search'">上传资源</a>
                    <a href="#" onclick="chooseHost()" class="easyui-linkbutton" data-options="plain:true,iconCls:'icon-search'">选择host组</a>
                </div>
                <div id="file_tabs">
                </div>
            </div>

            <div class="easyui-tabs" style="width:70px;">
                <div id="edit_file" title="编辑">
                </div>
                <div id="debug_history" title="调试历史">
                </div>
            </div>

        </div>
    </div>
</div>
</div>
</body>
</html>
