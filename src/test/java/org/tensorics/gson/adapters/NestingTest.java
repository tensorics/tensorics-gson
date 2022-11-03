package org.tensorics.gson.adapters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.ImmutableScalar;
import org.tensorics.core.tensor.Scalar;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensorbacked.AbstractTensorbacked;
import org.tensorics.core.tensorbacked.TensorbackedInternals;
import org.tensorics.core.tensorbacked.annotation.Dimensions;

import static org.tensorics.core.lang.Tensorics.at;

public class NestingTest {

    @Test
    public void mapoutTensorWorks() {
        AnInheritedTensorbacked val = Tensorics.builderFor(AnInheritedTensorbacked.class)//
                .put(at("A", 1), 0.11)//
                .put(at("B", 1), 0.21)
                .put(at("A", 2), 0.12)//
                .put(at("B", 2), 0.22)
                .build();

        Object nested = TensorbackedGsonAdapter.nested(val.tensor(), TensorbackedInternals.dimensionListFrom(AnInheritedTensorbacked.class));

        Assertions.assertThat(nested).isEqualTo(ImmutableMap.of(
                "A", ImmutableMap.of(1, 0.11, 2, 0.12), //
                "B", ImmutableMap.of(1, 0.21, 2, 0.22)
        ));
    }


    @Test
    public void mapoutScalarIsPlain() {
        Scalar<Double> scalar = Tensorics.scalarOf(0.33);

        Object nested = TensorbackedGsonAdapter.nested(scalar, ImmutableList.of());
        Assertions.assertThat(nested).isEqualTo(0.33);
    }

    @Dimensions({String.class, Integer.class})
    public static class AnInheritedTensorbacked extends AbstractTensorbacked<Double> {

        public AnInheritedTensorbacked(Tensor<Double> tensor) {
            super(tensor);
        }

    }

    @Test
    public void mapoutEmptyTensorIsNull() {
        Tensor<Object> empty = Tensorics.builder(String.class, Integer.class).build();
        Object nested = TensorbackedGsonAdapter.nested(empty, ImmutableList.of(String.class, Integer.class));
        Assertions.assertThat(nested).isNull();
    }

}
