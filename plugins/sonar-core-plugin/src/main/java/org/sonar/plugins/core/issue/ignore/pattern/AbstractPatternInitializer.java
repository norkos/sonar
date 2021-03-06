/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.core.issue.ignore.pattern;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.core.issue.ignore.IgnoreIssuesConfiguration;
import static com.google.common.base.Objects.firstNonNull;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;

public abstract class AbstractPatternInitializer implements BatchExtension {

  private Settings settings;

  private List<IssuePattern> multicriteriaPatterns;

  private PatternMatcher patternMatcher;

  protected AbstractPatternInitializer(Settings settings) {
    this.settings = settings;
    this.patternMatcher = new PatternMatcher();
    initPatterns();
  }

  protected Settings getSettings() {
    return settings;
  }

  public PatternMatcher getPatternMatcher() {
    return patternMatcher;
  }

  public List<IssuePattern> getMulticriteriaPatterns() {
    return multicriteriaPatterns;
  }

  public boolean hasConfiguredPatterns() {
    return hasMulticriteriaPatterns();
  }

  public boolean hasMulticriteriaPatterns() {
    return ! multicriteriaPatterns.isEmpty();
  }

  public void initializePatternsForPath(String relativePath, String componentKey) {
    for (IssuePattern pattern: getMulticriteriaPatterns()) {
      if (shouldAddPatternIfMatch(pattern.matchResource(relativePath))) {
        getPatternMatcher().addPatternForComponent(componentKey, pattern);
      }
    }
  }

  protected abstract boolean shouldAddPatternIfMatch(boolean match);

  @VisibleForTesting
  protected final void initPatterns() {
    loadPatternsFromNewProperties();
  }

  protected abstract String getMulticriteriaConfigurationKey();

  protected void loadPatternsFromNewProperties() {
    // Patterns Multicriteria
    multicriteriaPatterns = Lists.newArrayList();
    String patternConf = StringUtils.defaultIfBlank(settings.getString(getMulticriteriaConfigurationKey()), "");
    for (String id : StringUtils.split(patternConf, ',')) {
      String propPrefix = getMulticriteriaConfigurationKey() + "." + id + ".";
      String resourceKeyPattern = settings.getString(propPrefix + IgnoreIssuesConfiguration.RESOURCE_KEY);
      String ruleKeyPattern = settings.getString(propPrefix + IgnoreIssuesConfiguration.RULE_KEY);
      String lineRange = settings.getString(propPrefix + IgnoreIssuesConfiguration.LINE_RANGE_KEY);
      String[] fields = new String[] { resourceKeyPattern, ruleKeyPattern, lineRange };
      PatternDecoder.checkRegularLineConstraints(StringUtils.join(fields, ","), fields);
      IssuePattern pattern = new IssuePattern(firstNonNull(resourceKeyPattern, "*"), firstNonNull(ruleKeyPattern, "*"));
      PatternDecoder.decodeRangeOfLines(pattern, firstNonNull(lineRange, "*"));
      multicriteriaPatterns.add(pattern);
    }
  }
}
