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
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.IFallSufixBlock;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionStats;
import tgw.evolution.items.IMelee;
import tgw.evolution.util.collection.OArrayList;
import tgw.evolution.util.collection.OList;

public class EvolutionCombatTracker extends CombatTracker {
    private final OList<EvolutionCombatEntry> entries = new OArrayList<>();
    private final LivingEntity fighter;
    private int combatEndTime;
    private int combatStartTime;
    private boolean inCombat;
    private int lastDamageTime;
    private @Nullable IFallSufixBlock lastSuffix;
    private boolean takingDamage;

    public EvolutionCombatTracker(LivingEntity fighter) {
        super(fighter);
        this.fighter = fighter;
    }

    private static String getFallSuffix(EvolutionCombatEntry entry) {
        return entry.getFallSuffix() == null ? "generic" : entry.getFallSuffix();
    }

    /**
     * @return Checks the previous CombatEntries to determine if the fighter fell before they were killed or if they were pushed to their death /
     * into the void.
     */
    @Nullable
    private EvolutionCombatEntry getBestCombatEntry() {
        EvolutionCombatEntry promFallOrVoidOrDoomEntry = null;
        EvolutionCombatEntry promSuffixEntry = null;
        float suffixDamage = 0.0F;
        float greatestFallOrVoidDmg = 0.0F;
        for (int i = 0, l = this.entries.size(); i < l; ++i) {
            EvolutionCombatEntry currentEntry = this.entries.get(i);
            EvolutionCombatEntry possibleDoomEntry = i > 0 ? this.entries.get(i - 1) : null;
            if ((currentEntry.getDamageSrc() == EvolutionDamage.FALL || currentEntry.getDamageSrc() == EvolutionDamage.VOID) &&
                currentEntry.getDamage() > 0.0F &&
                (promFallOrVoidOrDoomEntry == null || currentEntry.getDamageAmount() > greatestFallOrVoidDmg)) {
                if (i > 0) {
                    promFallOrVoidOrDoomEntry = possibleDoomEntry;
                }
                else {
                    promFallOrVoidOrDoomEntry = currentEntry;
                }
                greatestFallOrVoidDmg = currentEntry.getDamageAmount();
            }
            if (currentEntry.getFallSuffix() != null && (promSuffixEntry == null || currentEntry.getDamage() > suffixDamage)) {
                promSuffixEntry = currentEntry;
                suffixDamage = currentEntry.getDamage();
            }
        }
        if (greatestFallOrVoidDmg > 5.0F && promFallOrVoidOrDoomEntry != null) {
            return promFallOrVoidOrDoomEntry;
        }
        if (suffixDamage > 5.0F) {
            return promSuffixEntry;
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
        EvolutionCombatEntry bestEntry = this.getBestCombatEntry();
        EvolutionCombatEntry lastEntry = this.entries.get(this.entries.size() - 1);
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
        for (int i = 0, l = this.entries.size(); i < l; i++) {
            EvolutionCombatEntry entry = this.entries.get(i);
            Entity source = entry.getDamageSrc().getEntity();
            float dmg = entry.getDamage();
            if (source instanceof Player p && (player == null || dmg > playerDamage)) {
                playerDamage = dmg;
                player = p;
            }
            if (source instanceof LivingEntity e && (livingEntity == null || dmg > livingDamage)) {
                livingDamage = dmg;
                livingEntity = e;
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
    public void recheckStatus() {
        int time = this.inCombat ? 300 : 100;
        if (this.takingDamage && (this.fighter.deathTime > 0 || this.fighter.tickCount - this.lastDamageTime > time)) {
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
    public void recordDamage(DamageSource source, float health, float damageAmount) {
        this.recheckStatus();
        EvolutionCombatEntry entry = new EvolutionCombatEntry(source, damageAmount, this.lastSuffix != null ? this.lastSuffix.getFallSuffix() : null);
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

    public void setLastSuffix(@Nullable IFallSufixBlock lastSuffix) {
        this.lastSuffix = lastSuffix;
    }
}
