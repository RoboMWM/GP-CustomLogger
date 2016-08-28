package me.robomwm.gpcustomlogger;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.Bukkit.getServer;


public class Main extends JavaPlugin
{
    GriefPrevention gp;
    DataStore ds;
    World world;
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new CommandLogger(), this);
        gp = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");
        ds = gp.dataStore;
        world = Bukkit.getWorld("world");
        new BukkitRunnable()
        {
            public void run()
            {
                afkChecker();
            }
        }.runTaskTimer(this, 6000L, 6000L);
    }

    void afkChecker()
    {
        PlayerData playerData;
        for (Player player : Bukkit.getOnlinePlayers())
        {
            playerData = ds.getPlayerData(player.getUniqueId());
            //Hmm, would be nice if he had an API method to determine if a player is AFK.
            Location lastLocation = playerData.lastAfkCheckLocation;
            if(!player.isInsideVehicle() && (lastLocation == null || lastLocation.distanceSquared(player.getLocation()) >= 0) && !player.getLocation().getBlock().isLiquid())
            {
                if (!player.isOp() && player.getWorld().equals(world))
                {
                    player.setSleepingIgnored(false);
                    Bukkit.broadcast(ChatColor.DARK_RED + player.getName() + " is NOT afk and is in overworld", "topkek");
                }
                Bukkit.broadcast(ChatColor.DARK_RED + player.getName() + " isSleepIgnored = " + String.valueOf(player.isSleepingIgnored()) , "topkek");
                continue;
            }
            player.setSleepingIgnored(true);
            Bukkit.broadcast(ChatColor.DARK_RED + player.getName() + " is afk", "topkek");
        }
    }
}
