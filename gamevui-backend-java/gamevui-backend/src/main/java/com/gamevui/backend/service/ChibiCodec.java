package com.gamevui.backend.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Tuong duong parse_active_chibis() / serialize_active_chibis() trong backend.py.
 * Cot users.active_chibi_code luu nhieu ma chibi cung luc, ngan cach boi dau phay (vd "1,2").
 */
public final class ChibiCodec {

    private ChibiCodec() {
    }

    public static List<String> parse(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isEmpty()) {
            return result;
        }
        for (String c : raw.split(",")) {
            if (!c.isEmpty()) {
                result.add(c);
            }
        }
        return result;
    }

    public static String serialize(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return null;
        }
        return String.join(",", codes);
    }
}
