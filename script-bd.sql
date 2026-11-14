CREATE DATABASE IF NOT EXISTS instituto
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'instituto'@'localhost' IDENTIFIED BY 'instituto_tp';

GRANT ALL PRIVILEGES ON instituto.* TO 'instituto'@'localhost';
FLUSH PRIVILEGES;