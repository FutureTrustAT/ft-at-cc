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

import java.util.EnumSet;

import javax.servlet.annotation.WebServlet;

import com.helger.commons.http.EHttpMethod;
import com.helger.photon.basic.app.appid.CApplicationID;
import com.helger.photon.basic.app.appid.XServletFilterAppIDExplicit;
import com.helger.photon.core.app.html.IHTMLProvider;
import com.helger.photon.core.servlet.AbstractApplicationXServletHandler;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.AbstractXServlet;

/**
 * The main action servlet.
 * 
 * @author Philip Helger
 */
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
    handlerRegistry ().copyHandler (EHttpMethod.GET, EnumSet.of (EHttpMethod.POST));
    filterHighLevelList ().add (new XServletFilterAppIDExplicit (CApplicationID.APP_ID_PUBLIC));
  }
}
