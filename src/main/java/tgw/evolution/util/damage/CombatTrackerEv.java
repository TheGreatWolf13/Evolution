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
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.items.IMelee;

import javax.annotation.Nullable;
import java.util.List;

public class CombatTrackerEv extends CombatTracker {
    private final List<CombatEntryEv> entries = Lists.newArrayList();
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

    /**
     * @return Always returns fall or void entries, or the ones before them, if any.
     */
    @Nullable
    private CombatEntryEv getBestCombatEntry() {
        CombatEntryEv lastFallVoidEntry = null;
        CombatEntryEv lastSuffixEntry = null;
        float suffixDamage = 0.0F;
        float fallOrVoidAmount = 0.0F;
        for (int i = 0; i < this.entries.size(); ++i) {
            CombatEntryEv currentEntry = this.entries.get(i);
            CombatEntryEv possibleDoomEntry = i > 0 ? this.entries.get(i - 1) : null;
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
        return this.inCombat ? this.fighter.tickCount - this.combatStartTime : this.combatEndTime - this.combatStartTime;
    }

    @Override
    public ITextComponent getDeathMessage() {
        if (this.entries.isEmpty()) {
            return new TranslationTextComponent("death.attack.generic", this.fighter.getDisplayName());
        }
        CombatEntryEv bestEntry = this.getBestCombatEntry();
        CombatEntryEv lastEntry = this.entries.get(this.entries.size() - 1);
        Entity lastEntity = lastEntry.getDamageSrc().getEntity();
        //hit then fall
        if (bestEntry != null && lastEntry.getDamageSrc() == EvolutionDamage.FALL) {
            ITextComponent bestEntryDisplay = bestEntry.getDamageSrcDisplayName();
            if (bestEntry.getDamageSrc() != EvolutionDamage.FALL && bestEntry.getDamageSrc() != EvolutionDamage.VOID) {
                if (bestEntryDisplay != null) {
                    DamageSource bestDamageSource = bestEntry.getDamageSrc();
                    ITextComponent itemComp;
                    if (bestDamageSource instanceof DamageSourceEntity) {
                        itemComp = ((DamageSourceEntity) bestDamageSource).getItemDisplay();
                    }
                    else {
                        Entity bestEntity = bestDamageSource.getEntity();
                        ItemStack bestStack = bestEntity instanceof LivingEntity ? ((LivingEntity) bestEntity).getMainHandItem() : ItemStack.EMPTY;
                        itemComp = bestStack.getItem() instanceof IMelee ? bestStack.getHoverName() : null;
                    }
                    //was doomed to fall by using
                    if (itemComp != null) {
                        if (this.fighter instanceof ServerPlayerEntity) {
                            ((ServerPlayerEntity) this.fighter).awardStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                        }
                        return new TranslationTextComponent("death.fell.assist.item." + getFallSuffix(bestEntry),
                                                            this.fighter.getDisplayName(),
                                                            bestEntryDisplay,
                                                            itemComp);
                    }
                    //was doomed to fall by
                    if (this.fighter instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) this.fighter).awardStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                    }
                    return new TranslationTextComponent("death.fell.assist." + getFallSuffix(bestEntry),
                                                        this.fighter.getDisplayName(),
                                                        bestEntryDisplay);
                }
                //was doomed to fall
                if (this.fighter instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) this.fighter).awardStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                }
                return new TranslationTextComponent("death.fell.killer." + getFallSuffix(bestEntry), this.fighter.getDisplayName());
            }
        }
        //fall then hit
        if (bestEntry != null && bestEntry.getDamageSrc() == EvolutionDamage.FALL) {
            ITextComponent lastEntryDisplay = lastEntry.getDamageSrcDisplayName();
            if (lastEntryDisplay != null) {
                DamageSource lastSource = lastEntry.getDamageSrc();
                ITextComponent itemComp;
                if (lastSource instanceof DamageSourceEntity) {
                    itemComp = ((DamageSourceEntity) lastSource).getItemDisplay();
                }
                else {
                    ItemStack lastStack = lastEntity instanceof LivingEntity ? ((LivingEntity) lastEntity).getMainHandItem() : ItemStack.EMPTY;
                    itemComp = lastStack.getItem() instanceof IMelee ? lastStack.getHoverName() : null;
                }
                //fell too far and was finished by using
                if (itemComp != null) {
                    if (this.fighter instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity) this.fighter).awardStat(EvolutionStats.DEATH_SOURCE.get("fall_then_finished"));
                    }
                    return new TranslationTextComponent("death.fell.finish.item." + getFallSuffix(bestEntry),
                                                        this.fighter.getDisplayName(),
                                                        lastEntryDisplay,
                                                        itemComp);
                }
                //fell too far and was finished by
                if (this.fighter instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) this.fighter).awardStat(EvolutionStats.DEATH_SOURCE.get("fall_then_finished"));
                }
                return new TranslationTextComponent("death.fell.finish." + getFallSuffix(bestEntry), this.fighter.getDisplayName(), lastEntryDisplay);
            }
            //Fell from a high place, ladder, rope, vine
            if (this.fighter instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) this.fighter).awardStat(EvolutionStats.DEATH_SOURCE.get("fall"));
            }
            return new TranslationTextComponent("death.fell.accident." + getFallSuffix(bestEntry), this.fighter.getDisplayName());
        }
        return lastEntry.getDamageSrc().getLocalizedDeathMessage(this.fighter);
    }

    @Override
    @Nullable
    public LivingEntity getKiller() {
        LivingEntity livingEntity = null;
        PlayerEntity player = null;
        float livingDamage = 0.0F;
        float playerDamage = 0.0F;
        for (CombatEntryEv entry : this.entries) {
            if (entry.getDamageSrc().getEntity() instanceof PlayerEntity && (player == null || entry.getDamage() > playerDamage)) {
                playerDamage = entry.getDamage();
                player = (PlayerEntity) entry.getDamageSrc().getEntity();
            }
            if (entry.getDamageSrc().getEntity() instanceof LivingEntity && (livingEntity == null || entry.getDamage() > livingDamage)) {
                livingDamage = entry.getDamage();
                livingEntity = (LivingEntity) entry.getDamageSrc().getEntity();
            }
        }
        if (player != null && playerDamage >= livingDamage) {
            return player;
        }
        return livingEntity;
    }

    @Override
    public LivingEntity getMob() {
        return this.fighter;
    }

    @Override
    public void prepareForDamage() {
        this.resetPreparedStatus();
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
            else {
                Evolution.LOGGER.warn("Missing fall suffix for {}", this.fallSuffixBlock);
            }
            this.fallSuffixBlock = null;
        }
    }

    @Override
    public void recheckStatus() {
        int i = this.inCombat ? 300 : 100;
        if (this.takingDamage && (this.fighter.deathTime > 0 || this.fighter.tickCount - this.lastDamageTime > i)) {
            boolean isInCombat = this.inCombat;
            this.takingDamage = false;
            this.inCombat = false;
            this.combatEndTime = this.fighter.tickCount;
            if (isInCombat) {
                this.fighter.onLeaveCombat();
            }
            this.entries.clear();
        }
    }

    @Override
    public void recordDamage(DamageSource source, float healthIn, float damageAmount) {
        this.recheckStatus();
        this.prepareForDamage();
        CombatEntryEv entry = new CombatEntryEv(source, damageAmount, this.fallSuffix);
        this.entries.add(entry);
        this.lastDamageTime = this.fighter.tickCount;
        this.takingDamage = true;
        if (entry.isLivingDamageSrc() && !this.inCombat && this.fighter.isAlive()) {
            this.inCombat = true;
            this.combatStartTime = this.fighter.tickCount;
            this.combatEndTime = this.combatStartTime;
            this.fighter.onEnterCombat();
        }
    }

    private void resetPreparedStatus() {
        this.fallSuffix = null;
    }

    public void setFallSuffixBlock(@Nullable Block block) {
        this.fallSuffixBlock = block;
    }
}
