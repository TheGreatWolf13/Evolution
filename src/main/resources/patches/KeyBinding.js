var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var GETLOCALIZEDNAME = ASMAPI.mapMethod("func_197978_k");
var KEYCODE = ASMAPI.mapField("field_74512_d");

function log(message) {
	print("[evolution/KeyBinding Transformer]: " + message);
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
		"Evolution KeyBinding Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.settings.KeyBinding"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], GETLOCALIZEDNAME, patchLocalizedName)) {
						methods[i].localVariables.clear();
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchLocalizedName(instructions) {
	instructions.clear();
	//this
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	//keyCode field
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/settings/KeyBinding", KEYCODE, "Lnet/minecraft/client/util/InputMappings$Input;"));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/KeyBindingHooks",
		"getLocalizedName",
		"(Lnet/minecraft/client/settings/KeyBinding;Lnet/minecraft/client/util/InputMappings$Input;)Ljava/lang/String;",
		false
	));
	instructions.add(new InsnNode(Opcodes.ARETURN));
}