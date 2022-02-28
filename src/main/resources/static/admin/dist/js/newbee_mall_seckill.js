$(function () {
    $("#jqGrid").jqGrid({
        url: _ctx + 'admin/seckill/list',
        datatype: "json",
        viewrecords: true,
        colModel: [
            {label: '序号', name: 'seckillId', index: 'seckillId', key: true, width: '50px'},
            {label: '秒杀商品Id', name: 'goodsId', index: 'goodsId'},
            {label: '秒杀价格', name: 'seckillPrice', index: 'seckillPrice'},
            {label: '秒杀数量', name: 'seckillNum', index: 'seckillNum', width: '100px'},
            {label: '上架状态', name: 'status', index: 'status', formatter: statusFormatter},
            {label: '秒杀开始', name: 'seckillBegin', index: 'seckillBegin'},
            {label: '秒杀结束', name: 'seckillEnd', index: 'seckillEnd'},
            {label: '商品排序', name: 'seckillRank', index: 'seckillRank'},
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
            // 隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        }
    });

    function statusFormatter(cellvalue) {
        if (cellvalue == 0) {
            return "<button type='button' class='btn btn-block btn-danger btn-sm' style='width: 80%;'>已下架</button>";
        } else {
            return "<button type='button' class='btn btn-block btn-success btn-sm' style='width: 80%;'>上架中</button>";
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

    $('#seckillBegin, #seckillEnd').daterangepicker({
        singleDatePicker: true,
        autoUpdateInput: false,
        showDropdowns: true,
        timePicker: true,
        timePicker24Hour: true,
        timePickerSeconds: true,
        startDate: moment().hours(0).minutes(0).seconds(0), //设置开始日期
        locale: datepickerLocale()
    });

    $('#seckillBegin, #seckillEnd').on('apply.daterangepicker', function (ev, picker) {
        vm.form[$(this).attr('id')] = picker.startDate.format('YYYY-MM-DD HH:mm:ss')
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
        form: {
            goodsId: '',
            seckillPrice: '',
            seckillNum: undefined,
            status: 0,
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
                    swal(res.msg, {
                        icon: "error",
                    });
                    return
                }
                that.form = res.map.data
            }, 'json')
            $('#seckillModal').modal('show');
        },
        reset() {
            $('#edit-error-msg').css("display", "none");
            vm.form = {
                goodsId: '',
                seckillPrice: '',
                seckillNum: undefined,
                status: 0,
                seckillBegin: undefined,
                seckillEnd: undefined,
                seckillRank: undefined,
            }
        },
        save() {
            var form = this.form
            if (isNull(form.goodsId)) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入商品ID！");
                return;
            }
            if (isNull(form.seckillPrice)) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入秒杀价格！");
                return;
            }
            if (isNull(form.seckillNum)) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("请输入秒杀数量！");
                return;
            }
            if (isNull(form.seckillBegin)) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("秒杀开始时间！");
                return;
            }
            if (isNull(form.seckillEnd)) {
                $('#edit-error-msg').css("display", "block");
                $('#edit-error-msg').html("秒杀结束时间！");
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
                        // $('#seckillModal').modal('hide');
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
