/**
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
package org.terracotta.management.entity.monitoringconsumer.client;

import org.terracotta.management.entity.monitoringconsumer.MonitoringConsumer;
import org.terracotta.voltron.proxy.client.ProxyEntityClientService;

/**
 * @author Mathieu Carbou
 */
public class MonitoringConsumerEntityClientService extends ProxyEntityClientService<MonitoringConsumerEntity, Void> {
  public MonitoringConsumerEntityClientService() {
    super(MonitoringConsumerEntity.class, MonitoringConsumer.class, Void.class);
  }
}