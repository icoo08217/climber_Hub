package com.climbers.hub.auth.controller;

import com.climbers.hub.member.controller.MemberProfileController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {AuthController.class, MemberProfileController.class})
public class AuthExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> authenticationFailed() {
        return ResponseEntity.status(401).body(Map.of("message", "ID 또는 비밀번호를 확인해주세요."));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> invalidInput(MethodArgumentNotValidException exception) {
        String objectName = exception.getBindingResult().getObjectName();
        String message = switch (objectName) {
            case "profileUpdateRequest" -> "이름은 1~50자로 입력하고 이메일 형식을 확인해주세요.";
            case "passwordChangeRequest" -> "현재 비밀번호와 8~72자의 새 비밀번호를 입력해주세요.";
            default -> "입력값을 확인해주세요. ID는 소문자·숫자·밑줄 4~50자, 비밀번호는 8~72자입니다.";
        };
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> duplicate() {
        return ResponseEntity.status(409).body(Map.of("message", "이미 사용 중인 ID 또는 이메일입니다."));
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> status(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(Map.of("message", exception.getReason()));
    }
}
