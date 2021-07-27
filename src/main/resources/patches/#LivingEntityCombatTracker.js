//var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
//var Opcodes = Java.type("org.objectweb.asm.Opcodes");
//
//var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
//var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
//var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
//var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
//var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
//
//var CONSTRUCTOR = ASMAPI.mapMethod("<init>");
//
//function log(message) {
//	print("[evolution/ LivingEntity#<init>(EntityType, World) Transformer]: " + message);
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
//		"Evolution LivingEntityCombatTracker Transformer": {
//			"target": {
//				"type": "CLASS",
//				"name": "net.minecraft.entity.Entity"
//			},
//			"transformer": function(classNode) {
//				var methods = classNode.methods;
//				for (var i in methods) {
//					if (patch(methods[i], CONSTRUCTOR, patchCombatTracker)) {
//						break;
//					}
//				}
//				return classNode;
//			}
//		}
//	};
//}
//
//function patchCombatTracker(instructions) {
//	var attackEntityFrom;
//    for (var i = 0; i < instructions.size(); i++) {
//        var instruction = instructions.get(i);
//        if (instruction.getOpcode() == Opcodes.NEW && instruction.name == ATTACKENTITYFROM) {
//            attackEntityFrom = instruction;
//            break;
//        }
//    }
//}