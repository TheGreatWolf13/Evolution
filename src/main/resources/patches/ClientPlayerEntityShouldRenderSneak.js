var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

var SHOULDRENDERSNEAK = ASMAPI.mapMethod("func_213287_bg");

function log(message) {
	print("[evolution/ ClientPlayerEntity#shouldRenderSneaking() Transformer]: " + message);
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
		"Evolution ClientPlayerEntityShouldRenderSneak Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.entity.player.ClientPlayerEntity"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], SHOULDRENDERSNEAK, patchSneak)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchSneak(instructions) {
    instructions.clear();
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new MethodInsnNode(
    	Opcodes.INVOKESPECIAL,
    	"net/minecraft/entity/Entity",
    	"shouldRenderSneaking",
    	"()Z",
    	false
    ));
    instructions.add(new InsnNode(Opcodes.IRETURN));
}