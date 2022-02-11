/*
 * Copyright (C) 2006-2022 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.mongo.datastore;

import org.talend.components.mongo.Address;
import org.talend.components.mongo.AddressType;
import org.talend.components.mongo.Auth;
import org.talend.components.mongo.ConnectionParameter;

import java.io.Serializable;
import java.util.List;

public interface MongoCommonDataStore extends Serializable {

    String getDatabase();

    AddressType getAddressType();

    Address getAddress();

    List<Address> getReplicaSetAddress();

    Auth getAuth();

    List<ConnectionParameter> getConnectionParameter();
}