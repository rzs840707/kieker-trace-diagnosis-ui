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

package kieker.diagnosis.common.model.importer.stages;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import kieker.common.record.IMonitoringRecord;
import teetime.framework.CompositeStage;
import teetime.framework.OutputPort;
import teetime.framework.Stage;
import teetime.framework.pipe.IPipeFactory;
import teetime.framework.pipe.PipeFactoryRegistry;
import teetime.framework.pipe.PipeFactoryRegistry.PipeOrdering;
import teetime.framework.pipe.PipeFactoryRegistry.ThreadCommunication;
import teetime.stage.InitialElementProducer;
import teetime.stage.className.ClassNameRegistryRepository;
import teetime.stage.io.filesystem.Dir2RecordsFilter;

/**
 * This is a composite stage which deserializes monitoring records from a specific directory and forwards them to the output port.
 *
 * @author Nils Christian Ehmke
 */
public final class ReadingComposite extends CompositeStage {

	private final InitialElementProducer<File> producer;
	private final Dir2RecordsFilter reader;

	public ReadingComposite(final File importDirectory) {
		this.producer = new InitialElementProducer<>(importDirectory);
		this.reader = new Dir2RecordsFilter(new ClassNameRegistryRepository());

		final IPipeFactory pipeFactory = PipeFactoryRegistry.INSTANCE.getPipeFactory(ThreadCommunication.INTRA, PipeOrdering.ARBITRARY, false);
		pipeFactory.create(this.producer.getOutputPort(), this.reader.getInputPort());
	}

	public OutputPort<IMonitoringRecord> getOutputPort() {
		return this.reader.getOutputPort();
	}

	@Override
	protected Stage getFirstStage() {
		return this.producer;
	}

	@Override
	protected Collection<? extends Stage> getLastStages() {
		return Arrays.asList(this.reader);
	}

}
