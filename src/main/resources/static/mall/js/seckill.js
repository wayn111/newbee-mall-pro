// 存放主要交换逻辑js代码
// javascript 模块化
var seckill = {
    // 封装秒杀相关ajax的url
    URL: {
        basePath: function () {
            return _ctx;
        },
        now: function () {
            return seckill.URL.basePath() + 'seckill/time/now';
        },
        exposer: function (seckillId) {
            return seckill.URL.basePath() + 'seckill/' + seckillId + '/exposer';
        },
        execution: function (seckillId, md5) {
            return seckill.URL.basePath() + 'seckill/' + seckillId + '/' + md5 + '/execution';
        },
        settle: function (seckillSuccessId, md5) {
            return seckill.URL.basePath() + 'seckill/' + seckillSuccessId + '/' + md5 + '/settle';
        }
    },
    // 处理秒杀逻辑
    handleSeckill: function (seckillId, node) {
        // 获取秒杀地址，控制显示逻辑，执行秒杀
        // node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        node.html('秒杀开始');
        console.log('exposerUrl=' + seckill.URL.exposer(seckillId));//TODO
        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            // 在回调函数中，执行交互流程
            if (result['code'] == 200) {
                var exposer = result['map']['exposer'];
                if (exposer['exposed']) {
                    // 开启秒杀
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log('killUrl=' + killUrl);
                    $('#killBtn').one('click', function () {
                        // 执行秒杀请求
                        // 1.先禁用按钮
                        $(this).addClass('disabled');
                        // 2.发送秒杀请求
                        $.post(killUrl, {}, function (result) {
                            if (result['code'] == 200) {
                                var seckillSuccess = result['map']['seckillSuccess'];
                                var seckillSuccessId = seckillSuccess['seckillSuccessId'];
                                var md5 = seckillSuccess['md5'];
                                var settleUrl = seckill.URL.settle(seckillSuccessId, md5);
                                location.href = settleUrl;
                            } else {
                                swal(result['msg'], {
                                    icon: "error",
                                });
                            }
                        });
                    });
                    // node.show();
                    $('#killBtn').attr('disabled', false);
                } else {
                    // 未开启秒杀
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    // 重新计算计时逻辑
                    seckill.countdown(seckillId, now, start, end);
                }
            } else {
                console.log('result=' + result);
            }
        });
    },
    // 倒计时
    countdown: function (seckillId, nowTime, startTime, endTime) {
        // 时间判断
        var seckillBox = $('#seckillBox');
        if (nowTime > endTime) {
            // 秒杀结束
            seckillBox.html('秒杀结束!');
        } else if (nowTime < startTime) {
            // 秒杀未开始，计时事件绑定
            var killTime = new Date(startTime + 1000);
            seckillBox.countdown(killTime, function (event) {
                // 时间格式
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                seckillBox.html(format);
                // 时间完成后回调事件
            }).on('finish.countdown', function () {
                // 获取秒杀地址，控制显示逻辑，执行秒杀
                seckill.handleSeckill(seckillId, seckillBox);
            });
        } else {
            // 秒杀开始
            seckill.handleSeckill(seckillId, seckillBox);
        }
    },
    // 详情页秒杀逻辑
    detail: {
        // 详情页初始化
        init: function (params) {
            // 规划我们的交互流程
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            // 计时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(), {}, function (result) {
                if (result['code'] == 200) {
                    var nowTime = result['map']['now'];
                    // 时间判断，计时交互
                    seckill.countdown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log(result['msg']);
                }
            });
        }
    }
}
