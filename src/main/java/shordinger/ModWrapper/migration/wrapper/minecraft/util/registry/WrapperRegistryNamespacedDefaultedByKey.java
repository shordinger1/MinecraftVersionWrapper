package shordinger.ModWrapper.migration.wrapper.minecraft.util.registry;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

public class WrapperRegistryNamespacedDefaultedByKey<K, V> extends WrapperRegistryNamespaced<K, V> {

    /** The key of the default value. */
    private final K defaultValueKey;
    /** The default value for this registry, retrurned in the place of a null value. */
    private V defaultValue;

    public WrapperRegistryNamespacedDefaultedByKey(K defaultValueKeyIn) {
        this.defaultValueKey = defaultValueKeyIn;
    }

    public void register(int id, K key, V value) {
        if (this.defaultValueKey.equals(key)) {
            this.defaultValue = value;
        }

        super.register(id, key, value);
    }

    /**
     * validates that this registry's key is non-null
     */
    public void validateKey() {
        Validate.notNull(this.defaultValue, "Missing default of DefaultedMappedRegistry: " + this.defaultValueKey);
    }

    /**
     * Gets the integer ID we use to identify the given object.
     */
    public int getIDForObject(Object value) {
        int i = super.getIDForObject(value);
        return i == -1 ? super.getIDForObject(this.defaultValue) : i;
    }

    /**
     * Gets the name we use to identify the given object.
     */
    @Nonnull
    public K getNameForObject(Object value) {
        K k = (K) super.getNameForObject(value);
        return (K) (k == null ? this.defaultValueKey : k);
    }

    @Nonnull
    public V getObject(@Nullable Object name) {
        V v = (V) super.getObject(name);
        return (V) (v == null ? this.defaultValue : v);
    }

    /**
     * Gets the object identified by the given ID.
     */
    @Nonnull
    public V getObjectById(int id) {
        V v = (V) super.getObjectById(id);
        return (V) (v == null ? this.defaultValue : v);
    }

    @Nonnull
    public V getRandomObject(Random random) {
        V v = (V) super.getRandomObject(random);
        return (V) (v == null ? this.defaultValue : v);
    }
}
