/*
 Navicat Premium Data Transfer

 Source Server         : majun
 Source Server Type    : MySQL
 Source Server Version : 80016
 Source Host           : rm-bp1262h6dc7w4hy319o.mysql.rds.aliyuncs.com:3306
 Source Schema         : newbee_mall_db

 Target Server Type    : MySQL
 Target Server Version : 80016
 File Encoding         : 65001

 Date: 20/12/2020 18:47:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_newbee_mall_seckill
-- ----------------------------
DROP TABLE IF EXISTS `tb_newbee_mall_seckill`;
CREATE TABLE `tb_newbee_mall_seckill`  (
  `seckill_id` int(11) NOT NULL COMMENT '自增ID',
  `seckill_price` int(11) NOT NULL COMMENT '秒杀价格',
  `seckill_num` int(11) NOT NULL COMMENT '秒杀数量',
  `limit_num` smallint(4) NOT NULL COMMENT '限购数量',
  `seckill_begin` datetime(0) NULL DEFAULT NULL COMMENT '秒杀开始时间',
  `seckill_end` datetime(0) NULL DEFAULT NULL COMMENT '秒杀结束时间',
  `seckill_rank` int(11) NULL DEFAULT NULL COMMENT '秒杀商品排序',
  `goods_id` int(11) NOT NULL COMMENT '秒杀商品ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`seckill_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
