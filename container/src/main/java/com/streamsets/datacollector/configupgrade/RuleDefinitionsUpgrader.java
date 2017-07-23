/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.datacollector.configupgrade;

import com.google.common.base.Preconditions;
import com.streamsets.datacollector.config.RuleDefinitions;
import com.streamsets.datacollector.store.PipelineStoreTask;
import com.streamsets.datacollector.validation.RuleIssue;
import com.streamsets.datacollector.validation.ValidationError;
import com.streamsets.pipeline.api.Config;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RuleDefinitionsUpgrader {
  private static final Logger LOG = LoggerFactory.getLogger(PipelineConfigurationUpgrader.class);

  private static final RuleDefinitionsUpgrader UPGRADER = new RuleDefinitionsUpgrader() {};

  public static RuleDefinitionsUpgrader get() {
    return UPGRADER;
  }

  private RuleDefinitionsUpgrader() {
  }

  public RuleDefinitions upgradeIfNecessary(
      String pipelineId,
      RuleDefinitions ruleDefinitions,
      List<RuleIssue> issues
  ) {
    Preconditions.checkArgument(issues.size() == 0, "Given list of issues must be empty.");
    boolean upgrade;

    // Firstly upgrading schema if needed, then data
    upgrade = needsSchemaUpgrade(ruleDefinitions, issues);
    if(upgrade && issues.isEmpty()) {
      ruleDefinitions = upgradeSchema(pipelineId, ruleDefinitions, issues);
    }

    return (issues.isEmpty()) ? ruleDefinitions : null;
  }

  private boolean needsSchemaUpgrade(RuleDefinitions ruleDefinitions, List<RuleIssue> ownIssues) {
    return ruleDefinitions.getSchemaVersion() != PipelineStoreTask.RULE_DEFINITIONS_SCHEMA_VERSION;
  }

  private RuleDefinitions upgradeSchema(String pipelineId, RuleDefinitions ruleDefinitions, List<RuleIssue> issues) {
    LOG.debug("Upgrading schema from version {} on rule definitions for pipeline {}",
        ruleDefinitions.getSchemaVersion(), pipelineId);
    switch (ruleDefinitions.getSchemaVersion()) {
      case 1:
      case 2:
        upgradeSchema2to3(ruleDefinitions, issues);
        break;
      default:
        issues.add(RuleIssue.createRuleIssue(null, ValidationError.VALIDATION_0000, ruleDefinitions.getSchemaVersion()));
    }

    ruleDefinitions.setSchemaVersion(PipelineStoreTask.RULE_DEFINITIONS_SCHEMA_VERSION);
    return issues.isEmpty() ? ruleDefinitions : null;
  }

  private void upgradeSchema2to3(RuleDefinitions ruleDefinitions, List<RuleIssue> issues) {
    if (!CollectionUtils.isEmpty(ruleDefinitions.getEmailIds())) {
      List<Config> configList = ruleDefinitions.getConfiguration();
      for (int i = 0; i < configList.size(); i++) {
        Config config = configList.get(i);
        if (config.getName().equals("emailIDs")) {
          configList.remove(config);
          break;
        }
      }
      configList.add(new Config("emailIDs", ruleDefinitions.getEmailIds()));
    }
  }
}
