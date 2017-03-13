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

package kieker.diagnosis.guitestarchitecture.components;

import javafx.scene.Node;
import javafx.scene.control.TextInputControl;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;

/**
 * @author Nils Christian Ehmke
 */
@Component
@Scope ( ConfigurableListableBeanFactory.SCOPE_PROTOTYPE )
public final class Link extends AbstractGuiComponent {

	public Link( final Predicate<Node> aMatcher ) {
		super( aMatcher );
	}

	public Link( final String aId ) {
		super( aId );
	}

	public String getText( ) {
		return ( (TextInputControl) getNode( ) ).getText( );
	}

}