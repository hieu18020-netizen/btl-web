package com.gamevui.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Doc header "Authorization: Bearer <token>" va giai ma JWT, roi bom thang
 * username vao tham so controller co @CurrentUsername hoac @OptionalUsername.
 *
 * Day chinh la ban Java cua 2 ham get_current_username() / get_optional_username()
 * trong backend.py - gop chung logic doc + giai ma token vao 1 noi duy nhat.
 */
public class UsernameArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    public UsernameArgumentResolver(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUsername.class)
                || parameter.hasParameterAnnotation(OptionalUsername.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        boolean required = parameter.hasParameterAnnotation(CurrentUsername.class);

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            if (required) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Thieu token dang nhap");
            }
            return null;
        }

        String token = authorization.substring("Bearer ".length());
        JwtUtil.DecodeResult result = jwtUtil.decode(token);

        if (result instanceof JwtUtil.Ok ok) {
            return ok.username();
        }
        if (!required) {
            return null;
        }
        if (result instanceof JwtUtil.Expired) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Phien dang nhap da het han, vui long dang nhap lai");
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Token khong hop le");
    }
}
