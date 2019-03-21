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

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.terracotta.config.TCConfigurationParser;
import org.terracotta.config.service.ExtendedConfigParser;
import org.terracotta.config.service.ValidationException;
import org.terracotta.offheapresource.config.MemoryUnit;
import org.terracotta.offheapresource.config.ResourceType;
import org.w3c.dom.Element;

import org.terracotta.offheapresource.config.OffheapResourcesType;
import org.xml.sax.SAXException;

public class OffHeapResourceConfigurationParser implements ExtendedConfigParser {
  
  private static final URL XML_SCHEMA = OffHeapResourceConfigurationParser.class.getResource("/offheap-resource.xsd");
  private static final URI NAMESPACE = URI.create("http://www.terracotta.org/config/offheap-resource");

  @Override
  public Source getXmlSchema() throws IOException {
    return new StreamSource(XML_SCHEMA.openStream());
  }

  @Override
  public URI getNamespace() {
    return NAMESPACE;
  }

  @Override
  public OffHeapResourcesProvider parse(Element elmnt, String string) {
    return new OffHeapResourcesProvider(getOffheapResourcesMap(elmnt));
  }

  @Override
  public void validateAgainst(Element fragment, Element validFragment) throws ValidationException {
    Map<String, Long> validResources = getOffheapResourcesMap(validFragment);
    Map<String, Long> resources = getOffheapResourcesMap(validFragment);

    if (validResources.size() != resources.size()) {
      throw new ValidationException(1000, "Offheap resource count does not match");
    }

    for (Map.Entry<String, Long> entry : validResources.entrySet()) {
      Long resourceSize = resources.get(entry.getKey());
      if (resourceSize == null) {
        throw new ValidationException(1001, "Offheap resource with name " + entry.getKey() + " is missing");
      }

      Long validResourceSize = entry.getValue();

      if (!validResourceSize.equals(resourceSize)) {
        throw new ValidationException(1002, "Offheap resource size does not match for " + entry.getKey());
      }
    }
  }

  private Map<String, Long> getOffheapResourcesMap(Element fragment) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(OffheapResourcesType.class.getPackage().getName(), OffHeapResourceConfigurationParser.class.getClassLoader());
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      Collection<Source> schemaSources = new ArrayList<>();
      schemaSources.add(new StreamSource(TCConfigurationParser.TERRACOTTA_XML_SCHEMA.openStream()));
      schemaSources.add(new StreamSource(XML_SCHEMA.openStream()));
      unmarshaller.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaSources.toArray(new Source[0])));
      @SuppressWarnings("unchecked")
      JAXBElement<OffheapResourcesType> parsed = (JAXBElement<OffheapResourcesType>) unmarshaller.unmarshal(fragment);
      OffheapResourcesType resourcesType = parsed.getValue();

      Map<String, Long> resources = new HashMap<>();
      for (ResourceType type : resourcesType.getResource()) {
        resources.put(type.getName(), longValueExact(convert(type.getValue(), type.getUnit())));
      }

      return resources;
    } catch (JAXBException e) {
      throw new IllegalArgumentException(e);
    } catch (SAXException | IOException e) {
      throw new AssertionError(e);
    }
  }

  static BigInteger convert(BigInteger value, MemoryUnit unit) {
    switch (unit) {
      case B: return value.shiftLeft(0);
      case K_B: return value.shiftLeft(10);
      case MB: return value.shiftLeft(20);
      case GB: return value.shiftLeft(30);
      case TB: return value.shiftLeft(40);
      case PB: return value.shiftLeft(50);
    }
    throw new IllegalArgumentException("Unknown unit " + unit);
  }

  private static final BigInteger MAX_LONG_PLUS_ONE = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);

  static long longValueExact(BigInteger value) {
    if (value.compareTo(MAX_LONG_PLUS_ONE) < 0) {
      return value.longValue();
    } else {
      throw new ArithmeticException("BigInteger out of long range");
    }
  }
}
