package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.awt.print.Pageable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * Spring Data JPA 쓰지 않고 순수 JPA
 *
 * JpaRepository<T, ID>
 * `T` : 엔티티
 * `ID` : 엔티티의 식별자 타입
 */
public interface MemberRepository extends JpaRepository<Member,Long> {

    /**
     * Spring Data JPA에서 공통으로 지원하지 않는 메소드를 새로 구혀하고싶을때
     * 해당 인터페이스를 구현하는 구현체 클래스를 만들때 자동으로 생성되던 구현체가 실제로 구현되어야하는 문제점이 발생한다.
     *
     * 스프링 데이터 JPA는 메소드 이름을 분석해서 JPQL을 생성하고 실행
     */
    List<Member> findByUsername(String username);

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // 실무에서 괘 쓴다. 애플리케이션 실행 시점에 문법 오류를 발견할 수 있음. + 이름 지정 가능
    // 스프링 데이터 JPA가 제공하는 쿼리 메소드 기능 쓰다가 이름이 너무 길어질때 이 방법 사용
    // 이때 동적 쿼리의 경우에는 Query DSL 사용
    // 가급적 파라미터 바인딩은 위치기반
    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    // DTO로 직접 조회
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    // 컬렉션 파라미터 바인딩
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    /**
     * Return Type
     */
    List<Member> findListByUsername(String name); //컬렉션
    Member findMemberByUsername(String name); //단건
    Optional<Member> findOptionalByUsername(String name); //단건 Optional

    /**
     * 페이징과 정렬 파라미터
     * Pageable -> 페이징(내부에 Sort 포함)
     * Sort -> 정렬
     *
     * 특별한 반환 타입
     * Page -> 추가 count 쿼리 결과를 포함하는 페이징
     * Slice -> 추가 count 쿼리 없이 다음 페이지만 확인 가능(내부적으로 limit + 1조회)
     * Page, Slice Interface를 보면 어떤
     *
     * List(자바 컬렉션) -> 추가 count 쿼리 없이 결과만 반환
     */
//    Page<Member> findByUsernamePage(String name, Pageable pageable); //count 쿼리 사용
//    Slice<Member> findByUsernameSlice(String name, Pageable pageable); //count 쿼리 사용 안함
//    List<Member> findByUsernameList(String name, Pageable pageable); //count 쿼리 사용 안함
//    List<Member> findByUsername(String name, Sort sort);

    Page<Member> findByAge(int age, PageRequest pageRequest);
}
