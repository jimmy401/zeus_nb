var pwdmin = 6;
$(function() {
	$('#switch_qlogin').click(
			function() {
				$('#switch_login').removeClass("switch_btn_focus").addClass(
						'switch_btn');
				$('#switch_qlogin').removeClass("switch_btn").addClass(
						'switch_btn_focus');
				$('#switch_bottom').animate({
					left : '0px',
					width : '70px'
				});
				$('#qlogin').css('display', 'none');
				$('#web_qr_login').css('display', 'block');

			});
	$('#switch_login').click(
			function() {
				$('#switch_login').removeClass("switch_btn").addClass(
						'switch_btn_focus');
				$('#switch_qlogin').removeClass("switch_btn_focus").addClass(
						'switch_btn');
				$('#switch_bottom').animate({
					left : '199px',
					width : '70px'
				});

				$('#qlogin').css('display', 'block');
				$('#web_qr_login').css('display', 'none');
			});
	if (getParam("a") == '0') {
		$('#switch_login').trigger('click');
	}
    $("#button").click(function() {
        var username = $("#username").val();
        var password = $("#password").val();
        if (username == "") {
            $("#username").addClass("has-error");
            return false;
        }
        if (password == "") {
            $("#password").addClass("has-error");
            return false;
        }
        $.ajax({
            url : "logon",
            async : false,
            data : {
                username : username,
                password : password
            },
            type : "post",

            error : function(response) {
                alert("请求发送失败");

            },
            success : function(response) {
//								console.log(response);
                if (response == "null") {
                    alert("用户名不存在");
                } else if (response.msg == "failed") {
                    alert("账户错误");
                } else {
                    window.location.href = '/zeus-web/homepage.jsp';
                }
            }
        });
    });

    $("#container input,textarea,select").on(
        'input propertychange', function(event) {
            validate(event);
        });
    $('#register')
        .click(
            function() {
                var user = $("#user").val();
                var passwd = $("#passwd").val();
                var email = $("#email").val();
                var phone = $("#phone").val();
                var userType = $('input[name="userTypes"]:checked').val();
                var description = $("#description").val();
                if ($('#user').val() == "") {
                    $('#user').focus().css({
                        border : "1px solid red",
                        boxShadow : "0 0 2px red"
                    });
                    $('#userCue')
                        .html(
                            "<font color='red'><b>×用户名不能为空</b></font>");
                    return false;
                }

                if (user.length < 4
                    || user.length > 16) {

                    $('#user').focus().css({
                        border : "1px solid red",
                        boxShadow : "0 0 2px red"
                    });
                    $('#userCue')
                        .html(
                            "<font color='red'><b>×用户名位4-16字符</b></font>");
                    return false;

                }
                //密码验证
                if (passwd.length < pwdmin) {
                    $('#passwd').focus();
                    $('#userCue').html(
                        "<font color='red'><b>×密码不能小于"
                        + pwdmin
                        + "位</b></font>");
                    return false;
                }
                //两次密码验证
                if ($('#passwd2').val() != $('#passwd')
                    .val()) {
                    $('#passwd2').focus();
                    $('#userCue')
                        .html(
                            "<font color='red'><b>×两次密码不一致！</b></font>");
                    return false;
                }

                //邮箱验证
                //var emailPatrn  = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
                var emailPattern = /^([\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/;
                if (!emailPattern.test(email)) {
                    $('#userCue')
                        .html(
                            "<font color='red'><b>×邮件格式不正确！</b></font>");
                    return false;
                }

                //手机号码验证
                var phonePattern=/^[0-9]{1,12}$/;
                if (!phonePattern.exec(phone)) {
                    $('#userCue')
                        .html(
                            "<font color='red'><b>×手机号码格式不正确！</b></font>");
                    return false;
                }

                //申请描述
                if ($('#description').val() == "") {
                    $('#description').focus().css({
                        border : "1px solid red",
                        boxShadow : "0 0 2px red"
                    });
                    $('#userCue')
                        .html(
                            "<font color='red'><b>×注册说明信息不能为空</b></font>");
                    return false;
                }

                //提交
                $.ajax({
                    url : "register",
                    async : false,
                    data : {
                        user : user,
                        passwd : passwd,
                        email : email,
                        phone : phone,
                        userType : userType,
                        description : description
                    },
                    type : "post",

                    error : function(response) {
                        alert("请求发送失败");

                    },
                    success : function(response) {
//												console.log(response);
                        if (response == "exist") {
                            $('#userCue')
                                .html(
                                    "<font color='red'><b>警告：用户名已经存在！</b></font>");
                        } else if (response == "error") {
                            $('#userCue')
                                .html(
                                    "<font color='red'><b>警告：用户注册失败！</b></font>");
                        } else{
                            $('#userCue')
                                .html(
                                    "<font color='green'><b>用户注册成功！</b></font>");
                            $("#user").val("");
                            $("#passwd").val("");
                            $("#passwd2").val("");
                            $("#email").val("");
                            $("#phone").val("");
                            $("#description").val("");
                        }
                    }
                });
            });
    $('#description').myHoverTip('descriptionInfo');
});

function logintab() {
	scrollTo(0);
	$('#switch_qlogin').removeClass("switch_btn_focus").addClass('switch_btn');
	$('#switch_login').removeClass("switch_btn").addClass('switch_btn_focus');
	$('#switch_bottom').animate({
		left : '154px',
		width : '96px'
	});
	$('#qlogin').css('display', 'none');
	$('#web_qr_login').css('display', 'block');

}

// 根据参数名获得该参数 pname等于想要的参数名
function getParam(pname) {
	var params = location.search.substr(1); // 获取参数 平且去掉？
	var ArrParam = params.split('&');
	if (ArrParam.length == 1) {
		// 只有一个参数的情况
		return params.split('=')[1];
	} else {
		// 多个参数参数的情况
		for (var i = 0; i < ArrParam.length; i++) {
			if (ArrParam[i].split('=')[0] == pname) {
				return ArrParam[i].split('=')[1];
			}
		}
	}
}
function validate(event) {
	var target = $(event.target), reg = target.attr("data-reg"), val = target
			.val(), status = true, disabled = false;
	if (reg) {
		switch (reg) {
		case 'email':
			if (!/^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/
					.test(val)) {
				status = false;
			}
			break;
		case 'noempty':
			if (val == '' || /^\s+$/.test(val)) {
				status = false;
			}
			break;
		case 'number':
			if (val == '' || !/^\d+$/.test(val)) {
				status = false;
			}
			break;
		}
	}
	if (!status) {
		target.removeClass('has-success').addClass('has-error');
	} else {
		target.removeClass('has-error').addClass('has-success');
	}

}

/** 
 * 鼠标移上去显示层 
 * @param divId 显示的层ID 
 * @returns 
 */  
$.fn.myHoverTip = function(divId) {  
    var div = $("#" + divId); //要浮动在这个元素旁边的层  
    div.css("position", "absolute");//让这个层可以绝对定位  
    var self = $(this); //当前对象  
    self.hover(function() {  
        div.css("display", "block");  
        var p = self.position(); //获取这个元素的left和top  
        var x = p.left + self.width();//获取这个浮动层的left  
        var docWidth = $(document).width();//获取网页的宽  
        if (x > docWidth - div.width() - 20) {  
            x = p.left - div.width();  
        }  
        div.css("left", x+25);  
        div.css("top", p.top+25);  
        div.show();  
    },  
    function() {  
        div.css("display", "none");  
    }  
    );  
    return this;  
}