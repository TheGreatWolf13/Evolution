//var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
//var Opcodes = Java.type("org.objectweb.asm.Opcodes");
//
//var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
//var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
//var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
//
//var PLAYEQUIPSOUND = ASMAPI.mapMethod("func_184606_a_");
//
//function log(message) {
//	print("[evolution/ LivingEntity#playEquipSound(ItemStack) Transformer]: " + message);
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
//		"Evolution LivingEntityEquipSound Transformer": {
//			"target": {
//				"type": "CLASS",
//				"name": "net.minecraft.entity.LivingEntity"
//			},
//			"transformer": function(classNode) {
//				var methods = classNode.methods;
//				for (var i in methods) {
//					if (patch(methods[i], PLAYEQUIPSOUND, patchEquipSound)) {
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
//function patchEquipSound(instructions) {
//	instructions.clear();
//	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
//	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
//	instructions.add(new MethodInsnNode(
//		Opcodes.INVOKESTATIC,
//		"tgw/evolution/hooks/LivingEntityHooks",
//		"playEquipSound",
//		"(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V",
//		false
//	));
//	instructions.add(new InsnNode(Opcodes.RETURN));
//}