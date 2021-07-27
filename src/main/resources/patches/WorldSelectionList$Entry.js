var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var RENDER = ASMAPI.mapMethod("func_211234_a");
var WORLDSUMMARY = ASMAPI.mapField("field_214451_d");
var DYNAMICTEXTURE = ASMAPI.mapField("field_214454_g");
var RESLOC = ASMAPI.mapField("field_214452_e");
var SCREEN = ASMAPI.mapField("field_214450_c");

function log(message) {
	print("[evolution/WorldSelectionList$Entry Transformer]: " + message);
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
		"Evolution WorldSelectionList$Entry Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.gui.screen.WorldSelectionList$Entry"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], RENDER, patchRender)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchRender(instructions) {
	instructions.clear();
	//this
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	//WorldSummary field
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/screen/WorldSelectionList$Entry", WORLDSUMMARY, "Lnet/minecraft/world/storage/WorldSummary;"));
    //DynamicTexture field
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/screen/WorldSelectionList$Entry", DYNAMICTEXTURE, "Lnet/minecraft/client/renderer/texture/DynamicTexture;"));
    //ResourceLocation field
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/screen/WorldSelectionList$Entry", RESLOC, "Lnet/minecraft/util/ResourceLocation;"));
    //WorldSelectionScreen field
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/screen/WorldSelectionList$Entry", SCREEN, "Lnet/minecraft/client/gui/screen/WorldSelectionScreen;"));
    //Parameters
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 5));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 6));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 7));
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 8));
	instructions.add(new VarInsnNode(Opcodes.FLOAD, 9));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/WorldSelectionHooks",
		"render",
		"(Lnet/minecraft/client/gui/screen/WorldSelectionList$Entry;Lnet/minecraft/world/storage/WorldSummary;Lnet/minecraft/client/renderer/texture/DynamicTexture;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/gui/screen/WorldSelectionScreen;IIIIIIIZF)V",
		false
	));
	instructions.add(new InsnNode(Opcodes.RETURN));
}