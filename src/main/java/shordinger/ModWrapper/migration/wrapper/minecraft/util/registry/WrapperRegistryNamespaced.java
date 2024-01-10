package shordinger.ModWrapper.migration.wrapper.minecraft.util.registry;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.RegistryNamespaced;

public class WrapperRegistryNamespaced<K, V> extends RegistryNamespaced {

    /** The backing store that maps Integers to objects. */
    /** A BiMap of objects (key) to their names (value). */
    protected final Map<V, K> inverseObjectRegistry;

    public WrapperRegistryNamespaced() {
        this.inverseObjectRegistry = ((BiMap) this.registryObjects).inverse();
    }

    public void register(int id, K key, V value) {
        addObject(id, key.toString(),value);
    }

    /**
     * Creates the Map we will use to map keys to their registered values.
     */
    protected Map<K, V> createUnderlyingMap() {
        return HashBiMap.<K, V>create();
    }

    @Nullable
    public V getObject(@Nullable Object name) {
        return (V) super.getObject(name);
    }

    /**
     * Gets the name we use to identify the given object.
     */
    @Nullable
    public K getNameForObject(Object value) {
        return this.inverseObjectRegistry.get(value);
    }

    /**
     * Does this registry contain an entry for the given key?
     */
    public boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    /**
     * Gets the integer ID we use to identify the given object.
     */
    public int getIDForObject(@Nullable Object value) {
        return this.underlyingIntegerMap.getId(value);
    }

    /**
     * Gets the object identified by the given ID.
     */
    @Nullable
    public V getObjectById(int id) {
        return this.underlyingIntegerMap.get(id);
    }

    public Iterator<V> iterator() {
        return this.underlyingIntegerMap.iterator();
    }
}
