var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var DEALFIREDAMAGE = ASMAPI.mapMethod("func_70081_e");
var ATTACKENTITYFROM = ASMAPI.mapMethod("func_70097_a");
var UPDATEFALLSTATE = ASMAPI.mapMethod("func_184231_a");
var FALLDISTANCE = ASMAPI.mapField("field_70143_R");

function log(message) {
	print("[evolution/Entity Transformer]: " + message);
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
		"Evolution Entity Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.Entity"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], DEALFIREDAMAGE, patchDealFireDamage)) {
						methods[i].localVariables.clear();
						break;
					}
				}
                for (var i in methods) {
                    if (patch(methods[i], UPDATEFALLSTATE, patchUpdateFallState)) {
                        break;
                    }
                }
				return classNode;
			}
		}
	};
}

function patchDealFireDamage(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/EntityHooks",
		"dealFireDamage",
		"(Lnet/minecraft/entity/Entity;I)V",
		false
	));
	instructions.add(new InsnNode(Opcodes.RETURN));
}

function patchUpdateFallState(instructions) {
    var fallDist;
    for (var i = 0; i < instructions.size(); i++) {
        var inst = instructions.get(i);
        if (inst.getOpcode() == Opcodes.GETFIELD && inst.name == FALLDISTANCE) {
            fallDist = inst;
            break;
        }
    }

    var aload0 = fallDist.getPrevious();
    var fconst0 = fallDist.getNext();
    var fcmpl = fconst0.getNext();
    var iflel3 = fcmpl.getNext();

    instructions.remove(aload0);
    instructions.remove(fallDist);
    instructions.remove(fconst0);
    instructions.remove(fcmpl);
    instructions.remove(iflel3);
}