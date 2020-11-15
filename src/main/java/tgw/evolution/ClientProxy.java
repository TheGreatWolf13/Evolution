package tgw.evolution;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.client.gui.ScreenInventoryExtended;
import tgw.evolution.client.renderer.EvolutionRenderer;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.init.EvolutionContainers;

public class ClientProxy implements IProxy {

    public static final KeyBinding TOGGLE_PRONE = new KeyBinding("key.prone.toggle",
                                                                 KeyConflictContext.IN_GAME,
                                                                 InputMappings.Type.KEYSYM,
                                                                 GLFW.GLFW_KEY_X,
                                                                 "key.categories.movement");

    public static void changeWorldOrders() {
        int evId = 0;
        for (WorldType worldType : WorldType.WORLD_TYPES) {
            if (worldType != null && "ev_default".equals(worldType.getName())) {
                evId = worldType.getId();
                break;
            }
        }
        WorldType evWorld = WorldType.WORLD_TYPES[evId];
        System.arraycopy(WorldType.WORLD_TYPES, 0, WorldType.WORLD_TYPES, 1, evId);
        WorldType.WORLD_TYPES[0] = evWorld;
    }

    @Override
    public void init() {
        EvolutionRenderer.registryEntityRenders();
        ScreenManager.registerFactory(EvolutionContainers.EXTENDED_INVENTORY.get(), ScreenInventoryExtended::new);
        ColorManager.registerBlockColorHandlers(Minecraft.getInstance().getBlockColors());
        ColorManager.registerItemColorHandlers(Minecraft.getInstance().getItemColors());
        MinecraftForge.EVENT_BUS.register(new ClientEvents(Minecraft.getInstance()));
        ClientRegistry.registerKeyBinding(TOGGLE_PRONE);
        changeWorldOrders();
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
