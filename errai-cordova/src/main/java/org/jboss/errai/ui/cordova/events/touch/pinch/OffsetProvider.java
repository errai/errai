/*
 * Copyright 2012 Daniel Kurka
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.errai.ui.cordova.events.touch.pinch;

/**
 * {@link PinchRecognizer} needs to know about display settings to calculate a
 * pinch properly, but it should not know about widgets. THis is an abstraction
 * interface to encapsulate that knowledge
 * 
 * @author Daniel
 * 
 */
public interface OffsetProvider {
	/**
	 * the upper left corner of the widget
	 * 
	 * @return the upper left corner in px
	 */
	public int getLeft();

	/**
	 * the upper left corner of the widget
	 * 
	 * @return the upper left corner in px
	 */
	public int getTop();
}
