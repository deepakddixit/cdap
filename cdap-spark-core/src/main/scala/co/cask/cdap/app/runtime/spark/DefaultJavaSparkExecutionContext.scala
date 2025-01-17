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

package co.cask.cdap.app.runtime.spark

import java.util

import co.cask.cdap.api.app.ApplicationSpecification
import co.cask.cdap.api.data.batch.Split
import co.cask.cdap.api.flow.flowlet.StreamEvent
import co.cask.cdap.api.metrics.Metrics
import co.cask.cdap.api.plugin.PluginContext
import co.cask.cdap.api.spark.{JavaSparkExecutionContext, SparkExecutionContext, SparkSpecification}
import co.cask.cdap.api.stream.StreamEventDecoder
import co.cask.cdap.api.workflow.WorkflowToken
import co.cask.cdap.api.{Admin, ServiceDiscoverer, TxRunnable}
import co.cask.cdap.data.stream.StreamInputFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.LongWritable
import org.apache.spark.api.java.{JavaPairRDD, JavaRDD}
import org.apache.spark.rdd.RDD
import org.apache.twill.api.RunId

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

/**
  * Implementation of [[co.cask.cdap.api.spark.JavaSparkExecutionContext]] that simply delegates all calls to
  * a [[co.cask.cdap.api.spark.SparkExecutionContext]].
  */
class DefaultJavaSparkExecutionContext(sec: SparkExecutionContext) extends JavaSparkExecutionContext {

  import DefaultJavaSparkExecutionContext._

  override def getSpecification: SparkSpecification = sec.getSpecification

  override def getMetrics: Metrics = sec.getMetrics

  override def getServiceDiscoverer: ServiceDiscoverer = sec.getServiceDiscoverer

  override def getLogicalStartTime: Long = sec.getLogicalStartTime

  override def getPluginContext: PluginContext = sec.getPluginContext

  override def getWorkflowToken: WorkflowToken = sec.getWorkflowToken.orNull

  override def getLocalizationContext = sec.getLocalizationContext

  override def getRuntimeArguments: util.Map[String, String] = sec.getRuntimeArguments

  override def getRunId: RunId = sec.getRunId

  override def getNamespace: String = sec.getNamespace

  override def getApplicationSpecification: ApplicationSpecification = sec.getApplicationSpecification

  override def getAdmin: Admin = sec.getAdmin

  override def execute(runnable: TxRunnable): Unit = sec.execute(runnable)

  override def fromDataset[K, V](datasetName: String, arguments: util.Map[String, String],
                                 splits: java.lang.Iterable[_ <: Split]): JavaPairRDD[K, V] = {
    // Create the implicit fake ClassTags to satisfy scala type system at compilation time.
    implicit val kTag: ClassTag[K] = createClassTag
    implicit val vTag: ClassTag[V] = createClassTag
    JavaPairRDD.fromRDD(
      sec.fromDataset(SparkContextCache.getContext, datasetName, arguments.toMap, Option(splits).map(_.toIterable)))
  }

  override def fromStream(streamName: String, startTime: Long, endTime: Long) : JavaRDD[StreamEvent] = {
    val ct: ClassTag[StreamEvent] = createClassTag
    JavaRDD.fromRDD(
      sec.fromStream(SparkContextCache.getContext, streamName, startTime, endTime)(ct, (e: StreamEvent) => e))
  }

  override def fromStream[V](streamName: String, startTime: Long,
                             endTime: Long, valueType: Class[V]): JavaPairRDD[java.lang.Long, V] = {
    val conf = new Configuration
    StreamInputFormat.inferDecoderClass(conf, valueType)
    val decoderClass: Class[_ <: StreamEventDecoder[LongWritable, V]] = StreamInputFormat.getDecoderClass(conf)

    implicit val ct: ClassTag[StreamEvent] = createClassTag
    implicit val vTag: ClassTag[V] = ClassTag(valueType)
    JavaPairRDD.fromRDD(decodeFromStream(streamName, startTime, endTime, decoderClass)
      .map(t => (t._1.get(): java.lang.Long, t._2)))
  }

  override def fromStream[K, V](streamName: String, startTime: Long, endTime: Long,
                                decoderClass: Class[_ <: StreamEventDecoder[K, V]],
                                keyType: Class[K], valueType: Class[V]): JavaPairRDD[K, V] = {

    implicit val ct: ClassTag[StreamEvent] = createClassTag
    implicit val kTag: ClassTag[K] = ClassTag(keyType)
    implicit val vTag: ClassTag[V] = ClassTag(valueType)

    JavaPairRDD.fromRDD(decodeFromStream(streamName, startTime, endTime, decoderClass))
  }

  override def saveAsDataset[K, V](rdd: JavaPairRDD[K, V], datasetName: String,
                                   arguments: util.Map[String, String]): Unit = {
    // Create the implicit fake ClassTags to satisfy scala type system at compilation time.
    implicit val kTag: ClassTag[K] = createClassTag
    implicit val vTag: ClassTag[V] = createClassTag
    sec.saveAsDataset(JavaPairRDD.toRDD(rdd), datasetName, arguments.toMap)
  }

  /**
    * Creates a [[scala.reflect.ClassTag]] for the parameterized type T.
    */
  private def createClassTag[T]: ClassTag[T] = ClassTag.AnyRef.asInstanceOf[ClassTag[T]]

  /**
    * Reads from the given stream and decode the event with the given decoder class. There will
    * be one instance of decoder created per partition.
    */
  private def decodeFromStream[K: ClassTag, V: ClassTag](streamName: String, startTime: Long, endTime: Long,
                                                         decoderClass: Class[_ <: StreamEventDecoder[K, V]])
                                                        (implicit ct: ClassTag[StreamEvent]): RDD[(K, V)] = {
    val identity = (e: StreamEvent) => e
    sec.fromStream(SparkContextCache.getContext, streamName, startTime, endTime)(ct, identity)
       .mapPartitions(createStreamMap(decoderClass))
  }

}

/**
  * Companion object to provide static helpers
  */
object DefaultJavaSparkExecutionContext {

  /**
    * Creates a function for mapping [[scala.Iterator]] of [[co.cask.cdap.api.flow.flowlet.StreamEvent]] to
    * the desired type using the given decoder.
    */
  def createStreamMap[K, V](decoderClass: Class[_ <: StreamEventDecoder[K, V]]) = (itor: Iterator[StreamEvent]) => {
    val decoder = decoderClass.newInstance
    val result = new StreamEventDecoder.DecodeResult[K, V];
    itor.map(e => decoder.decode(e, result)).map(r => (r.getKey, r.getValue))
  }
}
