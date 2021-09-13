var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

var RUN = ASMAPI.mapMethod("func_240802_v_");

function log(message) {
	print("[evolution/MinecraftServer Transformer]: " + message);
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
		"Evolution MinecraftServer Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.server.MinecraftServer"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], RUN, patchServer)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchServer(instructions) {
    var list = new InsnList();
	for (var i = 0; i < instructions.size(); i++) {
	    var inst = instructions.get(i);
	    if(inst instanceof LdcInsnNode) {
            if(inst.cst == 50) {
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                            "tgw/evolution/hooks/TickrateChanger",
                                            "getMSPT",
                                            "()J",
                                            false));
                continue;
            }
        }
        list.add(inst);
    }
    instructions.clear();
    instructions.add(list);
}