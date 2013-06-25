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

package org.sonar.server.issue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.sonar.server.util.RubyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @since 3.7
 */
public class IssueBulkChangeQuery {

  private List<String> issues;
  private List<String> actions;
  private String comment;

  Map<String, Map<String, Object>> propertiesByActions = new HashMap<String, Map<String, Object>>();

  public IssueBulkChangeQuery(Map<String, Object> props, String comment) {
    parse(props, comment);
  }

  @VisibleForTesting
  IssueBulkChangeQuery(Map<String, Object> props) {
    parse(props, null);
  }

  private void parse(Map<String, Object> props, String comment) {
    this.comment = comment;
    this.issues = RubyUtils.toStrings(props.get("issues"));
    if (issues == null || issues.isEmpty()) {
      throw new IllegalArgumentException("Issues must not be empty");
    }
    actions = RubyUtils.toStrings(props.get("actions"));
    if (actions == null || actions.isEmpty()) {
      throw new IllegalArgumentException("At least one action must be provided");
    }
    for (String action : actions) {
      Map<String, Object> actionProperties = getActionProps(action, props);
      if (actionProperties.isEmpty()) {
        throw new IllegalArgumentException("Missing properties for action: "+ action);
      }
      propertiesByActions.put(action, actionProperties);
    }
    if (!Strings.isNullOrEmpty(comment)) {
      actions.add(CommentAction.COMMENT_ACTION_KEY);
      Map<String, Object> commentMap = newHashMap();
      commentMap.put(CommentAction.COMMENT_ACTION_KEY, comment);
      propertiesByActions.put(CommentAction.COMMENT_ACTION_KEY, commentMap);
    }
  }

  public List<String> issues() {
    return issues;
  }

  public List<String> actions() {
    return actions;
  }

  public Map<String, Object> properties(String action) {
    return propertiesByActions.get(action);
  }

  private static Map<String, Object> getActionProps(String action, Map<String, Object> props) {
    Map<String, Object> actionProps = newHashMap();
    for (Map.Entry<String, Object> propsEntry : props.entrySet()) {
      String key = propsEntry.getKey();
      String actionPrefix = action + ".";
      String property = StringUtils.substringAfter(key, actionPrefix);
      if (!property.isEmpty()) {
        actionProps.put(property, propsEntry.getValue());
      }
    }
    props.get(action);
    return actionProps;
  }

}