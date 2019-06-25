/**
 * Copyright (C) 2018-2019 BRZ
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
package at.gv.brz.futuretrust.brzclient;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.system.SystemProperties;
import com.helger.html.resource.css.ConstantCSSPathProvider;
import com.helger.httpclient.HttpDebugger;
import com.helger.photon.app.html.PhotonCSS;
import com.helger.photon.bootstrap4.servlet.WebAppListenerBootstrap;
import com.helger.photon.core.appid.CApplicationID;
import com.helger.photon.core.appid.PhotonGlobalState;
import com.helger.photon.core.locale.ILocaleManager;
import com.helger.photon.core.menu.MenuTree;
import com.helger.photon.core.page.AbstractPage;
import com.helger.xservlet.requesttrack.RequestTracker;

public final class FTWebAppListener extends WebAppListenerBootstrap
{
  public static final String SYS_PROP_SERVLET_CONTEXT_BASE = "servletContextBase";

  @Override
  protected String getInitParameterDebug (@Nonnull final ServletContext aSC)
  {
    return FTConfiguration.getGlobalDebug ();
  }

  @Override
  protected String getInitParameterProduction (@Nonnull final ServletContext aSC)
  {
    return FTConfiguration.getGlobalProduction ();
  }

  @Override
  @Nonnull
  protected String getServletContextPath (@Nonnull final ServletContext aSC) throws IllegalStateException
  {
    // Set in MainStart
    final String ret = SystemProperties.getPropertyValueOrNull (SYS_PROP_SERVLET_CONTEXT_BASE);
    if (ret != null)
      return ret;

    return super.getServletContextPath (aSC);
  }

  @Override
  protected String getDataPath (@Nonnull final ServletContext aSC)
  {
    return FTConfiguration.getDataPath ();
  }

  @Override
  protected boolean shouldCheckFileAccess (@Nonnull final ServletContext aSC)
  {
    return false;
  }

  @Override
  protected void initGlobalSettings ()
  {
    // Logging: JUL to SLF4J
    SLF4JBridgeHandler.removeHandlersForRootLogger ();
    SLF4JBridgeHandler.install ();

    if (GlobalDebug.isDebugMode ())
      RequestTracker.getInstance ().getRequestTrackingMgr ().setLongRunningCheckEnabled (false);

    HttpDebugger.setEnabled (false);
  }

  @Override
  protected void initLocales (@Nonnull final ILocaleManager aLocaleMgr)
  {
    // One locale is required
    aLocaleMgr.registerLocale (Locale.US);
    aLocaleMgr.setDefaultLocale (Locale.US);
  }

  @Override
  protected void initMenu ()
  {
    // One dummy menu is required
    final MenuTree aMenuTree = new MenuTree ();
    aMenuTree.createRootItem ("main", new AbstractPage ("bla")
    {});
    PhotonGlobalState.state (CApplicationID.APP_ID_PUBLIC).setMenuTree (aMenuTree);
  }

  @Override
  protected void initUI ()
  {
    PhotonCSS.registerCSSIncludeForGlobal (ConstantCSSPathProvider.create ("/css/default.css"));
  }

  @Override
  protected void initManagers ()
  {
    // Ensure initialization
    FTConfiguration.getPrivateKey ();
  }
}
