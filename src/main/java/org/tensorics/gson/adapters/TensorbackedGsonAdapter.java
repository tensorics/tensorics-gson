package org.tensorics.gson.adapters;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Position;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensorbacked.Tensorbacked;
import org.tensorics.core.tensorbacked.TensorbackedInternals;
import org.tensorics.gson.util.Nestmaps;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.tensorics.core.tensorbacked.TensorbackedInternals.valueTypeFrom;

public class TensorbackedGsonAdapter<V, TB extends Tensorbacked<V>> extends TypeAdapter<TB> {

    private final Gson context;
    private final Class<TB> tensorbackedClass;

    private final TypeAdapter<V> valueAdapter;

    public TensorbackedGsonAdapter(Gson context, Class<TB> tensorbackedClass) {
        this.context = context;
        this.tensorbackedClass = requireNonNull(tensorbackedClass, "tensorbackedClass must not be null.");

        this.valueAdapter = adapterFor(valueTypeFrom(tensorbackedClass));
    }

    @Override
    public void write(JsonWriter out, TB value) throws IOException {
        /*XXX: The context of the tensor will currently NOT be serialized! */

        List<Class<?>> dimensions = TensorbackedInternals.dimensionListFrom(tensorbackedClass);
        Object nested = Nestmaps.nestmap(value.tensor(), dimensions);
        if (nested instanceof Map) {
            TypeAdapter<Map<?, ?>> adapter = context.getAdapter(new TypeToken<Map<?, ?>>() {
            });
            adapter.write(out, (Map<?, ?>) nested);
        } else { /* This is the special case of a scalar */
            valueAdapter.write(out, (V) nested);
        }
    }

    @Override
    public TB read(JsonReader in) throws IOException {
        List<Class<?>> dimensions = TensorbackedInternals.dimensionListFrom(tensorbackedClass);
        Object object = recursiveRead(in, dimensions);
        Tensor<V> unnested = Nestmaps.unnestmap(object, dimensions);
        return TensorbackedInternals.createBackedByTensor(tensorbackedClass, unnested);
    }

    @VisibleForTesting
    Object recursiveRead(JsonReader in, List<Class<?>> dimensions) throws IOException {
        if (dimensions.isEmpty()) {
            /* This is the special case of a scalar and the final value */
            return valueAdapter.read(in);
        } else {
            Class<?> thisDim = dimensions.get(0);
            List<Class<?>> remainingDims = dimensions.subList(1, dimensions.size());
            return readMap(in, thisDim, remainingDims);
        }
    }

    private <T> Map<T, Object> readMap(JsonReader in, Class<T> keyDim, List<Class<?>> remainingDimensions) throws IOException {
        TypeAdapter<T> dimAdapter = adapterFor(keyDim);
        Map<T, Object> map = new HashMap<>();

        in.beginObject();
        while (in.hasNext()) {
            JsonReaderInternalAccess.INSTANCE.promoteNameToValue(in);
            T key = dimAdapter.read(in);
            Object value = recursiveRead(in, remainingDimensions);
            map.put(key, value);
        }
        in.endObject();

        return map;
    }


    private <T> TypeAdapter<T> adapterFor(Class<T> valueType) {
        return context.getAdapter(TypeToken.get(valueType));
    }


    /*
     JsonToken peek = in.peek();
      if (peek == JsonToken.NULL) {
        in.nextNull();
        return null;
      }

      Map<K, V> map = constructor.construct();

      if (peek == JsonToken.BEGIN_ARRAY) {
        in.beginArray();
        while (in.hasNext()) {
          in.beginArray(); // entry array
          K key = keyTypeAdapter.read(in);
          V value = valueTypeAdapter.read(in);
          V replaced = map.put(key, value);
          if (replaced != null) {
            throw new JsonSyntaxException("duplicate key: " + key);
          }
          in.endArray();
        }
        in.endArray();
      }
     */
}
