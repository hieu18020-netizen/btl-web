package com.gamevui.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Diem khoi dong ung dung, tuong duong:
 *   if __name__ == "__main__":
 *       uvicorn.run(app, host="0.0.0.0", port=8000)
 * trong backend.py.
 *
 * @EnableScheduling bat cac tac vu don rac dinh ky (phien game het han,
 * phong cho qua lau, bao cao co vua qua han...) - xem cac lop *CleanupTask.
 */
@SpringBootApplication
@EnableScheduling
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
