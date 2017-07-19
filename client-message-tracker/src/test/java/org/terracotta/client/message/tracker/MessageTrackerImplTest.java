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
package org.terracotta.client.message.tracker;

import org.junit.Test;
import org.terracotta.entity.EntityMessage;
import org.terracotta.entity.EntityResponse;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageTrackerImplTest {

  private TrackerPolicy trackerPolicy = mock(TrackerPolicy.class);

  @Test
  public void trackTrackableMessage() throws Exception {
    EntityMessage message = mock(EntityMessage.class);
    EntityResponse response = mock(EntityResponse.class);
    when(trackerPolicy.trackable(message)).thenReturn(true);

    MessageTracker tracker = new MessageTrackerImpl(trackerPolicy);
    tracker.track(1L, message, response);

    assertThat(tracker.getTrackedResponse(1L), sameInstance(response));
  }

  @Test
  public void trackUnTrackableMessage() throws Exception {
    EntityMessage message = mock(EntityMessage.class);
    EntityResponse response = mock(EntityResponse.class);
    when(trackerPolicy.trackable(message)).thenReturn(false);

    MessageTracker tracker = new MessageTrackerImpl(trackerPolicy);
    tracker.track(1L, message, response);

    assertThat(tracker.getTrackedResponse(1L), nullValue());
  }

  @Test
  public void trackInvalidMessage() throws Exception {
    EntityMessage message = mock(EntityMessage.class);
    EntityResponse response = mock(EntityResponse.class);
    when(trackerPolicy.trackable(message)).thenReturn(true);

    MessageTracker tracker = new MessageTrackerImpl(trackerPolicy);
    tracker.track(-1L, message, response);  // a message with non-positive message id

    assertThat(tracker.getTrackedResponse(-1L), nullValue());
  }

  @Test
  public void reconcile() throws Exception {
    EntityMessage message = mock(EntityMessage.class);
    EntityResponse response = mock(EntityResponse.class);
    when(trackerPolicy.trackable(message)).thenReturn(true);

    MessageTracker tracker = new MessageTrackerImpl(trackerPolicy);
    tracker.track(1L, message, response);
    tracker.track(2L, message, response);
    tracker.track(3L, message, response);

    assertThat(tracker.getTrackedResponse(1L), notNullValue());
    assertThat(tracker.getTrackedResponse(2L), notNullValue());
    assertThat(tracker.getTrackedResponse(3L), notNullValue());

    tracker.reconcile(2L);
    assertThat(tracker.getTrackedResponse(1L), nullValue());
    assertThat(tracker.getTrackedResponse(2L), nullValue());
    assertThat(tracker.getTrackedResponse(3L), notNullValue());
  }

}