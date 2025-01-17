/*
 * Copyright © 2016 Cask Data, Inc.
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

package co.cask.cdap.api.spark

import co.cask.cdap.api.annotation.Beta
import co.cask.cdap.api.data.batch.Split
import co.cask.cdap.api.flow.flowlet.StreamEvent
import co.cask.cdap.api.metrics.Metrics
import co.cask.cdap.api.plugin.PluginContext
import co.cask.cdap.api.workflow.WorkflowToken
import co.cask.cdap.api.{RuntimeContext, ServiceDiscoverer, TaskLocalizationContext, Transactional}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag

/**
  * Spark program execution context. User Spark program can interact with CDAP through this context.
  */
@Beta
trait SparkExecutionContext extends RuntimeContext with Transactional {

  /**
    * @return The specification used to configure this Spark job instance.
    */
  def getSpecification: SparkSpecification

  /**
    * Returns the logical start time of this Spark job. Logical start time is the time when this Spark
    * job is supposed to start if this job is started by the scheduler. Otherwise it would be the current time when the
    * job runs.
    *
    * @return Time in milliseconds since epoch time (00:00:00 January 1, 1970 UTC).
    */
  def getLogicalStartTime: Long

  /**
    * Returns a [[scala.Serializable]] [[co.cask.cdap.api.ServiceDiscoverer]] for Service Discovery
    * in Spark Program which can be passed in Spark program's closures.
    *
    * @return A [[scala.Serializable]] [[co.cask.cdap.api.ServiceDiscoverer]]
    */
  def getServiceDiscoverer: ServiceDiscoverer

  /**
    * Returns a [[scala.Serializable]] [[co.cask.cdap.api.metrics.Metrics]] which can be used to emit
    * custom metrics from the Spark program. This can also be passed in closures and workers can emit their own metrics.
    *
    * @return A [[scala.Serializable]] [[co.cask.cdap.api.metrics.Metrics]]
    */
  def getMetrics: Metrics

  /**
    * Returns a [[scala.Serializable]] [[co.cask.cdap.api.plugin.PluginContext]] which can be used to request
    * for plugins instances. The instance returned can also be used in Spark program's closures.
    *
    * @return A [[scala.Serializable]] [[co.cask.cdap.api.plugin.PluginContext]]
    */
  def getPluginContext: PluginContext

  /**
    * Returns the [[co.cask.cdap.api.workflow.WorkflowToken]] if the Spark program
    * is started from a [[co.cask.cdap.api.workflow.Workflow]].
    *
    * @return An [[co.cask.cdap.api.workflow.WorkflowToken]] associated with
    *         the current [[co.cask.cdap.api.workflow.Workflow]]
    */
  def getWorkflowToken: Option[WorkflowToken]

  /**
    * Returns the [[co.cask.cdap.api.TaskLocalizationContext]] that gives access to files that were localized
    * by [[co.cask.cdap.api.spark.Spark]] `beforeSubmit` method.
    */
  def getLocalizationContext: TaskLocalizationContext

  /**
    * Creates a [[org.apache.spark.rdd.RDD]] from the given [[co.cask.cdap.api.dataset.Dataset]].
    * Using the implicit object [[co.cask.cdap.api.spark.SparkMain.SparkProgramContextFunctions]] is preferred.
    *
    * @param sc the [[org.apache.spark.SparkContext]] to use
    * @param datasetName name of the Dataset
    * @param arguments arguments for the Dataset
    * @param splits an [[scala.Option]] of [[scala.collection.Iterable]] of [[co.cask.cdap.api.data.batch.Split]]
    *               to be used to compute the partitions of the RDD
    * @tparam K key type
    * @tparam V value type
    * @return A new [[org.apache.spark.rdd.RDD]] instance that reads from the given Dataset.
    * @throws co.cask.cdap.api.data.DatasetInstantiationException if the Dataset doesn't exist
    */
  def fromDataset[K: ClassTag, V: ClassTag](sc: SparkContext,
                                            datasetName: String,
                                            arguments: Map[String, String],
                                            splits: Option[Iterable[_ <: Split]]): RDD[(K, V)]

  /**
    * Creates a [[org.apache.spark.rdd.RDD]] that represents data from the given stream for events in the given
    * time range.
    * Using the implicit object [[co.cask.cdap.api.spark.SparkMain.SparkProgramContextFunctions]] is preferred.
    *
    * @param sc the [[org.apache.spark.SparkContext]] to use
    * @param streamName name of the stream
    * @param startTime  the starting time of the stream to be read in milliseconds (inclusive);
    *                   passing in `0` means start reading from the first event available in the stream.
    * @param endTime the ending time of the streams to be read in milliseconds (exclusive);
    *                passing in `Long#MAX_VALUE` means read up to latest event available in the stream.
    * @param decoder a function to convert a [[co.cask.cdap.api.flow.flowlet.StreamEvent]] to a value
    * @tparam T value type
    * @return a new [[org.apache.spark.rdd.RDD]] instance that reads from the given stream.
    * @throws co.cask.cdap.api.data.DatasetInstantiationException if the Stream doesn't exist
    */
  def fromStream[T: ClassTag](sc: SparkContext, streamName: String, startTime: Long, endTime: Long)
                             (implicit decoder: StreamEvent => T): RDD[T]

  /**
    * Saves the given [[org.apache.spark.rdd.RDD]] to the given [[co.cask.cdap.api.dataset.Dataset]].
    * Using the implicit object [[co.cask.cdap.api.spark.SparkMain.SparkProgramRDDFunctions]] is preferred.
    *
    * @param rdd the [[org.apache.spark.rdd.RDD]] to operate on
    * @param datasetName name of the Dataset
    * @param arguments arguments for the Dataset
    * @throws co.cask.cdap.api.data.DatasetInstantiationException if the Dataset doesn't exist
    */
  def saveAsDataset[K: ClassTag, V: ClassTag](rdd: RDD[(K, V)],
                                              datasetName: String, arguments: Map[String, String]): Unit
}
