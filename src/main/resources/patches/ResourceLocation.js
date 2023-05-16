var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
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

var NAMESPACE = ASMAPI.mapField("f_135804_");
var PATH = ASMAPI.mapField("f_135805_");
var IS_VALID_NAMESPACE = ASMAPI.mapMethod("m_135843_");
var IS_VALID_PATH = ASMAPI.mapMethod("m_135841_");


function log(message) {
	print("[evolution/ResourceLocation Transformer]: " + message);
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
		"Evolution ResourceLocation Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.resources.ResourceLocation"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				var first = false;
				var second = false;
				for (var i in methods) {
					if (!first && patch(methods[i], "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", patchResLocTwoStrings)) {
						if (second) {
						    break;
						}
						first = true;
					}
					if (!second && patch(methods[i], "<init>", "(Ljava/lang/String;)V", patchResLocOneString)) {
                    	if (first) {
                            break;
                        }
                        second = true;
                    }
				}
				return classNode;
			}
		}
	};
}

function patchResLocTwoStrings(instructions) {
    instructions.clear();
    //Call object constructor
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
    //Assign field namespace
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/apache/commons/lang3/StringUtils", "isEmpty", "(Ljava/lang/CharSequence;)Z"));
    var lbl17 = new LabelNode(new Label());
    instructions.add(new JumpInsnNode(Opcodes.IFEQ, lbl17));
    instructions.add(new LdcInsnNode("minecraft"));
    var lbl18 = new LabelNode(new Label());
    instructions.add(new JumpInsnNode(Opcodes.GOTO, lbl18));
    instructions.add(lbl17);
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(lbl18);
    instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/resources/ResourceLocation", NAMESPACE, "Ljava/lang/String;"));
    //Assign field path
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
    instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/resources/ResourceLocation", PATH, "Ljava/lang/String;"));
    //Check if namespace is valid
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", NAMESPACE, "Ljava/lang/String;"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceLocation", IS_VALID_NAMESPACE, "(Ljava/lang/String;)Z"));
    var lbl45 = new LabelNode(new Label());
    instructions.add(new JumpInsnNode(Opcodes.IFNE, lbl45));
    //If true goto label 45
    //      Throw exception
    instructions.add(new TypeInsnNode(Opcodes.NEW, "net/minecraft/ResourceLocationException"));
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", NAMESPACE, "Ljava/lang/String;"));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", PATH, "Ljava/lang/String;"));
    var handle0 = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/StringConcatFactory", "makeConcatWithConstants", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;", false);
    instructions.add(new InvokeDynamicInsnNode("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", handle0, "Non [a-z0-9_.-] character in namespace of location: \u0001:\u0001"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/ResourceLocationException", "<init>", "(Ljava/lang/String;)V"));
    instructions.add(new InsnNode(Opcodes.ATHROW));
    //Label 45
    instructions.add(lbl45);
    //Check if path is valid
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", PATH, "Ljava/lang/String;"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceLocation", IS_VALID_PATH, "(Ljava/lang/String;)Z"));
    var lbl76 = new LabelNode(new Label());
    instructions.add(new JumpInsnNode(Opcodes.IFNE, lbl76));
    //If true goto label 76
    //      Throw exception
    instructions.add(new TypeInsnNode(Opcodes.NEW, "net/minecraft/ResourceLocationException"));
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", NAMESPACE, "Ljava/lang/String;"));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", PATH, "Ljava/lang/String;"));
    instructions.add(new InvokeDynamicInsnNode("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", handle0, "Non [a-z0-9/._-] character in path of location: \u0001:\u0001"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/ResourceLocationException", "<init>", "(Ljava/lang/String;)V"));
    instructions.add(new InsnNode(Opcodes.ATHROW));
    //Label 76
    instructions.add(lbl76);
    //Return
    instructions.add(new InsnNode(Opcodes.RETURN));
}

function patchResLocOneString(instructions) {
    instructions.clear();
    //Call object constructor
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
    //Assign index of ':' on var 1 to var 2
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new IntInsnNode(Opcodes.BIPUSH, 58));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "indexOf", "(I)I"));
    instructions.add(new VarInsnNode(Opcodes.ISTORE, 2));
    //Assign "minecraft" to var 3
    instructions.add(new LdcInsnNode("minecraft"));
    instructions.add(new VarInsnNode(Opcodes.ASTORE, 3));
    //Assign var 1 to var 4
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new VarInsnNode(Opcodes.ASTORE, 4));
    //If var 2 is less than 0 goto label 42
    var lbl42 = new LabelNode(new Label());
    instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
    instructions.add(new JumpInsnNode(Opcodes.IFLT, lbl42));
    //      Assign substring to var 4
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
    instructions.add(new InsnNode(Opcodes.ICONST_1));
    instructions.add(new InsnNode(Opcodes.IADD));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(I)Ljava/lang/String;"));
    instructions.add(new VarInsnNode(Opcodes.ASTORE, 4));
    //      If var 2 is less than 1 goto label 42
    instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
    instructions.add(new InsnNode(Opcodes.ICONST_1));
    instructions.add(new JumpInsnNode(Opcodes.IF_ICMPLT, lbl42));
    //          Assign substring to var 3
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
    instructions.add(new InsnNode(Opcodes.ICONST_0));
    instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
    instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;"));
    instructions.add(new VarInsnNode(Opcodes.ASTORE, 3));
    //Label 42
    instructions.add(lbl42);
    //Assign field namespace
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
    instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/resources/ResourceLocation", NAMESPACE, "Ljava/lang/String;"));
    //Assign field path
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
    instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/resources/ResourceLocation", PATH, "Ljava/lang/String;"));
    //Check if namespace is valid
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", NAMESPACE, "Ljava/lang/String;"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceLocation", IS_VALID_NAMESPACE, "(Ljava/lang/String;)Z"));
    var lbl84 = new LabelNode(new Label());
    instructions.add(new JumpInsnNode(Opcodes.IFNE, lbl84));
    //If true goto label 84
    //      Throw exception
    instructions.add(new TypeInsnNode(Opcodes.NEW, "net/minecraft/ResourceLocationException"));
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", NAMESPACE, "Ljava/lang/String;"));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", PATH, "Ljava/lang/String;"));
    var handle0 = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/StringConcatFactory", "makeConcatWithConstants", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;", false);
    instructions.add(new InvokeDynamicInsnNode("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", handle0, "Non [a-z0-9_.-] character in namespace of location: \u0001:\u0001"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/ResourceLocationException", "<init>", "(Ljava/lang/String;)V"));
    instructions.add(new InsnNode(Opcodes.ATHROW));
    //Label 84
    instructions.add(lbl84);
    //Check if path is valid
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", PATH, "Ljava/lang/String;"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/resources/ResourceLocation", IS_VALID_PATH, "(Ljava/lang/String;)Z"));
    var lbl115 = new LabelNode(new Label());
    instructions.add(new JumpInsnNode(Opcodes.IFNE, lbl115));
    //If true goto label 115
    //      Throw exception
    instructions.add(new TypeInsnNode(Opcodes.NEW, "net/minecraft/ResourceLocationException"));
    instructions.add(new InsnNode(Opcodes.DUP));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", NAMESPACE, "Ljava/lang/String;"));
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/resources/ResourceLocation", PATH, "Ljava/lang/String;"));
    instructions.add(new InvokeDynamicInsnNode("makeConcatWithConstants", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", handle0, "Non [a-z0-9/._-] character in path of location: \u0001:\u0001"));
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/ResourceLocationException", "<init>", "(Ljava/lang/String;)V"));
    instructions.add(new InsnNode(Opcodes.ATHROW));
    //Label 115
    instructions.add(lbl115);
    //Return
    instructions.add(new InsnNode(Opcodes.RETURN));
}