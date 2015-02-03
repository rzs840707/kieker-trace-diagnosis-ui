package kieker.diagnosis.subview.util;

import kieker.diagnosis.common.domain.AbstractTrace;

public final class ContainerSortListener extends AbstractTraceTreeColumnSortListener<AbstractTrace<?>> {

	@Override
	protected int compare(final AbstractTrace<?> fstTrace, final AbstractTrace<?> sndTrace) {
		final String fstContainer = fstTrace.getRootOperationCall().getContainer();
		final String sndContainer = sndTrace.getRootOperationCall().getContainer();

		return fstContainer.compareTo(sndContainer);
	}

}