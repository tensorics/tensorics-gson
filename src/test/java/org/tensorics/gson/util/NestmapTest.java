package org.tensorics.gson.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Scalar;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensorbacked.AbstractTensorbacked;
import org.tensorics.core.tensorbacked.TensorbackedInternals;
import org.tensorics.core.tensorbacked.annotation.Dimensions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.tensorics.core.lang.Tensorics.at;

public class NestmapTest {

    private final AnInheritedTensorbacked TENSOR_BACKED = Tensorics.builderFor(AnInheritedTensorbacked.class)//
            .put(at("A", 1), 0.11)//
            .put(at("B", 1), 0.21)
            .put(at("A", 2), 0.12)//
            .put(at("B", 2), 0.22)
            .build();

    private final Map<String, Map<Integer, Double>> NESTED_MAP = ImmutableMap.of(
            "A", ImmutableMap.of(1, 0.11, 2, 0.12), //
            "B", ImmutableMap.of(1, 0.21, 2, 0.22)
    );

    @Test
    public void nestmapTensorWorks() {
        Object nested = Nestmaps.nestmap(TENSOR_BACKED.tensor(), TensorbackedInternals.dimensionListFrom(AnInheritedTensorbacked.class));
        assertThat(nested).isEqualTo(NESTED_MAP);
    }

    @Test
    public void nestmapScalarIsPlain() {
        Scalar<Double> scalar = Tensorics.scalarOf(0.33);

        Object nested = Nestmaps.nestmap(scalar, ImmutableList.of());
        assertThat(nested).isEqualTo(0.33);
    }

    @Test
    public void unnestmapTensor() {
        Tensor<Double> unnested = Nestmaps.unnestmap(NESTED_MAP, ImmutableList.of(String.class, Integer.class));
        assertThat(unnested).isEqualTo(TENSOR_BACKED.tensor());
    }

    @Test
    public void wrongDimensionsThrowsOnUnnest() {
        assertThatThrownBy(() -> Nestmaps.unnestmap(NESTED_MAP, ImmutableList.of(Integer.class)))//
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("coordinates are not consistent");
    }

    @Test
    public void unnestScalar() {
        Scalar<Double> scalar = Tensorics.scalarOf(0.33);
        Tensor<Double> unnested = Nestmaps.unnestmap(0.33, ImmutableList.of());
        Assertions.assertThat(unnested).isEqualTo(scalar);
    }

    @Dimensions({String.class, Integer.class})
    public static class AnInheritedTensorbacked extends AbstractTensorbacked<Double> {
        public AnInheritedTensorbacked(Tensor<Double> tensor) {
            super(tensor);
        }
    }

    /* XXX Is this the correct behaviour? */
    @Test
    public void nestmapEmptyTensorIsNull() {
        Tensor<Object> empty = Tensorics.builder(String.class, Integer.class).build();
        Object nested = Nestmaps.nestmap(empty, ImmutableList.of(String.class, Integer.class));
        assertThat(nested).isNull();
    }

    @Test
    public void nestmapWrongDimensionAmountThrows() {
        Tensor<Object> empty = Tensorics.builder(String.class, Integer.class).build();
        assertThatThrownBy(() -> Nestmaps.nestmap(empty, ImmutableList.of(Integer.class)))//
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not match!");
    }

}
