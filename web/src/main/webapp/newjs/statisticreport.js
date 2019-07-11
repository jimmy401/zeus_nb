$(function() {
    showAccordin();
});

function showAccordin() {
    var objId=$.Request("objId");
    $('#' + objId).addClass('leftbackground');
}