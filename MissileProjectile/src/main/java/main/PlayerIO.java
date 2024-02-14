package main;

import java.sql.SQLException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerIO implements Listener{
	
	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		
		Player p = e.getPlayer();
		String name = p.getName();
		String uuid = p.getUniqueId().toString().toUpperCase();
		
		Main main = new Main();
		try {
			main.insertInitShooter(name, uuid, 0);
			
			for (Class<?> prj_cls : Main.PROJECTILES) {
				String prj = prj_cls.getSimpleName().toUpperCase();
				main.insertInitProjectile(name, prj);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
