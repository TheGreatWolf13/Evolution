var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var GETHURTSOUND = ASMAPI.mapMethod("func_184601_bQ");

function log(message) {
	print("[evolution/ PlayerEntity#getHurtSound(DamageSource) Transformer]: " + message);
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
		"Evolution PlayerEntityHurtSound Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.player.PlayerEntity"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], GETHURTSOUND, patchHurtSound)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchHurtSound(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/PlayerHooks",
		"getHurtSound",
		"(Lnet/minecraft/util/DamageSource;)Lnet/minecraft/util/SoundEvent;",
		false
	));
	instructions.add(new InsnNode(Opcodes.ARETURN));
}