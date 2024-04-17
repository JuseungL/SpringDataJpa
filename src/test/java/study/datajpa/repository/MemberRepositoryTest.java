package study.datajpa.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false )
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext EntityManager em;

    @Test
    public void testMember() {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        // 이때 있을 수도 있고 없을 수도 있어서 Optional Return Type이다.
        Optional<Member> byId = memberRepository.findById(savedMember.getId());
        Member findMember = byId.get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

        assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);
        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);
        List<Member> byUsername = memberRepository.findByUsername("member2");
        assertThat(byUsername.size()).isEqualTo(1);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);
        //카운트 검증
        long count = memberRepository.count(); assertThat(count).isEqualTo(2);
        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findUsername() {
        Member member1 = new Member("AAA");
        Member member2 = new Member("BBB");
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() {
        Team teamA = new Team("A");
        Team teamB = new Team("B");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("AAA",25);
        Member member2 = new Member("BBB", 27);
        member1.changeTeam(teamA);
        member2.changeTeam(teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);


        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = "  + dto);
        }

    }

    @Test
    public void findByNames() {
        Member member1 = new Member("AAA");
        Member member2 = new Member("BBB");
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> byNames = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member byName : byNames) {
            System.out.println(byName);
        }
    }

    @Test
    public void returnType() {

        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        // Collection으로 받는 경우 매치되는 데이터가 없어도 null이 아니라 빈 컬렉션이 반환됨
        List<Member> listByUsername = memberRepository.findListByUsername("AAA");

        // 단건 조회의 경우 조회 시 매칭되는 값이 없을 경우 null이다.
        // 원래는 NoResultException 터져야하는데 Spring Data JPA가 try catch로 묶어 그냥 null로 반환해버린다.
        Member memberByUsername = memberRepository.findMemberByUsername("AAA");

        // 그러나 매칭되는 데이터가 있는지 없는지 모를 경우 Optional로 받아라! (Java8 이후)
        // .orElse()등으로 처리
        // 단건 처리에서 2개 이상이 쿼리되면 NoneUniqueResultException(IncorrectResultSizeDataAccessException) 터짐.
        Optional<Member> optionalByUsername = memberRepository.findOptionalByUsername("AAA");
    }

    //페이징 조건과 정렬 조건 설정
    @Test
    public void page() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        //when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(10, pageRequest); // 이때 반환 타입이 Slice라면 totalCount몰라
        Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), m.getTeam().getName())); // 이렇게 DTO로 변환 후 API로 반환
        //then
        List<Member> content = page.getContent(); //조회된 데이터
        long totalCount = page.getTotalElements();
        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalCount = " + totalCount);

        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); // 전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
         assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
    }

    @Test
    public void bulkUpdate() throws Exception {
            //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));
        //when
        int resultCount = memberRepository.bulkAgePlus(20);

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5 = " + member5);
        /**
         * 위의 출력값 결과 중 member5의 age가 41이 아닌 40인 이유.
         * 벌크 연산 관련 메소드를 실행하면 데이터베이스 레코드가 직접 업데이트해버려서 41이 되지만
         * 영속성 컨텍스트에는 반영되지 않아 40이다.
         * 그런 상황에 .findByUsername을 통해 조회할때 영속성 컨텍스트의 age = 40이 끌려와
         * 41이 아닌 것 처럼 보인다.
         *
         * 그렇기 때문에 꼭 벌크 관련 연산이 있으면 em.flush()와 em.clear()를 호출하여 영속성 컨텍스트를 수동으로 동기화시켜주거나
         * @
         */

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() throws Exception {
        //given
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamB));
        em.flush();
        em.clear();
        //when
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");
        //then
        for (Member member : members) {
            // 지연로딩
            member.getTeam().getName();
        }
    }

    @Test
    public void queryHint() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when

        // @QueryHints 어노테이션을 사용하여 org.hibernate.readOnly를 true로 설정했기 때문에,
        // 이 엔티티는 읽기 전용으로 처리되어, em.flush()를 호출해도 업데이트 쿼리가 실행되지 않는다.
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.changeUsernames("member2");
        em.flush(); //Update Query 실행X
    }

    @Test
    public void lock() {
        //given
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        //when
        List<Member> member = memberRepository.findLockByUsername("member1");
//        member.changeUsernames("member2");
        em.flush(); //Update Query 실행X
    }
}