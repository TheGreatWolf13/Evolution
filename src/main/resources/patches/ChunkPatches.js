var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

var GETFLUIDSTATE = ASMAPI.mapMethod("func_206914_b");

function log(message) {
	print("[evolution/ Chunk#getFluidState(BlockPos) Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}
	if (method.desc != "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;") {
	    return false;
	}
	log("Patching method: " + name + " (" + method.name + ")");
	patchFunction(method.instructions);
	return true;
}

function initializeCoreMod() {
	return {
		"Evolution Chunk Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.world.chunk.Chunk"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], GETFLUIDSTATE, patchGetFluidState)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchGetFluidState(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/ChunkHooks",
		"getFluidState",
		"(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;",
		false
	));
	instructions.add(new InsnNode(Opcodes.ARETURN));
}