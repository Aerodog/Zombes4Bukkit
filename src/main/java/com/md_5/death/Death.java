package com.md_5.death;

import com.md_5.zmod.BaseMod;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Death extends BaseMod implements Listener {

    private final Map<String, PlayerInventory> inventories = new HashMap<String, PlayerInventory>();
    private final Map<String, Integer> exp = new HashMap<String, Integer>();
    private boolean dropInv, loseExp;
    private int penalty;

    public Death() {
        super("Death");
    }

    @Override
    public void enable() {
        final FileConfiguration conf = getConfig();
        dropInv = conf.getBoolean("death.dropInv");
        loseExp = conf.getBoolean("death.loseExp");
        penalty = conf.getInt("death.healthPenalty");
        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event instanceof PlayerDeathEvent) {
            final Player player = (Player) event.getEntity();
            if (!dropInv && player.hasPermission(Permissions.keepInventory)) {
                event.getDrops().clear();
                inventories.put(player.getName(), player.getInventory());
            }
            if (loseExp && !player.hasPermission(Permissions.keepExp)) {
                event.setDroppedExp(0);
                exp.put(player.getName(), player.getTotalExperience());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        if (!dropInv && player.hasPermission(Permissions.keepInventory)) {
            final PlayerInventory inventory = inventories.get(player.getName());
            if (inventory != null) {
                for (int i = 0; i <= inventory.getSize(); i++) {
                    final ItemStack stack = inventory.getItem(i);
                    if (stack.getType() == Material.AIR) {
                        player.getInventory().setItem(i, null);
                    } else {
                        player.getInventory().setItem(i, stack);
                    }
                }
                player.sendMessage(ChatColor.GREEN + "Your inventory has been restored");
            }
        }
        if (!player.hasPermission(Permissions.penaltyExempt) && penalty != 0) {
            player.sendMessage(ChatColor.RED + "You have had a penalty of " + penalty + " hp applied.");
            player.damage(penalty);
        }
    }
}
