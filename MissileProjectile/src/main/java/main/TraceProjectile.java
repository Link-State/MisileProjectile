package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class TraceProjectile implements Listener {
	
	@EventHandler
	public void launchedProjectile(ProjectileLaunchEvent e) {
		
		Projectile prj = e.getEntity();
		ProjectileSource source = prj.getShooter();
		
		if (source == null) {
			return;
		}
		
		LivingEntity shooter = null;
		if (!(source instanceof LivingEntity)) {
			return;
		}

		// SQL에서 발사자 정보 가져옴,
		// SQL에서 해당 엔티티의 발사체 고유 정보 가져옴
		shooter = (LivingEntity) source;
		
		final Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("name"   , shooter.getName());

		String prjType = null;
        for (Class<?> cls : Main.PROJECTILES) {
        	if (cls.isInstance(prj)) {
        		prjType = cls.getSimpleName().toUpperCase();
        		break;
        	}
        }
        
        if (prjType == null) {
        	return;
        }
        
        // 데이터 조회
        // 발사체 발사할 때 마다, 재연결 -> 탐색 -> 연결종료 함. 너무 비효율적임
        List<Map<String, Object>> result = Main.DQL.selectProjectile(prjType, dataMap);
        
        // DB 생성
        if (result.size() <= 0) {
        	return;
        }
        
        // 조회 결과 출력
        Main.DQL.printMapList(result);
		
		// 해당 발사체 유도 활성화 검사
		
		
		
		// 발사체 유도
		
		ArrayList<Entity> entities = new ArrayList<Entity>(shooter.getNearbyEntities(23, 23, 23));
		
		if (entities.size() <= 0) {
			return;
		}
		
		// 가장 가까운 상대에게 유도
		double minDistance = 10000.0;
		double maxAngle = 1000.0;
		LivingEntity target = null;
		Vector missileVec = null;
		for (Entity entity : entities) {
			if (!(entity instanceof LivingEntity)) {
				continue;
			}
			
			LivingEntity living_entity = (LivingEntity) entity;
			
			// 몹의 위치 - 내 위치
			Vector player2target_vec = new Vector(
					living_entity.getEyeLocation().getX() - shooter.getEyeLocation().getX(),
					(living_entity.getLocation().getY() + (living_entity.getHeight() / 2.0)) - shooter.getEyeLocation().getY(),
					living_entity.getEyeLocation().getZ() - shooter.getEyeLocation().getZ());
			
			double angle = player2target_vec.angle(shooter.getLocation().getDirection()) * (180/Math.PI);
			
			if (angle > 70.0) {
				continue;
			}
			
			// 거리
			double distance = player2target_vec.lengthSquared();
			System.out.println(living_entity.getName() + " : " + angle);
			
			if (maxAngle > angle) {
				maxAngle = angle;
				minDistance = distance;
				target = living_entity;
				missileVec = new Vector();
				missileVec.setX(player2target_vec.getX());
				missileVec.setY(player2target_vec.getY());
				missileVec.setZ(player2target_vec.getZ());
			}
		}
		
		
		if (target == null) {
			return;
		}
		
		System.out.println(" -> " + target.getName() + " : " + maxAngle);
		System.out.println(shooter.getVelocity());
		
		System.out.println("--------------------------------------------");
		
		// 발사체의 방향을 수정
		missileVec = missileVec.add(target.getVelocity()); // 자동추격 대상의 속도를 적용
		missileVec = missileVec.normalize(); // 자동추격 발사체의 힘을 1로 수정
		missileVec = missileVec.multiply(prj.getVelocity().length() + 30); // 자동추격 발사체의 힘을 원래 발사체의 힘으로 수정
		
		
		Location direction = new Location(prj.getWorld(), 0, 0, 0);
		direction.add(missileVec);
		
		prj.setVelocity(missileVec);
		prj.setRotation(direction.getYaw(), direction.getPitch());
		
		
		
		// 죽음의 추격 활성화 시, SQL에 해당 발사체 등록 및 쓰레드 실행
	}
}
