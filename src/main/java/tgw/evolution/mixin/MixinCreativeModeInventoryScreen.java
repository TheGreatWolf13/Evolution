package tgw.evolution.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.network.Message;
import tgw.evolution.network.PacketCSSimpleMessage;

import java.util.Objects;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow private static int selectedTab;
    @Shadow private CreativeInventoryListener listener;
    @Shadow private EditBox searchBox;

    public MixinCreativeModeInventoryScreen(CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu,
                                            Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    /**
     * @author TheGreatWolf
     * @reason Replace inventory screen
     */
    @Overwrite
    @Override
    public void containerTick() {
        super.containerTick();
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        assert this.minecraft.gameMode != null;
        if (!this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.player.connection.send(new PacketCSSimpleMessage(Message.C2S.OPEN_INVENTORY));
        }
        else {
            this.searchBox.tick();
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Replace inventory screen
     */
    @Overwrite
    @Override
    public void init() {
        assert this.minecraft != null;
        assert this.minecraft.gameMode != null;
        assert this.minecraft.player != null;
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            super.init();
            this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
            Objects.requireNonNull(this.font);
            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, new TranslatableComponent("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(0xff_ffff);
            this.addWidget(this.searchBox);
            int i = selectedTab;
            selectedTab = -1;
            this.selectTab(CreativeModeTab.TABS[i]);
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
        }
        else {
            this.minecraft.player.connection.send(new PacketCSSimpleMessage(Message.C2S.OPEN_INVENTORY));
        }
    }

    @Shadow
    protected abstract void selectTab(CreativeModeTab creativeModeTab);
}
