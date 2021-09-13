package tgw.evolution.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import tgw.evolution.blocks.BlockGenericSlowable;
import tgw.evolution.entities.util.AnimalFoodWaterController;
import tgw.evolution.entities.util.Gender;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.EntityStates;
import tgw.evolution.util.Time;

public abstract class EntityGenericAnimal<T extends EntityGenericAnimal<T>> extends EntityGenericAgeable<T> implements IEntityAdditionalSpawnData {

    private static final DataParameter<Integer> PREGNANCY_TIME = EntityDataManager.defineId(EntityGenericAgeable.class, DataSerializers.INT);
    private final AnimalFoodWaterController foodController;
    private Gender gender = Gender.MALE;
    private boolean inLove;

    protected EntityGenericAnimal(EntityType<T> type, World worldIn) {
        super(type, worldIn);
        this.foodController = new AnimalFoodWaterController(this);
        this.gender = Gender.fromBoolean(this.random.nextBoolean());
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("InLove", this.inLove);
        compound.putInt("PregnancyTime", this.entityData.get(PREGNANCY_TIME));
        compound.putBoolean("Gender", this.gender.toBoolean());
        this.foodController.writeToNBT(compound);
        if (this instanceof IMammal) {
            compound.putInt("LactationTime", ((IMammal) this).getLactationTime());
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.isAdult() || this.isDead()) {
            this.inLove = false;
        }
        if (!this.isDead()) {
            if (this.getAge() % Time.HOUR_IN_TICKS == 0) {
                this.foodController.tick();
            }
            if (this.entityData.get(PREGNANCY_TIME) == 0) {
                this.haveBabies();
            }
            if (this.entityData.get(PREGNANCY_TIME) > -Time.MONTH_IN_TICKS) {
                this.entityData.set(PREGNANCY_TIME, this.entityData.get(PREGNANCY_TIME) - 1);
            }
            if (this.canBeInLove()) {
                this.inLove = true;
            }
            if (this.inLove) {
                if (this.getAge() % 200 == 0) {
                    this.showLoveHearts();
                }
            }
        }
    }

    public abstract void appendDebugInfo(IFormattableTextComponent text);

    public abstract boolean canBeInLove();

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(EntityGenericAnimal otherAnimal) {
        return otherAnimal != this &&
               otherAnimal.getClass() == this.getClass() &&
               this.gender != otherAnimal.gender &&
               this.inLove &&
               otherAnimal.inLove;
    }

    @Override
    protected void customServerAiStep() {
        if (!this.isAdult()) {
            this.inLove = false;
        }
        super.customServerAiStep();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PREGNANCY_TIME, -Time.MONTH_IN_TICKS);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    /**
     * Returns the gestation time period of the female of the species in ticks.
     */
    public abstract int getGestationPeriod();

    public abstract int getNumberOfBabies();

    @Override
    public double getPassengersRidingOffset() {
        return 0.14;
    }

    public int getPregnancyTime() {
        return this.entityData.get(PREGNANCY_TIME);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, IWorldReader worldIn) {
        return worldIn.getBlockState(pos.below()).getBlock() instanceof BlockGenericSlowable ? 10.0F : worldIn.getBrightness(pos) - 0.5F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte id) {
        if (id == EntityStates.LOVE_HEARTS_PARTICLES) {
            for (int i = 0; i < 7; ++i) {
                this.level.addParticle(ParticleTypes.HEART,
                                       this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                                       this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                                       this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                                       this.random.nextGaussian() * 0.02,
                                       this.random.nextGaussian() * 0.02,
                                       this.random.nextGaussian() * 0.02);
            }
        }
        else {
            super.handleEntityEvent(id);
        }
    }

    public void haveBabies() {
        this.entityData.set(PREGNANCY_TIME, -1);
        int numberOfBabies = this.getNumberOfBabies();
        for (int i = 0; i < numberOfBabies; i++) {
            this.spawnBaby();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && super.hurt(source, amount);
    }

    /**
     * Returns if the entity is currently in 'love mode'.
     */
    public boolean isInLove() {
        return this.inLove;
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() == EvolutionItems.debug_item.get()) {
            if (!this.level.isClientSide) {
                IFormattableTextComponent debug = new StringTextComponent("[EntityDebug]").withStyle(TextFormatting.YELLOW)
                                                                                          .withStyle(TextFormatting.BOLD);
                IFormattableTextComponent text = new StringTextComponent("Gender = " + this.gender + "\n");
                text.append("Health = " + this.getHealth() + "/" + this.getMaxHealth() + "\n")
                    .append("Age = " + Time.getFormattedTime(this.getAge()) + "\n")
                    .append("LifeSpan =" + " " + Time.getFormattedTime(this.getLifeSpan()) + "\n")
                    .append("InLove = " + this.inLove + "\n")
                    .append("PregnancyTime = " + Time.getFormattedTime(this.getPregnancyTime()) + "\n")
                    .append("DeathTimer" + " = " + Time.getFormattedTime(this.getDeathTime()) + "\n");
                if (this instanceof IMammal) {
                    text.append("LactationTime = " + Time.getFormattedTime(((IMammal) this).getLactationTime()));
                }
                this.appendDebugInfo(text);
                player.displayClientMessage(debug, false);
                player.displayClientMessage(text, false);
            }
            return ActionResultType.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT compound) {
        super.readAdditionalSaveData(compound);
        this.inLove = compound.getBoolean("InLove");
        this.entityData.set(PREGNANCY_TIME, compound.getInt("PregnancyTime"));
        this.gender = Gender.fromBoolean(compound.getBoolean("Gender"));
        this.foodController.readFromNBT(compound);
        if (this instanceof IMammal) {
            ((IMammal) this).setLactationTime(compound.getInt("LactationTime"));
        }
    }

    @Override
    public void readSpawnData(PacketBuffer buffer) {
        this.gender = Gender.fromBoolean(buffer.readBoolean());
    }

    public void resetInLove() {
        this.inLove = false;
    }

    public void showLoveHearts() {
        this.level.broadcastEntityEvent(this, EntityStates.LOVE_HEARTS_PARTICLES);
    }

    public abstract void spawnBaby();

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeBoolean(this.gender.toBoolean());
    }
}