/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
//renaming to kk, j
package test31;
class A{
    private void m(final int kk, int j){
        new Object(){
            int kk;
            void fred(){
                kk= 0;
            }
        };
    }
}
