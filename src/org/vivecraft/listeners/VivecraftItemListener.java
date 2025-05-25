package org.vivecraft.listeners;

import cn.handyplus.lib.adapter.FoliaScheduler;
import cn.handyplus.lib.adapter.HandySchedulerUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.Vector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.spigot.network.BodyPart;

public class VivecraftItemListener implements Listener {
    VSE vse;

    public VivecraftItemListener(VSE vse) {
        this.vse = vse;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        if (!VSE.isVive(player))
            return;

        VivePlayer vp = VSE.vivePlayers.get(player.getUniqueId());

        if (vp == null) return;

        float f2 = 0.3F;

        final Item dropItem = event.getItemDrop();
        if (dropItem.getType() == EntityType.ITEM){
            Vector v = new Vector();
            float yaw = player.getLocation().getYaw();
            float pitch = -player.getLocation().getPitch();
            v.setX((double) (-Mth.sin(yaw * 0.017453292F) * Mth.cos(player.getLocation().getPitch() * 0.017453292F) * f2));
            v.setZ((double) (Mth.cos(yaw * 0.017453292F) * Mth.cos(player.getLocation().getPitch() * 0.017453292F) * f2));
            v.setY((double) (Mth.sin(pitch * 0.017453292F) * f2 + 0.1F));

            Vec3 aim = vp.getControllerDir(BodyPart.MAIN_HAND);
            Runnable runnable = () -> {
                dropItem.teleport(vp.getControllerPos(BodyPart.MAIN_HAND).add(0.2f * aim.x,0.25f * aim.y - 0.2f,0.2f * aim.z));
                dropItem.setVelocity(v);
            };
            if (HandySchedulerUtil.isFolia()){
                dropItem.getScheduler().execute(vse,runnable,null,0);
            } else {
                runnable.run();
            }
        }
    }
}
