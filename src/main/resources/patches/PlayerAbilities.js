var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

var CONST = ASMAPI.mapMethod("<init>");

function log(message) {
	print("[evolution/PlayerAbilities Transformer]: " + message);
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
		"Evolution PlayerAbilities Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.player.PlayerAbilities"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], CONST, patchConst)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchConst(instructions) {
    var list = new InsnList();
	for (var i = 0; i < instructions.size(); i++) {
	    var inst = instructions.get(i);
	    if(inst instanceof LdcInsnNode) {
            if(inst.cst == 0.10000000149011612) {
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                            "tgw/evolution/util/PlayerHelper",
                                            "getWalkSpeed",
                                            "()F",
                                            false));
                continue;
            }
        }
        list.add(inst);
    }
    instructions.clear();
    instructions.add(list);
}