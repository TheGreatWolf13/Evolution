var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");

var REGISTERATTRIBUTES = ASMAPI.mapMethod("func_110147_ax");
var GETHURTSOUND = ASMAPI.mapMethod("func_184601_bQ");
var TRAVEL = ASMAPI.mapMethod("func_213352_e");
var ISJUMPING = ASMAPI.mapField("field_70703_bu");
var FLAGS = ASMAPI.mapField("field_184240_ax");
var addMovementStat = ASMAPI.mapMethod("func_71000_j");
var addMountedMovementStat = ASMAPI.mapMethod("func_71015_k");

function log(message) {
	print("[evolution/PlayerEntity Transformer]: " + message);
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
		"Evolution PlayerEntity Transformer": {
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
				for (var i in methods) {
                    if (patch(methods[i], GETHURTSOUND, patchHurtSound)) {
                        methods[i].localVariables.clear();
                        break;
                    }
                }
                for (var i in methods) {
                    if (patch(methods[i], TRAVEL, patchTravel)) {
                        methods[i].localVariables.clear();
                        break;
                    }
                }
                for (var i in methods) {
                    if (patch(methods[i], addMovementStat, patchMovementStat)) {
                        methods[i].localVariables.clear();
                        break;
                    }
                }
                for (var i in methods) {
                    if (patch(methods[i], addMountedMovementStat, patchMountedMovementStat)) {
                        methods[i].localVariables.clear();
                        break;
                    }
                }
				return classNode;
			}
		}
	};
}

function patchMountedMovementStat(instructions) {
    instructions.clear();
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.DLOAD, 1));
    instructions.add(new VarInsnNode(Opcodes.DLOAD, 3));
    instructions.add(new VarInsnNode(Opcodes.DLOAD, 5));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "tgw/evolution/hooks/PlayerHooks",
        "addMountedMovementStat",
        "(Lnet/minecraft/entity/player/PlayerEntity;DDD)V",
        false
    ));
    instructions.add(new InsnNode(Opcodes.RETURN));
}

function patchMovementStat(instructions) {
    instructions.clear();
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new VarInsnNode(Opcodes.DLOAD, 1));
    instructions.add(new VarInsnNode(Opcodes.DLOAD, 3));
    instructions.add(new VarInsnNode(Opcodes.DLOAD, 5));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "tgw/evolution/hooks/PlayerHooks",
        "addMovementStat",
        "(Lnet/minecraft/entity/player/PlayerEntity;DDD)V",
        false
    ));
    instructions.add(new InsnNode(Opcodes.RETURN));
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

function patchHurtSound(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/PlayerHooks",
		"getHurtSound",
		"(Lnet/minecraft/util/DamageSource;)Lnet/minecraft/util/SoundEvent;",
		false
	));
	instructions.add(new InsnNode(Opcodes.ARETURN));
}

function patchTravel(instructions) {
	instructions.clear();
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
	instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/entity/LivingEntity", ISJUMPING, "Z"));
	instructions.add(new FieldInsnNode(
	    Opcodes.GETSTATIC,
	    "net/minecraft/entity/Entity",
	    FLAGS,
	    "Lnet/minecraft/network/datasync/DataParameter;"
	));
	instructions.add(new MethodInsnNode(
		Opcodes.INVOKESTATIC,
		"tgw/evolution/hooks/PlayerHooks",
		"travel",
		"(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;ZLnet/minecraft/network/datasync/DataParameter;)V",
		false
	));
	instructions.add(new InsnNode(Opcodes.RETURN));
}