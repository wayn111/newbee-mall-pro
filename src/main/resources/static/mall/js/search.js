$(function () {
    $('#keyword').keypress(function (e) {
        var key = e.which; //e.which是按键的值
        if (key == 13) {
            var q = $(this).val();
            var c = $('#goodsCategoryId').val() || '';
            if (q && q != '') {
                window.location.href = _ctx + '/search?keyword=' + q + '&goodsCategoryId=' + c;
            }
        }
    });
});

function search() {
    var q = $('#keyword').val();
    var c = $('#goodsCategoryId').val() || '';
    if (q && q != '') {
        window.location.href = _ctx + '/search?keyword=' + q + '&goodsCategoryId=' + c;
    }
}