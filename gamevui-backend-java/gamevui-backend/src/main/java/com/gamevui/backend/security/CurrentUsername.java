package com.gamevui.backend.security;

import java.lang.annotation.*;

/**
 * Danh dau 1 tham so controller la username lay tu token JWT bat buoc.
 * Tuong duong: current_username: str = Depends(get_current_username)
 * Neu thieu token hoac token khong hop le -> 401, xu ly boi UsernameArgumentResolver.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUsername {
}
