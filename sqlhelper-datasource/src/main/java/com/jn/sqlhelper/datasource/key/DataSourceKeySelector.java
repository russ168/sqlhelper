/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jn.sqlhelper.datasource.key;

import com.jn.langx.annotation.NonNull;
import com.jn.langx.annotation.Nullable;
import com.jn.langx.invocation.MethodInvocation;
import com.jn.langx.util.Emptys;
import com.jn.langx.util.Preconditions;
import com.jn.langx.util.collection.Collects;
import com.jn.langx.util.collection.Pipeline;
import com.jn.langx.util.collection.multivalue.CommonMultiValueMap;
import com.jn.langx.util.collection.multivalue.MultiValueMap;
import com.jn.langx.util.collection.stack.ListableStack;
import com.jn.langx.util.function.Consumer;
import com.jn.langx.util.function.Predicate;
import com.jn.langx.util.function.Supplier;
import com.jn.langx.util.function.Supplier0;
import com.jn.langx.util.struct.Holder;
import com.jn.langx.util.struct.ThreadLocalHolder;
import com.jn.sqlhelper.datasource.DataSourceRegistry;
import com.jn.sqlhelper.datasource.NamedDataSource;
import com.jn.sqlhelper.datasource.key.filter.DataSourceKeyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class DataSourceKeySelector {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceKeySelector.class);
    private static final ThreadLocalHolder<ListableStack<DataSourceKey>> DATA_SOURCE_KEY_HOLDER = new ThreadLocalHolder<ListableStack<DataSourceKey>>(
            new Supplier0<ListableStack<DataSourceKey>>() {
                @Override
                public ListableStack<DataSourceKey> get() {
                    return new ListableStack<DataSourceKey>();
                }
            });

    private static final ThreadLocalHolder<DataSourceKey> CURRENT_SELECTED = new ThreadLocalHolder<DataSourceKey>();

    @NonNull
    private DataSourceKeyRegistry dataSourceKeyRegistry;
    @NonNull
    private DataSourceRegistry dataSourceRegistry;
    // 初始化阶段初始化，后续只是使用
    private MultiValueMap<String, DataSourceKeyFilter> groupToFiltersMap = new CommonMultiValueMap<String, DataSourceKeyFilter>(new ConcurrentHashMap<String, Collection<DataSourceKeyFilter>>(), new Supplier<String, Collection<DataSourceKeyFilter>>() {
        @Override
        public Collection<DataSourceKeyFilter> get(String group) {
            return Collects.emptyArrayList();
        }
    });

    public void setDataSourceRegistry(DataSourceRegistry registry) {
        this.dataSourceRegistry = registry;
    }

    public DataSourceKeyRegistry getDataSourceKeyRegistry() {
        return dataSourceKeyRegistry;
    }

    public void setDataSourceKeyRegistry(DataSourceKeyRegistry dataSourceKeyRegistry) {
        this.dataSourceKeyRegistry = dataSourceKeyRegistry;
    }

    public void addDataSourceKeyFilter(final DataSourceKeyFilter filter) {
        Preconditions.checkNotNull(filter);
        List<String> groups = Pipeline.of(filter.applyTo()).clearNulls().asList();
        Collects.forEach(groups, new Consumer<String>() {
            @Override
            public void accept(String group) {
                groupToFiltersMap.add(group, filter);
            }
        });
    }

    public void addDataSourceKeyFilters(List<DataSourceKeyFilter> filters) {
        Collects.forEach(filters, new Consumer<DataSourceKeyFilter>() {
            @Override
            public void accept(DataSourceKeyFilter filter) {
                addDataSourceKeyFilter(filter);
            }
        });
    }

    /**
     * 当进入具有 DataSourceKey 定义的方法时调用
     */
    public static void addChoice(DataSourceKey key) {
        Preconditions.checkNotNull(key);
        ListableStack<DataSourceKey> stack = DATA_SOURCE_KEY_HOLDER.get();
        stack.push(key);
    }

    /**
     * 当离开具有 DataSourceKey 定义的方法时调用
     */
    public static void removeChoice() {
        clearCurrent();
        ListableStack<DataSourceKey> stack = DATA_SOURCE_KEY_HOLDER.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    /**
     * 当离开根方法时调用
     */
    public static void clearChoices() {
        ListableStack<DataSourceKey> stack = DATA_SOURCE_KEY_HOLDER.get();
        stack.clear();
    }

    public static void setCurrent(DataSourceKey key) {
        Preconditions.checkNotNull(key);
        CURRENT_SELECTED.set(key);
    }

    public static DataSourceKey getCurrent() {
        return CURRENT_SELECTED.get();
    }

    public static void clearCurrent() {
        CURRENT_SELECTED.reset();
    }

    public final DataSourceKey select(@Nullable DataSourceKeyFilter dataSourceKeyFilter, @Nullable final MethodInvocation methodInvocation) {
        DataSourceKey key = this.dataSourceKeyRegistry.get(methodInvocation.getJoinPoint());
        if (key != null) {
            NamedDataSource dataSource = dataSourceRegistry.get(key);
            if (dataSource != null) {
                DataSourceKeySelector.addChoice(key);
                DataSourceKeySelector.setCurrent(key);
            }
        }
        key = DataSourceKeySelector.getCurrent();
        if (key == null) {
            key = doSelect(dataSourceKeyFilter, methodInvocation);
            if (key != null) {
                DataSourceKeySelector.addChoice(key);
                DataSourceKeySelector.setCurrent(key);
            }
        }
        if (key != null) {
            NamedDataSource dataSource = dataSourceRegistry.get(key);
            if (dataSource == null) {
                logger.warn("Can't find a datasource named: {}", key);
            }
        }
        return key;
    }

    /**
     * 指定的group下，选择某个datasource, 返回的是该组下的匹配到的 datasource key
     * <p>
     * 这里面不能去设置CURRENT_SELECTED
     */
    protected DataSourceKey doSelect(@Nullable DataSourceKeyFilter filter, @Nullable final MethodInvocation methodInvocation) {
        Preconditions.checkArgument(dataSourceRegistry.size() > 0, "has no any datasource registered");
        if (dataSourceRegistry.size() == 1) {
            return dataSourceRegistry.getPrimary();
        }
        // 从线程栈里过滤
        ListableStack<DataSourceKey> stack = DATA_SOURCE_KEY_HOLDER.get();
        if (Emptys.isEmpty(stack)) {
            return dataSourceRegistry.getPrimary();
        }

        if (!CURRENT_SELECTED.isNull()) {
            return getCurrent();
        }

        final Holder<List<DataSourceKey>> dataSourceKeyList = new Holder<List<DataSourceKey>>();
        // 遍历 stack
        Collects.forEach(stack, new Predicate<DataSourceKey>() {
            @Override
            public boolean test(DataSourceKey dataSourceKey) {
                return dataSourceKey != null;
            }
        }, new Consumer<DataSourceKey>() {
            @Override
            public void accept(DataSourceKey dataSourceKey) {
                List<DataSourceKey> matched = dataSourceRegistry.findKeys(dataSourceKey);
                if (Emptys.isNotEmpty(matched)) {
                    dataSourceKeyList.set(matched);
                }
            }
        }, new Predicate<DataSourceKey>() {
            @Override
            public boolean test(DataSourceKey dataSourceKey) {
                return !dataSourceKeyList.isEmpty();
            }
        });

        if (!dataSourceKeyList.isEmpty()) {
            // keys 必然是同一个group下的
            final List<DataSourceKey> keys = dataSourceKeyList.get();
            if (keys.size() == 1) {
                return keys.get(0);
            }

            // 如果匹配到的过多，则进行二次过滤
            final Holder<DataSourceKey> keyHolder = new Holder<DataSourceKey>();
            if (filter != null) {
                keyHolder.set(filter.apply(keys, methodInvocation));
            }
            if (!keyHolder.isNull()) {
                return keyHolder.get();
            }

            // 指定的参数过滤不到的情况下，则基于 group filter
            Collection<DataSourceKeyFilter> filters = groupToFiltersMap.get(keys.get(0).getGroup());

            Collects.forEach(filters, new Consumer<DataSourceKeyFilter>() {
                @Override
                public void accept(DataSourceKeyFilter filter) {
                    keyHolder.set(filter.apply(keys, methodInvocation));
                }
            }, new Predicate<DataSourceKeyFilter>() {
                @Override
                public boolean test(DataSourceKeyFilter filter) {
                    return !keyHolder.isNull();
                }
            });

            return keyHolder.get();
        }

        return null;
    }

    public DataSourceRegistry getDataSourceRegistry() {
        return dataSourceRegistry;
    }
}
