package tgw.evolution.util.constants;

import net.minecraft.nbt.Tag;
import org.intellij.lang.annotations.MagicConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_USE)
@MagicConstant(intValues = {Tag.TAG_END,
                            Tag.TAG_BYTE,
                            Tag.TAG_SHORT,
                            Tag.TAG_INT,
                            Tag.TAG_LONG,
                            Tag.TAG_FLOAT,
                            Tag.TAG_DOUBLE,
                            Tag.TAG_BYTE_ARRAY,
                            Tag.TAG_STRING,
                            Tag.TAG_LIST,
                            Tag.TAG_COMPOUND,
                            Tag.TAG_INT_ARRAY,
                            Tag.TAG_LONG_ARRAY,
                            Tag.TAG_ANY_NUMERIC})
public @interface NBTType {

}
