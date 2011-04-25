/*
 * Copyright 2008-2011 LinkedIn, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package voldemort.client;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import voldemort.VoldemortException;
import voldemort.cluster.Node;
import voldemort.secondary.RangeQuery;
import voldemort.versioning.ObsoleteVersionException;
import voldemort.versioning.Version;
import voldemort.versioning.Versioned;

/**
 * A {@link StoreClient} with lazy initialization. This is useful
 * existing codes that initializes clients at service deployment time,
 * when the servers may or may not be available
 * 
 */
public class LazyStoreClient<K, V> implements StoreClient<K, V> {

    private final Callable<StoreClient<K, V>> storeClientThunk;
    private StoreClient<K, V> storeClient;

    public LazyStoreClient(Callable<StoreClient<K, V>> storeClientThunk) {
        this.storeClientThunk = storeClientThunk;
    }

    public synchronized StoreClient<K, V> getStoreClient() {
        if(storeClient == null)
            storeClient = initStoreClient();

        return storeClient;
    }

    protected StoreClient<K, V> initStoreClient() {
        try {
            return storeClientThunk.call();
        } catch(VoldemortException ve) {
            throw ve;
        } catch(Exception e) {
            // Callable's type signature includes checked exceptions
            throw new VoldemortException("Unexpected exception during initialization", e);
        }
    }

    public V getValue(K key) {
        return getStoreClient().getValue(key);
    }

    public V getValue(K key, V defaultValue) {
        return getStoreClient().getValue(key, defaultValue);
    }

    public Versioned<V> get(K key) {
        return getStoreClient().get(key);
    }

    public Versioned<V> get(K key, Object transforms) {
        return getStoreClient().get(key, transforms);
    }

    public Map<K, Versioned<V>> getAll(Iterable<K> keys) {
        return getStoreClient().getAll(keys);
    }

    public Map<K, Versioned<V>> getAll(Iterable<K> keys, Map<K, Object> transforms) {
        return getStoreClient().getAll(keys, transforms);
    }

    public Versioned<V> get(K key, Versioned<V> defaultValue) {
        return getStoreClient().get(key, defaultValue);
    }

    public void put(K key, V value) {
        getStoreClient().put(key, value);
    }

    public void put(K key, V value, Object transforms) {
        getStoreClient().put(key, value, transforms);
    }

    public void put(K key, Versioned<V> versioned) throws ObsoleteVersionException {
        getStoreClient().put(key, versioned);
    }

    public boolean putIfNotObsolete(K key, Versioned<V> versioned) {
        return getStoreClient().putIfNotObsolete(key, versioned);
    }

    public boolean applyUpdate(UpdateAction<K, V> action) {
        return getStoreClient().applyUpdate(action);
    }

    public boolean applyUpdate(UpdateAction<K, V> action, int maxTries) {
        return getStoreClient().applyUpdate(action, maxTries);
    }

    public boolean delete(K key) {
        return getStoreClient().delete(key);
    }

    public boolean delete(K key, Version version) {
        return getStoreClient().delete(key, version);
    }

    public List<Node> getResponsibleNodes(K key) {
        return getStoreClient().getResponsibleNodes(key);
    }

    public Set<K> getAllKeys(RangeQuery query) {
        return getStoreClient().getAllKeys(query);
    }
}
