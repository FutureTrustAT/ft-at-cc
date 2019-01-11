package at.gv.brz.futuretrust.cc.servlet;

import javax.servlet.annotation.WebServlet;

import com.helger.commons.http.EHttpMethod;
import com.helger.photon.basic.app.appid.CApplicationID;
import com.helger.photon.basic.app.appid.XServletFilterAppIDExplicit;
import com.helger.photon.core.app.html.IHTMLProvider;
import com.helger.photon.core.servlet.AbstractApplicationXServletHandler;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.AbstractXServlet;

@WebServlet (urlPatterns = "/public")
public final class FTPublicServlet extends AbstractXServlet
{
  public FTPublicServlet ()
  {
    handlerRegistry ().registerHandler (EHttpMethod.GET, new AbstractApplicationXServletHandler ()
    {
      @Override
      protected IHTMLProvider createHTMLProvider (final IRequestWebScopeWithoutResponse aRequestScope)
      {
        return new PublicHTMLProvider ();
      }
    });
    filterHighLevelList ().add (new XServletFilterAppIDExplicit (CApplicationID.APP_ID_PUBLIC));
  }
}
