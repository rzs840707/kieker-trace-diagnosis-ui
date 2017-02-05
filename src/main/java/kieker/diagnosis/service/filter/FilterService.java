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

package kieker.diagnosis.service.filter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import jfxtras.scene.control.CalendarTimeTextField;
import kieker.diagnosis.service.InjectService;
import kieker.diagnosis.service.ServiceIfc;
import kieker.diagnosis.service.data.domain.AbstractOperationCall;
import kieker.diagnosis.service.properties.PropertiesService;

public final class FilterService implements ServiceIfc {

	@InjectService
	private PropertiesService ivPropertiesService;

	public <T> Predicate<T> useFilter( final TextField aFilter, final Function<T, String> aFunction ) {
		final String text = aFilter.getText( );

		if ( (text == null) || text.isEmpty( ) ) {
			return (x -> true);
		} else {
			final boolean regularExpressionsActive = ivPropertiesService.isRegularExpressionsActive( );
			if ( regularExpressionsActive ) {
				checkRegularExpression( text );
				return (x -> aFunction.apply( x ).matches( text ));
			} else {
				final boolean caseSensitivityActive = ivPropertiesService.isCaseSensitivityActive( );
				if ( caseSensitivityActive ) {
					return (x -> aFunction.apply( x ).contains( text ));
				} else {
					return (x -> aFunction.apply( x ).toLowerCase( ).contains( text.toLowerCase( ) ));
				}
			}
		}
	}

	public <T extends AbstractOperationCall<T>> Predicate<T> useFilter( final TextField aFilter, final Function<T, String> aFunction,
			final boolean aSearchInChildren ) {
		final Predicate<T> filter = useFilter( aFilter, aFunction );

		if ( aSearchInChildren ) {
			return x -> {
				return checkRecursive( x, filter );
			};
		} else {
			return filter;
		}
	}

	private <T extends AbstractOperationCall<T>> boolean checkRecursive( final T aElement, final Predicate<T> aPredicate ) {
		if ( aPredicate.test( aElement ) ) {
			return true;
		} else {
			for ( final T child : aElement.getChildren( ) ) {
				if ( checkRecursive( child, aPredicate ) ) {
					return true;
				}
			}
		}

		return false;
	}

	public <T> Predicate<T> alwaysTrue( ) {
		return (x -> true);
	}

	private void checkRegularExpression( final String aText ) {
		Pattern.compile( aText );
	}

	public <T> Predicate<T> useFilter( final DatePicker aDatePicker, final Function<T, Long> aFunction, final boolean aFilterBefore ) {
		final LocalDate value = aDatePicker.getValue( );
		if ( value == null ) {
			return x -> true;
		}
		final Predicate<T> result = x -> {
			final long timestamp = aFunction.apply( x );
			final long timestampInMS = TimeUnit.MILLISECONDS.convert( timestamp, ivPropertiesService.getTimeUnit( ) );
			final Instant instant = Instant.ofEpochMilli( timestampInMS );
			final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( instant, ZoneId.systemDefault( ) );
			final LocalDate localDate = LocalDate.from( zonedDateTime );
			if ( aFilterBefore ) {
				return value.isBefore( localDate ) || value.isEqual( localDate );
			} else {
				return value.isAfter( localDate ) || value.isEqual( localDate );
			}
		};

		return result;
	}

	public <T extends AbstractOperationCall<T>> Predicate<T> useFilter( final DatePicker aDatePicker, final Function<T, Long> aFunction,
			final boolean aFilterBefore, final boolean aSearchInChildren ) {
		final Predicate<T> filter = useFilter( aDatePicker, aFunction, aFilterBefore );

		if ( aSearchInChildren ) {
			return x -> {
				return checkRecursive( x, filter );
			};
		} else {
			return filter;
		}
	}

	public <T extends AbstractOperationCall<T>> Predicate<T> useFilter( final CalendarTimeTextField aTimeTextField, final Function<T, Long> aFunction,
			final boolean aFilterBefore, final boolean aSearchInChildren ) {
		final Predicate<T> filter = useFilter( aTimeTextField, aFunction, aFilterBefore );

		if ( aSearchInChildren ) {
			return x -> {
				return checkRecursive( x, filter );
			};
		} else {
			return filter;
		}
	}

	public <T> Predicate<T> useFilter( final CalendarTimeTextField aTimeTextField, final Function<T, Long> aFunction, final boolean aFilterBefore ) {
		final Calendar value = aTimeTextField.getCalendar( );
		if ( value == null ) {
			return x -> true;
		}
		final Predicate<T> result = x -> {
			final long timestamp = aFunction.apply( x );
			final long timestampInMS = TimeUnit.MILLISECONDS.convert( timestamp, ivPropertiesService.getTimeUnit( ) );
			final Instant instant = Instant.ofEpochMilli( timestampInMS );
			final ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant( instant, ZoneId.systemDefault( ) );

			final int hour1 = zonedDateTime.get( ChronoField.HOUR_OF_DAY );
			final int minute1 = zonedDateTime.get( ChronoField.MINUTE_OF_HOUR );

			final int hour2 = value.get( Calendar.HOUR_OF_DAY );
			final int minute2 = value.get( Calendar.MINUTE );

			if ( aFilterBefore ) {
				if ( hour2 < hour1 ) {
					return true;
				}
				if ( hour2 > hour1 ) {
					return false;
				}
				if ( minute2 < minute1 ) {
					return true;
				}
				if ( minute2 > minute1 ) {
					return false;
				}
				return true;
			} else {
				if ( hour2 > hour1 ) {
					return true;
				}
				if ( hour2 < hour1 ) {
					return false;
				}
				if ( minute2 > minute1 ) {
					return true;
				}
				if ( minute2 < minute1 ) {
					return false;
				}
				return true;
			}
		};

		return result;
	}

	public <T> Predicate<T> useFilter( final RadioButton aShowAllButton, final RadioButton aShowJustSuccessfulButton, final RadioButton aShowJustFailedButton,
			final Function<T, Boolean> aIsFailedFunction ) {
		if ( aShowAllButton.isSelected( ) ) {
			return (x -> true);
		}
		Predicate<T> predicate = (x -> aIsFailedFunction.apply( x ));
		if ( aShowJustSuccessfulButton.isSelected( ) ) {
			predicate = predicate.negate( );
		}
		return predicate;
	}

	public static <T> Predicate<T> useFilter( final RadioButton aShowAllButton, final RadioButton aShowJustSuccessfulButton,
			final RadioButton aShowJustFailedButton, final RadioButton aShowJustFailureContainingButton, final Function<T, Boolean> aIsFailedFunction,
			final Function<T, Boolean> aContainsFailureFunction ) {
		if ( aShowAllButton.isSelected( ) ) {
			return (x -> true);
		}
		if ( aShowJustFailureContainingButton.isSelected( ) ) {
			return (x -> aContainsFailureFunction.apply( x ));
		}
		Predicate<T> predicate = (x -> aIsFailedFunction.apply( x ));
		if ( aShowJustSuccessfulButton.isSelected( ) ) {
			predicate = predicate.negate( );
		}
		return predicate;
	}

}