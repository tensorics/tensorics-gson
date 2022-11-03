package org.tensorics.gson.adapters;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensor.operations.TensorInternals;
import org.tensorics.core.tensorbacked.Tensorbacked;
import org.tensorics.core.tensorbacked.TensorbackedInternals;
import org.tensorics.core.tensorbacked.Tensorbackeds;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class TensorbackedGsonAdapter<V, TB extends Tensorbacked<V>> extends TypeAdapter<TB> {

    private final Gson context;
    private final Class<TB> tensorbackedClass;

    public TensorbackedGsonAdapter(Gson context, Class<TB> tensorbackedClass) {
        this.context = context;
        this.tensorbackedClass = requireNonNull(tensorbackedClass, "tensorbackedClass must not be null.");
    }

    @Override
    public void write(JsonWriter out, TB value) throws IOException {
        /*XXX: The context of the tensor will currently NOT be serialized! */

        List<Class<?>> dimensions = TensorbackedInternals.dimensionListFrom(tensorbackedClass);
        Object nested = nested(value.tensor(), dimensions);
        if (nested instanceof Map) {
            TypeAdapter<Map<?, ?>> adapter = context.getAdapter(new TypeToken<Map<?, ?>>() {
            });
            adapter.write(out, (Map<?, ?>) nested);
        } else { /* This is the special case of a scalar */
            Class<V> valueType = TensorbackedInternals.valueTypeFrom(tensorbackedClass);
            TypeAdapter<V> adapter = context.getAdapter(TypeToken.get(valueType));
            adapter.write(out, (V) nested);
        }
    }

    @VisibleForTesting
    static Object nested(Tensor<?> tensor, List<Class<?>> dimensions) {
        if (Tensorics.dimensionsOf(tensor).size() != dimensions.size()) {
            throw new IllegalArgumentException("Tensor dimension and provided dimension do not match!");
        }

        if (dimensions.isEmpty()) {
            return Tensorics.from(tensor).optional().orElse(null);
        }

        Class<?> dimension = Iterables.getLast(dimensions);
        Tensor<? extends Map<?, ?>> mappedOut = TensorInternals.mapOut(tensor).inDirectionOf(dimension);

        List<Class<?>> remainingDimensions = dimensions.subList(0, dimensions.size() - 1);
        return nested(mappedOut, remainingDimensions);
    }

    @Override
    public TB read(JsonReader in) throws IOException {
        return null;
    }
}
