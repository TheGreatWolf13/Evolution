//var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
//var Opcodes = Java.type("org.objectweb.asm.Opcodes");
//
//var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
//var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
//var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
//var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
//
//var TRAVEL = ASMAPI.mapMethod("func_213352_e");
//var ISJUMPING = ASMAPI.mapField("field_70703_bu");
//var FLAGS = ASMAPI.mapField("field_184240_ax");
//
//function log(message) {
//	print("[evolution/ PlayerEntity#travel(Vec3d) Transformer]: " + message);
//}
//
//function patch(method, name, patchFunction) {
//	if (method.name != name) {
//		return false;
//	}
//	log("Patching method: " + name + " (" + method.name + ")");
//	patchFunction(method.instructions);
//	return true;
//}
//
//function initializeCoreMod() {
//	return {
//		"Evolution PlayerEntityTravel Transformer": {
//			"target": {
//				"type": "CLASS",
//				"name": "net.minecraft.entity.player.PlayerEntity"
//			},
//			"transformer": function(classNode) {
//				var methods = classNode.methods;
//				for (var i in methods) {
//					if (patch(methods[i], TRAVEL, patchTravel)) {
//						methods[i].localVariables.clear();
//						break;
//					}
//				}
//				return classNode;
//			}
//		}
//	};
//}
//
//function patchTravel(instructions) {
//	instructions.clear();
//	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
//	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
//	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
//	instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/LivingEntity", ISJUMPING, "Z"));
//	instructions.add(new FieldInsnNode(
//	    Opcodes.GETSTATIC,
//	    "net/minecraft/entity/Entity",
//	    FLAGS,
//	    "Lnet/minecraft/network/datasync/DataParameter;"
//	));
//	instructions.add(new MethodInsnNode(
//		Opcodes.INVOKESTATIC,
//		"tgw/evolution/hooks/PlayerHooks",
//		"travel",
//		"(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;ZLnet/minecraft/network/datasync/DataParameter;)V",
//		false
//	));
//	instructions.add(new InsnNode(Opcodes.RETURN));
//}