/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.core.dashboards;

import org.junit.Test;
import org.sonar.api.web.Dashboard;
import org.sonar.api.web.DashboardLayout;
import org.sonar.plugins.core.CorePlugin;

import static org.fest.assertions.Assertions.assertThat;

public class ProjectHotspotDashboardTest {
  ProjectHotspotDashboard template = new ProjectHotspotDashboard();

  @Test
  public void should_have_a_name() {
    assertThat(template.getName()).isEqualTo("Hotspots");
  }

  @Test
  public void should_be_registered_as_an_extension() {
    assertThat(new CorePlugin().getExtensions()).contains(template.getClass());
  }

  @Test
  public void should_create_dashboard() {
    Dashboard dashboard = template.createDashboard();

    assertThat(dashboard.getLayout()).isEqualTo(DashboardLayout.TWO_COLUMNS);
    assertThat(dashboard.getWidgets()).hasSize(8);
  }
}