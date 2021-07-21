package tgw.evolution.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.ForgeHooks;
import tgw.evolution.Evolution;
import tgw.evolution.client.LungeChargeInfo;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.items.ILunge;
import tgw.evolution.items.IOffhandAttackable;
import tgw.evolution.items.IParry;
import tgw.evolution.network.PacketCSLungeAnim;
import tgw.evolution.network.PacketCSStartLunge;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.reflection.FieldHandler;

public final class InputHooks {

    private static final FieldHandler<Minecraft, Integer> RIGHT_CLICK_COUNTER = new FieldHandler<>(Minecraft.class, "field_71467_ac");
    public static boolean isMainhandLungeInProgress;
    public static boolean isMainhandLunging;
    public static boolean isOffhandLungeInProgress;
    public static boolean isOffhandLunging;
    public static float lastMainhandLungeStrength;
    public static float lastOffhandLungeStrength;
    public static ItemStack mainhandLungingStack = ItemStack.EMPTY;
    public static ItemStack offhandLungingStack = ItemStack.EMPTY;
    private static int attackKeyDownTicks;
    private static int useKeyDownTicks;
    private static boolean attackKeyReleased = true;
    private static boolean useKeyReleased = true;
    private static int parryCooldown;

    private InputHooks() {
    }

    public static void clickMouse(Minecraft mc) {
        if (ClientEvents.LEFT_COUNTER_FIELD.get(mc) <= 0) {
            if (mc.objectMouseOver == null) {
                Evolution.LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
                return;
            }
            else if (!mc.player.isRowingBoat()) {
                switch (mc.objectMouseOver.getType()) {
                    case ENTITY:
                        ClientEvents.getInstance().leftMouseClick();
                        break;
                    case BLOCK:
                        BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) mc.objectMouseOver;
                        BlockPos pos = blockRayTrace.getPos();
                        if (!mc.world.getBlockState(pos).isAir(mc.world, pos)) {
                            mc.playerController.clickBlock(pos, blockRayTrace.getFace());
                            ClientEvents.getInstance().swingArm(Hand.MAIN_HAND);
                            break;
                        }
                    case MISS:
                        ClientEvents.getInstance().leftMouseClick();
                        ForgeHooks.onEmptyLeftClick(mc.player);
                        break;
                }
            }
        }
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
        mainhandLungingStack = mc.player.getHeldItemMainhand();
        ClientEvents.addLungingPlayer(mc.player.getEntityId(), Hand.MAIN_HAND);
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSLungeAnim(Hand.MAIN_HAND));
    }

    public static void middleClickMouse(Minecraft mc) {
        if (mc.objectMouseOver != null && mc.objectMouseOver.getType() != RayTraceResult.Type.MISS) {
            ForgeHooks.onPickBlock(mc.objectMouseOver, mc.player, mc.world);
        }
    }

    public static void parryCooldownTick() {
        if (parryCooldown > 0) {
            parryCooldown--;
        }
    }

    /**
     * Hooks from {@link Minecraft#processKeyBinds()} func_184117_aA
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @EvolutionHook
    public static void processKeyBinds(Minecraft mc) {
        Item offhandItem = mc.player.getHeldItemOffhand().getItem();
        if (mc.player.isHandActive()) {
            if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
                mc.playerController.onStoppedUsingItem(mc.player);
            }
            while (mc.gameSettings.keyBindAttack.isPressed()) {
                //shield bash
            }
            while (mc.gameSettings.keyBindUseItem.isPressed()) {

            }
            while (mc.gameSettings.keyBindPickBlock.isPressed()) {

            }
        }
        else {
            Item mainhandItem = mc.player.getHeldItemMainhand().getItem();
            if (mainhandItem instanceof ILunge) {
                int lungeFullTime = ((ILunge) mainhandItem).getFullLungeTime();
                int lungeMinTime = ((ILunge) mainhandItem).getMinLungeTime();
                if (mc.gameSettings.keyBindAttack.isKeyDown()) {
                    if (ClientEvents.getInstance().getMainhandCooledAttackStrength(0.0f) >= 1.0f && attackKeyReleased) {
                        attackKeyDownTicks++;
                        ClientEvents.LEFT_COUNTER_FIELD.set(mc, 1);
                        if (attackKeyDownTicks >= lungeMinTime) {
                            if (!isMainhandLungeInProgress) {
                                isMainhandLungeInProgress = true;
                                LungeChargeInfo lunge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(mc.player.getEntityId());
                                if (lunge == null) {
                                    ClientEvents.ABOUT_TO_LUNGE_PLAYERS.put(mc.player.getEntityId(),
                                                                            new LungeChargeInfo(Hand.MAIN_HAND,
                                                                                                mc.player.getHeldItemMainhand(),
                                                                                                lungeFullTime - lungeMinTime));
                                }
                                else {
                                    lunge.addInfo(Hand.MAIN_HAND, mc.player.getHeldItemMainhand(), lungeFullTime - lungeMinTime);
                                }
                                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSStartLunge(Hand.MAIN_HAND, lungeFullTime - lungeMinTime));
                            }
                        }
                        if (attackKeyDownTicks >= lungeFullTime) {
                            leftLunge(mc, lungeMinTime, lungeFullTime);
                            attackKeyDownTicks = 0;
                            attackKeyReleased = false;
                        }
                    }
                }
                else {
                    attackKeyReleased = true;
                    if (attackKeyDownTicks > lungeMinTime) {
                        leftLunge(mc, lungeMinTime, lungeFullTime);
                    }
                    else if (attackKeyDownTicks > 0) {
                        isMainhandLungeInProgress = false;
                        clickMouse(mc);
                    }
                    attackKeyDownTicks = 0;
                }
            }
            else {
                isMainhandLungeInProgress = false;
                attackKeyDownTicks = 0;
                attackKeyReleased = true;
                while (mc.gameSettings.keyBindAttack.isPressed()) {
                    clickMouse(mc);
                }
            }
            if (offhandItem instanceof ILunge) {
                int lungeFullTime = ((ILunge) offhandItem).getFullLungeTime();
                int lungeMinTime = ((ILunge) offhandItem).getMinLungeTime();
                if (mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    if (ClientEvents.getInstance().getOffhandCooledAttackStrength(offhandItem, 0.0f) >= 1.0f && useKeyReleased) {
                        useKeyDownTicks++;
                        if (useKeyDownTicks >= lungeMinTime) {
                            if (!isOffhandLungeInProgress) {
                                isOffhandLungeInProgress = true;
                                LungeChargeInfo lunge = ClientEvents.ABOUT_TO_LUNGE_PLAYERS.get(mc.player.getEntityId());
                                if (lunge == null) {
                                    ClientEvents.ABOUT_TO_LUNGE_PLAYERS.put(mc.player.getEntityId(),
                                                                            new LungeChargeInfo(Hand.OFF_HAND,
                                                                                                mc.player.getHeldItemOffhand(),
                                                                                                lungeFullTime - lungeMinTime));
                                }
                                else {
                                    lunge.addInfo(Hand.OFF_HAND, mc.player.getHeldItemOffhand(), lungeFullTime - lungeMinTime);
                                }
                                EvolutionNetwork.INSTANCE.sendToServer(new PacketCSStartLunge(Hand.OFF_HAND, lungeFullTime - lungeMinTime));
                            }
                        }
                        if (useKeyDownTicks >= lungeFullTime) {
                            rightLunge(mc, lungeMinTime, lungeFullTime);
                            useKeyDownTicks = 0;
                            useKeyReleased = false;
                        }
                    }
                }
                else {
                    useKeyReleased = true;
                    if (useKeyDownTicks > lungeMinTime) {
                        rightLunge(mc, lungeMinTime, lungeFullTime);
                    }
                    else if (useKeyDownTicks > 0) {
                        isOffhandLungeInProgress = false;
                        rightClickMouse(mc);
                    }
                    useKeyDownTicks = 0;
                }
            }
            else {
                isOffhandLungeInProgress = false;
                useKeyDownTicks = 0;
                useKeyReleased = true;
                while (mc.gameSettings.keyBindUseItem.isPressed()) {
                    rightClickMouse(mc);
                }
            }
            while (mc.gameSettings.keyBindPickBlock.isPressed()) {
                middleClickMouse(mc);
            }
        }
        if (!(offhandItem instanceof IOffhandAttackable) &&
            mc.gameSettings.keyBindUseItem.isKeyDown() &&
            RIGHT_CLICK_COUNTER.get(mc) == 0 &&
            !mc.player.isHandActive()) {
            rightClickMouse(mc);
        }
        sendClickBlockToController(mc, mc.currentScreen == null && mc.gameSettings.keyBindAttack.isKeyDown() && mc.mouseHelper.isMouseGrabbed());
    }

    public static void rightClickMouse(Minecraft mc) {
        if (!mc.playerController.getIsHittingBlock()) {
            RIGHT_CLICK_COUNTER.set(mc, 4);
            if (!mc.player.isRowingBoat()) {
                if (mc.objectMouseOver == null) {
                    Evolution.LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
                    return;
                }
                for (Hand hand : Hand.values()) {
                    ItemStack stack = mc.player.getHeldItem(hand);
                    if (mc.objectMouseOver != null) {
                        switch (mc.objectMouseOver.getType()) {
                            case ENTITY:
                                EntityRayTraceResult entityRayTrace = (EntityRayTraceResult) mc.objectMouseOver;
                                Entity entity = entityRayTrace.getEntity();
                                if (mc.playerController.interactWithEntity(mc.player, entity, entityRayTrace, hand) == ActionResultType.SUCCESS) {
                                    return;
                                }
                                if (mc.playerController.interactWithEntity(mc.player, entity, hand) == ActionResultType.SUCCESS) {
                                    return;
                                }
                                break;
                            case BLOCK:
                                BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) mc.objectMouseOver;
                                int count = stack.getCount();
                                ActionResultType actionResult = mc.playerController.func_217292_a(mc.player, mc.world, hand, blockRayTrace);
                                if (actionResult == ActionResultType.SUCCESS) {
                                    mc.player.swingArm(hand);
                                    ClientEvents.getInstance().swingArm(hand);
                                    if (stack.getCount() != count || mc.playerController.isInCreativeMode()) {
                                        mc.gameRenderer.itemRenderer.resetEquippedProgress(hand);
                                        ClientRenderer.instance.resetEquipProgress(hand);
                                    }
                                    return;
                                }
                                if (actionResult == ActionResultType.FAIL) {
                                    return;
                                }
                        }
                    }
                }
                ItemStack stackOffhand = mc.player.getHeldItemOffhand();
                Item itemOffhand = stackOffhand.getItem();
                if (itemOffhand instanceof IOffhandAttackable) {
                    ClientEvents.getInstance().rightMouseClick((IOffhandAttackable) itemOffhand);
                    return;
                }
                boolean isLungingMainhand = isMainhandLungeInProgress || isMainhandLunging;
                boolean isOffhandShield = mc.player.getHeldItemOffhand().isShield(mc.player);
                for (Hand hand : MathHelper.HANDS_LEFT_PRIORITY) {
                    ItemStack stack = mc.player.getHeldItem(hand);
                    if (stack.isEmpty() && (mc.objectMouseOver == null || mc.objectMouseOver.getType() == RayTraceResult.Type.MISS)) {
                        ForgeHooks.onEmptyClick(mc.player, hand);
                    }
                    if (hand == Hand.MAIN_HAND && (isLungingMainhand || ClientEvents.getInstance().getMainhandCooledAttackStrength(0.0f) < 1.0f)) {
                        return;
                    }
                    if (isLungingMainhand && isOffhandShield) {
                        return;
                    }
                    if (stack.getItem() instanceof IParry) {
                        if (parryCooldown > 0) {
                            return;
                        }
                    }
                    if (!stack.isEmpty() && mc.playerController.processRightClick(mc.player, mc.world, hand) == ActionResultType.SUCCESS) {
                        if (stack.getItem() instanceof IParry) {
                            parryCooldown = 6;
                        }
                        return;
                    }
                }
            }
        }
    }

    public static void rightLunge(Minecraft mc, int minTime, int fullTime) {
        isOffhandLungeInProgress = false;
        isOffhandLunging = true;
        lastOffhandLungeStrength = MathHelper.relativize(useKeyDownTicks, minTime, fullTime);
        offhandLungingStack = mc.player.getHeldItemOffhand();
        ClientEvents.addLungingPlayer(mc.player.getEntityId(), Hand.OFF_HAND);
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSLungeAnim(Hand.OFF_HAND));
    }

    public static void sendClickBlockToController(Minecraft mc, boolean leftClick) {
        if (!leftClick) {
            ClientEvents.LEFT_COUNTER_FIELD.set(mc, 0);
        }
        if (ClientEvents.LEFT_COUNTER_FIELD.get(mc) <= 0 && !mc.player.isHandActive()) {
            if (leftClick &&
                ClientEvents.getInstance().getObjectMouseOver() != null &&
                ClientEvents.getInstance().getObjectMouseOver().getType() == RayTraceResult.Type.BLOCK) {
                BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) ClientEvents.getInstance().getObjectMouseOver();
                BlockPos pos = blockRayTrace.getPos();
                if (!mc.world.isAirBlock(pos)) {
                    Direction face = blockRayTrace.getFace();
                    if (mc.playerController.onPlayerDamageBlock(pos, face)) {
                        mc.particles.addBlockHitEffects(pos, blockRayTrace);
                        mc.player.swingArm(Hand.MAIN_HAND);
                        ClientEvents.getInstance().swingArm(Hand.MAIN_HAND);
                    }
                }
            }
            else {
                mc.playerController.resetBlockRemoving();
            }
        }
    }
}
