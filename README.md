# 🧱 ARKANOID - CLASSIC BRICK BREAKER GAME

> 🎮 *Một phiên bản hiện đại của game Arkanoid huyền thoại — điều khiển thanh chắn, phản bóng và phá vỡ toàn bộ gạch!  
> Game được viết bằng **JavaFX**, mang phong cách retro, gameplay mượt và đầy thử thách.*

---

## ✨ GIỚI THIỆU NGẮN GỌN

Arkanoid là tựa game arcade cổ điển. Người chơi điều khiển một thanh trượt (paddle) ở cuối màn hình để đỡ bóng, tránh để nó rơi, và dùng bóng đó phá vỡ toàn bộ các viên gạch ở trên.  
Mỗi viên gạch bị phá sẽ cho điểm, và một số gạch có thể rơi ra vật phẩm (power-up) giúp người chơi có thêm lợi thế.

---

## ⚙️ TÍNH NĂNG CHÍNH  
- 🎨 **Đồ họa retro pixel / flat style**, nhẹ và mượt trên mọi máy.  
- 🔊 **Âm thanh sống động:** hiệu ứng khi bóng chạm gạch, mất mạng, qua màn...  
- 💥 **Power-ups**:
  - 🧡 *Extra Life* – thêm mạng.
  - ☢️ *Multi Ball* - thêm 2 quả bóng.
  - 🏋️ *Expand Paddle* - mở rộng thanh chắn.
  - 🌀 *Slow Ball* – giảm tốc độ bóng.
  - ⚡️ *Fast Ball* - tăng tốc độ bóng.
- 👔 **Kho trang phục đa dạng :** đừng lo nhàm chán khi chơi nha ^^!
- 🌈 **Hệ thống điểm số (Score System):**
  - High Score được lưu lại khi chơi lại.
- 🧱 **Nhiều cấp độ (Levels):**
  - Có hơn 10 loại map đa dạng
  - Mỗi lần qua màn, bóng sẽ tăng tốc độ, số màn chơi là không giới hạn!
- 🧠 **Menu & Gameplay UI:**
  - Main menu, select skin, highsrcores and exit.
  - Scoreboard hiển thị real-time.
- 💾 **Lưu dữ liệu:**
  - Ghi nhớ điểm cao nhất (local save hoặc JSON file).

---

## 🕹️ CÁCH CHƠI CHI TIẾT

| Phím | Hành động |
|------|------------|
| `A` hoặc `←` | Di chuyển thanh chắn sang trái | 
| `D` hoặc `→`| Di chuyển thanh chắn sang phải |
| `P` | Tạm dừng / Tiếp tục |


### 🎯 **Mục tiêu**
> Phá vỡ tất cả gạch trên màn chơi để qua level tiếp theo.  
> Đừng để bóng rơi xuống — mất hết mạng là game over 😭

### 💡 **Mẹo chơi**
> - Cố gắng điều chỉnh góc nảy của bóng bằng vị trí va chạm trên thanh chắn.  
> - Giữ bóng ở tầm cao để phá nhiều gạch nhanh hơn.  
> - Đừng ham power-up nếu nó ở vị trí nguy hiểm 😏  

---

## 🧩 CẤU TRÚC THƯ MỤC

```bash
Arkanoid/
├── src/
│   ├── entities/        # Ball, Paddle, Brick, PowerUp
│   ├── controller/      # Game logic, event handling
│   ├── ui/              # Menu, HUD, Scoreboard
│   ├── assets/          # Sprite, sound, font
│   └── main/            # Entry point (Main.cpp / Main.java)
├── resources/
│   ├── images/
│   ├── sounds/
│   └── levels/
└── README.md
```
## 💻 LÀM SAO ĐỂ CHẠY CODE 

# Clone repo
``` bash
git clone https://github.com/yourname/Arkanoid.git
cd Arkanoid
```
# Chạy game 
``` bash
javac -d bin --module-path "path/to/javafx/lib" --add-modules javafx.controls src/**/*.java
java -cp bin --module-path "path/to/javafx/lib" --add-modules javafx.controls main.Main
```



