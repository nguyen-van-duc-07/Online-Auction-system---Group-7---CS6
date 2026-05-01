package com.example.auctionserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController // Đánh dấu đây là Controller xử lý API
@RequestMapping("/api/auth") // Tất cả các link bắt đầu bằng /api/auth
public class AuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Spring tự động kết nối DB qua đây

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> body) {
        try {
            String sql = "SELECT COUNT(*) FROM new_table WHERE accountname = ? AND password = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class,
                    body.get("accountname"),
                    body.get("password"));

            return (count != null && count > 0) ? "EXACT" : "WRONG";
        } catch (Exception e) {
            return "FAIL";
        }
    }

    @PostMapping("/signup")
    public String signup(@RequestBody Map<String, String> body) {
        try {
            // 1. Kiểm tra xem user đã tồn tại chưa
            String checkSql = "SELECT COUNT(*) FROM new_table WHERE accountname = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, body.get("accountname"));

            if (count != null && count > 0) return "EXISTED";

            // 2. Nếu chưa thì Thêm mới
            String insertSql = "INSERT INTO new_table (accountname, realname, password, dob) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertSql,
                    body.get("accountname"),
                    body.get("realname"),
                    body.get("password"),
                    body.get("dob"));

            return "OK";
        } catch (Exception e) {
            return "FAIL";
        }
    }
}