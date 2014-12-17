/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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

package kieker.gui.common.importer.stages;

import kieker.gui.common.domain.Execution;
import teetime.framework.AbstractConsumerStage;
import teetime.framework.OutputPort;

public final class FailureContainingTraceFilter extends AbstractConsumerStage<Execution> {

	private final OutputPort<Execution> outputPort = super.createOutputPort();

	@Override
	protected void execute(final Execution element) {
		if (element.containsFailure()) {
			this.outputPort.send(element);
		}
	}

	public OutputPort<Execution> getOutputPort() {
		return this.outputPort;
	}

}