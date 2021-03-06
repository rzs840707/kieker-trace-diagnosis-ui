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

package kieker.diagnosis;

import com.google.inject.AbstractModule;

import kieker.diagnosis.architecture.KiekerTraceDiagnosisArchitectureModule;
import kieker.diagnosis.service.KiekerTraceDiagnosisServiceModule;

/**
 * This is the Guice module for the UI.
 *
 * @author Nils Christian Ehmke
 */
public class KiekerTraceDiagnosisUIModule extends AbstractModule {

	@Override
	protected void configure( ) {
		// We need to make sure that the Guice module from the service and the architecture sub-projects are installed
		install( new KiekerTraceDiagnosisServiceModule( ) );
		install( new KiekerTraceDiagnosisArchitectureModule( ) );
	}

}
