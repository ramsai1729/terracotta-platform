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
package org.terracotta.lease.service.config.parser;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class SchemaProvider {

  public static final URL XML_SCHEMA = SchemaProvider.class.getResource("/lease-service.xsd");
  public static final String NAMESPACE_STRING = "http://www.terracotta.org/service/lease";
  public static final URI NAMESPACE_URI = URI.create(NAMESPACE_STRING);

  public static Source getXmlSchema() throws IOException {
    return new StreamSource(XML_SCHEMA.openStream());
  }
}
