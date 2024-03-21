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

package org.apache.shardingsphere.metadata.persist;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.metadata.persist.service.config.database.datasource.DataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.rule.DatabaseRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.GlobalRulePersistService;
import org.apache.shardingsphere.metadata.persist.service.config.global.PropertiesPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MetaDataPersistServiceTest {

    @Mock
    private DataSourceUnitPersistService dataSourceService;

    @Mock
    private DatabaseRulePersistService databaseRulePersistService;

    @Mock
    private GlobalRulePersistService globalRuleService;

    @Mock
    private PropertiesPersistService propsService;

    @Mock
    private DatabaseMetaDataPersistService databaseMetaDataService;

    @Mock
    private DataSourceUnitPersistService dataSourceUnitService;

    private MetaDataPersistService metaDataPersistService;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        metaDataPersistService = new MetaDataPersistService(mock(PersistRepository.class));
        setField("dataSourceUnitService", dataSourceService);
        setField("databaseRulePersistService", databaseRulePersistService);
        setField("globalRuleService", globalRuleService);
        setField("propsService", propsService);
        setField("databaseMetaDataService", databaseMetaDataService);
        setField("dataSourceUnitService", dataSourceUnitService);
    }

    private void setField(final String name, final Object value) throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(metaDataPersistService.getClass().getDeclaredField(name), metaDataPersistService, value);
    }

    @Test
    void assertLoadDataSourceConfigurations() {
        assertTrue(metaDataPersistService.loadDataSourceConfigurations("foo_db").isEmpty());
    }

    @Test
    void testPersistGlobalRuleConfiguration() {
        doNothing().when(globalRuleService).persist(anyCollection());
        doNothing().when(propsService).persist(any());
        metaDataPersistService.persistGlobalRuleConfiguration(Collections.emptyList(), new Properties());
    }

    @Test
    void testPersistConfigurations() {
        doNothing().when(databaseMetaDataService).addDatabase(anyString());
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.emptyList());
        metaDataPersistService.persistConfigurations("123", databaseConfig, Collections.emptyMap(), Collections.emptyList());
    }
}
