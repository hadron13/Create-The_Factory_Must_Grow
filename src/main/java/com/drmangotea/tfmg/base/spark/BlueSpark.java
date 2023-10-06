package com.drmangotea.tfmg.base.spark;


import com.drmangotea.tfmg.blocks.gadgets.explosives.thermite_grenades.fire.BlueFireBlock;
import com.drmangotea.tfmg.registry.TFMGEntityTypes;
import com.drmangotea.tfmg.registry.TFMGItems;
import com.simibubi.create.content.trains.CubeParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BlueSpark extends ThrowableProjectile {
    public BlueSpark(EntityType<? extends BlueSpark> p_37391_, Level p_37392_) {
        super(p_37391_, p_37392_);

    }
    public BlueSpark(Level p_37399_, LivingEntity p_37400_) {
        super(TFMGEntityTypes.SPARK.get(), p_37400_, p_37399_);
    }

    public BlueSpark(Level p_37394_, double p_37395_, double p_37396_, double p_37397_) {
        super(TFMGEntityTypes.BLUE_SPARK.get(), p_37395_, p_37396_, p_37397_, p_37394_);
    }


    @Override
    protected float getGravity(){
        return 0.02f;
    }
    @Override
    protected void defineSynchedData() {

    }

    public void tick(){
        super.tick();
        if (this.isInWaterOrRain()) {
            this.discard();
        }
        if(this.level.isClientSide) {

            CubeParticleData data =
                    new CubeParticleData(4.1f, 60.2f, 100.3f, .0125f + .0625f * random.nextFloat(), 30, false);
            level.addParticle(data, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05D, -this.getDeltaMovement().y * 0.5D, this.random.nextGaussian() * 0.05D);

        }
    }

    protected Item getDefaultItem() {
        return TFMGItems.THERMITE_GRENADE.get();
    }

    private ParticleOptions getParticle() {

        return ParticleTypes.FLAME;
    }

    public void handleEntityEvent(byte p_37402_) {
        if (p_37402_ == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for(int i = 0; i < 8; ++i) {
                this.level.addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }

    }
    protected void onHitBlock(BlockHitResult p_37384_) {
        super.onHitBlock(p_37384_);
        if (!this.level.isClientSide) {
            Entity entity = this.getOwner();
            if (!(entity instanceof Mob) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
                BlockPos blockpos = p_37384_.getBlockPos().relative(p_37384_.getDirection());
                if (this.level.isEmptyBlock(blockpos)) {
                    this.level.setBlockAndUpdate(blockpos, BlueFireBlock.getState(this.level, blockpos));
                }
            }

        }
    }

    protected void onHitEntity(EntityHitResult p_37386_) {
        super.onHitEntity(p_37386_);
        if (!this.level.isClientSide) {
            Entity entity = p_37386_.getEntity();
            Entity entity1 = this.getOwner();
            int i = entity.getRemainingFireTicks();
            entity.setSecondsOnFire(10);


        }
    }

    protected void onHit(HitResult p_37406_) {
        super.onHit(p_37406_);

        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)3);


            //this.level.explode(this, this.getX(), this.getY(0.0625D), this.getZ(), 2.0F, Explosion.BlockInteraction.NONE);
            this.discard();
        }

    }

    @SuppressWarnings("unchecked")
    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        EntityType.Builder<BlueSpark> entityBuilder = (EntityType.Builder<BlueSpark>) builder;
        return entityBuilder.sized(.25f, .25f);
    }
}