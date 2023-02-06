//KindEditor变量
var editor;

$(function () {

    var editor = new FroalaEditor('#editor', {
        language: 'zh_cn',
        height: 300,
        fullPage: true,
        imageUploadURL: _ctx + 'common/froalaUpload',
        imageManagerToggleTags: false,
        imageDefaultWidth: 0,
        imageInsertButtons: ['imageBack', '|', 'imageUpload', 'imageByURL']
    }, function () {
        // console.log(editor.html.get())
    });
    new AjaxUpload('#uploadGoodsCoverImg', {
        action: _ctx + 'common/upload',
        name: 'file',
        autoSubmit: true,
        responseType: "json",
        onSubmit: function (file, extension) {
            if (!(extension && /^(jpg|jpeg|png|gif)$/.test(extension.toLowerCase()))) {
                alert('只支持jpg、png、gif格式的文件！');
                return false;
            }
        },
        onComplete: function (file, r) {
            if (r != null && r.code == 200) {
                $("#goodsCoverImg").attr("src", r.map.url);
                $("#goodsCoverImg").attr("style", "width: 128px;height: 128px;display:block;");
                return false;
            } else {
                alert("error");
            }
        }
    });

    $(".js-example-tags").select2({
        theme: "bootstrap",
        language: "zh-CN",
        tags: true,
        placeholder: "请输入商品标签",
    });

    $('#confirmButton').click(function () {
        var goodsName = $('#goodsName').val();
        var tag = $('#tag').val() || [];
        var originalPrice = $('#originalPrice').val();
        var sellingPrice = $('#sellingPrice').val();
        var stockNum = $('#stockNum').val();
        var goodsIntro = $('#goodsIntro').val();
        var goodsCategoryId = $('#levelThree option:selected').val();
        var goodsSellStatus = $("input[name='goodsSellStatus']:checked").val();
        var goodsDetailContent = editor.html.get();
        if (isNull(goodsCategoryId)) {
            swal("请选择分类", {
                icon: "error",
            });
            return;
        }
        if (isNull(goodsName)) {
            swal("请输入商品名称", {
                icon: "error",
            });
            return;
        }
        if (!validLength(goodsName, 100)) {
            swal("请输入商品名称", {
                icon: "error",
            });
            return;
        }
        if (isNull(tag)) {
            swal("请输入商品小标签", {
                icon: "error",
            });
            return;
        }
        if (!validLength(tag.join('|'), 500)) {
            swal("标签过长", {
                icon: "error",
            });
            return;
        }
        if (isNull(goodsIntro)) {
            swal("请输入商品简介", {
                icon: "error",
            });
            return;
        }
        if (!validLength(goodsIntro, 100)) {
            swal("简介过长", {
                icon: "error",
            });
            return;
        }
        if (isNull(originalPrice) || originalPrice < 1) {
            swal("请输入商品价格", {
                icon: "error",
            });
            return;
        }
        if (isNull(sellingPrice) || sellingPrice < 1) {
            swal("请输入商品售卖价", {
                icon: "error",
            });
            return;
        }
        if (isNull(stockNum) || sellingPrice < 0) {
            swal("请输入商品库存数", {
                icon: "error",
            });
            return;
        }
        if (isNull(goodsSellStatus)) {
            swal("请选择上架状态", {
                icon: "error",
            });
            return;
        }
        if (isNull(goodsDetailContent)) {
            swal("请输入商品介绍", {
                icon: "error",
            });
            return;
        }
        var goodsId = $('#goodsId').val();
        var goodsCategoryId = $('#levelThree option:selected').val();
        var goodsName = $('#goodsName').val();
        var tag = $('#tag').val().join("|");
        var originalPrice = $('#originalPrice').val();
        var sellingPrice = $('#sellingPrice').val();
        var goodsIntro = $('#goodsIntro').val();
        var stockNum = $('#stockNum').val();
        var goodsSellStatus = $("input[name='goodsSellStatus']:checked").val();
        var goodsDetailContent = editor.html.get();
        var goodsCoverImg = $('#goodsCoverImg')[0].src;
        if (isNull(goodsCoverImg) || goodsCoverImg.indexOf('img-upload') != -1) {
            swal("封面图片不能为空", {
                icon: "error",
            });
            return;
        }
        var url = _ctx + 'admin/goods/save';
        var swlMessage = '保存成功';
        var data = {
            "goodsName": goodsName,
            "goodsIntro": goodsIntro,
            "goodsCategoryId": goodsCategoryId,
            "tag": tag,
            "originalPrice": originalPrice,
            "sellingPrice": sellingPrice,
            "stockNum": stockNum,
            "goodsDetailContent": goodsDetailContent,
            "goodsCoverImg": goodsCoverImg,
            "goodsCarousel": goodsCoverImg,
            "goodsSellStatus": goodsSellStatus
        };
        if (goodsId > 0) {
            url = _ctx + 'admin/goods/update';
            swlMessage = '修改成功';
            data = {
                "goodsId": goodsId,
                "goodsName": goodsName,
                "goodsIntro": goodsIntro,
                "goodsCategoryId": goodsCategoryId,
                "tag": tag,
                "originalPrice": originalPrice,
                "sellingPrice": sellingPrice,
                "stockNum": stockNum,
                "goodsDetailContent": goodsDetailContent,
                "goodsCoverImg": goodsCoverImg,
                "goodsCarousel": goodsCoverImg,
                "goodsSellStatus": goodsSellStatus
            };
        }
        console.log(data);
        $.ajax({
            type: 'POST',//方法类型
            url: url,
            contentType: 'application/json',
            data: JSON.stringify(data),
            success: function (result) {
                if (result.code == 200) {
                    $('#goodsModal').modal('hide');
                    swal({
                        title: swlMessage,
                        type: 'success',
                        showCancelButton: false,
                        confirmButtonColor: '#1baeae',
                        confirmButtonText: '返回商品列表',
                        confirmButtonClass: 'btn btn-success',
                        buttonsStyling: false
                    }).then(function () {
                        window.location.href = _ctx + "admin/goods";
                    })
                } else {
                    $('#goodsModal').modal('hide');
                    swal(result.msg, {
                        icon: "error",
                    });
                }
            },
            error: function () {
                swal("操作失败", {
                    icon: "error",
                });
            }
        });
    });

    $('#cancelButton').click(function () {
        window.location.href = _ctx + "admin/goods";
    });

    $('#levelOne').on('change', function () {
        $.ajax({
            url: _ctx + 'admin/categories/listForSelect?categoryId=' + $(this).val(),
            type: 'GET',
            success: function (result) {
                if (result.code == 200) {
                    var levelTwoSelect = '';
                    var secondLevelCategories = result.map.data.secondLevelCategories;
                    var length2 = secondLevelCategories.length;
                    for (var i = 0; i < length2; i++) {
                        levelTwoSelect += '<option value=\"' + secondLevelCategories[i].categoryId + '\">' + secondLevelCategories[i].categoryName + '</option>';
                    }
                    $('#levelTwo').html(levelTwoSelect);
                    var levelThreeSelect = '';
                    var thirdLevelCategories = result.map.data.thirdLevelCategories;
                    var length3 = thirdLevelCategories.length;
                    for (var i = 0; i < length3; i++) {
                        levelThreeSelect += '<option value=\"' + thirdLevelCategories[i].categoryId + '\">' + thirdLevelCategories[i].categoryName + '</option>';
                    }
                    $('#levelThree').html(levelThreeSelect);
                } else {
                    swal(result.msg, {
                        icon: "error",
                    });
                }
            },
            error: function () {
                swal("操作失败", {
                    icon: "error",
                });
            }
        });
    });

    $('#levelTwo').on('change', function () {
        $.ajax({
            url: _ctx + 'admin/categories/listForSelect?categoryId=' + $(this).val(),
            type: 'GET',
            success: function (result) {
                if (result.code == 200) {
                    var levelThreeSelect = '';
                    var thirdLevelCategories = result.map.data.thirdLevelCategories;
                    var length = thirdLevelCategories.length;
                    for (var i = 0; i < length; i++) {
                        levelThreeSelect += '<option value=\"' + thirdLevelCategories[i].categoryId + '\">' + thirdLevelCategories[i].categoryName + '</option>';
                    }
                    $('#levelThree').html(levelThreeSelect);
                } else {
                    swal(result.msg, {
                        icon: "error",
                    });
                }
            },
            error: function () {
                swal("操作失败", {
                    icon: "error",
                });
            }
        });
    });
});
