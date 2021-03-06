/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jn.sqlhelper.datasource.definition;

import com.jn.langx.configuration.Configuration;
import com.jn.langx.text.StringTemplates;
import com.jn.langx.util.Strings;
import com.jn.sqlhelper.datasource.DataSources;
import com.jn.sqlhelper.datasource.key.DataSourceKey;

import java.util.Properties;

public class DataSourceProperties implements Configuration {
    /**
     * datasource:
     */
    private String group = DataSources.DATASOURCE_GROUP;
    private String name;
    private String implementation;

    /**
     * driver properties:
     */
    private String driverClassName;
    private String url;
    private String username;
    private String password;

    private String catalog;
    private String schema;

    private boolean isReadOnly;
    private boolean isAutoCommit;
    private String transactionIsolationName;

    private long leakDetectionThresholdInMills;
    private String validationQuery;

    private long connectionTimeoutInMills;
    private long validationTimeoutInMills;

    private long idleTimeoutInMills;
    private long maxLifetimeInMills;
    private int maxPoolSize;
    private int initialSize = 0;
    private int minIdle;

    private Properties driverProps;

    public DataSourceProperties() {
        this.leakDetectionThresholdInMills = 0L;
        this.validationQuery = null;
        this.connectionTimeoutInMills = 30000L;
        this.validationTimeoutInMills = 5000L;
        this.idleTimeoutInMills = 1800000L;
        this.maxLifetimeInMills = 1800000L;
        this.maxPoolSize = 10;
        this.minIdle = 10;
        this.isAutoCommit = true;
        this.isReadOnly = false;
        this.transactionIsolationName = "TRANSACTION_READ_COMMITTED";
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getCatalog() {
        return this.catalog;
    }

    public void setCatalog(final String catalog) {
        this.catalog = Strings.getNullIfEmpty(catalog);
    }

    public long getLeakDetectionThresholdInMills() {
        return this.leakDetectionThresholdInMills;
    }

    public void setLeakDetectionThresholdInMills(final long leakDetectionThresholdInMills) {
        this.leakDetectionThresholdInMills = leakDetectionThresholdInMills;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public long getConnectionTimeoutInMills() {
        return this.connectionTimeoutInMills;
    }

    public void setConnectionTimeoutInMills(final long connectionTimeoutInMills) {
        this.connectionTimeoutInMills = connectionTimeoutInMills;
    }

    public long getValidationTimeoutInMills() {
        return this.validationTimeoutInMills;
    }

    public void setValidationTimeoutInMills(final long validationTimeoutInMills) {
        this.validationTimeoutInMills = validationTimeoutInMills;
    }

    public long getIdleTimeoutInMills() {
        return this.idleTimeoutInMills;
    }

    public void setIdleTimeoutInMills(final long idleTimeoutInMills) {
        this.idleTimeoutInMills = idleTimeoutInMills;
    }

    public long getMaxLifetimeInMills() {
        return this.maxLifetimeInMills;
    }

    public void setMaxLifetimeInMills(final long maxLifetimeInMills) {
        this.maxLifetimeInMills = maxLifetimeInMills;
    }

    public int getMaxPoolSize() {
        return this.maxPoolSize;
    }

    public void setMaxPoolSize(final int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinIdle() {
        return this.minIdle;
    }

    public void setMinIdle(final int minIdle) {
        this.minIdle = minIdle;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public void setDriverClassName(final String driverClassName) {
        this.driverClassName = Strings.getNullIfEmpty(driverClassName);
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(final String url) {
        this.url = Strings.getNullIfEmpty(url);
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(final String username) {
        this.username = Strings.getNullIfEmpty(username);
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(final String password) {
        this.password = Strings.getNullIfEmpty(password);
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = Strings.getNullIfEmpty(name);
    }

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(final String schema) {
        this.schema = Strings.getNullIfEmpty(schema);
    }

    public boolean isAutoCommit() {
        return this.isAutoCommit;
    }

    public void setAutoCommit(final boolean autoCommit) {
        this.isAutoCommit = autoCommit;
    }

    public String getValidationQuery() {
        return this.validationQuery;
    }

    public void setValidationQuery(final String validationQuery) {
        this.validationQuery = Strings.getNullIfEmpty(validationQuery);
    }

    public boolean isReadOnly() {
        return this.isReadOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.isReadOnly = readOnly;
    }

    public String getTransactionIsolationName() {
        return this.transactionIsolationName;
    }

    public void setTransactionIsolationName(final String transactionIsolationName) {
        this.transactionIsolationName = Strings.getNullIfEmpty(transactionIsolationName);
    }

    public Properties getDriverProps() {
        return driverProps;
    }

    public void setDriverProps(Properties driverProps) {
        this.driverProps = driverProps;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    @Override
    public String getId() {
        return group + DataSources.getDatasourceIdSeparator() + name;
    }

    @Override
    public void setId(String idString) {
        String separator = DataSources.getDatasourceIdSeparator();
        if (!Strings.contains(idString, separator)) {
            throw new IllegalArgumentException(StringTemplates.formatWithPlaceholder("Illegal datasource id: {}", "/"));
        }

        int index = idString.indexOf(separator);
        if (index > 0) {
            String group = idString.substring(0, index);
            String name = idString.substring(index + separator.length());
            if (Strings.isNotBlank(group)) {
                setGroup(group);
            }
            if (Strings.isNotBlank(name)) {
                setName(name);
            }
        }
    }

    public DataSourceKey getDataSourceKey(){
        return new DataSourceKey(group, name);
    }
}
