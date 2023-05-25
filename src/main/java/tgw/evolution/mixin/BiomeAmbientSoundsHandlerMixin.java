package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

@Mixin(BiomeAmbientSoundsHandler.class)
public abstract class BiomeAmbientSoundsHandlerMixin {

    @Unique
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    @Shadow
    private Optional<AmbientAdditionsSettings> additionsSettings;
    @Shadow
    @Final
    private BiomeManager biomeManager;
    @Shadow
    @Final
    private Object2ObjectArrayMap<Biome, BiomeAmbientSoundsHandler.LoopSoundInstance> loopSounds;
    @Shadow
    private Optional<AmbientMoodSettings> moodSettings;
    @Shadow
    private float moodiness;
    @Shadow
    @Final
    private LocalPlayer player;
    @Shadow
    @Nullable
    private Biome previousBiome;
    @Shadow
    @Final
    private Random random;
    @Shadow
    @Final
    private SoundManager soundManager;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when possible
     */
    @Overwrite
    public void tick() {
        this.loopSounds.values().removeIf(AbstractTickableSoundInstance::isStopped);
        Biome biome = this.biomeManager.getNoiseBiomeAtPosition(this.player.getX(), this.player.getY(), this.player.getZ()).value();
        if (biome != this.previousBiome) {
            this.previousBiome = biome;
            this.moodSettings = biome.getAmbientMood();
            this.additionsSettings = biome.getAmbientAdditions();
            this.loopSounds.values().forEach(BiomeAmbientSoundsHandler.LoopSoundInstance::fadeOut);
            Optional<SoundEvent> ambientLoop = biome.getAmbientLoop();
            if (ambientLoop.isPresent()) {
                BiomeAmbientSoundsHandler.LoopSoundInstance loopSoundInstance = this.loopSounds.get(biome);
                boolean created = false;
                if (loopSoundInstance == null) {
                    loopSoundInstance = new BiomeAmbientSoundsHandler.LoopSoundInstance(ambientLoop.get());
                    this.soundManager.play(loopSoundInstance);
                    created = true;
                }
                loopSoundInstance.fadeIn();
                if (created) {
                    this.loopSounds.put(biome, loopSoundInstance);
                }
            }
        }
        if (this.additionsSettings.isPresent()) {
            AmbientAdditionsSettings settings = this.additionsSettings.get();
            if (this.random.nextDouble() < settings.getTickChance()) {
                this.soundManager.play(SimpleSoundInstance.forAmbientAddition(settings.getSoundEvent()));
            }
        }
        if (this.moodSettings.isPresent()) {
            AmbientMoodSettings settings = this.moodSettings.get();
            Level level = this.player.level;
            int i = settings.getBlockSearchExtent() * 2 + 1;
            this.mutablePos.set(this.player.getX() + this.random.nextInt(i) - settings.getBlockSearchExtent(),
                                this.player.getEyeY() + this.random.nextInt(i) - settings.getBlockSearchExtent(),
                                this.player.getZ() + this.random.nextInt(i) - settings.getBlockSearchExtent());
            int j = level.getBrightness(LightLayer.SKY, this.mutablePos);
            if (j > 0) {
                this.moodiness -= j / (float) level.getMaxLightLevel() * 0.001F;
            }
            else {
                this.moodiness -= (level.getBrightness(LightLayer.BLOCK, this.mutablePos) - 1) / (float) settings.getTickDelay();
            }
            if (this.moodiness >= 1.0F) {
                double x = this.mutablePos.getX() + 0.5;
                double y = this.mutablePos.getY() + 0.5;
                double z = this.mutablePos.getZ() + 0.5;
                double dx = x - this.player.getX();
                double dy = y - this.player.getEyeY();
                double dz = z - this.player.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double d7 = length + settings.getSoundPositionOffset();
                SimpleSoundInstance soundInstance = SimpleSoundInstance.forAmbientMood(settings.getSoundEvent(),
                                                                                       this.player.getX() + dx / length * d7,
                                                                                       this.player.getEyeY() + dy / length * d7,
                                                                                       this.player.getZ() + dz / length * d7);
                this.soundManager.play(soundInstance);
                this.moodiness = 0.0F;
            }
            else {
                this.moodiness = Math.max(this.moodiness, 0.0F);
            }
        }
    }
}
