/***************************************************************************
 * Copyright 2015-2018 Kieker Project (http://kieker-monitoring.net)
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

package kieker.diagnosis.ui.complex.traces.components;

import java.util.concurrent.TimeUnit;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.util.Callback;
import kieker.diagnosis.architecture.service.ServiceFactory;
import kieker.diagnosis.architecture.service.properties.PropertiesService;
import kieker.diagnosis.service.data.MethodCall;
import kieker.diagnosis.service.settings.properties.TimeUnitProperty;

/**
 * This is a cell factory for a tree table which shows the duration of a method call in the configured manner.
 *
 * @author Nils Christian Ehmke
 */
public class DurationCellValueFactory implements Callback<CellDataFeatures<MethodCall, Long>, ObservableValue<Long>> {

	private final PropertiesService ivPropertiesService = ServiceFactory.getService( PropertiesService.class );

	@Override
	public ObservableValue<Long> call( final CellDataFeatures<MethodCall, Long> aParam ) {
		final TimeUnit timeUnit = ivPropertiesService.loadApplicationProperty( TimeUnitProperty.class );
		return new ReadOnlyObjectWrapper<>( timeUnit.convert( aParam.getValue( ).getValue( ).getDuration( ), TimeUnit.NANOSECONDS ) );
	}

}