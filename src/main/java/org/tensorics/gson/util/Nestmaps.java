package org.tensorics.gson.util;

import com.google.common.collect.Iterables;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Position;
import org.tensorics.core.tensor.Positions;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensor.TensorBuilder;
import org.tensorics.core.tensor.operations.TensorInternals;

import java.util.List;
import java.util.Map;

/**
 * Contains utility methods to convert tensors into nested maps and vice versa.
 */
public class Nestmaps {

    private Nestmaps() {
        throw new UnsupportedOperationException("Only static methods");
    }

    /**
     * Creates a map of map of ... from the given tensor. If the passed in tensor is a scalar (tensor with dimension 0),
     * then a simple value is returned. If the tensor is empty, then null is returned.
     *
     * @param tensor     the tensor to convert into a nested map
     * @param dimensions the dimensions of the tensor. The first value in there will be the top level key type,
     *                   the second the next level, etc.
     * @return a nested map, or a value if the passed in value is a scalar.
     */
    public static Object nestmap(Tensor<?> tensor, List<Class<?>> dimensions) {
        int tensorDimensionality = Tensorics.dimensionsOf(tensor).size();
        if (tensorDimensionality != dimensions.size()) {
            throw new IllegalArgumentException("Tensor dimensionality (" + tensorDimensionality +
                    ") and number of provided dimensions (" + dimensions.size() + ": " + dimensions +
                    ") do not match!");
        }

        if (dimensions.isEmpty()) {
            return Tensorics.from(tensor).optional().orElse(null);
        }

        Class<?> dimension = Iterables.getLast(dimensions);
        Tensor<? extends Map<?, ?>> mappedOut = TensorInternals.mapOut(tensor).inDirectionOf(dimension);

        List<Class<?>> remainingDimensions = dimensions.subList(0, dimensions.size() - 1);
        return nestmap(mappedOut, remainingDimensions);
    }

    public static <T> Tensor<T> unnestmap(Object in, List<Class<?>> dimensions) {
        TensorBuilder<T> builder = Tensorics.builder(dimensions);
        unnestmap(in, Position.of(), builder);
        return builder.build();
    }

    public static <T> void unnestmap(Object remainingIn, Position pos, TensorBuilder<T> builder) {
        if (remainingIn instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) remainingIn;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Position newPos = Positions.union(pos, Position.of(entry.getKey()));
                unnestmap(entry.getValue(), newPos, builder);
            }
        } else {
            builder.put(pos, (T) remainingIn);
        }
    }

}
