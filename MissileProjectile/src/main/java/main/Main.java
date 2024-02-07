package main;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
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
	
	DDLService DDL = null;
    
	@Override
	public void onEnable() {
		PLUGINS = Bukkit.getPluginManager().getPlugin("MissileProjectile");
		DB_URL = "jdbc:sqlite:" + PLUGINS.getDataFolder() + File.separator + "data.db";
		
		SQLiteManager manager = new SQLiteManager(DB_URL);
		
        manager.createConnection();     // 연결
        manager.closeConnection();      // 연결 해제
        manager.ensureConnection();     // 재연결
        
        DDL = new DDLService(DB_URL);
        try {
        	createSchema();   // 테이블 생성
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        
//		Bukkit.getPluginManager().registerEvents(new MissileArrow(), this);
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
        //
        String Shooter_Schema = "CREATE TABLE IF NOT EXISTS Shooter ( "+"\n"
        		+ "  name				TEXT           NOT NULL,      "+"\n"
        		+ "  uuid      	  		TEXT,						  "+"\n"
        		+ "  entityType			INTEGER        NOT NULL,      "+"\n"
        		+ "  PRIMARY KEY (name)         				)";
        createTable("Shooter", Shooter_Schema);
        
        String Global_Schema = "CREATE TABLE IF NOT EXISTS Global ( 							"+"\n"
        		+ "  id					INTEGER				PRIMARY KEY			AUTOINCREMENT,	"+"\n"
        		+ "  projectile      	TEXT,				DEFAULT \"\"						"+"\n"
        		+ "  )";
        createTable("Shooter", Global_Schema);
        
        ArrayList<String> projectiles = new ArrayList<String>(); 
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
    			
				projectiles.add(entity_class.getSimpleName());
    		}
        }
        
        String ProjectileMap_SQL = null;
        for (String prj : projectiles) {
            ProjectileMap_SQL = "CREATE TABLE IF NOT EXISTS " + prj + " ("+"\n"
            		+ "  name			TEXT		PRIMARY KEY,		 "+"\n"
            		+ "  isEnable		BOOLEAN		NOT NULL,			 "+"\n"
            		+ "  targetMethod	INTEGER		DEFAULT 0,			 "+"\n"
            		+ "  isTrace		BOOLEAN		DEFAULT false,		 "+"\n"
            		+ "  FOREIGN KEY (name) REFERENCES Shooter(name)	 "+"\n"
            		+ "  )";
            createTable("ProjectileMap", ProjectileMap_SQL);
        }
        
        // ProjectileMap_SQL의 컬럼에 모든 발사체 타입이 있는지 확인
        // 없으면 새 컬럼 생성
        
        // DB 연결 종료
        DDL.closeConnection();
	}
	
	public void createTable(String name, String sql) throws SQLException {
		 // 테이블 생성
        ResultType result = DDL.createTable(name, sql);
 
        // 테이블 생성 결과 출력
        switch( result ) {
            case SUCCESS:
                System.out.println("Success Create Table");
                break;
            case WARNING:
                System.out.println("Already Exist Table");
                break;
            case FAILURE:
                System.out.println("Failed Create Table");
                break;
        }
	}
}
