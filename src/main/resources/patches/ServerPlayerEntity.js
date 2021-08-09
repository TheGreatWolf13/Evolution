var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var LineNumberNode = Java.type("org.objectweb.asm.tree.LineNumberNode");
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
var FrameNode = Java.type("org.objectweb.asm.tree.FrameNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
var TryCatchBlockNode = Java.type("org.objectweb.asm.tree.TryCatchBlockNode");
var LocalVariableNode = Java.type("org.objectweb.asm.tree.LocalVariableNode");

var dropItem = ASMAPI.mapMethod("func_146097_a");
var awardKillScore = ASMAPI.mapMethod("func_191956_a");
var trySleep = "lambda$" + ASMAPI.mapMethod("func_213819_a") + "$3";

function log(message) {
	print("[evolution/ServerPlayerEntity Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}
	log("Patching method: " + name + method.desc);
	patchFunction(method.instructions);
	return true;
}

function getLine(instructions, number, index) {
    var indexOld = index;
    for (var i = 0; i < instructions.size(); i++) {
        var inst = instructions.get(i);
        if (inst instanceof LineNumberNode) {
            if (inst.line == number) {
                index--;
                if (index == 0) {
                    return i;
                }
            }
        }
    }
    log("Could not find line " + number + " index " + indexOld);
    return -1;
}

function initializeCoreMod() {
	return {
		"Evolution ServerPlayerEntity Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.player.ServerPlayerEntity"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], dropItem, patchDropItem)) {
						break;
					}
				}
				for (var i in methods) {
                    if (patch(methods[i], awardKillScore, patchAwardKillScore)) {
                        break;
                    }
                }
                for (var i in methods) {
                    if (patch(methods[i], trySleep, patchTrySleep)) {
                        break;
                    }
                }
				return classNode;
			}
		}
	};
}

function patchDropItem(instructions) {
    var line1328 = getLine(instructions, 1328, 1);
    var getField = instructions.get(line1328 + 3);
    getField.owner = "tgw/evolution/init/EvolutionStats";
    getField.name = "ITEMS_DROPPED";
}

function patchTrySleep(instructions) {
    var line721 = getLine(instructions, 721, 1);
    var getField = instructions.get(line721 + 2);
    getField.owner = "tgw/evolution/init/EvolutionStats";
    getField.name = "TIMES_SLEPT";
}

function patchAwardKillScore(instructions) {
    var line514 = getLine(instructions, 514, 1);
    var getField = instructions.get(line514 + 2);
    getField.owner = "tgw/evolution/init/EvolutionStats";
    getField.name = "PLAYER_KILLS";

    var line517 = getLine(instructions, 517, 1);
    var getField = instructions.get(line517 + 3);
    getField.owner = "tgw/evolution/init/EvolutionStats";
    getField.name = "MOB_KILLS";
}