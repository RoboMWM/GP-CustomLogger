package me.robomwm.gpcustomlogger;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin
{
    GriefPrevention gp;
    DataStore ds;
    World world;

    /** A thread-safe way of notifying staff */
    Player oppedPlayer;
    @EventHandler
    void onOppedPlayerJoin(PlayerJoinEvent event)
    {
        if (event.getPlayer().isOp())
            oppedPlayer = event.getPlayer();
    }
    @EventHandler
    void onOppedPlayerQuit(PlayerQuitEvent event)
    {
        if (oppedPlayer != null && oppedPlayer == event.getPlayer())
            oppedPlayer = null;
    }
    void notifyServer(String message)
    {
        this.getLogger().info(message);
        if (oppedPlayer != null)
            oppedPlayer.sendMessage(message);
    }
    /** End of a thread-safe way of notifying staff */

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(new CommandLogger(this), this);
        gp = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");
        ds = gp.dataStore;
        world = Bukkit.getWorld("world");
        new BukkitRunnable()
        {
            public void run()
            {
                afkChecker();
            }
        }.runTaskTimer(this, 12000L, 12000L);
    }

    void afkChecker()
    {
        StringBuilder afkPlayers = new StringBuilder();
        PlayerData playerData;
        Location lastLocation;
        //No need to check if only one player is online
        if (Bukkit.getOnlinePlayers().size() < 2)
            return;

        for (Player player : Bukkit.getOnlinePlayers())
        {
            playerData = ds.getPlayerData(player.getUniqueId());
            //Hmm, would be nice if he had an API method to determine if a player is AFK.
            lastLocation = playerData.lastAfkCheckLocation;
            try
            {
                if(!player.isInsideVehicle() && (lastLocation == null || lastLocation.distanceSquared(player.getLocation()) >= 0) && !player.getLocation().getBlock().isLiquid())
                { //Player is NOT afk
                    if (!player.isOp())
                        player.setSleepingIgnored(false);
                }
                else
                { //Otherwise, is afk
                    player.setSleepingIgnored(true);
                    afkPlayers.append(player.getName());
                    afkPlayers.append(lastLocation.toString());
                    afkPlayers.append(", ");
                }
            }
            catch(IllegalArgumentException e)
            { //Occurs when distanceSquared function evaluates two locations in different worlds.
            }
        } //end foreach loop
        notifyServer(ChatColor.GOLD + "AFK: " + afkPlayers.toString());
    }
}
