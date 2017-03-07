/***************************************************************************
 * Copyright 2015-2016 Kieker Project (http://kieker-monitoring.net)
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

package kieker.diagnosis.gui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import kieker.diagnosis.common.TechnicalException;
import kieker.diagnosis.service.InjectService;
import kieker.diagnosis.service.ServiceIfc;
import kieker.diagnosis.service.ServiceUtil;
import kieker.diagnosis.service.properties.LogoProperty;
import kieker.diagnosis.service.properties.PropertiesService;
import kieker.diagnosis.service.properties.SplashscreenProperty;
import kieker.diagnosis.service.properties.TitleProperty;

/**
 * @author Nils Christian Ehmke
 */
public final class GUIUtil {

	private static final Map<Class<?>, LoadedView> cvLoadedViewCache = new HashMap<>( );
	private static AbstractController<?> cvMainController;

	private GUIUtil( ) {
	}

	public static void clearCache( ) {
		cvLoadedViewCache.clear( );
	}

	public static <V extends AbstractView, C extends AbstractController<V>> void loadView( final Class<C> aControllerClass, final Stage aRootStage,
			final boolean aCacheViews, final Class<? extends AbstractController<?>> aMainControllerClass ) throws TechnicalException {
		LoadedView loadedView = getLoadedViewFromCache( aControllerClass, aCacheViews, new ContextEntry[0] );

		if ( loadedView == null ) {
			try {
				loadedView = createLoadedView( aControllerClass, aMainControllerClass, aCacheViews, new ContextEntry[0] );
			} catch ( final ReflectiveOperationException | IOException ex ) {
				throw new TechnicalException( ex );
			}
		}

		applyLoadedView( loadedView, aRootStage );
	}

	public static <V extends AbstractView, C extends AbstractController<V>> void loadView( final Class<C> aControllerClass, final AnchorPane aRootStage,
			final Class<? extends AbstractController<?>> aMainControllerClass, final boolean aCacheViews, final ContextEntry[] aArguments )
			throws TechnicalException {
		LoadedView loadedView = getLoadedViewFromCache( aControllerClass, aCacheViews, aArguments );

		if ( loadedView == null ) {
			try {
				loadedView = createLoadedView( aControllerClass, aMainControllerClass, aCacheViews, aArguments );
			} catch ( final ReflectiveOperationException | IOException ex ) {
				throw new TechnicalException( ex );
			}
		}

		applyLoadedView( loadedView, aRootStage );
	}

	public static <V extends AbstractView, C extends AbstractController<V>> void loadDialog( final Class<C> aControllerClass,
			final Class<? extends AbstractController<?>> aMainControllerClass, final boolean aCacheViews, final Window aOwner ) throws TechnicalException {
		LoadedView loadedView = getLoadedViewFromCache( aControllerClass, aCacheViews, new ContextEntry[0] );

		if ( loadedView == null ) {
			try {
				loadedView = createLoadedView( aControllerClass, aMainControllerClass, aCacheViews, new ContextEntry[0] );
			} catch ( final ReflectiveOperationException | IOException ex ) {
				throw new TechnicalException( ex );
			}
		}

		// We have to reuse the scene if necessary. Otherwise we get problems when we used the cache views.
		final Parent parent = (Parent) loadedView.getNode( );
		Scene scene = parent.getScene( );
		if ( scene == null ) {
			scene = new Scene( parent );
		}

		scene.getStylesheets( ).add( loadedView.getStylesheetURL( ) );

		final PropertiesService propertiesService = ServiceUtil.getService( PropertiesService.class );
		final String logo = propertiesService.loadSystemProperty( LogoProperty.class );

		final Stage dialogStage = new Stage( );
		dialogStage.getIcons( ).add( new Image( logo ) );
		dialogStage.setTitle( loadedView.getTitle( ) );
		dialogStage.setResizable( false );
		dialogStage.initModality( Modality.WINDOW_MODAL );
		dialogStage.initOwner( aOwner );
		dialogStage.setScene( scene );
		dialogStage.showAndWait( );
	}

	private static LoadedView getLoadedViewFromCache( final Class<?> aControllerClass, final boolean aCacheViews, final ContextEntry[] aArguments ) {
		// If we should not cache the views, we do not access the cache
		if ( !aCacheViews ) {
			return null;
		}

		// If there are arguments for the controller, we have to create a new controller
		if ( ( aArguments != null ) && ( aArguments.length > 0 ) ) {
			return null;
		}

		// Otherwise we can check whether the cache contains the view already
		return cvLoadedViewCache.get( aControllerClass );
	}

	private static <V extends AbstractView, C extends AbstractController<V>> LoadedView createLoadedView( final Class<C> aControllerClass,
			final Class<? extends AbstractController<?>> aMainControllerClass, final boolean aCacheViews, final ContextEntry[] aArguments )
			throws ReflectiveOperationException, IOException {
		final ClassLoader classLoader = GUIUtil.class.getClassLoader( );

		final String baseName = aControllerClass.getCanonicalName( ).replace( "Controller", "" );

		// Get the FXML file
		final String viewFXMLName = baseName.replace( ".", "/" ) + ".fxml";
		final URL viewResource = classLoader.getResource( viewFXMLName );

		// Get the resource bundle
		final String bundleBaseName = baseName.toLowerCase( Locale.ROOT );
		final ResourceBundle resourceBundle = ResourceBundle.getBundle( bundleBaseName, Locale.getDefault( ) );

		// Create the controller
		final Constructor<C> controllerConstructor = aControllerClass.getConstructor( Context.class );
		final Context context = new Context( aArguments );
		final C controller = controllerConstructor.newInstance( context );

		// Create the controller proxy
		final String controllerIfcName = aControllerClass.getCanonicalName( ) + "Ifc";
		final Class<?> controllerIfc = classLoader.loadClass( controllerIfcName );
		final Object contrProxy = Proxy.newProxyInstance( classLoader, new Class<?>[] { controllerIfc }, new ErrorHandlingInvocationHandler( controller ) );

		// Load the FXML file
		final FXMLLoader loader = new FXMLLoader( );
		loader.setController( contrProxy );
		loader.setLocation( viewResource );
		loader.setResources( resourceBundle );
		final Node node = (Node) loader.load( );

		// Create the view
		final String viewName = aControllerClass.getCanonicalName( ).replace( "Controller", "View" );
		@SuppressWarnings ( "unchecked" )
		final Class<V> viewClass = (Class<V>) classLoader.loadClass( viewName );
		final V view = viewClass.newInstance( );
		view.setResourceBundle( resourceBundle );
		controller.setView( view );

		node.applyCss( );
		injectViewFields( node, viewClass, view );
		injectControllerFields( aControllerClass, aMainControllerClass, controller );

		// Get the CSS file name
		final String cssName = baseName.replace( ".", "/" ) + ".css";
		final URL cssResource = classLoader.getResource( cssName );

		// Now that everything should be set, the controller can be initialized
		controller.doInitialize( );

		final String title = ( resourceBundle.containsKey( "title" ) ? resourceBundle.getString( "title" ) : "" );
		final LoadedView loadedView = new LoadedView( node, title, cssResource.toExternalForm( ) );

		if ( aCacheViews ) {
			cvLoadedViewCache.put( aControllerClass, loadedView );
		}

		if ( aControllerClass == aMainControllerClass ) {
			cvMainController = controller;
		}

		return loadedView;
	}

	private static <T> void injectViewFields( final Node aNode, final Class<T> aViewClass, final T aView ) throws IllegalAccessException {
		final Field[] declaredViewFields = aViewClass.getDeclaredFields( );

		for ( final Field field : declaredViewFields ) {
			// Inject only JavaFX based fields
			if ( field.isAnnotationPresent( InjectComponent.class ) && Node.class.isAssignableFrom( field.getType( ) ) ) {
				field.setAccessible( true );

				final String fieldName = "#" + field.getName( );
				Object fieldValue = aNode.lookup( fieldName );
				if ( fieldValue == null ) {
					// In some cases (TitledPane in dialogs which are not visible yet), the lookup does not work.
					// We correct this for the cases we know of.
					fieldValue = lookup( aNode, fieldName );
				}

				field.set( aView, fieldValue );
			}
		}
	}

	private static <T> void injectControllerFields( final Class<T> aControllerClass, final Class<?> aMainControllerClass, final T aController )
			throws IllegalAccessException {
		final Field[] declaredControllerFields = aControllerClass.getDeclaredFields( );
		for ( final Field field : declaredControllerFields ) {
			// Inject services
			final Class<?> fieldType = field.getType( );

			if ( field.isAnnotationPresent( InjectService.class ) ) {
				field.setAccessible( true );

				if ( !ServiceIfc.class.isAssignableFrom( fieldType ) ) {
					throw new TechnicalException( "Type '" + fieldType + "' is not a service class." );
				}

				@SuppressWarnings ( "unchecked" )
				final Object service = ServiceUtil.getService( (Class<? extends ServiceIfc>) fieldType );
				field.set( aController, service );
			}

			// Inject the main controller
			if ( aMainControllerClass.isAssignableFrom( fieldType ) ) {
				field.setAccessible( true );

				field.set( aController, cvMainController );
			}
		}
	}

	private static Object lookup( final Node aNode, final String aFieldName ) {
		Object element = aNode.lookup( aFieldName );

		// Did we already found the element?
		if ( element != null ) {
			return element;
		}

		if ( aNode instanceof Pane ) {
			final Pane pane = (Pane) aNode;

			for ( final Node child : pane.getChildren( ) ) {
				element = lookup( child, aFieldName );
				if ( element != null ) {
					break;
				}
			}
		} else if ( aNode instanceof TitledPane ) {
			final TitledPane titledPane = (TitledPane) aNode;
			final Node content = titledPane.getContent( );

			if ( content != null ) {
				element = lookup( content, aFieldName );
			}
		}

		return element;
	}

	private static void applyLoadedView( final LoadedView aLoadedView, final Stage aRootStage ) {
		final Scene scene = new Scene( (Parent) aLoadedView.getNode( ) );
		aRootStage.setScene( scene );

		final PropertiesService propertiesService = ServiceUtil.getService( PropertiesService.class );
		final String logo = propertiesService.loadSystemProperty( LogoProperty.class );
		final String title = propertiesService.loadSystemProperty( TitleProperty.class );

		aRootStage.getIcons( ).add( new Image( logo ) );
		aRootStage.setTitle( title );
		aRootStage.setMaximized( true );

		showSplashScreen( scene );

		aRootStage.show( );
	}

	private static void showSplashScreen( final Scene aRoot ) {
		final PropertiesService propertiesService = ServiceUtil.getService( PropertiesService.class );
		final String splashscreen = propertiesService.loadSystemProperty( SplashscreenProperty.class );

		final ImageView imageView = new ImageView( splashscreen );
		final Pane parent = new Pane( imageView );
		final Scene scene = new Scene( parent );

		final Stage stage = new Stage( );
		stage.setResizable( false );
		stage.initStyle( StageStyle.UNDECORATED );
		stage.initModality( Modality.WINDOW_MODAL );
		stage.initOwner( aRoot.getWindow( ) );
		stage.setScene( scene );

		final FadeTransition transition = new FadeTransition( Duration.millis( 3000 ), stage.getScene( ).getRoot( ) );
		transition.setFromValue( 1.0 );
		transition.setToValue( 0.0 );
		final EventHandler<ActionEvent> handler = t -> stage.hide( );
		transition.setOnFinished( handler );
		transition.play( );

		stage.showAndWait( );
	}

	private static void applyLoadedView( final LoadedView aLoadedView, final AnchorPane aRootPane ) {
		final Node node = aLoadedView.getNode( );

		// Add the node as children of the root pane
		aRootPane.getChildren( ).clear( );
		aRootPane.getChildren( ).add( node );

		// Add the corresponding stylesheets of the node
		aRootPane.getStylesheets( ).clear( );
		aRootPane.getStylesheets( ).add( aLoadedView.getStylesheetURL( ) );

		// Make sure that the node is display in full view in the root pane
		AnchorPane.setLeftAnchor( node, 0.0 );
		AnchorPane.setBottomAnchor( node, 0.0 );
		AnchorPane.setRightAnchor( node, 0.0 );
		AnchorPane.setTopAnchor( node, 0.0 );
	}
}