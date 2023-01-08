$(function () {
    $("#jqGrid").jqGrid({
        url: _ctx + 'admin/goods/list',
        datatype: "json",
        viewrecords: true,
        colModel: [
            {label: '商品编号', name: 'goodsId', index: 'goodsId', width: 60, key: true},
            {label: '商品名', name: 'goodsName', index: 'goodsName', width: 120},
            {label: '商品简介', name: 'goodsIntro', index: 'goodsIntro', width: 120},
            {label: '商品图片', name: 'goodsCoverImg', index: 'goodsCoverImg', width: 120, formatter: coverImageFormatter},
            {label: '商品库存', name: 'stockNum', index: 'stockNum', width: 60},
            {label: '商品售价', name: 'sellingPrice', index: 'sellingPrice', width: 60},
            {
                label: '上架状态',
                name: 'goodsSellStatus',
                index: 'goodsSellStatus',
                width: 80,
                formatter: goodsSellStatusFormatter
            },
            {label: '创建时间', name: 'createTime', index: 'createTime', width: 60}
        ],
        height: 760,
        rowNum: 20,
        rowList: [20, 50, 80],
        styleUI: 'Bootstrap',
        loadtext: '信息读取中...',
        rownumbers: false,
        rownumWidth: 20,
        autowidth: true,
        multiselect: true,
        sortable: true,
        sortname: 'createTime', //设置默认的排序列
        sortorder: 'desc',
        pager: "#jqGridPager",
        jsonReader: {
            root: "records",
            page: "current",
            total: "pages",
            records: "total"
        },
        prmNames: {
            page: "pageNumber",
            rows: "pageSize",
            order: "order",
        },
        gridComplete: function () {
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        }
    });

    $(window).resize(function () {
        $("#jqGrid").setGridWidth($(".card-body").width());
    });

    function goodsSellStatusFormatter(cellvalue) {
        //商品上架状态 0-上架 1-下架
        if (cellvalue == 0) {
            return "<button type=\"button\" class=\"btn btn-block btn-success btn-sm\" style=\"width: 80%;\">销售中</button>";
        }
        if (cellvalue == 1) {
            return "<button type=\"button\" class=\"btn btn-block btn-secondary btn-sm\" style=\"width: 80%;\">已下架</button>";
        }
    }

    function coverImageFormatter(cellvalue) {
        if (cellvalue.toString().indexOf('http://') > -1) {
            return "<img src='" + cellvalue + "' height=\"80\" width=\"80\" alt='商品主图'/>";
        }
        var ctx = _ctx + cellvalue;
        ctx = ctx.replaceAll('//', '/');
        return "<img src='" + ctx + "' height=\"80\" width=\"80\" alt='商品主图'/>";
    }

});

/**
 * jqGrid重新加载
 */
function reload() {
    var goodsId = $('#goodsId').val() || '';
    var goodsName = $('#goodsName').val() || '';
    var goodsIntro = $('#goodsIntro').val() || '';
    var goodsSellStatus = $('#goodsSellStatus').val() || '';
    $("#jqGrid").jqGrid('setGridParam', {
        page: 1,
        postData: {
            goodsId: goodsId,
            goodsName: goodsName,
            goodsIntro: goodsIntro,
            goodsSellStatus: goodsSellStatus
        }
    }).trigger("reloadGrid");
}

/**
 * 添加商品
 */
function addGoods() {
    window.location.href = _ctx + "admin/goods/add";
}

/**
 * 修改商品
 */
function editGoods() {
    var id = getSelectedRow();
    if (id == null) {
        return;
    }
    window.location.href = _ctx + "admin/goods/edit/" + id;
}

/**
 * 上架
 */
function putUpGoods() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认要执行上架操作吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "PUT",
                    url: _ctx + "admin/goods/status/0",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.code == 200) {
                            swal("上架成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.msg, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    )
}

/**
 * 下架
 */
function putDownGoods() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认要执行下架操作吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                $.ajax({
                    type: "PUT",
                    url: _ctx + "admin/goods/status/1",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function (r) {
                        if (r.code == 200) {
                            swal("下架成功", {
                                icon: "success",
                            });
                            $("#jqGrid").trigger("reloadGrid");
                        } else {
                            swal(r.msg, {
                                icon: "error",
                            });
                        }
                    }
                });
            }
        }
    )
}

/**
 * 同步redisSearch
 */
function syncRedisSearch() {
    swal({
        title: "确认弹框",
        text: "确认要同步RedisSearch吗?",
        icon: "warning",
        button: {
            text: "同步",
            closeModal: false,
        },
        dangerMode: true,
    }).then((flag) => {
            if (flag) {
                return new Promise(function (resolve, reject) {
                    $.ajax({
                        type: "POST",
                        url: _ctx + "admin/goods/syncRs",
                        success: function (r) {
                            if (r.code == 200) {
                                resolve("同步成功");
                            } else {
                                reject(r.msg);
                            }
                        }
                    });
                });
            }
        }
    ).then(value => {
        swal(value, {
            icon: "success",
        });
    }).catch(err => {
        if (err) {
            swal(err, {
                icon: "error",
            });
        } else {
            swal.stopLoading();
            swal.close();
        }
    });
}
