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
package org.talend.components.common.stream.output.excel;

import org.talend.components.common.stream.api.output.RecordWriter;
import org.talend.components.common.stream.api.output.RecordWriterSupplier;
import org.talend.components.common.stream.api.output.TargetFinder;
import org.talend.components.common.stream.format.ContentFormat;
import org.talend.components.common.stream.format.excel.ExcelConfiguration;
import org.talend.components.common.stream.format.excel.ExcelConfiguration.ExcelFormat;

public class ExcelWriterSupplier implements RecordWriterSupplier {

    @Override
    public RecordWriter getWriter(TargetFinder target, ContentFormat config) {
        if (!(config instanceof ExcelConfiguration)) {
            throw new IllegalArgumentException("try to get excel-writer with other than excel config");
        }

        final ExcelConfiguration excelConfig = (ExcelConfiguration) config;
        if (excelConfig.getExcelFormat() == ExcelFormat.HTML) {
            throw new IllegalArgumentException("HTML Excel format is not supported for writing.");
        }

        return new ExcelWriter(excelConfig, target);
    }
}
