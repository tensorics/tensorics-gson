package org.tensorics.gson.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensorbacked.dimtyped.TensorbackedScalar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tensorics.core.lang.Tensorics.at;

public class TensorbackedGsonAdapterTest {

    private final Gson gson = new GsonBuilder()//
            .registerTypeAdapterFactory(new TensorbackedGsonAdapterFactory())//
            .create();

    @Test
    public void simpleTensorSerializationIsOk() {
        TensorbackedGsonAdapterFactoryTest.AnInheritedTensorbacked val = Tensorics.builderFor(TensorbackedGsonAdapterFactoryTest.AnInheritedTensorbacked.class)//
                .put(at("A", 1), 0.11)//
                .put(at("B", 1), 0.21)
                .put(at("A", 2), 0.12)//
                .put(at("B", 2), 0.22)
                .build();

        String string = gson.toJson(val);
        System.out.println(string);
        assertThat(string).isNotNull();
        assertThat(string).isNotEmpty();
        assertThat(string).isEqualTo("{\"A\":{\"1\":0.11,\"2\":0.12},\"B\":{\"1\":0.21,\"2\":0.22}}");
    }

    @Test
    public void simpleScalarSerializationIsOk() {
        AScalarBacked val = Tensorics.builderForScalar(AScalarBacked.class).put(0.33).build();

        String string = gson.toJson(val);
        System.out.println(string);
        assertThat(string).isNotNull();
        assertThat(string).isNotEmpty();
        assertThat(string).isEqualTo("0.33");
    }

    public interface AScalarBacked extends TensorbackedScalar<Double> {

    }


}
