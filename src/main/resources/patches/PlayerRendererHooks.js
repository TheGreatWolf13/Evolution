var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

var FUNC = ASMAPI.mapMethod("func_217766_a");

function log(message) {
	print("[evolution/ PlayerRenderer#func_217766_a(AbstractClientPlayerEntity, ItemStack, ItemStack, Hand) Transformer]: " + message);
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
		"Evolution PlayerRenderer Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.renderer.entity.PlayerRenderer"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], FUNC, patchFunc)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchFunc(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/PlayerRenderHooks",
		"func_217766_a",
		"(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/renderer/entity/model/BipedModel$ArmPose;",
		false
	));
	instructions.add(new InsnNode(Opcodes.ARETURN));
}