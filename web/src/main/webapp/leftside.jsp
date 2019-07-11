<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
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
<input type="hidden" id="h_user_id" value="${user.uid}"/>
<div id="sidebar" style="width: 100px;">
    <div class="easyui-accordion" data-options="multiple:true" style="width:150px;height1:700px;">
        <div title="开发中心" style="overflow:auto;padding:10px;height: 300px;">
            <a href="homepage.jsp?objId=a_home_page" id="a_home_page"  class="leftnav"><span class="c_text">首页 </span></a>
            <a href="developcenter.jsp?objId=a_develop_center" id="a_develop_center" class="leftnav"><span class="c_text">开发中心</span></a>
            <a href="schedulecenter.jsp?objId=a_schedule_center" id="a_schedule_center" class="leftnav"><span class="c_text">调度中心</span></a>
            <a href="statisticreport.jsp?objId=a_statistic_report" id="a_statistic_report"  class="leftnav"><span class="c_text">统计报表</span></a>
            <a href="usermanager.jsp?objId=a_user_manager" id="a_user_manager"  class="leftnav"><span class="c_text">用户管理</span></a>
        </div>
        <div title="Java" style="padding:10px;height: 200px;">
            <p>qita</p>
        </div>
    </div>
</div>
