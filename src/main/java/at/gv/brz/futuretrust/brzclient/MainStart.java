/**
 * Copyright (C) 2014-2019 Philip Helger (www.helger.com)
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

import java.net.URL;

import com.helger.cli.CmdLineParser;
import com.helger.cli.HelpFormatter;
import com.helger.cli.Option;
import com.helger.cli.Options;
import com.helger.cli.ParsedCmdLine;
import com.helger.commons.io.resourceresolver.DefaultResourceResolver;
import com.helger.commons.lang.ClassLoaderHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.system.SystemProperties;
import com.helger.photon.jetty.JettyStarter;

/**
 * Main application to start a standalone server.
 *
 * @author Philip Helger
 */
public final class MainStart extends AbstractMain
{
  public static void main (final String [] args) throws Exception
  {
    final Options aOptions = new Options ().addOption (Option.builder ("p")
                                                             .longOpt ("port")
                                                             .desc ("Port to use. Defaults to " + DEFAULT_PORT)
                                                             .args (1))
                                           .addOption (Option.builder ("sp")
                                                             .longOpt ("stop-port")
                                                             .desc ("Port to listen for stop signal. Defaults to " +
                                                                    DEFAULT_STOP_PORT)
                                                             .args (1))
                                           .addOption (Option.builder ("?").longOpt ("help").desc ("Show this help"));

    final ParsedCmdLine aCL = new CmdLineParser (aOptions).parseOrNull (args);

    final boolean bCanStart = aCL != null && !aCL.hasOption ("?");
    if (bCanStart)
    {
      final int nPort = aCL.getAsInt ("p", DEFAULT_PORT);
      final int nStopPort = aCL.getAsInt ("sp", DEFAULT_STOP_PORT);

      System.out.println ("Start server on port " + nPort + " and waiting for shutdown signal at " + nStopPort);

      final URL aWebXmlURL = ClassLoaderHelper.getResource (ClassLoaderHelper.getContextClassLoader (),
                                                            "WEB-INF/web.xml");
      if (aWebXmlURL == null)
        throw new IllegalStateException ("web.xml could not be found!");
      final String sWebXmlURL = aWebXmlURL.toExternalForm ();

      // Base URL - Eclipse vs. JAR:
      // file:/D:/git/ft-at-cc/target/classes/WEB-INF/web.xml
      // jar:file:/D:/git/ft-at-cc/target/ft-at-cc-0.1.0-SNAPSHOT.jar!/WEB-INF/web.xml
      String sBaseURL = sWebXmlURL;
      if (DefaultResourceResolver.isExplicitJarFileResource (sBaseURL))
      {
        // Cmdline:
        // jar:file:/D:/git/ft-at-cc/target/ft-at-cc-0.1.0-SNAPSHOT.jar!/
        sBaseURL = sBaseURL.substring (0, sBaseURL.lastIndexOf ('!') + 1) + "/";
      }
      else
      {
        // In Eclipse:
        // file:/D:/git/ft-at-cc/target/classes
        sBaseURL = StringHelper.trimEnd (sBaseURL, "WEB-INF/web.xml");
      }

      // Used in the WebAppListener of this project
      SystemProperties.setPropertyValue (FTWebAppListener.SYS_PROP_SERVLET_CONTEXT_BASE, sBaseURL);

      new JettyStarter (MainStart.class).setPort (nPort)
                                        .setStopPort (nStopPort)
                                        .setResourceBase (sBaseURL)
                                        .setWebXmlResource (sWebXmlURL)
                                        .setSessionCookieName ("FT-AT-CC-SESSION")
                                        .run ();
    }
    else
    {
      new HelpFormatter ().printHelp ("ft-at-cc start", aOptions, true);
      System.out.println ("Press Enter to end the application...");
      System.in.read ();
    }
  }
}
