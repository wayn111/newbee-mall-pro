$(function () {
    var params = new URLSearchParams(window.location.search);
    var key = params.get('keyword')
    if (key) {
        $('#keyword').val(key)
    }

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

    $('#keyword').focus(function (e) {
        $(this).addClass("search_btn")
        $("#nav .banner_x .fr .button").addClass("search_btn")
    }).blur(function (e) {
        $(this).removeClass("search_btn")
        $("#nav .banner_x .fr .button").removeClass("search_btn")
    });

    /**
     * 查询购物车数量
     */
    $.get(_ctx + 'shopCart/getUserShopCartCount', function (res) {
        if (res.code == 200) {
            $('#shopCatCount').text(res.map.count);
        }
    });

    /**
     * 查询用户信息
     */
    $.get(_ctx + 'personal/info', function (res) {
        if (res.code == 200) {
            $('.user-not-login').hide();
            $('.user-login').css({"display": "flex"});
            $('#headNickName').text(res.map.userInfo.nickName);
        } else {
            $('.user-login').hide();
            $('.user-not-login').css({"display": "flex"});
        }
    });
});

function search() {
    var q = $('#keyword').val();
    var c = $('#goodsCategoryId').val() || '';
    if (q && q != '') {
        window.location.href = _ctx + 'search?keyword=' + q + '&goodsCategoryId=' + c;
    } else {
        swal("请输入商品信息", {
            icon: "warning",
        });
    }
}
