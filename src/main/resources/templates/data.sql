-- ================================================================
-- SEED DATA: Tài khoản mặc định (chạy 1 lần khi khởi tạo hệ thống)
-- Đặt file này tại: src/main/resources/data.sql
-- Spring Boot sẽ tự chạy khi spring.sql.init.mode=always
-- ================================================================

-- Genres (hardcode theo SRS)
INSERT IGNORE INTO genres (genre_name) VALUES
    ('Hành động'),
    ('Tình cảm'),
    ('Hài hước'),
    ('Kinh dị'),
    ('Hoạt hình'),
    ('Khoa học viễn tưởng'),
    ('Tâm lý'),
    ('Phiêu lưu');

-- Rooms (hardcode theo SRS)
INSERT IGNORE INTO rooms (room_name, total_seat, room_type) VALUES
    ('Phòng 1', 80, '2D'),
    ('Phòng 2', 80, '3D'),
    ('Phòng 3', 60, 'IMAX');

-- Tài khoản Admin mặc định
-- Username: admin | Password: admin123
INSERT IGNORE INTO users (username, email, password, role, status)
VALUES (
    'admin',
    'admin@smartcinema.vn',
    'admin123',
    'ADMIN',
    true
);
INSERT IGNORE INTO user_profiles (user_id, fullname)
SELECT user_id, 'Quản trị viên' FROM users WHERE username = 'admin';

-- Tài khoản Staff mặc định
-- Username: staff | Password: staff123
INSERT IGNORE INTO users (username, email, password, role, status)
VALUES (
    'staff',
    'staff@smartcinema.vn',
    'staff123',
    'STAFF',
    true
);
INSERT IGNORE INTO user_profiles (user_id, fullname)
SELECT user_id, 'Nhân viên rạp' FROM users WHERE username = 'staff';

INSERT IGNORE INTO movie_genres (movie_id, genre_id)
SELECT m.movie_id, g.genre_id
FROM movies m, genres g
WHERE m.title = 'Avengers: Endgame' AND g.genre_name = 'Hành động';

INSERT IGNORE INTO movie_genres (movie_id, genre_id)
SELECT m.movie_id, g.genre_id
FROM movies m, genres g
WHERE m.title = 'Inside Out 2' AND g.genre_name = 'Hoạt hình';

INSERT IGNORE INTO user_profiles (user_id, fullname)
SELECT user_id, 'Nhân viên rạp' FROM users WHERE username = 'staff';

INSERT IGNORE INTO movies (title, description, director, actors, duration, release_date, poster, trailer_url, age_rating, created_at, updated_at, status)
VALUES
    ('Avengers: Endgame', 'Trận chiến cuối cùng chống lại Thanos', 'Anthony Russo', 'Robert Downey Jr., Chris Evans', 180, '2019-04-26', 'https://tse1.mm.bing.net/th/id/OIP.QPzMe0bb0D6TEAp_OA2bFwHaEK?pid=Api&P=0&h=180', 'https://youtube.com/trailer', 'PG-13', NOW(), NOW(), 'NOW_SHOWING'),
    ('Inside Out 2', 'Cuộc phiêu lưu mới trong tâm trí', 'Kelsey Mann', 'Amy Poehler, Maya Hawke', 120, '2024-06-14', 'https://tse1.mm.bing.net/th/id/OIP.QPzMe0bb0D6TEAp_OA2bFwHaEK?pid=Api&P=0&h=180', 'https://youtube.com/trailer', 'PG', NOW(), NOW(), 'COMING_SOON');

-- Sinh ghế tự động cho Phòng 1 (8 hàng A-H, 10 ghế/hàng)
INSERT IGNORE INTO seats (room_id, seat_row, seat_number, seat_type, price_extra)
SELECT r.room_id, rows.r, nums.n,
       CASE WHEN rows.r IN ('D','E') THEN 'VIP' ELSE 'NORMAL' END,
       CASE WHEN rows.r IN ('D','E') THEN 20000 ELSE 0 END
FROM rooms r
         JOIN (SELECT 'A' r UNION SELECT 'B' UNION SELECT 'C' UNION SELECT 'D'
               UNION SELECT 'E' UNION SELECT 'F' UNION SELECT 'G' UNION SELECT 'H') rows
JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) nums
WHERE r.room_name = 'Phòng 1';

-- PHÒNG 2
INSERT IGNORE INTO seats (room_id, seat_row, seat_number, seat_type, price_extra)
SELECT r.room_id, rows.r, nums.n,
       CASE WHEN rows.r IN ('D','E') THEN 'VIP' ELSE 'NORMAL' END,
       CASE WHEN rows.r IN ('D','E') THEN 20000 ELSE 0 END
FROM rooms r
         JOIN (SELECT 'A' r UNION SELECT 'B' UNION SELECT 'C' UNION SELECT 'D'
               UNION SELECT 'E' UNION SELECT 'F' UNION SELECT 'G' UNION SELECT 'H') rows
JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) nums
WHERE r.room_name = 'Phòng 2';

-- PHÒNG 3
INSERT IGNORE INTO seats (room_id, seat_row, seat_number, seat_type, price_extra)
SELECT r.room_id, rows.r, nums.n,
       CASE WHEN rows.r IN ('D','E') THEN 'VIP' ELSE 'NORMAL' END,
       CASE WHEN rows.r IN ('D','E') THEN 20000 ELSE 0 END
FROM rooms r
         JOIN (SELECT 'A' r UNION SELECT 'B' UNION SELECT 'C' UNION SELECT 'D'
               UNION SELECT 'E' UNION SELECT 'F' UNION SELECT 'G' UNION SELECT 'H') rows
JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
      UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) nums
WHERE r.room_name = 'Phòng 3';

