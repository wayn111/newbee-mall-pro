/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3306
 Source Schema         : newbee_mall_db

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 21/12/2020 21:40:33
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_newbee_mall_seckill
-- ----------------------------
DROP TABLE IF EXISTS `tb_newbee_mall_seckill`;
CREATE TABLE `tb_newbee_mall_seckill`  (
  `seckill_id` int(11) NOT NULL COMMENT '自增ID',
  `goods_id` int(11) NOT NULL COMMENT '秒杀商品ID',
  `seckill_price` int(11) NOT NULL COMMENT '秒杀价格',
  `seckill_num` int(11) NOT NULL COMMENT '秒杀数量',
  `limit_num` smallint(4) NOT NULL COMMENT '限购数量',
  `seckill_begin` datetime(0) NOT NULL COMMENT '秒杀开始时间',
  `seckill_end` datetime(0) NOT NULL COMMENT '秒杀结束时间',
  `seckill_rank` int(11) NULL DEFAULT NULL COMMENT '秒杀商品排序',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`seckill_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
