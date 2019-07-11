<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html>
<head>
    <title>开发中心</title>
    <meta charset="utf-8">
    <link rel="stylesheet" href="newcss/easyui.css" />
    <link rel="stylesheet" href="newcss/color.css" />
    <link rel="stylesheet" href="newcss/icon.css" />
    <link rel="stylesheet" href="newcss/custom.css" />

    <script type="text/javascript" src="newjs/jquery.min.js"></script>
    <script type="text/javascript" src="newjs/jquery.easyui.min.js"></script>
    <script type="text/javascript" src="newjs/jquery.extend.js"></script>
    <script type="text/javascript" src="newjs/developcenter.js"></script>
</head>
<body>
<div id="body" class="page" style="position: relative;">
    <%@include file="header.jsp"%>
    <%@include file="leftside.jsp"%>
    <div class="mainContent">
        <div data-options="region:'west',split:true" style="width:200px;">
            <div class="easyui-accordion" style="width:200px;height: 500px;">
                <div title="我的文档" style="overflow:auto;padding:10px;">
                    <ul id="personal_files" class="easyui-tree" data-options="animate:true,dnd:true">
                    </ul>
                    <div id="rc_follow_menu" class="easyui-menu" style="width:120px;">
                        <div onclick="newFolder()">新建文件夹</div>
                        <div onclick="newHive()">新建hive</div>
                        <div onclick="newShell()">新建shell</div>
                        <div onclick="newFile()">新建文件</div>
                        <div onclick="rename()">重命名</div>
                        <div onclick="openFile()">打开</div>
                        <div onclick="deleteFile()">删除</div>
                    </div>
                </div>
                <div title="公共文档" style="padding:10px 0;">
                    <ul id="public_files" class="easyui-tree" data-options="animate:true,dnd:true">
                    </ul>
                </div>
            </div>
        </div>
        <div id="region_east" data-options="region:'east',split:true" style="width:100px;"></div>
        <div data-options="region:'center',iconCls:'icon-ok'"></div>
    </div>
</div>
</body>
</html>
