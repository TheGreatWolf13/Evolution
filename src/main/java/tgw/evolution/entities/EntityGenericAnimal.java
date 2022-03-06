package tgw.evolution.entities;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import tgw.evolution.blocks.BlockGenericSlowable;
import tgw.evolution.entities.util.AnimalFoodWaterController;
import tgw.evolution.entities.util.Gender;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.constants.EntityStates;
import tgw.evolution.util.time.Time;

public abstract class EntityGenericAnimal<T extends EntityGenericAnimal<T>> extends EntityGenericAgeable<T> implements IEntityAdditionalSpawnData {

    private static final EntityDataAccessor<Integer> PREGNANCY_TIME = SynchedEntityData.defineId(EntityGenericAgeable.class,
                                                                                                 EntityDataSerializers.INT);
    private final AnimalFoodWaterController foodController;
    private Gender gender = Gender.MALE;
    private boolean inLove;

    protected EntityGenericAnimal(EntityType<T> type, Level level) {
        super(type, level);
        this.foodController = new AnimalFoodWaterController(this);
        this.gender = Gender.fromBoolean(this.random.nextBoolean());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("InLove", this.inLove);
        tag.putInt("PregnancyTime", this.entityData.get(PREGNANCY_TIME));
        tag.putBoolean("Gender", this.gender.toBoolean());
        this.foodController.writeToNBT(tag);
        if (this instanceof IMammal) {
            tag.putInt("LactationTime", ((IMammal) this).getLactationTime());
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

    public abstract void appendDebugInfo(MutableComponent text);

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
    public Packet<?> getAddEntityPacket() {
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
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos.below()).getBlock() instanceof BlockGenericSlowable ? 10.0F : level.getBrightness(pos) - 0.5F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte id) {
        if (id == EntityStates.LOVE_HEARTS_PARTICLES) {
            for (int i = 0; i < 7; ++i) {
                this.level.addParticle(ParticleTypes.HEART, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                                       this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                                       this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(),
                                       this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
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
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() == EvolutionItems.DEBUG_ITEM.get()) {
            if (!this.level.isClientSide) {
                MutableComponent debug = new TextComponent("[EntityDebug]").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.BOLD);
                MutableComponent text = new TextComponent("Gender = " + this.gender + "\n");
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
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.inLove = tag.getBoolean("InLove");
        this.entityData.set(PREGNANCY_TIME, tag.getInt("PregnancyTime"));
        this.gender = Gender.fromBoolean(tag.getBoolean("Gender"));
        this.foodController.readFromNBT(tag);
        if (this instanceof IMammal) {
            ((IMammal) this).setLactationTime(tag.getInt("LactationTime"));
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
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
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.gender.toBoolean());
    }
}