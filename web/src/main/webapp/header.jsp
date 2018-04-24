<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<script type="text/javascript">
    function logOut() {
        $.ajax({
            type : "POST",
            url : "logout",
            data : {},
            dataType : "json",
            success : function(ret) {
                if (ret.code == "00000") {
                    var url = ret.data;
                    window.location.href = url;
                }
            }
        });
        return false;
    }

    function gotoAccount() {
        window.location.href = "user_index_page";
        return false;
    }
</script>
<div id="div_header">
    <div class="easyui-panel" style="padding:5px;">
        <a href="#" class="easyui-linkbutton" data-options="menu:'#mm1'">${user.name}</a>
    </div>
    <div id="mm1" style="width:100px;">
        <div><a href="javascript:void(0)" onclick="gotoAccount();">账户</a></div>
        <div><a href="javascript:void(0)" onclick="logOut();">退出登录</a></div>
    </div>
</div>