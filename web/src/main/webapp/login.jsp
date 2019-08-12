<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>数据管理平台</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <link href="newcss/login.css" rel="stylesheet" type="text/css" />
<%--    <link rel="stylesheet" href="newcss/easyui.css" />
    <link rel="stylesheet" href="newcss/color.css" />
    <link rel="stylesheet" href="newcss/icon.css" />
    <link rel="stylesheet" href="newcss/custom.css" />--%>
    <script type="text/javascript" src="newjs/jquery.min.js"></script>
    <script type="text/javascript" src="newjs/jquery.easyui.min-1.8.2.js"></script>
    <script type="text/javascript" src="newjs/login.js"></script>
</head>
<body>
<h1>
    宙斯数据管理平台<sup>V2019</sup>
</h1>

<div class="login" style="margin-top: 50px;">

    <div class="header">
        <div class="switch" id="switch">
            <a class="switch_btn_focus" id="switch_qlogin"
               href="javascript:void(0);" tabindex="7">快速登录</a>
            <a class="switch_btn" id="switch_login" href="javascript:void(0);"
                tabindex="8">快速注册</a>
            <div class="switch_bottom" id="switch_bottom"
                 style="position: absolute; width: 64px; left: 0px;"></div>
        </div>
    </div>


    <div class="web_qr_login" id="web_qr_login"
         style="display: block; height: 235px;">

        <!--登录-->
        <div class="web_login" id="web_login">
            <div class="login-box">
                <div class="login_form">
                        <input type="hidden" name="did" value="0" />
                        <input type="hidden" name="to" value="log" />
                        <div class="uinArea" id="uinArea">
                            <label class="input-tips">帐号：</label>
                            <div class="inputOuter" id="uArea">
                                <input type="text" id="username" name="username" class="inputstyle" />
                            </div>
                        </div>
                        <div class="pwdArea" id="pwdArea">
                            <label class="input-tips" >密码：</label>
                            <div class="inputOuter" id="pArea">
                                <input type="password" id="password" name="password" class="inputstyle" />
                            </div>
                        </div>
                        <div style="padding-left: 120px; margin-top: 20px;">
                            <input id="button" type="button" value="登 录" style="width: 150px;" class="button_blue" />
                        </div>
                </div>
            </div>
        </div>
        <!--登录end-->
    </div>

    <!--注册-->
    <div class="qlogin" id="qlogin" style="display: none;">
        <div class="web_login">
                <input type="hidden" name="to" value="reg" />
                <input type="hidden" name="did" value="0" />
                <ul class="reg_form" id="reg-ul">
                    <div id="userCue" class="cue"><font color='green'>提醒：Zeus帐号与Hive帐号必须相同</font></div>
                    <li style="display:none"><label class="input-tips2">用户类型：</label>
                        <div class="inputOuter2" style="margin-top:15px">
                            <input type="radio" name="userTypes" value="0" checked="checked"/> 组用户&nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="radio" name="userTypes" value="1" /> 个人用户
                        </div></li>
                    <li><label for="user" class="input-tips2">帐号：</label>
                        <div class="inputOuter2">
                            <input type="text" id="user" name="user" maxlength="16" class="inputstyle2" />
                        </div></li>
                    <li><label for="passwd" class="input-tips2">密码：</label>
                        <div class="inputOuter2">
                            <input type="password" id="passwd" name="passwd" maxlength="16" class="inputstyle2" />
                        </div></li>
                    <li><label for="passwd2" class="input-tips2">确认密码：</label>
                        <div class="inputOuter2">
                            <input type="password" id="passwd2" name="" maxlength="16" class="inputstyle2" />
                        </div></li>
                    <li><label for="email" class="input-tips2">邮箱：</label>
                        <div class="inputOuter2">
                            <input type="text" id="email" name="email" maxlength="2000" class="inputstyle2" />
                        </div></li>
                    <li><label for="phone" class="input-tips2">手机：</label>
                        <div class="inputOuter2">
                            <input type="text" id="phone" name="phone" maxlength="2000"  class="inputstyle2" />
                        </div></li>
                    <li><label for="description" class="input-tips2">帐号描述：</label>
                        <div class="inputOuter2">
                            <textarea id="description" name="description" cols="10" rows="5" class="inputstyle2" style="height:180px;" maxlength="5000"></textarea>
                        </div>
                    </li>
                    <li>
                        <div class="inputArea">
                            <input type="button" id="register"
                                   style="margin-top: 10px; margin-left: 150px; width: 150px"
                                   class="button_blue" value="注 册" />
                        </div>
                    </li>
                    <div class="cl"></div>
                </ul>
        </div>
    </div>
    <div id="descriptionInfo" style="display:none; width:300px; height:200px; background-color:#fff; position:absolute;padding:5px">
        <div>
            <ul>
                <li style="font-weight: bold;color: green;">申请hive新帐号，需要说明以下信息：</li>
                <li>1、申请的这个新帐号给什么项目或者业务用</li>
                <li>2、帐号的负责人</li>
                <li>3、业务量大概多少，每天或每周需要跑多少job数据规模有多大</li>
                <li>4、hive账号是否会做数据导入工作，如果查询出来的数据要导入hdfs的话，导入量有多大，需要保留多长时间</li>
                <li>5、需要开通哪些库及表的权限</li>
            </ul>
        </div>
    </div>
    <!--注册end-->
</div>
<div class="jianyi">*推荐使用ie8或以上版本ie浏览器或Firefox、Chrome内核浏览器访问本站</div>
</body>
</html>
