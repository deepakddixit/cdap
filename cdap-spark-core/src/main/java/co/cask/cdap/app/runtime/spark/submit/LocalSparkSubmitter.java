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

package co.cask.cdap.app.runtime.spark.submit;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link SparkSubmitter} to submit Spark job that runs in the same local process.
 */
public class LocalSparkSubmitter extends AbstractSparkSubmitter {

  private static final Pattern LOCAL_MASTER_PATTERN = Pattern.compile("local\\[([0-9]+|\\*)\\]");

  @Override
  protected String getMaster(Map<String, String> configs) {
    String master = configs.get("spark.master");
    if (master != null) {
      Matcher matcher = LOCAL_MASTER_PATTERN.matcher(master);
      if (matcher.matches()) {
        return "local[" + matcher.group(1) + "]";
      }
    }
    // Use at least two threads for Spark Streaming
    return "local[2]";
  }
}
