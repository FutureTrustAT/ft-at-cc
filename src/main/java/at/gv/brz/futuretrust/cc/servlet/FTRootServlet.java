package at.gv.brz.futuretrust.cc.servlet;

import javax.servlet.annotation.WebServlet;

import com.helger.commons.http.EHttpMethod;
import com.helger.photon.basic.xservlet.RootXServletHandler;
import com.helger.xservlet.AbstractXServlet;

@WebServlet (urlPatterns = "")
public final class FTRootServlet extends AbstractXServlet
{
  public FTRootServlet ()
  {
    handlerRegistry ().registerHandler (EHttpMethod.GET, new RootXServletHandler ("/public"));
    handlerRegistry ().copyHandlerToAll (EHttpMethod.GET);
  }
}
