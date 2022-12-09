package tgw.evolution.mixin;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.evolution.patches.obj.PixelDirection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(ItemModelGenerator.class)
public abstract class ItemModelGeneratorMixin {

    @SuppressWarnings("ObjectAllocationInLoop")
    private static List<BlockElement> createPixelLayerElements(int layer, String key, TextureAtlasSprite sprite) {
        List<BlockElement> elements = new ArrayList<>();
        int width = sprite.getWidth();
        int height = sprite.getHeight();
        float xFactor = width / 16.0F;
        float yFactor = height / 16.0F;
        int[] frames = sprite.getUniqueFrames().toArray();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (!isPixelAlwaysTransparent(sprite, frames, x, y)) {
                    Map<Direction, BlockElementFace> faces = new EnumMap<>(Direction.class);
                    BlockElementFace face = new BlockElementFace(null, layer, key, new BlockFaceUV(
                            new float[]{x / xFactor, y / yFactor, (x + 1) / xFactor, (y + 1) / yFactor}, 0));
                    BlockElementFace flippedFace = new BlockElementFace(null, layer, key, new BlockFaceUV(
                            new float[]{(x + 1) / xFactor, y / yFactor, x / xFactor, (y + 1) / yFactor}, 0));
                    faces.put(Direction.SOUTH, face);
                    faces.put(Direction.NORTH, flippedFace);
                    for (PixelDirection pixelDirection : PixelDirection.VALUES) {
                        if (doesPixelHaveEdge(sprite, frames, x, y, pixelDirection)) {
                            faces.put(pixelDirection.getDirection(), pixelDirection.isVertical() ? face : flippedFace);
                        }
                    }
                    elements.add(new BlockElement(new Vector3f(x / xFactor, (height - (y + 1)) / yFactor, 7.5F),
                                                  new Vector3f((x + 1) / xFactor, (height - y) / yFactor, 8.5F), faces, null, true));
                }
            }
        }
        return elements;
    }

    private static boolean doesPixelHaveEdge(TextureAtlasSprite sprite, int[] frames, int x, int y, PixelDirection direction) {
        int x1 = x + direction.getOffsetX();
        int y1 = y + direction.getOffsetY();
        if (isPixelOutsideSprite(sprite, x1, y1)) {
            return true;
        }
        for (int frame : frames) {
            if (!isPixelTransparent(sprite, frame, x, y) && isPixelTransparent(sprite, frame, x1, y1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPixelAlwaysTransparent(TextureAtlasSprite sprite, int[] frames, int x, int y) {
        for (int frame : frames) {
            if (!isPixelTransparent(sprite, frame, x, y)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPixelOutsideSprite(TextureAtlasSprite sprite, int x, int y) {
        return x < 0 || y < 0 || x >= sprite.getWidth() || y >= sprite.getHeight();
    }

    private static boolean isPixelTransparent(TextureAtlasSprite sprite, int frame, int x, int y) {
        return isPixelOutsideSprite(sprite, x, y) || sprite.isTransparent(frame, x, y);
    }

    @Inject(at = @At(value = "HEAD"), method = "processFrames", cancellable = true)
    private void onHeadAddLayerElements(int layer, String key, TextureAtlasSprite sprite, CallbackInfoReturnable<List<BlockElement>> cir) {
        cir.setReturnValue(createPixelLayerElements(layer, key, sprite));
    }
}
