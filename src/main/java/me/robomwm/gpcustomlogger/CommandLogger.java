package me.robomwm.gpcustomlogger;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
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
            if (cmdIndex < 0) //if command has no arguments, log
            {
                gp.AddLogEntry(event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
                return;
            }
            //Don't log whispers (already logged by GP)
            if ((gp.config_eavesdrop_whisperCommands.contains(event.getMessage().substring(0, cmdIndex))))
                return;
            //Don't log monitored slash commands (already logged by GP)
            else if ((gp.config_spam_monitorSlashCommands.contains(event.getMessage().substring(0, cmdIndex))))
                return;
            else
            {
                gp.AddLogEntry(event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
            }
        }
    }

    //Feature: log all cancelled chat messages
    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.isCancelled())
        {
            gp.AddLogEntry("(Cancelled) " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
            return;
        }
        
        //Feature: log "spam-muted" chat messages
        //Don't waste time figuring out if a player was softmuted if they're the only one on the server
        if (Bukkit.getOnlinePlayers().size() < 2)
            return;

        //Is the player the only recipient?
        if (event.getRecipients().size() == 1)
        {
            //Is the player already softmuted? GP logs softmuted messages for us already
            if (!gp.dataStore.isSoftMuted(event.getPlayer().getUniqueId()))
            {
                gp.AddLogEntry("(spam-muted) " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
                gp.AddLogEntry("Players online: " + Bukkit.getOnlinePlayers().size(), CustomLogEntryTypes.AdminActivity, true);
            }
        }
    }
}
