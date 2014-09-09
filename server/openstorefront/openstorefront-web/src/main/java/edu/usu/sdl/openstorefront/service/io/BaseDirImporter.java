/*
 * Copyright 2014 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.openstorefront.service.io;

import edu.usu.sdl.openstorefront.service.ServiceProxy;
import edu.usu.sdl.openstorefront.util.TimeUtil;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.jobs.DirectoryScanListener;

/**
 * Handles the basic flow of the Directory Scan Importers
 *
 * @author dshurtleff
 */
public abstract class BaseDirImporter
		implements DirectoryScanListener
{

	private static final Logger log = Logger.getLogger(BaseDirImporter.class.getName());

	protected final ServiceProxy serviceProxy = new ServiceProxy();

	@Override
	public void filesUpdatedOrAdded(File[] updatedFiles)
	{
		String lastSyncDts = serviceProxy.getSystemService().getPropertyValue(getSyncProperty());
		log.log(Level.FINER, MessageFormat.format("{0} Last Sync time:  {1}", new Object[]{getSyncProperty(), lastSyncDts}));

		for (File file : updatedFiles) {

			boolean process = true;
			Date lastSyncDate = null;
			if (lastSyncDts != null) {
				lastSyncDate = TimeUtil.fromString(lastSyncDts);
			}
			if (lastSyncDate != null) {
				if (file.lastModified() <= lastSyncDate.getTime()) {
					process = false;
				}
			}

			if (process) {
				processFile(file);
			}
		}
		serviceProxy.getSystemService().saveProperty(getSyncProperty(), TimeUtil.dateToString(TimeUtil.currentDate()));
	}

	protected abstract String getSyncProperty();

	protected abstract void processFile(File file);

}