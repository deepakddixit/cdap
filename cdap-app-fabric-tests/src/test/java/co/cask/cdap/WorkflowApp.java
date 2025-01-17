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
package co.cask.cdap;

import co.cask.cdap.api.annotation.Property;
import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.mapreduce.AbstractMapReduce;
import co.cask.cdap.api.mapreduce.MapReduceContext;
import co.cask.cdap.api.spark.AbstractSpark;
import co.cask.cdap.api.spark.JavaSparkExecutionContext;
import co.cask.cdap.api.spark.JavaSparkMain;
import co.cask.cdap.api.workflow.AbstractWorkflow;
import co.cask.cdap.api.workflow.AbstractWorkflowAction;
import co.cask.cdap.api.workflow.WorkflowContext;
import co.cask.cdap.api.workflow.WorkflowToken;
import co.cask.cdap.internal.app.runtime.batch.WordCount;
import co.cask.cdap.runtime.WorkflowTest;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.apache.hadoop.mapreduce.Job;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A workflow app used by {@link WorkflowTest} for testing.
 */
public class WorkflowApp extends AbstractApplication {

  @Override
  public void configure() {
    setName("WorkflowApp");
    setDescription("WorkflowApp");
    addMapReduce(new WordCountMapReduce());
    addSpark(new SparkWorkflowTestApp());
    addWorkflow(new FunWorkflow());
  }

  /**
   *
   */
  public static class FunWorkflow extends AbstractWorkflow {
    public static final String NAME = "FunWorkflow";
    @Override
    public void configure() {
      setName(NAME);
      setDescription("FunWorkflow description");
      addMapReduce("ClassicWordCount");
      addAction(new CustomAction("verify"));
      addSpark("SparkWorkflowTest");
    }
  }

  /**
   *
   */
  public static final class WordCountMapReduce extends AbstractMapReduce {
    public static final String NAME = "ClassicWordCount";
    @Override
    public void configure() {
      setName(NAME);
      setDescription("WordCount job from Hadoop examples");
    }

    @Override
    public void beforeSubmit(MapReduceContext context) throws Exception {
      Map<String, String> args = context.getRuntimeArguments();
      String inputPath = args.get("inputPath");
      String outputPath = args.get("outputPath");
      WordCount.configureJob((Job) context.getHadoopJob(), inputPath, outputPath);
    }

    @Override
    public void onFinish(boolean succeeded, MapReduceContext context) throws Exception {
      // No-op
    }
  }

  public static class SparkWorkflowTestApp extends AbstractSpark {
    public static final String NAME = "SparkWorkflowTest";
    @Override
    public void configure() {
      setName(NAME);
      setDescription("Test Spark with Workflow");
      setMainClass(SparkWorkflowTestProgram.class);
    }
  }

  public static class SparkWorkflowTestProgram implements JavaSparkMain {
    @Override
    public void run(JavaSparkExecutionContext sec) throws Exception {
      JavaSparkContext jsc = new JavaSparkContext();
      File outputDir = new File(sec.getRuntimeArguments().get("outputPath"));
      File successFile = new File(outputDir, "_SUCCESS");
      Preconditions.checkState(successFile.exists());
      try {
        Preconditions.checkState(successFile.delete());
      } catch (Exception e) {
        Throwables.propagate(e);
      }
      List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
      JavaRDD<Integer> distData = jsc.parallelize(data);
      distData.collect();
      // If there are problems accessing workflow token here, Spark will throw a NotSerializableException in the test
      final WorkflowToken workflowToken = sec.getWorkflowToken();
      if (workflowToken != null) {
        workflowToken.put("otherKey", "otherValue");
      }
      distData.map(new Function<Integer, Integer>() {
        @Override
        public Integer call(Integer val) throws Exception {
          if (workflowToken != null && workflowToken.get("tokenKey") != null) {
            return 2 * val;
          }
          return val;
        }
      });
      Preconditions.checkState(!successFile.exists());
    }
  }
  
  /**
   * Action to test configurer-style action configuration, extending AbstractWorkflowAction.
   */
  public static final class CustomAction extends AbstractWorkflowAction {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAction.class);

    private final String name;

    @Property
    private final boolean condition = true;

    public CustomAction(String name) {
      this.name = name;
    }

    @Override
    public void configure() {
      setName(name);
      setDescription(name);
    }

    @Override
    public void initialize(WorkflowContext context) throws Exception {
      super.initialize(context);
      LOG.info("Custom action initialized: " + context.getSpecification().getName());
      WorkflowToken workflowToken = context.getToken();
      if (workflowToken != null) {
        workflowToken.put("tokenKey", "value");
      }
    }

    @Override
    public void destroy() {
      super.destroy();
      LOG.info("Custom action destroyed: " + getContext().getSpecification().getName());
    }

    @Override
    public void run() {
      LOG.info("Custom action run");
      File outputDir = new File(getContext().getRuntimeArguments().get("outputPath"));
      Preconditions.checkState(condition && new File(outputDir, "_SUCCESS").exists());
      LOG.info("Custom run completed.");
    }
  }
}
