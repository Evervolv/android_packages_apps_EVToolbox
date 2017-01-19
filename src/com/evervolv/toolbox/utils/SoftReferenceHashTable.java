/*
 * Copyright (C) 2013 Koushik Dutta (@koush)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evervolv.toolbox.utils;

import java.lang.ref.SoftReference;
import java.util.Hashtable;

public class SoftReferenceHashTable<K,V> {
    Hashtable<K, SoftReference<V>> mTable = new Hashtable<K, SoftReference<V>>();

    public V put(K key, V value) {
        SoftReference<V> old = mTable.put(key, new SoftReference<V>(value));
        if (old == null)
            return null;
        return old.get();
    }

    public V get(K key) {
        SoftReference<V> val = mTable.get(key);
        if (val == null)
            return null;
        V ret = val.get();
        if (ret == null)
            mTable.remove(key);
        return ret;
    }

    public V remove(K k) {
        SoftReference<V> v = mTable.remove(k);
        if (v == null)
            return null;
        return v.get();
    }

}