package study.datajpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter // 아래와 같이 하면 Setter 없앨 수 있음.
@ToString(of = {"id", "username", "age"}) // Team은 찍으면 안돼. 연관관계로 인해 무한루프 돌 수도 있다.
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this.username = username;
    }

    public Member(String username, int age) {
        this(username, age, null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    /*
        엔티티에서 Default 생성자(파라미터 없는 생성자)가 필요하다.
        JPA는 엔티티 객체를 생성할 때 기본 생성자를 사용하느데 이떄
        JPA에서 프록시 기술을 쓸 수도 있다.
        이때 private으로 막아두면 그게 막혀서protected까지 열어둬야함
        => @NoArgsConstructor(access = AccessLevel.PROTECTED)이걸로 대체 가능
     */
    protected Member() {}

    // Setter없을때 혹시 변경할 일이 있을때 해당 메소드
    public void changeUsernames(String username){
        this.username = username;
    }
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
