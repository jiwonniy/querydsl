package study.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"}) //본인이 소유한 필드정도만, 연관관계 필드는 x
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    //여기가 연관관계의 주인임
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;


    public Member(String username){
        this(username,0);
    }

    public Member(String username, int age){
        this(username, age, null);
    }

    public Member(String username, int age, Team team){
        this.username = username;
        this.age = age;
        if (team != null){
            changeTeam(team);
        }

    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

}
