<p align="center">
<img src="https://img.shields.io/github/issues/wayn111/newbee-mall" alt="issues">
<img src="https://img.shields.io/github/forks/wayn111/newbee-mall" alt="forks">
<img src="https://img.shields.io/github/stars/wayn111/newbee-mall" alt="stars">
<img src="https://img.shields.io/github/license/wayn111/newbee-mall" alt="license">
</p>

> 本项目是在newbee-mall项目的基础上改造而来, 使用mybatis-plus，集成RedisSearch作为商城搜索中间件，
> 添加了秒杀功能、优惠劵功能以及后台搜索功能，喜欢的话麻烦给我个star

### 简介

本人之前一直是在传统公司上班，接触的都是政企项目，公司各系统业务繁多数据库表设计复杂，多是业务逻辑庞大使用技术也都不算新，算是j2ee时代的遗留产物，鉴于现在springboot、微服务、中间件等等风生水起，又因本人对商城系统情有独钟，因此打算先用springboot搭建一个商城系统，刚好发现了newbee-mall项目，就在此项目的基础上添加了一些自己没有接触过的新功能，一起进步。

- 后台管理模块添加了优惠卷管理、秒杀管理，统计分析
- 商城支持RedisSearch中文分词搜索，支持新品、价格排序搜索
- 添加Spring事件监听机制，解耦下单流程
- 集成Pace页面，添加网页进度条
- 前台添加了秒杀专区，可以购买秒杀商品
- 前台添加了优惠卷领取页面，再订单结算页面可以选择优惠卷使用
- 支付时添加了支付宝沙箱支付
- 集成spring-session-redis，支持分布式部署
- 本项目秉持原作者简单易用的原则，代码书写清晰，注释完整，便于新人理解，快速上手
- [本项目源码](https://github.com/wayn111/newbee-mall)
- [在线地址](http://121.89.238.61/newbeemall)

### 2022年4月23日更新日志
商城登录页面添加滑块验证码登录,优化登录体验

1. 集成`tianai-captcha`滑块验证码,支持后端验证码校验
<img style="width:80%" src="https://img-blog.csdnimg.cn/dd521d119cde4eddb934eda27532cc95.png? x-oss-process=image/watermark, type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAV2F5bjExMQ==,size_20,color_FFFFFF,t_70,g_se,x_16">

### 2022年4月12日更新日志

使用Spring事件监听机制，解耦下单流程，集成Pace美化商城进度条

1. 添加Spring事件监听机制，解耦下单流程，将下单流程拆解为订单校验、生成订单号、发送事件异步保存订单流程
2. 集成Pace页面，添加网页进度条，美化商城页面

### 2022年3月27日更新日志

商城添加RedisSearch搜索，支持中文分词搜索，推荐、新品、价格排序搜索

1. 添加RedisSearch测试用例
2. 后台添加RedisSearch同步按钮
3. 商城支持RedisSearch中文分词搜索

### 2022年3月21日更新日志

升级版本号至2.1.3，是一个功能完善版本

1. 支持分布式部署
2. 升级spring版本至2.6.4
3. 升级mybatis-plus至3.5.1
4. 添加Dockerfile文件支持容器部署

### 2021年5月30日更新日志

升级版本号至2.1.2，是一个代码优化版本

1. 秒杀专区购物车数量显示错误bug修复
2. 支付宝沙箱支付bug修复，显示沙箱账号
   ![$K%_1@7UM)9$)Q{CWC{Z3DO.png](https://upload-images.jianshu.io/upload_images/10522714-864bb229d2460a4e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
3. 将默认编辑器替换为[froala editor](https://froala.com/wysiwyg-editor/)
   ，[与tinyMCE比较](https://froala.com//blog/editor/froala-vs-tinymce/)
   ![froala编辑器.png](https://upload-images.jianshu.io/upload_images/10522714-9ca28b3808115969.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
4. 添加站点演示模式，通过拦截器对不允许操作的后台路径进行错误提示处理
   ![QQ截图20210530155159.png](https://upload-images.jianshu.io/upload_images/10522714-1631176d29e984a0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
5. 升级pom文件部分依赖

### 2021年1月14日 秒杀接口升级

本次升级主要在原有秒杀功能的基础上进行了完善，秒杀优化如下：

1. 秒杀页面静态化
2. 添加了秒杀接口限流，基于springAOP实现
3. 添加了秒杀接口防止重复提交，基于spring拦截器实现
4. 使用令牌桶算法过滤用户请求
5. 使用redis-set数据结构判断用户是否买过秒杀商品
6. 使用redis配合lua脚本进行原子自减，判断商品缓存库存是否大于0
7. 获取商品缓存，判断秒杀商品是否再有效期内
8. 执行存储过程（减库存 + 记录购买行为）
9. 使用redis-set数据结构记录购买过的用户
10. 返回用户秒杀成功VO
11. 下单后启用秒杀订单5分钟未支付超期任务
12. 订单5分钟内未支付则自动取消订单并回退库存

------

### 开发部署

```
# 1. 克隆项目
git clone git@github.com:wayn111/newbee-mall.git

# 2. 导入项目依赖
将newbee-mall目录用idea打开，导入maven依赖

# 3. 安装Mysql8.0+、Redis3.0+、Jdk8+、Maven3.5+

# 4. 导入sql文件
在项目根目录下sql文件夹下，找到`newbee_mall_db_包含秒杀and优惠卷.sql`文件，新建mysql数据库newbee_mall_db，导入其中

# 5. 解压项目图片
将项目根目录下upload.zip文件加压缩到D盘upload文件夹中，eg:D:\\upload

# 6. 修改Mysql、Redis连接配置
修改`application-dev.yml`文件中数据连接配置相关信息

# 7. 启动项目
找到NewBeeMallApplication文件，右键`run AdminApplication`，启动项目

# 8. 访问
打开浏览器输入：http://localhost:84/newbeemall
```

------

**这里推荐另一套H5商城项目， [waynboot-mall](https://github.com/wayn111/waynboot-mall) 是一套全部开源的微商城项目，包含一个运营后台、h5商城和api接口。
实现了一个商城所需的首页展示、商品分类、商品详情、sku详情、商品搜索、加入购物车、结算下单、订单状态流转、商品评论等一系列功能。
技术上基于Springboot2.0，整合了Redis、RabbitMQ、ElasticSearch等常用中间件，
贴近生产环境实际经验开发而来不断完善、优化、改进中。                                        
在线地址：http://82.157.141.70/mall**

### 在线截图

| 商城首页 ![index](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/index-01.gif) | 商品搜索 ![search](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/search.png)|
| ---------------------------------- | ---------------------------------- |
| 购物车 ![cart](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/cart.png) | 订单结算 ![settle](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/settle.png)|
| 订单列表 ![orders](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/orders.png) | 支付页面 ![settle](https://newbee-mall.oss-cn-beijing.aliyuncs.com/poster/product/wx-pay.png)
| 分类管理 ![分类管理](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/4b04d591cf7c4b64b69998936298a521~tplv-k3u1fbpfcp-watermark.image) | 会员管理 ![会员管理](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/92d7d36101d14fd8bb2a78ac776f4061~tplv-k3u1fbpfcp-watermark.image)|
| 优惠劵管理 ![优惠劵管理](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d795de360a0042a88e66f7e40807dfcd~tplv-k3u1fbpfcp-watermark.image) | 商品管理 ![商品管理](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/9cbe8bcdba2448c091a6f56a85e4277f~tplv-k3u1fbpfcp-watermark.image)|
| 秒杀管理 ![秒杀管理](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e1a2adfd2300497b9f5e95aade9b7fe7~tplv-k3u1fbpfcp-watermark.image)| 订单管理 ![订单管理](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/612fa67ad62d48929ae64d10e9ea58c7~tplv-k3u1fbpfcp-watermark.image)|

------

##### 秒杀专区

秒杀专区为用户展示了后台设置的秒杀商品，在秒杀有效期内可以进行商品秒杀操作. 秒杀接口使用了接口限流、Redis以及储存过程提高秒杀操作的tps

![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/e712d152fec14cc2ab2ba49a7fa8ffaa~tplv-k3u1fbpfcp-watermark.image)
![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6cbe7bc5834947f888f3264de5b377c7~tplv-k3u1fbpfcp-watermark.image)
![](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6dc788dffb534669888aff9791498be0~tplv-k3u1fbpfcp-watermark.image)
![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/c3159a78f6204176822baa2823b7005e~tplv-k3u1fbpfcp-watermark.image)
------

##### 优惠劵领取

优惠劵页面为用户展示了后台设置的可用优惠劵，在下单时可以使用优惠卷减少下单金额

![](https://p1-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d182eaea972b4de7862207bcf1910551~tplv-k3u1fbpfcp-watermark.image)
![](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/263b6e8143a343e5b4d759df289135a3~tplv-k3u1fbpfcp-watermark.image)
------

##### 商城15天交易额统计

商城15天交易额统计可以为商城运营人员展示商城近期总交易金额

![](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6c7b1a13fa17400ca72380daca83e3b0~tplv-k3u1fbpfcp-watermark.image)

------

### 感谢

* [newbee-mall](https://github.com/newbee-ltd/newbee-mall) 项目原作者十三提供的基础项目支持

##### 参考资料

* [秒杀架构模型设计](https://www.cnblogs.com/wyq178/p/11261711.html)

* [Java高并发秒杀API（慕课网）](https://github.com/liyifeng1994/seckill)

* [⭐⭐⭐⭐秒杀系统设计与实现.互联网工程师进阶与分析🙋🐓](https://github.com/qiurunze123/miaosha)

> 推荐一下本人的[后台权限管理系统](https://github.com/wayn111/spring-mybatis-admin) ,集成了消息通知，任务调度，代码生成等常用功能，易于上手，学习，使用二次开发
