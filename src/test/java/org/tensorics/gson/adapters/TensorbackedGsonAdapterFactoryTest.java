package org.tensorics.gson.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensorbacked.AbstractTensorbacked;
import org.tensorics.core.tensorbacked.annotation.Dimensions;

import static org.tensorics.core.lang.Tensorics.at;

public class TensorbackedGsonAdapterFactoryTest {

    private final TypeAdapterFactory factory = new TensorbackedGsonAdapterFactory();
    private final Gson gson = new Gson();

    @Test
    public void noAdapterForNotATensorbacked() {
        TypeAdapter<InvalidTensorbacked> adapter = factory.create(gson, TypeToken.get(InvalidTensorbacked.class));
        Assertions.assertThat(adapter).isNotNull();
        /* The adapter returned here is not null, however, it will give problems when determining the dimensions...*/
    }

    @Test
    public void adapterReturnedForTensorbacked() {
        TypeAdapter<AnInheritedTensorbacked> adapter = factory.create(gson, new TypeToken<AnInheritedTensorbacked>() {
        });
        Assertions.assertThat(adapter).isNotNull();
    }

    @Dimensions({String.class, Integer.class})
    public static class AnInheritedTensorbacked extends AbstractTensorbacked<Double> {

        public AnInheritedTensorbacked(Tensor<Double> tensor) {
            super(tensor);
        }

    }

    /**
     * Despite not inheriting from abstract tensorbacked, this is not valid tensorbacked object, as it does not have
     * the required annotation.
     */
    public static class InvalidTensorbacked extends AbstractTensorbacked<Double> {

        public InvalidTensorbacked(Tensor<Double> tensor) {
            super(tensor);
        }

    }


}