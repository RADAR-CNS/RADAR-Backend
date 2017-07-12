/*
 * Copyright 2017 King's College London and The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package org.radarcns.phone;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public enum UsageEventType {
  FOREGROUND, BACKGROUND, CONFIG, SHORTCUT, INTERACTION, NONE  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"UsageEventType\",\"namespace\":\"org.radarcns.phone\",\"symbols\":[\"FOREGROUND\",\"BACKGROUND\",\"CONFIG\",\"SHORTCUT\",\"INTERACTION\",\"NONE\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
}
