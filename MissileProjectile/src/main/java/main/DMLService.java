package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
 
public class DMLService extends SQLiteManager {
    
    // 생성자
    public DMLService(String url) {
        super(url);
    }
    
    public int insertGlobal(Map<String, Object> dataMap) throws SQLException {
        final String sql = "INSERT INTO Global ("+"\n"
                   + "    id,                         "+"\n"
                   + "    projectile,                 "+"\n"
           		   + "    lastModified      		  "+"\n"
                   + ") VALUES (                      "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?                           "+"\n"
                   + ")";

        // 상수 설정
        //   - DateFormat 설정
        SimpleDateFormat DATETIME_FMT = new SimpleDateFormat("yyyyMMddHHmmss");
        
        // 변수설정
        //   - Database 변수
        Connection conn = ensureConnection();
        PreparedStatement pstmt = null;
 
        //   - 입력 결과 변수
        int inserted = 0;
 
        try {
            // PreparedStatement 생성
            pstmt = conn.prepareStatement(sql);
 
            // 입력 데이터 매핑
            pstmt.setObject(1, dataMap.get("id"));
            pstmt.setObject(2, dataMap.get("projectile"));
            pstmt.setObject(3, DATETIME_FMT.format(new Date()));
 
            // 쿼리 실행
            pstmt.executeUpdate();
 
            // 입력건수  조회
            inserted = pstmt.getUpdateCount();
 
            // 트랜잭션 COMMIT
            conn.commit();
 
        } catch (SQLException e) {
            // 오류출력
            System.out.println(e.getMessage());
            
            // 트랜잭션 ROLLBACK
            if( conn != null ) {
                conn.rollback();
            }
            
            // 오류
            inserted = -1;
 
        } finally {
            // PreparedStatement 종료
            if( pstmt != null ) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
 
        // 결과 반환
        //   - 입력된 데이터 건수
        return inserted;
    }
    
    // 데이터 삽입 함수
    public int insertShooter(Map<String, Object> dataMap) throws SQLException {
        final String sql = "INSERT INTO Shooter ("+"\n"
                   + "    name,                         "+"\n"
                   + "    uuid,                         "+"\n"
                   + "    entityType,                   "+"\n"
           		   + "    lastModified      		    "+"\n"
                   + ") VALUES (                        "+"\n"
                   + "    ?,                            "+"\n"
                   + "    ?,                            "+"\n"
                   + "    ?,                            "+"\n"
                   + "    ?                             "+"\n"
                   + ")";

        // 상수 설정
        //   - DateFormat 설정
        SimpleDateFormat DATETIME_FMT = new SimpleDateFormat("yyyyMMddHHmmss");
        
        // 변수설정
        //   - Database 변수
        Connection conn = ensureConnection();
        PreparedStatement pstmt = null;
 
        //   - 입력 결과 변수
        int inserted = 0;
 
        try {
            // PreparedStatement 생성
            pstmt = conn.prepareStatement(sql);
 
            // 입력 데이터 매핑
            pstmt.setObject(1, dataMap.get("name"));
            pstmt.setObject(2, dataMap.get("uuid"));
            pstmt.setObject(3, dataMap.get("entityType"));
            pstmt.setObject(4, DATETIME_FMT.format(new Date()));
 
            // 쿼리 실행
            pstmt.executeUpdate();
 
            // 입력건수  조회
            inserted = pstmt.getUpdateCount();
 
            // 트랜잭션 COMMIT
            conn.commit();
 
        } catch (SQLException e) {
            // 오류출력
            System.out.println(e.getMessage());
            
            // 트랜잭션 ROLLBACK
            if( conn != null ) {
                conn.rollback();
            }
            
            // 오류
            inserted = -1;
 
        } finally {
            // PreparedStatement 종료
            if( pstmt != null ) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
 
        // 결과 반환
        //   - 입력된 데이터 건수
        return inserted;
    }
    

    // 데이터 삽입 함수
    public int insertProjectile(String projectile, Map<String, Object> dataMap) throws SQLException {
        final String sql = "INSERT INTO " + projectile.toUpperCase() + " ("+"\n"
                   + "    name,                       "+"\n"
                   + "    isEnable,                   "+"\n"
                   + "    targetPriority,             "+"\n"
                   + "    isTrace,                    "+"\n"
                   + "    hasGravity,                 "+"\n"
                   + "    minDistance,                "+"\n"
                   + "    maxDistance,                "+"\n"
                   + "    RecogRange,                 "+"\n"
                   + "    minAngle,                   "+"\n"
                   + "    maxAngle,                   "+"\n"
           		   + "    lastModified      	      "+"\n"
                   + ") VALUES (                      "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?,                          "+"\n"
                   + "    ?                           "+"\n"
                   + ")";

        // 상수 설정
        //   - DateFormat 설정
        SimpleDateFormat DATETIME_FMT = new SimpleDateFormat("yyyyMMddHHmmss");
        
        // 변수설정
        //   - Database 변수
        Connection conn = ensureConnection();
        PreparedStatement pstmt = null;
 
        //   - 입력 결과 변수
        int inserted = 0;
 
        try {
            // PreparedStatement 생성
            pstmt = conn.prepareStatement(sql);
 
            // 입력 데이터 매핑
            pstmt.setObject(1,  dataMap.get("name"));
            pstmt.setObject(2,  dataMap.get("isEnable"));
            pstmt.setObject(3,  dataMap.get("targetPriority"));
            pstmt.setObject(4,  dataMap.get("isTrace"));
            pstmt.setObject(5,  dataMap.get("hasGravity"));
            pstmt.setObject(6,  dataMap.get("minDistance"));
            pstmt.setObject(7,  dataMap.get("maxDistance"));
            pstmt.setObject(8,  dataMap.get("RecogRange"));
            pstmt.setObject(9,  dataMap.get("minAngle"));
            pstmt.setObject(10, dataMap.get("maxAngle"));
            pstmt.setObject(11, DATETIME_FMT.format(new Date()));
 
            // 쿼리 실행
            pstmt.executeUpdate();
 
            // 입력건수  조회
            inserted = pstmt.getUpdateCount();
 
            // 트랜잭션 COMMIT
            conn.commit();
 
        } catch (SQLException e) {
            // 오류출력
            System.out.println(e.getMessage());
            
            // 트랜잭션 ROLLBACK
            if( conn != null ) {
                conn.rollback();
            }
            
            // 오류
            inserted = -1;
 
        } finally {
            // PreparedStatement 종료
            if( pstmt != null ) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
 
        // 결과 반환
        //   - 입력된 데이터 건수
        return inserted;
    }
}