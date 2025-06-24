package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

/*
사용자 정의 리포지토리 사용법
1. 사용자 정의 인터페이스 작성
2. 사용자 정의 인터페이스 구현 --> 이 단계!
3. 스프링 데이터 리포지토리에 사용자 정의 인터페이스 상속
 */
public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;
//
//    public MemberRepositoryImpl(EntityManager em) {
//        this.queryFactory = new JPAQueryFactory(em);
//    }

    public MemberRepositoryImpl(EntityManager em) {
        super(Member.class);
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    //회원명, 팀명, 나이(ageGoe, ageLoe)
    public List<MemberTeamDto> search(MemberSearchCondition condition) {

        List<MemberTeamDto> result = from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .fetch();

        return result;

        /*
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
           */
    }

    /**
     * 전체 카운트를 한번에 조회하는 단순한 방법
     * 단순한 페이징, fetchResults() 사용
     */
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        //fetchresults를 사용하면 querydsl이 카운트, 컨텐츠용 쿼리 두번을 날리게 됨
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())  //몇 번째를 스킵하고, 몇 번째부터 시작할 건지?
                .limit(pageable.getPageSize()) //한 번 조회할 때 몇개까지?
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 데이터 내용과 전체 카운트를 별도로 조회하는 방법
     * 복잡한 페이징
     * 데이터 조회 쿼리와, 전체 카운트 쿼리를 분리
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {

        //content만 가져옴
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // total용 count 쿼리
        // 최적화할때는 count 쿼리 분리하는 방법이 더 나음
        /* 
        //최적화 전 쿼리
        long total = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetchCount();
        */

        // 최적화 쿼리
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(
                        condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        //() -> countQuery.fetchCount() 와 countQuery::fetchCount 는 같은 표현
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
        // return new PageImpl<>(content, pageable, total);
    }


    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }


    // 리포지토리 지원 - QuerydslRepositorySupport 버전으로
    public Page<MemberTeamDto> searchPageSimple2(MemberSearchCondition condition, Pageable pageable) {
        JPQLQuery<MemberTeamDto> jpaQuery = from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name));
        JPQLQuery<MemberTeamDto> query = getQuerydsl().applyPagination(pageable, jpaQuery); //offset, sorting 적용하는 applyPagination

        List<MemberTeamDto> content = query.fetch();

        // 강의에서 안다룬 코드
        JPQLQuery<Long> countQuery = from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .select(member.count());

        //List<MemberTeamDto> content = results.getResults();
        //long total = results.getTotal();

        return new PageImpl<>(content, pageable, countQuery.fetchOne());
    }
}
