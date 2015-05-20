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

package kieker.diagnosis.mainview.subview.database;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import kieker.diagnosis.czi.Utils;
import kieker.diagnosis.domain.DatabaseOperationCall;
import kieker.diagnosis.domain.OperationCall;
import kieker.diagnosis.mainview.subview.ISubView;
import kieker.diagnosis.mainview.subview.database.DatabaseCallsViewModel.Filter;
import kieker.diagnosis.mainview.subview.util.CallTableColumnSortListener;
import kieker.diagnosis.mainview.subview.util.NameConverter;
import kieker.diagnosis.model.DataModel;
import kieker.diagnosis.model.PropertiesModel;
import kieker.diagnosis.model.PropertiesModel.OperationNames;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.wb.swt.SWTResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Christian Zirkelbach
 */

@Component
public final class DatabaseCallsView implements ISubView, Observer {

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("kieker.diagnosis.mainview.subview.database.databasecallsview"); //$NON-NLS-1$

	private static final String N_A = "N/A";

	@Autowired
	private DataModel dataModel;
	@Autowired
	private PropertiesModel propertiesModel;

	@Autowired
	private DatabaseCallsViewModel model;
	@Autowired
	private DatabaseCallsViewController controller;

	private List<DatabaseOperationCall> cachedDataModelContent;

	private Composite composite;
	private Composite detailComposite;
	private Composite statusBar;
	private Label lbCounter;
	private Table table;
	private Text lblFailedDisplay;
	private Text lblMinimalDurationDisplay;
	private Text lblOperationDisplay;
	private Text lblStatementDisplay;
	private Text lblReturnValueDisplay;
	private Label lblFailed;
	private Button btnShowAll;
	private Button btnShowJustFailed;
	private ScrolledComposite ivSc;
	private Text filterText;

	@PostConstruct
	public void initialize() {
		this.updateCachedDataModelContent();
		this.dataModel.addObserver(this);
		this.propertiesModel.addObserver(this);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createComposite(final Composite parent) { // NOPMD (This method
															// violates some
															// metrics)
		if (this.composite != null) {
			this.composite.dispose();
		}

		this.composite = new Composite(parent, SWT.NONE);
		final GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.horizontalSpacing = 0;
		this.composite.setLayout(gl_composite);

		final Composite filterComposite = new Composite(this.composite,
				SWT.NONE);
		final GridLayout gl_filterComposite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.horizontalSpacing = 0;
		filterComposite.setLayout(gl_filterComposite);

		this.btnShowAll = new Button(filterComposite, SWT.RADIO);
		this.btnShowAll.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.btnShowAll.text")); //$NON-NLS-1$
		this.btnShowAll.setSelection(true);
		this.btnShowJustFailed = new Button(filterComposite, SWT.RADIO);
		this.btnShowJustFailed.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.btnShowJustFailed.text")); //$NON-NLS-1$

		this.filterText = new Text(filterComposite, SWT.BORDER);
		this.filterText.setMessage(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.text.message")); //$NON-NLS-1$
		this.filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));

		final SashForm sashForm = new SashForm(this.composite, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));

		this.table = new Table(sashForm, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.VIRTUAL);
		this.table.setHeaderVisible(true);
		this.table.setLinesVisible(true);

		final TableColumn tblclmnOperation = new TableColumn(this.table,
				SWT.NONE);
		tblclmnOperation.setWidth(100);
		tblclmnOperation.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.tblclmnOperation.text")); //$NON-NLS-1$
		
		final TableColumn tblclmnStatement = new TableColumn(this.table,
				SWT.NONE);
		tblclmnStatement.setWidth(400);
		tblclmnStatement.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.tblclmnStatement.text")); //$NON-NLS-1$
		
		final TableColumn tblclmnReturnValue = new TableColumn(this.table,
				SWT.NONE);
		tblclmnReturnValue.setWidth(100);
		tblclmnReturnValue.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.tblclmnReturnValue.text")); //$NON-NLS-1$
		
		final TableColumn tblclmnDuration = new TableColumn(this.table,
				SWT.NONE);
		tblclmnDuration.setWidth(100);
		tblclmnDuration.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.tblclmnDuration.text")); //$NON-NLS-1$

		final TableColumn tblclmnTraceID = new TableColumn(this.table, SWT.NONE);
		tblclmnTraceID.setWidth(100);
		tblclmnTraceID.setText(DatabaseCallsView.BUNDLE.getString("DatabaseCallsView.tblclmnTraceID.text")); //$NON-NLS-1$
		
		final TableColumn tblclmnTimestamp = new TableColumn(this.table,
				SWT.NONE);
		tblclmnTimestamp.setWidth(150);
		tblclmnTimestamp.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.tblclmnTimestamp.text")); //$NON-NLS-1$

		this.ivSc = new ScrolledComposite(sashForm, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);

		this.detailComposite = new Composite(this.ivSc, SWT.NONE);
		this.detailComposite.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		this.detailComposite.setLayout(new GridLayout(2, false));

		this.ivSc.setContent(this.detailComposite);
		this.ivSc.setExpandHorizontal(true);
		this.ivSc.setExpandVertical(true);

		final Label lblOperation = new Label(this.detailComposite, SWT.NONE);
		lblOperation
				.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblOperation.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.lblOperation.text") + ":"); //$NON-NLS-1$

		this.lblOperationDisplay = new Text(this.detailComposite, SWT.READ_ONLY
				| SWT.NONE);
		this.lblOperationDisplay.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		this.lblOperationDisplay.setText(DatabaseCallsView.N_A);
		
		final Label lblStatement = new Label(this.detailComposite, SWT.NONE);
		lblStatement
				.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblStatement.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.lblStatement.text") + ":"); //$NON-NLS-1$

		this.lblStatementDisplay = new Text(this.detailComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY
				| SWT.NONE);
		this.lblStatementDisplay.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		this.lblStatementDisplay.setText(DatabaseCallsView.N_A);

		final Label lblReturnValue = new Label(this.detailComposite, SWT.NONE);
		lblReturnValue
				.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblReturnValue.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.lblReturnValue.text") + ":"); //$NON-NLS-1$

		this.lblReturnValueDisplay = new Text(this.detailComposite, SWT.READ_ONLY
				| SWT.NONE);
		this.lblReturnValueDisplay.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		this.lblReturnValueDisplay.setText(DatabaseCallsView.N_A);

		final Label lblDuration = new Label(this.detailComposite, SWT.NONE);
		lblDuration.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblDuration.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.lblDuration.text") + ":"); //$NON-NLS-1$

		this.lblMinimalDurationDisplay = new Text(this.detailComposite,
				SWT.READ_ONLY);
		this.lblMinimalDurationDisplay.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		this.lblMinimalDurationDisplay.setText(DatabaseCallsView.N_A);

		this.lblFailed = new Label(this.detailComposite, SWT.NONE);
		this.lblFailed.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		this.lblFailed.setText(DatabaseCallsView.BUNDLE
				.getString("DatabaseCallsView.lblFailed.text")); //$NON-NLS-1$

		this.lblFailedDisplay = new Text(this.detailComposite, SWT.READ_ONLY);
		this.lblFailedDisplay.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WHITE));
		this.lblFailedDisplay.setText(DatabaseCallsView.N_A);
		sashForm.setWeights(new int[] { 2, 1 });

		this.statusBar = new Composite(this.composite, SWT.NONE);
		this.statusBar.setLayout(new GridLayout(1, false));

		this.lbCounter = new Label(this.statusBar, SWT.NONE);
		this.lbCounter
				.setText("0 " + DatabaseCallsView.BUNDLE.getString("DatabaseCallsView.lbCounter.text")); //$NON-NLS-1$

		this.table.addListener(SWT.SetData, new DataProvider());
		this.table.addSelectionListener(this.controller);

		tblclmnOperation
				.addSelectionListener(new CallTableColumnSortListener<OperationCall>(
						OperationCall::getOperation));
		
		tblclmnStatement
		.addSelectionListener(new CallTableColumnSortListener<DatabaseOperationCall>(
				DatabaseOperationCall::getStringClassArgs));
		
		tblclmnReturnValue
		.addSelectionListener(new CallTableColumnSortListener<DatabaseOperationCall>(
				DatabaseOperationCall::getFormattedReturnValue));
		
		tblclmnTraceID.addSelectionListener(new CallTableColumnSortListener<DatabaseOperationCall>(DatabaseOperationCall::getTraceID));
		
		tblclmnDuration
				.addSelectionListener(new CallTableColumnSortListener<DatabaseOperationCall>(
						DatabaseOperationCall::getDuration));
		tblclmnTimestamp
				.addSelectionListener(new CallTableColumnSortListener<DatabaseOperationCall>(
						DatabaseOperationCall::getTimestamp));

		this.filterText.addTraverseListener(this.controller);

		this.btnShowAll.addSelectionListener(this.controller);
		this.btnShowJustFailed.addSelectionListener(this.controller);
	}

	@Override
	public void update(final Observable observable, final Object obj) {
		if (observable == this.dataModel) {
			this.updateCachedDataModelContent();
			this.updateTable();
			this.updateStatusBar();
		}
		if (observable == this.propertiesModel) {
			this.clearTable();
		}
	}

	public void notifyAboutChangedOperationCall() {
		this.updateDetailComposite();
	}

	public void notifyAboutChangedRegExpr() {
		this.updateCachedDataModelContent();
		this.updateTable();
		this.updateStatusBar();
		this.updateDetailComposite();
	}

	public void notifyAboutChangedFilter() {
		this.updateCachedDataModelContent();
		this.updateTable();
		this.updateStatusBar();
		this.updateDetailComposite();
	}

	private void updateCachedDataModelContent() {
		if (this.model.getFilter() == Filter.NONE) {
			this.cachedDataModelContent = this.dataModel
					.getDatabaseOperationCalls(this.model.getRegExpr());
		} else {
			// TODO Failed Operations
			// this.cachedDataModelContent =
			// this.dataModel.getFailedOperationCalls(this.model.getRegExpr());
			this.cachedDataModelContent = this.dataModel
					.getDatabaseOperationCalls(this.model.getRegExpr());
		}
	}

	/*
	 * Detailed Panel (bottom of the window)
	 */
	private void updateDetailComposite() {
		final DatabaseOperationCall call = this.model
				.getDatabaseOperationCall();

		if (call != null) {
			final String shortTimeUnit = NameConverter
					.toShortTimeUnit(this.propertiesModel.getTimeUnit());
			final long duration = this.propertiesModel.getTimeUnit().convert(
					call.getDuration(), this.dataModel.getTimeUnit());
			final String durationString = duration + " " + shortTimeUnit;

			this.lblMinimalDurationDisplay.setText(durationString);
			this.lblOperationDisplay.setText(call.getOperation());
			
			// customizes the SQL-Statement for visualization purposes
			final String statementText = call.getStringClassArgs().toUpperCase();			
			final String newStatementText = Utils.formatSQLStatement(statementText);		
			
			// TODO colors KEYWORDS (SELECT, FROM, WHERE) ?
			this.lblStatementDisplay.setText(newStatementText);
			
			this.lblReturnValueDisplay.setText(call.getFormattedReturnValue());

			if (call.isFailed()) {
				this.lblFailedDisplay.setText("Yes (" + call.getFailedCause()
						+ ")");
				this.lblFailedDisplay.setForeground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_RED));
				this.lblFailed.setForeground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_RED));
			} else {
				this.lblFailedDisplay.setText("No");
				this.lblFailedDisplay.setForeground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_BLACK));
				this.lblFailed.setForeground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_BLACK));
			}
		}

		this.detailComposite.layout();
		this.ivSc.setMinSize(this.detailComposite.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));
	}

	private void updateStatusBar() {
		this.lbCounter.setText(this.cachedDataModelContent.size()
				+ " "
				+ DatabaseCallsView.BUNDLE
						.getString("DatabaseCallsView.lbCounter.text"));
		this.statusBar.getParent().layout();
	}

	private void updateTable() {
		this.table.setData(this.cachedDataModelContent);
		this.table.setItemCount(this.cachedDataModelContent.size());

		this.clearTable();
	}

	@Override
	public Composite getComposite() {
		return this.composite;
	}

	private void clearTable() {
		this.table.clearAll();
	}

	public Text getFilterText() {
		return this.filterText;
	}

	public Button getBtnShowAll() {
		return this.btnShowAll;
	}

	public Button getBtnShowJustFailed() {
		return this.btnShowJustFailed;
	}

	public Widget getTable() {
		return this.table;
	}

	/**
	 * @author Nils Christian Ehmke
	 * @author Christian Zirkelbach
	 */
	private class DataProvider implements Listener {

		@Override
		@SuppressWarnings("unchecked")
		public void handleEvent(final Event event) {
			// Get the necessary information from the event
			final Table table = (Table) event.widget;
			final TableItem item = (TableItem) event.item;
			final int tableIndex = event.index;

			// Get the data for the current row
			final List<DatabaseOperationCall> calls = (List<DatabaseOperationCall>) table
					.getData();
			final DatabaseOperationCall call = calls.get(tableIndex);

			// Get the data to display
			String operationString = call.getOperation();
			if (DatabaseCallsView.this.propertiesModel.getOperationNames() == OperationNames.SHORT) {
				operationString = NameConverter
						.toShortOperationName(operationString);
			}
			
			String returnValue = call.getFormattedReturnValue();
			String statement = call.getStringClassArgs();

			final TimeUnit sourceTimeUnit = DatabaseCallsView.this.dataModel
					.getTimeUnit();
			final TimeUnit targetTimeUnit = DatabaseCallsView.this.propertiesModel
					.getTimeUnit();
			final String shortTimeUnit = NameConverter
					.toShortTimeUnit(targetTimeUnit);

			final String duration = targetTimeUnit.convert(call.getDuration(),
					sourceTimeUnit) + " " + shortTimeUnit;

			item.setText(new String[] {operationString, statement, returnValue, duration, Long.toString(call.getTraceID()), 
					Long.toString(call.getTimestamp()) });

			if (call.isFailed()) {
				final Color colorRed = Display.getCurrent().getSystemColor(
						SWT.COLOR_RED);
				item.setForeground(colorRed);
			}
			item.setData(call);
		}
	}
}