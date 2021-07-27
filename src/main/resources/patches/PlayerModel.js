var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

var SETROTATIONANGLES = ASMAPI.mapMethod("func_212844_a_");

function log(message) {
	print("[evolution/PlayerModel Transformer]: " + message);
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
		"Evolution PlayerModel Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.renderer.entity.model.PlayerModel"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], SETROTATIONANGLES, patchRotationAngles)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchRotationAngles(instructions) {
    var ret;
    for (var i = 0; i < instructions.size(); i++) {
        var inst = instructions.get(i);
        if (inst.getOpcode() == Opcodes.RETURN) {
            ret = inst;
        }
    }
    instructions.remove(ret);
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	instructions.add(new VarInsnNode(Opcodes.FLOAD, 4));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/PlayerRenderHooks",
		"setRotationAngles",
		"(Lnet/minecraft/client/renderer/entity/model/PlayerModel;Lnet/minecraft/entity/LivingEntity;F)V",
		false
	));
    instructions.add(new InsnNode(Opcodes.RETURN));
}