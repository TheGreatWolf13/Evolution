var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var LineNumberNode = Java.type("org.objectweb.asm.tree.LineNumberNode");
var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
var FrameNode = Java.type("org.objectweb.asm.tree.FrameNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");
var TryCatchBlockNode = Java.type("org.objectweb.asm.tree.TryCatchBlockNode");
var LocalVariableNode = Java.type("org.objectweb.asm.tree.LocalVariableNode");

var getTextComponent = ASMAPI.mapMethod("func_151000_E");

function log(message) {
	print("[evolution/NewChatGui Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}
	log("Patching method: " + name + method.desc);
	patchFunction(method.instructions);
	return true;
}

function getLine(instructions, number, index) {
    var indexOld = index;
    for (var i = 0; i < instructions.size(); i++) {
        var inst = instructions.get(i);
        if (inst instanceof LineNumberNode) {
            if (inst.line == number) {
                index--;
                if (index == 0) {
                    return i;
                }
            }
        }
    }
    log("Could not find line " + number + " index " + indexOld);
    return -1;
}

function initializeCoreMod() {
	return {
		"Evolution NewChatGui Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.gui.NewChatGui"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], getTextComponent, patchGetTextComponent)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchGetTextComponent(instructions) {
    var line202 = getLine(instructions, 202, 1);
    var ldc = instructions.get(line202 + 8);
    ldc.cst = 50.0;
}

