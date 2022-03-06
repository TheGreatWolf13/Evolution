package tgw.evolution.util.constants;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

public final class CommonRotations {

    //XN
    public static final Quaternion XN90 = Vector3f.XP.rotationDegrees(-90);
    //XP
    public static final Quaternion XP135 = Vector3f.XP.rotationDegrees(135.0F);
    public static final Quaternion XP185_5 = Vector3f.XP.rotationDegrees(185.5F);
    //YN
    public static final Quaternion YN22_5 = Vector3f.YP.rotationDegrees(-22.5F);
    public static final Quaternion YN90 = Vector3f.YP.rotationDegrees(-90);
    //YP
    public static final Quaternion YP62 = Vector3f.YP.rotationDegrees(62.0F);
    public static final Quaternion YP180 = Vector3f.YP.rotationDegrees(180.0F);
    //ZN
    //ZP
    public static final Quaternion ZP180 = Vector3f.ZP.rotationDegrees(180);

    private CommonRotations() {
    }
}
