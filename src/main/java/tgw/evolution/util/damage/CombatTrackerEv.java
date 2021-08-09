package tgw.evolution.util.damage;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.items.IMelee;

import javax.annotation.Nullable;
import java.util.List;

public class CombatTrackerEv extends CombatTracker {
    private final List<CombatEntryEv> combatEntries = Lists.newArrayList();
    private final LivingEntity fighter;
    private int combatEndTime;
    private int combatStartTime;
    @Nullable
    private String fallSuffix;
    @Nullable
    private Block fallSuffixBlock;
    private boolean inCombat;
    private int lastDamageTime;
    private boolean takingDamage;

    public CombatTrackerEv(LivingEntity fighter) {
        super(fighter);
        this.fighter = fighter;
    }

    private static String getFallSuffix(CombatEntryEv entry) {
        return entry.getFallSuffix() == null ? "generic" : entry.getFallSuffix();
    }

    @Override
    public void calculateFallSuffix() {
        this.resetFallSuffix();
        if (this.fallSuffixBlock != null) {
            if (this.fallSuffixBlock == Blocks.LADDER) {
                this.fallSuffix = "ladder";
            }
            else if (this.fallSuffixBlock == EvolutionBlocks.ROPE.get()) {
                this.fallSuffix = "rope";
            }
            else if (this.fallSuffixBlock == Blocks.VINE) {
                this.fallSuffix = "vines";
            }
            this.fallSuffixBlock = null;
        }
    }

    @Override
    @Nullable
    public LivingEntity getBestAttacker() {
        LivingEntity livingEntity = null;
        PlayerEntity player = null;
        float livingDamage = 0.0F;
        float playerDamage = 0.0F;
        for (CombatEntryEv entry : this.combatEntries) {
            if (entry.getDamageSrc().getTrueSource() instanceof PlayerEntity && (player == null || entry.getDamage() > playerDamage)) {
                playerDamage = entry.getDamage();
                player = (PlayerEntity) entry.getDamageSrc().getTrueSource();
            }
            if (entry.getDamageSrc().getTrueSource() instanceof LivingEntity && (livingEntity == null || entry.getDamage() > livingDamage)) {
                livingDamage = entry.getDamage();
                livingEntity = (LivingEntity) entry.getDamageSrc().getTrueSource();
            }
        }
        if (player != null && playerDamage >= livingDamage) {
            return player;
        }
        return livingEntity;
    }

    /**
     * @return Always returns fall or void entries, or the ones before them, if any.
     */
    @Nullable
    private CombatEntryEv getBestCombatEntry() {
        CombatEntryEv lastFallVoidEntry = null;
        CombatEntryEv lastSuffixEntry = null;
        float suffixDamage = 0.0F;
        float fallOrVoidAmount = 0.0F;
        for (int i = 0; i < this.combatEntries.size(); ++i) {
            CombatEntryEv currentEntry = this.combatEntries.get(i);
            CombatEntryEv possibleDoomEntry = i > 0 ? this.combatEntries.get(i - 1) : null;
            if ((currentEntry.getDamageSrc() == EvolutionDamage.FALL || currentEntry.getDamageSrc() == EvolutionDamage.VOID) &&
                currentEntry.getDamage() > 0.0F &&
                (lastFallVoidEntry == null || currentEntry.getDamageAmount() > fallOrVoidAmount)) {
                if (i > 0) {
                    lastFallVoidEntry = possibleDoomEntry;
                }
                else {
                    lastFallVoidEntry = currentEntry;
                }
                fallOrVoidAmount = currentEntry.getDamageAmount();
            }
            if (currentEntry.getFallSuffix() != null && (lastSuffixEntry == null || currentEntry.getDamage() > suffixDamage)) {
                lastSuffixEntry = currentEntry;
                suffixDamage = currentEntry.getDamage();
            }
        }
        if (fallOrVoidAmount > 5.0F && lastFallVoidEntry != null) {
            return lastFallVoidEntry;
        }
        if (suffixDamage > 5.0F) {
            return lastSuffixEntry;
        }
        return null;
    }

    @Override
    public int getCombatDuration() {
        return this.inCombat ? this.fighter.ticksExisted - this.combatStartTime : this.combatEndTime - this.combatStartTime;
    }

    @Override
    public ITextComponent getDeathMessage() {
        if (this.combatEntries.isEmpty()) {
            return new TranslationTextComponent("death.attack.generic", this.fighter.getDisplayName());
        }
        CombatEntryEv bestEntry = this.getBestCombatEntry();
        CombatEntryEv lastEntry = this.combatEntries.get(this.combatEntries.size() - 1);
        Entity lastEntity = lastEntry.getDamageSrc().getTrueSource();
        //hit then fall
        if (bestEntry != null && lastEntry.getDamageSrc() == EvolutionDamage.FALL) {
            ITextComponent bestEntryDisplay = bestEntry.getDamageSrcDisplayName();
            if (bestEntry.getDamageSrc() != EvolutionDamage.FALL && bestEntry.getDamageSrc() != EvolutionDamage.VOID) {
                if (bestEntryDisplay != null) {
                    Entity bestEntity = bestEntry.getDamageSrc().getTrueSource();
                    ItemStack bestStack = bestEntity instanceof LivingEntity ? ((LivingEntity) bestEntity).getHeldItemMainhand() : ItemStack.EMPTY;
                    ITextComponent itemComp = bestStack.getItem() instanceof IMelee ? bestStack.getTextComponent() : null;
                    DamageSource bestDamageSource = bestEntry.getDamageSrc();
                    if (bestDamageSource instanceof DamageSourceEntity) {
                        itemComp = ((DamageSourceEntity) bestDamageSource).getItemDisplay();
                    }
                    //was doomed to fall by using
                    if (itemComp != null) {
                        if (this.fighter instanceof ServerPlayerEntity) {
                            ((ServerPlayerEntity) this.fighter).addStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                        }
                        return new TranslationTextComponent("death.fell.assist.item." + getFallSuffix(bestEntry),
                                                            this.fighter.getDisplayName(),
                                                            bestEntryDisplay,
                                                            itemComp);
                    }
                    //was doomed to fall by
                    if (this.fighter instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) this.fighter).addStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                    }
                    return new TranslationTextComponent("death.fell.assist." + getFallSuffix(bestEntry),
                                                        this.fighter.getDisplayName(),
                                                        bestEntryDisplay);
                }
                //was doomed to fall
                if (this.fighter instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) this.fighter).addStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                }
                return new TranslationTextComponent("death.fell.killer." + getFallSuffix(bestEntry), this.fighter.getDisplayName());
            }
        }
        //fall then hit
        if (bestEntry != null && bestEntry.getDamageSrc() == EvolutionDamage.FALL) {
            ITextComponent lastEntryDisplay = lastEntry.getDamageSrcDisplayName();
            if (lastEntryDisplay != null) {
                ItemStack lastStack = lastEntity instanceof LivingEntity ? ((LivingEntity) lastEntity).getHeldItemMainhand() : ItemStack.EMPTY;
                ITextComponent itemComp = lastStack.getItem() instanceof IMelee ? lastStack.getTextComponent() : null;
                DamageSource lastSource = lastEntry.getDamageSrc();
                if (lastSource instanceof DamageSourceEntity) {
                    itemComp = ((DamageSourceEntity) lastSource).getItemDisplay();
                }
                //fell too far and was finished by using
                if (itemComp != null) {
                    if (this.fighter instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) this.fighter).addStat(EvolutionStats.DEATH_SOURCE.get("fall_then_finished"));
                    }
                    return new TranslationTextComponent("death.fell.finish.item." + getFallSuffix(bestEntry),
                                                        this.fighter.getDisplayName(),
                                                        lastEntryDisplay,
                                                        itemComp);
                }
                //fell too far and was finished by
                if (this.fighter instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) this.fighter).addStat(EvolutionStats.DEATH_SOURCE.get("fall_then_finished"));
                }
                return new TranslationTextComponent("death.fell.finish." + getFallSuffix(bestEntry), this.fighter.getDisplayName(), lastEntryDisplay);
            }
            //Fell from a high place, ladder, rope, vine
            if (this.fighter instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) this.fighter).addStat(EvolutionStats.DEATH_SOURCE.get("fall"));
            }
            return new TranslationTextComponent("death.fell.accident." + getFallSuffix(bestEntry), this.fighter.getDisplayName());
        }
        return lastEntry.getDamageSrc().getDeathMessage(this.fighter);
    }

    @Override
    public LivingEntity getFighter() {
        return this.fighter;
    }

    @Override
    public void reset() {
        int i = this.inCombat ? 300 : 100;
        if (this.takingDamage && (this.fighter.deathTime > 0 || this.fighter.ticksExisted - this.lastDamageTime > i)) {
            boolean isInCombat = this.inCombat;
            this.takingDamage = false;
            this.inCombat = false;
            this.combatEndTime = this.fighter.ticksExisted;
            if (isInCombat) {
                this.fighter.sendEndCombat();
            }
            this.combatEntries.clear();
        }
    }

    private void resetFallSuffix() {
        this.fallSuffix = null;
    }

    public void setFallSuffixBlock(@Nullable Block block) {
        this.fallSuffixBlock = block;
    }

    @Override
    public void trackDamage(DamageSource source, float healthIn, float damageAmount) {
        this.reset();
        this.calculateFallSuffix();
        CombatEntryEv entry = new CombatEntryEv(source, damageAmount, this.fallSuffix);
        this.combatEntries.add(entry);
        this.lastDamageTime = this.fighter.ticksExisted;
        this.takingDamage = true;
        if (entry.isLivingDamageSrc() && !this.inCombat && this.fighter.isAlive()) {
            this.inCombat = true;
            this.combatStartTime = this.fighter.ticksExisted;
            this.combatEndTime = this.combatStartTime;
            this.fighter.sendEnterCombat();
        }
    }
}
