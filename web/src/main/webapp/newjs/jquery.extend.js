(function ($) {
    $.extend({
        //1、取值使用    $.Request("name")
        Request: function (name) {
            var sValue = location.search.match(new RegExp("[\?\&]" + name + "=([^\&]*)(\&?)", "i"));
            //decodeURIComponent解码
            return sValue ? decodeURIComponent(sValue[1]) : decodeURIComponent(sValue);

        }
    });
})(jQuery);