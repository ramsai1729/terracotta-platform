/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.offheapresource;

import org.junit.Test;
import org.terracotta.offheapresource.config.MemoryUnit;
import org.terracotta.offheapresource.config.OffheapResourcesType;
import org.terracotta.offheapresource.config.ResourceType;
import org.terracotta.statistics.StatisticsManager;
import org.terracotta.statistics.ValueStatistic;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OffHeapResourcesProviderTest {

  @Test
  public void testObserverExposed() {
    Map<String, Long> resources = new HashMap<>();
    resources.put("foo", 2L * 1024 * 1024);

    OffHeapResourcesProvider provider = new OffHeapResourcesProvider(resources);

    OffHeapResource offHeapResource = provider.getOffHeapResource(OffHeapResourceIdentifier.identifier("foo"));
    assertThat(offHeapResource.available(), equalTo(2L * 1024 * 1024));

    assertThat(StatisticsManager.nodeFor(offHeapResource).getChildren().size(), equalTo(1));
    ValueStatistic<Long> valueStatistic = (ValueStatistic<Long>) StatisticsManager.nodeFor(offHeapResource).getChildren().iterator().next().getContext().attributes().get("this");
    assertThat(valueStatistic.value(), equalTo(0L));
  }

  @Test
  public void testInitializeWithValidConfig() {
    Map<String, Long> resources = new HashMap<>();
    resources.put("foo", 2L * 1024 * 1024);

    OffHeapResourcesProvider provider = new OffHeapResourcesProvider(resources);

    assertThat(provider.getOffHeapResource(OffHeapResourceIdentifier.identifier("foo")), notNullValue());
    assertThat(provider.getOffHeapResource(OffHeapResourceIdentifier.identifier("foo")).available(), is(2L * 1024 * 1024));
  }

  @Test
  public void testNullReturnOnInvalidResource() {
    Map<String, Long> resources = new HashMap<>();
    resources.put("foo", 2L * 1024 * 1024);

    OffHeapResourcesProvider provider = new OffHeapResourcesProvider(resources);

    assertThat(provider.getOffHeapResource(OffHeapResourceIdentifier.identifier("bar")), nullValue());
  }


  @Test
  public void testResourceMax() throws Exception {
    Map<String, Long> resources = new HashMap<>();
    resources.put("foo", Long.MAX_VALUE);

    OffHeapResourcesProvider provider = new OffHeapResourcesProvider(resources);
  }
}
