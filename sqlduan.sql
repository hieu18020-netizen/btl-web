USE gamevui_db;
GO

CREATE TABLE friends (
    id INT IDENTITY(1,1) PRIMARY KEY,
    requester_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    addressee_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    status VARCHAR(10) NOT NULL DEFAULT 'pending',
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_friend_pair UNIQUE (requester_id, addressee_id)
);
CREATE TABLE messages (
    id INT IDENTITY(1,1) PRIMARY KEY,
    sender_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    receiver_id INT NOT NULL FOREIGN KEY REFERENCES users(id),
    content NVARCHAR(2000) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    is_read BIT NOT NULL DEFAULT 0
);
CREATE INDEX IX_messages_conversation ON messages(sender_id, receiver_id, created_at);