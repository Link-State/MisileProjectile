package main;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.common.reflect.TypeToken;

import main.DDLService.ResultType;

/*
 * SQLite 사용법
 * https://heodolf.tistory.com/141
 * 
 */

public class Main extends JavaPlugin {
	public static Plugin PLUGINS = null;
	public static String DB_URL = null;
	public static HashSet<Class<?>> PROJECTILES = null;
	
	public static DDLService DDL = null;
	public static DMLService DML = null;
	public static DQLService DQL = null;
    
	@Override
	public void onEnable() {
		PLUGINS = Bukkit.getPluginManager().getPlugin("MissileProjectile");
		DB_URL = "jdbc:sqlite:" + PLUGINS.getDataFolder() + File.separator + "data.db";
		PROJECTILES = new HashSet<Class<?>>();
		
		File plugin_folder = PLUGINS.getDataFolder();
		if (!plugin_folder.exists()) {
			plugin_folder.mkdirs();
		}

        // Projectile인 Entity들 가져오기
        EntityType[] entities = EntityType.values();
        for (EntityType entity : entities) {
        	Class<? extends Entity> entity_class = entity.getEntityClass();
        	if (entity_class == null) {
        		continue;
        	}
    		Set<?> subinterface_list = TypeToken.of(entity_class).getTypes().interfaces();
    		
    		for (Object subinterface : subinterface_list) {
    			String subinterfaceName = subinterface.toString();
    			
    			if (!subinterfaceName.equals(Projectile.class.getName())) {
    				continue;
    			}
    			
    			PROJECTILES.add(entity_class);
    		}
        }
        
        DDL = new DDLService(DB_URL);
        DML = new DMLService(DB_URL);
        DQL = new DQLService(DB_URL);
        
        try {
        	createSchema();   // 테이블 생성
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        
//		Bukkit.getPluginManager().registerEvents(new MissileShoot(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerIO(), this);
		Bukkit.getPluginManager().registerEvents(new TraceProjectile(), this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		/*
		 * /missileProjectile <on | off> [player_name]
		 * /missileProjectile setting 
		 */
		
		return false;
	}
	
	// 테이블 생성 함수
	public void createSchema() throws SQLException {

        // 공통 테이블 생성
        final String Global_Schema = "CREATE TABLE IF NOT EXISTS Global ( 							"+"\n"
        		+ "  id					INTEGER				PRIMARY KEY			AUTOINCREMENT,		"+"\n"
        		+ "  projectile      	TEXT				DEFAULT '',								"+"\n"
        		+ "  lastModified      	DATETIME													"+"\n"
        		+ "  )";
        createTable("Global", Global_Schema);
        
        // 발사자 테이블 생성
        final String Shooter_Schema = "CREATE TABLE IF NOT EXISTS Shooter ( "+"\n"
        		+ "  name				TEXT           NOT NULL,      "+"\n"
        		+ "  uuid      	  		TEXT,						  "+"\n"
        		+ "  entityType			INTEGER        NOT NULL,      "+"\n"
        		+ "  lastModified      	DATETIME,					  "+"\n"
        		+ "  PRIMARY KEY (name)         				)";
        createTable("Shooter", Shooter_Schema);
        
        // 발사체 테이블 생성
        String ProjectileMap_SQL = null;
        for (Class<?> cls : PROJECTILES) {
        	String prj = cls.getSimpleName().toUpperCase();
        	
            ProjectileMap_SQL = "CREATE TABLE IF NOT EXISTS " + prj + " ("+"\n"
            		+ "  name				TEXT		PRIMARY KEY,		 "+"\n"
            		+ "  isEnable			BOOLEAN		NOT NULL,			 "+"\n"
            		+ "  targetPriority		INTEGER		DEFAULT 0,			 "+"\n"
            		+ "  isTrace			BOOLEAN		DEFAULT false,		 "+"\n"
            		+ "  hasGravity			BOOLEAN     DEFAULT false,		 "+"\n"
            		+ "  minDistance		REAL     	DEFAULT 0.0,		 "+"\n"
            		+ "  maxDistance		REAL     	DEFAULT 23.0,		 "+"\n"
            		+ "  recog_X_Range		REAL     	DEFAULT 23.0,		 "+"\n"
            		+ "  recog_Y_Range		REAL     	DEFAULT 23.0,		 "+"\n"
            		+ "  recog_Z_Range		REAL     	DEFAULT 23.0,		 "+"\n"
            		+ "  minAngle			REAL     	DEFAULT 0.0,		 "+"\n"
            		+ "  maxAngle			REAL     	DEFAULT 70.0,		 "+"\n"
            		+ "  lastModified      	DATETIME,						 "+"\n"
            		+ "  FOREIGN KEY (name) REFERENCES Shooter(name)		 "+"\n"
            		+ "  )";
            createTable(prj, ProjectileMap_SQL);
        }
        
        // DB 연결 종료
        DDL.closeConnection();

        
        // 초기데이터 삽입
        insertInitData();
	}
	
	public void createTable(String name, String sql) throws SQLException {
		 // 테이블 생성
        ResultType result = DDL.createTable(name, sql);
 
        // 테이블 생성 결과 출력
        switch( result ) {
            case SUCCESS:
                System.out.println("[MissileProjectile] Success Create Table - " + name);
                break;
            case WARNING:
                System.out.println("[MissileProjectile] Already Exist Table - " + name);
                break;
            case FAILURE:
                System.out.println("[MissileProjectile] Failed Create Table - " + name);
                break;
        }
	}
	
	public void insertInitData() throws SQLException {
        // 상수 설정
        //   - Data를 저장할 객체 생성
        //     * 입력/수정/삭제/조회 에서 공통으로 사용
		ArrayList<String> projectiles = new ArrayList<String>();
		for (Class<?> cls : PROJECTILES) {
			projectiles.add(cls.getSimpleName().toUpperCase());
		}
		
        final Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("id", 1);
        
        List<Map<String, Object>> resultList = DQL.selectGlobal(dataMap);
        if (resultList.size() > 0) {
            dataMap.put("projectile", String.join(",", projectiles));
            
            // 데이터 입력
            int inserted = DML.insertGlobal(dataMap);
            if( inserted < 0 ) {
                System.out.println("[MissileProjectile] init data input failed");
            }
        }
        
        
		OfflinePlayer[] all_players = Bukkit.getOfflinePlayers();
		EntityType[] all_entities = EntityType.values();

		for (OfflinePlayer offline_p : all_players) {
			dataMap.clear();
			
			// Shooter
			dataMap.put("name", offline_p.getName());
            resultList = DQL.selectShooter(dataMap);

            if (resultList.size() <= 0) {
    			dataMap.put("uuid", offline_p.getUniqueId().toString().toUpperCase());
    			dataMap.put("entityType", 0);
    			DML.insertShooter(dataMap);
            }

			for (String prj : projectiles) {
				dataMap.clear();
	            dataMap.put("name", offline_p.getName());
	            resultList = DQL.selectProjectile(prj, dataMap);
	            
	            if (resultList.size() > 0) {
	            	continue;
	            }
	            
	            dataMap.put("isEnable", false);
	            dataMap.put("targetPriority", 0);
	            dataMap.put("isTrace", false);
	            dataMap.put("hasGravity", true);
	            dataMap.put("minDistance", 0.0);
	            dataMap.put("maxDistance", 5.0);
	            dataMap.put("recog_X_Range", 23.0);
	            dataMap.put("recog_Y_Range", 23.0);
	            dataMap.put("recog_Z_Range", 23.0);
	            dataMap.put("minAngle", 0.0);
	            dataMap.put("maxAngle", 70.0);
	            DML.insertProjectile(prj, dataMap);
			}
		}

		for (EntityType entity : all_entities) {
			if (!entity.isAlive() || entity.equals(EntityType.PLAYER)) {
				continue;
			}

			dataMap.clear();
			
			// Shooter
			dataMap.put("name", entity.getKey().getKey().toUpperCase());
            resultList = DQL.selectShooter(dataMap);

            if (resultList.size() <= 0) {
    			dataMap.put("uuid", "");
    			dataMap.put("entityType", 1);
    			DML.insertShooter(dataMap);
            }

			for (String prj : projectiles) {
				dataMap.clear();
	            dataMap.put("name", entity.getKey().getKey().toUpperCase());
	            resultList = DQL.selectProjectile(prj, dataMap);
	            
	            if (resultList.size() > 0) {
	            	continue;
	            }
	            
	            dataMap.put("isEnable", false);
	            dataMap.put("targetPriority", 0);
	            dataMap.put("isTrace", false);
	            dataMap.put("hasGravity", true);
	            dataMap.put("minDistance", 0.0);
	            dataMap.put("maxDistance", 5.0);
	            dataMap.put("recog_X_Range", 23.0);
	            dataMap.put("recog_Y_Range", 23.0);
	            dataMap.put("recog_Z_Range", 23.0);
	            dataMap.put("minAngle", 0.0);
	            dataMap.put("maxAngle", 70.0);
	            DML.insertProjectile(prj, dataMap);
			}
		}
	}
}
