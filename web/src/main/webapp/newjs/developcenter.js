$(function() {
    initialTree();
});
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
                    $.messager.alert('警告', '加载个人文件节点失败', 'info');
                }
            }
        }
    });
}
