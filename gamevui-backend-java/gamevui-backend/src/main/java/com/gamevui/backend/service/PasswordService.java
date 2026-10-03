package com.gamevui.backend.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * Tuong duong hash_password() / verify_password() trong backend.py.
 * Dung truc tiep BCrypt (org.springframework.security.crypto.bcrypt) - hash bcrypt
 * sinh ra tuong thich hoan toan voi hash cua thu vien "bcrypt" ben Python (deu la $2...),
 * nen du lieu password cu trong CSDL van dang nhap duoc binh thuong.
 */
@Service
public class PasswordService {

    public String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public boolean verify(String plainPassword, String storedValue) {
        if (storedValue == null || storedValue.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedValue);
        } catch (IllegalArgumentException e) {
            // storedValue khong phai hash hop le (du lieu cu / loi dinh dang)
            return false;
        }
    }

    /** Chuoi bcrypt luon bat dau bang "$2" (giong kiem tra is_hashed trong Python) */
    public boolean isHashed(String storedValue) {
        return storedValue != null && storedValue.startsWith("$2");
    }
}
