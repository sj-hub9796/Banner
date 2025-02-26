package com.mohistmc.banner.mixin.world.entity.vehicle;

import com.mohistmc.banner.injection.world.entity.vehicle.InjectionAbstractMinecart;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractMinecart.class)
public abstract class MixinAbstractMinecart extends Entity implements InjectionAbstractMinecart {

    // @formatter:off
    @Shadow public abstract void setHurtDir(int rollingDirection);
    @Shadow public abstract int getHurtDir();
    @Shadow public abstract void setHurtTime(int rollingAmplitude);
    @Shadow public abstract void setDamage(float damage);
    @Shadow public abstract float getDamage();
    @Shadow public abstract void destroy(DamageSource source);
    @Shadow public abstract int getHurtTime();
    @Shadow private int lSteps;
    @Shadow private double lx;
    @Shadow private double ly;
    @Shadow private double lz;
    @Shadow private double lyr;
    @Shadow private double lxr;
    @Shadow protected abstract void moveAlongTrack(BlockPos pos, BlockState state);
    @Shadow public abstract void activateMinecart(int x, int y, int z, boolean receivingPower);
    @Shadow private boolean flipped;
    @Shadow public abstract AbstractMinecart.Type getMinecartType();
    // @formatter:on

    @Shadow private boolean onRails;
    public boolean slowWhenEmpty = true;
    private double derailedX = 0.5;
    private double derailedY = 0.5;
    private double derailedZ = 0.5;
    private double flyingX = 0.95;
    private double flyingY = 0.95;
    private double flyingZ = 0.95;
    public double maxSpeed = 0.4D;

    public MixinAbstractMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }


    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void banner$init(EntityType<?> type, Level worldIn, CallbackInfo ci) {
        slowWhenEmpty = true;
        derailedX = 0.5;
        derailedY = 0.5;
        derailedZ = 0.5;
        flyingX = 0.95;
        flyingY = 0.95;
        flyingZ = 0.95;
        maxSpeed = 0.4D;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public boolean hurt(DamageSource damagesource, float f) {
        if (!this.level().isClientSide && !this.isRemoved()) {
            if (this.isInvulnerableTo(damagesource)) {
                return false;
            } else {
                // CraftBukkit start - fire VehicleDamageEvent
                Vehicle vehicle = (Vehicle) this.getBukkitEntity();
                org.bukkit.entity.Entity passenger = (damagesource.getEntity() == null) ? null : damagesource.getEntity().getBukkitEntity();

                VehicleDamageEvent event = new VehicleDamageEvent(vehicle, passenger, f);
                this.level().getCraftServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return false;
                }

                f = (float) event.getDamage();
                // CraftBukkit end
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.markHurt();
                this.setDamage(this.getDamage() + f * 10.0F);
                this.gameEvent(GameEvent.ENTITY_DAMAGE, damagesource.getEntity());
                boolean flag = damagesource.getEntity() instanceof Player && ((Player) damagesource.getEntity()).getAbilities().instabuild;

                if (flag || this.getDamage() > 40.0F) {
                    // CraftBukkit start
                    VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, passenger);
                    this.level().getCraftServer().getPluginManager().callEvent(destroyEvent);

                    if (destroyEvent.isCancelled()) {
                        this.setDamage(40); // Maximize damage so this doesn't get triggered again right away
                        return true;
                    }
                    // CraftBukkit end
                    this.ejectPassengers();
                    if (flag && !this.hasCustomName()) {
                        this.discard();
                    } else {
                        this.destroy(damagesource);
                    }
                }

                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    public void tick() {
        // CraftBukkit start
        double prevX = this.getX();
        double prevY = this.getY();
        double prevZ = this.getZ();
        float prevYaw = this.getYRot();
        float prevPitch = this.getXRot();
        // CraftBukkit end

        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        this.checkBelowWorld();
        // this.handleNetherPortal(); // CraftBukkit - handled in postTick
        double d0;

        if (this.level().isClientSide) {
            if (this.lSteps > 0) {
                d0 = this.getX() + (this.lx - this.getX()) / (double) this.lSteps;
                double d1 = this.getY() + (this.ly - this.getY()) / (double) this.lSteps;
                double d2 = this.getZ() + (this.lz - this.getZ()) / (double) this.lSteps;
                double d3 = Mth.wrapDegrees(this.lyr - (double) this.getYRot());

                this.setYRot(this.getYRot() + (float) d3 / (float) this.lSteps);
                this.setXRot(this.getXRot() + (float) (this.lxr - (double) this.getXRot()) / (float) this.lSteps);
                --this.lSteps;
                this.setPos(d0, d1, d2);
                this.setRot(this.getYRot(), this.getXRot());
            } else {
                this.reapplyPosition();
                this.setRot(this.getYRot(), this.getXRot());
            }

        } else {
            if (!this.isNoGravity()) {
                d0 = this.isInWater() ? -0.005D : -0.04D;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d0, 0.0D));
            }

            int i = Mth.floor(this.getX());
            int j = Mth.floor(this.getY());
            int k = Mth.floor(this.getZ());

            if (this.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
                --j;
            }

            BlockPos blockposition = new BlockPos(i, j, k);
            BlockState iblockdata = this.level().getBlockState(blockposition);

            this.onRails = BaseRailBlock.isRail(iblockdata);
            if (this.onRails) {
                this.moveAlongTrack(blockposition, iblockdata);
                if (iblockdata.is(Blocks.ACTIVATOR_RAIL)) {
                    this.activateMinecart(i, j, k, (Boolean) iblockdata.getValue(PoweredRailBlock.POWERED));
                }
            } else {
                this.comeOffTrack();
            }

            this.checkInsideBlocks();
            this.setXRot(0.0F);
            double d4 = this.xo - this.getX();
            double d5 = this.zo - this.getZ();

            if (d4 * d4 + d5 * d5 > 0.001D) {
                this.setYRot((float) (Mth.atan2(d5, d4) * 180.0D / 3.141592653589793D));
                if (this.flipped) {
                    this.setYRot(this.getYRot() + 180.0F);
                }
            }

            double d6 = (double) Mth.wrapDegrees(this.getYRot() - this.yRotO);

            if (d6 < -170.0D || d6 >= 170.0D) {
                this.setYRot(this.getYRot() + 180.0F);
                this.flipped = !this.flipped;
            }

            this.setRot(this.getYRot(), this.getXRot());
            // CraftBukkit start
            org.bukkit.World bworld = this.level().getWorld();
            Location from = new Location(bworld, prevX, prevY, prevZ, prevYaw, prevPitch);
            Location to = CraftLocation.toBukkit(this.position(), bworld, this.getYRot(), this.getXRot());
            Vehicle vehicle = (Vehicle) this.getBukkitEntity();

            this.level().getCraftServer().getPluginManager().callEvent(new org.bukkit.event.vehicle.VehicleUpdateEvent(vehicle));

            if (!from.equals(to)) {
                this.level().getCraftServer().getPluginManager().callEvent(new org.bukkit.event.vehicle.VehicleMoveEvent(vehicle, from, to));
            }
            // CraftBukkit end
            if (this.getMinecartType() == AbstractMinecart.Type.RIDEABLE && this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
                List<Entity> list = this.level().getEntities((Entity) this, this.getBoundingBox().inflate(0.20000000298023224D, 0.0D, 0.20000000298023224D), EntitySelector.pushableBy(this));

                if (!list.isEmpty()) {
                    for (Entity value : list) {
                        Entity entity = (Entity) value;

                        if (!(entity instanceof Player) && !(entity instanceof IronGolem) && !(entity instanceof AbstractMinecart) && !this.isVehicle() && !entity.isPassenger()) {
                            // CraftBukkit start
                            VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity.getBukkitEntity());
                            this.level().getCraftServer().getPluginManager().callEvent(collisionEvent);

                            if (collisionEvent.isCancelled()) {
                                continue;
                            }
                            // CraftBukkit end
                            entity.startRiding(this);
                        } else {
                            // CraftBukkit start
                            if (!this.isPassengerOfSameVehicle(entity)) {
                                VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity.getBukkitEntity());
                                this.level().getCraftServer().getPluginManager().callEvent(collisionEvent);

                                if (collisionEvent.isCancelled()) {
                                    continue;
                                }
                            }
                            // CraftBukkit end
                            entity.push(this);
                        }
                    }
                }
            } else {

                for (Entity entity1 : this.level().getEntities(this, this.getBoundingBox().inflate(0.20000000298023224D, 0.0D, 0.20000000298023224D))) {
                    if (!this.hasPassenger(entity1) && entity1.isPushable() && entity1 instanceof AbstractMinecart) {
                        // CraftBukkit start
                        VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity1.getBukkitEntity());
                        this.level().getCraftServer().getPluginManager().callEvent(collisionEvent);

                        if (collisionEvent.isCancelled()) {
                            continue;
                        }
                        // CraftBukkit end
                        entity1.push(this);
                    }
                }
            }

            this.updateInWaterStateAndDoFluidPushing();
            if (this.isInLava()) {
                this.lavaHurt();
                this.fallDistance *= 0.5F;
            }

            this.firstTick = false;
        }
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * @author wdog5
     * @reason
     */
    @Overwrite
    protected void comeOffTrack() {
        double d0 = this.getMaxSpeed();
        Vec3 vec3d = this.getDeltaMovement();

        this.setDeltaMovement(Mth.clamp(vec3d.x, -d0, d0), vec3d.y, Mth.clamp(vec3d.z, -d0, d0));
        if (this.onGround()) {
            // CraftBukkit start - replace magic numbers with our variables
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x * this.derailedX, this.getDeltaMovement().y * this.derailedY, this.getDeltaMovement().z * this.derailedZ));
            // CraftBukkit end
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround()) {
            // CraftBukkit start - replace magic numbers with our variables
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x * this.flyingX, this.getDeltaMovement().y * this.flyingY, this.getDeltaMovement().z * this.flyingZ));
            // CraftBukkit end
        }
    }

    @Redirect(method = "applyNaturalSlowdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;isVehicle()Z"))
    private boolean banner$slowWhenEmpty(AbstractMinecart abstractMinecartEntity) {
        return this.isVehicle() || !this.slowWhenEmpty;
    }

    @Inject(method = "push", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;hasPassenger(Lnet/minecraft/world/entity/Entity;)Z"))
    private void banner$vehicleCollide(Entity entityIn, CallbackInfo ci) {
        if (!this.hasPassenger(entityIn)) {
            VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent((Vehicle) this.getBukkitEntity(), entityIn.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(collisionEvent);
            if (collisionEvent.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Override
    public Vector getFlyingVelocityMod() {
        return new Vector(flyingX, flyingY, flyingZ);
    }

    @Override
    public void setFlyingVelocityMod(Vector flying) {
        flyingX = flying.getX();
        flyingY = flying.getY();
        flyingZ = flying.getZ();
    }

    @Override
    public Vector getDerailedVelocityMod() {
        return new Vector(derailedX, derailedY, derailedZ);
    }

    @Override
    public void setDerailedVelocityMod(Vector derailed) {
        derailedX = derailed.getX();
        derailedY = derailed.getY();
        derailedZ = derailed.getZ();
    }

    @Override
    public double bridge$maxSpeed() {
        return maxSpeed;
    }

    @Override
    public void banner$setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public boolean bridge$slowWhenEmpty() {
        return slowWhenEmpty;
    }

    @Override
    public void banner$setSlowWhenEmpty(boolean slowWhenEmpty) {
        this.slowWhenEmpty = slowWhenEmpty;
    }
}
