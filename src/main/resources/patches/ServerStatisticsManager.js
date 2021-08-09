var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");

var CONSTRUCTOR = ASMAPI.mapMethod("<init>");

function log(message) {
	print("[evolution/ServerStatisticsManager Transformer]: " + message);
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
		"Evolution ServerStatisticsManager Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.stats.ServerStatisticsManager"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], CONSTRUCTOR, patchConst)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchConst(instructions) {
    var ifeq;
    for (var i = 0; i < instructions.size(); i++) {
        var inst = instructions.get(i);
        if (inst.getOpcode() == Opcodes.IFEQ) {
            ifeq = inst;
            break;
        }
    }
    ifeq.opcode = Opcodes.GOTO;
}