package me.robomwm.gpcustomlogger;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static org.bukkit.Bukkit.getServer;

public class CommandLogger implements Listener
{
    GriefPrevention gp = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");
    Main instance;
    CommandLogger(Main main)
    {
        instance = main; //YES I KNOW NEVER NAME MAIN CLASS MAIN I'M SCREAMING AT MY FORMER SELF
    }

    //Feature: log all slash commands GP doesn't log
    @EventHandler(priority = EventPriority.MONITOR)
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
    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (event.isCancelled())
        {
            gp.AddLogEntry("(Cancelled) " + event.getPlayer().getName() + ": " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
            instance.notifyServer(ChatColor.DARK_RED + "(canceled) <" + event.getPlayer().getName() + "> " + event.getMessage());
            return;
        }

        //Feature: log "spam-muted" chat messages

        //Is the player the only recipient? (Spam messages are only sent to recipient)
        if (event.getRecipients().size() == 1)
        {
            //Don't waste time figuring out if a player was softmuted if they're the only one on the server
            if (Bukkit.getOnlinePlayers().size() < 2)
                return;

            //Is the player already softmuted?
            if (gp.dataStore.isSoftMuted(event.getPlayer().getUniqueId()))
                return; //Already logged by GP
            else
            { //TODO: this doesn't account for the edge case that a player is /ignored by all other online players
                gp.AddLogEntry("(spam-muted) " + event.getPlayer().getName() + "> " + event.getMessage(), CustomLogEntryTypes.AdminActivity, true);
                instance.notifyServer(ChatColor.DARK_RED + "(spam) <" + event.getPlayer().getName() + "> " + event.getMessage());
            }
        }

        /**
         * Note if not all players hear this player (either due to /ignore or other means)
         */
        else if (event.getRecipients().size() < Bukkit.getOnlinePlayers().size())
        {
            if (!gp.dataStore.isSoftMuted(event.getPlayer().getUniqueId())) //GP logs softmuted players, ensure this isn't the case
            {
                gp.AddLogEntry("(semi-muted) " + event.getPlayer().getName() + "^^", CustomLogEntryTypes.AdminActivity, true);
                instance.notifyServer(ChatColor.GRAY + "(semi-muted) <" + event.getPlayer().getName() + "> " + event.getMessage());
            }
        }
    }

    //Shoulda just jammed this all in main. Oh well, too lazy to refactor
    //Feature: add note that sign editing was canceled
    @EventHandler(priority = EventPriority.MONITOR)
    void onSignEdit(SignChangeEvent event)
    {
        if (event.isCancelled())
            gp.AddLogEntry("Sign text was canceled", CustomLogEntryTypes.AdminActivity, true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onItemRename(InventoryClickEvent event)
    {
        Inventory inventory = event.getInventory();

        //We don't care if it isn't an anvil
        //Could also do event.getInventory().getType() != InventoryType.ANVIL
        //which would be more performant? Or it is just the same thing.
        if (!(inventory instanceof AnvilInventory))
            return;

        /* Would this be more performant? I doubt it but idk I'm new to the inventory side of things
        InventoryView view = event.getView();
        int rawSlot = event.getRawSlot();

        if (rawSlot != 2)
            return;
        if (rawSlot != view.convertSlot(rawSlot))
            return;
         */

        //Only care about the resulting item
        if (event.getSlotType() != InventoryType.SlotType.RESULT)
            return;

        ItemStack item = event.getCurrentItem();
        //nullcheck, I guess
        if (item == null)
            return;

        ItemMeta meta = item.getItemMeta();

        //Does item have metadata/a custom name?
        if (meta == null || !meta.hasDisplayName())
            return;

        gp.AddLogEntry(event.getWhoClicked().getName() + " Named a " + item.getType().name() + ": " + meta.getDisplayName(), CustomLogEntryTypes.AdminActivity, true);
        instance.notifyServer(event.getWhoClicked().getName() + " Named a " + item.getType().name() + ": " + meta.getDisplayName());
    }
}
