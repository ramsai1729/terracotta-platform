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
package org.terracotta.management.tms.entity.server;

import org.terracotta.management.tms.entity.TmsAgent;
import org.terracotta.management.tms.entity.TmsAgentConfig;
import org.terracotta.entity.ClientDescriptor;
import org.terracotta.management.sequence.SequenceGenerator;
import org.terracotta.management.service.monitoring.IMonitoringConsumer;
import org.terracotta.voltron.proxy.server.ProxiedServerEntity;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Mathieu Carbou
 */
class TmsAgentServerEntity extends ProxiedServerEntity<TmsAgent> {

  private final IMonitoringConsumer consumer;
  private final AtomicBoolean connected  = new AtomicBoolean();

  TmsAgentServerEntity(TmsAgentConfig config, IMonitoringConsumer consumer, SequenceGenerator sequenceGenerator) {
    super(new TmsAgentImpl(config, consumer, sequenceGenerator));
    this.consumer = consumer;
  }

  @Override
  public void destroy() {
    consumer.close();
    super.destroy();
  }

  @Override
  public void connected(ClientDescriptor clientDescriptor) {
    super.connected(clientDescriptor);
    if(!connected.compareAndSet(false, true)) {
      throw new AssertionError("Only one connection allowed per TmsAgentServerEntity");
    }
  }

  @Override
  public void disconnected(ClientDescriptor clientDescriptor) {
    connected.set(false);
    super.disconnected(clientDescriptor);
  }
}
