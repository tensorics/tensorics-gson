# tensorics-gson

GSON bindings for tensorbacked objects

## Usage

In the simplest case, when coordinates of tensors are well dserializable by their string representation, then it is
sufficient to register the adapter when creating the gson instance:

```java
Gson gson=new GsonBuilder()
        .registerTypeAdapterFactory(TensorbackedGsonAdapter.FACTORY)
        .create();
```

If we e.g. then assume, we have a tensorbacked object, defined as:

```java
public interface AnInterfaceTensorbacked extends Tensorbacked2d<String, Integer, Double> {
}
```

with an example instance of:

```java
 AnInterfaceTensorbacked tb=Tensorics.builderFor(AnInterfaceTensorbacked.class)//
        .put(at("A",1),0.11)//
        .put(at("B",1),0.21)
        .put(at("A",2),0.12)//
        .put(at("B",2),0.22)
        .build();
```

This can be serialized by:

```java
String string=gson.toJson(tb);
```

This results in a json string, containing a nested map, as:

```json
{
  "A": {
    "1": 0.11,
    "2": 0.12
  },
  "B": {
    "1": 0.21,
    "2": 0.22
  }
}
```

Deserialization works like this:

```java
AnInterfaceTensorbacked deserialized=simpleGson.fromJson(string,AnInterfaceTensorbacked.class);
```

### Scalars

If the tensor is a scalar (it has no dimensions and contains exactly one entry), then it is serialized as a simple
scalara value. E.g.:

```java
 public interface AScalarBacked extends TensorbackedScalar<Double> {
}
```

```java
AScalarBacked val=Tensorics.builderForScalar(AScalarBacked.class)
        .put(0.33)
        .build();
        String string=simpleGson.toJson(val);
```

results in the json string:

```json
0.33
```

### Complex coordinates

If maps are serialized into json, it gets problematic, if the keys are not simple types. In this case the keys would
simply be serialized using the toString method, which results in invalid json strings, which cannot be deserialized
anymore. To overcome this, gson offers an alternative way to serialize such maps, using 2-element arrays containing key
and value for each map. As the tensorbacked adapter uses the standard gson map adapter, this is implemented out of the
box for serialization and is also implemented for deserialization correctly.

E.g. let us assume, we have an object `Pair`

```java
public class Pair {
    public final String a;
    public final String b;

    public Pair(String a, String b) {
        this.a = a;
        this.b = b;
    }

    /* valid hashcode, equals and toString methods (omitted here) */
}
```

... this we now want to use as a coordinate in a tensorbacked, e.g.:

```java
public interface AComplexCoordTensorbacked extends Tensorbacked1d<Pair, Double> {
}
```

 ```java
AComplexCoordTensorbacked complexCoordTb=Tensorics.builderFor(AComplexCoordTensorbacked.class)//
        .put(at(new Pair("a1","b1")),0.11)//
        .put(at(new Pair("a2","b2")),0.22)//
        .build();
 ```

This would result in unvalid json, if we configure our gson instance like shown above. However, using the gson flag for
complex map keys, can handle this situation:

```java
Gson complexMapKeyGson=new GsonBuilder()//
        .registerTypeAdapterFactory(TensorbackedGsonAdapter.FACTORY)//
        .enableComplexMapKeySerialization() //
        .create();
```

Using this for serialization, results in the following json:

```json
[
  [
    {
      "a": "a1",
      "b": "b1"
    },
    0.11
  ],
  [
    {
      "a": "a2",
      "b": "b2"
    },
    0.22
  ]
]
```

Deserialization works with and without this flag, as the reader can detect the structure (i.e. the same behaviour as for
maps)

## Remarks, current limitations and further thoughts

* A good starting point for further reading should be the test for the adapter, which shows some more specifics: [TensorbackedGsonAdapterTest](./src/test/java/org/tensorics/gson/adapters/TensorbackedGsonAdapterTest.java)
* The context of the tensor is not serialized (and also not deserialized ;-) ... This ishard to change, as the types are
  not explizitely defined there.
* With this complex map strategy, nested maps are strictly not necessary ... simply the Map<Position,Object> could be serialized ... To be seen what would be preferrrable wrt
  * json readibility
  * json size
* Another idea could also be to even store the key once and e.g. introduce unique ids ... then having a keymap and a valuemap ... but probably even less readable and jsonic ;-)