package tgw.evolution.util.hitbox;

public interface IRoot {

    Matrix4d helperColliderTransform();

    Matrix4d helperTransform();

    float scaleX();

    float scaleY();

    float scaleZ();

    void transformParent(Matrix4d matrix);

    double transformX(double x, double y, double z);

    double transformY(double x, double y, double z);

    double transformZ(double x, double y, double z);
}
