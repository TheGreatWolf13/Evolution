package tgw.evolution.mixin;

import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.ClientRenderer;
import tgw.evolution.init.EvolutionResources;

import java.util.List;

@Mixin(PlayerTabOverlay.class)
public abstract class MixinPlayerTabOverlay extends GuiComponent {

    @Shadow @Final private static Ordering<PlayerInfo> PLAYER_ORDERING;
    @Shadow private @Nullable Component footer;
    @Shadow private @Nullable Component header;
    @Shadow @Final private Minecraft minecraft;

    @Shadow
    public abstract Component getNameForDisplay(PlayerInfo playerInfo);

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void render(PoseStack matrices, int screenWidth, Scoreboard scoreboard, @Nullable Objective objective) {
        assert this.minecraft.player != null;
        assert this.minecraft.level != null;
        ClientPacketListener connection = this.minecraft.player.connection;
        List<PlayerInfo> list = PLAYER_ORDERING.sortedCopy(connection.getOnlinePlayers());
        int nameLength = 0;
        int objLength = 0;
        boolean isObjectiveNotHearts = objective != null && objective.getRenderType() != ObjectiveCriteria.RenderType.HEARTS;
        for (int i = 0, len = list.size(); i < len; ++i) {
            PlayerInfo playerInfo = list.get(i);
            nameLength = Math.max(nameLength, this.minecraft.font.width(this.getNameForDisplay(playerInfo)));
            if (isObjectiveNotHearts) {
                //noinspection ObjectAllocationInLoop
                objLength = Math.max(objLength, this.minecraft.font.width(" " + scoreboard.getOrCreatePlayerScore(playerInfo.getProfile().getName(), objective).getScore()));
            }
        }
        int size = Math.min(list.size(), 80);
        int amountPerColumn = size;
        int columns;
        for (columns = 1; amountPerColumn > 20; amountPerColumn = (size + columns - 1) / columns) {
            ++columns;
        }
        assert this.minecraft.getConnection() != null;
        boolean singleplayer = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted();
        int objectiveSize;
        if (objective != null) {
            if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
                objectiveSize = 82;
            }
            else {
                objectiveSize = objLength;
            }
        }
        else {
            objectiveSize = 0;
        }
        int lineSize = Math.min(columns * ((singleplayer ? 9 : 0) + nameLength + objectiveSize + 40), screenWidth - 50) / columns;
        int q = screenWidth / 2 - (lineSize * columns + (columns - 1) * 5) / 2;
        int y = 10;
        int s = lineSize * columns + (columns - 1) * 5;
        List<FormattedCharSequence> headerList = null;
        if (this.header != null) {
            headerList = this.minecraft.font.split(this.header, screenWidth - 50);
            for (FormattedCharSequence formattedCharSequence : headerList) {
                s = Math.max(s, this.minecraft.font.width(formattedCharSequence));
            }
        }
        List<FormattedCharSequence> footerList = null;
        if (this.footer != null) {
            footerList = this.minecraft.font.split(this.footer, screenWidth - 50);
            for (FormattedCharSequence sequence : footerList) {
                s = Math.max(s, this.minecraft.font.width(sequence));
            }
        }
        if (headerList != null) {
            fill(matrices, screenWidth / 2 - s / 2 - 1, y - 1, screenWidth / 2 + s / 2 + 1, y + headerList.size() * 9, Integer.MIN_VALUE);
            for (FormattedCharSequence sequence : headerList) {
                this.minecraft.font.drawShadow(matrices, sequence, screenWidth / 2.0f - this.minecraft.font.width(sequence) / 2.0f, y, -1);
                y += 9;
            }
            ++y;
        }
        fill(matrices, screenWidth / 2 - s / 2 - 1, y - 1, screenWidth / 2 + s / 2 + 1, y + amountPerColumn * 9, Integer.MIN_VALUE);
        int u = this.minecraft.options.getBackgroundColor(0x20ff_ffff);
        for (int i = 0; i < size; ++i) {
            int t = i / amountPerColumn;
            int x0 = q + t * lineSize + t * 5;
            int y0 = y + i % amountPerColumn * 9;
            fill(matrices, x0, y0, x0 + lineSize, y0 + 8, u);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (i < list.size()) {
                PlayerInfo playerInfo = list.get(i);
                GameProfile gameProfile = playerInfo.getProfile();
                if (singleplayer) {
                    Player player = this.minecraft.level.getPlayerByUUID(gameProfile.getId());
                    boolean bl2 = player != null && LivingEntityRenderer.isEntityUpsideDown(player);
                    RenderSystem.setShaderTexture(0, playerInfo.getSkinLocation());
                    GuiComponent.blit(matrices, x0, y0, 8, 8,
                                      8,
                                      8 + (bl2 ? 8 : 0),
                                      8,
                                      8 * (bl2 ? -1 : 1), 64, 64
                    );
                    if (player != null && player.isModelPartShown(PlayerModelPart.HAT)) {
                        GuiComponent.blit(matrices, x0, y0, 8, 8,
                                          40,
                                          8 + (bl2 ? 8 : 0),
                                          8,
                                          8 * (bl2 ? -1 : 1), 64, 64
                        );
                    }
                    x0 += 9;
                }
                this.minecraft.font.drawShadow(matrices, this.getNameForDisplay(playerInfo), x0, y0, playerInfo.getGameMode() == GameType.SPECTATOR ? 0x90ff_ffff : 0xffff_ffff);
                if (objective != null && playerInfo.getGameMode() != GameType.SPECTATOR) {
                    int ad = x0 + nameLength + 1;
                    int ae = ad + objectiveSize;
                    if (ae - ad > 5) {
                        this.renderTablistScore(objective, y0, gameProfile.getName(), ad, ae, playerInfo, matrices);
                    }
                }
                this.renderPingIcon(matrices, lineSize, x0 - (singleplayer ? 9 : 0), y0, playerInfo);
            }
        }
        if (footerList != null) {
            y += amountPerColumn * 9 + 1;
            fill(matrices, screenWidth / 2 - s / 2 - 1, y - 1, screenWidth / 2 + s / 2 + 1, y + footerList.size() * 9, Integer.MIN_VALUE);
            for (FormattedCharSequence formattedCharSequence3 : footerList) {
                this.minecraft.font.drawShadow(matrices, formattedCharSequence3, (screenWidth - this.minecraft.font.width(formattedCharSequence3)) / 2.0f, y, -1);
                y += 9;
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void renderPingIcon(PoseStack matrices, int lineSize, int x, int y, PlayerInfo playerInfo) {
        int ping = playerInfo.getLatency();
        int color = 0xffff_0000;
        if (ping >= 0) {
            if (ping < 500) {
                float r = ping / 500.0f;
                color = 0xff00_ff00 | (int) (r * 255) << 16;
            }
            else if (ping < 1_000) {
                float g = 1 - (ping - 500) / 500.0f;
                color = 0xffff_0000 | (int) (g * 255) << 8;
            }
        }
        String pingStr = ping + "ms";
        Font font = this.minecraft.font;
        int offset = font.width(pingStr) + 2;
        if (offset > 38) {
            pingStr = "-";
            offset = (40 + font.width("-")) / 2;
        }
        font.drawShadow(matrices, pingStr, x + lineSize - offset, y, color);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void renderTablistScore(Objective objective, int y, String name, int x, int otherX, PlayerInfo info, PoseStack matrices) {
        int score = objective.getScoreboard().getOrCreatePlayerScore(name, objective).getScore();
        if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            assert this.minecraft.level != null;
            final int heartTextureYPos = this.minecraft.level.getLevelData().isHardcore() ? EvolutionResources.ICON_HEARTS_HARDCORE : EvolutionResources.ICON_HEARTS;
            RenderSystem.setShaderTexture(0, EvolutionResources.GUI_ICONS);
            int scoreAmount = ClientRenderer.roundToHearts(score);
            if (score > 0) {
                if (score <= 100) {
                    for (int currentHeart = 9; currentHeart >= 0; currentHeart--) {
                        int row = Mth.ceil((currentHeart + 1) / 10.0F) - 1;
                        int x0 = x + currentHeart % 10 * 8;
                        int y0 = y - row * 10;
                        this.blit(matrices, x0, y0, 0, heartTextureYPos, 9, 9);
                        if (scoreAmount > currentHeart * 4) {
                            int offset = switch (scoreAmount - currentHeart * 4) {
                                case 1 -> 9 * 3;
                                case 2 -> 9 * 2;
                                case 3 -> 9;
                                default -> 0;
                            };
                            this.blit(matrices, x0, y0, 9 * 2 + offset, heartTextureYPos, 9, 9);
                        }
                    }
                }
                else {
                    float f = Mth.clamp(score / 20.0F, 0.0F, 1.0F);
                    int r = (int) ((1.0F - f) * 255.0F) << 16 | (int) (f * 255.0F) << 8;
                    String hpString = String.valueOf(score);
                    if (otherX - this.minecraft.font.width(hpString + "HP") >= x) {
                        hpString += "HP";
                    }
                    this.minecraft.font.drawShadow(matrices, hpString, (otherX + x) / 2.0f - this.minecraft.font.width(hpString) / 2.0f, y, r);
                }
            }
        }
        else {
            String scoreString = ChatFormatting.YELLOW.toString() + score;
            this.minecraft.font.drawShadow(matrices, scoreString, otherX - this.minecraft.font.width(scoreString), y, 0xff_ffff);
        }
    }
}
