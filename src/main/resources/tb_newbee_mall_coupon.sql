/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.31.49
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : 192.168.31.49:3306
 Source Schema         : newbee_mall_db

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 13/05/2020 11:02:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_newbee_mall_coupon
-- ----------------------------
DROP TABLE IF EXISTS `tb_newbee_mall_coupon`;
CREATE TABLE `tb_newbee_mall_coupon`  (
  `coupon_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券名称',
  `coupon_desc` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '优惠券介绍，通常是显示优惠券使用限制文字',
  `coupon_total` int(0) NOT NULL DEFAULT 0 COMMENT '优惠券数量，如果是0，则是无限量',
  `discount` int(0) NULL DEFAULT 0 COMMENT '优惠金额，',
  `min` int(0) NULL DEFAULT 0 COMMENT '最少消费金额才能使用优惠券。',
  `coupon_limit` tinyint(0) NULL DEFAULT 1 COMMENT '用户领券限制数量，如果是0，则是不限制；默认是1，限领一张.',
  `coupon_type` tinyint(0) NULL DEFAULT 0 COMMENT '优惠券赠送类型，如果是0则通用券，用户领取；如果是1，则是注册赠券；如果是2，则是优惠券码兑换；',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '优惠券状态，如果是0则是正常可用；如果是1则是过期; 如果是2则是下架。',
  `goods_type` tinyint(0) NULL DEFAULT 0 COMMENT '商品限制类型，如果0则全商品，如果是1则是类目限制，如果是2则是商品限制。',
  `goods_value` varchar(1023) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '商品限制值，goods_type如果是0则空集合，如果是1则是类目集合，如果是2则是商品集合。',
  `code` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '优惠券兑换码',
  `days` smallint(0) NULL DEFAULT 0 COMMENT '基于领取时间的有效天数days。',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(0) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`coupon_id`) USING BTREE,
  INDEX `code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券信息及规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_newbee_mall_coupon
-- ----------------------------
INSERT INTO `tb_newbee_mall_coupon` VALUES (1, '限时满减券', '全场通用', 0, 5, 99, 1, 0, 0, 0, '', NULL, 10, '2018-02-01 00:00:00', '2018-02-01 00:00:00', 0);
INSERT INTO `tb_newbee_mall_coupon` VALUES (2, '限时满减券', '全场通用', 0, 10, 900, 1, 0, 0, 0, '', NULL, 10, '2018-02-01 00:00:00', '2018-02-01 00:00:00', 0);
INSERT INTO `tb_newbee_mall_coupon` VALUES (3, '新用户优惠券', '全场通用', 0, 10, 99, 1, 1, 0, 0, '', NULL, 10, '2018-02-01 00:00:00', '2018-02-01 00:00:00', 0);
INSERT INTO `tb_newbee_mall_coupon` VALUES (8, '可兑换优惠券', '全场通用', 0, 15, 900, 1, 2, 0, 0, '', 'DC6FF8SE', 7, '2018-12-23 09:29:57', '2018-12-23 09:29:57', 0);
INSERT INTO `tb_newbee_mall_coupon` VALUES (11, '限时满减券', '全场通用,数量有限,先到先得', 100, 100, 1000, 1, 0, 0, 0, '', NULL, 3, '2020-05-09 00:54:08', NULL, 0);
INSERT INTO `tb_newbee_mall_coupon` VALUES (12, '荣耀商品满减', '荣耀10x', 0, 8, 80, 1, 0, 0, 2, '10700', NULL, 7, '2020-05-11 22:45:15', NULL, 0);
INSERT INTO `tb_newbee_mall_coupon` VALUES (13, '华为分类满减', '华为手机通用', 0, 15, 1500, 1, 0, 0, 1, '45,46', NULL, 7, '2020-05-11 23:15:09', NULL, 0);

-- ----------------------------
-- Table structure for tb_newbee_mall_coupon_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_newbee_mall_coupon_user`;
CREATE TABLE `tb_newbee_mall_coupon_user`  (
  `coupon_user_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `coupon_id` bigint(0) NOT NULL COMMENT '优惠券ID',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '使用状态, 如果是0则未使用；如果是1则已使用；如果是2则已过期；如果是3则已经下架；',
  `used_time` datetime(0) NULL DEFAULT NULL COMMENT '使用时间',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '有效期开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '有效期截至时间',
  `order_id` bigint(0) NULL DEFAULT NULL COMMENT '订单ID',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(0) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`coupon_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券用户使用表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
