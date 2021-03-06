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
package org.sonar.plugins.core.utils;

import org.apache.commons.io.IOUtils;
import org.sonar.api.BatchExtension;
import org.sonar.api.utils.SonarException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @since 4.0
 */
public final class HashBuilder implements BatchExtension {

  public String computeHash(File file) {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
      return org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
    } catch (IOException e) {
      throw new SonarException("Unable to compute file hash", e);
    } finally {
      IOUtils.closeQuietly(fis);
    }
  }
}
