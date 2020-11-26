var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var DEALFIREDAMAGE = ASMAPI.mapMethod("func_70081_e");
var BASETICK = ASMAPI.mapMethod("func_70030_z");
var ATTACKENTITYFROM = ASMAPI.mapMethod("func_70097_a");

function log(message) {
	print("[Evolution Entity#dealFireDamage(int) Transformer]: " + message);
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
		"Evolution EntityDealFireDamage Transformer": {
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
                    if (patch(methods[i], BASETICK, patchOnFire)) {
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

function patchOnFire(instructions) {
	var attackEntityFrom;
	for (var i = 0; i < instructions.size(); i++) {
        var instruction = instructions.get(i);
        if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL && instruction.name == ATTACKENTITYFROM) {
            attackEntityFrom = instruction;
            break;
        }
    }
    var pop = attackEntityFrom.getNext();
    var fconst = attackEntityFrom.getPrevious();
    var onfire = fconst.getPrevious();
    var aload = onfire.getPrevious();
    instructions.remove(onfire);
    instructions.remove(fconst);
    instructions.remove(pop);

    attackEntityFrom.setOpcode(Opcodes.INVOKESTATIC);
    attackEntityFrom.owner = "tgw/evolution/hooks/EntityHooks";
    attackEntityFrom.name = "onFireDamage";
    attackEntityFrom.desc = "(Lnet/minecraft/entity/Entity;)V";
}