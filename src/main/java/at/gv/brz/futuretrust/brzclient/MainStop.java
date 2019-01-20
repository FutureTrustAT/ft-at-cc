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

import com.helger.cli.CmdLineParser;
import com.helger.cli.HelpFormatter;
import com.helger.cli.Option;
import com.helger.cli.Options;
import com.helger.cli.ParsedCmdLine;
import com.helger.photon.jetty.JettyStopper;

/**
 * Main application to stop a server previously started with {@link MainStart}.
 *
 * @author Philip Helger
 */
public final class MainStop extends AbstractMain
{
  public static void main (final String [] args) throws Exception
  {
    final Options aOptions = new Options ().addOption (Option.builder ("sp")
                                                             .longOpt ("stop-port")
                                                             .desc ("Port to listen for stop signal. Defaults to " +
                                                                    DEFAULT_STOP_PORT)
                                                             .args (1))
                                           .addOption (Option.builder ("?").longOpt ("help").desc ("Show this help"));

    final ParsedCmdLine aCL = new CmdLineParser (aOptions).parseOrNull (args);

    final boolean bCanStart = aCL != null && !aCL.hasOption ("?");

    if (bCanStart)
    {
      final int nStopPort = aCL.getAsInt ("sp", DEFAULT_STOP_PORT);
      new JettyStopper ().setStopPort (nStopPort).run ();
    }
    else
    {
      new HelpFormatter ().printHelp ("ft-at-cc stop", aOptions, true);
      System.out.println ("Press Enter to end the application...");
      System.in.read ();
    }
  }
}
