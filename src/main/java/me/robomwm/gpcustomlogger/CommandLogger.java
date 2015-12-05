package me.robomwm.gpcustomlogger;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import static org.bukkit.Bukkit.getServer;

public class CommandLogger implements Listener
{
    GriefPrevention gp = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");

    //Feature: log all slash commands GP doesn't log
    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (event.isCancelled())
        {
                gp.AddLogEntry("(Cancelled) " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
        }
        else
        {
            int cmdIndex = event.getMessage().indexOf(' ');
            //Don't log whispers (already logged by GP)
            if ((gp.config_eavesdrop_whisperCommands.contains(event.getMessage().substring(0, cmdIndex))))
                return;
            //Don't log monitored slash commands (already logged by GP)
            else if ((gp.config_spam_monitorSlashCommands.contains(event.getMessage().substring(0, cmdIndex))))
                return;
            else
                gp.AddLogEntry(event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
        }
    }

    //Feature: log all cancelled chat messages
    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.isCancelled())
        {
            gp.AddLogEntry("(Cancelled) " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
        }
    }
}
