package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Team;

// 스프링 데이터 JPA가 구현 클래스 대신 생성
// @Repository를 생략 가능
public interface TeamRepository extends JpaRepository<Team, Long> {
}
