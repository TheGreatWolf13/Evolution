package tgw.evolution.mixin;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.gui.GUIUtils;
import tgw.evolution.client.util.CreativeTabs;
import tgw.evolution.client.util.MouseButton;
import tgw.evolution.network.Message;
import tgw.evolution.network.PacketCSSimpleMessage;
import tgw.evolution.util.collection.lists.OList;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Unique private static final int LAST_PAGE = Mth.ceil((CreativeTabs.size() - 3) / 9.0f) - 1;
    @Shadow private static int selectedTab;
    @Shadow @Final private static ResourceLocation CREATIVE_TABS_LOCATION;
    @Shadow @Final private static Component TRASH_SLOT_TOOLTIP;
    @Unique private static int currentPage;
    @Shadow private @Nullable Slot destroyItemSlot;
    @Shadow private boolean hasClickedOutside;
    @Unique private Button leftArrow;
    @Shadow private CreativeInventoryListener listener;
    @Unique private Button rightArrow;
    @Shadow private float scrollOffs;
    @Shadow private boolean scrolling;
    @Shadow private EditBox searchBox;
    @Shadow @Final private Set<TagKey<Item>> visibleTags;

    public MixinCreativeModeInventoryScreen(CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Overwrite
    private boolean canScroll() {
        return selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && CreativeTabs.get(selectedTab).canScroll() && this.menu.canScroll();
    }

    @Shadow
    protected abstract boolean checkTabClicked(CreativeModeTab creativeModeTab, double d, double e);

    @Shadow
    protected abstract boolean checkTabHovering(PoseStack poseStack, CreativeModeTab creativeModeTab, int i, int j);

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

    @Override
    @Overwrite
    public boolean hasClickedOutside(double mouseX, double mouseY, int leftPos, int topPos, @MouseButton int button) {
        boolean bl = mouseX < leftPos || mouseY < topPos || mouseX >= leftPos + this.imageWidth || mouseY >= topPos + this.imageHeight;
        this.hasClickedOutside = bl && !this.checkTabClicked(CreativeTabs.get(selectedTab), mouseX, mouseY);
        return this.hasClickedOutside;
    }

    @Overwrite
    @Override
    public void init() {
        assert this.minecraft != null;
        assert this.minecraft.gameMode != null;
        assert this.minecraft.player != null;
        if (this.minecraft.gameMode.hasInfiniteItems()) {
            super.init();
            this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
            this.leftArrow = this.addRenderableWidget(new Button(this.leftPos - 22, this.topPos - 22, 20, 20, new TextComponent("<"), b -> this.setPage(currentPage - 1)));
            this.rightArrow = this.addRenderableWidget(new Button(this.leftPos + this.imageWidth + 2, this.topPos - 22, 20, 20, new TextComponent(">"), b -> this.setPage(currentPage + 1)));
            this.setPage(currentPage);
            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, new TranslatableComponent("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(0xff_ffff);
            this.addWidget(this.searchBox);
            int i = selectedTab;
            selectedTab = -1;
            this.selectTab(CreativeTabs.get(i));
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
        }
        else {
            this.minecraft.player.connection.send(new PacketCSSimpleMessage(Message.C2S.OPEN_INVENTORY));
        }
    }

    @Shadow
    protected abstract boolean insideScrollbar(double d, double e);

    @Override
    @Overwrite
    public boolean mouseClicked(double mouseX, double mouseY, @MouseButton int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            double x = mouseX - this.leftPos;
            double y = mouseY - this.topPos;
            OList<CreativeModeTab> tabs = CreativeTabs.tabs();
            for (int i = 0; i < 3; ++i) {
                if (this.checkTabClicked(tabs.get(i), x, y)) {
                    return true;
                }
            }
            int start = currentPage * 9 + 3;
            for (int i = start, len = Math.min(tabs.size(), start + 9); i < len; ++i) {
                if (this.checkTabClicked(tabs.get(i), x, y)) {
                    return true;
                }
            }
            if (selectedTab != CreativeModeTab.TAB_INVENTORY.getId() && this.insideScrollbar(mouseX, mouseY)) {
                this.scrolling = this.canScroll();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @Overwrite
    public boolean mouseReleased(double mouseX, double mouseY, @MouseButton int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            double x = mouseX - this.leftPos;
            double y = mouseY - this.topPos;
            this.scrolling = false;
            OList<CreativeModeTab> tabs = CreativeTabs.tabs();
            for (int i = 0; i < 3; ++i) {
                CreativeModeTab tab = tabs.get(i);
                if (this.checkTabClicked(tab, x, y)) {
                    this.selectTab(tab);
                    return true;
                }
            }
            int start = currentPage * 9 + 3;
            for (int i = start, len = Math.min(tabs.size(), start + 9); i < len; ++i) {
                CreativeModeTab tab = tabs.get(i);
                if (this.checkTabClicked(tab, x, y)) {
                    this.selectTab(tab);
                    return true;
                }
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @Overwrite
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        drawCenteredString(matrices, this.font, new TextComponent((currentPage + 1) + " / " + (LAST_PAGE + 1)), this.leftPos + this.imageWidth / 2, this.topPos - 40, 0xff_ffff);
        super.render(matrices, mouseX, mouseY, partialTicks);
        OList<CreativeModeTab> tabs = CreativeTabs.tabs();
        for (int i = 0; i < 3; ++i) {
            if (this.checkTabHovering(matrices, tabs.get(i), mouseX, mouseY)) {
                break;
            }
        }
        int start = currentPage * 9 + 3;
        for (int i = start, len = Math.min(tabs.size(), start + 9); i < len; ++i) {
            if (this.checkTabHovering(matrices, tabs.get(i), mouseX, mouseY)) {
                break;
            }
        }
        if (this.destroyItemSlot != null && selectedTab == CreativeModeTab.TAB_INVENTORY.getId() && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, mouseX, mouseY)) {
            this.renderTooltip(matrices, TRASH_SLOT_TOOLTIP, mouseX, mouseY);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    @Overwrite
    public void renderBg(PoseStack matrices, float partialTicks, int mouseX, int mouseY) {
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        CreativeModeTab selected = CreativeTabs.get(selectedTab);
        OList<CreativeModeTab> tabs = CreativeTabs.tabs();
        for (int i = 0; i < 3; ++i) {
            CreativeModeTab tab = tabs.get(i);
            AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
            RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION);
            if (tab != selected) {
                this.renderTabButton(matrices, tab);
            }
        }
        int start = currentPage * 9 + 3;
        for (int i = start, len = Math.min(tabs.size(), start + 9); i < len; ++i) {
            CreativeModeTab tab = tabs.get(i);
            AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
            RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION);
            if (tab != selected) {
                this.renderTabButton(matrices, tab);
            }
        }
        AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
        RenderSystem.setShaderTexture(0, new ResourceLocation("textures/gui/container/creative_inventory/tab_" + selected.getBackgroundSuffix()));
        this.blit(matrices, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.searchBox.render(matrices, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        AccessorRenderSystem.setShader(GameRenderer.getPositionTexShader());
        RenderSystem.setShaderTexture(0, CREATIVE_TABS_LOCATION);
        if (selected.canScroll()) {
            this.blit(matrices, this.leftPos + 175, this.topPos + 18 + (int) (95 * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
        }
        this.renderTabButton(matrices, selected);
        if (selected == CreativeModeTab.TAB_INVENTORY) {
            GUIUtils.renderEntityInInventory(this.leftPos + 88, this.topPos + 45, 20, this.leftPos + 88 - mouseX, this.topPos + 45 - 30 - mouseY, this.minecraft.player);
        }
    }

    @Override
    @Overwrite
    public void renderLabels(PoseStack poseStack, int i, int j) {
        CreativeModeTab selected = CreativeTabs.get(selectedTab);
        if (selected.showTitle()) {
            RenderSystem.disableBlend();
            this.font.draw(poseStack, selected.getDisplayName(), 8.0F, 6.0F, 0x40_4040);
        }
    }

    @Shadow
    protected abstract void renderTabButton(PoseStack poseStack, CreativeModeTab creativeModeTab);

    @Override
    @Overwrite
    public void renderTooltip(PoseStack matrices, ItemStack stack, int mouseX, int mouseY) {
        assert this.minecraft != null;
        if (selectedTab == CreativeModeTab.TAB_SEARCH.getId()) {
            List<Component> list = stack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
            List<Component> list2 = Lists.newArrayList((Iterable) list);
            Item item = stack.getItem();
            CreativeModeTab tab = item.getItemCategory();
            if (tab == null && stack.is(Items.ENCHANTED_BOOK)) {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
                if (map.size() == 1) {
                    Enchantment enchantment = map.keySet().iterator().next();
                    OList<CreativeModeTab> tabs = CreativeTabs.tabs();
                    for (int i = 0, len = tabs.size(); i < len; ++i) {
                        CreativeModeTab creativeModeTab = tabs.get(i);
                        if (creativeModeTab.hasEnchantmentCategory(enchantment.category)) {
                            tab = creativeModeTab;
                            break;
                        }
                    }
                }
            }
            this.visibleTags.forEach(tagKey -> {
                if (stack.is(tagKey)) {
                    list2.add(1, new TextComponent("#" + tagKey.location()).withStyle(ChatFormatting.DARK_PURPLE));
                }
            });
            if (tab != null) {
                list2.add(1, tab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }
            this.renderTooltip(matrices, list2, stack.getTooltipImage(), mouseX, mouseY);
        }
        else {
            super.renderTooltip(matrices, stack, mouseX, mouseY);
        }
    }

    @Shadow
    protected abstract void selectTab(CreativeModeTab creativeModeTab);

    @Unique
    private void setPage(int page) {
        page = Mth.clamp(page, 0, LAST_PAGE);
        currentPage = page;
        this.leftArrow.active = page != 0;
        this.rightArrow.active = page != LAST_PAGE;
        switch (selectedTab) {
            case 0, 1, 2 -> {
                //Don't need to do anything as these tabs exist in every page
            }
            default -> {
                int relSelected = (selectedTab - 3) % 9;
                int newSelected = page * 9 + relSelected + 3;
                if (newSelected >= CreativeTabs.size()) {
                    newSelected = CreativeTabs.size() - 1;
                }
                this.selectTab(CreativeTabs.get(newSelected));
            }
        }
    }
}
