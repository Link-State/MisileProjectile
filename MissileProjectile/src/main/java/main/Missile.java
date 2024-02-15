package main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class Missile implements Listener {
	
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
		String name = shooter.getName();
		if (!(shooter instanceof Player)) {
			name = name.toUpperCase();
		}
        dataMap.put("name"   , name);
        
        // 해당 발사체 객체의 클래스 탐색
		String prjType = null;
        for (Class<?> cls : Main.PROJECTILES) {
        	if (cls.isInstance(prj)) {
        		prjType = cls.getSimpleName().toUpperCase();
        		break;
        	}
        }
        
        // 발사체 클래스를 찾을 수 없으면 종료
        if (prjType == null) {
        	return;
        }
        
        // 데이터 조회
        DQLService DQL = new DQLService(Main.DB_URL);
        List<Map<String, Object>> result = DQL.selectProjectile(prjType, dataMap);
        
        // DB 생성
        if (result.size() <= 0) {
        	return;
        }
		
		// 해당 발사체 유도 활성화 검사
        Map<String, Object> projectile_data = result.get(0);
        boolean isEnable = (((int) projectile_data.get("isEnable")) != 0);
        boolean hasGravity = (((int) projectile_data.get("hasGravity")) != 0);
        boolean isTrace = (((int) projectile_data.get("isTrace")) != 0);
        int traceLife = (int) projectile_data.get("traceLife");
        int targetPriority = (int) projectile_data.get("targetPriority");
        double minDistance = (double) projectile_data.get("minDistance");
        double maxDistance = (double) projectile_data.get("maxDistance");
        double recog_X_Range = (double) projectile_data.get("recog_X_Range");
        double recog_Y_Range = (double) projectile_data.get("recog_Y_Range");
        double recog_Z_Range = (double) projectile_data.get("recog_Z_Range");
        double minAngle = (double) projectile_data.get("minAngle");
        double maxAngle = (double) projectile_data.get("maxAngle");
        
        // 발사체유도 기능이 꺼져있으면
        if (!isEnable) {
        	return;
        }
		
		int standardUnit = 0; // 0 = 거리, 1 = 각도
		int sort = 1; // 1 = 가장 가까운 대상, -1 = 가장 먼 대상
		double minValue = Math.pow(minDistance, 2);
		double maxValue = Math.pow(maxDistance, 2);
		double localExtremumValue = maxAngle + maxValue + 1.0;
		
		// 우선순위 대상 - 조준선 설정
		if (targetPriority == 2 || targetPriority == 3 || targetPriority == 4) {
			standardUnit = 1;
			minValue = minAngle;
			maxValue = maxAngle;
		}
		
		// 우선순위 대상 - 거리 설정
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
		
		// 지정 범위 내 엔티티가 없으면 종료
		if (entities.size() <= 0) {
			return;
		}
		
		LivingEntity missileTarget = null; // 발사체 유도 대상
		Vector missileVector = null; // 발사체 유도 벡터
		
		ArrayList<LivingEntity> living_entities = new ArrayList<LivingEntity>();
		
		// 범위 내 모든 엔티티 순회
		for (Entity entity : entities) {
			// 살아있는 엔티티가 아니면 스킵
			if (!(entity instanceof LivingEntity)) {
				continue;
			}
			LivingEntity living_entity = (LivingEntity) entity;
			
			if (!living_entity.hasLineOfSight(shooter)) {
				continue;
			}
			
			// 타겟을 랜덤으로 정한 경우
			if (targetPriority == 4) {
				// 살아있는 엔티티만 저장
				living_entities.add(living_entity);
			}
			
			// 몹의 위치에서 내 위치를 빼서 두 위치 간 거리벡터 생성
			Vector shooter2target = distanceVector(living_entity, shooter);
			
			// 거리 또는 각도를 계산
			double distance_or_angle = 0.0;
			if (standardUnit == 0) {
				distance_or_angle = shooter2target.lengthSquared();
			}
			else if (standardUnit == 1) {
				distance_or_angle = shooter2target.angle(shooter.getLocation().getDirection()) * (180/Math.PI);
			}
			
			// 최소거리, 최소각도, 최대거리, 최대각도 조건 확인
			if (minValue > distance_or_angle || distance_or_angle > maxValue) {
				continue;
			}
			
			// 극값(가장 멀리있거나 가장 가까이 있거나) 탐색
			if (localExtremumValue > sort * distance_or_angle) {
				localExtremumValue = sort * distance_or_angle;
				missileTarget = living_entity;
				missileVector = new Vector();
				missileVector.setX(shooter2target.getX());
				missileVector.setY(shooter2target.getY());
				missileVector.setZ(shooter2target.getZ());
			}
		}
		
		// 타겟이 랜덤일 경우
		if (targetPriority == 4) {
			// 살아있는 엔티티 중 랜덤으로 선택
			if (living_entities.size() <= 0) {
				return;
			}
			int randIdx = (int) (Math.random() * living_entities.size());
			missileTarget = living_entities.get(randIdx);
			missileVector = distanceVector(missileTarget, shooter);
		}
		
		// 찾을 수 없으면 종료
		if (missileTarget == null) {
			return;
		}
		
		
		// 발사체 힘 수정
		missileVector = missileVector.normalize(); // 자동추격 발사체의 힘을 1로 수정
		missileVector = missileVector.multiply(prj.getVelocity().length()); // 자동추격 발사체의 힘을 원래 발사체의 힘으로 수정

		// 발사체가 바라봐야할 방향 계산
		Vector Y_Axis = new Vector(0, 1, 0);
		Vector Z_Axis = new Vector(0, 0, 1);
		Vector XZ_Vector = new Vector(0, 0, 0);
		XZ_Vector.setX(missileVector.getX());
		XZ_Vector.setZ(missileVector.getZ());
		float yaw = (float) (XZ_Vector.angle(Z_Axis) * (180/Math.PI));
		float pitch = (float) (missileVector.angle(Y_Axis) * (180/Math.PI));
		yaw *= (XZ_Vector.getX() > 0.0) ? -1.0 : 1.0; // x방향으로 양수 값일 때 yaw는 음수
		pitch -= 90;

		// 발사체가 바라보는 방향 수정
		prj.setRotation(-yaw, -pitch);
		
		// 발사체 중력 여부
		prj.setGravity(hasGravity);
		
		// 발사체 속도
		prj.setVelocity(missileVector);
		
		// 죽음의 추격
		if (isTrace) {
			Runnable r1 = new TraceProjectile(prj, missileTarget, traceLife);
			Thread t1 = new Thread(r1);
			
			// SQL에 발사체 UUID : 쓰레드 ID
			dataMap.clear();
			dataMap.put("uuid", prj.getUniqueId().toString());
			dataMap.put("target_uuid", missileTarget.getUniqueId().toString());
			dataMap.put("threadID", t1.getId());

	        List<Map<String, Object>> resultList = DQL.selectAliveProjectile(dataMap);
	        
	        // 해당 UUID가 이미 존재하면 종료
	        if (resultList.size() > 0) {
	        	return;
	        }
	        
	        // SQL에 해당 UUID:threadID 저장
			try {
	            DMLService DML = new DMLService(Main.DB_URL);
				DML.insertAliveProjectile(dataMap);
			} catch (SQLException e1) {
				e1.printStackTrace();
				return;
			}
			
			t1.start();
		}
	}
	
	
	@EventHandler
	public void hitMissile(ProjectileHitEvent e) {
		Projectile prj = e.getEntity();
		
		if (e.getHitEntity() == null) {
			return;
		}
		
		// 타격한 발사체가 sql에 존재하는지 검사
		final Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("uuid", prj.getUniqueId().toString());

        DQLService DQL = new DQLService(Main.DB_URL);
        List<Map<String, Object>> result = DQL.selectAliveProjectile(dataMap);
        
        if (result.size() <= 0) {
        	return;
        }
		
        Map<String, Object> prj_record = result.get(0);
        String target_uuid = (String) prj_record.get("target_uuid");
        
        // 타겟의 uuid와 적중한 엔티티의 uuid가 일치하지 않으면
        if (!target_uuid.equals(e.getHitEntity().getUniqueId().toString())) {
        	return;
        }
        
		long threadID = Long.parseLong(prj_record.get("threadID").toString());
		Thread thread = null;
		
		// 쓰레드ID로 해당 쓰레드 탐색
		ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
		ThreadGroup parentGroup;
		while ((parentGroup = rootGroup.getParent()) != null) {
			rootGroup = parentGroup;
		}
		
		Thread[] threads = new Thread[rootGroup.activeCount()];
		
		while (rootGroup.enumerate(threads, true) == threads.length) {
			threads = new Thread[threads.length * 2];
		}
		
		for (Thread t : threads) {
			if (t.getId() == threadID) {
				thread = t;
				break;
			}
		}
		
		// 해당 쓰레드를 찾지 못했으면 종료
		if (thread == null) {
			// 해당 레코드 삭제
			try {
	            DMLService DML = new DMLService(Main.DB_URL);
				DML.deleteAliveProjectile(dataMap);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			return;
		}
		
//		thread.interrupt();
	}
	
	
	// 두 엔티티 위치를 이용하여 발사자->대상 벡터 구하는 함수
	public Vector distanceVector(LivingEntity target, LivingEntity shooter) {
		Vector shooter2target = new Vector(
				target.getEyeLocation().getX() - shooter.getEyeLocation().getX(),
				(target.getLocation().getY() + (target.getHeight() / 2.0)) - shooter.getEyeLocation().getY(),
				target.getEyeLocation().getZ() - shooter.getEyeLocation().getZ());
		return shooter2target;
	}
}
