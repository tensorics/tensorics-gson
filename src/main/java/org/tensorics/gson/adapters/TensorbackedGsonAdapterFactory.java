package org.tensorics.gson.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.tensorics.core.tensorbacked.Tensorbacked;

class TensorbackedGsonAdapterFactory implements TypeAdapterFactory {


    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (!Tensorbacked.class.isAssignableFrom(rawType)) {
            return null;
        }
        Class<? extends Tensorbacked<?>> tensorbackedClass = (Class<? extends Tensorbacked<?>>) rawType;
        return new TensorbackedGsonAdapter(gson, tensorbackedClass);
    }
}
