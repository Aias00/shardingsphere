/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.metadata.database.resource;

import lombok.Getter;
import org.apache.shardingsphere.infra.datasource.pool.CatalogSwitchableDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeAggregator;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNodeName;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapUtils;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Resource meta data.
 */
@Getter
public final class ResourceMetaData {
    
    private final Map<StorageNodeName, DataSource> dataSources;
    
    private final Map<String, StorageUnit> storageUnits;
    
    public ResourceMetaData(final Map<String, DataSource> dataSources) {
        this.dataSources = StorageNodeAggregator.aggregateDataSources(dataSources);
        Map<String, StorageNode> storageNodes = StorageUnitNodeMapUtils.fromDataSources(dataSources);
        Map<String, DataSourcePoolProperties> dataSourcePoolPropertiesMap = dataSources.entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, entry -> DataSourcePoolPropertiesCreator.create(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        storageUnits = new LinkedHashMap<>();
        for (Entry<String, StorageNode> entry : storageNodes.entrySet()) {
            DataSource dataSource = dataSources.get(entry.getValue().getName().getName());
            if (!(dataSource instanceof CatalogSwitchableDataSource)) {
                dataSource = new CatalogSwitchableDataSource(dataSource, entry.getValue().getCatalog(), entry.getValue().getUrl());
            }
            storageUnits.put(entry.getKey(), new StorageUnit(null, entry.getValue(), dataSourcePoolPropertiesMap.get(entry.getKey()), dataSource));
        }
    }
    
    public ResourceMetaData(final String databaseName, final Map<StorageNodeName, DataSource> dataSources,
                            final Map<String, StorageNode> storageNodes, final Map<String, DataSourcePoolProperties> propsMap) {
        this.dataSources = dataSources;
        storageUnits = new LinkedHashMap<>();
        for (Entry<String, StorageNode> entry : storageNodes.entrySet()) {
            DataSource dataSource = dataSources.get(entry.getValue().getName());
            if (!(dataSource instanceof CatalogSwitchableDataSource)) {
                dataSource = new CatalogSwitchableDataSource(dataSource, entry.getValue().getCatalog(), entry.getValue().getUrl());
            }
            storageUnits.put(entry.getKey(), new StorageUnit(databaseName, entry.getValue(), propsMap.get(entry.getKey()), dataSource));
        }
    }
    
    /**
     * Get all instance data source names.
     *
     * @return instance data source names
     */
    public Collection<String> getAllInstanceDataSourceNames() {
        Collection<String> result = new LinkedList<>();
        for (String each : storageUnits.keySet()) {
            if (!isExisted(each, result)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isExisted(final String dataSourceName, final Collection<String> existedDataSourceNames) {
        return existedDataSourceNames.stream().anyMatch(each -> storageUnits.get(dataSourceName).getConnectionProperties().isInSameDatabaseInstance(storageUnits.get(each).getConnectionProperties()));
    }
    
    /**
     * Get not existed resource name.
     * 
     * @param resourceNames resource names to be judged
     * @return not existed resource names
     */
    public Collection<String> getNotExistedDataSources(final Collection<String> resourceNames) {
        return resourceNames.stream().filter(each -> !storageUnits.containsKey(each)).collect(Collectors.toSet());
    }
}
