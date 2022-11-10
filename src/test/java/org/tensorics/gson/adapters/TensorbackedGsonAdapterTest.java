package org.tensorics.gson.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Position;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensorbacked.AbstractTensorbacked;
import org.tensorics.core.tensorbacked.annotation.Dimensions;
import org.tensorics.core.tensorbacked.dimtyped.Tensorbacked2d;
import org.tensorics.core.tensorbacked.dimtyped.TensorbackedScalar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tensorics.core.lang.Tensorics.at;
import static org.tensorics.core.lang.Tensorics.sizeOf;

public class TensorbackedGsonAdapterTest {

    private static final AnInheritedTensorbacked TENSORBACKED = Tensorics.builderFor(AnInheritedTensorbacked.class)//
            .put(at("A", 1), 0.11)//
            .put(at("B", 1), 0.21)
            .put(at("A", 2), 0.12)//
            .put(at("B", 2), 0.22)
            .build();

    private static final String JSON_STRING = "{\"A\":{\"1\":0.11,\"2\":0.12},\"B\":{\"1\":0.21,\"2\":0.22}}";

    private final Gson gson = new GsonBuilder()//
            .registerTypeAdapterFactory(new TensorbackedGsonAdapterFactory())//
            .create();

    @Test
    public void simpleTensorSerializationIsOk() {
        String string = gson.toJson(TENSORBACKED);
        assertThat(string).isNotNull();
        assertThat(string).isNotEmpty();
        assertThat(string).isEqualTo(JSON_STRING);
    }

    @Test
    public void simpleTensorDeserializationIsOk() {
        AnInheritedTensorbacked val = gson.fromJson(JSON_STRING, AnInheritedTensorbacked.class);
        assertThat(val).isNotNull();
        assertThat(val).isEqualTo(TENSORBACKED);
    }

    @Test
    public void simpleScalarSerializationIsOk() {
        AScalarBacked val = Tensorics.builderForScalar(AScalarBacked.class).put(0.33).build();

        String string = gson.toJson(val);
        assertThat(string).isNotNull();
        assertThat(string).isNotEmpty();
        assertThat(string).isEqualTo("0.33");
    }

    @Test
    public void simpleScalarDeserializationIsOk() {
        AScalarBacked val = gson.fromJson("0.33", AScalarBacked.class);
        assertThat(val).isNotNull();
        assertThat(sizeOf(val)).isEqualTo(1);
        assertThat(val.get()).isEqualTo(0.33);
    }

    /**
     * A content of a tensorbacked can be deserialized into another type, as long as the dimensions match in
     * number, type AND ORDER. However, the equality between the original and the deserialized object is then
     * not given, as the class of the object is taken into account. This behaviour is considered to be correct.
     */
    @Test
    public void deserializationIntoDifferentTbWorks() {
        /* This shows cross-deserialization: Into another tensoribacked with the same dimensions.*/
        AnInterfaceTensorbacked val = gson.fromJson(JSON_STRING, AnInterfaceTensorbacked.class);
        assertThat(val).isNotNull();

        /* The tensorbacked objects are not equal in this case ....*/
        assertThat(val).isNotEqualTo(TENSORBACKED);

        /* However, the underlaying tensors are ...*/
        assertThat(val.tensor()).isEqualTo(TENSORBACKED.tensor());
    }

    /**
     * The context is not serialized or deserialzed currently. This is demonstrated here.
     * Can be discussed, if this is good behaviour.... tricky to change anyhow, as the context
     * dimensions are not well defined by a tensorbacked.
     */
    @Test
    public void contextIsNotSerialized() {
        AnInheritedTensorbacked tbWithContext = Tensorics.builderFor(AnInheritedTensorbacked.class)//
                .putAll(TENSORBACKED)//
                .context(Position.of(AB.A))//
                .build();

        String string = gson.toJson(tbWithContext);
        AnInheritedTensorbacked deserialized = gson.fromJson(string, AnInheritedTensorbacked.class);

        /* As the context is currently neither serialized nor deserialized,
        the equality to the original object does currently not hold.*/
        assertThat(deserialized).isNotEqualTo(tbWithContext);

        /* Instead, the context is stripped, so the equality to the original object
        (with the same content, but no context) holds...*/
        assertThat(deserialized).isEqualTo(TENSORBACKED);
    }


    public interface AScalarBacked extends TensorbackedScalar<Double> {

    }


    @Dimensions({String.class, Integer.class})
    public static class AnInheritedTensorbacked extends AbstractTensorbacked<Double> {

        public AnInheritedTensorbacked(Tensor<Double> tensor) {
            super(tensor);
        }

    }

    public static interface AnInterfaceTensorbacked extends Tensorbacked2d<String, Integer, Double> {

    }

    public static enum AB {
        A, B
    }

}
