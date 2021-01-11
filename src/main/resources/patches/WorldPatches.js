var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

var REMOVEBLOCK = ASMAPI.mapMethod("func_217377_a");

function log(message) {
	print("[evolution/ World#removeBlock(BlockPos, boolean) Transformer]: " + message);
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
		"Evolution World Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.world.World"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], REMOVEBLOCK, patchRemoveBlock)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchRemoveBlock(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/WorldHooks",
		"removeBlock",
		"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Z)Z",
		false
	));
	instructions.add(new InsnNode(Opcodes.IRETURN));
}