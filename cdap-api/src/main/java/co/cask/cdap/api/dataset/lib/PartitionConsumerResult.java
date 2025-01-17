/*
 * Copyright © 2015-2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.api.dataset.lib;

import java.util.Iterator;
import java.util.List;

/**
 * Returns access to an iterator of the requested partitions as well as a {@link PartitionConsumerState} which can be
 * used to request partitions created after the previous request of partitions.
 */
public class PartitionConsumerResult {
  private final PartitionConsumerState partitionConsumerState;
  private final List<PartitionDetail> partitions;

  public PartitionConsumerResult(PartitionConsumerState partitionConsumerState,
                                 List<PartitionDetail> partitions) {
    this.partitionConsumerState = partitionConsumerState;
    this.partitions = partitions;
  }

  public PartitionConsumerState getPartitionConsumerState() {
    return partitionConsumerState;
  }

  public List<PartitionDetail> getPartitions() {
    return partitions;
  }

  /**
   * @deprecated Deprecated as of 3.3.0. Use {@link #getPartitions()} instead.
   */
  @Deprecated
  public Iterator<Partition> getPartitionIterator() {
    final Iterator<PartitionDetail> iterator = partitions.iterator();
    return new Iterator<Partition>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public Partition next() {
        return iterator.next();
      }

      @Override
      public void remove() {
        iterator.remove();
      }
    };
  }
}
