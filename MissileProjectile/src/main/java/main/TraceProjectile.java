package main;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

public class TraceProjectile implements Runnable {
	private Projectile projectile;
	private LivingEntity target;
	private Location prevLocation;
	private int lifeTime;
	
	public TraceProjectile(Projectile prj, LivingEntity entity, int life) {
		this.projectile = prj;
		this.target = entity;
		this.lifeTime = life;
		this.prevLocation = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
		prj.getLocation(this.prevLocation);

		// SQL에 발사체 uuid 등록
	}
	
	public void run() {
		int count = 0;
		int loop = this.lifeTime / 10;
		for (int i = 0; i < loop; i++) {
			// 쓰레드가 interrupt되면 종료
			if (threadSleep(10)) {
				break;
			}

//			// 발사체가 없어지면 종료
//			if (!this.projectile.isValid()) {
//				break;
//			}
//
//			// 타겟이 죽었으면 종료
//			if (this.target.isDead()) {
//				break;
//			}
//			
//			// 발사체 속도가 0에 근사하면 종료
//			if (this.projectile.getVelocity().lengthSquared() < 0.0005) {
//				break;
//			}
//			
//			// 발사체 위치변화 없으면 종료
//			if (this.projectile.getLocation().equals(this.prevLocation)) {
//				// 0.1초 이상 위치변화 없으면 종료
//				if (count >= 10) {
//					break;
//				}
//				count++;
//			}
//			else {
//				count = 0;
//			}
			
			// 히트박스가 한 개인 엔티티
			// 여러개인 경우도 만들기~
//			if (this.target.getBoundingBox().overlaps(this.projectile.getBoundingBox())) {
//				break;
//			}
			
			// 위치 업데이트
			this.projectile.getLocation(this.prevLocation);
			
			// 속도 변경
			Vector shooter2target = new Vector(
					this.target.getEyeLocation().getX() - this.projectile.getLocation().getX(),
					(this.target.getLocation().getY() + (this.target.getHeight() / 2.0)) - this.projectile.getLocation().getY(),
					this.target.getEyeLocation().getZ() - this.projectile.getLocation().getZ());
			
			shooter2target = shooter2target.normalize();
			shooter2target = shooter2target.multiply(this.projectile.getVelocity().length());
			
			this.projectile.setVelocity(shooter2target);
		}
		
		// 레코드 삭제
		deleteRecord();
	}
	
	public void deleteRecord() {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("uuid", this.projectile.getUniqueId().toString());

        DQLService DQL = new DQLService(Main.DB_URL);
        List<Map<String, Object>> resultList = DQL.selectAliveProjectile(dataMap);
        
        // 해당 UUID가 존재하지 않으면 종료
        if (resultList.size() <= 0) {
        	return;
        }
        
        // 레코드 삭제
        try {
            DMLService DML = new DMLService(Main.DB_URL);
			DML.deleteAliveProjectile(dataMap);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public boolean threadSleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			return true;
		}
		
		return false;
	}
}
