//var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
//var Opcodes = Java.type("org.objectweb.asm.Opcodes");
//var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
//var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
//var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
//
//var UPDATEFALLSTATE = ASMAPI.mapMethod("func_184231_a");
//var FALLDISTANCE = ASMAPI.mapField("field_70143_R");
//
//function log(message) {
//	print("[evolution/ Entity#updateFallState(double, boolean, BlockState, BlockPos) Transformer]: " + message);
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
//		"Evolution EntityUpdateFallState Transformer": {
//			"target": {
//				"type": "CLASS",
//				"name": "net.minecraft.entity.Entity"
//			},
//			"transformer": function(classNode) {
//				var methods = classNode.methods;
//				for (var i in methods) {
//					if (patch(methods[i], UPDATEFALLSTATE, patchUpdateFallState)) {
//						break;
//					}
//				}
//				return classNode;
//			}
//		}
//	};
//}
//
//function patchUpdateFallState(instructions) {
//    var fallDist;
//    for (var i = 0; i < instructions.size(); i++) {
//        var inst = instructions.get(i);
//        if (inst.getOpcode() == Opcodes.GETFIELD && inst.name == FALLDISTANCE) {
//            fallDist = inst;
//            break;
//        }
//    }
//
//    var aload0 = fallDist.getPrevious();
//    var fconst0 = fallDist.getNext();
//    var fcmpl = fconst0.getNext();
//    var iflel3 = fcmpl.getNext();
//
//    instructions.remove(aload0);
//    instructions.remove(fallDist);
//    instructions.remove(fconst0);
//    instructions.remove(fcmpl);
//    instructions.remove(iflel3);
//}