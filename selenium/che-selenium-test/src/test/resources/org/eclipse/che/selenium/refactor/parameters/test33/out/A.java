/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
//renaming to b, no ref update
package test33;
class A{
    A A;
    A m(A b){
      A:
         for (;;){
           if (A.m(A)==A)
               break A;
        }
      return A;
   };
}
