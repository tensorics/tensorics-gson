package org.tensorics.gson.adapters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.junit.Test;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Position;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensorbacked.AbstractTensorbacked;
import org.tensorics.core.tensorbacked.annotation.Dimensions;
import org.tensorics.core.tensorbacked.dimtyped.Tensorbacked1d;
import org.tensorics.core.tensorbacked.dimtyped.Tensorbacked2d;
import org.tensorics.core.tensorbacked.dimtyped.TensorbackedScalar;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private final Gson simpleGson = new GsonBuilder()//
            .registerTypeAdapterFactory(TensorbackedGsonAdapter.FACTORY)//
            .create();

    private static final AComplexCoordTensorbacked COMPLEX_COORD_TB = Tensorics.builderFor(AComplexCoordTensorbacked.class)//
            .put(at(new Pair("a1", "b1")), 0.11)//
            .put(at(new Pair("a2", "b2")), 0.22)//
            .build();
    private static final String COMPLEX_COORD_JSON_STRING = "[[{\"a\":\"a1\",\"b\":\"b1\"},0.11],[{\"a\":\"a2\",\"b\":\"b2\"},0.22]]";

    private final Gson complexMapKeyGson = new GsonBuilder()//
            .registerTypeAdapterFactory(TensorbackedGsonAdapter.FACTORY)//
            .enableComplexMapKeySerialization() //
            .create();

    @Test
    public void simpleTensorSerializationIsOk() {
        String string = simpleGson.toJson(TENSORBACKED);
        assertThat(string).isNotNull();
        assertThat(string).isNotEmpty();
        assertThat(string).isEqualTo(JSON_STRING);
    }

    @Test
    public void simpleTensorDeserializationIsOk() {
        AnInheritedTensorbacked val = simpleGson.fromJson(JSON_STRING, AnInheritedTensorbacked.class);
        assertThat(val).isNotNull();
        assertThat(val).isEqualTo(TENSORBACKED);
    }

    @Test
    public void simpleScalarSerializationIsOk() {
        AScalarBacked val = Tensorics.builderForScalar(AScalarBacked.class).put(0.33).build();

        String string = simpleGson.toJson(val);
        System.out.println(string);
        assertThat(string).isNotNull();
        assertThat(string).isNotEmpty();
        assertThat(string).isEqualTo("0.33");
    }

    @Test
    public void simpleScalarDeserializationIsOk() {
        AScalarBacked val = simpleGson.fromJson("0.33", AScalarBacked.class);
        assertThat(val).isNotNull();
        assertThat(sizeOf(val)).isEqualTo(1);
        assertThat(val.get()).isEqualTo(0.33);
    }

    /**
     * A content of a tensorbacked can be deserialized into another type, as long as the dimensions match in
     * number, type AND ORDER. However, the equality between the original and the deserialized object is then
     * not given, as the class of the object is taken into account. This behaviour is considered to be correct.
     */
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    public void deserializationIntoDifferentTbWorks() {
        /* This shows cross-deserialization: Into another tensorbacked with the same dimensions.*/
        AnInterfaceTensorbacked val = simpleGson.fromJson(JSON_STRING, AnInterfaceTensorbacked.class);
        assertThat(val).isNotNull();

        /* The tensorbacked objects are not equal in this case ....*/
        assertThat(val).isNotEqualTo(TENSORBACKED);

        /* However, the underlying tensors are ...*/
        assertThat(val.tensor()).isEqualTo(TENSORBACKED.tensor());
    }

    /**
     * The context is not serialized or deserialized currently. This is demonstrated here.
     * Can be discussed, if this is good behaviour.... tricky to change anyhow, as the context
     * dimensions are not well-defined by a tensorbacked.
     */
    @Test
    public void contextIsNotSerialized() {
        AnInheritedTensorbacked tbWithContext = Tensorics.builderFor(AnInheritedTensorbacked.class)//
                .putAll(TENSORBACKED)//
                .context(Position.of(AB.A))//
                .build();

        String string = simpleGson.toJson(tbWithContext);
        AnInheritedTensorbacked deserialized = simpleGson.fromJson(string, AnInheritedTensorbacked.class);

        /* As the context is currently neither serialized nor deserialized,
        the equality to the original object does currently not hold.*/
        assertThat(deserialized).isNotEqualTo(tbWithContext);

        /* Instead, the context is stripped, so the equality to the original object
        (with the same content, but no context) holds...*/
        assertThat(deserialized).isEqualTo(TENSORBACKED);
    }

    @Test
    public void complexCoordinateNotSupportedPerDefault() {
        String string = simpleGson.toJson(COMPLEX_COORD_TB);

        /* This cannot be deserialized anymore, as the simple result of "toString" was put as key...*/
        assertThatThrownBy(() -> simpleGson.fromJson(string, AComplexCoordTensorbacked.class)) //
                .isInstanceOf(JsonSyntaxException.class) //
                .hasMessageContaining("Expected BEGIN_OBJECT but was STRING");
    }

    @Test
    public void complexCoordinateSerializationWithComplexMapKeySupport() {
        String string = complexMapKeyGson.toJson(COMPLEX_COORD_TB);
        System.out.println(string);
        assertThat(string).isEqualTo(COMPLEX_COORD_JSON_STRING);
    }

    @Test
    public void complexCoordinateDeserializationIsOk() {
        /* deserialization works with any gson (as it is an if in the adapter) */
        AComplexCoordTensorbacked deserialized = simpleGson.fromJson(COMPLEX_COORD_JSON_STRING, AComplexCoordTensorbacked.class);
        assertThat(deserialized).isEqualTo(COMPLEX_COORD_TB);
    }

    public interface AScalarBacked extends TensorbackedScalar<Double> {

    }


    @Dimensions({String.class, Integer.class})
    public static class AnInheritedTensorbacked extends AbstractTensorbacked<Double> {

        public AnInheritedTensorbacked(Tensor<Double> tensor) {
            super(tensor);
        }

    }

    public interface AnInterfaceTensorbacked extends Tensorbacked2d<String, Integer, Double> {

    }

    public interface AComplexCoordTensorbacked extends Tensorbacked1d<Pair, Double> {

    }

    public enum AB {
        A
    }

    public static class Pair {
        public final String a;
        public final String b;

        public Pair(String a, String b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return Objects.equals(a, pair.a) && Objects.equals(b, pair.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "a='" + a + '\'' +
                    ", b='" + b + '\'' +
                    '}';
        }
    }

}
