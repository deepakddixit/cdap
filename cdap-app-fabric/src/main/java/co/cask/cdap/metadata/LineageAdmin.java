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

package co.cask.cdap.metadata;

import co.cask.cdap.app.store.Store;
import co.cask.cdap.common.NotFoundException;
import co.cask.cdap.common.app.RunIds;
import co.cask.cdap.data2.metadata.lineage.Lineage;
import co.cask.cdap.data2.metadata.lineage.LineageStore;
import co.cask.cdap.data2.metadata.lineage.Relation;
import co.cask.cdap.data2.metadata.store.MetadataStore;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.metadata.MetadataRecord;
import co.cask.cdap.proto.metadata.MetadataScope;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.apache.twill.api.RunId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service to compute Lineage based on Dataset accesses of a Program stored in {@link LineageStore}.
 */
public class LineageAdmin {
  private static final Logger LOG = LoggerFactory.getLogger(LineageAdmin.class);

  private static final Function<Relation, Id.Program> RELATION_TO_PROGRAM_FUNCTION =
    new Function<Relation, Id.Program>() {
      @Override
      public Id.Program apply(Relation input) {
        return input.getProgram();
      }
    };

  private static final Function<Relation, Id.NamespacedId> RELATION_TO_DATA_FUNCTION =
    new Function<Relation, Id.NamespacedId>() {
      @Override
      public Id.NamespacedId apply(Relation input) {
        return input.getData();
      }
    };

  private final LineageStore lineageStore;
  private final Store store;
  private final MetadataStore metadataStore;
  private final EntityValidator entityValidator;

  @Inject
  LineageAdmin(LineageStore lineageStore, Store store, MetadataStore metadataStore,
               EntityValidator entityValidator) {
    this.lineageStore = lineageStore;
    this.store = store;
    this.metadataStore = metadataStore;
    this.entityValidator = entityValidator;
  }

  /**
   * Computes lineage for a dataset between given time period.
   *
   * @param sourceDataset dataset to compute lineage for
   * @param startMillis start time period
   * @param endMillis end time period
   * @param levels number of levels to compute lineage for
   * @return lineage for sourceDataset
   */
  public Lineage computeLineage(final Id.DatasetInstance sourceDataset, long startMillis, long endMillis, int levels)
    throws NotFoundException {
    return doComputeLineage(sourceDataset, startMillis, endMillis, levels);
  }

  /**
   * Computes lineage for a stream between given time period.
   *
   * @param sourceStream stream to compute lineage for
   * @param startMillis start time period
   * @param endMillis end time period
   * @param levels number of levels to compute lineage for
   * @return lineage for sourceStream
   */
  public Lineage computeLineage(final Id.Stream sourceStream, long startMillis, long endMillis, int levels)
    throws NotFoundException {
    return doComputeLineage(sourceStream, startMillis, endMillis, levels);
  }

  /**
   * @return metadata associated with a run
   */
  public Set<MetadataRecord> getMetadataForRun(Id.Run run) throws NotFoundException {
    entityValidator.ensureRunExists(run);

    Set<Id.NamespacedId> runEntities = new HashSet<>(lineageStore.getEntitiesForRun(run));
    // No entities associated with the run, but run exists.
    if (runEntities.isEmpty()) {
      return ImmutableSet.of();
    }

    RunId runId = RunIds.fromString(run.getId());

    // The entities returned by lineageStore does not contain application
    Id.Application application = run.getProgram().getApplication();
    runEntities.add(application);
    return metadataStore.getSnapshotBeforeTime(MetadataScope.USER, runEntities,
                                               RunIds.getTime(runId, TimeUnit.MILLISECONDS));
  }

  Lineage doComputeLineage(final Id.NamespacedId sourceData, long startMillis, long endMillis, int levels)
    throws NotFoundException {
    LOG.trace("Computing lineage for data {}, startMillis {}, endMillis {}, levels {}",
              sourceData, startMillis, endMillis, levels);

    entityValidator.ensureEntityExists(sourceData);

    // Convert start time and end time period into scan keys in terms of program start times.
    Set<RunId> runningInRange = store.getRunningInRange(TimeUnit.MILLISECONDS.toSeconds(startMillis),
                                                        TimeUnit.MILLISECONDS.toSeconds(endMillis));
    if (LOG.isTraceEnabled()) {
      LOG.trace("Got {} rundIds in time range ({}, {})", runningInRange.size(), startMillis, endMillis);
    }

    ScanRangeWithFilter scanRange = getScanRange(runningInRange);
    LOG.trace("Using scan start = {}, scan end = {}", scanRange.getStart(), scanRange.getEnd());

    Set<Relation> relations = new HashSet<>();
    Set<Id.NamespacedId> visitedDatasets = new HashSet<>();
    Set<Id.NamespacedId> toVisitDatasets = new HashSet<>();
    Set<Id.Program> visitedPrograms = new HashSet<>();
    Set<Id.Program> toVisitPrograms = new HashSet<>();

    toVisitDatasets.add(sourceData);
    for (int i = 0; i < levels; ++i) {
      LOG.trace("Level {}", i);
      toVisitPrograms.clear();
      for (Id.NamespacedId d : toVisitDatasets) {
        if (!visitedDatasets.contains(d)) {
          LOG.trace("Visiting dataset {}", d);
          visitedDatasets.add(d);
          // Fetch related programs
          Iterable<Relation> programRelations = getProgramRelations(d, scanRange.getStart(), scanRange.getEnd(),
                                                                    scanRange.getFilter());
          LOG.trace("Got program relations {}", programRelations);
          Iterables.addAll(relations, programRelations);
          Iterables.addAll(toVisitPrograms, Iterables.transform(programRelations, RELATION_TO_PROGRAM_FUNCTION));
        }
      }

      toVisitDatasets.clear();
      for (Id.Program p : toVisitPrograms) {
        if (!visitedPrograms.contains(p)) {
          LOG.trace("Visiting program {}", p);
          visitedPrograms.add(p);
          // Fetch related datasets
          Iterable<Relation> datasetRelations = lineageStore.getRelations(p, scanRange.getStart(), scanRange.getEnd(),
                                                                          scanRange.getFilter());
          LOG.trace("Got data relations {}", datasetRelations);
          Iterables.addAll(relations, datasetRelations);
          Iterables.addAll(toVisitDatasets,
                           Iterables.transform(datasetRelations, RELATION_TO_DATA_FUNCTION));
        }
      }
    }

    Lineage lineage = new Lineage(relations);
    LOG.trace("Got lineage {}", lineage);
    return lineage;
  }

  private Iterable<Relation> getProgramRelations(Id.NamespacedId data, long start, long end,
                                                 Predicate<Relation> filter) {
    if (data instanceof Id.DatasetInstance) {
      return lineageStore.getRelations((Id.DatasetInstance) data, start, end, filter);
    }

    if (data instanceof Id.Stream) {
      return lineageStore.getRelations((Id.Stream) data, start, end, filter);
    }

    throw new IllegalStateException("Unknown data type " + data);
  }

  /**
   * Convert a set of runIds into a scan range based on earliest runtime and latest runtime of runIds.
   * Also, add a scan filter to include only runIds in the given set.
   * @param runIds input runIds set
   * @return scan range
   */
  @VisibleForTesting
  static ScanRangeWithFilter getScanRange(final Set<RunId> runIds) {
    if (runIds.isEmpty()) {
      return new ScanRangeWithFilter(0, 0, Predicates.<Relation>alwaysFalse());
    }

    // Pick the earliest start time and latest start time for lineage range
    long earliest = Long.MAX_VALUE;
    long latest = 0;
    for (RunId runId : runIds) {
      long runStartTime = RunIds.getTime(runId, TimeUnit.MILLISECONDS);
      if (runStartTime < earliest) {
        earliest = runStartTime;
      }
      if (runStartTime > latest) {
        latest = runStartTime;
      }
    }

    // scan end key is exclusive, so need to add 1 to  to include the last runid
    return new ScanRangeWithFilter(earliest, latest + 1, new Predicate<Relation>() {
      @Override
      public boolean apply(Relation input) {
        return runIds.contains(input.getRun());
      }
    });
  }

  @VisibleForTesting
  static class ScanRangeWithFilter {
    private final long start;
    private final long end;
    private final Predicate<Relation> filter;

    public ScanRangeWithFilter(long start, long end, Predicate<Relation> filter) {
      this.start = start;
      this.end = end;
      this.filter = filter;
    }

    public long getStart() {
      return start;
    }

    public long getEnd() {
      return end;
    }

    public Predicate<Relation> getFilter() {
      return filter;
    }
  }
}
