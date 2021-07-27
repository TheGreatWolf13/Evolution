var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var FUNC = ASMAPI.mapMethod("func_216536_h");

function log(message) {
	print("[evolution/ShaderInstance Transformer]: " + message);
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
		"Evolution ShaderInstance Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.shader.ShaderInstance"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], FUNC, patchShaderInstance)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchShaderInstance(instructions) {
	var warn;
	var first = true;
    for (var i = 0; i < instructions.size(); i++) {
    	var instruction = instructions.get(i);
    	if (instruction.getOpcode() == Opcodes.INVOKEINTERFACE && instruction.name == "warn") {
    		if (first) {
    		    first = false;
    		    continue;
    		}
    		warn = instruction;
    		break;
    	}
    }
    var aload = warn.getPrevious();
    var ldc = aload.getPrevious();
    var logger = ldc.getPrevious();
    instructions.remove(logger);
    instructions.remove(ldc);
    instructions.remove(aload);
    instructions.remove(warn);
}