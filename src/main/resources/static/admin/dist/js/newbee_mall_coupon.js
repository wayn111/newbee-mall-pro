$(function () {
    $("#jqGrid").jqGrid({
        url: _ctx + 'admin/coupon/list',
        datatype: "json",
        viewrecords: true,
        colModel: [
            {label: 'id', name: 'couponId', index: 'couponId', key: true, hidden: true},
            {label: '优惠卷名称', name: 'name', index: 'name'},
            {label: '优惠卷简介', name: 'couponDesc', index: 'couponDesc', width: '100px'},
            {label: '数量', name: 'couponTotal', index: 'couponTotal', width: '100px', formatter: totalFormatter},
            {label: '优惠金额', name: 'discount', index: 'discount', width: '100px'},
            {label: '最低消费金额', name: 'min', index: 'min', width: '100px'},
            {label: '限制数量', name: 'couponLimit', index: 'couponLimit', width: '100px', formatter: limitFormatter},
            {label: '有效天数', name: 'days', index: 'days', width: '100px'},
            {label: '赠送类型', name: 'couponType', index: 'couponType', formatter: typeFormatter},
            {label: '优惠卷状态', name: 'status', index: 'status', formatter: statusFormatter},
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
            return "<button type='button' class='btn btn-block btn-success btn-sm' style='width: 80%;'>上架</button>";
        } else if (cellvalue == 1) {
            return "<button type='button' class='btn btn-block btn-warning btn-sm' style='width: 80%;'>已过期</button>";
        } else {
            return "<button type='button' class='btn btn-block btn-danger btn-sm' style='width: 80%;'>下架</button>";
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
    var couponName = $('#couponName').val() || '';
    var couponType = $('#couponType').val() || '';
    var status = $('#status').val() || '';
    var createTime = $('#createTime').val() || '';
    var timeArr = createTime && createTime.split('-') || ['', ''];
    var startTime = timeArr[0].trim();
    var endTime = timeArr[1].trim();

    var page = $("#jqGrid").jqGrid('getGridParam', 'page');
    $("#jqGrid").jqGrid('setGridParam', {
        page: page,
        postData: {
            name: couponName,
            couponType: couponType,
            status: status,
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
            name: '',
            couponDesc: '',
            couponTotal: undefined,
            discount: undefined,
            min: undefined,
            days: undefined,
            couponLimit: 0,
            couponType: 0,
            status: 0,
            goodsType: 0,
            goodsValue: ''
        }
    },
    methods: {
        couponAdd() {
            this.reset();
            this.title = '首页配置项添加';
            $('#couponModal').modal('show');
        },
        couponEdit() {
            this.reset();
            this.title = '首页配置项编辑';
            var that = this;
            var id = getSelectedRow();
            if (id == null) {
                return;
            }
            var url = _ctx + 'admin/coupon/' + id;
            $.get(url, function (res) {
                if (res.code != 200) {
                    swal(res.msg, {
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
            $('#couponModal').modal('show');
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
                name: '',
                couponDesc: '',
                couponTotal: undefined,
                discount: undefined,
                min: undefined,
                days: undefined,
                couponLimit: 0,
                couponType: 0,
                status: 0,
                goodsType: 0,
                goodsValue: ''
            }
        },
        save() {
            var form = this.form
            if (isNull(form.name)) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入优惠卷名称！");
                return;
            }
            if (isNull(form.couponTotal) || form.couponTotal < 0) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入优惠卷数量且不能小于0！");
                return;
            }
            if (isNull(form.discount) || form.discount < 0) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入优惠金额且不能小于0！");
                return;
            }
            if (isNull(form.min) || form.min < 0) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入最少消费金额且不能小于0！");
                return;
            }
            if (isNull(form.days) || form.days < 0) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入有效天数且不能小于0！");
                return;
            }
            if (form.goodsType != 0 && isNull(form.goodsValue)) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入" + this.goodsValueLabel + "！");
                return;
            }
            var url = _ctx + 'admin/coupon/save';
            var data = this.form;
            if (form.couponId != null) {
                url = _ctx + 'admin/coupon/update';
            }
            $.ajax({
                type: 'POST',//方法类型
                url: url,
                contentType: 'application/json',
                data: JSON.stringify(data),
                success: function (result) {
                    if (result.code == 200) {
                        $('#couponModal').modal('hide');
                        swal("保存成功", {
                            icon: "success",
                        });
                        reload();
                    } else {
                        // $('#couponModal').modal('hide');
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
        deleteCoupon() {
            var id = getSelectedRow();
            if (id == null) {
                return;
            }
            var url = _ctx + 'admin/coupon/' + id;
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
