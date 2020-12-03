var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");

var DRAWNAMEPLATE = ASMAPI.mapMethod("func_190052_a");

function log(message) {
	print("[evolution/ GameRenderer#drawNameplate(FontRenderer, String, float, float, float, int, float, float, boolean) Transformer]: " + message);
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
		"Evolution GameRendererDrawNameplate Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.renderer.GameRenderer"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], DRAWNAMEPLATE, patchDrawNameplate)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchDrawNameplate(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	instructions.add(new VarInsnNode(Opcodes.FLOAD, 2));
	instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));
	instructions.add(new VarInsnNode(Opcodes.FLOAD, 4));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 5));
	instructions.add(new VarInsnNode(Opcodes.FLOAD, 6));
	instructions.add(new VarInsnNode(Opcodes.FLOAD, 7));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 8));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/GameRendererHooks",
		"drawNameplate",
		"(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;FFFIFFZ)V",
		false
	));
	instructions.add(new InsnNode(Opcodes.RETURN));
}