package study.datajpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data // 엔티티는 @Data 안되지만 DTO는 된다.
public class MemberDto {
    private Long id;
    private String username;
    private String teamName;
}
