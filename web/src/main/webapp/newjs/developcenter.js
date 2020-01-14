$(function() {
    initialTree();
    initialTabs();
});

var fileId;
var fileName;
var myCodeMirror;

function initialTabs() {
    $('#file_tabs').tabs({
        onSelect: function(title,index){
            fileId =title.split('|')[0];
            fileName = title.split('|')[1];
            //myCodeMirror = CodeMirror.fromTextArea(document.getElementById(fileId));
        }
    });
}
var activeNode;
function initialTree() {
    var getData = {};

    $.ajax({
        type: "GET",
        url: "file/personal_files",
        data: getData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    $('#personal_files').tree({
                        dnd:true,
                        data: ret.data,
                        onDblClick: function (node) {
                            activeNode = node;
                            if(node.type==2){
                                openFile(node);
                            }
                        },
                        onClick:function(node){
                            if (node.state == 'closed')
                            {
                                $(this).tree('expand',node.target);
                            }
                            else {
                                $(this).tree('collapse',node.target);
                            }
                        },
                        onContextMenu: function(e,node){
                            e.preventDefault();
                            $(this).tree('select',node.target);
                            $('#rc_follow_menu').menu('show',{
                                left: e.pageX,
                                top: e.pageY
                            });
                            activeNode=node;
                            if(node.type!=1){
                                $('#rc_follow_menu').menu('disableItem',($('#new_folder')[0]));
                                $('#rc_follow_menu').menu('disableItem',($('#new_hive')[0]));
                                $('#rc_follow_menu').menu('disableItem',($('#new_shell')[0]));
                                $('#rc_follow_menu').menu('disableItem',($('#new_file')[0]));

                                $('#rc_follow_menu').menu('enableItem',($('#rename_file')[0]));
                                $('#rc_follow_menu').menu('enableItem',($('#open_file')[0]));
                                $('#rc_follow_menu').menu('enableItem',($('#delete_file')[0]));
                            }else{
                                $('#rc_follow_menu').menu('enableItem',($('#new_folder')[0]));
                                $('#rc_follow_menu').menu('enableItem',($('#new_hive')[0]));
                                $('#rc_follow_menu').menu('enableItem',($('#new_shell')[0]));
                                $('#rc_follow_menu').menu('enableItem',($('#new_file')[0]));

                                $('#rc_follow_menu').menu('enableItem',($('#rename_file')[0]));
                                $('#rc_follow_menu').menu('disableItem',($('#open_file')[0]));
                                $('#rc_follow_menu').menu('enableItem',($('#delete_file')[0]));
                            }
                        },
                        onAfterEdit : function(node){
                            var _tree = $(this);
                            //新增文件夹
                            if(node.id == 0){
                                addNewNode(node,0);
                            }
                            //新增文件
                            else if(node.id == 1){
                                addNewNode(node,1);
                            }
                            else {//编辑节点
                                editNode(node);
                            }
                        },
                        onDrop:function (target,source,point) {
                            $('#personal_files').tree('expand',target);
                            moveFile(source.id,$('#personal_files').tree('getNode',target).id)
                        }
                    });

                } else {
                    $.messager.alert('警告', '加载个人文件节点失败', 'info');
                }
            }
        }
    });
}

function newFolder() {
    var newNode = [{
        id: 0,
        text: 'folder',
        state:'closed'
    }];

    $('#personal_files').tree('expand',activeNode.target);

    $('#personal_files').tree('append', {
        parent: activeNode.target,
        data: newNode
    });
    var _node = $('#personal_files').tree('find',0);
    $('#personal_files').tree('beginEdit',_node.target);
}

function  moveFile(sourceId,targetId) {
    var getData = {
        sourceId:sourceId,
        targetId:targetId
    };

    $.ajax({
        type: "GET",
        url: "file/move_file",
        data: getData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                } else {
                    $.messager.alert('警告', '移动失败,信息：' + ret.msg, 'info');
                }
            }
        }
    });
}

function addNewNode(node,type){
    var getData = {
        parentId:activeNode.id,
        name:node.text,
        folder:type==0?true:false
    };

    $.ajax({
        type: "GET",
        url: "file/add_file",
        data: getData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    initialTree();
                } else {
                    $.messager.alert('警告', '添加失败,信息：' + ret.msg, 'info');
                }
            }
        }
    });
}

function editNode(node){
    var getData = {
        fileId:activeNode.id,
        name:node.text
    };

    $.ajax({
        type: "GET",
        url: "file/update_file_name",
        data: getData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                } else {
                    $.messager.alert('警告', '编辑失败,信息：' + ret.msg, 'info');
                }
            }
        }
    });
}

function newHiveFile() {
    var newNode = [{
        id: 1,
        text: 'file.hive',
        state:'open'
    }];

    $('#personal_files').tree('expand',activeNode.target);

    $('#personal_files').tree('append', {
        parent: activeNode.target,
        data: newNode
    });
    var _node = $('#personal_files').tree('find',1);
    $('#personal_files').tree('beginEdit',_node.target);

}

function newShellFile() {
    var newNode = [{
        id: 1,
        text: 'file.shell',
        state:'open'
    }];
    $('#personal_files').tree('expand',activeNode.target);

    $('#personal_files').tree('append', {
        parent: activeNode.target,
        data: newNode
    });
    var _node = $('#personal_files').tree('find',1);
    $('#personal_files').tree('beginEdit',_node.target);
}

function newFile() {
    var newNode = [{
        id: 1,
        text: 'file',
        state:'open'
    }];

    $('#personal_files').tree('expand',activeNode.target);

    $('#personal_files').tree('append', {
        parent: activeNode.target,
        data: newNode
    });
    var _node = $('#personal_files').tree('find',1);
    $('#personal_files').tree('beginEdit',_node.target);
}


function rename() {
    var node = $('#personal_files').tree('find', activeNode.id);
    $('#personal_files').tree('beginEdit',node.target);

}

function deleteFile() {
    $.messager.confirm('确认', '删除: ' + activeNode.text + '?', function (r) {
        if (r) {
            var getData = {
                fileId: activeNode.id
            };

            $.ajax({
                type: "GET",
                url: "file/delete_file",
                data: getData,
                success: function (ret) {
                    if (ret != null) {
                        if (ret.msg == "success") {
                            $('#personal_files').tree('remove', activeNode.target);
                        } else {
                            $.messager.alert('警告', '删除失败,信息：' + ret.msg, 'info');
                        }
                    }
                }
            });
        }
    });
}

function openFile(node) {
    fileId = node.id;
    fileName=node.text;
    var getData = {
        fileId: fileId
    };
    $.ajax({
        type: "GET",
        url: "file/get_file_content",
        data: getData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    var file_id = node.id;
                    var title = node.id+ "|" + node.text;
                    if(!$('#file_tabs').tabs('exists',title))
                    {
                        $('#file_tabs').tabs('add',{
                        title: title,
                        content: '<div id="tabs_' + file_id+
                        '" class="easyui-tabs" style="width:700px;min-height:500px">' +
                        '<div id="edit_file_' +file_id+
                        '" title="编辑">' + '<div id="file_content_'+file_id + '"></div>' +
                        '<div id="debug_tabs_' +file_id+
                        '" class="easyui-tabs" style="display: none;"></div></div>' +
                        '<div id="debug_history_' +file_id+
                        '"  title="调试历史"></div></div>',
                        closable:true
                        });

                        var textfile = '<textarea id="' + file_id+ '" style="min-height: 300px;min-width: 600px;" name="fileText">'+ ret.data +'</textarea>';
                        $('#file_content_'+file_id).html(textfile);

                            myCodeMirror = CodeMirror.fromTextArea(document.getElementById(file_id),
                            {lineNumbers: true,
                                mode:"shell",
                                lineWrapping:true});

                            var t1;
                            myCodeMirror.on("change", function (editor, change) {
                                  if(t1!=null)
                                  {
                                      window.clearTimeout(t1);
                                  }
                                  t1=window.setTimeout(updateFileContent, 3000);
                                });
                    }else{
                        $('#file_tabs').tabs('select',title);
                    }
                } else {
                    $.messager.alert('警告', '打开文件失败,信息：' + ret.msg, 'info');
                }

            }
        }
    });
}

function updateFileContent() {
    var getData = {
        fileId: fileId,
        content:myCodeMirror.getValue()
    };

    $.ajax({
        type: "POST",
        url: "file/update_file_content",
        data: getData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {

                } else {
                    $.messager.alert('警告', '保存内容失败,信息：' + ret.msg, 'info');
                }
            }
        }
    });
}

function runCode() {
    var suffix = fileName.split('.')[1];
    var debugId = "";
    var mode = "";
    if (suffix == "sh")
    {
        mode="shell";
    }else if(suffix == "hive"){
        mode="hive";
    }

    var postData = {
        fileId: fileId,
        mode:mode,
        script:myCodeMirror.getValue(),
        hostGroupId:""
    };
    $('#debug_tabs_'+fileId).show();


    $('#debug_tabs_'+fileId).tabs({
        onClose: function (title, index) {
            alert(index);
            if (index == 0) {
                $('#debug_tabs_' + fileId).hide();
            }
        }
    });

    $.ajax({
        type: "POST",
        url: "develop_center/debug",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    debugId = ret.data;
                    $('#debug_tabs_'+fileId).tabs('add',{
                        title:'ID:' + debugId,
                        closable:true,
                        selected: true
                    });
                    getDebugLog(debugId);
                } else {
                    $.messager.alert('警告', '执行脚本失败,信息：' + ret.msg, 'info');
                }
            }
        }
    });
}

function getDebugLog(debugId) {
    var postData = {
        debugId: debugId
    };

    $.ajax({
        type: "POST",
        url: "develop_center/get_log",
        data: postData,
        success: function (ret) {
            if (ret != null) {
                if (ret.msg == "success") {
                    if(ret.data.status!="success" || ret.data.status!="failed" )
                        var tab = $('#debug_tabs_'+fileId).tabs('getTab',"ID:" + debugId);
                        $('#tt').tabs('update', {
                            tab: tab,
                            options: {
                                content: ret.data
                            }
                        });
                        setTimeout("getDebugLog("+ debugId+")",1000);
                } else if(ret.data.status="success"){

                }
                else {
                    $.messager.alert('警告', '获取日志失败,信息：' + ret.msg, 'info');
                }
            }
        }
    });
}

function runSelected() {
    alert(myCodeMirror.getSelection());
}

function uploadResources(){
    
}

function chooseHost() {
    
}
