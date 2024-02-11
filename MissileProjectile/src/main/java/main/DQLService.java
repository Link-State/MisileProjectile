package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
public class DQLService extends SQLiteManager {
    
    // 생성자
    public DQLService(String url) {
        super(url);
    }
 
    // 데이터 조회 함수
    public List<Map<String, Object>> selectProjectile(String projectile, Map<String, Object> dataMap){
        // 상수설정
        //   - SQL
        final String query = "SELECT name            "+"\n"
                         + "     , isEnable            "+"\n"
                         + "     , targetPriority            "+"\n"
                         + "     , isTrace              "+"\n"
                         + "     , hasGravity                "+"\n"
                         + "     , minDistance            "+"\n"
                         + "     , maxDistance           "+"\n"
                         + "     , RecogRange           "+"\n"
                         + "     , minAngle           "+"\n"
                         + "     , maxAngle           "+"\n"
                         + "  FROM " + projectile + " "+"\n"
                         + "  WHERE name = ?    "+"\n"
                         ;
        
        //   - 조회 결과 변수
        final Set<String> columnNames = new HashSet<String>();        
        final List<Map<String, Object>> selected = new ArrayList<Map<String, Object>>();
 
        // 변수설정
        //   - Database 변수
        Connection conn = ensureConnection();
        PreparedStatement pstmt = null;
        ResultSetMetaData meta = null;
        
        try {
            // PreparedStatement 객체 생성
            pstmt = conn.prepareStatement(query);
            
            // 조회 데이터 조건 매핑
            pstmt.setObject(1, dataMap.get("name"));
            
            // 데이터 조회
            ResultSet rs = pstmt.executeQuery();
            
            // 조회된 데이터의 컬럼명 저장
            meta = pstmt.getMetaData();
            for(int i=1; i<=meta.getColumnCount(); i++) {
                columnNames.add(meta.getColumnName(i));
            }
            
            // ResultSet -> List<Map> 객체
            Map<String, Object> resultMap = null;
            
            while(rs.next()) {
                resultMap = new HashMap<String, Object>();
                
                for(String column : columnNames) {
                    resultMap.put(column, rs.getObject(column));
                }
                
                if( resultMap != null ) {
                    selected.add(resultMap);
                }
            }
            
        } catch (SQLException e) {
            // 오류처리
            System.out.println(e.getMessage());
            
        } finally  {
            try {
                // PreparedStatement 종료
                if( pstmt != null ) {
                    pstmt.close();
                }
                
                // Database 연결 종료
                closeConnection();
                
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
 
        // 결과 반환
        //   - 조회된 데이터 리스트
        return selected;
    }
    

    // 데이터 조회 함수
    public List<Map<String, Object>> selectShooter(Map<String, Object> dataMap){
        // 상수설정
        //   - SQL
        final String query = "SELECT name            "+"\n"
                         + "     , uuid            "+"\n"
                         + "     , entityType      "+"\n"
                         + "  FROM Shooter "+"\n"
                         + "  WHERE name = ?    "+"\n"
                         ;
        
        //   - 조회 결과 변수
        final Set<String> columnNames = new HashSet<String>();        
        final List<Map<String, Object>> selected = new ArrayList<Map<String, Object>>();
 
        // 변수설정
        //   - Database 변수
        Connection conn = ensureConnection();
        PreparedStatement pstmt = null;
        ResultSetMetaData meta = null;
        
        try {
            // PreparedStatement 객체 생성
            pstmt = conn.prepareStatement(query);
            
            // 조회 데이터 조건 매핑
            pstmt.setObject(1, dataMap.get("name"));
            
            // 데이터 조회
            ResultSet rs = pstmt.executeQuery();
            
            // 조회된 데이터의 컬럼명 저장
            meta = pstmt.getMetaData();
            for(int i=1; i<=meta.getColumnCount(); i++) {
                columnNames.add(meta.getColumnName(i));
            }
            
            // ResultSet -> List<Map> 객체
            Map<String, Object> resultMap = null;
            
            while(rs.next()) {
                resultMap = new HashMap<String, Object>();
                
                for(String column : columnNames) {
                    resultMap.put(column, rs.getObject(column));
                }
                
                if( resultMap != null ) {
                    selected.add(resultMap);
                }
            }
            
        } catch (SQLException e) {
            // 오류처리
            System.out.println(e.getMessage());
            
        } finally  {
            try {
                // PreparedStatement 종료
                if( pstmt != null ) {
                    pstmt.close();
                }
                
                // Database 연결 종료
                closeConnection();
                
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
 
        // 결과 반환
        //   - 조회된 데이터 리스트
        return selected;
    }

    // 데이터 조회 함수
    public List<Map<String, Object>> selectGlobal(Map<String, Object> dataMap){
        // 상수설정
        //   - SQL
        final String query = "SELECT id                 "+"\n"
                         + "     , projectile         "+"\n"
                         + "  FROM Global "+"\n"
                         + "  WHERE id = ?    "+"\n"
                         ;
        
        //   - 조회 결과 변수
        final Set<String> columnNames = new HashSet<String>();        
        final List<Map<String, Object>> selected = new ArrayList<Map<String, Object>>();
 
        // 변수설정
        //   - Database 변수
        Connection conn = ensureConnection();
        PreparedStatement pstmt = null;
        ResultSetMetaData meta = null;
        
        try {
            // PreparedStatement 객체 생성
            pstmt = conn.prepareStatement(query);
            
            // 조회 데이터 조건 매핑
            pstmt.setObject(1, dataMap.get("id"));
            
            // 데이터 조회
            ResultSet rs = pstmt.executeQuery();
            
            // 조회된 데이터의 컬럼명 저장
            meta = pstmt.getMetaData();
            for(int i=1; i<=meta.getColumnCount(); i++) {
                columnNames.add(meta.getColumnName(i));
            }
            
            // ResultSet -> List<Map> 객체
            Map<String, Object> resultMap = null;
            
            while(rs.next()) {
                resultMap = new HashMap<String, Object>();
                
                for(String column : columnNames) {
                    resultMap.put(column, rs.getObject(column));
                }
                
                if( resultMap != null ) {
                    selected.add(resultMap);
                }
            }
            
        } catch (SQLException e) {
            // 오류처리
            System.out.println(e.getMessage());
            
        } finally  {
            try {
                // PreparedStatement 종료
                if( pstmt != null ) {
                    pstmt.close();
                }
                
                // Database 연결 종료
                closeConnection();
                
            } catch ( SQLException e ) {
                e.printStackTrace();
            }
        }
 
        // 결과 반환
        //   - 조회된 데이터 리스트
        return selected;
    }
 
    // 조회 결과 출력 함수
    public void printMapList(List<Map<String, Object>> mapList) {
        if( mapList.size() == 0 ) {
            System.out.println("No searched data");
            return;
        }
        
        // 상세 데이터 출력
        System.out.println(String.format("Data search result: %d", mapList.size()));
        
        for(int i = 1; i <= mapList.size(); i++) {
            Map<String, Object> map = mapList.get(i-1);
            
            StringBuilder sb = new StringBuilder();
            
            sb.append(i);
            sb.append(": {");
            map.entrySet().forEach(( entry )->{
                sb.append('"')
                    .append(entry.getKey())
                    .append("\": \"")
                    .append(entry.getValue())
                    .append("\", ");
            });
            sb.append("}");
            
            System.out.println(sb.toString());
        }
    }
}