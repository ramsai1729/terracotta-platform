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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.terracotta.lease.service.config.parser.SchemaProvider.NAMESPACE_STRING;

public class LeaseElementParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(LeaseElementParser.class);

  private static final String LEASE_LENGTH_ELEMENT_NAME =  "lease-length";
  private static final String TIME_UNIT_ATTRIBUTE_NAME =  "unit";

  private LeaseElementParser() {

  }

  public static LeaseElementParser getParser() {
    return new LeaseElementParser();
  }

  public LeaseElement parse(Element element) {
    NodeList childElements = element.getElementsByTagNameNS(NAMESPACE_STRING, LEASE_LENGTH_ELEMENT_NAME);

    if (childElements.getLength() != 1) {
      LOGGER.error("Found " + childElements.getLength() + " lease-length elements. The XSD should have prevented this.");
      throw new AssertionError("The schema for connection-leasing element requires one and only one lease-length element");
    }

    Element leaseLengthElement = (Element) childElements.item(0);

    String leaseLengthString = leaseLengthElement.getTextContent();
    LOGGER.info("Found lease length XML text: " + leaseLengthString);

    String timeUnitString = leaseLengthElement.getAttribute(TIME_UNIT_ATTRIBUTE_NAME);
    LOGGER.info("Found lease length time unit: " + timeUnitString);

    LeaseElement leaseElement = new LeaseElement();
    leaseElement.setLeaseValue(leaseLengthString);
    leaseElement.setTimeUnit(timeUnitString);

    return leaseElement;
  }
}
