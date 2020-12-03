var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

var JUMP = ASMAPI.mapMethod("func_70664_aZ");
var GETJUMPUPWARDSMOTION = ASMAPI.mapMethod("func_175134_bD");

function log(message) {
	print("[evolution/ LivingEntity#jump() Transformer]: " + message);
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
		"Evolution LivingEntityJump Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.LivingEntity"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], JUMP, patchJump)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				for (var i in methods) {
                    if (patch(methods[i], GETJUMPUPWARDSMOTION, patchUpMotion)) {
                        methods[i].localVariables.clear();
                        break;
                    }
                }
				return classNode;
			}
		}
	};
}

function patchJump(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/entity/LivingEntity", GETJUMPUPWARDSMOTION, "()F", false));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/LivingEntityHooks",
		"jump",
		"(Lnet/minecraft/entity/LivingEntity;F)V",
		false
	));
	instructions.add(new InsnNode(Opcodes.RETURN));
}

function patchUpMotion(instructions) {
	instructions.clear();
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/LivingEntityHooks",
		"getJumpUpwardsMotion",
		"()F",
		false
	));
	instructions.add(new InsnNode(Opcodes.FRETURN));
}