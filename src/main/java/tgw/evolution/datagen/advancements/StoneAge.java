//package tgw.evolution.datagen.advancements;
//
//import net.minecraft.advancements.Advancement;
//import net.minecraft.advancements.FrameType;
//import net.minecraft.advancements.critereon.*;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.resources.ResourceLocation;
//import tgw.evolution.capabilities.modular.part.PartTypes;
//import tgw.evolution.client.gui.advancements.TagDisplayInfo;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.init.EvolutionItemTags;
//import tgw.evolution.init.EvolutionItems;
//import tgw.evolution.init.EvolutionMaterials;
//import tgw.evolution.util.constants.RockVariant;
//
//import java.util.function.Consumer;
//
//public class StoneAge implements Consumer<Consumer<Advancement>> {
//
//    @Override
//    public void accept(Consumer<Advancement> consumer) {
//        Advancement root = Advancement.Builder.advancement()
//                                              .display(EvolutionBlocks.COBBLESTONES.get(RockVariant.ANDESITE).get(),
//                                                       new TranslatableComponent("evolution.advancements.stone_age.root.title"),
//                                                       new TranslatableComponent("evolution.advancements.stone_age.root.description"),
//                                                       new ResourceLocation("evolution:textures/block/cobblestone_andesite.png"), FrameType.TASK,
//                                                       true,
//                                                       true,
//                                                       false)
//                                              .addCriterion("spawned", new TickTrigger.TriggerInstance(EntityPredicate.Composite.ANY))
//                                              .save(consumer, "evolution:stone_age/root");
//        Advancement defARock = Advancement.Builder.advancement()
//                                                  .parent(root)
//                                                  .display(new TagDisplayInfo(EvolutionItemTags.ROCKS,
//                                                                              new TranslatableComponent(
//                                                                                      "evolution.advancements.stone_age.definitely_a_rock.title"),
//                                                                              new TranslatableComponent(
//                                                                                      "evolution.advancements.stone_age.definitely_a_rock" +
//                                                                                      ".description"),
//                                                                              null,
//                                                                              FrameType.TASK,
//                                                                              true,
//                                                                              true,
//                                                                              false))
//                                                  .addCriterion("has_any_rock", InventoryChangeTrigger.TriggerInstance.hasItems(
//                                                          ItemPredicate.Builder.item().of(EvolutionItemTags.ROCKS).build()))
//                                                  .save(consumer,
//                                                        "evolution:stone_age/definitely_a_rock");
//        Advancement knapping = Advancement.Builder.advancement()
//                                                  .parent(defARock)
//                                                  .display(EvolutionItems.PART_HEAD.get().newStack(PartTypes.Head.AXE, EvolutionMaterials.ANDESITE),
//                                                           new TranslatableComponent("evolution.advancements.stone_age.knapping.title"),
//                                                           new TranslatableComponent("evolution.advancements.stone_age.knapping.description"),
//                                                           null,
//                                                           FrameType.TASK,
//                                                           true,
//                                                           true,
//                                                           false)
//                                                  .addCriterion("placed_block", new PlacedBlockTrigger.TriggerInstance(
//                                                          EntityPredicate.Composite.wrap(
//                                                                  EntityPredicate.Builder.entity()
//                                                                                         .flags(EntityFlagsPredicate.Builder.flags()
//                                                                                                                            .setCrouching(true)
//                                                                                                                            .build()).build()),
//                                                          null,
//                                                          StatePropertiesPredicate.ANY,
//                                                          LocationPredicate.ANY,
//                                                          ItemPredicate.Builder.item().of(EvolutionItemTags.ROCKS).build()))
//                                                  .save(consumer, "evolution:stone_age/knapping");
//    }
//}
