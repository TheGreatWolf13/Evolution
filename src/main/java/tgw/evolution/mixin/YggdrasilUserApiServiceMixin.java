package tgw.evolution.mixin;

import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import com.mojang.authlib.yggdrasil.response.UserAttributesResponse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.Executor;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(YggdrasilUserApiService.class)
public abstract class YggdrasilUserApiServiceMixin {

    @Redirect(method = "fetchProperties", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/yggdrasil/response" +
                                                                              "/UserAttributesResponse$Privileges;getTelemetry()Z", remap = false),
            remap = false, require = 0)
    private boolean getTelemetry(UserAttributesResponse.Privileges privileges) {
        return false;
    }

    /**
     * @author The Great Wolf
     * <p>
     * Disable telemetry.
     */
    @Overwrite(remap = false)
    public TelemetrySession newTelemetrySession(final Executor executor) {
        return TelemetrySession.DISABLED;
    }
}
