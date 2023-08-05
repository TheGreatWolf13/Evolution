package tgw.evolution.patches;

import tgw.evolution.network.*;

public interface PatchServerPacketListener {

//    void handleChangeBlock(PacketCSChangeBlock packet);

    void handleCollision(PacketCSCollision packet);

    void handleEntityInteraction(PacketCSEntityInteraction packet);

    void handleImpactDamage(PacketCSImpactDamage packet);

    void handlePlaySoundEntityEmitted(PacketCSPlaySoundEntityEmitted packet);

    void handlePlayerAction(PacketCSPlayerAction packet);

    void handlePlayerFall(PacketCSPlayerFall packet);

    void handleSetCrawling(PacketCSSetCrawling packet);

    void handleSetKnappingType(PacketCSSetKnappingType packet);

    void handleSetMoldingType(PacketCSSetMoldingType packet);

    void handleSimpleMessage(PacketCSSimpleMessage packet);

    void handleSkinType(PacketCSSkinType packet);

    void handleSpecialAttackStart(PacketCSSpecialAttackStart packet);

    void handleSpecialAttackStop(PacketCSSpecialAttackStop packet);

    void handleSpecialHit(PacketCSSpecialHit packet);

    void handleUpdateBeltBackItem(PacketCSUpdateBeltBackItem packet);

    void handleUpdatePuzzle(PacketCSUpdatePuzzle packet);

    void handleUpdateSchematicBlock(PacketCSUpdateSchematicBlock packet);
}
