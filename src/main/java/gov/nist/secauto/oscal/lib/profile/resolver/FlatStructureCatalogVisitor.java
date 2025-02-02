/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.oscal.lib.profile.resolver;

import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.oscal.lib.model.Catalog;
import gov.nist.secauto.oscal.lib.model.CatalogGroup;
import gov.nist.secauto.oscal.lib.model.Control;
import gov.nist.secauto.oscal.lib.model.Parameter;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class FlatStructureCatalogVisitor {

  public void visitCatalog(@NotNull Catalog catalog) {
    // process children
    for (Iterator<CatalogGroup> iter = CollectionUtil.listOrEmpty(catalog.getGroups()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      CatalogGroup child = iter.next();
      ControlResult result = visitGroup(child);

      // remove this group and promote its contents
      iter.remove();
      result.apply(catalog);
    }

    for (Iterator<Control> iter = CollectionUtil.listOrEmpty(catalog.getControls()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      Control child = iter.next();
      ControlResult result = visitControl(child);

      // apply result to current context
      result.apply(catalog);
    }
  }

  public ControlResult visitGroup(@NotNull CatalogGroup group) {
    // process children
    ControlResult retval = new ControlResult();

    // groups
    for (Iterator<CatalogGroup> iter = CollectionUtil.listOrEmpty(group.getGroups()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      CatalogGroup child = iter.next();
      ControlResult result = visitGroup(child);
      retval.append(result);
    }

    for (Iterator<Parameter> iter = CollectionUtil.listOrEmpty(group.getParams()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      Parameter child = iter.next();
      retval.promoteParameter(child);
    }

    for (Iterator<Control> iter = CollectionUtil.listOrEmpty(group.getControls()).iterator(); iter.hasNext();) {
      @SuppressWarnings("null")
      @NotNull
      Control child = iter.next();
      retval.promoteControl(child);
      ControlResult result = visitControl(child);
      retval.append(result);
    }

    return retval;
  }

  public ControlResult visitControl(@NotNull Control control) {
    ControlResult result = new ControlResult();

    List<Control> controlChildren = CollectionUtil.listOrEmpty(control.getControls());
    if (!controlChildren.isEmpty()) {

      for (Iterator<Control> iter = CollectionUtil.listOrEmpty(control.getControls()).iterator(); iter.hasNext();) {
        @SuppressWarnings("null")
        @NotNull
        Control child = iter.next();
        ControlResult childResult = visitControl(child);

        // promote and remove this control as a child
        result.promoteControl(child);
        result.append(childResult);
        iter.remove();
      }
    }

    return result;
  }
}
