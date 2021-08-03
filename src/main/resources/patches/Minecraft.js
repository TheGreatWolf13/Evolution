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

var ISHANDACTIVE = ASMAPI.mapMethod("func_184587_cr");
var PROCESSKEYBINDS = ASMAPI.mapMethod("func_184117_aA");
var FREEMEMORY = ASMAPI.mapMethod("func_71398_f");
var RUN = ASMAPI.mapMethod("func_213178_c");
var getCrashReport = ASMAPI.mapMethod("func_71575_a");
var addGraphicsAndWorldToCrashReport = ASMAPI.mapMethod("func_71396_d");
var LOGGER = ASMAPI.mapField("field_211189_a");

function log(message) {
	print("[evolution/Minecraft Transformer]: " + message);
}

function patch(method, name, patchFunction) {
	if (method.name != name) {
		return false;
	}
	log("Patching method: " + name + method.desc);
	patchFunction(method.instructions);
	return true;
}

function patchSpecial(method, name) {
	if (method.name != name) {
		return false;
	}
	log("Patching method: " + name + method.desc);
	return true;
}

function initializeCoreMod() {
	return {
		"Evolution Minecraft Transformer": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.client.Minecraft"
			},
			"transformer": function(classNode) {
				var methods = classNode.methods;
				for (var i in methods) {
					if (patch(methods[i], PROCESSKEYBINDS, patchKeyBinds)) {
						break;
					}
				}
				for (var i in methods) {
                    if (patch(methods[i], FREEMEMORY, patchFreeMemory)) {
						methods[i].localVariables.clear();
						methods[i].tryCatchBlocks.clear();
                        break;
                    }
                }
                for (var i in methods) {
                    if (patchSpecial(methods[i], RUN)) {
                        patchRun(methods[i]);
                        break;
                    }
                }
				return classNode;
			}
		}
	};
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

function patchRun(method) {
    var instructions = method.instructions;

    var line384 = getLine(instructions, 384, 1);
    var L3 = instructions.get(line384 - 1);

    var iload1 = instructions.get(line384 + 3);
    var ifneL22 = instructions.get(line384 + 4);
    var iconst1 = instructions.get(line384 + 5);
    var gotoL23 = instructions.get(line384 + 6);
    var iconst0 = instructions.get(line384 + 9);
    var frameFull = instructions.get(line384 + 11);
    instructions.remove(iload1);
    instructions.remove(ifneL22);
    instructions.remove(iconst1);
    instructions.remove(gotoL23);
    instructions.remove(iconst0);
    instructions.insert(frameFull, new InsnNode(Opcodes.ICONST_1));

    var line386 = getLine(instructions, 386, 1);
    var iload1 = instructions.get(line386 + 1);
    var ifeqL25 = instructions.get(line386 + 2);
    instructions.remove(iload1);
    instructions.remove(ifeqL25);

    var line387 = getLine(instructions, 387, 1);
    var aload2 = instructions.get(line387 + 1);
    var athrow = instructions.get(line387 + 2);
    instructions.remove(aload2);
    instructions.remove(athrow);

    var line391 = getLine(instructions, 391, 1);
    var new0 = instructions.get(line391 + 2);
    var init = instructions.get(line391 + 4);
    new0.desc = "tgw/evolution/client/gui/ScreenMemoryError";
    instructions.insertBefore(init, new VarInsnNode(Opcodes.ILOAD, 1));
    init.owner = "tgw/evolution/client/gui/ScreenMemoryError";
    init.desc = "(Z)V";

    var line377 = getLine(instructions, 377, 1);
    var L18 = instructions.get(line377 - 1);

    var line395 = getLine(instructions, 395, 1);
    var L4 = instructions.get(line395 - 1);

    //reportedException
    var reportedExc = new InsnList();
    var L46 = new LabelNode();
    reportedExc.add(L46);
    reportedExc.add(new FrameNode(Opcodes.F_FULL, 2, ["net/minecraft/client/Minecraft", "I"], 1, ["net/minecraft/crash/ReportedException"]));
    reportedExc.add(new VarInsnNode(Opcodes.ASTORE, 2));
    var L47 = new LabelNode();
    reportedExc.add(L47);
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 0));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        "net/minecraft/client/Minecraft",
        FREEMEMORY,
        "()V",
        false
    ));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 0));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 2));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        "net/minecraft/crash/ReportedException",
        getCrashReport,
        "()Lnet/minecraft/crash/CrashReport;",
        false
    ));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        "net/minecraft/client/Minecraft",
        addGraphicsAndWorldToCrashReport,
        "(Lnet/minecraft/crash/CrashReport;)Lnet/minecraft/crash/CrashReport;",
        false
    ));
    reportedExc.add(new InsnNode(Opcodes.POP));
    reportedExc.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/client/Minecraft", LOGGER, "Lorg/apache/logging/log4j/Logger;"));
    reportedExc.add(new LdcInsnNode("Reported exception thrown!"));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 2));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKEINTERFACE,
        "org/apache/logging/log4j/Logger",
        "fatal",
        "(Ljava/lang/String;Ljava/lang/Throwable;)V",
        true
    ));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 0));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 2));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        "net/minecraft/crash/ReportedException",
        getCrashReport,
        "()Lnet/minecraft/crash/CrashReport;",
        false
    ));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "tgw/evolution/hooks/MinecraftHooks",
        "displayCrashScreen",
        "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/crash/CrashReport;)V",
        false
    ));
    var L48 = new LabelNode();
    reportedExc.add(L48);
    reportedExc.add(new JumpInsnNode(Opcodes.GOTO, L18));
    //throwable
    var L49 = new LabelNode();
    reportedExc.add(L49);
    reportedExc.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, ["java/lang/Throwable"]));
    reportedExc.add(new VarInsnNode(Opcodes.ASTORE, 2));
    var L50 = new LabelNode();
    reportedExc.add(L50);
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 0));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        "net/minecraft/client/Minecraft",
        FREEMEMORY,
        "()V",
        false
    ));
    reportedExc.add(new TypeInsnNode(Opcodes.NEW, "net/minecraft/crash/CrashReport"));
    reportedExc.add(new InsnNode(Opcodes.DUP));
    reportedExc.add(new LdcInsnNode("Unexpected error"));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 2));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKESPECIAL,
        "net/minecraft/crash/CrashReport",
        "<init>",
        "(Ljava/lang/String;Ljava/lang/Throwable;)V",
        false
    ));
    reportedExc.add(new VarInsnNode(Opcodes.ASTORE, 3));
    var L51 = new LabelNode();
    reportedExc.add(L51);
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 0));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 3));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        "net/minecraft/client/Minecraft",
        addGraphicsAndWorldToCrashReport,
        "(Lnet/minecraft/crash/CrashReport;)Lnet/minecraft/crash/CrashReport;",
        false
    ));
    reportedExc.add(new InsnNode(Opcodes.POP));
    reportedExc.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/client/Minecraft", LOGGER, "Lorg/apache/logging/log4j/Logger;"));
    reportedExc.add(new LdcInsnNode("Unexpected exception thrown!"));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 2));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKEINTERFACE,
        "org/apache/logging/log4j/Logger",
        "fatal",
        "(Ljava/lang/String;Ljava/lang/Throwable;)V",
        true
    ));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 0));
    reportedExc.add(new VarInsnNode(Opcodes.ALOAD, 3));
    reportedExc.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "tgw/evolution/hooks/MinecraftHooks",
        "displayCrashScreen",
        "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/crash/CrashReport;)V",
        false
    ));
    var L52 = new LabelNode();
    reportedExc.add(L52);
    reportedExc.add(new JumpInsnNode(Opcodes.GOTO, L18));
    //end exceptions
    var line395_2 = getLine(instructions, 395, 2);
    var gotoL18 = instructions.get(line395_2 + 1);
    instructions.insert(gotoL18, reportedExc);
    //update tryCatches
    var tryCatch = method.tryCatchBlocks;
    tryCatch.add(2, new TryCatchBlockNode(L3, L4, L46, "net/minecraft/crash/ReportedException"));
    tryCatch.add(3, new TryCatchBlockNode(L3, L4, L49, "java/lang/Throwable"));
    //update variables
    method.maxLocals = 5;
    var localVars = method.localVariables;
    localVars.add(new LocalVariableNode("e", "Lnet/minecraft/crash/ReportedException;", null, L47, L48, 2));
    localVars.add(new LocalVariableNode("report", "Lnet/minecraft/crash/CrashReport;", null, L51, L52, 3));
    localVars.add(new LocalVariableNode("t", "Ljava/lang/Throwable;", null, L50, L52, 2));
}

function patchFreeMemory(instructions) {
    instructions.clear();
    instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    instructions.add(new MethodInsnNode(
        Opcodes.INVOKESTATIC,
        "tgw/evolution/hooks/MinecraftHooks",
        "freeMemory",
        "(Lnet/minecraft/client/Minecraft;)V",
        false
    ));
    instructions.add(new InsnNode(Opcodes.RETURN));
}

function patchKeyBinds(instructions) {
    //line 1466
	var isHandActive;
	for (var i = 0; i < instructions.size(); i++) {
	    var inst = instructions.get(i);
	    if (inst instanceof MethodInsnNode && inst.name == ISHANDACTIVE) {
	        isHandActive = inst;
	        break;
	    }
	}
	var player_0 = isHandActive.getPrevious();
	var this_0 = player_0.getPrevious();
	var if_0 = isHandActive.getNext();
	var label_47 = if_0.getNext();
    //1467
    var linenumber_1467 = label_47.getNext();
	var this_1 = linenumber_1467.getNext();
	var gameSettings_0 = this_1.getNext();
	var keyBindUseItem_0 = gameSettings_0.getNext();
	var isKeyDown_0 = keyBindUseItem_0.getNext();
	var if_1 = isKeyDown_0.getNext();
	var label_49 = if_1.getNext();
	//1468
    var linenumber_1468 = label_49.getNext();
    var this_2 = linenumber_1468.getNext();
    var playerController_0 = this_2.getNext();
    var this_3 = playerController_0.getNext();
    var player_1 = this_3.getNext();
    var onStoppedUsingItem_0 = player_1.getNext();
    var label_48 = onStoppedUsingItem_0.getNext();
    //1471
    var linenumber_1471 = label_48.getNext();
    var frameSame_0 = linenumber_1471.getNext();
    var this_4 = frameSame_0.getNext();
    var gameSettings_1 = this_4.getNext();
    var keyBindAttack_0 = gameSettings_1.getNext();
    var isPressed_0 = keyBindAttack_0.getNext();
    var if_2 = isPressed_0.getNext();
    var goto_0 = if_2.getNext();
    var label_50 = goto_0.getNext();
    //1475
    var linenumber_1475 = label_50.getNext();
    var frameSame_1 = linenumber_1475.getNext();
    var this_5 = frameSame_1.getNext();
    var gameSettings_2 = this_5.getNext();
    var keyBindUseItem_1 = gameSettings_2.getNext();
    var isPressed_1 = keyBindUseItem_1.getNext();
    var if_3 = isPressed_1.getNext();
    var goto_1 = if_3.getNext();
    var label_51 = goto_1.getNext();
    //1479
    var linenumber_1479 = label_51.getNext();
    var frameSame_2 = linenumber_1479.getNext();
    var this_6 = frameSame_2.getNext();
    var gameSettings_3 = this_6.getNext();
    var keyBindPickBlock_0 = gameSettings_3.getNext();
    var isPressed_2 = keyBindPickBlock_0.getNext();
    var if_4 = isPressed_2.getNext();
    var goto_2 = if_4.getNext();
    var label_46 = goto_2.getNext();
    //1483
    var linenumber_1483 = label_46.getNext();
    var frameSame_3 = linenumber_1483.getNext();
    var this_7 = frameSame_3.getNext();
    var gameSettings_4 = this_7.getNext();
    var keyBindAttack_1 = gameSettings_4.getNext();
    var isPressed_3 = keyBindAttack_1.getNext();
    var if_5 = isPressed_3.getNext();
    var label_54 = if_5.getNext();
    //1484
    var linenumber_1484 = label_54.getNext();
    var this_8 = linenumber_1484.getNext();
    var clickMouse_0 = this_8.getNext();
    var goto_3 = clickMouse_0.getNext();
    var label_53 = goto_3.getNext();
    //1487
    var linenumber_1487 = label_53.getNext();
    var frameSame_4 = linenumber_1487.getNext();
    var this_9 = frameSame_4.getNext();
    var gameSettings_5 = this_9.getNext();
    var keyBindUseItem_2 = gameSettings_5.getNext();
    var isPressed_4 = keyBindUseItem_2.getNext();
    var if_6 = isPressed_4.getNext();
    var label_56 = if_6.getNext();
    //1488
    var linenumber_1488 = label_56.getNext();
    var this_10 = linenumber_1488.getNext();
    var rightClickMouse_0 = this_10.getNext();
    var goto_4 = rightClickMouse_0.getNext();
    var label_55 = goto_4.getNext();
    //1491
    var linenumber_1491 = label_55.getNext();
    var frameSame_5 = linenumber_1491.getNext();
    var this_11 = frameSame_5.getNext();
    var gameSettings_6 = this_11.getNext();
    var keyBindPickBlock_1 = gameSettings_6.getNext();
    var isPressed_5 = keyBindPickBlock_1.getNext();
    var if_7 = isPressed_5.getNext();
    var label_57 = if_7.getNext();
    //1492
    var linenumber_1492 = label_57.getNext();
    var this_12 = linenumber_1492.getNext();
    var middleClickMouse_0 = this_12.getNext();
    var goto_5 = middleClickMouse_0.getNext();
    var label_52 = goto_5.getNext();
    //1496
    var linenumber_1496 = label_52.getNext();
    var frameSame_6 = linenumber_1496.getNext();
    var this_13 = frameSame_6.getNext();
    var gameSettings_7 = this_13.getNext();
    var keyBindUseItem_3 = gameSettings_7.getNext();
    var isKeyDown_1 = keyBindUseItem_3.getNext();
    var if_8 = isKeyDown_1.getNext();
    var this_14 = if_8.getNext();
    var rightClickDelayTimer_0 = this_14.getNext();
    var if_9 = rightClickDelayTimer_0.getNext();
    var this_15 = if_9.getNext();
    var player_2 = this_15.getNext();
    var isHandActive_1 = player_2.getNext();
    var if_10 = isHandActive_1.getNext();
    var label_59 = if_10.getNext();
    //1497
    var linenumber_1497 = label_59.getNext();
    var this_16 = linenumber_1497.getNext();
    var rightClickMouse_1 = this_16.getNext();
    var label_58 = rightClickMouse_1.getNext();
    //1500
    var linenumber_1500 = label_58.getNext();
    var frameSame_7 = linenumber_1500.getNext();
    var this_17 = frameSame_7.getNext();
    var this_18 = this_17.getNext();
    var currentScreen = this_18.getNext();
    var ifnonnull = currentScreen.getNext();
    var this_19 = ifnonnull.getNext();
    var gameSettings_8 = this_19.getNext();
    var keyBindAttack_2 = gameSettings_8.getNext();
    var isKeyDown_2 = keyBindAttack_2.getNext();
    var if_11 = isKeyDown_2.getNext();
    var this_20 = if_11.getNext();
    var mouseHelper = this_20.getNext();
    var isMouseGrabbed = mouseHelper.getNext();
    var if_12 = isMouseGrabbed.getNext();
    var iconst1 = if_12.getNext();
    var goto_6 = iconst1.getNext();
    var label_60 = goto_6.getNext();
    var frameSame_8 = label_60.getNext();
    var iconst0 = frameSame_8.getNext();
    var label_61 = iconst0.getNext();
    var frameFull = label_61.getNext();
    var sendClickBlockToController = frameFull.getNext();

    instructions.remove(sendClickBlockToController);
    instructions.remove(mouseHelper);
    instructions.remove(isMouseGrabbed);
    instructions.remove(iconst0);
    instructions.remove(iconst1);
    instructions.remove(currentScreen);
    instructions.remove(isHandActive);
    instructions.remove(isHandActive_1);
    instructions.remove(player_0);
    instructions.remove(player_1);
    instructions.remove(player_2);
    instructions.remove(playerController_0);
    instructions.remove(this_0);
    instructions.remove(this_1);
    instructions.remove(this_2);
    instructions.remove(this_3);
    instructions.remove(this_4);
    instructions.remove(this_5);
    instructions.remove(this_6);
    instructions.remove(this_7);
    instructions.remove(this_8);
    instructions.remove(this_9);
    instructions.remove(this_10);
    instructions.remove(this_11);
    instructions.remove(this_12);
    instructions.remove(this_13);
    instructions.remove(this_14);
    instructions.remove(this_15);
    instructions.remove(this_17);
    instructions.remove(this_18);
    instructions.remove(this_19);
    instructions.remove(this_20);
    instructions.remove(frameSame_0);
    instructions.remove(frameSame_1);
    instructions.remove(frameSame_2);
    instructions.remove(frameSame_3);
    instructions.remove(frameSame_4);
    instructions.remove(frameSame_5);
    instructions.remove(frameSame_6);
    instructions.remove(frameSame_7);
    instructions.remove(frameSame_8);
    instructions.remove(frameFull);
    instructions.remove(if_0);
    instructions.remove(if_1);
    instructions.remove(if_2);
    instructions.remove(if_3);
    instructions.remove(if_4);
    instructions.remove(if_5);
    instructions.remove(if_6);
    instructions.remove(if_7);
    instructions.remove(if_8);
    instructions.remove(if_9);
    instructions.remove(if_10);
    instructions.remove(if_11);
    instructions.remove(if_12);
    instructions.remove(ifnonnull);
    instructions.remove(goto_0);
    instructions.remove(goto_1);
    instructions.remove(goto_2);
    instructions.remove(goto_3);
    instructions.remove(goto_4);
    instructions.remove(goto_5);
    instructions.remove(goto_6);
    instructions.remove(gameSettings_0);
    instructions.remove(gameSettings_1);
    instructions.remove(gameSettings_2);
    instructions.remove(gameSettings_3);
    instructions.remove(gameSettings_4);
    instructions.remove(gameSettings_5);
    instructions.remove(gameSettings_6);
    instructions.remove(gameSettings_7);
    instructions.remove(gameSettings_8);
    instructions.remove(keyBindUseItem_0);
    instructions.remove(keyBindUseItem_1);
    instructions.remove(keyBindUseItem_2);
    instructions.remove(keyBindUseItem_3);
    instructions.remove(keyBindPickBlock_0);
    instructions.remove(keyBindPickBlock_1);
    instructions.remove(keyBindAttack_0);
    instructions.remove(keyBindAttack_1);
    instructions.remove(keyBindAttack_2);
    instructions.remove(rightClickMouse_0);
    instructions.remove(rightClickDelayTimer_0);
    instructions.remove(clickMouse_0);
    instructions.remove(middleClickMouse_0);
    instructions.remove(isKeyDown_0);
    instructions.remove(isKeyDown_1);
    instructions.remove(isKeyDown_2);
    instructions.remove(isPressed_0);
    instructions.remove(isPressed_1);
    instructions.remove(isPressed_2);
    instructions.remove(isPressed_3);
    instructions.remove(isPressed_4);
    instructions.remove(isPressed_5);
    instructions.remove(onStoppedUsingItem_0);

    rightClickMouse_1.setOpcode(Opcodes.INVOKESTATIC);
    rightClickMouse_1.owner = "tgw/evolution/hooks/InputHooks";
    rightClickMouse_1.name = "processKeyBinds";
    rightClickMouse_1.desc = "(Lnet/minecraft/client/Minecraft;)V";
}