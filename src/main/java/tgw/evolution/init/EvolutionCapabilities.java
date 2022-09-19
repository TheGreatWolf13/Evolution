package tgw.evolution.init;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.NonNullSupplier;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.food.CapabilityHunger;
import tgw.evolution.capabilities.health.CapabilityHealth;
import tgw.evolution.capabilities.inventory.CapabilityInventory;
import tgw.evolution.capabilities.modular.CapabilityModular;
import tgw.evolution.capabilities.stamina.CapabilityStamina;
import tgw.evolution.capabilities.temperature.CapabilityTemperature;
import tgw.evolution.capabilities.thirst.CapabilityThirst;
import tgw.evolution.capabilities.toast.CapabilityToast;

public final class EvolutionCapabilities {

    private static final NonNullSupplier<IllegalStateException> EXCEPTION_MAKER = IllegalStateException::new;

    private EvolutionCapabilities() {
    }

    public static <T extends INBTSerializable<CompoundTag>> void clonePlayer(Player oldPlayer, Player newPlayer, Capability<T> cap) {
        try {
            T oldCap = getCapabilityOrThrow(oldPlayer, cap);
            T newCap = getCapabilityOrThrow(newPlayer, cap);
            newCap.deserializeNBT(oldCap.serializeNBT());
        }
        catch (Exception e) {
            Evolution.error("Could not clone {} for {}: ", cap.getName(), oldPlayer.getScoreboardName(), e);
        }
    }

    /**
     * Gets the holder object associated with this capability, if it's present, or {@code null}. Note that if the holder object does not exist, or the
     * capability was invalidated via
     * {@link net.minecraft.world.entity.LivingEntity#invalidateCaps}, the method will return {@code null}.
     */
    public static <T> T getCapability(ICapabilityProvider player, Capability<T> instance, T orElse) {
        return player.getCapability(instance).orElse(orElse);
    }

    /**
     * Gets the holder object associated with this capability. Note that if the holder object does not exist, or the capability was invalidated via
     * {@link net.minecraft.world.entity.LivingEntity#invalidateCaps}, the method will throw {@link IllegalStateException}.
     */
    public static <T> T getCapabilityOrThrow(ICapabilityProvider player, Capability<T> instance) {
        return player.getCapability(instance).orElseThrow(EXCEPTION_MAKER);
    }

    /**
     * Gets the holder object associated with this capability. Note that if the holder object does not exist, the method will throw
     * {@link IllegalStateException}. The method will still return if the capabilities were invalidated, however, reviving them.
     */
    public static <T> T getRevivedCapability(Player player, Capability<T> instance) {
        boolean shouldRevive = !player.isAlive();
        if (shouldRevive) {
            player.reviveCaps();
        }
        T capability = player.getCapability(instance).orElseThrow(EXCEPTION_MAKER);
        if (shouldRevive) {
            player.invalidateCaps();
        }
        return capability;
    }

    public static void invalidate(Player player) {
        if (!player.isAlive()) {
            player.invalidateCaps();
        }
    }

    public static void register(RegisterCapabilitiesEvent event) {
        CapabilityChunkStorage.register(event);
        CapabilityInventory.register(event);
        CapabilityThirst.register(event);
        CapabilityHealth.register(event);
        CapabilityToast.register(event);
        CapabilityHunger.register(event);
        CapabilityTemperature.register(event);
        CapabilityModular.register(event);
        CapabilityStamina.register(event);
    }

    public static void revive(Player player) {
        if (!player.isAlive()) {
            player.reviveCaps();
        }
    }
}
