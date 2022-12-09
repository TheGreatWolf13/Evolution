package tgw.evolution.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.model.*;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.ModelPlayer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(LayerDefinitions.class)
public abstract class LayerDefinitionsMixin {

    @Shadow
    @Final
    public static CubeDeformation OUTER_ARMOR_DEFORMATION;

    @Shadow
    @Final
    public static CubeDeformation INNER_ARMOR_DEFORMATION;

    @Shadow
    @Final
    private static CubeDeformation FISH_PATTERN_DEFORMATION;

    /**
     * @author TheGreatWolf
     * @reason Modify various layers.
     */
    @Overwrite
    public static Map<ModelLayerLocation, LayerDefinition> createRoots() {
        ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder = ImmutableMap.builder();
        LayerDefinition humanoid = LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64);
        LayerDefinition outerArmor = LayerDefinition.create(HumanoidModel.createMesh(OUTER_ARMOR_DEFORMATION, 0.0F), 64, 32);
        LayerDefinition piglinOuterArmor = LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(1.02F), 0.0F), 64, 32);
        LayerDefinition innerArmor = LayerDefinition.create(HumanoidModel.createMesh(INNER_ARMOR_DEFORMATION, 0.0F), 64, 32);
        LayerDefinition minecart = MinecartModel.createBodyLayer();
        LayerDefinition mobHead = SkullModel.createMobHeadLayer();
        LayerDefinition horse = LayerDefinition.create(HorseModel.createBodyMesh(CubeDeformation.NONE), 64, 64);
        LayerDefinition illager = IllagerModel.createBodyLayer();
        LayerDefinition cow = CowModel.createBodyLayer();
        LayerDefinition ocelot = LayerDefinition.create(OcelotModel.createBodyMesh(CubeDeformation.NONE), 64, 32);
        LayerDefinition piglin = LayerDefinition.create(PiglinModel.createMesh(CubeDeformation.NONE), 64, 64);
        LayerDefinition skull = SkullModel.createHumanoidHeadLayer();
        LayerDefinition llama = LlamaModel.createBodyLayer(CubeDeformation.NONE);
        LayerDefinition strider = StriderModel.createBodyLayer();
        LayerDefinition hoglin = HoglinModel.createBodyLayer();
        LayerDefinition skeleton = SkeletonModel.createBodyLayer();
        LayerDefinition villager = LayerDefinition.create(VillagerModel.createBodyModel(), 64, 64);
        LayerDefinition spider = SpiderModel.createSpiderBodyLayer();
        builder.put(ModelLayers.ARMOR_STAND, ArmorStandModel.createBodyLayer());
        builder.put(ModelLayers.ARMOR_STAND_INNER_ARMOR, ArmorStandArmorModel.createBodyLayer(INNER_ARMOR_DEFORMATION));
        builder.put(ModelLayers.ARMOR_STAND_OUTER_ARMOR, ArmorStandArmorModel.createBodyLayer(OUTER_ARMOR_DEFORMATION));
        builder.put(ModelLayers.AXOLOTL, AxolotlModel.createBodyLayer());
        builder.put(ModelLayers.BANNER, BannerRenderer.createBodyLayer());
        builder.put(ModelLayers.BAT, BatModel.createBodyLayer());
        builder.put(ModelLayers.BED_FOOT, BedRenderer.createFootLayer());
        builder.put(ModelLayers.BED_HEAD, BedRenderer.createHeadLayer());
        builder.put(ModelLayers.BEE, BeeModel.createBodyLayer());
        builder.put(ModelLayers.BELL, BellRenderer.createBodyLayer());
        builder.put(ModelLayers.BLAZE, BlazeModel.createBodyLayer());
        builder.put(ModelLayers.BOOK, BookModel.createBodyLayer());
        builder.put(ModelLayers.CAT, ocelot);
        builder.put(ModelLayers.CAT_COLLAR, LayerDefinition.create(OcelotModel.createBodyMesh(new CubeDeformation(0.01F)), 64, 32));
        builder.put(ModelLayers.CAVE_SPIDER, spider);
        builder.put(ModelLayers.CHEST, ChestRenderer.createSingleBodyLayer());
        builder.put(ModelLayers.DOUBLE_CHEST_LEFT, ChestRenderer.createDoubleBodyLeftLayer());
        builder.put(ModelLayers.DOUBLE_CHEST_RIGHT, ChestRenderer.createDoubleBodyRightLayer());
        builder.put(ModelLayers.CHEST_MINECART, minecart);
        builder.put(ModelLayers.CHICKEN, ChickenModel.createBodyLayer());
        builder.put(ModelLayers.COD, CodModel.createBodyLayer());
        builder.put(ModelLayers.COMMAND_BLOCK_MINECART, minecart);
        builder.put(ModelLayers.CONDUIT_EYE, ConduitRenderer.createEyeLayer());
        builder.put(ModelLayers.CONDUIT_WIND, ConduitRenderer.createWindLayer());
        builder.put(ModelLayers.CONDUIT_SHELL, ConduitRenderer.createShellLayer());
        builder.put(ModelLayers.CONDUIT_CAGE, ConduitRenderer.createCageLayer());
        builder.put(ModelLayers.COW, cow);
        builder.put(ModelLayers.CREEPER, CreeperModel.createBodyLayer(CubeDeformation.NONE));
        builder.put(ModelLayers.CREEPER_ARMOR, CreeperModel.createBodyLayer(new CubeDeformation(2.0F)));
        builder.put(ModelLayers.CREEPER_HEAD, mobHead);
        builder.put(ModelLayers.DOLPHIN, DolphinModel.createBodyLayer());
        builder.put(ModelLayers.DONKEY, ChestedHorseModel.createBodyLayer());
        builder.put(ModelLayers.DRAGON_SKULL, DragonHeadModel.createHeadLayer());
        builder.put(ModelLayers.DROWNED, DrownedModel.createBodyLayer(CubeDeformation.NONE));
        builder.put(ModelLayers.DROWNED_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.DROWNED_OUTER_ARMOR, innerArmor);
        builder.put(ModelLayers.DROWNED_OUTER_LAYER, DrownedModel.createBodyLayer(new CubeDeformation(0.25F)));
        builder.put(ModelLayers.ELDER_GUARDIAN, GuardianModel.createBodyLayer());
        builder.put(ModelLayers.ELYTRA, ElytraModel.createLayer());
        builder.put(ModelLayers.ENDERMAN, EndermanModel.createBodyLayer());
        builder.put(ModelLayers.ENDERMITE, EndermiteModel.createBodyLayer());
        builder.put(ModelLayers.ENDER_DRAGON, EnderDragonRenderer.createBodyLayer());
        builder.put(ModelLayers.END_CRYSTAL, EndCrystalRenderer.createBodyLayer());
        builder.put(ModelLayers.EVOKER, illager);
        builder.put(ModelLayers.EVOKER_FANGS, EvokerFangsModel.createBodyLayer());
        builder.put(ModelLayers.FOX, FoxModel.createBodyLayer());
        builder.put(ModelLayers.FURNACE_MINECART, minecart);
        builder.put(ModelLayers.GHAST, GhastModel.createBodyLayer());
        builder.put(ModelLayers.GIANT, humanoid);
        builder.put(ModelLayers.GIANT_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.GIANT_OUTER_ARMOR, outerArmor);
        builder.put(ModelLayers.GLOW_SQUID, SquidModel.createBodyLayer());
        builder.put(ModelLayers.GOAT, GoatModel.createBodyLayer());
        builder.put(ModelLayers.GUARDIAN, GuardianModel.createBodyLayer());
        builder.put(ModelLayers.HOGLIN, hoglin);
        builder.put(ModelLayers.HOPPER_MINECART, minecart);
        builder.put(ModelLayers.HORSE, horse);
        builder.put(ModelLayers.HORSE_ARMOR, LayerDefinition.create(HorseModel.createBodyMesh(new CubeDeformation(0.1F)), 64, 64));
        builder.put(ModelLayers.HUSK, humanoid);
        builder.put(ModelLayers.HUSK_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.HUSK_OUTER_ARMOR, outerArmor);
        builder.put(ModelLayers.ILLUSIONER, illager);
        builder.put(ModelLayers.IRON_GOLEM, IronGolemModel.createBodyLayer());
        builder.put(ModelLayers.LEASH_KNOT, LeashKnotModel.createBodyLayer());
        builder.put(ModelLayers.LLAMA, llama);
        builder.put(ModelLayers.LLAMA_DECOR, LlamaModel.createBodyLayer(new CubeDeformation(0.5F)));
        builder.put(ModelLayers.LLAMA_SPIT, LlamaSpitModel.createBodyLayer());
        builder.put(ModelLayers.MAGMA_CUBE, LavaSlimeModel.createBodyLayer());
        builder.put(ModelLayers.MINECART, minecart);
        builder.put(ModelLayers.MOOSHROOM, cow);
        builder.put(ModelLayers.MULE, ChestedHorseModel.createBodyLayer());
        builder.put(ModelLayers.OCELOT, ocelot);
        builder.put(ModelLayers.PANDA, PandaModel.createBodyLayer());
        builder.put(ModelLayers.PARROT, ParrotModel.createBodyLayer());
        builder.put(ModelLayers.PHANTOM, PhantomModel.createBodyLayer());
        builder.put(ModelLayers.PIG, PigModel.createBodyLayer(CubeDeformation.NONE));
        builder.put(ModelLayers.PIG_SADDLE, PigModel.createBodyLayer(new CubeDeformation(0.5F)));
        builder.put(ModelLayers.PIGLIN, piglin);
        builder.put(ModelLayers.PIGLIN_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.PIGLIN_OUTER_ARMOR, piglinOuterArmor);
        builder.put(ModelLayers.PIGLIN_BRUTE, piglin);
        builder.put(ModelLayers.PIGLIN_BRUTE_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.PIGLIN_BRUTE_OUTER_ARMOR, piglinOuterArmor);
        builder.put(ModelLayers.PILLAGER, illager);
        builder.put(ModelLayers.PLAYER, LayerDefinition.create(ModelPlayer.createMesh(CubeDeformation.NONE, false), 64, 64));
        builder.put(ModelLayers.PLAYER_HEAD, skull);
        builder.put(ModelLayers.PLAYER_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.PLAYER_OUTER_ARMOR, outerArmor);
        builder.put(ModelLayers.PLAYER_SLIM, LayerDefinition.create(ModelPlayer.createMesh(CubeDeformation.NONE, true), 64, 64));
        builder.put(ModelLayers.PLAYER_SLIM_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.PLAYER_SLIM_OUTER_ARMOR, outerArmor);
        builder.put(ModelLayers.PLAYER_SPIN_ATTACK, SpinAttackEffectLayer.createLayer());
        builder.put(ModelLayers.POLAR_BEAR, PolarBearModel.createBodyLayer());
        builder.put(ModelLayers.PUFFERFISH_BIG, PufferfishBigModel.createBodyLayer());
        builder.put(ModelLayers.PUFFERFISH_MEDIUM, PufferfishMidModel.createBodyLayer());
        builder.put(ModelLayers.PUFFERFISH_SMALL, PufferfishSmallModel.createBodyLayer());
        builder.put(ModelLayers.RABBIT, RabbitModel.createBodyLayer());
        builder.put(ModelLayers.RAVAGER, RavagerModel.createBodyLayer());
        builder.put(ModelLayers.SALMON, SalmonModel.createBodyLayer());
        builder.put(ModelLayers.SHEEP, SheepModel.createBodyLayer());
        builder.put(ModelLayers.SHEEP_FUR, SheepFurModel.createFurLayer());
        builder.put(ModelLayers.SHIELD, ShieldModel.createLayer());
        builder.put(ModelLayers.SHULKER, ShulkerModel.createBodyLayer());
        builder.put(ModelLayers.SHULKER_BULLET, ShulkerBulletModel.createBodyLayer());
        builder.put(ModelLayers.SILVERFISH, SilverfishModel.createBodyLayer());
        builder.put(ModelLayers.SKELETON, skeleton);
        builder.put(ModelLayers.SKELETON_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.SKELETON_OUTER_ARMOR, outerArmor);
        builder.put(ModelLayers.SKELETON_HORSE, horse);
        builder.put(ModelLayers.SKELETON_SKULL, mobHead);
        builder.put(ModelLayers.SLIME, SlimeModel.createInnerBodyLayer());
        builder.put(ModelLayers.SLIME_OUTER, SlimeModel.createOuterBodyLayer());
        builder.put(ModelLayers.SNOW_GOLEM, SnowGolemModel.createBodyLayer());
        builder.put(ModelLayers.SPAWNER_MINECART, minecart);
        builder.put(ModelLayers.SPIDER, spider);
        builder.put(ModelLayers.SQUID, SquidModel.createBodyLayer());
        builder.put(ModelLayers.STRAY, skeleton);
        builder.put(ModelLayers.STRAY_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.STRAY_OUTER_ARMOR, outerArmor);
        builder.put(ModelLayers.STRAY_OUTER_LAYER, LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.25F), 0), 64, 32));
        builder.put(ModelLayers.STRIDER, strider);
        builder.put(ModelLayers.STRIDER_SADDLE, strider);
        builder.put(ModelLayers.TNT_MINECART, minecart);
        builder.put(ModelLayers.TRADER_LLAMA, llama);
        builder.put(ModelLayers.TRIDENT, TridentModel.createLayer());
        builder.put(ModelLayers.TROPICAL_FISH_LARGE, TropicalFishModelB.createBodyLayer(CubeDeformation.NONE));
        builder.put(ModelLayers.TROPICAL_FISH_LARGE_PATTERN, TropicalFishModelB.createBodyLayer(FISH_PATTERN_DEFORMATION));
        builder.put(ModelLayers.TROPICAL_FISH_SMALL, TropicalFishModelA.createBodyLayer(CubeDeformation.NONE));
        builder.put(ModelLayers.TROPICAL_FISH_SMALL_PATTERN, TropicalFishModelA.createBodyLayer(FISH_PATTERN_DEFORMATION));
        builder.put(ModelLayers.TURTLE, TurtleModel.createBodyLayer());
        builder.put(ModelLayers.VEX, VexModel.createBodyLayer());
        builder.put(ModelLayers.VILLAGER, villager);
        builder.put(ModelLayers.VINDICATOR, illager);
        builder.put(ModelLayers.WANDERING_TRADER, villager);
        builder.put(ModelLayers.WITCH, WitchModel.createBodyLayer());
        builder.put(ModelLayers.WITHER, WitherBossModel.createBodyLayer(CubeDeformation.NONE));
        builder.put(ModelLayers.WITHER_ARMOR, WitherBossModel.createBodyLayer(INNER_ARMOR_DEFORMATION));
        builder.put(ModelLayers.WITHER_SKULL, WitherSkullRenderer.createSkullLayer());
        builder.put(ModelLayers.WITHER_SKELETON, skeleton);
        builder.put(ModelLayers.WITHER_SKELETON_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.WITHER_SKELETON_OUTER_ARMOR, outerArmor);
        builder.put(ModelLayers.WITHER_SKELETON_SKULL, mobHead);
        builder.put(ModelLayers.WOLF, WolfModel.createBodyLayer());
        builder.put(ModelLayers.ZOGLIN, hoglin);
        builder.put(ModelLayers.ZOMBIE, humanoid);
        builder.put(ModelLayers.ZOMBIE_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.ZOMBIE_OUTER_ARMOR, outerArmor);
        builder.put(ModelLayers.ZOMBIE_HEAD, skull);
        builder.put(ModelLayers.ZOMBIE_HORSE, horse);
        builder.put(ModelLayers.ZOMBIE_VILLAGER, ZombieVillagerModel.createBodyLayer());
        builder.put(ModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR, ZombieVillagerModel.createArmorLayer(INNER_ARMOR_DEFORMATION));
        builder.put(ModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR, ZombieVillagerModel.createArmorLayer(OUTER_ARMOR_DEFORMATION));
        builder.put(ModelLayers.ZOMBIFIED_PIGLIN, piglin);
        builder.put(ModelLayers.ZOMBIFIED_PIGLIN_INNER_ARMOR, innerArmor);
        builder.put(ModelLayers.ZOMBIFIED_PIGLIN_OUTER_ARMOR, piglinOuterArmor);
        LayerDefinition boat = BoatModel.createBodyModel();
        for (Boat.Type type : Boat.Type.values()) {
            //noinspection ObjectAllocationInLoop
            builder.put(ModelLayers.createBoatModelName(type), boat);
        }
        LayerDefinition sign = SignRenderer.createSignLayer();
        WoodType.values().forEach(t -> builder.put(ModelLayers.createSignModelName(t), sign));
        ForgeHooksClient.loadLayerDefinitions(builder);
        ImmutableMap<ModelLayerLocation, LayerDefinition> map = builder.build();
        List<ModelLayerLocation> list = ModelLayers.getKnownLocations()
                                                   .filter(l -> !map.containsKey(l))
                                                   .collect(Collectors.toList());
        if (!list.isEmpty()) {
            throw new IllegalStateException("Missing layer definitions: " + list);
        }
        return map;
    }
}
