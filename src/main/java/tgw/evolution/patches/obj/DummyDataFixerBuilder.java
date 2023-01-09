package tgw.evolution.patches.obj;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;

import java.util.concurrent.Executor;

public class DummyDataFixerBuilder extends DataFixerBuilder {

    public DummyDataFixerBuilder(int dataVersion) {
        super(dataVersion);
    }

    @Override
    public DataFixer build(Executor executor) {
        return new DummyDataFixerUpper();
    }
}
