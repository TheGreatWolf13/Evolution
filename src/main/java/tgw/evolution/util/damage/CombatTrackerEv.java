package tgw.evolution.util.damage;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import tgw.evolution.Evolution;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

import javax.annotation.Nullable;

public class CombatTrackerEv extends CombatTracker {
    private final OList<CombatEntryEv> entries = new OArrayList<>();
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
        for (int i = 0, l = this.entries.size(); i < l; ++i) {
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
    public Component getDeathMessage() {
        if (this.entries.isEmpty()) {
            return new TranslatableComponent("death.attack.generic", this.fighter.getDisplayName());
        }
        CombatEntryEv bestEntry = this.getBestCombatEntry();
        CombatEntryEv lastEntry = this.entries.get(this.entries.size() - 1);
        Entity lastEntity = lastEntry.getDamageSrc().getEntity();
        //hit then fall
        if (bestEntry != null && lastEntry.getDamageSrc() == EvolutionDamage.FALL) {
            Component bestEntryDisplay = bestEntry.getDamageSrcDisplayName();
            if (bestEntry.getDamageSrc() != EvolutionDamage.FALL && bestEntry.getDamageSrc() != EvolutionDamage.VOID) {
                if (bestEntryDisplay != null) {
                    DamageSource bestDamageSource = bestEntry.getDamageSrc();
                    Component itemComp;
                    if (bestDamageSource instanceof DamageSourceEntity dse) {
                        itemComp = dse.getItemDisplay();
                    }
                    else {
                        Entity bestEntity = bestDamageSource.getEntity();
                        ItemStack bestStack = bestEntity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
                        itemComp = bestStack.getItem() instanceof IMelee ? bestStack.getHoverName() : null;
                    }
                    //was doomed to fall by using
                    if (itemComp != null) {
                        if (this.fighter instanceof ServerPlayer serverPlayer) {
                            serverPlayer.awardStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                        }
                        return new TranslatableComponent("death.fell.assist.item." + getFallSuffix(bestEntry),
                                                         this.fighter.getDisplayName(),
                                                         bestEntryDisplay,
                                                         itemComp);
                    }
                    //was doomed to fall by
                    if (this.fighter instanceof ServerPlayer serverPlayer) {
                        serverPlayer.awardStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                    }
                    return new TranslatableComponent("death.fell.assist." + getFallSuffix(bestEntry),
                                                     this.fighter.getDisplayName(),
                                                     bestEntryDisplay);
                }
                //was doomed to fall
                if (this.fighter instanceof ServerPlayer serverPlayer) {
                    serverPlayer.awardStat(EvolutionStats.DEATH_SOURCE.get("doomed_to_fall"));
                }
                return new TranslatableComponent("death.fell.killer." + getFallSuffix(bestEntry), this.fighter.getDisplayName());
            }
        }
        //fall then hit
        if (bestEntry != null && bestEntry.getDamageSrc() == EvolutionDamage.FALL) {
            Component lastEntryDisplay = lastEntry.getDamageSrcDisplayName();
            if (lastEntryDisplay != null) {
                DamageSource lastSource = lastEntry.getDamageSrc();
                Component itemComp;
                if (lastSource instanceof DamageSourceEntity dse) {
                    itemComp = dse.getItemDisplay();
                }
                else {
                    ItemStack lastStack = lastEntity instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
                    itemComp = lastStack.getItem() instanceof IMelee ? lastStack.getHoverName() : null;
                }
                //fell too far and was finished by using
                if (itemComp != null) {
                    if (this.fighter instanceof ServerPlayer serverPlayer) {
                        serverPlayer.awardStat(EvolutionStats.DEATH_SOURCE.get("fall_then_finished"));
                    }
                    return new TranslatableComponent("death.fell.finish.item." + getFallSuffix(bestEntry),
                                                     this.fighter.getDisplayName(),
                                                     lastEntryDisplay,
                                                     itemComp);
                }
                //fell too far and was finished by
                if (this.fighter instanceof ServerPlayer serverPlayer) {
                    serverPlayer.awardStat(EvolutionStats.DEATH_SOURCE.get("fall_then_finished"));
                }
                return new TranslatableComponent("death.fell.finish." + getFallSuffix(bestEntry), this.fighter.getDisplayName(), lastEntryDisplay);
            }
            //Fell from a high place, ladder, rope, vine
            if (this.fighter instanceof ServerPlayer serverPlayer) {
                serverPlayer.awardStat(EvolutionStats.DEATH_SOURCE.get("fall"));
            }
            return new TranslatableComponent("death.fell.accident." + getFallSuffix(bestEntry), this.fighter.getDisplayName());
        }
        return lastEntry.getDamageSrc().getLocalizedDeathMessage(this.fighter);
    }

    @Override
    @Nullable
    public LivingEntity getKiller() {
        LivingEntity livingEntity = null;
        Player player = null;
        float livingDamage = 0.0F;
        float playerDamage = 0.0F;
        for (CombatEntryEv entry : this.entries) {
            if (entry.getDamageSrc().getEntity() instanceof Player && (player == null || entry.getDamage() > playerDamage)) {
                playerDamage = entry.getDamage();
                player = (Player) entry.getDamageSrc().getEntity();
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
                Evolution.warn("Missing fall suffix for {}", this.fallSuffixBlock);
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
