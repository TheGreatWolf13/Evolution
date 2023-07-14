package tgw.evolution.util.damage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2BHashMap;
import tgw.evolution.util.collection.maps.R2BMap;
import tgw.evolution.util.collection.maps.R2FHashMap;
import tgw.evolution.util.collection.maps.R2FMap;

public class EvolutionCombatTracker extends CombatTracker {
    private final R2BMap<EvolutionDamage.Type> damageImmunity = new R2BHashMap<>();
    private final OList<EvolutionCombatEntry> entries = new OArrayList<>();
    private final LivingEntity fighter;
    private final R2FMap<EvolutionDamage.Type> lastDamages = new R2FHashMap<>();
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

    public float accountForImmunity(DamageSource source, float amount) {
        if (!(source instanceof DamageSourceEv dmg)) {
            return amount;
        }
        EvolutionDamage.Type type = dmg.getType();
        byte maxImmunity = type.getImmunity();
        if (maxImmunity == 0) {
            return amount;
        }
        byte immunity = this.damageImmunity.getOrDefault(type, (byte) 0);
        if (immunity == 0) {
            this.damageImmunity.put(type, maxImmunity);
            this.lastDamages.put(type, amount);
            return amount;
        }
        float lastDmg = this.lastDamages.getOrDefault(type, 0);
        if (lastDmg >= amount) {
            return 0;
        }
        this.lastDamages.put(type, amount);
        return amount - lastDmg;
    }

    /**
     * @return Checks the previous CombatEntries to determine if the fighter fell before they were killed or if they were pushed to their death /
     * into the void.
     */
    private @Nullable EvolutionCombatEntry getBestCombatEntry() {
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
    public @Nullable LivingEntity getKiller() {
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

    public void readAdditional(CompoundTag tag) {
        if (tag.contains("RecordedDamages", Tag.TAG_LIST)) {
            ListTag list = tag.getList("RecordedDamages", Tag.TAG_COMPOUND);
            for (int i = 0, len = list.size(); i < len; i++) {
                CompoundTag t = list.getCompound(i);
                EvolutionDamage.Type type = EvolutionDamage.Type.byName(t.getString("Type"));
                if (type != null) {
                    byte immunity = t.getByte("Immunity");
                    if (immunity > 0) {
                        float dmg = t.getFloat("Damage");
                        if (dmg > 0) {
                            this.damageImmunity.put(type, immunity);
                            this.lastDamages.put(type, dmg);
                        }
                    }
                }
            }
        }
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

    public void saveAdditional(CompoundTag tag) {
        if (this.damageImmunity.isEmpty()) {
            return;
        }
        ListTag list = new ListTag();
        for (EvolutionDamage.Type type : EvolutionDamage.Type.VALUES) {
            byte immunity = this.damageImmunity.getOrDefault(type, (byte) 0);
            if (immunity > 0) {
                float dmg = this.lastDamages.getOrDefault(type, 0);
                if (dmg > 0) {
                    //noinspection ObjectAllocationInLoop
                    CompoundTag t = new CompoundTag();
                    t.putString("Type", type.getName());
                    t.putByte("Immunity", immunity);
                    t.putFloat("Damage", dmg);
                    list.add(t);
                }
            }
        }
        if (!list.isEmpty()) {
            tag.put("RecordedDamages", list);
        }
    }

    public void setLastSuffix(@Nullable IFallSufixBlock lastSuffix) {
        this.lastSuffix = lastSuffix;
    }

    public void tick() {
        if (this.damageImmunity.isEmpty()) {
            return;
        }
        for (EvolutionDamage.Type value : EvolutionDamage.Type.VALUES) {
            byte immunity = this.damageImmunity.getOrDefault(value, (byte) 0);
            if (--immunity <= 0) {
                this.damageImmunity.removeByte(value);
                this.lastDamages.removeFloat(value);
            }
            else {
                this.damageImmunity.put(value, immunity);
            }
        }
    }
}
