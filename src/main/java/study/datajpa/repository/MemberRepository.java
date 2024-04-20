package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
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
public interface MemberRepository extends JpaRepository<Member,Long>, MemberRepositoryCustom {

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
     *
     * 실제론 이 결과를 DTO로 변환화여 반환하라
     */
//    Page<Member> findByUsernamePage(String name, Pageable pageable); //count 쿼리 사용
//    Slice<Member> findByUsernameSlice(String name, Pageable pageable); //count 쿼리 사용 안함
//    List<Member> findByUsernameList(String name, Pageable pageable); //count 쿼리 사용 안함
//    List<Member> findByUsername(String name, Sort sort);
    Page<Member> findByAge(int age, PageRequest pageRequest);

    /*
        아래와 같이 count 쿼리를 분리할 수도 있다.
        count 쿼리에서 불필요하게 left join을 하게되면 비 효율적
        count 쿼리에선 굳이 join안해도 될때
     */
//    @Query(value = "select m from Member m left join m.team ",
//            countQuery = "select count(m.username) from Member m")
//    Page<Member> findMemberAllCountBy(Pageable pageable);

    @Modifying(clearAutomatically = true) // -> 얘가 있어야지 JPA에서 .getResult() 등이 아닌 .executeUpdate()가 실행된다.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    // Member를 끌고올때 연관된 Team 같이 끌고옴
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    // 원래는 fetch join 쿼리문 짜야하는데 간편하게 -> Entity Graph로 간편하게
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"}) // or @EntityGraph("Member.all")
    List<Member> findEntityGraphByUsername(@Param("username") String username);


    /**
     *     해당 쿼리를 통해 조회된 엔티티가 읽기 전용으로 처리되어, 변경 감지(dirty checking) 및 스냅샷 관리를 하지 않게 된다.
     *     이는 메모리 사용량을 줄이고, 성능을 향상시키는 데 도움
     */
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    // JPA인데 Spring Data에서 Lock을 편리하게 쓸수있도록 해준다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);
}
