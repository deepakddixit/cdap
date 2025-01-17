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

package co.cask.cdap.spark.app

import java.util.concurrent.TimeUnit

import co.cask.cdap.api.common.Bytes
import co.cask.cdap.api.spark.{AbstractSpark, SparkExecutionContext, SparkMain}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.JavaConversions._

/**
  * A Spark application for performing data operations using explicit transaction.
  */
class ExplicitTransactionSpark extends AbstractSpark with SparkMain  {

  override protected def configure() = setMainClassName(classOf[ExplicitTransactionSpark].getName)

  override def run(implicit sec: SparkExecutionContext): Unit = {
    val sc = new SparkContext

    val runtimeArgs: Map[String, String] = sec.getRuntimeArguments.toMap
    val streamRDD: RDD[String] = sc.fromStream(runtimeArgs("source.stream"))

    // Compute wordcount on the Stream and stores all the counts to a dataset.
    // Then read the counts back and filter it with a threshold count and store them to another dataset
    // Both steps are done in the same transaction
    Transaction {
      streamRDD
        .flatMap(_.split("\\s+"))
        .map((_, 1))
        .reduceByKey(_ + _)
        .map(t => (Bytes.toBytes(t._1), Bytes.toBytes(t._2)))
        .saveAsDataset(runtimeArgs("result.all.dataset"))

      val rdd: RDD[(Array[Byte], Array[Byte])] = sc.fromDataset(runtimeArgs("result.all.dataset"))
      rdd.map(t => (t._1, Bytes.toInt(t._2)))
        .filter(t => t._2 >= runtimeArgs("result.threshold").toInt)
        .map(t => (t._1, Bytes.toBytes(t._2)))
        .saveAsDataset(runtimeArgs("result.threshold.dataset"))
    }

    // Sleep for 5 mins. This allows the unit-test to verify the dataset results
    // committed by the transaction above.
    // When unit-test try to stop the Spark program, this thread should get interrupted and hence terminating
    // the Spark program
    try {
      TimeUnit.SECONDS.sleep(300)
    } catch {
      case e: InterruptedException => // no-op
    }
  }
}
