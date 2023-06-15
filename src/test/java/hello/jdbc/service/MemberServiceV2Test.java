package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 커넥션 파라미터 전달 방식 동기화
 */
class MemberServiceV2Test {

    private MemberRepositoryV2 memberRepository;
    private MemberServiceV2 memberService;

    @BeforeEach // 각각의 테스트가 수행되기 전에 실행된다.
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV2(dataSource);
        memberService = new MemberServiceV2(dataSource, memberRepository);
    }

    // 테스트 끝나고 다 지워주기 때문에, 반복 수행 가능해진다.
    @AfterEach // 각각의 테스트가 실행되고 난 이후에 실행된다.
    void after() throws SQLException {
        memberRepository.delete("memberA");
        memberRepository.delete("memberB");
        memberRepository.delete("ex");
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        // given 계좌이체가 정상 수행되었는지 검증한다.
        Member memberA = new Member("memberA", 10000);
        Member memberB = new Member("memberB", 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when 계좌이체 로직을 실행한다.
        //        실행                        멤버A           ->        멤버B          =  2000원 이체
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        // then 계좌이체가 정상 수행되었는지 검증한다.
        Member findMemberA = memberRepository.finById(memberA.getMemberId());
        Member findMemberB = memberRepository.finById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferEx() throws SQLException {
        // given 다음 데이터를 저장해서 테스트를 준비한다.
        Member memberA = new Member("memberA", 10000);
        Member memberEx = new Member("ex", 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when 계좌이체 로직을 실행한다.
        //                                  실행                       멤버A           ->        멤버B          =  2000원 이체
        // memberEx 회원의 ID는 ex 이므로 중간에 예외가 발생한다
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then 계좌이체가 정상 수행되었는지 검증한다.
        Member findMemberA = memberRepository.finById(memberA.getMemberId());
        Member findMemberB = memberRepository.finById(memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        // memberEx 10000원 - 중간에 실패로 로직이 수행되지 않았다. 따라서 그대로 10000원으로 남아있게 된다.
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}