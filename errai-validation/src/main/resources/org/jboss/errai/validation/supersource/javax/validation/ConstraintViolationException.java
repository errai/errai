/*
 * Copyright (C) 2024 Red Hat, Inc. and/or its affiliates.
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

/**
 * GWT supersource stub bridging the old javax.validation.ConstraintViolationException
 * to the new jakarta.validation.ConstraintViolationException.
 *
 * This ensures that GWT's RPC custom field serializer naming convention works correctly:
 * javax.validation.ConstraintViolationException_CustomFieldSerializer (from
 * gwt-bean-validators-engine-gwt-user) can satisfy the serializer requirement for
 * this type because both javax and jakarta ConstraintViolationException are the
 * same type from GWT's perspective via this supersource bridge.
 */
package javax.validation;

import java.util.Set;

public class ConstraintViolationException extends jakarta.validation.ConstraintViolationException {

  public ConstraintViolationException(final String message,
      final Set<? extends jakarta.validation.ConstraintViolation<?>> constraintViolations) {
    super(message, constraintViolations);
  }

  public ConstraintViolationException(
      final Set<? extends jakarta.validation.ConstraintViolation<?>> constraintViolations) {
    super(constraintViolations);
  }
}
