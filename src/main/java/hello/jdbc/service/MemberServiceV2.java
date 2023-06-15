package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    // formId 의 회원을 조회해서 toId 의 회원에게 money 만큼의 돈을 계좌이체 하는 로직이다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // Connection con = dataSource.getConnection() 트랜잭션을 시작하려면 커넥션이 필요하다.
        Connection con = dataSource.getConnection();
        try {
            // 트랜잭션 오토커밋 리모컨
            con.setAutoCommit(false); // 트랜잭션 시작
            // 비즈니스 로직 수행, 분리한 이유는 트랜잭션을 관리하는 로직과 실제 비즈니스 로직을 구분하기 위함이다.
            // con.commit() 비즈니스 로직이 정상 수행되면 트랜잭션을 커밋
            bizLogic(con, fromId, toId, money); con.commit(); //성공시 커밋
        } catch (Exception e) {
            // 예외가 발생하면 트랜잭션을 롤백
            con.rollback(); // 실패 시 롤백
            throw new IllegalStateException(e);
        } finally {
            // finally {..} 를 사용해서 커넥션을 모두 사용하고 나면 안전하게 종료한다.
            release(con);
        }
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.finById(con, fromId);
        Member toMember = memberRepository.finById(con, toId);
        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(con, toId, toMember.getMoney() + money);
    }

    private static void validation(Member toMember) {
        // 예외 상황을 테스트해보기 위해 toId 가 "ex" 인 경우 예외를 발생한다.
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }

    private static void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); // 커넥션 풀 고려
                con.close();
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}

