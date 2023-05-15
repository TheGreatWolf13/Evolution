var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
var Label = Java.type("org.objectweb.asm.Label");
var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var InvokeDynamicInsnNode = Java.type("org.objectweb.asm.tree.InvokeDynamicInsnNode");
var Handle = Java.type("org.objectweb.asm.Handle");
var IntInsnNode = Java.type("org.objectweb.asm.tree.IntInsnNode");

var VARIANT = ASMAPI.mapField("f_119435_");
var GET_NAMESPACE = ASMAPI.mapMethod("m_135827_");
var GET_PATH = ASMAPI.mapMethod("m_135815_");

function log(message) {
	print("[evolution/ModelResourceLocation Transformer]: " + message);
}

function patch(method, name, desc, patchFunction) {
	if (method.name != name) {
		return false;
	}
	if (method.desc != desc) {
	    return false;
	}
	log("Patching method: " + name + method.desc);
	patchFunction(method.instructions);
	return true;
}

function initializeCoreMod() {
	return {
		"Evolution ModelResourceLocation Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.resources.model.ModelResourceLocation"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				var first = false;
				var second = false;
				var third = false;
				var fourth = false;
				for (var i in methods) {
					if (!first && patch(methods[i], "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", patchResLoc3Strings)) {
						if (second && third && fourth) {
						    break;
						}
						first = true;
					}
					if (!second && patch(methods[i], "<init>", "(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)V", patchResLocRL1String)) {
                    	if (first && third && fourth) {
                            break;
                        }
                        second = true;
                    }
                    if (!third && patch(methods[i], "<init>", "(Ljava/lang/String;)V", patchResLoc1String)) {
                        if (first && second && fourth) {
                            break;
                        }
                        third = true;
                    }
                    if (!fourth && patch(methods[i], "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", patchResLoc2Strings)) {
                        if (first && second && third) {
                            break;
                        }
                        fourth = true;
                    }
				}
				return classNode;
			}
		}
	};
}

function patchResLoc3Strings(instructions) {
    instructions.clear();
    //Call ResourceLocation constructor
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/resources/ResourceLocation", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V"));
    //Assign var 3 to field variant
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
    instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/util/Locale", "ROOT", "Ljava/util/Locale;"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toLowerCase", "(Ljava/util/Locale;)Ljava/lang/String;"));
    instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/resources/model/ModelResourceLocation", VARIANT, "Ljava/lang/String;"));
    instructions.add(new InsnNode(Opcodes.RETURN));
}

function patchResLocRL1String(instructions) {
    instructions.clear();
    //Call ModelResourceLocation constructor with 3 strings
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/resources/ResourceLocation", GET_NAMESPACE, "()Ljava/lang/String;"));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/resources/ResourceLocation", GET_PATH, "()Ljava/lang/String;"));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/client/resources/model/ModelResourceLocation", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"));
    instructions.add(new InsnNode(Opcodes.RETURN));
}

function patchResLoc1String(instructions) {
    instructions.clear();
    //Call ResourceLocation constructor with 1 string
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new InsnNode(Opcodes.ICONST_0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new IntInsnNode(Opcodes.BIPUSH, 35));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I"));
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new VarInsnNode(Opcodes.ISTORE, 2));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/resources/ResourceLocation", "<init>", "(Ljava/lang/String;)V"));
    //Assign substring to field variant
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
    instructions.add(new InsnNode(Opcodes.ICONST_1));
    instructions.add(new InsnNode(Opcodes.IADD));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(I)Ljava/lang/String;"));
    instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/util/Locale", "ROOT", "Ljava/util/Locale;"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toLowerCase", "(Ljava/util/Locale;)Ljava/lang/String;"));
    instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/resources/model/ModelResourceLocation", VARIANT, "Ljava/lang/String;"));
    instructions.add(new InsnNode(Opcodes.RETURN));
}

function patchResLoc2Strings(instructions) {
    instructions.clear();
    //Call ResourceLocation constructor with 1 string
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/resources/ResourceLocation", "<init>", "(Ljava/lang/String;)V"));
    //Assign var 2 to field variant
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
    instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/util/Locale", "ROOT", "Ljava/util/Locale;"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "toLowerCase", "(Ljava/util/Locale;)Ljava/lang/String;"));
    instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/resources/model/ModelResourceLocation", VARIANT, "Ljava/lang/String;"));
    instructions.add(new InsnNode(Opcodes.RETURN));
}