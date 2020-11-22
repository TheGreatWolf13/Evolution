var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

var REGISTERATTRIBUTES = ASMAPI.mapMethod("func_110147_ax");

function log(message) {
	print("[Evolution PlayerEntity#registerAttributes() Transformer]: " + message);
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
		"Evolution PlayerEntityAttributes Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.player.PlayerEntity"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], REGISTERATTRIBUTES, patchAttributes)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchAttributes(instructions) {
    var returnVoid;
	for (var i = instructions.size() - 1; i >= 0; i--) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == Opcodes.RETURN) {
			returnVoid = instruction;
			break;
		}
	}
    var newInst = new InsnList();
    newInst.add(new VarInsnNode(Opcodes.ALOAD, 0));
    newInst.add(new MethodInsnNode(
    		Opcodes.INVOKESTATIC,
    		"tgw/evolution/hooks/PlayerHooks",
    		"registerAttributes",
    		"(Lnet/minecraft/entity/player/PlayerEntity;)V",
    		false
    ));
    instructions.insertBefore(returnVoid, newInst);
}