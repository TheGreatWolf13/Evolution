package tgw.evolution.util.constants;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
@MagicConstant(valuesFromClass = NBTType.class)
public @interface NBTType {

    int END = 0;
    int BYTE = 1;
    int SHORT = 2;
    int INT = 3;
    int LONG = 4;
    int FLOAT = 5;
    int DOUBLE = 6;
    int BYTE_ARRAY = 7;
    int STRING = 8;
    int LIST = 9;
    int COMPOUND = 10;
    int INT_ARRAY = 11;
    int LONG_ARRAY = 12;
    int ANY_NUMERIC = 99;
}
