package tgw.evolution.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSLungeAnim;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.reflection.MethodHandler;

public final class InputHooks {

    private static final MethodHandler<KeyBinding, Void> UNPRESS_KEY = new MethodHandler<>(KeyBinding.class, "func_74505_d");

    public static boolean isMainhandLungeInProgress;
    public static boolean isMainhandLunging;
    public static boolean isOffhandLungeInProgress;
    public static boolean isOffhandLunging;
    public static float lastMainhandLungeStrength;
    public static float lastOffhandLungeStrength;
    public static ItemStack mainhandLungingStack = ItemStack.EMPTY;
    public static ItemStack offhandLungingStack = ItemStack.EMPTY;
    public static boolean attackKeyReleased = true;
    public static int useKeyDownTicks;
    public static boolean useKeyReleased = true;
    public static int parryCooldown;
    private static int attackKeyDownTicks;

    private InputHooks() {
    }

    public static void attackKeyDown() {
        attackKeyDownTicks++;
    }

    public static int getAttackKeyDownTicks() {
        return attackKeyDownTicks;
    }

    public static int getLungeTime(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return attackKeyDownTicks;
        }
        return useKeyDownTicks;
    }

    public static void leftLunge(Minecraft mc, int minTime, int fullTime) {
        isMainhandLungeInProgress = false;
        isMainhandLunging = true;
        lastMainhandLungeStrength = MathHelper.relativize(attackKeyDownTicks, minTime, fullTime);
        mainhandLungingStack = mc.player.getMainHandItem();
        ClientEvents.addLungingPlayer(mc.player.getId(), Hand.MAIN_HAND);
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSLungeAnim(Hand.MAIN_HAND));
    }

    public static void parryCooldownTick() {
        if (parryCooldown > 0) {
            parryCooldown--;
        }
    }

    public static void releaseAttack(KeyBinding attack) {
        if (attackKeyDownTicks != 0) {
            UNPRESS_KEY.call(attack);
            attackKeyDownTicks = 0;
        }
    }

    public static void rightLunge(Minecraft mc, int minTime, int fullTime) {
        isOffhandLungeInProgress = false;
        isOffhandLunging = true;
        lastOffhandLungeStrength = MathHelper.relativize(useKeyDownTicks, minTime, fullTime);
        offhandLungingStack = mc.player.getOffhandItem();
        ClientEvents.addLungingPlayer(mc.player.getId(), Hand.OFF_HAND);
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSLungeAnim(Hand.OFF_HAND));
    }
}
