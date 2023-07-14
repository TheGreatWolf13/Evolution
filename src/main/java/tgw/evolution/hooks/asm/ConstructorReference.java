package tgw.evolution.hooks.asm;

import org.objectweb.asm.Opcodes;

public class ConstructorReference extends MethodReference {

    public ConstructorReference(ClassReference owner, String desc) {
        super(Opcodes.INVOKESPECIAL, owner, "<init>", desc, false);
    }
}
