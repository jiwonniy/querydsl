package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

//QueryProjection을 사용하면 QueryDsl 에 의존하게 되는 단점 존재
@Data
public class MemberTeamDto {

    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
