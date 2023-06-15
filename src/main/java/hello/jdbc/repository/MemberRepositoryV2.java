package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - ConnectionParam
 */
@Slf4j
public class MemberRepositoryV2 {

    // DataSource 에 의존관계 주입
    // 이제 외부에서 DataSource 를 주입받기에, 직접만든 DBConnectionUtil 사용하지 않아도 된다.
    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        // 데이터베이스에 전달할 SQL을 정의한다. 여기서는 데이터를 등록해야 하므로 insert sql 을 준비했다.
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            // 이전에 만들어둔 DBConnectionUtil 를 통해서 데이터베이스 커넥션을 획득
            con = getConnection();
            // 데이터베이스에 전달할 SQL 과 파라미터로 전달할 데이터들을 준비한다.
            // sql : insert into member(member_id, money) values(?, ?)"
            pstmt = con.prepareStatement(sql);
            // SQL 의 첫번째 ? 에 값을 지정한다. 문자이므로 setString 을 사용한다.
            pstmt.setString(1, member.getMemberId());
            // SQL 의 두번째 ? 에 값을 지정한다. Int 형 숫자이므로 setInt 를 지정한다.
            pstmt.setInt(2, member.getMoney());
            // Statement 를 통해 준비된 SQL 을 커넥션을 통해 실제 데이터베이스에 전달한다.
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public Member finById(String memberId) throws SQLException {
        // 데이터 조회를 위한 select SQL 을 준비한다.
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            // 데이터를 조회, 결과를 ResultSet 에 담아서 반환한다.
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId" + memberId);
            }

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public Member finById(Connection con, String memberId) throws SQLException {
        // 데이터 조회를 위한 select SQL 을 준비한다.
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            // 데이터를 조회, 결과를 ResultSet 에 담아서 반환한다.
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId" + memberId);
            }

        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            // connection 여기서 닫지 않는다.
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(con);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            // connection 여기서 닫지 않는다.
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            // 이전에 만들어둔 DBConnectionUtil 를 통해서 데이터베이스 커넥션을 획득
            con = getConnection();
            // 데이터베이스에 전달할 SQL 과 파라미터로 전달할 데이터들을 준비한다.
            // sql : insert into member(member_id, money) values(?, ?)"
            pstmt = con.prepareStatement(sql);
            // SQL 의 첫번째 ? 에 값을 지정한다. 문자이므로 setString 을 사용한다.
            pstmt.setString(1, memberId);
            // Statement 를 통해 준비된 SQL 을 커넥션을 통해 실제 데이터베이스에 전달한다.
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        // JdbcUtils 을 사용하면 커넥션을 좀 더 편리하게 닫을 수 있다.
        // 주렁주렁 다 까트 할 수 있다.
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
