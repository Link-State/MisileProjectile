package main;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class MissileShoot implements Listener {

	/*
	 * targetPriority
	 * 0 - 가장 가까이 있는 대상 우선
	 * 1 - 가장 멀리 있는 대상 우선
	 * 2 - 조준선으로부터 가장 가까이 있는 대상 우선
	 * 3 - 조준선으로부터 가장 멀리 있는 대상 우선
	 * 4 - 랜덤
	 */
	
	@EventHandler
	public void launchedMissile(ProjectileLaunchEvent e) {
		Projectile prj = e.getEntity();
		
		// DB에서 발사자 정보 가져오기
		if (!(prj.getShooter() instanceof Player)) {
			return;
		}
		
		// 대상 우선순위
		// 발사체 중력여부
		// 죽음의추격 여부
		// 최소거리, 최대거리 가져오기
		// 인식범위, 최소각도, 최대각도 가져오기
		ProjectileSource shooter = prj.getShooter();
		Player p = (Player) shooter;
		
		ArrayList<Entity> entities = new ArrayList<Entity>(p.getNearbyEntities(23, 23, 23));
		
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
					living_entity.getEyeLocation().getX() - p.getEyeLocation().getX(),
					(living_entity.getLocation().getY() + (living_entity.getHeight() / 2.0)) - p.getEyeLocation().getY(),
					living_entity.getEyeLocation().getZ() - p.getEyeLocation().getZ());
			
			double angle = player2target_vec.angle(p.getLocation().getDirection()) * (180/Math.PI);
			
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
		System.out.println(p.getVelocity());
		
		System.out.println("--------------------------------------------");
		
		// 발사체의 방향을 수정
		missileVec = missileVec.add(target.getVelocity()); // 자동추격 대상의 속도를 적용
		missileVec = missileVec.normalize(); // 자동추격 발사체의 힘을 1로 수정
		missileVec = missileVec.multiply(prj.getVelocity().length() + 15); // 자동추격 발사체의 힘을 원래 발사체의 힘으로 수정
		
		
		Location direction = new Location(prj.getWorld(), 0, 0, 0);
		direction.add(missileVec);
		
		prj.setVelocity(missileVec);
		prj.setRotation(direction.getYaw(), direction.getPitch());
	}
}
