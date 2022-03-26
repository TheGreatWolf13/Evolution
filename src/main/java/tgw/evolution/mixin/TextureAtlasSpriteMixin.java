package tgw.evolution.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.ITextureAtlasSpritePatch;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin implements ITextureAtlasSpritePatch {

    private boolean active;

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
}
