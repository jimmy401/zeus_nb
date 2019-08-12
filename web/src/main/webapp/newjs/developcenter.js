$(function() {
    initialTree();
    initialTabs();
});

function initialTabs() {
    $('#tt').tabs('add',{
        title:'New Tab',
        content:'Tab Body',
        closable:true,
        tools:[{
            iconCls:'icon-mini-refresh',
            handler:function(){
                alert('refresh');
            }
        }]
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
                    $.messager.alert('警告', '添加失败,信息：' + ret.data, 'info');
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
                    $.messager.alert('警告', '编辑失败,信息：' + ret.data, 'info');
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

function openFile() {
    
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
                            $.messager.alert('警告', '删除失败,信息：' + ret.data, 'info');
                        }
                    }
                }
            });
        }
    });
}

function openFile(node) {
    alert("open file " + node.text);
}
