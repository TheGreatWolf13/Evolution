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
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
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

    private static final DataParameter<Integer> PREGNANCY_TIME = EntityDataManager.createKey(EntityGenericAgeable.class, DataSerializers.VARINT);
    private final AnimalFoodWaterController foodController;
    private Gender gender = Gender.MALE;
    private boolean inLove;

    protected EntityGenericAnimal(EntityType<T> type, World worldIn) {
        super(type, worldIn);
        this.foodController = new AnimalFoodWaterController(this);
        this.gender = Gender.fromBoolean(this.rand.nextBoolean());
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && super.attackEntityFrom(source, amount);
    }

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
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public float getBlockPathWeight(BlockPos pos, IWorldReader worldIn) {
        return worldIn.getBlockState(pos.down()).getBlock() instanceof BlockGenericSlowable ? 10.0F : worldIn.getBrightness(pos) - 0.5F;
    }

    /**
     * Returns the gestation time period of the female of the species in ticks.
     */
    public abstract int getGestationPeriod();

    public abstract int getNumberOfBabies();

    public int getPregnancyTime() {
        return this.dataManager.get(PREGNANCY_TIME);
    }

    @Override
    public int getTalkInterval() {
        return 120;
    }

    @Override
    public double getYOffset() {
        return 0.14;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == EntityStates.LOVE_HEARTS_PARTICLES) {
            for (int i = 0; i < 7; ++i) {
                this.world.addParticle(ParticleTypes.HEART,
                                       this.posX + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(),
                                       this.posY + 0.5 + this.rand.nextFloat() * this.getHeight(),
                                       this.posZ + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(),
                                       this.rand.nextGaussian() * 0.02,
                                       this.rand.nextGaussian() * 0.02,
                                       this.rand.nextGaussian() * 0.02);
            }
        }
        else {
            super.handleStatusUpdate(id);
        }
    }

    public void haveBabies() {
        this.dataManager.set(PREGNANCY_TIME, -1);
        int numberOfBabies = this.getNumberOfBabies();
        for (int i = 0; i < numberOfBabies; i++) {
            this.spawnBaby();
        }
    }

    /**
     * Returns if the entity is currently in 'love mode'.
     */
    public boolean isInLove() {
        return this.inLove;
    }

    @Override
    public void livingTick() {
        super.livingTick();
        if (!this.isAdult() || this.isDead()) {
            this.inLove = false;
        }
        if (!this.isDead()) {
            if (this.getAge() % Time.HOUR_IN_TICKS == 0) {
                this.foodController.tick();
            }
            if (this.dataManager.get(PREGNANCY_TIME) == 0) {
                this.haveBabies();
            }
            if (this.dataManager.get(PREGNANCY_TIME) > -Time.MONTH_IN_TICKS) {
                this.dataManager.set(PREGNANCY_TIME, this.dataManager.get(PREGNANCY_TIME) - 1);
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

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == EvolutionItems.placeholder_item.get()) {
            if (!this.world.isRemote()) {
                ITextComponent debug = new StringTextComponent("[EntityDebug]").setStyle(new Style().setColor(TextFormatting.YELLOW).setBold(true));
                ITextComponent text = new StringTextComponent("Gender = " + this.gender + "\n");
                text.appendText("Health = " + this.getHealth() + "/" + this.getMaxHealth() + "\n")
                    .appendText("Age = " + Time.getFormattedTime(this.getAge()) + "\n")
                    .appendText("LifeSpan =" + " " + Time.getFormattedTime(this.getLifeSpan()) + "\n")
                    .appendText("InLove = " + this.inLove + "\n")
                    .appendText("PregnancyTime = " + Time.getFormattedTime(this.getPregnancyTime()) + "\n")
                    .appendText("DeathTimer" + " = " + Time.getFormattedTime(this.getDeathTime()) + "\n");
                if (this instanceof IMammal) {
                    text.appendText("LactationTime = " + Time.getFormattedTime(((IMammal) this).getLactationTime()));
                }
                player.sendMessage(debug);
                player.sendMessage(text);
            }
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.inLove = compound.getBoolean("InLove");
        this.dataManager.set(PREGNANCY_TIME, compound.getInt("PregnancyTime"));
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

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(PREGNANCY_TIME, -Time.MONTH_IN_TICKS);
    }

    public void resetInLove() {
        this.inLove = false;
    }

    public void showLoveHearts() {
        this.world.setEntityState(this, EntityStates.LOVE_HEARTS_PARTICLES);
    }

    public abstract void spawnBaby();

    @Override
    protected void updateAITasks() {
        if (!this.isAdult()) {
            this.inLove = false;
        }
        super.updateAITasks();
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("InLove", this.inLove);
        compound.putInt("PregnancyTime", this.dataManager.get(PREGNANCY_TIME));
        compound.putBoolean("Gender", this.gender.toBoolean());
        this.foodController.writeToNBT(compound);
        if (this instanceof IMammal) {
            compound.putInt("LactationTime", ((IMammal) this).getLactationTime());
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeBoolean(this.gender.toBoolean());
    }
}