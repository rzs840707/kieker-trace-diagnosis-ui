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

package kieker.gui.subview.aggregatedtraces;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import kieker.gui.common.domain.AggregatedExecution;
import kieker.gui.common.model.PropertiesModel;
import kieker.gui.subview.ISubView;
import kieker.gui.subview.aggregatedtraces.util.AggregatedExecutionAvgDurationComparator;
import kieker.gui.subview.aggregatedtraces.util.AggregatedExecutionCallComparator;
import kieker.gui.subview.aggregatedtraces.util.AggregatedExecutionMaxDurationComparator;
import kieker.gui.subview.aggregatedtraces.util.AggregatedExecutionMinDurationComparator;
import kieker.gui.subview.aggregatedtraces.util.AggregatedExecutionTotalDurationComparator;
import kieker.gui.subview.util.ExecutionComponentComparator;
import kieker.gui.subview.util.ExecutionContainerComparator;
import kieker.gui.subview.util.ExecutionOperationComparator;
import kieker.gui.subview.util.IModel;
import kieker.gui.subview.util.TreeColumnSortListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

public final class View implements Observer, ISubView {

	private final Model aggregatedTracesSubViewModel;
	private final SelectionListener controller;
	private final IModel<AggregatedExecution> model;
	private Composite composite;
	private Tree tree;
	private Composite detailComposite;
	private Label lblComponentDisplay;
	private Label lblOperationDisplay;
	private Label lblNumberOfCallsDisplay;
	private Label lblMinimalDurationDisplay;
	private Label lblAverageDurationDisplay;
	private Label lblMaximalDurationDisplay;
	private Label lblFailedDisplay;
	private Label lblTraceDepthDisplay;
	private Label lblTraceSizeDisplay;
	private Label lblExecutionContainerDisplay;
	private Label lblFailed;
	private final PropertiesModel propertiesModel;
	private Label lblTotalDurationDisplay;

	public View(final IModel<AggregatedExecution> model, final Model aggregatedTracesSubViewModel, final PropertiesModel propertiesModel,
			final SelectionListener controller) {
		this.controller = controller;
		this.model = model;
		this.propertiesModel = propertiesModel;
		this.aggregatedTracesSubViewModel = aggregatedTracesSubViewModel;

		model.addObserver(this);
		aggregatedTracesSubViewModel.addObserver(this);
		propertiesModel.addObserver(this);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createComposite(final Composite parent) {
		if (this.composite != null) {
			this.composite.dispose();
		}

		this.composite = new Composite(parent, SWT.NONE);
		this.composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		final SashForm sashForm = new SashForm(this.composite, SWT.VERTICAL);

		this.tree = new Tree(sashForm, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
		this.tree.setHeaderVisible(true);

		final TreeColumn trclmnExecutionContainer = new TreeColumn(this.tree, SWT.NONE);
		trclmnExecutionContainer.setWidth(100);
		trclmnExecutionContainer.setText("Execution Container");

		final TreeColumn trclmnComponent = new TreeColumn(this.tree, SWT.NONE);
		trclmnComponent.setWidth(100);
		trclmnComponent.setText("Component");

		final TreeColumn trclmnOperation = new TreeColumn(this.tree, SWT.NONE);
		trclmnOperation.setWidth(100);
		trclmnOperation.setText("Operation");

		final TreeColumn trclmnCalls = new TreeColumn(this.tree, SWT.RIGHT);
		trclmnCalls.setWidth(100);
		trclmnCalls.setText("Number of Calls");

		final TreeColumn trclmnMinimalDuration = new TreeColumn(this.tree, SWT.RIGHT);
		trclmnMinimalDuration.setWidth(100);
		trclmnMinimalDuration.setText("Minimal Duration");

		final TreeColumn trclmnAverageDuration = new TreeColumn(this.tree, SWT.RIGHT);
		trclmnAverageDuration.setWidth(100);
		trclmnAverageDuration.setText("Average Duration");

		final TreeColumn trclmnMaximalDuration = new TreeColumn(this.tree, SWT.RIGHT);
		trclmnMaximalDuration.setWidth(100);
		trclmnMaximalDuration.setText("Maximal Duration");

		final TreeColumn trclmnTotalDuration = new TreeColumn(this.tree, SWT.RIGHT);
		trclmnTotalDuration.setWidth(100);
		trclmnTotalDuration.setText("Total Duration");

		this.detailComposite = new Composite(sashForm, SWT.BORDER);
		this.detailComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.detailComposite.setLayout(new GridLayout(2, false));

		final Label lblExecutionContainer = new Label(this.detailComposite, SWT.NONE);
		lblExecutionContainer.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblExecutionContainer.setText("Execution Container:");

		this.lblExecutionContainerDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblExecutionContainerDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblExecutionContainerDisplay.setText("N/A");

		final Label lblComponent = new Label(this.detailComposite, SWT.NONE);
		lblComponent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblComponent.setText("Component:");

		this.lblComponentDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblComponentDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblComponentDisplay.setText("N/A");

		final Label lblOperation = new Label(this.detailComposite, SWT.NONE);
		lblOperation.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblOperation.setText("Operation:");

		this.lblOperationDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblOperationDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblOperationDisplay.setText("N/A");

		final Label lblNumberOfCalls = new Label(this.detailComposite, SWT.NONE);
		lblNumberOfCalls.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblNumberOfCalls.setText("Number of Calls:");

		this.lblNumberOfCallsDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblNumberOfCallsDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblNumberOfCallsDisplay.setText("N/A");

		final Label lblMinimalDuration = new Label(this.detailComposite, SWT.NONE);
		lblMinimalDuration.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblMinimalDuration.setText("Minimal Duration:");

		this.lblMinimalDurationDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblMinimalDurationDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblMinimalDurationDisplay.setText("N/A");

		final Label lblAverageDuration = new Label(this.detailComposite, SWT.NONE);
		lblAverageDuration.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblAverageDuration.setText("Average Duration:");

		this.lblAverageDurationDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblAverageDurationDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblAverageDurationDisplay.setText("N/A");

		final Label lblMaximalDuration = new Label(this.detailComposite, SWT.NONE);
		lblMaximalDuration.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblMaximalDuration.setText("Maximal Duration:");

		this.lblMaximalDurationDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblMaximalDurationDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblMaximalDurationDisplay.setText("N/A");

		final Label lblTotalDuration = new Label(this.detailComposite, SWT.NONE);
		lblTotalDuration.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblTotalDuration.setText("Total Duration:");

		this.lblTotalDurationDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblTotalDurationDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblTotalDurationDisplay.setText("N/A");

		this.lblFailed = new Label(this.detailComposite, SWT.NONE);
		this.lblFailed.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblFailed.setText("Failed:");

		this.lblFailedDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblFailedDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblFailedDisplay.setText("N/A");

		final Label lblTraceDepth = new Label(this.detailComposite, SWT.NONE);
		lblTraceDepth.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblTraceDepth.setText("Trace Depth:");

		this.lblTraceDepthDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblTraceDepthDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblTraceDepthDisplay.setText("N/A");

		final Label lblTraceSize = new Label(this.detailComposite, SWT.NONE);
		lblTraceSize.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblTraceSize.setText("Trace Size:");

		this.lblTraceSizeDisplay = new Label(this.detailComposite, SWT.NONE);
		this.lblTraceSizeDisplay.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		this.lblTraceSizeDisplay.setText("N/A");
		sashForm.setWeights(new int[] { 2, 1 });

		this.tree.addSelectionListener(this.controller);
		this.tree.addListener(SWT.SetData, new DataProvider());

		trclmnExecutionContainer.addSelectionListener(new TreeColumnSortListener<>(new ExecutionContainerComparator()));
		trclmnComponent.addSelectionListener(new TreeColumnSortListener<>(new ExecutionComponentComparator()));
		trclmnOperation.addSelectionListener(new TreeColumnSortListener<>(new ExecutionOperationComparator()));
		trclmnMinimalDuration.addSelectionListener(new TreeColumnSortListener<>(new AggregatedExecutionMinDurationComparator()));
		trclmnMaximalDuration.addSelectionListener(new TreeColumnSortListener<>(new AggregatedExecutionMaxDurationComparator()));
		trclmnAverageDuration.addSelectionListener(new TreeColumnSortListener<>(new AggregatedExecutionAvgDurationComparator()));
		trclmnCalls.addSelectionListener(new TreeColumnSortListener<>(new AggregatedExecutionCallComparator()));
		trclmnTotalDuration.addSelectionListener(new TreeColumnSortListener<>(new AggregatedExecutionTotalDurationComparator()));
	}

	@Override
	public Composite getComposite() {
		return this.composite;
	}

	@Override
	public void update(final Observable observable, final Object obj) {
		if (observable == this.model) {
			this.updateTree();
		}
		if (observable == this.aggregatedTracesSubViewModel) {
			this.updateDetailComposite();
		}
		if (observable == this.propertiesModel) {
			this.clearTree();
		}
	}

	private void updateTree() {
		final List<AggregatedExecution> records = this.model.getContent();

		this.tree.setData(records);
		this.tree.setItemCount(records.size());

		this.clearTree();
	}

	private void clearTree() {
		this.tree.clearAll(true);

		for (final TreeColumn column : this.tree.getColumns()) {
			column.pack();
		}
	}

	private void updateDetailComposite() {
		final AggregatedExecution trace = this.aggregatedTracesSubViewModel.getCurrentActiveTrace();

		final String minDuration = (Long.toString(trace.getMinDuration()) + " " + this.model.getShortTimeUnit()).trim();
		final String maxDuration = (Long.toString(trace.getMaxDuration()) + " " + this.model.getShortTimeUnit()).trim();
		final String avgDuration = (Long.toString(trace.getAvgDuration()) + " " + this.model.getShortTimeUnit()).trim();
		final String totalDuration = (Long.toString(trace.getTotalDuration()) + " " + this.model.getShortTimeUnit()).trim();

		this.lblMinimalDurationDisplay.setText(minDuration);
		this.lblMaximalDurationDisplay.setText(maxDuration);
		this.lblAverageDurationDisplay.setText(avgDuration);
		this.lblTotalDurationDisplay.setText(totalDuration);

		this.lblExecutionContainerDisplay.setText(trace.getContainer());
		this.lblComponentDisplay.setText(trace.getComponent());
		this.lblOperationDisplay.setText(trace.getOperation());
		this.lblTraceDepthDisplay.setText(Integer.toString(trace.getTraceDepth()));
		this.lblTraceSizeDisplay.setText(Integer.toString(trace.getTraceSize()));
		this.lblNumberOfCallsDisplay.setText(Integer.toString(trace.getCalls()));

		if (trace.isFailed()) {
			this.lblFailedDisplay.setText("Yes (" + trace.getFailedCause() + ")");
			this.lblFailedDisplay.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			this.lblFailed.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		} else {
			this.lblFailedDisplay.setText("No");
			this.lblFailedDisplay.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
			this.lblFailed.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		}

		this.detailComposite.layout();
	}

	private class DataProvider implements Listener {

		@Override
		@SuppressWarnings("unchecked")
		public void handleEvent(final Event event) {
			// Get the necessary information from the event
			final Tree tree = (Tree) event.widget;
			final TreeItem item = (TreeItem) event.item;
			final int tableIndex = event.index;
			final TreeItem parent = item.getParentItem();

			// Decide whether the current item is a root or not
			final AggregatedExecution executionEntry;
			if (parent == null) {
				executionEntry = ((List<AggregatedExecution>) tree.getData()).get(tableIndex);
			} else {
				executionEntry = ((AggregatedExecution) parent.getData()).getChildren().get(tableIndex);
			}

			String componentName = executionEntry.getComponent();
			if (View.this.propertiesModel.isShortComponentNames()) {
				final int lastPointPos = componentName.lastIndexOf('.');
				componentName = componentName.substring(lastPointPos + 1);
			}
			String operationString = executionEntry.getOperation();
			if (View.this.propertiesModel.isShortOperationNames()) {
				operationString = operationString.replaceAll("\\(..*\\)", "(...)");

				final int lastPointPos = operationString.lastIndexOf('.', operationString.length() - 5);
				operationString = operationString.substring(lastPointPos + 1);
			}

			final String minDuration = (Long.toString(executionEntry.getMinDuration()) + " " + View.this.model.getShortTimeUnit()).trim();
			final String maxDuration = (Long.toString(executionEntry.getMaxDuration()) + " " + View.this.model.getShortTimeUnit()).trim();
			final String avgDuration = (Long.toString(executionEntry.getAvgDuration()) + " " + View.this.model.getShortTimeUnit()).trim();
			final String totalDuration = (Long.toString(executionEntry.getTotalDuration()) + " " + View.this.model.getShortTimeUnit()).trim();

			if (parent != null) {
				item.setText(new String[] { executionEntry.getContainer(), componentName, operationString, "", minDuration, avgDuration, maxDuration, totalDuration });
			} else {
				item.setText(new String[] { executionEntry.getContainer(), componentName, operationString, Integer.toString(executionEntry.getCalls()), minDuration, avgDuration,
						maxDuration, totalDuration });
			}

			if (executionEntry.isFailed()) {
				final Color colorRed = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
				item.setForeground(colorRed);
			}

			item.setData(executionEntry);
			item.setItemCount(executionEntry.getChildren().size());
		}

	}

}