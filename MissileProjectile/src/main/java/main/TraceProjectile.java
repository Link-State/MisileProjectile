package main;

import java.util.ArrayList;
import java.util.Date;
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
        List<Map<String, Object>> result = Main.DQL.selectProjectile(prjType, dataMap);
        
        // DB 생성
        if (result.size() <= 0) {
        	return;
        }
		
		// 해당 발사체 유도 활성화 검사
        Map<String, Object> asdf = result.get(0);
        boolean isEnable = (((int) asdf.get("isEnable")) != 0);
        boolean hasGravity = (((int) asdf.get("hasGravity")) != 0);
        boolean isTrace = (((int) asdf.get("isTrace")) != 0);
        int targetPriority = (int) asdf.get("targetPriority");
        double minDistance = (double) asdf.get("minDistance");
        double maxDistance = (double) asdf.get("maxDistance");
        double recog_X_Range = (double) asdf.get("recog_X_Range");
        double recog_Y_Range = (double) asdf.get("recog_Y_Range");
        double recog_Z_Range = (double) asdf.get("recog_Z_Range");
        double minAngle = (double) asdf.get("minAngle");
        double maxAngle = (double) asdf.get("maxAngle");
        
        // 유도발사체 기능이 꺼져있으면
        if (!isEnable) {
        	return;
        }
		
		// 
		int standardUnit = 0; // 0 = 거리, 1 = 각도
		int sort = 1; // 1 = 가장 가까운 대상, -1 = 가장 먼 대상
		double minValue = Math.pow(minDistance, 2);
		double maxValue = Math.pow(maxDistance, 2);
		double localExtremumValue = maxAngle + maxValue + 1.0;
		
		// 조준선 
		if (targetPriority == 2 || targetPriority == 3) {
			standardUnit = 1;
			minValue = minAngle;
			maxValue = maxAngle;
		}
		
		if (targetPriority == 1 || targetPriority == 3) {
			sort = -1;
		}
		
		// 인식범위 내 엔티티 검사
		ArrayList<Entity> entities = null;
		if (standardUnit == 0) {
			entities = new ArrayList<Entity>(shooter.getNearbyEntities(maxDistance, maxDistance, maxDistance));
		} else if (standardUnit == 1) {
			entities = new ArrayList<Entity>(shooter.getNearbyEntities(recog_X_Range, recog_Y_Range, recog_Z_Range));
		}
		
		if (entities.size() <= 0) {
			return;
		}
		
		LivingEntity missileTarget = null;
		Vector missileVector = null;
		for (Entity entity : entities) {
			// 살아있는 엔티티가 아니면 스킵
			if (!(entity instanceof LivingEntity)) {
				continue;
			}
			
			LivingEntity living_entity = (LivingEntity) entity;
			
			// 몹의 위치에서 내 위치를 빼서 두 위치 간 거리벡터 생성
			Vector player2target_vec = new Vector(
					living_entity.getEyeLocation().getX() - shooter.getEyeLocation().getX(),
					(living_entity.getLocation().getY() + (living_entity.getHeight() / 2.0)) - shooter.getEyeLocation().getY(),
					living_entity.getEyeLocation().getZ() - shooter.getEyeLocation().getZ());
			
			// 
			double distance_or_angle = 0.0;
			if (standardUnit == 0) {
				distance_or_angle = player2target_vec.lengthSquared();
			}
			else if (standardUnit == 1) {
				distance_or_angle = player2target_vec.angle(shooter.getLocation().getDirection()) * (180/Math.PI);
			}
			
			if (minValue > distance_or_angle || distance_or_angle > maxValue) {
				continue;
			}
			
			System.out.println(entity.getType() + " : " + distance_or_angle);
			
			if (localExtremumValue > sort * distance_or_angle) {
				localExtremumValue = sort * distance_or_angle;
				missileTarget = living_entity;
				missileVector = new Vector();
				missileVector.setX(player2target_vec.getX());
				missileVector.setY(player2target_vec.getY());
				missileVector.setZ(player2target_vec.getZ());
			}
		}
		
		if (missileTarget == null) {
			return;
		}
		
		// 발사체의 방향을 수정
		missileVector = missileVector.normalize(); // 자동추격 발사체의 힘을 1로 수정
		missileVector = missileVector.multiply(prj.getVelocity().length()); // 자동추격 발사체의 힘을 원래 발사체의 힘으로 수정
		
		// 발사체가 바라보는 방향 수정
		Vector Z_Axis = new Vector(0, 0, 1);
		Vector XZ_Vector = new Vector(0, 0, 0);
		XZ_Vector.setX(missileVector.getX());
		XZ_Vector.setZ(missileVector.getZ());
		float yaw = (float) (XZ_Vector.angle(Z_Axis) * (180/Math.PI));
		yaw *= (XZ_Vector.getX() > 0.0) ? -1.0 : 1.0;
		
		Vector Y_Axis = new Vector(0, 1, 0);
		float pitch = (float) (missileVector.angle(Y_Axis) * (180/Math.PI));
		pitch -= 90;
		
		prj.setRotation(yaw, pitch);
		
		// 발사체 중력 여부
		prj.setGravity(hasGravity);
		
		// 발사체 속도
		prj.setVelocity(missileVector);
		
		// 죽음의 추격 활성화 시, SQL에 해당 발사체 등록 및 쓰레드 실행
	}
}
