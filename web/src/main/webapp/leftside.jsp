<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<!-- Authority Control -->
<script type="text/javascript">
    /*$(function() {
        var userId = $("#user_id").val();
        if (userId == "") {
            return;
        }

        $.ajax({
            type : "GET",
            url : "data_users/loadUserModulePage",
            data : {
                userId : userId
            },
            dataType : "json",
            success : function(data) {
                var userInfoList = data.userInfoList;
                for (var i = 0; i < userInfoList.length; i++) {
                    var moduleDivId = userInfoList[i].moduleDivId;
                    var pageDivId = userInfoList[i].pageDivId;
                    var isButtonShow = userInfoList[i].isButtonShow;

                    $("#" + moduleDivId).css('display', 'block');
                    $("#" + pageDivId).css('display', 'block');

                    //$("#"+pageDivId+"-content").css('display', 'block');

                    if (0 == isButtonShow) {
                        // $("." + pageDivId + "-toolbar").remove();
                        $("." + pageDivId + "-toolbar").css("display", "none");
                        // set for restful_monitor's button show
                        $("#" + pageDivId + "-isButtonShow").val('no');
                    }
                }
            }
        });
    });*/
</script>
<input type="hidden" id="user_id" value="${user.uid}" />
<div id="sidebar" style="width: 100px;">
    <ul>
        <li id="data_module" class="submenu" style="display: none;"><a href="#"><i class="icon icon-th-list icon-home"></i>
            <span class="custom_menu">数据开发</span></a>
            <ul>
                <li id="home_page" style="display: none;"><a href="home_page/index"><span class="custom_suojin">首页</span></a></li>
                <li id="develop_center_page" style="display: none;"><a href="develop_center/index"><span class="custom_suojin">开发中心</span></a></li>
                <li id="schedule_center_page" style="display: none;"><a href="schedule_center/index"><span class="custom_suojin">调度中心</span></a></li>
                <li id="statistic_report_page" style="display: none;"><a href="statistic_report/index"><span class="custom_suojin">统计报表</span></a></li>
                <li id="user_manager_page" style="display: none;"><a href="user_manager/index"><span class="custom_suojin">用户管理</span></a></li>
            </ul>
        </li>
        <li id="channel_module" class="submenu" style="display: none;"><a href="#"><i class="icon icon-file"></i> <span class="custom_menu">其他</span></a>
            <ul>
                <li id="channel_page" style="display: none;"><a href="qita"><span class="custom_suojin">其他</span> </a></li>
            </ul>
        </li>
    </ul>
</div>
