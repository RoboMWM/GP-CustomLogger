package me.robomwm.gpcustomlogger;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bukkit.Bukkit.getServer;

public class CommandLogger implements Listener
{
    GriefPrevention gp = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm");

    //Feature: log all slash commands GP doesn't log
    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {

        if (event.isCancelled())
        {
            String timestamp = this.timestampFormat.format(new Date());
            gp.AddLogEntry(timestamp + " (Cancelled) " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
        }
        else
        {
            int cmdIndex = event.getMessage().indexOf(' ');
            if (cmdIndex < 0) //if command has no arguments, log
            {
                String timestamp = this.timestampFormat.format(new Date());
                gp.AddLogEntry(timestamp + " " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
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
                String timestamp = this.timestampFormat.format(new Date());
                gp.AddLogEntry(timestamp + " " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
            }
        }
    }

    //Feature: log all cancelled chat messages
    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.isCancelled())
        {
            String timestamp = this.timestampFormat.format(new Date());
            gp.AddLogEntry(timestamp + " (Cancelled) " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
        }
    }
}
