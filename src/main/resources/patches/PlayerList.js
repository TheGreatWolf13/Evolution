var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var GETPLAYERSTATS = ASMAPI.mapMethod("func_152602_a");
var PLAYERSTATSFILES = ASMAPI.mapField("field_148547_k");

function log(message) {
	print("[evolution/PlayerList Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}
	log("Patching method: " + name + method.desc);
	patchFunction(method.instructions);
	return true;
}

function initializeCoreMod() {
	return {
		"Evolution PlayerList Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.server.management.PlayerList"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], GETPLAYERSTATS, patchPlayerStats)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchPlayerStats(instructions) {
    instructions.clear();
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/server/management/PlayerList", PLAYERSTATSFILES, "Ljava/util/Map;"));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "tgw/evolution/hooks/PlayerHooks",
        "getPlayerStats",
        "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/server/management/PlayerList;Ljava/util/Map;)Lnet/minecraft/stats/ServerStatisticsManager;",
        false
    ));
    instructions.add(new InsnNode(Opcodes.ARETURN));
}