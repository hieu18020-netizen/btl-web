package com.gamevui.backend.security;

import java.lang.annotation.*;

/**
 * Danh dau 1 tham so controller la username lay tu token JWT, KHONG bat buoc.
 * Tuong duong: current_username: str | None = Depends(get_optional_username)
 * Neu khong co token hoac token khong hop le -> tra ve null (khong nem loi).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalUsername {
}
