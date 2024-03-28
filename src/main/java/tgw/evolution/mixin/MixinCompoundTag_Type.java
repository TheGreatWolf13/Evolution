package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.io.DataInput;
import java.io.IOException;

@Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
public abstract class MixinCompoundTag_Type {

    /**
     * @author TheGreatWolf
     * @reason Replace map
     */
    @Overwrite
    public CompoundTag load(DataInput input, int depth, NbtAccounter accounter) throws IOException {
        accounter.accountBits(384L);
        if (depth > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }
        O2OMap<String, Tag> map = new O2OHashMap<>();
        for (byte b = CompoundTag.readNamedTagType(input, accounter); b != 0; b = CompoundTag.readNamedTagType(input, accounter)) {
            String string = CompoundTag.readNamedTagName(input, accounter);
            accounter.accountBits(224 + 16L * string.length());
            Tag tag = CompoundTag.readNamedTagData(TagTypes.getType(b), string, input, depth + 1, accounter);
            if (map.put(string, tag) != null) {
                accounter.accountBits(288L);
            }
        }
        return new CompoundTag(map);
    }
}

