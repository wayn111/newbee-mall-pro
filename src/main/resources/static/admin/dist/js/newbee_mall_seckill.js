$(function () {
    $("#jqGrid").jqGrid({
        url: _ctx + 'admin/seckill/list',
        datatype: "json",
        viewrecords: true,
        colModel: [
            {label: 'id', name: 'seckillId', index: 'seckillId', key: true, hidden: true},
            {label: '秒杀商品Id', name: 'goodsId', index: 'goodsId'},
            {label: '秒杀价格', name: 'seckillPrice', index: 'seckillPrice'},
            {label: '秒杀数量', name: 'seckillNum', index: 'seckillNum', width: '100px', formatter: totalFormatter},
            {label: '限购数量', name: 'limitNum', index: 'limitNum', width: '100px', formatter: limitFormatter},
            {label: '秒杀开始', name: 'seckillBegin', index: 'seckillBegin', width: '100px'},
            {label: '秒杀结束', name: 'seckillEnd', index: 'seckillEnd', width: '100px'},
            {label: '商品排序', name: 'seckillRank', index: 'seckillRank', width: '100px'},
            {label: '创建时间', name: 'createTime', index: 'createTime'}
        ],
        height: 560,
        rowNum: 10,
        rowList: [10, 20, 50],
        styleUI: 'Bootstrap',
        loadtext: '信息读取中...',
        rownumbers: false,
        rownumWidth: 20,
        autowidth: true,
        multiselect: true,
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

    function totalFormatter(cellvalue) {
        if (cellvalue == 0) {
            return "无限制";
        }
        return cellvalue;
    }

    function typeFormatter(cellvalue) {
        if (cellvalue == 0) {
            return "通用卷";
        } else if (cellvalue == 1) {
            return "注册用卷";
        } else {
            return "优惠码兑换卷";
        }
    }

    function limitFormatter(cellvalue) {
        if (cellvalue == 0) {
            return "不限制";
        } else if (cellvalue == 1) {
            return "限领一张";
        }
    }

    function statusFormatter(cellvalue) {
        if (cellvalue == 0) {
            return "<button type='button' class='btn btn-block btn-success btn-sm' style='width: 80%;'>可用</button>";
        } else if (cellvalue == 1) {
            return "<button type='button' class='btn btn-block btn-warning btn-sm' style='width: 80%;'>已过期</button>";
        } else {
            return "<button type='button' class='btn btn-block btn-danger btn-sm' style='width: 80%;'>已下架</button>";
        }
    }

    $(window).resize(function () {
        $("#jqGrid").setGridWidth($(".card-body").width());
    });

    $('#createTime').daterangepicker({
        autoUpdateInput: false,
        showDropdowns: true,
        startDate: moment().startOf('hour'),
        endDate: moment().startOf('hour').add(12, 'hour'),
        locale: datepickerLocale()
    });

    $('#createTime').on('apply.daterangepicker', function (ev, picker) {
        $(this).val(picker.startDate.format('YYYY/MM/DD') + ' - ' + picker.endDate.format('YYYY/MM/DD'));
    });
});

/**
 * jqGrid重新加载
 */
function reload() {
    var goodsId = $('#queryGoodsId').val() || '';
    var createTime = $('#createTime').val() || '';
    var timeArr = createTime && createTime.split('-') || ['', ''];
    var startTime = timeArr[0].trim();
    var endTime = timeArr[1].trim();

    var page = $("#jqGrid").jqGrid('getGridParam', 'page');
    $("#jqGrid").jqGrid('setGridParam', {
        page: page,
        postData: {
            goodsId: goodsId,
            startTime: startTime,
            endTime: endTime
        }
    }).trigger("reloadGrid");
}

var vm = new Vue({
    el: '#app',
    data: {
        title: '',
        goodsValueLabel: '',
        form: {
            goodsId: '',
            seckillPrice: '',
            seckillNum: undefined,
            limitNum: undefined,
            seckillBegin: undefined,
            seckillEnd: undefined,
            seckillRank: undefined,
        }
    },
    methods: {
        seckillAdd() {
            this.reset();
            this.title = '秒杀商品添加';
            $('#seckillModal').modal('show');
        },
        seckillEdit() {
            this.reset();
            this.title = '秒杀商品编辑';
            var that = this;
            var id = getSelectedRow();
            if (id == null) {
                return;
            }
            var url = _ctx + 'admin/seckill/' + id;
            $.get(url, function (res) {
                if (res.code != 200) {
                    swal("操作失败", {
                        icon: "error",
                    });
                    return
                }
                that.form = res.map.data
                if (that.form.goodsType == 1) {
                    that.goodsValueLabel = '类目id';
                } else if (that.form.goodsType == 2) {
                    that.goodsValueLabel = '商品id'
                }
            }, 'json')
            $('#seckillModal').modal('show');
        },
        changeGoodsType(goodsType) {
            this.form.goodsValue = '';
            if (goodsType == 1) {
                this.goodsValueLabel = '类目id'
            } else if (goodsType == 2) {
                this.goodsValueLabel = '商品id'
            }
        },
        reset() {
            $('#edit-error-msg').css("display", "none");
            vm.form = {
                goodsId: '',
                seckillPrice: '',
                seckillNum: undefined,
                limitNum: undefined,
                seckillBegin: undefined,
                seckillEnd: undefined,
                seckillRank: undefined,
            }
        },
        save() {
            var form = this.form
            if (isNull(form.name)) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入优惠卷名称！");
                return;
            }
            var url = _ctx + 'admin/seckill/save';
            var data = this.form;
            if (form.seckillId != null) {
                url = _ctx + 'admin/seckill/update';
            }
            $.ajax({
                type: 'POST',//方法类型
                url: url,
                contentType: 'application/json',
                data: JSON.stringify(data),
                success: function (result) {
                    if (result.code == 200) {
                        $('#seckillModal').modal('hide');
                        swal("保存成功", {
                            icon: "success",
                        });
                        reload();
                    } else {
                        $('#seckillModal').modal('hide');
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
        },
        deleteSeckill() {
            var id = getSelectedRow();
            if (id == null) {
                return;
            }
            var url = _ctx + 'admin/seckill/' + id;
            $.ajax({
                url: url,
                cache: false,
                type: 'delete',
                dataType: 'json',
                success: function (result) {
                    if (result.code == 200) {
                        swal("删除成功", {
                            icon: "success",
                        });
                        reload();
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
        }
    }
})
