package at.gv.brz.futuretrust.cc.servlet;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.helger.commons.debug.GlobalDebug;
import com.helger.html.resource.css.ConstantCSSPathProvider;
import com.helger.httpclient.HttpDebugger;
import com.helger.photon.basic.app.appid.CApplicationID;
import com.helger.photon.basic.app.appid.PhotonGlobalState;
import com.helger.photon.basic.app.locale.ILocaleManager;
import com.helger.photon.basic.app.menu.MenuTree;
import com.helger.photon.basic.app.page.AbstractPage;
import com.helger.photon.core.app.html.PhotonCSS;
import com.helger.photon.core.servlet.WebAppListener;
import com.helger.xservlet.requesttrack.RequestTracker;

import at.gv.brz.futuretrust.cc.FTConfiguration;

@WebListener
public final class FTWebAppListener extends WebAppListener
{
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
