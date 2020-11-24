var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
var Opcodes = Java.type("org.objectweb.asm.Opcodes");

var LIVINGTICK = ASMAPI.mapMethod("func_70636_d");
var ISSERVERWORLD = ASMAPI.mapMethod("func_70613_aW");
var SETMOTIONVEC = ASMAPI.mapMethod("func_213293_j");
var SETMOTIONDOUBLE = ASMAPI.mapMethod("func_213317_d");
var GETMOTION = ASMAPI.mapMethod("func_213322_ci");

function log(message) {
	print("[Evolution LivingEntity#livingTick() Transformer]: " + message);
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
		"Evolution LivingEntityLivingTick Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.entity.LivingEntity"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], LIVINGTICK, patchLivingTick)) {
						break;
					}
				}
				return classNode;
			}
		}
	};
}

function patchLivingTick(instructions) {
	var isServerWorld;
	var setMotionVec;
	var setMotionDouble;
    for (var i = 0; i < instructions.size(); i++) {
    	var instruction = instructions.get(i);
    	if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL && instruction.name == ISSERVERWORLD) {
    		isServerWorld = instruction;
    		break;
    	}
    }
    for (var i = 0; i < instructions.size(); i++) {
        var instruction = instructions.get(i);
        if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL && instruction.name == SETMOTIONVEC) {
        	setMotionVec = instruction;
        	break;
        }
    }
    for (var i = 0; i < instructions.size(); i++) {
        var instruction = instructions.get(i);
        if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL && instruction.name == SETMOTIONDOUBLE && instruction.desc != "(Lnet/minecraft/util/math/Vec3d;)V") {
            setMotionDouble = instruction;
            break;
        }
    }

    var this0 = isServerWorld.getPrevious();
    var ifne = isServerWorld.getNext();
    instructions.remove(this0);
    instructions.remove(isServerWorld);
    instructions.remove(ifne);

    var scale = setMotionVec.getPrevious();
    var ldc = scale.getPrevious();
    var getMotion = ldc.getPrevious();
    var this2 = getMotion.getPrevious();
    var this1 = this2.getPrevious();
    instructions.remove(this1);
    instructions.remove(this2);
    instructions.remove(getMotion);
    instructions.remove(ldc);
    instructions.remove(scale);
    instructions.remove(setMotionVec);

    var dload2 = setMotionDouble.getPrevious();
    var dload1 = dload2.getPrevious();
    var dload0 = dload1.getPrevious();
    var this3 = dload0.getPrevious();
    instructions.remove(this3);
    instructions.remove(dload0);
    instructions.remove(dload1);
    instructions.remove(dload2);
    instructions.remove(setMotionDouble);

    for (var j = 0; j < 3; j++) {
        for (var i = 0; i < instructions.size(); i++) {
            var instruction = instructions.get(i);
            if (instruction.getOpcode() == Opcodes.DCONST_0 && instruction.getNext().getOpcode() == Opcodes.DSTORE) {
                instructions.remove(instruction.getNext());
                instructions.remove(instruction);
                break;
            }
        }
    }

    for (var j = 0; j < 3; j++) {
        for (var i = 0; i < instructions.size(); i++) {
            var instruction = instructions.get(i);
            if (instruction.getOpcode() == Opcodes.INVOKESTATIC && instruction.owner == "java/lang/Math" && instruction.name == "abs") {
                var getField = instruction.getPrevious();
                var aload = getField.getPrevious();
                var ldc = instruction.getNext();
                var dcmpg = ldc.getNext();
                var ifge = dcmpg.getNext();
                instructions.remove(aload);
                instructions.remove(getField);
                instructions.remove(instruction);
                instructions.remove(ldc);
                instructions.remove(dcmpg);
                instructions.remove(ifge);
                break;
            }
        }
    }

    for (var j = 0; j < 3; j++) {
        for (var i = 0; i < instructions.size(); i++) {
            var instruction = instructions.get(i);
            if (instruction.getOpcode() == Opcodes.GETFIELD && instruction.owner == "net/minecraft/util/math/Vec3d") {
                instructions.remove(instruction.getPrevious());
                instructions.remove(instruction.getNext());
                instructions.remove(instruction);
                break;
            }
        }
    }

    for (var i = 0; i < instructions.size(); i++) {
        var instruction = instructions.get(i);
        if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL && instruction.name == GETMOTION) {
            instructions.remove(instruction.getPrevious());
            instructions.remove(instruction.getNext());
            instructions.remove(instruction);
            break;
        }
    }
}