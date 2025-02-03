package org.vivecraft.entities;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;

public class CustomEndermanFreezeWhenLookedAt extends Goal {
    private final EnderMan enderman;
    private LivingEntity target;

    public CustomEndermanFreezeWhenLookedAt(EnderMan entityenderman) {
        this.enderman = entityenderman;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    public boolean canUse() {
        this.target = this.enderman.getTarget();
        if (!(this.target instanceof Player)) {
            return false;
        } else {
            double d0 = this.target.distanceToSqr(this.enderman);
            return d0 > 256.0 ? false : EndermanUtils.isLookingAtMe((Player)this.target, this.enderman);
        }
    }

    public void start() {
        this.enderman.getNavigation().stop();
    }

    public void tick() {
        this.enderman.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
    }
}