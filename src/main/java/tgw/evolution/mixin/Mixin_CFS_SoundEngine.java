package tgw.evolution.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import com.mojang.blaze3d.audio.Listener;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import net.minecraft.SharedConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2IHashMap;
import tgw.evolution.util.collection.maps.O2IMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;
import tgw.evolution.util.physics.EarthHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(SoundEngine.class)
public abstract class Mixin_CFS_SoundEngine {

    @Mutable @Shadow @Final @RestoreFinal private static Logger LOGGER;
    @Mutable @Shadow @Final @RestoreFinal private static Marker MARKER;
    @DeleteField @Shadow @Final private static Set<ResourceLocation> ONLY_WARN_ONCE;
    @Unique @RestoreFinal private static OSet<ResourceLocation> ONLY_WARN_ONCE_;
    @Mutable @Shadow @Final @RestoreFinal public static int OPEN_AL_SOFT_PREFIX_LENGTH;
    @Mutable @Shadow @Final @RestoreFinal private ChannelAccess channelAccess;
    @Mutable @Shadow @Final @RestoreFinal private AtomicReference<SoundEngine.DeviceCheckState> devicePoolState;
    @Mutable @Shadow @Final @RestoreFinal private SoundEngineExecutor executor;
    @Mutable @Shadow @Final @RestoreFinal private Multimap<SoundSource, SoundInstance> instanceBySource;
    @DeleteField @Shadow @Final private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;
    @Unique private final O2OMap<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel_;
    @Mutable @Shadow @Final @RestoreFinal private Library library;
    @Mutable @Shadow @Final @RestoreFinal private Listener listener;
    @DeleteField @Shadow @Final private List<SoundEventListener> listeners;
    @Unique private final OList<SoundEventListener> listeners_;
    @Shadow private boolean loaded;
    @Mutable @Shadow @Final @RestoreFinal private Options options;
    @DeleteField @Shadow @Final private List<Sound> preloadQueue;
    @Unique private final OList<Sound> preloadQueue_;
    @DeleteField @Shadow @Final private Map<SoundInstance, Integer> queuedSounds;
    @Unique private final O2IMap<SoundInstance> queuedSounds_;
    @DeleteField @Shadow @Final private List<TickableSoundInstance> queuedTickableSounds;
    @Unique private final OList<TickableSoundInstance> queuedTickableSounds_;
    @Mutable @Shadow @Final @RestoreFinal private SoundBufferLibrary soundBuffers;
    @DeleteField @Shadow @Final private Map<SoundInstance, Integer> soundDeleteTime;
    @Unique private final O2IMap<SoundInstance> soundDeleteTime_;
    @Mutable @Shadow @Final @RestoreFinal private SoundManager soundManager;
    @Shadow private int tickCount;
    @DeleteField @Shadow @Final private List<TickableSoundInstance> tickingSounds;
    @Unique private final OList<TickableSoundInstance> tickingSounds_;

    @ModifyConstructor
    public Mixin_CFS_SoundEngine(SoundManager soundManager, Options options, ResourceManager resourceManager) {
        this.library = new Library();
        this.listener = this.library.getListener();
        this.executor = new SoundEngineExecutor();
        this.channelAccess = new ChannelAccess(this.library, this.executor);
        this.devicePoolState = new AtomicReference<>(SoundEngine.DeviceCheckState.NO_CHANGE);
        this.instanceToChannel_ = new O2OHashMap<>();
        this.instanceBySource = HashMultimap.create();
        this.tickingSounds_ = new OArrayList<>();
        this.queuedSounds_ = new O2IHashMap<>();
        this.soundDeleteTime_ = new O2IHashMap<>();
        this.listeners_ = new OArrayList<>();
        this.queuedTickableSounds_ = new OArrayList<>();
        this.preloadQueue_ = new OArrayList<>();
        this.soundManager = soundManager;
        this.options = options;
        this.soundBuffers = new SoundBufferLibrary(resourceManager);
    }

    @Unique
    @ModifyStatic
    private static void _clinit() {
        MARKER = MarkerFactory.getMarker("SOUNDS");
        LOGGER = LogUtils.getLogger();
        ONLY_WARN_ONCE_ = new OHashSet<>();
        OPEN_AL_SOFT_PREFIX_LENGTH = "OpenAL Soft on ".length();
    }

    @Shadow
    private static boolean shouldLoopAutomatically(SoundInstance soundInstance) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static boolean shouldLoopManually(SoundInstance soundInstance) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void addEventListener(SoundEventListener listener) {
        this.listeners_.add(listener);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private float calculatePitch(SoundInstance soundInstance) {
        if (soundInstance.getSource() == SoundSource.MASTER) {
            return Mth.clamp(soundInstance.getPitch(), 0.5F, 2.0F);
        }
        float mul = ClientEvents.getPitchMul();
        return Mth.clamp(soundInstance.getPitch(), 0.5F, 2.0F) * mul;
    }

    @Shadow
    protected abstract float calculateVolume(SoundInstance soundInstance);

    @Shadow
    public abstract void destroy();

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private float getVolume(@Nullable SoundSource soundSource) {
        if (soundSource == SoundSource.MASTER) {
            return 1.0f;
        }
        float mul = ClientEvents.getVolumeMultiplier();
        if (mul <= 0) {
            return 0;
        }
        return soundSource != null ? this.options.getSoundSourceVolume(soundSource) * mul : mul;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean isActive(SoundInstance instance) {
        if (!this.loaded) {
            return false;
        }
        return this.soundDeleteTime_.containsKey(instance) && this.soundDeleteTime_.getInt(instance) <= this.tickCount || this.instanceToChannel_.containsKey(instance);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private synchronized void loadLibrary() {
        if (!this.loaded) {
            try {
                this.library.init(this.options.soundDevice.isEmpty() ? null : this.options.soundDevice);
                this.listener.reset();
                this.listener.setGain(this.options.getSoundSourceVolume(SoundSource.MASTER));
                this.soundBuffers.preload(this.preloadQueue_).thenRun(this.preloadQueue_::clear);
                this.loaded = true;
                LOGGER.info(MARKER, "Sound engine started");
            }
            catch (RuntimeException e) {
                LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", e);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void play(SoundInstance instance) {
        if (this.loaded) {
            if (instance.canPlaySound()) {
                WeighedSoundEvents weighedSoundEvents = instance.resolve(this.soundManager);
                ResourceLocation resLoc = instance.getLocation();
                if (weighedSoundEvents == null) {
                    if (ONLY_WARN_ONCE_.add(resLoc)) {
                        LOGGER.warn(MARKER, "Unable to play unknown soundEvent: {}", resLoc);
                    }
                }
                else {
                    Sound sound = instance.getSound();
                    if (sound == SoundManager.EMPTY_SOUND) {
                        if (ONLY_WARN_ONCE_.add(resLoc)) {
                            LOGGER.warn(MARKER, "Unable to play empty soundEvent: {}", resLoc);
                        }
                    }
                    else {
                        float actualVolume = Math.max(instance.getVolume(), 1.0F) * sound.getAttenuationDistance();
                        SoundSource soundSource = instance.getSource();
                        float volume = this.calculateVolume(instance);
                        float pitch = this.calculatePitch(instance);
                        SoundInstance.Attenuation attenuation = instance.getAttenuation();
                        boolean isRelative = instance.isRelative();
                        if (volume == 0.0F && !instance.canStartSilent()) {
                            LOGGER.debug(MARKER, "Skipped playing sound {}, volume was zero.", sound.getLocation());
                        }
                        else {
                            OList<SoundEventListener> listeners = this.listeners_;
                            double x = instance.getX();
                            double y = instance.getY();
                            double z = instance.getZ();
                            if (!listeners.isEmpty()) {
                                if (isRelative || attenuation == SoundInstance.Attenuation.NONE || this.listener.getListenerPosition().distanceToSqr(x, y, z) < actualVolume * actualVolume) {
                                    for (int i = 0, len = listeners.size(); i < len; ++i) {
                                        listeners.get(i).onPlaySound(instance, weighedSoundEvents);
                                    }
                                }
                            }
                            if (this.listener.getGain() > 0.0F) {
                                boolean shouldLoopAutomatically = shouldLoopAutomatically(instance);
                                boolean shouldStream = sound.shouldStream();
                                ChannelAccess.ChannelHandle handle = this.channelAccess.createHandle(sound.shouldStream() ? Library.Pool.STREAMING : Library.Pool.STATIC).join();
                                if (handle == null) {
                                    if (SharedConstants.IS_RUNNING_IN_IDE) {
                                        LOGGER.warn("Failed to create new sound handle");
                                    }
                                }
                                else {
                                    this.soundDeleteTime_.put(instance, this.tickCount + 20);
                                    this.instanceToChannel_.put(instance, handle);
                                    this.instanceBySource.put(soundSource, instance);
                                    handle.execute(channel -> {
                                        channel.setPitch(pitch);
                                        channel.setVolume(volume);
                                        if (attenuation == SoundInstance.Attenuation.LINEAR) {
                                            channel.linearAttenuation(actualVolume);
                                        }
                                        else {
                                            channel.disableAttenuation();
                                        }
                                        channel.setLooping(shouldLoopAutomatically && !shouldStream);
                                        channel.setSelfPosition(x, y, z);
                                        channel.setRelative(isRelative);
                                    });
                                    if (!shouldStream) {
                                        this.soundBuffers.getCompleteBuffer(sound.getPath()).thenAccept(soundBuffer -> handle.execute(channel -> {
                                            channel.attachStaticBuffer(soundBuffer);
                                            channel.play();
                                        }));
                                    }
                                    else {
                                        this.soundBuffers.getStream(sound.getPath(), shouldLoopAutomatically).thenAccept(audioStream -> handle.execute(channel -> {
                                            channel.attachBufferStream(audioStream);
                                            channel.play();
                                        }));
                                    }
                                    if (instance instanceof TickableSoundInstance tickable) {
                                        this.tickingSounds_.add(tickable);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void playDelayed(SoundInstance instance, int tickDelay) {
        this.queuedSounds_.put(instance, this.tickCount + tickDelay);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void queueTickingSound(TickableSoundInstance tickable) {
        this.queuedTickableSounds_.add(tickable);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void reload() {
        ONLY_WARN_ONCE_.clear();
        for (long it = Registry.SOUND_EVENT.beginIteration(); Registry.SOUND_EVENT.hasNextIteration(it); it = Registry.SOUND_EVENT.nextEntry(it)) {
            SoundEvent event = (SoundEvent) Registry.SOUND_EVENT.getIteration(it);
            ResourceLocation location = event.getLocation();
            if (this.soundManager.getSoundEvent(location) == null) {
                LOGGER.warn("Missing sound for event: {}", Registry.SOUND_EVENT.getKey(event));
                ONLY_WARN_ONCE_.add(location);
            }
        }
        this.destroy();
        this.loadLibrary();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void removeEventListener(SoundEventListener listener) {
        this.listeners_.remove(listener);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void requestPreload(Sound sound) {
        this.preloadQueue_.add(sound);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void stop(SoundInstance instance) {
        if (this.loaded) {
            ChannelAccess.ChannelHandle channelHandle = this.instanceToChannel_.get(instance);
            if (channelHandle != null) {
                channelHandle.execute(Channel::stop);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void stop(@Nullable ResourceLocation location, @Nullable SoundSource source) {
        if (source != null) {
            for (SoundInstance soundInstance : this.instanceBySource.get(source)) {
                if (location == null || soundInstance.getLocation().equals(location)) {
                    return;
                }
                this.stop(soundInstance);
            }
        }
        if (location == null) {
            this.stopAll();
        }
        else {
            O2OMap<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = this.instanceToChannel_;
            for (long it = instanceToChannel.beginIteration(); instanceToChannel.hasNextIteration(it); it = instanceToChannel.nextEntry(it)) {
                SoundInstance sound = instanceToChannel.getIterationKey(it);
                if (sound.getLocation().equals(location)) {
                    this.stop(sound);
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void stopAll() {
        if (this.loaded) {
            this.executor.flush();
            O2OMap<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = this.instanceToChannel_;
            for (long it = instanceToChannel.beginIteration(); instanceToChannel.hasNextIteration(it); it = instanceToChannel.nextEntry(it)) {
                instanceToChannel.getIterationValue(it).execute(Channel::stop);
            }
            instanceToChannel.clear();
            this.channelAccess.clear();
            this.queuedSounds_.clear();
            this.tickingSounds_.clear();
            this.instanceBySource.clear();
            this.soundDeleteTime_.clear();
            this.queuedTickableSounds_.clear();
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void tickNonPaused() {
        ++this.tickCount;
        OList<TickableSoundInstance> queuedTickableSounds = this.queuedTickableSounds_;
        for (int i = 0, len = queuedTickableSounds.size(); i < len; ++i) {
            TickableSoundInstance tickable = queuedTickableSounds.get(i);
            if (tickable.canPlaySound()) {
                this.play(tickable);
            }
        }
        queuedTickableSounds.clear();
        OList<TickableSoundInstance> tickingSounds = this.tickingSounds_;
        O2OMap<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = this.instanceToChannel_;
        for (int i = 0, len = tickingSounds.size(); i < len; ++i) {
            TickableSoundInstance tickable = tickingSounds.get(i);
            if (!tickable.canPlaySound()) {
                this.stop(tickable);
            }
            tickable.tick();
            if (tickable.isStopped()) {
                this.stop(tickable);
            }
            else {
                ChannelAccess.ChannelHandle handle = instanceToChannel.get(tickable);
                if (handle != null) {
                    float volume = this.calculateVolume(tickable);
                    float pitch = this.calculatePitch(tickable);
                    double x = tickable.getX();
                    double y = tickable.getY();
                    double z = tickable.getZ();
                    handle.execute(channel -> {
                        channel.setVolume(volume);
                        channel.setPitch(pitch);
                        channel.setSelfPosition(x, y, z);
                    });
                }
            }
        }
        O2IMap<SoundInstance> soundDeleteTime = this.soundDeleteTime_;
        O2IMap<SoundInstance> queuedSounds = this.queuedSounds_;
        for (long it = instanceToChannel.beginIteration(); instanceToChannel.hasNextIteration(it); it = instanceToChannel.nextEntry(it)) {
            ChannelAccess.ChannelHandle handle = instanceToChannel.getIterationValue(it);
            SoundInstance soundInstance = instanceToChannel.getIterationKey(it);
            float volume = this.options.getSoundSourceVolume(soundInstance.getSource());
            if (volume <= 0.0F) {
                handle.execute(Channel::stop);
                it = instanceToChannel.removeIteration(it);
            }
            else if (handle.isStopped()) {
                int deleteTime = soundDeleteTime.getInt(soundInstance);
                if (deleteTime <= this.tickCount) {
                    if (shouldLoopManually(soundInstance)) {
                        queuedSounds.put(soundInstance, this.tickCount + soundInstance.getDelay());
                    }
                    it = instanceToChannel.removeIteration(it);
                    LOGGER.debug(MARKER, "Removed channel {} because it's not playing anymore", handle);
                    soundDeleteTime.removeInt(soundInstance);
                    try {
                        this.instanceBySource.remove(soundInstance.getSource(), soundInstance);
                    }
                    catch (RuntimeException ignored) {
                    }
                    if (soundInstance instanceof TickableSoundInstance) {
                        this.tickingSounds_.remove(soundInstance);
                    }
                }
            }
        }
        for (long it = queuedSounds.beginIteration(); queuedSounds.hasNextIteration(it); it = queuedSounds.nextEntry(it)) {
            if (this.tickCount >= queuedSounds.getIterationValue(it)) {
                SoundInstance soundInstance = queuedSounds.getIterationKey(it);
                if (soundInstance instanceof TickableSoundInstance tickable) {
                    tickable.tick();
                }
                this.play(soundInstance);
                it = queuedSounds.removeIteration(it);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void updateCategoryVolume(SoundSource source, float gain) {
        if (this.loaded) {
            if (source == SoundSource.MASTER) {
                this.listener.setGain(gain);
            }
            else {
                O2OMap<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = this.instanceToChannel_;
                for (long it = instanceToChannel.beginIteration(); instanceToChannel.hasNextIteration(it); it = instanceToChannel.nextEntry(it)) {
                    float f = this.calculateVolume(instanceToChannel.getIterationKey(it));
                    instanceToChannel.getIterationValue(it).execute(c -> {
                        if (f <= 0.0F) {
                            c.stop();
                        }
                        else {
                            c.setVolume(f);
                        }
                    });
                }
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void updateSource(Camera camera) {
        if (this.loaded && camera.isInitialized()) {
            Vec3 pos = camera.getPosition();
            Vector3f look = camera.getLookVector();
            Vector3f up = camera.getUpVector();
            int xWrap = camera.getXWrap();
            int zWrap = camera.getZWrap();
            if (xWrap != 0 || zWrap != 0) {
                O2OMap<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = this.instanceToChannel_;
                for (long it = instanceToChannel.beginIteration(); instanceToChannel.hasNextIteration(it); it = instanceToChannel.nextEntry(it)) {
                    SoundInstance instance = instanceToChannel.getIterationKey(it);
                    if (!(instance instanceof TickableSoundInstance)) {
                        ChannelAccess.ChannelHandle handle = instanceToChannel.getIterationValue(it);
                        double x = instance.getX() + xWrap * EarthHelper.WORLD_SIZE;
                        double y = instance.getY();
                        double z = instance.getZ() + zWrap * EarthHelper.WORLD_SIZE;
                        handle.execute(channel -> channel.setSelfPosition(x, y, z));
                    }
                }
            }
            this.executor.execute(() -> {
                this.listener.setListenerPosition(pos);
                this.listener.setListenerOrientation(look, up);
            });
        }
    }
}
