package com.wonnabe.controller;

import com.wonnabe.common.security.account.domain.CustomUser;
import com.wonnabe.common.security.account.domain.MemberVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Spring Security 설정이 되어 있는 프로젝트에서
// 인증/인가가 어떻게 적용되는지 테스트하기 위한 컨트롤러
// => 인증된 사용자나 관리자만 접근 가능한 API 엔드포인트를 실험하고 확인하기 위한 컨트롤러
@Log4j2
@RequestMapping("/api/security")
@RestController
public class SecurityController {

    // 누구나 접근 가능한 API - 인증/로그인 안 해도 호출 가능
    // 보안필터 없이 열려 있음 (권한 제한 없음)
    @GetMapping("/all")
    public ResponseEntity<String> doAll() {
        log.info("do all can access everybody");
        return ResponseEntity.ok("All can access everybody");
    }

    // 인증된 사용자 정보 확인 - 로그인한 사용자만 접근 가능 (권한: ROLE_USER, ROLE_ADMIN 등)
    // Authentication 객체를 통해 현재 로그인된 유저 정보를 Spring Security의 인증 컨텍스트에서 꺼내서 username을 리턴
    @GetMapping("/member")
    public ResponseEntity<String> doMember(Authentication authentication) {
        UserDetails userDetails = (UserDetails)authentication.getPrincipal();
        log.info("username = " + userDetails.getUsername());
        return ResponseEntity.ok(userDetails.getUsername());
    }

    // 관리자 권한 사용자 정보 확인 - 관리자 권한이 있는 사용자만 접근 가능 (ROLE_ADMIN)
    // @AuthenticationPrincipal 어노테이션을 통해 현재 로그인된 유저 정보를 바로 주입
    // => 이 API는 Security 설정에서 /admin 요청에 hasRole("ADMIN") 같은 조건이 있어야 작동함!
    @GetMapping("/admin")
    public ResponseEntity<MemberVO> doAdmin(@AuthenticationPrincipal CustomUser customUser) {
        MemberVO member = customUser.getMember();
        log.info("username = " + member);
        return ResponseEntity.ok(member);
    }

    // Spring Security 기본 처리 1 (로그인)
    @GetMapping("/login")
    public void login() {
        log.info("login page");
    }

    // Spring Security 기본 처리 2 (로그아웃)
    @GetMapping("/logout")
    public void logout() {
        log.info("logout page");
    }
}