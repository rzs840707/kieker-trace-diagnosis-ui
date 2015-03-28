/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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
 ***************************************************************************/

package kieker.diagnosis.mainview.subview.monitoringstatistics;

import kieker.diagnosis.mainview.subview.ISubController;
import kieker.diagnosis.mainview.subview.ISubView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class MonitoringStatisticsViewController implements ISubController {

	private @Autowired MonitoringStatisticsViewModel model;
	private @Autowired MonitoringStatisticsView view;

	@Override
	public ISubView getView() {
		return this.view;
	}

}
