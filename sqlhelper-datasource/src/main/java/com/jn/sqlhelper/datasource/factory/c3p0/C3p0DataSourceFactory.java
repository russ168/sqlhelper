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

package com.jn.sqlhelper.datasource.factory.c3p0;

import com.jn.sqlhelper.datasource.factory.DataSourceFactory;
import com.jn.sqlhelper.datasource.DataSources;
import com.jn.sqlhelper.datasource.NamedDataSource;
import com.jn.sqlhelper.datasource.definition.DataSourceProperties;
import com.jn.langx.annotation.Name;
import com.jn.langx.annotation.OnClasses;
import com.jn.langx.text.StringTemplates;

import javax.sql.DataSource;
import java.util.Properties;

@Name(DataSources.DATASOURCE_IMPLEMENT_KEY_C3P0)
@OnClasses({
        "com.mchange.v2.c3p0.WrapperConnectionPoolDataSource",
})
public class C3p0DataSourceFactory implements DataSourceFactory {
    @Override
    public NamedDataSource get(DataSourceProperties dataSourceProperties) {
        if (DataSources.isImplementationKeyMatched(DataSources.DATASOURCE_IMPLEMENT_KEY_C3P0, dataSourceProperties)) {
            DataSource dataSource = C3p0DataSources.createDataSource(dataSourceProperties);
            String name = dataSourceProperties.getName();
            return DataSources.toNamedDataSource(dataSource, name);
        }
        throw new IllegalArgumentException(StringTemplates.formatWithPlaceholder("Illegal datasource implementationKey {}, expected key is {}", dataSourceProperties.getImplementation(), DataSources.DATASOURCE_IMPLEMENT_KEY_C3P0));
    }

    @Override
    public NamedDataSource get(Properties properties) {
        DataSource dataSource = C3p0DataSources.createDataSource(properties);
        String name = properties.getProperty(DataSources.DATASOURCE_NAME);
        return DataSources.toNamedDataSource(dataSource, name);
    }
}
