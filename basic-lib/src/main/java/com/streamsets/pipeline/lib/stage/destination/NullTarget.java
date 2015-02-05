/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.lib.stage.destination;

import com.streamsets.pipeline.api.Batch;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.StageException;
import com.streamsets.pipeline.api.base.BaseTarget;

@GenerateResourceBundle
@StageDef(
    version = "1.0.0",
    label = "Trash",
    description = "Discards all records",
    icon="trash.png"
)
public class NullTarget extends BaseTarget {

  @Override
  public void write(Batch batch) throws StageException {
  }

}
