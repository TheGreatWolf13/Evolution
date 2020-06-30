package tgw.evolution.entities;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
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
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.EnumFoodNutrients;
import tgw.evolution.util.Feces;
import tgw.evolution.util.FoodNutrients;
import tgw.evolution.util.Time;

public abstract class AnimalEntity extends AgeableEntity {

    public final FoodNutrients food = new FoodNutrients();
    public final Feces poo = new Feces();
    protected int pregnancyTime;
    private boolean inLove;
    private int domestication;
    private int milkTime;
    private boolean recentlyFed;

    protected AnimalEntity(EntityType<? extends AnimalEntity> type, World worldIn) {
        super(type, worldIn);
    }

    /**
     * Checks if the parameter is an item which this animal can be fed.
     */
    public static boolean isEdibleItem(ItemStack stack) {
        return stack.getItem() == Items.WHEAT;
    }

    /**
     * Tries to modify the animal's food level. If amount is negative and the entity does not have
     * that much food to lose, it will return false, otherwise will return true.
     */
    public boolean modifyFood(EnumFoodNutrients nutrient, int amount) {
        if (this.food.get(nutrient) + amount < 0) {
            return false;
        }
        this.food.add(nutrient, amount);
        return true;
    }

    /**
     * Makes the animal consume food in order to keep itself alive or regenerate health. Will produce poo. Will return
     * false if the animal does not have enough food.
     */
    public boolean consumeFood(int amount) {
        if (this.modifyFood(EnumFoodNutrients.FOOD, -amount)) {
            this.poo.add(EnumFoodNutrients.FOOD, amount);
            return true;
        }
        return false;
    }

    /**
     * Tries to regenerate the entity's health, if it needs and has food for it. Will produce poo.
     */
    public void regenHealth() {
        if (this.getMaxHealth() - this.getHealth() >= 1.0F) {
            if (this.consumeFood(1)) {
                this.setHealth(this.getHealth() + 1.0F);
            }
        }
    }

    /**
     * Returns the gestation time period of the female of the species in ticks.
     */
    //TODO pregnancy
    public abstract int getGestationTime();

    @Override
    protected void updateAITasks() {
        if (!this.isAdult()) {
            this.inLove = false;
        }
        super.updateAITasks();
    }

    @Override
    public void livingTick() {
        super.livingTick();
        if (!this.isAdult() || this.isDead()) {
            this.inLove = false;
            this.pregnancyTime = -Time.MONTH_IN_TICKS;
        }
        if (!this.isDead()) {
            if (this.getAge() % Time.HOUR_IN_TICKS == 0) {
                this.recentlyFed = false;
                if (this.isChild()) {
                    if (!this.consumeFood(1)) {
                        this.attackEntityFrom(DamageSource.STARVE, 1.0F);
                    }
                    this.regenHealth();
                }
                else if (this.isAdult()) {
                    if (!this.consumeFood(3)) {
                        this.attackEntityFrom(DamageSource.STARVE, 1.0F);
                    }
                    this.regenHealth();
                }
                else {
                    if (!this.consumeFood(3)) {
                        this.attackEntityFrom(DamageSource.STARVE, 1.0F);
                    }
                }
                //TODO shit
            }
            if (this.pregnancyTime > -Time.MONTH_IN_TICKS) {
                this.pregnancyTime--;
                //TODO have baby
            }
            if (this.isAdult() && this.pregnancyTime == -Time.MONTH_IN_TICKS && this.food.get(EnumFoodNutrients.FOOD) >= 200) {
                this.inLove = true;
            }
            if (this.inLove) {
                if (this.getAge() % 200 == 0) {
                    this.setInLove();
                }
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && super.attackEntityFrom(source, amount);
    }

    //TODO shit
    public void shit() {
    }

    @Override
    public float getBlockPathWeight(BlockPos pos, IWorldReader worldIn) {
        return worldIn.getBlockState(pos.down()).getBlock() == Blocks.GRASS_BLOCK ? 10.0F : worldIn.getBrightness(pos) - 0.5F;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("InLove", this.inLove);
        //        compound.putIntArray("Food", new int[]{this.food.get(EnumFoodNutrients.FOOD),
        //                                               this.food.get(EnumFoodNutrients.NITROGEN),
        //                                               this.food.get(EnumFoodNutrients.POTASSIUM),
        //                                               this.food.get(EnumFoodNutrients.PHOSPHORUS)});
        compound.putInt("PregnancyTime", this.pregnancyTime);
        compound.putByte("Domestication", (byte) this.domestication);
        compound.putBoolean("RecentlyFed", this.recentlyFed);
        compound.putInt("MilkTime", this.milkTime);
        //        compound.putByteArray("Poo", new byte[]{(byte) this.poo.get(EnumFoodNutrients.FOOD),
        //                                                (byte) this.poo.get(EnumFoodNutrients.NITROGEN),
        //                                                (byte) this.poo.get(EnumFoodNutrients.POTASSIUM),
        //                                                (byte) this.poo.get(EnumFoodNutrients.PHOSPHORUS)});
    }

    @Override
    public double getYOffset() {
        return 0.14D;
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.inLove = compound.getBoolean("InLove");
        //        this.food.set(EnumFoodNutrients.FOOD, compound.getIntArray("Food")[0]);
        //        this.food.set(EnumFoodNutrients.NITROGEN, compound.getIntArray("Food")[1]);
        //        this.food.set(EnumFoodNutrients.POTASSIUM, compound.getIntArray("Food")[2]);
        //        this.food.set(EnumFoodNutrients.PHOSPHORUS, compound.getIntArray("Food")[3]);
        this.pregnancyTime = compound.getInt("PregnancyTime");
        this.milkTime = compound.getInt("MilkTime");
        this.domestication = compound.getByte("Domestication");
        //        this.poo.set(EnumFoodNutrients.FOOD, compound.getByteArray("Poo")[0]);
        //        this.poo.set(EnumFoodNutrients.NITROGEN, compound.getByteArray("Poo")[1]);
        //        this.poo.set(EnumFoodNutrients.POTASSIUM, compound.getByteArray("Poo")[2]);
        //        this.poo.set(EnumFoodNutrients.PHOSPHORUS, compound.getByteArray("Poo")[3]);
    }

    @Override
    public int getTalkInterval() {
        return 120;
    }

    @Override
    protected int getExperiencePoints(PlayerEntity player) {
        return 0;
    }

    @Override
    public boolean processInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (AnimalEntity.isEdibleItem(itemstack) && !this.isDead()) {
            if (!this.recentlyFed) {
                //TODO make different items to feed the animal
                //				this.consumeItemFromStack(player, itemstack);
                //				this.recentlyFed = true;
                //				this.modifyFood(5);
                //				this.domestication += 1;
                //				this.playSound(SoundEvents.ENTITY_PLAYER_BURP, 1F, 1F);
                return true;
            }
        }
        else if (itemstack.getItem() == EvolutionItems.placeholder_item.get()) {
            if (!this.world.isRemote()) {
                ITextComponent debug = new StringTextComponent("[EntityDebug]").setStyle(new Style().setColor(TextFormatting.YELLOW).setBold(true));
                ITextComponent text = new StringTextComponent("Health = " + this.getHealth() + "/" + this.getMaxHealth() + "\n").appendText("Age = " + Time.getFormattedTime(this.getAge()) + "\n").appendText("LifeSpan = " + Time.getFormattedTime(this.getDeterminedLifeSpan()) + "\n").appendText("Food = " + this.food + "\n").appendText("RecentlyFed = " + this.recentlyFed + "\n").appendText("Poo = " + this.poo + "\n").appendText("InLove = " + this.inLove + "\n").appendText("MilkTime = " + Time.getFormattedTime(this.milkTime) + "\n").appendText("PregnancyTime = " + Time.getFormattedTime(this.pregnancyTime) + "\n").appendText("Domestication = " + this.domestication + "\n").appendText("DeathTimer = " + Time.getFormattedTime(this.getDeathTime()));
                player.sendMessage(debug);
                player.sendMessage(text);
            }
        }
        return super.processInteract(player, hand);
    }

    public void setInLove() {
        this.inLove = true;
        this.world.setEntityState(this, (byte) 18);
    }

    /**
     * Returns if the entity is currently in 'love mode'.
     */
    public boolean isInLove() {
        return this.inLove;
    }

    public void resetInLove() {
        this.inLove = false;
    }

    /**
     * Returns true if the mob is currently able to mate with the specified mob.
     */
    public boolean canMateWith(AnimalEntity otherAnimal) {
        return otherAnimal != this && otherAnimal.getClass() == this.getPartnerClass() && this.inLove && otherAnimal.inLove;
    }

    /**
     * Returns the class of this entity's mate partner.
     */
    public abstract Class<? extends AnimalEntity> getPartnerClass();

    /**
     * Returns the class of this species that is the female.
     */
    public abstract Class<? extends AnimalEntity> getFemaleClass();

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 18) {
            for (int i = 0; i < 7; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.addParticle(ParticleTypes.HEART, this.posX + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(), this.posY + 0.5D + this.rand.nextFloat() * this.getHeight(), this.posZ + this.rand.nextFloat() * this.getWidth() * 2.0F - this.getWidth(), d0, d1, d2);
            }
        }
        else {
            super.handleStatusUpdate(id);
        }
    }
}