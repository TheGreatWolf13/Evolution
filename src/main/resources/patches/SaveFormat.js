var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var GETSAVELIST = ASMAPI.mapMethod("func_75799_b");

function log(message) {
	print("[evolution/SaveFormat Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}
	log("Patching method: " + name + method.desc);
	patchFunction(method.instructions);
	return true;
}

function initializeCoreMod() {
	return {
		"Evolution SaveFormat Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.world.storage.SaveFormat"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], GETSAVELIST, patchSaveList)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchSaveList(instructions) {
    instructions.clear();
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "tgw/evolution/hooks/WorldSelectionHooks",
        "getSaveList",
        "(Lnet/minecraft/world/storage/SaveFormat;)Ljava/util/List;",
        false
    ));
    instructions.add(new InsnNode(Opcodes.ARETURN));
}