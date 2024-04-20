package study.datajpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    @GetMapping("/members2/{id}") // 도메인 클래스 컨버터. -> Only 조회용
    public String findMember2(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    @GetMapping("/members")
    public Page<Member> memberList( Pageable pageable) { // default값 application.yml에
        Page<Member> page = memberRepository.findAll(pageable);
        return page;
    }
    @GetMapping("/members2") // global설정보다 이 설정이 더 우선
    public Page<Member> memberList2(@PageableDefault(size=5, sort="username") Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        return page;
    }

    /**
     * 위와 같이 Member 엔티티를 웹 계층 외부로 드러내서는 안된다.
     * DTO로 싸서 반환해야한다.
     */
    @GetMapping("/members/dto")
    public Page<MemberDto> memberDtoList1(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        // 이렇게하거나 MemberDto의 생성자에서 인자로 Member엔티티를 그대로 받고 this.id = member.getId()이런식으로 하면 좀 더 편리하다.
        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
        return map;
    }

    @GetMapping("/members/dto2")
    public Page<MemberDto> memberDtoList2(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> map = page.map(member -> new MemberDto(member));
        return map;
    }


    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user"+i, i));
        }
    }
}
