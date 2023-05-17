var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

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
    var found = false;
    for (var i = 0, l = instructions.size(); i < l; i++) {
        var inst = instructions.get(i);
        if (inst.getOpcode() == Opcodes.NEW && inst.desc == "net/minecraftforge/client/gui/ForgeIngameGui") {
            inst.desc = "tgw/evolution/client/gui/EvolutionGui";
            found = true;
            continue;
        }
        if (found && inst.getOpcode() == Opcodes.INVOKESPECIAL && inst.owner == "net/minecraftforge/client/gui/ForgeIngameGui") {
            inst.owner = "tgw/evolution/client/gui/EvolutionGui";
            break;
        }
    }
}