package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

/*
만약 특정 페이지, 화면에 특화된 복잡한 쿼리라면, MemberRepository가 상속받게 안하고
그냥 MemberQueryRepository 같이 조회용 레포지토리를 따로 분리해서 만드는 것도 괜찮다
기본은 CUSTOM으로 설계하고 분리하는 것도 괜찮음
사용자 정의 리포지토리 사용법
1. 사용자 정의 인터페이스 작성 --> 이 단계!
2. 사용자 정의 인터페이스 구현
3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
 */
public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

    // 단순한 쿼리
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

    // 복잡한 쿼리
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
