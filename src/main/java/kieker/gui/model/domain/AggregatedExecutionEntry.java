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

package kieker.gui.model.domain;

public final class AggregatedExecutionEntry extends AbstractExecutionEntry<AggregatedExecutionEntry> {

	private long minDuration;
	private long maxDuration;
	private long avgDuration;
	private int calls;

	public AggregatedExecutionEntry(final ExecutionEntry execEntry) {
		super(execEntry.getContainer(), execEntry.getComponent(), execEntry.getOperation());

		this.setFailedCause(execEntry.getFailedCause());
		this.minDuration = execEntry.getDuration();
		this.maxDuration = execEntry.getDuration();

		for (final ExecutionEntry child : execEntry.getChildren()) {
			super.addExecutionEntry(new AggregatedExecutionEntry(child));
		}
	}

	public void incrementCalls(final ExecutionEntry executionEntry) {
		this.calls++;
		this.minDuration = Math.min(this.minDuration, executionEntry.getDuration());
		this.maxDuration = Math.max(this.maxDuration, executionEntry.getDuration());
		this.avgDuration += executionEntry.getDuration();
	}

	public void recalculateValues() {
		this.avgDuration /= this.calls;
	}

	public long getMinDuration() {
		return this.minDuration;
	}

	public long getMaxDuration() {
		return this.maxDuration;
	}

	public long getAvgDuration() {
		return this.avgDuration;
	}

	public int getCalls() {
		return this.calls;
	}

}