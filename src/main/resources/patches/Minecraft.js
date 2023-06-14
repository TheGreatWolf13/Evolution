var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");

function log(message) {
	print("[evolution/Minecraft Transformer]: " + message);
}

function patch(method, name, desc, patchFunction) {
	if (method.name != name) {
		return false;
	}
	if (method.desc != desc) {
	    return false;
	}
	log("Patching method: " + name + method.desc);
	patchFunction(method.instructions);
	return true;
}

function initializeCoreMod() {
	return {
		"Evolution Minecraft Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.Minecraft"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], "<init>", "(Lnet/minecraft/client/main/GameConfig;)V", patchMC)) {
                        break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchMC(instructions) {
    var list = new InsnList();
    var foundGuiInit = false;
    var foundGuiNew = false;
    var foundLvlRenderer = false;
    var isRemoving = false;
    for (var i = 0, l = instructions.size(); i < l; i++) {
        var inst = instructions.get(i);
        if (!foundLvlRenderer) {
            if (inst.getOpcode() == Opcodes.NEW && inst.desc == "net/minecraft/client/renderer/LevelRenderer") {
                foundLvlRenderer = true;
                isRemoving = true;
                list.add(new InsnNode(Opcodes.ACONST_NULL));
                continue;
            }
        }
        else {
            if (isRemoving) {
                if (inst.getOpcode() != Opcodes.PUTFIELD) {
                    continue;
                }
                isRemoving = false;
            }
            if (foundGuiNew) {
                if (!foundGuiInit) {
                    if (inst.getOpcode() == Opcodes.INVOKESPECIAL && inst.owner == "net/minecraftforge/client/gui/ForgeIngameGui") {
                        inst.owner = "tgw/evolution/client/gui/EvolutionGui";
                        foundGuiInit = true;
                    }
                }
            }
            else {
                if (inst.getOpcode() == Opcodes.NEW && inst.desc == "net/minecraftforge/client/gui/ForgeIngameGui") {
                    inst.desc = "tgw/evolution/client/gui/EvolutionGui";
                    foundGuiNew = true;
                }
            }
        }
        list.add(inst);
    }
    instructions.clear();
    instructions.add(list);
}