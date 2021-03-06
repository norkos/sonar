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
package org.sonar.server.technicaldebt;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.qualitymodel.Model;
import org.sonar.api.rules.Rule;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.core.qualitymodel.DefaultModelFinder;
import org.sonar.core.rule.DefaultRuleFinder;
import org.sonar.jpa.test.AbstractDbUnitTestCase;

import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TechnicalDebtManagerTest extends AbstractDbUnitTestCase {

  private TechnicalDebtManager manager;
  private TechnicalDebtModelFinder technicalDebtModelFinder = mock(TechnicalDebtModelFinder.class);

  @Before
  public void init() throws Exception {
    technicalDebtModelFinder = mock(TechnicalDebtModelFinder.class);
    when(technicalDebtModelFinder.createReaderForXMLFile("technical-debt")).thenReturn(
      new FileReader(Resources.getResource(TechnicalDebtManagerTest.class, "TechnicalDebtManagerTest/fake-default-model.xml").getPath()));

    manager = new TechnicalDebtManager(getSessionFactory(), new DefaultModelFinder(getSessionFactory()), technicalDebtModelFinder, new XMLImporter());
  }

  @Test
  public void create_only_default_model_on_first_execution_when_no_plugin() throws Exception {
    setupData("empty");

    manager.init(ValidationMessages.create(), defaultRuleCache());

    checkTables("create_default_model_on_first_execution", "quality_models", "characteristics", "characteristic_edges");
  }

  @Test
  public void create_model_with_requirements_from_plugin_on_first_execution() throws Exception {
    setupData("empty");

    addPluginModel("java", "fake-java-model.xml");

    RuleCache ruleCache = mock(RuleCache.class);
    Rule rule1 = Rule.create("checkstyle", "import", "Regular expression");
    rule1.setId(1);
    when(ruleCache.getRule("checkstyle", "import")).thenReturn(rule1);
    Rule rule2 = Rule.create("checkstyle", "export", "Regular expression");
    rule2.setId(2);
    when(ruleCache.getRule("checkstyle", "export")).thenReturn(rule2);

    manager.init(ValidationMessages.create(), ruleCache);

    checkTables("create_model_with_requirements_from_plugin_on_first_execution", "quality_models", "characteristics", "characteristic_edges", "characteristic_properties");
  }

  @Test
  public void add_new_requirements_from_plugin() throws Exception {
    setupData("add_new_requirements_from_plugin");

    addPluginModel("java", "fake-java-model.xml");

    manager.init(ValidationMessages.create(), defaultRuleCache());

    checkTables("add_new_requirements_from_plugin", "quality_models", "characteristics", "characteristic_edges", "characteristic_properties");
  }

  @Test
  public void disable_requirements_on_removed_rules() throws Exception {
    setupData("disable_requirements_on_removed_rules");

    addPluginModel("java", "fake-java-model.xml");

    manager.init(ValidationMessages.create(), defaultRuleCache());

    checkTables("disable_requirements_on_removed_rules", "quality_models", "characteristics", "characteristic_edges", "characteristic_properties");
  }

  @Test
  public void fail_when_plugin_defines_characteristics_not_defined_in_default_model() throws Exception {
    setupData("fail_when_plugin_defines_characteristics_not_defined_in_default_model");

    addPluginModel("java", "fake-java-model-adding-unknown-characteristic.xml");

    try {
      manager.init(ValidationMessages.create(), defaultRuleCache());
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
    }
    checkTables("fail_when_plugin_defines_characteristics_not_defined_in_default_model", "quality_models", "characteristics", "characteristic_edges", "characteristic_properties");
  }

  @Test
  public void recreate_previously_deleted_characteristic_from_default_model_when_plugin_define_requirements_on_it() throws Exception {
    setupData("recreate_previously_deleted_characteristic_from_default_model_when_plugin_define_requirements_on_it");

    addPluginModel("java", "fake-java-model.xml");

    manager.init(ValidationMessages.create(), defaultRuleCache());

    checkTables("recreate_previously_deleted_characteristic_from_default_model_when_plugin_define_requirements_on_it", "quality_models", "characteristics", "characteristic_edges", "characteristic_properties");
  }

  @Test
  public void provided_plugin_should_not_override_default_characteristics_name() throws FileNotFoundException {
    Model model = manager.init(ValidationMessages.create(), defaultRuleCache());
    // Default model values
    assertThat(model.getCharacteristicByKey("PORTABILITY").getName()).isEqualTo("Portability");
    assertThat(model.getCharacteristicByKey("COMPILER_RELATED_PORTABILITY").getName()).isEqualTo("Compiler related portability");
    assertThat(model.getCharacteristicByKey("HARDWARE_RELATED_PORTABILITY").getName()).isEqualTo("Hardware related portability");
    assertThat(model.getCharacteristicByKey("MAINTAINABILITY").getName()).isEqualTo("Maintainability");

    // Plugin has renamed it, but the value should stay as defined by default model
    assertThat(model.getCharacteristicByKey("READABILITY").getName()).isEqualTo("Readability");
  }

  @Test
  public void no_failure_on_unknown_rule() throws FileNotFoundException {
    setupData("empty");

    addPluginModel("java", "fake-java-model.xml");

    RuleCache ruleCache = mock(RuleCache.class);
    Rule rule1 = Rule.create("checkstyle", "import", "Regular expression");
    rule1.setId(1);
    when(ruleCache.getRule("checkstyle", "import")).thenReturn(rule1);
    Rule rule2 = Rule.create("checkstyle", "export", "Regular expression");
    rule2.setId(2);
    when(ruleCache.getRule("checkstyle", "export")).thenReturn(rule2);

    ValidationMessages messages = ValidationMessages.create();
    manager.init(messages, ruleCache);

    assertThat(messages.getWarnings()).hasSize(1);
    assertThat(messages.getWarnings().get(0)).isEqualTo("Rule not found: [repository=checkstyle, key=ConstantNameCheck]");
  }

  private RuleCache defaultRuleCache() {
    return new RuleCache(new DefaultRuleFinder(getSessionFactory()));
  }

  private void addPluginModel(String pluginKey, String xmlFile) throws FileNotFoundException {
    when(technicalDebtModelFinder.getContributingPluginList()).thenReturn(ImmutableList.of(pluginKey));
    when(technicalDebtModelFinder.createReaderForXMLFile(pluginKey)).thenReturn(
      new FileReader(Resources.getResource(TechnicalDebtManagerTest.class, "TechnicalDebtManagerTest/" + xmlFile).getPath()));
  }


}
