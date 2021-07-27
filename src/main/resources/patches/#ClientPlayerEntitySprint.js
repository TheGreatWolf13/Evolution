//var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
//var Opcodes = Java.type("org.objectweb.asm.Opcodes");
//var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
//var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
//var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
//
//var LIVINGTICK = ASMAPI.mapMethod("func_70636_d");
//var COLLIDEDHORIZONTALLY = ASMAPI.mapField("field_70123_F");
//
//function log(message) {
//	print("[evolution/ ClientPlayerEntity#livingTick() Transformer]: " + message);
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
//		"Evolution ClientPlayerEntitySprint Transformer": {
//			"target": {
//				"type": "CLASS",
//				"name": "net.minecraft.client.entity.player.ClientPlayerEntity"
//			},
//			"transformer": function(classNode) {
//				var methods = classNode.methods;
//				for (var i in methods) {
//					if (patch(methods[i], LIVINGTICK, patchLivingTick)) {
//						break;
//					}
//				}
//				return classNode;
//			}
//		}
//	};
//}
//
//function patchLivingTick(instructions) {
//    var iload8;
//    for (var i = 0; i < instructions.size(); i++) {
//        var inst = instructions.get(i);
//        if (inst.getOpcode() == Opcodes.ILOAD && inst.var == 8) {
//            iload8 = inst;
//            break;
//        }
//    }
//    if (iload8 != null) {
//        var newInst = new InsnList();
//        newInst.add(new VarInsnNode(Opcodes.ILOAD, 7));
//        newInst.add(new VarInsnNode(Opcodes.ALOAD, 0));
//        newInst.add(new MethodInsnNode(
//                    		Opcodes.INVOKESTATIC,
//                    		"tgw/evolution/hooks/ClientPlayerHooks",
//                    		"getSprintBoolean",
//                    		"(ZLnet/minecraft/client/entity/player/ClientPlayerEntity;)Z",
//                    		false
//        ));
//        instructions.insertBefore(iload8, newInst);
//        instructions.remove(iload8);
//    }
//}