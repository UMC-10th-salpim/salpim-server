package salpim.umc10thsalpim.domain.term.enums;

/**
 * 약관 버전 상태
 */
public enum TermsVersionStatus {
    DRAFT,      // 임시저장 (관리자 작성 중)
    PUBLISHED,  // 게시 (현재 유효한 버전)
    ARCHIVED    // 보관 (과거 버전, 조회/증빙용으로만 사용)
}
