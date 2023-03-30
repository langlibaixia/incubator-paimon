/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.flink.source;

import org.apache.paimon.flink.utils.TableScanUtils;
import org.apache.paimon.table.source.ReadBuilder;

import org.apache.flink.api.connector.source.Boundedness;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;

import javax.annotation.Nullable;

import java.util.Collection;

/** Bounded {@link FlinkSource} for reading records. It does not monitor new snapshots. */
public class StaticFileStoreSource extends FlinkSource {

    private static final long serialVersionUID = 3L;

    private final int splitBatchSize;
    private final TableScanUtils.TableScanFactory scanFactory;

    public StaticFileStoreSource(
            ReadBuilder readBuilder, @Nullable Long limit, int splitBatchSize) {
        this(readBuilder, limit, splitBatchSize, ReadBuilder::newScan);
    }

    public StaticFileStoreSource(
            ReadBuilder readBuilder,
            @Nullable Long limit,
            int splitBatchSize,
            TableScanUtils.TableScanFactory scanFactory) {
        super(readBuilder, limit);
        this.splitBatchSize = splitBatchSize;
        this.scanFactory = scanFactory;
    }

    @Override
    public Boundedness getBoundedness() {
        return Boundedness.BOUNDED;
    }

    @Override
    public SplitEnumerator<FileStoreSourceSplit, PendingSplitsCheckpoint> restoreEnumerator(
            SplitEnumeratorContext<FileStoreSourceSplit> context,
            PendingSplitsCheckpoint checkpoint) {
        Collection<FileStoreSourceSplit> splits =
                checkpoint == null
                        ? new FileStoreSourceSplitGenerator()
                                .createSplits(scanFactory.create(readBuilder).plan())
                        : checkpoint.splits();

        return new StaticFileStoreSplitEnumerator(context, null, splits, splitBatchSize);
    }
}