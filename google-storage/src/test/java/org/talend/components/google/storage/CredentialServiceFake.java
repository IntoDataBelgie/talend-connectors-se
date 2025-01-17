/*
 * Copyright (C) 2006-2023 Talend Inc. - www.talend.com
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
package org.talend.components.google.storage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;

import org.talend.components.google.storage.service.CredentialService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CredentialServiceFake extends CredentialService {

    private final Storage storage;

    private final CredentialService wrappedService;

    @Override
    public Storage newStorage(GoogleCredentials credentials) {
        return storage;
    }

    @Override
    public GoogleCredentials getCredentials(String jsonCredentials) {
        return this.wrappedService.getCredentials(jsonCredentials);
    }

    @Override
    public Storage newStorage(GoogleCredentials credentials, String customEndpoint) {
        return storage;
    }
}
