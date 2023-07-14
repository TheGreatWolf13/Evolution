package tgw.evolution.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.DataPackConfig;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.resources.ModResourcePackUtil;
import tgw.evolution.resources.ModdedPackSource;

import java.io.File;
import java.util.function.Supplier;

@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen extends Screen {

    @Shadow private @Nullable PackRepository tempDataPackRepository;

    public MixinCreateWorldScreen(Component component) {
        super(component);
    }

    @Redirect(method = "createFresh", at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"))
    private static <T> T loadDynamicRegistry(Supplier<T> instance) {
        RegistryAccess.Writable dynamicRegistryManager = RegistryAccess.builtinCopy();
        ModResourcePackUtil.loadDynamicRegistry(dynamicRegistryManager);
        return (T) dynamicRegistryManager.freeze();
    }

    @ModifyArg(method = {"createFresh", "createFromExisting"}, at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;" +
            "<init>(Lnet/minecraft/client/gui/screens/Screen;" +
            "Lnet/minecraft/world/level/DataPackConfig;" +
            "Lnet/minecraft/client/gui/screens/worldselection" +
            "/WorldGenSettingsComponent;)V"), index = 1)
    private static DataPackConfig onNew(DataPackConfig settings) {
        return ModResourcePackUtil.createDefaultDataPackSettings();
    }

    @Inject(method = "getDataPackSelectionSettings", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;" +
                                                                                         "reload()V", shift =
            At.Shift.BEFORE))
    private void onScanPacks(CallbackInfoReturnable<Pair<File, PackRepository>> cir) {
        // Allow to display built-in data packs in the data pack selection screen at world creation.
        assert this.tempDataPackRepository != null;
        this.tempDataPackRepository.sources.add(new ModdedPackSource(PackType.SERVER_DATA));
    }
}
