> 本项目是在newbee-mall项目的基础上改造而来,将orm层由mybatis替换为mybatis-plus，添加了秒杀功能、优惠劵功能以及后台搜索功能，喜欢的话麻烦给我个star

1. 后台管理模块添加了优惠卷、秒杀管理，统计分析
2. 前台添加了秒杀专区，可以购买秒杀商品
3. 前台添加了优惠卷领取页面，再订单结算页面可以选择优惠卷使用
4. 支付时添加了支付宝沙箱支付
5. 本项目秉持简单易用的原则，便于新人理解，快速上手

#### 在线地址：<a href="http://wayn.xin/mall">newebee-mall</a>

| 商城首页 ![index](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/index-01.gif) | 商品搜索 ![search](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/search.png)|
| ---------------------------------- | ---------------------------------- |
| 购物车 ![cart](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/cart.png) | 订单结算 ![settle](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/settle.png)|
| 订单列表 ![orders](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/orders.png) | 支付页面 ![settle](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/wx-pay.png)
| 分类管理 ![分类管理](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4b04d591cf7c4b64b69998936298a521~tplv-k3u1fbpfcp-watermark.image) | 会员管理 ![会员管理](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/92d7d36101d14fd8bb2a78ac776f4061~tplv-k3u1fbpfcp-watermark.image)|
| 优惠劵管理 ![优惠劵管理](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d795de360a0042a88e66f7e40807dfcd~tplv-k3u1fbpfcp-watermark.image) | 商品管理 ![商品管理](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9cbe8bcdba2448c091a6f56a85e4277f~tplv-k3u1fbpfcp-watermark.image)|
| 秒杀管理 ![秒杀管理](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e1a2adfd2300497b9f5e95aade9b7fe7~tplv-k3u1fbpfcp-watermark.image)| 订单管理 ![订单管理](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/612fa67ad62d48929ae64d10e9ea58c7~tplv-k3u1fbpfcp-watermark.image)|

#### 秒杀专区

秒杀专区为用户展示了后台设置的秒杀商品，在秒杀有效期内可以进行商品秒杀操作. 后台使用了储存过程提高秒杀操作的tps

![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e712d152fec14cc2ab2ba49a7fa8ffaa~tplv-k3u1fbpfcp-watermark.image)
![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6cbe7bc5834947f888f3264de5b377c7~tplv-k3u1fbpfcp-watermark.image)
![](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6dc788dffb534669888aff9791498be0~tplv-k3u1fbpfcp-watermark.image)
![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c3159a78f6204176822baa2823b7005e~tplv-k3u1fbpfcp-watermark.image)

#### 优惠劵领取

优惠劵页面为用户展示了后台设置的可用优惠劵，在下单时可以使用优惠卷减少下单金额

![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d182eaea972b4de7862207bcf1910551~tplv-k3u1fbpfcp-watermark.image)
![](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/263b6e8143a343e5b4d759df289135a3~tplv-k3u1fbpfcp-watermark.image)

#### 商城15天交易额统计

商城15天交易额统计可以为商城运营人员展示商城近期总交易金额

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6c7b1a13fa17400ca72380daca83e3b0~tplv-k3u1fbpfcp-watermark.image)

#### 感谢[newbee-mall](https://github.com/newbee-ltd/newbee-mall) 项目原作者十三提供的基础项目支持

> 推荐一下本人的[后台权限管理系统](https://github.com/wayn111/crowd-admin), 集成了消息通知，任务调度，代码生成等常用功能，易于上手，学习，使用二次开发
