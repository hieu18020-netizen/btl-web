USE gamevui_db;
GO

-- ==================== KHO CHIBI & TRANG PHỤC ====================

-- 1. Danh sách các chibi gốc (5-6 con, mỗi con có bộ khung chạy riêng)
CREATE TABLE chibi_characters (
    id INT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,        -- slug, trùng tên thư mục ảnh, vd 'miu'
    name NVARCHAR(50) NOT NULL,              -- tên hiển thị
    frame_count INT NOT NULL DEFAULT 10,     -- số khung ảnh walk-cycle
    sprite_dir VARCHAR(100) NOT NULL,        -- vd 'chibi/miu'
    is_default BIT NOT NULL DEFAULT 0,       -- ai cũng có sẵn, không cần mở khoá
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

-- 2. Loại vật phẩm / vị trí trang bị (mũ, kính, áo, giày...)
CREATE TABLE item_slots (
    id INT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,        -- 'hat','glasses','top','bottom','shoes','accessory'
    name NVARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0
);

-- 3. Vật phẩm trong kho đồ
CREATE TABLE items (
    id INT IDENTITY(1,1) PRIMARY KEY,
    slot_id INT NOT NULL FOREIGN KEY REFERENCES item_slots(id),
    code VARCHAR(30) NOT NULL UNIQUE,
    name NVARCHAR(50) NOT NULL,
    image_path VARCHAR(150) NOT NULL,        -- ảnh overlay (đổi thành image_dir nếu sau này cần nhiều khung)
    rarity VARCHAR(10) NOT NULL DEFAULT 'thuong',
    price INT NOT NULL DEFAULT 0,
    is_default BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

-- 4. Chibi nào user đã mở khoá / sở hữu
CREATE TABLE user_chibis (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    chibi_id INT NOT NULL FOREIGN KEY REFERENCES chibi_characters(id),
    unlocked_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_user_chibi UNIQUE (user_id, chibi_id)
);

-- 5. Vật phẩm nào user đã sở hữu (kho đồ cá nhân)
CREATE TABLE user_items (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    item_id INT NOT NULL FOREIGN KEY REFERENCES items(id),
    acquired_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_user_item UNIQUE (user_id, item_id)
);

-- 6. Đồ đang mặc của user — dùng chung cho MỌI chibi, mỗi slot chỉ 1 item
CREATE TABLE user_outfit (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    slot_id INT NOT NULL FOREIGN KEY REFERENCES item_slots(id),
    item_id INT NOT NULL FOREIGN KEY REFERENCES items(id),
    equipped_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_outfit_slot UNIQUE (user_id, slot_id)
);

-- 7. Con chibi nào đang được chọn để chạy trên màn hình
ALTER TABLE users ADD active_chibi_id INT NULL FOREIGN KEY REFERENCES chibi_characters(id);

-- ==================== DỮ LIỆU MẪU ====================
INSERT INTO item_slots (code, name, sort_order) VALUES
    ('hat', N'Mũ', 1),
    ('glasses', N'Kính', 2),
    ('top', N'Áo', 3),
    ('bottom', N'Quần/Váy', 4),
    ('shoes', N'Giày', 5),
    ('accessory', N'Phụ kiện', 6);

INSERT INTO chibi_characters (code, name, frame_count, sprite_dir, is_default) VALUES
    ('miu', N'Miu', 10, 'chibi/miu', 1);