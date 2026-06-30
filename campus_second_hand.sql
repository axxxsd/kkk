/*
 Navicat Premium Data Transfer

 Source Server         : s5
 Source Server Type    : MySQL
 Source Server Version : 90701 (9.7.1)
 Source Host           : localhost:3306
 Source Schema         : campus_second_hand

 Target Server Type    : MySQL
 Target Server Version : 90701 (9.7.1)
 File Encoding         : 65001

 Date: 30/06/2026 16:03:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `product_id` int NOT NULL COMMENT '关联商品ID（对应products表的id）',
  `user_id` int NOT NULL COMMENT '评论用户ID（对应users表的id）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `score` tinyint NULL DEFAULT 5 COMMENT '评分（1-5星，默认5星）',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0正常 1已删除',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comments
-- ----------------------------

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NULL DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` bigint NULL DEFAULT NULL COMMENT '接收者ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息内容',
  `is_read` int NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '站内信表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES (1, 2, 3, '你的商品《高等数学》已被购买，订单号：db23db3e0fd849d993d0c73628097dcc', 0, '2026-06-25 09:53:01');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '订单编号（时间戳+随机数）',
  `buyer_id` bigint NULL DEFAULT NULL COMMENT '买家ID',
  `seller_id` bigint NULL DEFAULT NULL COMMENT '卖家ID',
  `product_id` bigint NULL DEFAULT NULL COMMENT '商品ID',
  `total_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '成交价',
  `status` int NULL DEFAULT 1 COMMENT '订单状态：1-待交易 2-已完成 3-已取消',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址/交易地点',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_buyer`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller`(`seller_id` ASC) USING BTREE,
  INDEX `idx_product`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'db23db3e0fd849d993d0c73628097dcc', 2, 3, 3, 20.00, 1, '21', '2026-06-25 09:53:01');

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品标题',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `images` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片（多个用逗号分隔）',
  `status` int NULL DEFAULT 1 COMMENT '1=在售 2=已售出 0=下架',
  `user_id` bigint NOT NULL COMMENT '发布者ID',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '其他' COMMENT '商品分类（教材/数码/生活用品等）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (1, '二手笔记本', 2000.00, '性能好，无维修史', '1.png', 1, 1, '数码电子', '2026-06-23 09:15:18');
INSERT INTO `products` VALUES (2, '1', 1.00, '1', '1.png', 0, 1, '1', '2026-06-24 16:32:49');
INSERT INTO `products` VALUES (3, '高等数学', 20.00, '全新的高等数学', '2.png', 0, 3, '书籍教材', '2026-06-25 09:52:35');
INSERT INTO `products` VALUES (4, '高等数学', 20.00, '全新的高等数学', '2.png', 1, 3, '书籍教材', '2026-06-25 09:53:43');
INSERT INTO `products` VALUES (5, '张宇考研数学真题大全解（近15年）', 45.00, '正版考研数学真题集，包含数学一/二/三历年真题。书页保存完好，无任何涂写和字迹，附带答案解析。准备考研的同学千万不要错过，绝对是正版好书！', '3.png', 1, 2, '书籍教材', '2026-06-25 10:04:32');
INSERT INTO `products` VALUES (6, '宿舍免打孔桌面增高置物架', 38.00, '拯救杂乱书桌神器！采用加厚碳钢材质，承重力超强。免打孔设计不伤桌面，完美适配宿舍上床下桌。可以用来放书本、笔记本电脑或者水杯，瞬间让桌面空间翻倍，毕业低价出。', '4.png', 1, 2, '生活用品', '2026-06-25 10:04:32');
INSERT INTO `products` VALUES (7, '威尔胜官方比赛级5号足球', 65.00, '世界杯同款配色，手缝工艺，皮质非常柔软。平时在操场踢着玩用过几次，无任何破损和开胶。非常适合体育课、院队训练或者和朋友在操场放松，附赠一个原装打气筒和球针。', '5.png', 1, 2, '运动户外', '2026-06-25 10:04:32');
INSERT INTO `products` VALUES (8, 'Anker安克 65W氮化镓快充充电器', 120.00, '支持多口同时快充，笔记本、平板、手机都能充。体积小巧便携，出差带着特别方便。原包装盒、充电线都在，使用次数很少，九成新，没有任何发热问题。', '6.png', 1, 2, '数码电子', '2026-06-25 10:04:32');
INSERT INTO `products` VALUES (9, '完美日记 小细跟口红 限定版', 89.00, '李佳琦直播间抢购的正装，外壳是精致的细管设计，非常有质感。色号是超级显白的#116，全新未拆封，买多了用不完忍痛割爱。学生党平价替代，送闺蜜或自用都很合适。', '7.png', 1, 2, '美妆护肤', '2026-06-25 10:04:32');
INSERT INTO `products` VALUES (10, '罗技 G304 无线鼠标 电竞版', 79.00, '经典中的经典，手感超级棒，DPI可调节。电池续航能力极强，玩一整周都不用换电池。之前玩游戏留下的，现在换了电脑用不上了。功能一切正常，无任何双击或断连问题。', '8.png', 1, 2, '数码电子', '2026-06-25 10:04:32');
INSERT INTO `products` VALUES (11, '美的 便携式榨汁杯 无线充电款', 55.00, '夏天减肥神器！304不锈钢刀片，动力强劲，几十秒就能打好一杯果汁。小巧轻便，可以直接放进包包里带去图书馆或健身房。充满电可以用十几次，杯身无异味，清洗方便。', '9.png', 1, 2, '生活用品', '2026-06-25 10:04:32');
INSERT INTO `products` VALUES (12, '北欧风水晶台灯 七彩氛围灯', 25.00, '毕业季清闲置啦！这款小夜灯不仅颜值超高，而且是触摸式开关，支持七彩变光和三档亮度调节。晚上在宿舍看书、玩手机或者睡前当氛围灯都非常合适，充一次电能亮好几天，小仙女必备！', '10.png', 1, 2, '生活用品', '2026-06-25 10:04:32');
INSERT INTO `products` VALUES (13, '倍思 H1i 空间音频头戴式降噪耳机', 159.00, '宿舍神器！开启降噪模式后，室友打游戏、看视频的声音瞬间消失，学习效率直线提升。支持空间音频，听歌看电影沉浸感十足。蓝牙5.3连接稳定，续航长达70小时。外观成色很新，仅拆封试用过两次，配件齐全，原装收纳包也在。', '11.png', 1, 2, '数码电子', '2026-06-25 10:10:33');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` int NULL DEFAULT 0 COMMENT '0=普通用户 1=管理员',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'student001', '123456', '张三', '13800138000', NULL, 0);
INSERT INTO `users` VALUES (2, 'admin', '123456', '超级管理员', '13800000000', NULL, 1);
INSERT INTO `users` VALUES (3, 'student002', '123456', '孔天福', NULL, NULL, 0);

SET FOREIGN_KEY_CHECKS = 1;
