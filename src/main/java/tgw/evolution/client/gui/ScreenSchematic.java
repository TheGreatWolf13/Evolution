package tgw.evolution.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.blocks.tileentities.SchematicMode;
import tgw.evolution.blocks.tileentities.TESchematic;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSUpdateSchematicBlock;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ScreenSchematic extends Screen {

    private final TESchematic tile;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private SchematicMode mode = SchematicMode.SAVE;
    private boolean ignoreEntities;
    private boolean showAir;
    private boolean showBoundingBox;
    private TextFieldWidget nameEdit;
    private TextFieldWidget posXEdit;
    private TextFieldWidget posYEdit;
    private TextFieldWidget posZEdit;
    private TextFieldWidget sizeXEdit;
    private TextFieldWidget sizeYEdit;
    private TextFieldWidget sizeZEdit;
    private TextFieldWidget integrityEdit;
    private TextFieldWidget seedEdit;
    private Button saveButton;
    private Button loadButton;
    private Button rotateZeroDegreesButton;
    private Button rotateNinetyDegreesButton;
    private Button rotate180DegreesButton;
    private Button rotate270DegressButton;
    private Button modeButton;
    private Button detectSizeButton;
    private Button showEntitiesButton;
    private Button mirrorButton;
    private Button showAirButton;
    private Button showBoundingBoxButton;

    public ScreenSchematic(TESchematic tile) {
        super(new TranslationTextComponent(EvolutionBlocks.SCHEMATIC_BLOCK.get().getTranslationKey()));
        this.tile = tile;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    public static void open(TESchematic tile) {
        Minecraft.getInstance().displayGuiScreen(new ScreenSchematic(tile));
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

    private void done() {
        if (this.sendUpdates(TESchematic.UpdateCommand.UPDATE_DATA)) {
            this.minecraft.displayGuiScreen(null);
        }
    }

    private void cancel() {
        this.tile.setMirror(this.mirror);
        this.tile.setRotation(this.rotation);
        this.tile.setMode(this.mode);
        this.tile.setIgnoresEntities(this.ignoreEntities);
        this.tile.setShowAir(this.showAir);
        this.tile.setShowBoundingBox(this.showBoundingBox);
        this.minecraft.displayGuiScreen(null);
    }

    @Override
    protected void init() {
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.addButton(new Button(this.width / 2 - 4 - 150, 210, 150, 20, I18n.format("gui.done"), button -> this.done()));
        this.addButton(new Button(this.width / 2 + 4, 210, 150, 20, I18n.format("gui.cancel"), button -> this.cancel()));
        this.saveButton = this.addButton(new Button(this.width / 2 + 4 + 100, 185, 50, 20, I18n.format("structure_block.button.save"), button -> {
            if (this.tile.getMode() == SchematicMode.SAVE) {
                this.sendUpdates(TESchematic.UpdateCommand.SAVE_AREA);
                this.minecraft.displayGuiScreen(null);
            }
        }));
        this.loadButton = this.addButton(new Button(this.width / 2 + 4 + 100, 185, 50, 20, I18n.format("structure_block.button.load"), button -> {
            if (this.tile.getMode() == SchematicMode.LOAD) {
                this.sendUpdates(TESchematic.UpdateCommand.LOAD_AREA);
                this.minecraft.displayGuiScreen(null);
            }
        }));
        this.modeButton = this.addButton(new Button(this.width / 2 - 4 - 150, 185, 50, 20, "MODE", button -> {
            this.tile.nextMode();
            this.updateMode();
        }));
        this.detectSizeButton = this.addButton(new Button(this.width / 2 + 4 + 100, 120, 50, 20, I18n.format("structure_block.button.detect_size"), button -> {
            if (this.tile.getMode() == SchematicMode.SAVE) {
                this.sendUpdates(TESchematic.UpdateCommand.SCAN_AREA);
                this.minecraft.displayGuiScreen(null);
            }
        }));
        this.showEntitiesButton = this.addButton(new Button(this.width / 2 + 4 + 100, 160, 50, 20, "ENTITIES", button -> {
            this.tile.setIgnoresEntities(!this.tile.ignoresEntities());
            this.updateEntitiesButton();
        }));
        this.mirrorButton = this.addButton(new Button(this.width / 2 - 20, 185, 40, 20, "MIRROR", button -> {
            switch (this.tile.getMirror()) {
                case NONE:
                    this.tile.setMirror(Mirror.LEFT_RIGHT);
                    break;
                case LEFT_RIGHT:
                    this.tile.setMirror(Mirror.FRONT_BACK);
                    break;
                case FRONT_BACK:
                    this.tile.setMirror(Mirror.NONE);
            }
            this.updateMirrorButton();
        }));
        this.showAirButton = this.addButton(new Button(this.width / 2 + 4 + 100, 80, 50, 20, "SHOWAIR", button -> {
            this.tile.setShowAir(!this.tile.showsAir());
            this.updateToggleAirButton();
        }));
        this.showBoundingBoxButton = this.addButton(new Button(this.width / 2 + 4 + 100, 80, 50, 20, "SHOWBB", button -> {
            this.tile.setShowBoundingBox(!this.tile.showsBoundingBox());
            this.updateToggleBoundingBox();
        }));
        this.rotateZeroDegreesButton = this.addButton(new Button(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20, "0", button -> {
            this.tile.setRotation(Rotation.NONE);
            this.updateDirectionButtons();
        }));
        this.rotateNinetyDegreesButton = this.addButton(new Button(this.width / 2 - 1 - 40 - 20, 185, 40, 20, "90", button -> {
            this.tile.setRotation(Rotation.CLOCKWISE_90);
            this.updateDirectionButtons();
        }));
        this.rotate180DegreesButton = this.addButton(new Button(this.width / 2 + 1 + 20, 185, 40, 20, "180", button -> {
            this.tile.setRotation(Rotation.CLOCKWISE_180);
            this.updateDirectionButtons();
        }));
        this.rotate270DegressButton = this.addButton(new Button(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20, "270", button -> {
            this.tile.setRotation(Rotation.COUNTERCLOCKWISE_90);
            this.updateDirectionButtons();
        }));
        this.nameEdit = new TextFieldWidget(this.font, this.width / 2 - 152, 40, 300, 20, I18n.format("structure_block.structure_name")) {
            @Override
            public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
                return ScreenSchematic.this.isValidCharacterForName(this.getText(), p_charTyped_1_, this.getCursorPosition()) && super.charTyped(p_charTyped_1_, p_charTyped_2_);
            }
        };
        this.nameEdit.setMaxStringLength(64);
        this.nameEdit.setText(this.tile.getName());
        this.children.add(this.nameEdit);
        BlockPos blockpos = this.tile.getSchematicPos();
        this.posXEdit = new TextFieldWidget(this.font, this.width / 2 - 152, 80, 80, 20, I18n.format("structure_block.position.x"));
        this.posXEdit.setMaxStringLength(15);
        this.posXEdit.setText(Integer.toString(blockpos.getX()));
        this.children.add(this.posXEdit);
        this.posYEdit = new TextFieldWidget(this.font, this.width / 2 - 72, 80, 80, 20, I18n.format("structure_block.position.y"));
        this.posYEdit.setMaxStringLength(15);
        this.posYEdit.setText(Integer.toString(blockpos.getY()));
        this.children.add(this.posYEdit);
        this.posZEdit = new TextFieldWidget(this.font, this.width / 2 + 8, 80, 80, 20, I18n.format("structure_block.position.z"));
        this.posZEdit.setMaxStringLength(15);
        this.posZEdit.setText(Integer.toString(blockpos.getZ()));
        this.children.add(this.posZEdit);
        BlockPos blockpos1 = this.tile.getStructureSize();
        this.sizeXEdit = new TextFieldWidget(this.font, this.width / 2 - 152, 120, 80, 20, I18n.format("structure_block.size.x"));
        this.sizeXEdit.setMaxStringLength(15);
        this.sizeXEdit.setText(Integer.toString(blockpos1.getX()));
        this.children.add(this.sizeXEdit);
        this.sizeYEdit = new TextFieldWidget(this.font, this.width / 2 - 72, 120, 80, 20, I18n.format("structure_block.size.y"));
        this.sizeYEdit.setMaxStringLength(15);
        this.sizeYEdit.setText(Integer.toString(blockpos1.getY()));
        this.children.add(this.sizeYEdit);
        this.sizeZEdit = new TextFieldWidget(this.font, this.width / 2 + 8, 120, 80, 20, I18n.format("structure_block.size.z"));
        this.sizeZEdit.setMaxStringLength(15);
        this.sizeZEdit.setText(Integer.toString(blockpos1.getZ()));
        this.children.add(this.sizeZEdit);
        this.integrityEdit = new TextFieldWidget(this.font, this.width / 2 - 152, 120, 80, 20, I18n.format("structure_block.integrity.integrity"));
        this.integrityEdit.setMaxStringLength(15);
        this.integrityEdit.setText(this.decimalFormat.format(this.tile.getIntegrity()));
        this.children.add(this.integrityEdit);
        this.seedEdit = new TextFieldWidget(this.font, this.width / 2 - 72, 120, 80, 20, I18n.format("structure_block.integrity.seed"));
        this.seedEdit.setMaxStringLength(31);
        this.seedEdit.setText(Long.toString(this.tile.getSeed()));
        this.children.add(this.seedEdit);
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
        this.setFocusedDefault(this.nameEdit);
    }

    @Override
    public void resize(Minecraft p_resize_1_, int p_resize_2_, int p_resize_3_) {
        String s = this.nameEdit.getText();
        String s1 = this.posXEdit.getText();
        String s2 = this.posYEdit.getText();
        String s3 = this.posZEdit.getText();
        String s4 = this.sizeXEdit.getText();
        String s5 = this.sizeYEdit.getText();
        String s6 = this.sizeZEdit.getText();
        String s7 = this.integrityEdit.getText();
        String s8 = this.seedEdit.getText();
        this.init(p_resize_1_, p_resize_2_, p_resize_3_);
        this.nameEdit.setText(s);
        this.posXEdit.setText(s1);
        this.posYEdit.setText(s2);
        this.posZEdit.setText(s3);
        this.sizeXEdit.setText(s4);
        this.sizeYEdit.setText(s5);
        this.sizeZEdit.setText(s6);
        this.integrityEdit.setText(s7);
        this.seedEdit.setText(s8);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    private void updateEntitiesButton() {
        boolean flag = !this.tile.ignoresEntities();
        if (flag) {
            this.showEntitiesButton.setMessage(I18n.format("options.on"));
        }
        else {
            this.showEntitiesButton.setMessage(I18n.format("options.off"));
        }

    }

    private void updateToggleAirButton() {
        boolean flag = this.tile.showsAir();
        if (flag) {
            this.showAirButton.setMessage(I18n.format("options.on"));
        }
        else {
            this.showAirButton.setMessage(I18n.format("options.off"));
        }

    }

    private void updateToggleBoundingBox() {
        boolean flag = this.tile.showsBoundingBox();
        if (flag) {
            this.showBoundingBoxButton.setMessage(I18n.format("options.on"));
        }
        else {
            this.showBoundingBoxButton.setMessage(I18n.format("options.off"));
        }

    }

    private void updateMirrorButton() {
        Mirror mirror = this.tile.getMirror();
        switch (mirror) {
            case NONE:
                this.mirrorButton.setMessage("|");
                break;
            case LEFT_RIGHT:
                this.mirrorButton.setMessage("< >");
                break;
            case FRONT_BACK:
                this.mirrorButton.setMessage("^ v");
        }

    }

    private void updateDirectionButtons() {
        this.rotateZeroDegreesButton.active = true;
        this.rotateNinetyDegreesButton.active = true;
        this.rotate180DegreesButton.active = true;
        this.rotate270DegressButton.active = true;
        switch (this.tile.getRotation()) {
            case NONE:
                this.rotateZeroDegreesButton.active = false;
                break;
            case CLOCKWISE_180:
                this.rotate180DegreesButton.active = false;
                break;
            case COUNTERCLOCKWISE_90:
                this.rotate270DegressButton.active = false;
                break;
            case CLOCKWISE_90:
                this.rotateNinetyDegreesButton.active = false;
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
            case SAVE:
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
                break;
            case LOAD:
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
                break;
            case CORNER:
                this.nameEdit.setVisible(true);
                break;
        }
        this.modeButton.setMessage(I18n.format("structure_block.mode." + this.tile.getMode().getName()));
    }

    private boolean sendUpdates(TESchematic.UpdateCommand command) {
        BlockPos blockpos = new BlockPos(this.parseCoordinate(this.posXEdit.getText()), this.parseCoordinate(this.posYEdit.getText()), this.parseCoordinate(this.posZEdit.getText()));
        BlockPos blockpos1 = new BlockPos(this.parseCoordinate(this.sizeXEdit.getText()), this.parseCoordinate(this.sizeYEdit.getText()), this.parseCoordinate(this.sizeZEdit.getText()));
        float f = this.parseIntegrity(this.integrityEdit.getText());
        long i = this.parseSeed(this.seedEdit.getText());
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSUpdateSchematicBlock(this.tile.getPos(), command, this.tile.getMode(), this.nameEdit.getText(), blockpos, blockpos1, this.tile.getMirror(), this.tile.getRotation(), this.tile.ignoresEntities(), this.tile.showsAir(), this.tile.showsBoundingBox(), f, i));
        return true;
    }

    private long parseSeed(String seed) {
        try {
            return Long.parseLong(seed);
        }
        catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private float parseIntegrity(String integrity) {
        try {
            return Float.parseFloat(integrity);
        }
        catch (NumberFormatException exception) {
            return 1.0F;
        }
    }

    private int parseCoordinate(String coordinates) {
        try {
            return Integer.parseInt(coordinates);
        }
        catch (NumberFormatException exception) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        this.cancel();
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
            return true;
        }
        if (p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
            return false;
        }
        this.done();
        return true;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        SchematicMode structuremode = this.tile.getMode();
        this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 10, 16777215);
        if (true) {
            this.drawString(this.font, I18n.format("structure_block.structure_name"), this.width / 2 - 153, 30, 10526880);
            this.nameEdit.render(mouseX, mouseY, partialTicks);
        }
        if (structuremode == SchematicMode.LOAD || structuremode == SchematicMode.SAVE) {
            this.drawString(this.font, I18n.format("structure_block.position"), this.width / 2 - 153, 70, 10526880);
            this.posXEdit.render(mouseX, mouseY, partialTicks);
            this.posYEdit.render(mouseX, mouseY, partialTicks);
            this.posZEdit.render(mouseX, mouseY, partialTicks);
            String s = I18n.format("structure_block.include_entities");
            int i = this.font.getStringWidth(s);
            this.drawString(this.font, s, this.width / 2 + 154 - i, 150, 10526880);
        }
        if (structuremode == SchematicMode.SAVE) {
            this.drawString(this.font, I18n.format("structure_block.size"), this.width / 2 - 153, 110, 10526880);
            this.sizeXEdit.render(mouseX, mouseY, partialTicks);
            this.sizeYEdit.render(mouseX, mouseY, partialTicks);
            this.sizeZEdit.render(mouseX, mouseY, partialTicks);
            String s2 = I18n.format("structure_block.detect_size");
            int k = this.font.getStringWidth(s2);
            this.drawString(this.font, s2, this.width / 2 + 154 - k, 110, 10526880);
            String s1 = I18n.format("structure_block.show_air");
            int j = this.font.getStringWidth(s1);
            this.drawString(this.font, s1, this.width / 2 + 154 - j, 70, 10526880);
        }
        if (structuremode == SchematicMode.LOAD) {
            this.drawString(this.font, I18n.format("structure_block.integrity"), this.width / 2 - 153, 110, 10526880);
            this.integrityEdit.render(mouseX, mouseY, partialTicks);
            this.seedEdit.render(mouseX, mouseY, partialTicks);
            String s3 = I18n.format("structure_block.show_boundingbox");
            int l = this.font.getStringWidth(s3);
            this.drawString(this.font, s3, this.width / 2 + 154 - l, 70, 10526880);
        }
        String s4 = "structure_block.mode_info." + structuremode.getName();
        this.drawString(this.font, I18n.format(s4), this.width / 2 - 153, 174, 10526880);
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
