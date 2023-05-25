package tgw.evolution.patches;

public interface IEitherPatch<L, R> {

    boolean isLeft();

    boolean isRight();

    L left();

    R right();
}
