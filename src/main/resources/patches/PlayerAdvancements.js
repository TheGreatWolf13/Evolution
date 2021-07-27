var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var SHOULDBEVISIBLE = ASMAPI.mapMethod("func_192738_c");
var ISHIDDEN = ASMAPI.mapMethod("func_193224_j");

function log(message) {
	print("[evolution/PlayerAdvancements Transformer]: " + message);
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
		"Evolution PlayerAdvancements Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.advancements.PlayerAdvancements"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], SHOULDBEVISIBLE, patchPlayerAdv)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchPlayerAdv(instructions) {
	var ishidden;
    for (var i = 0; i < instructions.size(); i++) {
    	var instruction = instructions.get(i);
    	if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL && instruction.name == ISHIDDEN) {
    		ishidden = instruction;
    		break;
    	}
    }
    var getDisplay = ishidden.getPrevious();
    var aload = getDisplay.getPrevious();
    var ifeq = ishidden.getNext();
    var label = ifeq.getNext();
    var linenumber = label.getNext();
    var iconst0 = linenumber.getNext();
    var ireturn = iconst0.getNext();
    instructions.remove(ireturn);
    instructions.remove(iconst0);
    instructions.remove(ifeq);
    instructions.remove(aload);
    instructions.remove(getDisplay);
    instructions.remove(ishidden);
}