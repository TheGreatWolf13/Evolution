package tgw.evolution.resources;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackSource;

public class BuiltinModResourcePackSource implements PackSource {
    private final String modId;

    public BuiltinModResourcePackSource(String modId) {
        this.modId = modId;
    }

    @Override
    public Component decorate(Component packName) {
        return new TranslatableComponent("pack.nameAndSource", packName, new TranslatableComponent("pack.source.builtin", this.modId)).withStyle(
                ChatFormatting.GRAY);
    }
}
