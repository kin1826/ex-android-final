-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th5 27, 2026 lúc 07:46 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

-- --------------------------------------------------
-- Phần test

SELECT * FROM users;
SELECT * FROM categories;
SELECT * FROM games;
SELECT * FROM wishlists;
-- Nếu có game mua từ trước thì bỏ Note đoạn này và chạy để Sync Game
-- INSERT IGNORE INTO libraries (user_id, game_id, purchase_date)
-- SELECT o.user_id, oi.game_id, o.created_at
-- FROM orders o
-- JOIN order_items oi ON o.id = oi.order_id
-- WHERE o.status = 'COMPLETED';

-- --------------------------------------------------
-- Phần chính

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `gamestore_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `icon` varchar(10) DEFAULT '?'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `categories`
--

INSERT INTO `categories` (`id`, `name`, `icon`) VALUES
(1, 'Action', '⚔️'),
(2, 'RPG', '🏰'),
(3, 'Sports', '⚽'),
(4, 'Simulation', '🌿'),
(5, 'Adventure', '🗺️');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `coupons`
--

CREATE TABLE `coupons` (
  `id` int(11) NOT NULL,
  `code` varchar(50) NOT NULL,
  `discount_amount` decimal(10,2) DEFAULT 0.00,
  `min_order_value` decimal(10,2) DEFAULT 0.00,
  `expires_at` datetime DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `coupons`
--

INSERT INTO `coupons` (`id`, `code`, `discount_amount`, `min_order_value`, `expires_at`, `is_active`) VALUES
(1, 'SALE50K', 50000.00, 200000.00, '2027-12-31 00:00:00', 1),
(2, 'NEWUSER', 30000.00, 100000.00, '2027-12-31 00:00:00', 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `games`
--

CREATE TABLE `games` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `original_price` decimal(10,2) DEFAULT 0.00,
  `discount_percent` int(11) DEFAULT 0,
  `rating` float DEFAULT 0,
  `review_count` int(11) DEFAULT 0,
  `genre` varchar(100) DEFAULT NULL,
  `developer` varchar(200) DEFAULT NULL,
  `thumbnail_url` varchar(500) DEFAULT NULL,
  `is_featured` tinyint(1) DEFAULT 0,
  `is_hot` tinyint(1) DEFAULT 0,
  `is_new` tinyint(1) DEFAULT 1,
  `stock` int(11) DEFAULT 999,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `banner_url` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `games`
--

INSERT INTO `games` (`id`, `title`, `description`, `price`, `original_price`, `discount_percent`, `rating`, `review_count`, `genre`, `developer`, `thumbnail_url`, `is_featured`, `is_hot`, `is_new`, `stock`, `created_at`, `banner_url`) VALUES
(1, 'Cyberpunk 2077', 'Game nhập vai thế giới mở trong tương lai tại thành phố Night City.', 450000.00, 750000.00, 40, 4.8, 12400, 'Action RPG', 'CD Projekt Red', 'https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/header.jpg', 1, 1, 0, 999, '2026-05-23 14:03:50', 'https://cdn.cloudflare.steamstatic.com/steam/apps/1091500/page_bg_generated_v6b.jpg'),
(2, 'Hogwarts Legacy', 'Khám phá thế giới phù thủy Harry Potter với hàng trăm phép thuật.', 520000.00, 650000.00, 20, 4.9, 8900, 'Adventure', 'Avalanche Software', 'https://cdn.cloudflare.steamstatic.com/steam/apps/990080/header.jpg', 1, 0, 0, 999, '2026-05-23 14:03:50', 'https://cdn.cloudflare.steamstatic.com/steam/apps/990080/page_bg_generated_v6b.jpg'),
(3, 'FC 25', 'Siêu phẩm bóng đá mô phỏng thực tế nhất với hàng nghìn cầu thủ.', 350000.00, 350000.00, 0, 4.5, 5600, 'Sports', 'EA Sports', 'https://cdn.cloudflare.steamstatic.com/steam/apps/2195250/header.jpg', 0, 1, 0, 999, '2026-05-23 14:03:50', NULL),
(4, 'Stardew Valley', 'Xây dựng trang trại, kết bạn với dân làng và khám phá hang động.', 89000.00, 89000.00, 0, 5, 32000, 'Simulation', 'ConcernedApe', 'https://cdn.cloudflare.steamstatic.com/steam/apps/413150/header.jpg', 0, 0, 0, 999, '2026-05-23 14:03:50', NULL),
(5, 'Elden Ring', 'Tuyệt phẩm hành động RPG của FromSoftware.', 599000.00, 799000.00, 25, 4.9, 18000, 'Action RPG', 'FromSoftware', 'https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/header.jpg', 1, 1, 0, 999, '2026-05-23 14:03:50', 'https://cdn.cloudflare.steamstatic.com/steam/apps/1245620/page_bg_generated_v6b.jpg'),
(6, 'God of War Ragnarök', 'Kratos và Atreus đối mặt với thần thoại Bắc Âu.', 550000.00, 699000.00, 21, 4.9, 14200, 'Action', 'Santa Monica Studio', 'https://cdn.cloudflare.steamstatic.com/steam/apps/1593500/header.jpg', 1, 0, 1, 999, '2026-05-23 14:03:50', NULL),
(7, 'Minecraft', 'Xây dựng và khám phá thế giới vô tận.', 199000.00, 199000.00, 0, 4.7, 45000, 'Simulation', 'Mojang', 'https://cdn.cloudflare.steamstatic.com/steam/apps/1672970/header.jpg', 0, 0, 0, 999, '2026-05-23 14:03:50', NULL);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `orders`
--

CREATE TABLE `orders` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `discount` decimal(10,2) DEFAULT 0.00,
  `total` decimal(10,2) NOT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `payment_method` varchar(30) DEFAULT 'WALLET',
  `note` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `order_items`
--

CREATE TABLE `order_items` (
  `id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `game_id` int(11) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity` int(11) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(500) NOT NULL,
  `display_name` varchar(200) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `wallet_balance` decimal(10,2) DEFAULT 0.00,
  `points` int(11) DEFAULT 0,
  `membership_level` varchar(20) DEFAULT 'BRONZE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `wishlists`
--

CREATE TABLE `wishlists` (
  `user_id` int(11) NOT NULL,
  `game_id` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

----------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `Libraries`
--

CREATE TABLE `libraries` (
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `user_id` INT(11) NOT NULL,
    `game_id` INT(11) NOT NULL,
    `purchase_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_favorite` TINYINT(1) DEFAULT 0,
    `playtime_minutes` INT(11) DEFAULT 0,
    `last_played_at` DATETIME DEFAULT NULL, PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_game` (`user_id`, `game_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_game` (`game_id`),
    CONSTRAINT `fk_library_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_library_game` FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci;
----------------------------------------------------------

--
-- Đang đổ dữ liệu cho bảng `libraries`
--
INSERT INTO `libraries`
(`user_id`, `game_id`, `is_favorite`, `playtime_minutes`)
  VALUES
    (1, 1, 1, 540),
    (1, 5, 1, 1280),
    (2, 2, 1, 760),
    (3, 6, 1, 980),
    (4, 5, 1, 1450);

----------------------------------------------------------

--
-- Đang đổ dữ liệu cho bảng `users` (Nếu chưa có liệu users thì insert vào rồi mới insert dữ liệu của bảng `libraries`)
--
INSERT INTO `users`
(`id`, `username`, `email`, `password_hash`, `display_name`)
  VALUES
    (1, 'player1', 'player1@gmail.com', '123456', 'Player One'),
    (2, 'wizard99', 'wizard99@gmail.com', '123456', 'Wizard'),
    (3, 'shadowfox', 'shadowfox@gmail.com', '123456', 'Shadow Fox'),
    (4, 'nightblade', 'nightblade@gmail.com', '123456', 'Night Blade');
--
-- Chỉ mục cho bảng `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `coupons`
--
ALTER TABLE `coupons`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`);

--
-- Chỉ mục cho bảng `games`
--
ALTER TABLE `games`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Chỉ mục cho bảng `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `order_id` (`order_id`),
  ADD KEY `game_id` (`game_id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Chỉ mục cho bảng `wishlists`
--
ALTER TABLE `wishlists`
  ADD PRIMARY KEY (`user_id`,`game_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT cho bảng `coupons`
--
ALTER TABLE `coupons`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `games`
--
ALTER TABLE `games`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT cho bảng `orders`
--
ALTER TABLE `orders`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `order_items`
--
ALTER TABLE `order_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Các ràng buộc cho bảng `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

ALTER TABLE users ADD COLUMN is_admin TINYINT(1) DEFAULT 0 AFTER membership_level;

ALTER TABLE games ADD COLUMN publisher VARCHAR(255) AFTER developer;
ALTER TABLE games ADD COLUMN release_date VARCHAR(50) AFTER publisher;
ALTER TABLE games ADD COLUMN platforms VARCHAR(255) AFTER release_date;
ALTER TABLE games ADD COLUMN download_size VARCHAR(50) AFTER platforms;
ALTER TABLE users ADD COLUMN is_active TINYINT(1) DEFAULT 1 AFTER is_admin;
