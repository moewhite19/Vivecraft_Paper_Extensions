package org.vivecraft.entities;

import org.bukkit.Location;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EndermanUtils {
    //Vivecraft copy and modify from EnderMan
    public static boolean isLookingAtMe(Player pPlayer, EnderMan enderman)
    {
        ItemStack itemstack = pPlayer.getInventory().armor.get(3);

        if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem()))
        {
            return false;
        }
        else
        {
            Vec3 vec3 = pPlayer.getViewVector(1.0F).normalize();
            Vec3 vec31 = new Vec3(enderman.getX() - pPlayer.getX(), enderman.getEyeY() - pPlayer.getEyeY(), enderman.getZ() - pPlayer.getZ());
            //VSE MODIFICATION
            boolean vr = pPlayer instanceof Player && VSE.isVive((org.bukkit.entity.Player) pPlayer.getBukkitEntity());
            VivePlayer vp = null;
            Vec3 hmdpos = null;
            double f=0.025;
            if(vr){
                vp = VSE.vivePlayers.get(pPlayer.getBukkitEntity().getUniqueId());
                vec3 = vp.getHMDDir();
                Location h = vp.getHMDPos();
                hmdpos = new Vec3(h.getX(), h.getY(), h.getZ());
                vec31= new Vec3(enderman.getX() - hmdpos.x, enderman.getEyeY() - hmdpos.y, enderman.getZ() - hmdpos.z);
                f=0.1;
            }
            ////
            double d0 = vec31.length();
            vec31 = vec31.normalize();
            double d1 = vec3.dot(vec31);
            //VSE MODIFICATION
            if(!(d1 > 1.0D - f / d0)) return false;
            if(vr)
                return hasLineOfSight(hmdpos, enderman);
            else
                return pPlayer.hasLineOfSight(enderman);
            //
        }
    }

    //Vivecraft copy and modify from LivingEntity
    public static boolean hasLineOfSight(Vec3 source, Entity entity)
    {
        Vec3 vec31 = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());

        if (vec31.distanceTo(source) > 128.0D)
        {
            return false;
        }
        else
        {
            return entity.level().clip(new ClipContext(source, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
        }
    }
}