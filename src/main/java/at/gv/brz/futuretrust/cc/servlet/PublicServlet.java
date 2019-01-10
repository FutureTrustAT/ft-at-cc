package at.gv.brz.futuretrust.cc.servlet;

import javax.servlet.annotation.WebServlet;

import com.helger.photon.core.app.html.IHTMLProvider;
import com.helger.photon.core.servlet.AbstractApplicationXServletHandler;
import com.helger.photon.core.servlet.AbstractPublicApplicationServlet;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

@WebServlet (urlPatterns = "/public")
public final class PublicServlet extends AbstractPublicApplicationServlet
{
  public PublicServlet ()
  {
    super (new AbstractApplicationXServletHandler ()
    {
      @Override
      protected IHTMLProvider createHTMLProvider (final IRequestWebScopeWithoutResponse aRequestScope)
      {
        return new PublicHTMLProvider ();
      }
    });
  }
}
