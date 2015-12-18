/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.test.not.on.gwt.path;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * This entity is not on the GWT source path, and should not appear in Errai's Entity Manager.
 * <p>
 * Part of the regression test for ERRAI-675.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Entity
public class NonClientEntity {

    @Id private long id;

}
