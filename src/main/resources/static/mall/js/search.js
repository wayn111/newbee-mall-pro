$(function () {
    $('#keyword').keypress(function (e) {
        var key = e.which; //e.which是按键的值
        if (key == 13) {
            var q = $(this).val();
            var c = $('#goodsCategoryId').val() || '';
            if (q && q != '') {
                window.location.href = _ctx + 'search?keyword=' + q + '&goodsCategoryId=' + c;
            }
        }
    });

    /**
     * 查询购物车数量
     */
    $.get(_ctx + 'shopCart/getUserShopCartCount', function (res) {
        if (res.code == 200) {
            $('#shopCatCount').text(res.map.count);
        }
    });
});

function search() {
    var q = $('#keyword').val();
    var c = $('#goodsCategoryId').val() || '';
    if (q && q != '') {
        window.location.href = _ctx + 'search?keyword=' + q + '&goodsCategoryId=' + c;
    } else {
        $('#keyword').attr('placeholder', '请输入商品信息');
    }
}
