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
                    window.location.href = "login";
                }
            }
        });
        return false;
    }
</script>
<div id="div_header">
    <div class="easyui-panel" style="padding:5px;">
        <a href="#" class="easyui-menubutton" style="float:right;" data-options="menu:'#mm1',iconCls:'icon-edit'">${user.name}</a>
    </div>
    <div id="mm1" style="width:100px;">
        <div><a href="javascript:void(0)" class="c_text"  onclick="logOut();">退出登录</a></div>
    </div>
</div>