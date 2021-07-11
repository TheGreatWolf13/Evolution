var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

var READ = ASMAPI.mapMethod("func_152446_a");

function log(message) {
	print("[evolution/ EffectInstance#read(CompoundNBT) Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}
	log("Patching method: " + name + " (" + method.name + ")");
	patchFunction(method.instructions);
	log("Patching successful");
	return true;
}

function initializeCoreMod() {
	return {
		"Evolution EffectInstance Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.potion.EffectInstance"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], READ, patchRead)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchRead(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/potion/InfiniteEffectInstance",
		"read",
		"(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/potion/EffectInstance;",
		false
	));
	instructions.add(new InsnNode(Opcodes.ARETURN));
}