package study.datajpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import study.datajpa.entity.Member;

// 엔티티 자체로 꺼내면 안돼서 DTO로 변환해서 반환
@AllArgsConstructor
@Data // 엔티티는 @Data 안되지만 DTO는 된다.
public class MemberDto {
    private Long id;
    private String username;
    private String teamName;
    public MemberDto (Member member) {
        this.id = member.getId();
        this.username = member.getUsername();
    }
}
