package tgw.evolution.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;
import tgw.evolution.client.gui.widgets.EditBoxAdv;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketCSUpdateSchematicBlock;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class ScreenSchematic extends Screen {

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");
    private final Component textDetectSize = new TranslatableComponent("evolution.gui.schematic.detectSize");
    private final Component textEntities = new TranslatableComponent("evolution.gui.schematic.entities");
    private final Component textIntegrity = new TranslatableComponent("evolution.gui.schematic.integrity");
    private final Component textLoad = new TranslatableComponent("evolution.gui.schematic.load");
    private final Component textMirror = new TranslatableComponent("evolution.gui.schematic.mirror");
    private final Component textMode = new TranslatableComponent("evolution.gui.schematic.mode");
    private final Component textName = new TranslatableComponent("evolution.gui.schematic.name");
    private final Component textPos = new TranslatableComponent("evolution.gui.schematic.pos");
    private final Component textSave = new TranslatableComponent("evolution.gui.schematic.save");
    private final Component textShowAir = new TranslatableComponent("evolution.gui.schematic.showAir");
    private final Component textShowBB = new TranslatableComponent("evolution.gui.schematic.showBB");
    private final Component textSize = new TranslatableComponent("evolution.gui.schematic.size");
    private final TESchematic tile;
    private Button detectSizeButton;
    private boolean ignoreEntities;
    private EditBoxAdv integrityEdit;
    private Button loadButton;
    private Mirror mirror = Mirror.NONE;
    private Button mirrorButton;
    private SchematicMode mode = SchematicMode.SAVE;
    private Button modeButton;
    private EditBoxAdv nameEdit;
    private EditBoxAdv posXEdit;
    private EditBoxAdv posYEdit;
    private EditBoxAdv posZEdit;
    private Button rotate180DegreesButton;
    private Button rotate270DegressButton;
    private Button rotateNinetyDegreesButton;
    private Button rotateZeroDegreesButton;
    private Rotation rotation = Rotation.NONE;
    private Button saveButton;
    private EditBoxAdv seedEdit;
    private boolean showAir;
    private Button showAirButton;
    private boolean showBoundingBox;
    private Button showBoundingBoxButton;
    private Button showEntitiesButton;
    private EditBoxAdv sizeXEdit;
    private EditBoxAdv sizeYEdit;
    private EditBoxAdv sizeZEdit;

    public ScreenSchematic(TESchematic tile) {
        super(new TranslatableComponent(EvolutionBlocks.SCHEMATIC_BLOCK.get().getDescriptionId()));
        this.tile = tile;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    public static void open(TESchematic tile) {
        Minecraft.getInstance().setScreen(new ScreenSchematic(tile));
    }

    private static int parseCoordinate(String coordinates) {
        try {
            return Integer.parseInt(coordinates);
        }
        catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static float parseIntegrity(String integrity) {
        try {
            return Float.parseFloat(integrity);
        }
        catch (NumberFormatException exception) {
            return 1.0F;
        }
    }

    private static long parseSeed(String seed) {
        try {
            return Long.parseLong(seed);
        }
        catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private void cancel() {
        this.tile.setMirror(this.mirror);
        this.tile.setRotation(this.rotation);
        this.tile.setMode(this.mode);
        this.tile.setIgnoresEntities(this.ignoreEntities);
        this.tile.setShowAir(this.showAir);
        this.tile.setShowBoundingBox(this.showBoundingBox);
        this.minecraft.setScreen(null);
    }

    private void done() {
        if (this.sendUpdates(TESchematic.UpdateCommand.UPDATE_DATA)) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addRenderableWidget(new Button(this.width / 2 - 4 - 150, 210, 150, 20, EvolutionTexts.GUI_GENERAL_DONE, button -> this.done()));
        this.addRenderableWidget(new Button(this.width / 2 + 4, 210, 150, 20, EvolutionTexts.GUI_GENERAL_CANCEL, button -> this.cancel()));
        this.saveButton = this.addRenderableWidget(new Button(this.width / 2 + 4 + 100, 185, 50, 20, this.textSave, button -> {
            if (this.tile.getMode() == SchematicMode.SAVE) {
                this.sendUpdates(TESchematic.UpdateCommand.SAVE_AREA);
                this.minecraft.setScreen(null);
            }
        }));
        this.loadButton = this.addRenderableWidget(new Button(this.width / 2 + 4 + 100, 185, 50, 20, this.textLoad, button -> {
            if (this.tile.getMode() == SchematicMode.LOAD) {
                this.sendUpdates(TESchematic.UpdateCommand.LOAD_AREA);
                this.minecraft.setScreen(null);
            }
        }));
        this.modeButton = this.addRenderableWidget(new Button(this.width / 2 - 4 - 150, 185, 50, 20, this.textMode, button -> {
            this.tile.nextMode();
            this.updateMode();
        }));
        this.detectSizeButton = this.addRenderableWidget(new Button(this.width / 2 + 4 + 100, 120, 50, 20, this.textDetectSize, button -> {
            if (this.tile.getMode() == SchematicMode.SAVE) {
                this.sendUpdates(TESchematic.UpdateCommand.SCAN_AREA);
                this.minecraft.setScreen(null);
            }
        }));
        this.showEntitiesButton = this.addRenderableWidget(new Button(this.width / 2 + 4 + 100, 160, 50, 20, this.textEntities, button -> {
            this.tile.setIgnoresEntities(!this.tile.ignoresEntities());
            this.updateEntitiesButton();
        }));
        this.mirrorButton = this.addRenderableWidget(new Button(this.width / 2 - 20, 185, 40, 20, this.textMirror, button -> {
            switch (this.tile.getMirror()) {
                case NONE -> this.tile.setMirror(Mirror.LEFT_RIGHT);
                case LEFT_RIGHT -> this.tile.setMirror(Mirror.FRONT_BACK);
                case FRONT_BACK -> this.tile.setMirror(Mirror.NONE);
            }
            this.updateMirrorButton();
        }));
        this.showAirButton = this.addRenderableWidget(new Button(this.width / 2 + 4 + 100, 80, 50, 20, this.textShowAir, button -> {
            this.tile.setShowAir(!this.tile.showsAir());
            this.updateToggleAirButton();
        }));
        this.showBoundingBoxButton = this.addRenderableWidget(new Button(this.width / 2 + 4 + 100, 80, 50, 20, this.textShowBB, button -> {
            this.tile.setShowBoundingBox(!this.tile.showsBoundingBox());
            this.updateToggleBoundingBox();
        }));
        this.rotateZeroDegreesButton = this.addRenderableWidget(new Button(this.width / 2 - 1 - 40 - 1 - 40 - 20,
                                                                           185,
                                                                           40,
                                                                           20,
                                                                           new TextComponent("0\u00B0"),
                                                                           button -> {
                                                                               this.tile.setRotation(Rotation.NONE);
                                                                               this.updateDirectionButtons();
                                                                           }));
        this.rotateNinetyDegreesButton = this.addRenderableWidget(new Button(this.width / 2 - 1 - 40 - 20,
                                                                             185,
                                                                             40,
                                                                             20,
                                                                             new TextComponent("90\u00B0"),
                                                                             button -> {
                                                                                 this.tile.setRotation(Rotation.CLOCKWISE_90);
                                                                                 this.updateDirectionButtons();
                                                                             }));
        this.rotate180DegreesButton = this.addRenderableWidget(new Button(this.width / 2 + 1 + 20,
                                                                          185,
                                                                          40,
                                                                          20,
                                                                          new TextComponent("180\u00B0"),
                                                                          button -> {
                                                                              this.tile.setRotation(Rotation.CLOCKWISE_180);
                                                                              this.updateDirectionButtons();
                                                                          }));
        this.rotate270DegressButton = this.addRenderableWidget(new Button(this.width / 2 + 1 + 40 + 1 + 20,
                                                                          185,
                                                                          40,
                                                                          20,
                                                                          new TextComponent("270\u00B0"),
                                                                          button -> {
                                                                              this.tile.setRotation(Rotation.COUNTERCLOCKWISE_90);
                                                                              this.updateDirectionButtons();
                                                                          }));
        this.nameEdit = new EditBoxAdv(this.font, this.width / 2 - 152, 40, 300, 20, EvolutionTexts.EMPTY) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return ScreenSchematic.this.isValidCharacterForName(this.getValue(), codePoint, this.getCursorPosition()) &&
                       super.charTyped(codePoint, modifiers);
            }
        };
        this.nameEdit.setMaxLength(64);
        this.nameEdit.setValue(this.tile.getName());
        this.addWidget(this.nameEdit);
        BlockPos schematicPos = this.tile.getSchematicPos();
        this.posXEdit = new EditBoxAdv(this.font, this.width / 2 - 152, 80, 80, 20, EvolutionTexts.EMPTY);
        this.posXEdit.setMaxLength(15);
        this.posXEdit.setValue(Integer.toString(schematicPos.getX()));
        this.addWidget(this.posXEdit);
        this.posYEdit = new EditBoxAdv(this.font, this.width / 2 - 72, 80, 80, 20, EvolutionTexts.EMPTY);
        this.posYEdit.setMaxLength(15);
        this.posYEdit.setValue(Integer.toString(schematicPos.getY()));
        this.addWidget(this.posYEdit);
        this.posZEdit = new EditBoxAdv(this.font, this.width / 2 + 8, 80, 80, 20, EvolutionTexts.EMPTY);
        this.posZEdit.setMaxLength(15);
        this.posZEdit.setValue(Integer.toString(schematicPos.getZ()));
        this.addWidget(this.posZEdit);
        Vec3i size = this.tile.getStructureSize();
        this.sizeXEdit = new EditBoxAdv(this.font, this.width / 2 - 152, 120, 80, 20, EvolutionTexts.EMPTY);
        this.sizeXEdit.setMaxLength(15);
        this.sizeXEdit.setValue(Integer.toString(size.getX()));
        this.addWidget(this.sizeXEdit);
        this.sizeYEdit = new EditBoxAdv(this.font, this.width / 2 - 72, 120, 80, 20, EvolutionTexts.EMPTY);
        this.sizeYEdit.setMaxLength(15);
        this.sizeYEdit.setValue(Integer.toString(size.getY()));
        this.addWidget(this.sizeYEdit);
        this.sizeZEdit = new EditBoxAdv(this.font, this.width / 2 + 8, 120, 80, 20, EvolutionTexts.EMPTY);
        this.sizeZEdit.setMaxLength(15);
        this.sizeZEdit.setValue(Integer.toString(size.getZ()));
        this.addWidget(this.sizeZEdit);
        this.integrityEdit = new EditBoxAdv(this.font, this.width / 2 - 152, 120, 80, 20, EvolutionTexts.EMPTY);
        this.integrityEdit.setMaxLength(15);
        this.integrityEdit.setValue(this.decimalFormat.format(this.tile.getIntegrity()));
        this.addWidget(this.integrityEdit);
        this.seedEdit = new EditBoxAdv(this.font, this.width / 2 - 72, 120, 80, 20, EvolutionTexts.EMPTY);
        this.seedEdit.setMaxLength(31);
        this.seedEdit.setValue(Long.toString(this.tile.getSeed()));
        this.addWidget(this.seedEdit);
        this.mirror = this.tile.getMirror();
        this.updateMirrorButton();
        this.rotation = this.tile.getRotation();
        this.updateDirectionButtons();
        this.mode = this.tile.getMode();
        this.updateMode();
        this.ignoreEntities = this.tile.ignoresEntities();
        this.updateEntitiesButton();
        this.showAir = this.tile.showsAir();
        this.updateToggleAirButton();
        this.showBoundingBox = this.tile.showsBoundingBox();
        this.updateToggleBoundingBox();
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
            return false;
        }
        this.done();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.integrityEdit.setFocus(false);
        this.nameEdit.setFocus(false);
        this.seedEdit.setFocus(false);
        this.posXEdit.setFocus(false);
        this.posYEdit.setFocus(false);
        this.posZEdit.setFocus(false);
        this.sizeXEdit.setFocus(false);
        this.sizeYEdit.setFocus(false);
        this.sizeZEdit.setFocus(false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.cancel();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        SchematicMode schematicMode = this.tile.getMode();
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 10, 0xa0_a0a0);
        drawString(matrices, this.font, this.textName, this.width / 2 - 153, 30, 0xa0_a0a0);
        this.nameEdit.render(matrices, mouseX, mouseY, partialTicks);
        if (schematicMode == SchematicMode.LOAD || schematicMode == SchematicMode.SAVE) {
            drawString(matrices, this.font, this.textPos, this.width / 2 - 153, 70, 0xa0_a0a0);
            this.posXEdit.render(matrices, mouseX, mouseY, partialTicks);
            this.posYEdit.render(matrices, mouseX, mouseY, partialTicks);
            this.posZEdit.render(matrices, mouseX, mouseY, partialTicks);
            int textWidth = this.font.width(this.textEntities);
            drawString(matrices, this.font, this.textEntities, this.width / 2 + 154 - textWidth, 150, 0xa0_a0a0);
        }
        if (schematicMode == SchematicMode.SAVE) {
            drawString(matrices, this.font, this.textSize, this.width / 2 - 153, 110, 0xa0_a0a0);
            this.sizeXEdit.render(matrices, mouseX, mouseY, partialTicks);
            this.sizeYEdit.render(matrices, mouseX, mouseY, partialTicks);
            this.sizeZEdit.render(matrices, mouseX, mouseY, partialTicks);
            int textWidth = this.font.width(this.textDetectSize);
            drawString(matrices, this.font, this.textDetectSize, this.width / 2 + 154 - textWidth, 110, 0xa0_a0a0);
            textWidth = this.font.width(this.textShowAir);
            drawString(matrices, this.font, this.textShowAir, this.width / 2 + 154 - textWidth, 70, 0xa0_a0a0);
        }
        if (schematicMode == SchematicMode.LOAD) {
            drawString(matrices, this.font, this.textIntegrity, this.width / 2 - 153, 110, 0xa0_a0a0);
            this.integrityEdit.render(matrices, mouseX, mouseY, partialTicks);
            this.seedEdit.render(matrices, mouseX, mouseY, partialTicks);
            int textWidth = this.font.width(this.textShowBB);
            drawString(matrices, this.font, this.textShowBB, this.width / 2 + 154 - textWidth, 70, 0xa0_a0a0);
        }
        String modeInfo = "evolution.gui.schematic.modeInfo." + schematicMode.getSerializedName();
        drawString(matrices, this.font, I18n.get(modeInfo), this.width / 2 - 153, 174, 0xa0_a0a0);
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String name = this.nameEdit.getValue();
        String posX = this.posXEdit.getValue();
        String posY = this.posYEdit.getValue();
        String posZ = this.posZEdit.getValue();
        String sizeX = this.sizeXEdit.getValue();
        String sizeY = this.sizeYEdit.getValue();
        String sizeZ = this.sizeZEdit.getValue();
        String integrity = this.integrityEdit.getValue();
        String seed = this.seedEdit.getValue();
        this.init(mc, width, height);
        this.nameEdit.setValue(name);
        this.posXEdit.setValue(posX);
        this.posYEdit.setValue(posY);
        this.posZEdit.setValue(posZ);
        this.sizeXEdit.setValue(sizeX);
        this.sizeYEdit.setValue(sizeY);
        this.sizeZEdit.setValue(sizeZ);
        this.integrityEdit.setValue(integrity);
        this.seedEdit.setValue(seed);
    }

    private boolean sendUpdates(TESchematic.UpdateCommand command) {
        BlockPos pos = new BlockPos(parseCoordinate(this.posXEdit.getValue()),
                                    parseCoordinate(this.posYEdit.getValue()),
                                    parseCoordinate(this.posZEdit.getValue()));
        BlockPos size = new BlockPos(parseCoordinate(this.sizeXEdit.getValue()),
                                     parseCoordinate(this.sizeYEdit.getValue()),
                                     parseCoordinate(this.sizeZEdit.getValue()));
        float integrity = parseIntegrity(this.integrityEdit.getValue());
        long seed = parseSeed(this.seedEdit.getValue());
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSUpdateSchematicBlock(this.tile.getBlockPos(),
                                                                                command,
                                                                                this.tile.getMode(),
                                                                                this.nameEdit.getValue(),
                                                                                pos,
                                                                                size,
                                                                                this.tile.getMirror(),
                                                                                this.tile.getRotation(),
                                                                                this.tile.ignoresEntities(),
                                                                                this.tile.showsAir(),
                                                                                this.tile.showsBoundingBox(),
                                                                                integrity,
                                                                                seed));
        return true;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.posXEdit.tick();
        this.posYEdit.tick();
        this.posZEdit.tick();
        this.sizeXEdit.tick();
        this.sizeYEdit.tick();
        this.sizeZEdit.tick();
        this.integrityEdit.tick();
        this.seedEdit.tick();
    }

    private void updateDirectionButtons() {
        this.rotateZeroDegreesButton.active = true;
        this.rotateNinetyDegreesButton.active = true;
        this.rotate180DegreesButton.active = true;
        this.rotate270DegressButton.active = true;
        switch (this.tile.getRotation()) {
            case NONE -> this.rotateZeroDegreesButton.active = false;
            case CLOCKWISE_180 -> this.rotate180DegreesButton.active = false;
            case COUNTERCLOCKWISE_90 -> this.rotate270DegressButton.active = false;
            case CLOCKWISE_90 -> this.rotateNinetyDegreesButton.active = false;
        }
    }

    private void updateEntitiesButton() {
        if (!this.tile.ignoresEntities()) {
            this.showEntitiesButton.setMessage(EvolutionTexts.GUI_GENERAL_ON);
        }
        else {
            this.showEntitiesButton.setMessage(EvolutionTexts.GUI_GENERAL_OFF);
        }
    }

    private void updateMirrorButton() {
        switch (this.tile.getMirror()) {
            case NONE -> this.mirrorButton.setMessage(new TextComponent("|"));
            case LEFT_RIGHT -> this.mirrorButton.setMessage(new TextComponent("< >"));
            case FRONT_BACK -> this.mirrorButton.setMessage(new TextComponent("^ v"));
        }
    }

    private void updateMode() {
        this.nameEdit.setVisible(false);
        this.posXEdit.setVisible(false);
        this.posYEdit.setVisible(false);
        this.posZEdit.setVisible(false);
        this.sizeXEdit.setVisible(false);
        this.sizeYEdit.setVisible(false);
        this.sizeZEdit.setVisible(false);
        this.integrityEdit.setVisible(false);
        this.seedEdit.setVisible(false);
        this.saveButton.visible = false;
        this.loadButton.visible = false;
        this.detectSizeButton.visible = false;
        this.showEntitiesButton.visible = false;
        this.mirrorButton.visible = false;
        this.rotateZeroDegreesButton.visible = false;
        this.rotateNinetyDegreesButton.visible = false;
        this.rotate180DegreesButton.visible = false;
        this.rotate270DegressButton.visible = false;
        this.showAirButton.visible = false;
        this.showBoundingBoxButton.visible = false;
        switch (this.tile.getMode()) {
            case SAVE -> {
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.sizeXEdit.setVisible(true);
                this.sizeYEdit.setVisible(true);
                this.sizeZEdit.setVisible(true);
                this.saveButton.visible = true;
                this.detectSizeButton.visible = true;
                this.showEntitiesButton.visible = true;
                this.showAirButton.visible = true;
            }
            case LOAD -> {
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.integrityEdit.setVisible(true);
                this.seedEdit.setVisible(true);
                this.loadButton.visible = true;
                this.showEntitiesButton.visible = true;
                this.mirrorButton.visible = true;
                this.rotateZeroDegreesButton.visible = true;
                this.rotateNinetyDegreesButton.visible = true;
                this.rotate180DegreesButton.visible = true;
                this.rotate270DegressButton.visible = true;
                this.showBoundingBoxButton.visible = true;
                this.updateDirectionButtons();
            }
            case CORNER -> this.nameEdit.setVisible(true);
        }
        this.modeButton.setMessage(new TranslatableComponent("evolution.gui.schematic.mode." + this.tile.getMode().getSerializedName()));
    }

    private void updateToggleAirButton() {
        if (this.tile.showsAir()) {
            this.showAirButton.setMessage(EvolutionTexts.GUI_GENERAL_ON);
        }
        else {
            this.showAirButton.setMessage(EvolutionTexts.GUI_GENERAL_OFF);
        }
    }

    private void updateToggleBoundingBox() {
        if (this.tile.showsBoundingBox()) {
            this.showBoundingBoxButton.setMessage(EvolutionTexts.GUI_GENERAL_ON);
        }
        else {
            this.showBoundingBoxButton.setMessage(EvolutionTexts.GUI_GENERAL_OFF);
        }
    }
}
