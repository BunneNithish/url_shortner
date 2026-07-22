CREATE TABLE IF NOT EXISTS `url` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `full_url` varchar(2048) NOT NULL,
  `short_url` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expiry_date` timestamp NULL DEFAULT NULL,
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `short_url_unique` (`short_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `visit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `url_id` bigint NOT NULL,
  `click_timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `browser` varchar(100) DEFAULT NULL,
  `operating_system` varchar(100) DEFAULT NULL,
  `device` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `referrer` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_visit_url` FOREIGN KEY (`url_id`) REFERENCES `url` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
