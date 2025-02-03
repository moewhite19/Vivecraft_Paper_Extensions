package org.vivecraft.entities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public class CustomEndermanLookForPlayerGoal extends NearestAttackableTargetGoal<Player> {
    private final EnderMan enderman;
    private Player pendingTarget;
    private int aggroTime;
    private int teleportTime;
    private final Predicate<Entity> startAggroTargetConditions;
    private final TargetingConditions continueAggroTargetConditions = TargetingConditions.forCombat().ignoreLineOfSight();
    private final TargetingConditions.Selector isAngerInducing;

    public CustomEndermanLookForPlayerGoal(EnderMan entityenderman,TargetingConditions.Selector p) {
        super(entityenderman,Player.class,10,false,false,p);
        this.enderman = entityenderman;
        this.isAngerInducing = (entityliving,serverLevel) -> {
            final LivingEntity living = entityliving;
            return (EndermanUtils.isLookingAtMe((Player) entityliving,this.enderman) || entityenderman.isAngryAt(living,serverLevel)) && !entityenderman.hasIndirectPassenger(entityliving);
        };
//        this.startAggroTargetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this.isAngerInducing);
        this.startAggroTargetConditions = new Predicate<Entity>() {
            @Override
            public boolean test(Entity entity) {
                if (entity instanceof EnderMan enderMan){
                    return isAngerInducing.test(enderMan,enderMan.level().getMinecraftWorld());
                }
                return false;
            }
        };
    }

    public boolean canUse() {
//        this.pendingTarget = this.enderman.level().getNearestPlayer(this.startAggroTargetConditions,this.enderman);
        this.pendingTarget = this.enderman.level().findNearbyPlayer(this.enderman,this.getFollowDistance(),this.startAggroTargetConditions);
        return this.pendingTarget != null;
    }

    public void start() {
        this.aggroTime = this.adjustedTickDelay(5);
        this.teleportTime = 0;
        this.enderman.setBeingStaredAt();
    }

    public void stop() {
        this.pendingTarget = null;
        super.stop();
    }

    public boolean canContinueToUse() {
        if (this.pendingTarget != null){
            if (!this.isAngerInducing.test(this.pendingTarget,this.pendingTarget.level().getMinecraftWorld())){
                return false;
            } else {
                this.enderman.lookAt(this.pendingTarget,10.0F,10.0F);
                return true;
            }
        } else {
            if (this.target != null){
                if (this.enderman.hasIndirectPassenger(this.target)){
                    return false;
                }

                if (this.continueAggroTargetConditions.test(this.enderman.level().getMinecraftWorld(),this.enderman,this.target)){
                    return true;
                }
            }

            return super.canContinueToUse();
        }
    }

    public void tick() {
        if (this.enderman.getTarget() == null){
            super.setTarget((LivingEntity) null);
        }

        if (this.pendingTarget != null){
            if (--this.aggroTime <= 0){
                this.target = this.pendingTarget;
                this.pendingTarget = null;
                super.start();
            }
        } else {
            if (this.target != null && !this.enderman.isPassenger()){
                if (EndermanUtils.isLookingAtMe((Player) this.target,this.enderman)){
                    if (this.target.distanceToSqr(this.enderman) < 16.0){
                        this.enderman.teleport();
                    }

                    this.teleportTime = 0;
                } else if (this.target.distanceToSqr(this.enderman) > 256.0 && this.teleportTime++ >= this.adjustedTickDelay(30) && this.enderman.teleportTowards(this.target)){
                    this.teleportTime = 0;
                }
            }

            super.tick();
        }

    }

}