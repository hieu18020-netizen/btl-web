# GameVui Backend — bản chuyển đổi Java (Spring Boot)

Đây là bản chuyển đổi **toàn bộ chức năng** của `backend.py` (FastAPI/Python) sang
Java, dùng **Spring Boot**, giữ nguyên hợp đồng API (cùng URL, cùng field JSON dạng
`snake_case`) để **frontend cũ không cần sửa gì**.

## Cấu trúc thư mục (theo package, đúng tinh thần OOP)

```
com.gamevui.backend
├── entity/        Ánh xạ các bảng CSDL (User, Friend, Message)
├── repository/     Spring Data JPA — thay thế các câu lệnh pyodbc/cursor.execute
├── dto/            Request/Response tương đương các class BaseModel của Pydantic
├── security/       JWT (JwtUtil), HTTPException (ApiException), @CurrentUsername/@OptionalUsername
├── service/        Toàn bộ logic nghiệp vụ (tương đương thân hàm trong backend.py)
├── controller/      Endpoint REST (@RestController), map 1-1 với @app.get/post trong FastAPI
├── websocket/      2 kênh realtime: nhắn tin (/api/ws/messages) và phòng đấu (/api/ws/duel/{roomCode})
└── config/         CORS, đăng ký WebSocket, xử lý lỗi tập trung, resolver cho username
```

## Bảng đối chiếu nhanh (Python → Java)

| Python (FastAPI)                          | Java (Spring Boot)                                  |
|--------------------------------------------|------------------------------------------------------|
| `pyodbc` + SQL thô                         | Spring Data JPA + JDBC driver `mssql-jdbc`           |
| `bcrypt.hashpw/checkpw`                    | `PasswordService` (org.springframework.security.crypto.bcrypt.BCrypt — **tương thích 100%** với hash cũ trong CSDL, không cần migrate mật khẩu) |
| `pyjwt` (`jwt.encode/decode`)               | `JwtUtil` (thư viện `io.jsonwebtoken` / JJWT)        |
| `Depends(get_current_username)`             | annotation `@CurrentUsername` + `UsernameArgumentResolver` |
| `Depends(get_optional_username)`            | annotation `@OptionalUsername`                        |
| `raise HTTPException(status_code, detail)`  | `throw new ApiException(HttpStatus, "...")`          |
| `pydantic.BaseModel`                        | `record` trong package `dto`                           |
| dict `ACTIVE_GAME_SESSIONS` (RAM)            | `GameSessionService` (ConcurrentHashMap + `@Scheduled` dọn rác) |
| dict `DUEL_ROOMS` / `LOBBY_ROOMS` (RAM)      | `DuelRoomRegistry` (`websocket` package)               |
| dict `CHESS_ROOM_REPORTS` (RAM)              | `ChessRoomReportRegistry`                              |
| dict `ONLINE_MESSAGE_SOCKETS` (RAM)           | `OnlineMessageSocketRegistry`                          |
| `@app.websocket(...)`                        | `WebSocketHandler` + đăng ký trong `WebSocketConfig`   |

## Yêu cầu môi trường

- JDK 17+
- Maven 3.9+
- SQL Server với CSDL `gamevui_db` đã có sẵn (cùng cấu trúc bảng `users`,
  `friends`, `messages` như ghi chú trong `backend.py` gốc)

## Cấu hình trước khi chạy

Mở `src/main/resources/application.properties`:

1. **Kết nối CSDL**: mặc định dùng `integratedSecurity=true` (Windows Auth) giống
   bản Python. Trên Windows cần có file `mssql-jdbc_auth-<version>.x64.dll`
   (đi kèm driver `mssql-jdbc`, tải riêng) trong `PATH`. Nếu không dùng được,
   bỏ comment 2 dòng `username`/`password` và đổi sang SQL Authentication.
2. **`app.jwt.secret`**: đổi thành chuỗi bí mật thật dài và ngẫu nhiên trước khi
   deploy — đúng lưu ý đã có sẵn trong file gốc.

## Chạy thử

```bash
mvn spring-boot:run
```

Server chạy ở `http://localhost:8000`, đúng cổng như bản Python
(`uvicorn.run(app, host="0.0.0.0", port=8000)`).

## Những điểm cần lưu ý khi bạn tiếp tục phát triển (đồ án OOP)

- **Không đổi field JSON**: nhờ `spring.jackson.property-naming-strategy=SNAKE_CASE`,
  các field Java dạng `camelCase` (`sessionToken`, `roomCode`, `creatorName`...)
  tự động in ra JSON dạng `snake_case` (`session_token`, `room_code`,
  `creator_name`...) — khớp với những gì frontend đang gọi.
- **Trạng thái trong RAM** (phiên chơi game, phòng đấu, báo cáo cờ vua, socket
  đang mở) chỉ tồn tại trên **1 instance server** — giống hệt giới hạn của bản
  Python gốc (dùng dict thường). Nếu sau này chạy nhiều instance thì cần
  chuyển sang Redis hoặc một message broker.
- **Đã tách rõ Controller / Service / Repository / Entity** để bạn dễ trình bày
  tính đóng gói (encapsulation), phân lớp (layering) và các nguyên lý OOP khác
  trong báo cáo đồ án.
