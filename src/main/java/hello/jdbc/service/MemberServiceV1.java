package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    // formId 의 회원을 조회해서 toId 의 회원에게 money 만큼의 돈을 계좌이체 하는 로직이다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.finById(fromId);
        Member toMember = memberRepository.finById(toId);

        // fromId 회원의 돈을 money 만큼 감소한다
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        // toId 회원의 돈을 money 만큼 증가한다
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private static void validation(Member toMember) {
        // 예외 상황을 테스트해보기 위해 toId 가 "ex" 인 경우 예외를 발생한다.
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
