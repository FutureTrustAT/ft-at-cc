/**
 * Copyright (C) 2015-2018 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.gv.brz.futuretrust.cc.servlet;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.helger.commons.string.StringHelper;
import com.helger.css.property.ECSSProperty;
import com.helger.html.hc.html.embedded.HCImg;
import com.helger.html.hc.html.metadata.HCHead;
import com.helger.html.hc.html.metadata.HCStyle;
import com.helger.html.hc.html.root.HCHtml;
import com.helger.html.hc.html.sections.HCBody;
import com.helger.html.hc.html.sections.HCFooter;
import com.helger.html.hc.html.sections.HCH1;
import com.helger.html.hc.html.textlevel.HCSpan;
import com.helger.html.hc.impl.HCEntityNode;
import com.helger.photon.basic.app.appid.RequestSettings;
import com.helger.photon.basic.app.menu.IMenuItemPage;
import com.helger.photon.core.app.context.ISimpleWebExecutionContext;
import com.helger.photon.core.app.context.LayoutExecutionContext;
import com.helger.photon.core.app.html.AbstractSWECHTMLProvider;
import com.helger.photon.core.url.LinkHelper;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.forcedredirect.ForcedRedirectException;

/**
 * Main class for creating HTML output
 *
 * @author Philip Helger
 */
public class PublicHTMLProvider extends AbstractSWECHTMLProvider
{
  @Override
  protected void fillBody (@Nonnull final ISimpleWebExecutionContext aSWEC,
                           @Nonnull final HCHtml aHtml) throws ForcedRedirectException
  {
    final IRequestWebScopeWithoutResponse aRequestScope = aSWEC.getRequestScope ();
    final Locale aDisplayLocale = aSWEC.getDisplayLocale ();
    final IMenuItemPage aMenuItem = RequestSettings.getMenuItem (aRequestScope);
    final LayoutExecutionContext aLEC = new LayoutExecutionContext (aSWEC, aMenuItem);
    final HCHead aHead = aHtml.head ();
    final HCBody aBody = aHtml.body ();

    // Add menu item in page title
    aHead.setPageTitle (StringHelper.getConcatenatedOnDemand ("BRZ FutureTrust pilot client",
                                                              " - ",
                                                              aMenuItem.getDisplayText (aDisplayLocale)));

    // TODO
    aHead.addCSS (new HCStyle ("* { font-family: Arial, Helvetica, sans-serif; }" +
                               "footer { background-color: #ddd; padding: 1rem; }"));
    aBody.addChild (new HCH1 ().addChild (new HCImg ().setSrc (LinkHelper.getURLWithContext ("/imgs/FutureTrust-Logo-1.png"))
                                                      .addStyle (ECSSProperty.WIDTH, "200px"))
                               .addChild (new HCSpan ().addChild ("BRZ pilot client")
                                                       .addStyle (ECSSProperty.MARGIN_LEFT, "2rem")));

    aBody.addChild (new HCFooter ().addChild (HCEntityNode.newCopy ()).addChild (" 2019 BRZ GmbH"));
  }
}
