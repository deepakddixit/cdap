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

package co.cask.cdap.test;

import co.cask.cdap.api.flow.AbstractFlow;

/**
 *
 */
public final class TestFlow extends AbstractFlow {

  public static final String NAME = "SomeFlow";
  public static final String INPUT_STREAM = "someStream";


  @Override
  protected void configureFlow() {
    setName(NAME);
    setDescription("SomeDescription");
    addFlowlet(TestFlowlet.NAME, new TestFlowlet());
    connectStream(INPUT_STREAM, "theFlowlet");
  }
}
