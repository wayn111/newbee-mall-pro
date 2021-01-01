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

 Date: 01/01/2021 14:31:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_newbee_mall_seckill
-- ----------------------------
DROP TABLE IF EXISTS `tb_newbee_mall_seckill`;
CREATE TABLE `tb_newbee_mall_seckill`  (
  `seckill_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `goods_id` int(11) NOT NULL COMMENT '秒杀商品ID',
  `seckill_price` int(11) NOT NULL COMMENT '秒杀价格',
  `seckill_num` int(11) NOT NULL COMMENT '秒杀数量',
  `limit_num` smallint(4) NOT NULL COMMENT '限购数量',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '秒杀商品状态（0下架，1上架）',
  `seckill_begin` datetime(0) NULL DEFAULT NULL COMMENT '秒杀开始时间',
  `seckill_end` datetime(0) NULL DEFAULT NULL COMMENT '秒杀结束时间',
  `seckill_rank` int(11) NULL DEFAULT NULL COMMENT '秒杀商品排序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
  PRIMARY KEY (`seckill_id`) USING BTREE,
  INDEX `status_index`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_newbee_mall_seckill_success
-- ----------------------------
DROP TABLE IF EXISTS `tb_newbee_mall_seckill_success`;
CREATE TABLE `tb_newbee_mall_seckill_success`  (
  `sec_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `seckill_id` int(11) NOT NULL COMMENT '商品商品id',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `state` tinyint(4) NOT NULL DEFAULT -1 COMMENT '状态信息：-1无效，0成功，1已付款，2已发货',
  `create_time` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`sec_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `seckill_user_id`(`seckill_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '秒杀库存表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
