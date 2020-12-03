var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var HANDLESTATUSUPDATE = ASMAPI.mapMethod("func_70103_a");
var ATTACKENTITYFROM = ASMAPI.mapMethod("func_70097_a");
var DROWN = ASMAPI.mapField("field_76369_e");
var ON_FIRE = ASMAPI.mapField("field_76370_b");

function log(message) {
	print("[evolution/ LivingEntity#handleStatusUpdate(byte) Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}
	log("Patching method: " + name + " (" + method.name + ")");
	patchFunction(method.instructions);
	return true;
}

function initializeCoreMod() {
	return {
		"Evolution LivingEntityHandleStatusUpdate Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.LivingEntity"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], HANDLESTATUSUPDATE, patchStatusUpdate)) {
						break;
					}
				}
				for (var i in methods) {
                    if (patch(methods[i], ATTACKENTITYFROM, patchAttackEntityFrom)) {
                        break;
                    }
                }
				return classNode;
			}
		}
	};
}

function patchAttackEntityFrom(instructions) {
	for (var i = 0; i < instructions.size(); i++) {
	    var inst = instructions.get(i);
	    if (inst.getOpcode() == Opcodes.GETSTATIC && inst.name == DROWN) {
            inst.name = "DROWN";
            inst.owner = "tgw/evolution/init/EvolutionDamage";
	    }
	}
}

function patchStatusUpdate(instructions) {
	for (var i = 0; i < instructions.size(); i++) {
	    var inst = instructions.get(i);
	    if (inst.getOpcode() == Opcodes.GETSTATIC && inst.name == DROWN) {
            inst.name = "DROWN";
            inst.owner = "tgw/evolution/init/EvolutionDamage";
            break;
	    }
	}
	for (var i = 0; i < instructions.size(); i++) {
        var inst = instructions.get(i);
        if (inst.getOpcode() == Opcodes.GETSTATIC && inst.name == ON_FIRE) {
            inst.name = "ON_FIRE";
            inst.owner = "tgw/evolution/init/EvolutionDamage";
            break;
        }
    }
}