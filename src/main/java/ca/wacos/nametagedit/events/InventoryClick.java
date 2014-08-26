package ca.wacos.nametagedit.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import ca.wacos.nametagedit.NametagEdit;

public class InventoryClick implements Listener {

    private NametagEdit plugin = NametagEdit.getInstance();

    // Used in the GUI for group sorting
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getInventory().getName()
                .equalsIgnoreCase("§0NametagEdit Group Organizer")) {
            Player p = (Player) e.getWhoClicked();
            ItemStack item = e.getCurrentItem();
            if (item != null && item.hasItemMeta()
                    && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase("§aDone")) {
                    e.setCancelled(true);
                    p.sendMessage("[§6NametagEdit§f] §6- §fNew Group Order:");

                    List<String> temp = new ArrayList<>();

                    for (int a = 0; a <= 17; a++) {
                        ItemStack i = plugin.organizer.getItem(a);

                        if (i != null && i.getData().getData() == 0) {
                            String itemName = ChatColor.stripColor(i
                                    .getItemMeta().getDisplayName());
                            temp.add(itemName);
                            p.sendMessage("§6" + itemName);
                        }
                    }

                    plugin.getNTEHandler().allGroups = temp;

                    p.closeInventory();
                } else if (name.equalsIgnoreCase("§bNametagEdit Organizer")) {
                    e.setCancelled(true);
                }
            }
        }
    }
}