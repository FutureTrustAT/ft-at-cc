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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.xml.crypto.KeySelector;
import javax.xml.xpath.XPath;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.url.SimpleURL;
import com.helger.commons.url.URLHelper;
import com.helger.css.property.ECSSProperty;
import com.helger.ebinterface.EEbInterfaceVersion;
import com.helger.erechnung.erb.ws200.WS200Sender;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.embedded.HCImg;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.metadata.HCHead;
import com.helger.html.hc.html.root.HCHtml;
import com.helger.html.hc.html.sections.HCBody;
import com.helger.html.hc.html.sections.HCFooter;
import com.helger.html.hc.html.textlevel.HCA;
import com.helger.html.hc.html.textlevel.HCCode;
import com.helger.html.hc.html.textlevel.HCSpan;
import com.helger.html.hc.impl.HCEntityNode;
import com.helger.html.hc.impl.HCTextNode;
import com.helger.httpclient.HttpClientFactory;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerMicroDom;
import com.helger.photon.app.url.LinkHelper;
import com.helger.photon.bootstrap4.alert.BootstrapBox;
import com.helger.photon.bootstrap4.alert.EBootstrapAlertType;
import com.helger.photon.bootstrap4.buttongroup.BootstrapButtonToolbar;
import com.helger.photon.bootstrap4.card.BootstrapCard;
import com.helger.photon.bootstrap4.card.BootstrapCardBody;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.form.BootstrapFormHelper;
import com.helger.photon.bootstrap4.grid.BootstrapCol;
import com.helger.photon.bootstrap4.grid.BootstrapRow;
import com.helger.photon.bootstrap4.layout.BootstrapContainer;
import com.helger.photon.bootstrap4.navbar.BootstrapNavbar;
import com.helger.photon.bootstrap4.uictrls.ext.BootstrapFileUpload;
import com.helger.photon.core.execcontext.ILayoutExecutionContext;
import com.helger.photon.core.execcontext.ISimpleWebExecutionContext;
import com.helger.photon.core.execcontext.LayoutExecutionContext;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.html.AbstractSWECHTMLProvider;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.web.fileupload.FileItemResource;
import com.helger.web.fileupload.IFileItem;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xml.EXMLParserFeature;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.serialize.MicroWriter;
import com.helger.xml.namespace.MapBasedNamespaceContext;
import com.helger.xml.sax.WrappedCollectingSAXErrorHandler;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.read.DOMReaderSettings;
import com.helger.xml.xpath.XPathExpressionHelper;
import com.helger.xml.xpath.XPathHelper;
import com.helger.xmldsig.XMLDSigValidationResult;
import com.helger.xmldsig.XMLDSigValidator;
import com.helger.xmldsig.keyselect.ContainedX509KeySelector;
import com.helger.xservlet.forcedredirect.ForcedRedirectException;

import at.gv.brz.eproc.erb.ws.invoicedelivery._201306.DeliveryErrorDetailType;
import at.gv.brz.eproc.erb.ws.invoicedelivery._201306.DeliveryResponseType;
import at.gv.brz.eproc.erb.ws.invoicedelivery._201306.DeliverySettingsType;

/**
 * Main class for creating HTML output
 *
 * @author Philip Helger
 */
public class PublicHTMLProvider extends AbstractSWECHTMLProvider
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PublicHTMLProvider.class);
  private static final String FIELD_FILE = "file";

  @Override
  protected void fillBody (@Nonnull final ISimpleWebExecutionContext aSWEC,
                           @Nonnull final HCHtml aHtml) throws ForcedRedirectException
  {
    final IRequestWebScopeWithoutResponse aRequestScope = aSWEC.getRequestScope ();
    final Locale aDisplayLocale = aSWEC.getDisplayLocale ();
    final ILayoutExecutionContext aLEC = LayoutExecutionContext.createForAjaxOrAction (aRequestScope);
    final HCHead aHead = aHtml.head ();
    final HCBody aBody = aHtml.body ();

    aHead.setPageTitle ("BRZ FutureTrust Pilot Client");

    final BootstrapContainer aCont = aBody.addAndReturnChild (new BootstrapContainer ().setFluid (true));
    final BootstrapNavbar aNavbar = aCont.addAndReturnChild (new BootstrapNavbar ());
    aNavbar.addBrand (new HCImg ().setSrc (LinkHelper.getURLWithContext ("/imgs/FutureTrust-Logo-6.png"))
                                  .addStyle (ECSSProperty.WIDTH, "50px"),
                      new SimpleURL (aRequestScope.getURLDecoded ()));
    aNavbar.addAndReturnText ().addChild ("BRZ FutureTrust Pilot Client");

    final FormErrorList aFormErrors = new FormErrorList ();
    IHCNode aExecutionResult = null;

    if (aLEC.params ().hasStringValue (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM))
    {
      // do it
      final IFileItem aFile = aLEC.params ().getAsFileItem (FIELD_FILE);
      if (aFile == null || StringHelper.hasNoText (aFile.getName ()))
        aFormErrors.addFieldError (FIELD_FILE, "No file was selected");

      if (aFormErrors.isEmpty ())
      {
        final StopWatch aSW = StopWatch.createdStarted ();
        final ICommonsOrderedMap <IHCNode, ErrorList> aActions = new CommonsLinkedHashMap <> ();

        final Document aSrcDoc;
        boolean bValidEbiVersion = false;
        {
          final IHCNode aActionKey = new HCSpan ().addChild ("XML parsing ")
                                                  .addChild (new HCCode ().addChild (FilenameHelper.getWithoutPath (aFile.getName ())));
          final ErrorList aErrorList = new ErrorList ();
          aSrcDoc = DOMReader.readXMLDOM (new FileItemResource (aFile),
                                          new DOMReaderSettings ().setErrorHandler (new WrappedCollectingSAXErrorHandler (aErrorList))
                                                                  .setFeatureValues (EXMLParserFeature.AVOID_XML_ATTACKS));

          if (aSrcDoc == null)
            aActions.put (aActionKey, aErrorList);
          else
          {
            // XML is valid
            aActions.put (aActionKey, null);

            // check ebInterface Version
            final String sNamespaceURI = aSrcDoc.getDocumentElement () == null ? null : aSrcDoc.getDocumentElement ()
                                                                                               .getNamespaceURI ();
            final EEbInterfaceVersion eVersion = EEbInterfaceVersion.getFromNamespaceURIOrNull (sNamespaceURI);
            if (eVersion == null)
            {
              aActions.put (new HCSpan ().addChild ("ebInterface version"),
                            new ErrorList (SingleError.builderError ()
                                                      .setErrorText ("The provided XML is not an ebInterface document")
                                                      .build ()));
            }
            else
            {
              if (eVersion == EEbInterfaceVersion.V41 ||
                  eVersion == EEbInterfaceVersion.V42 ||
                  eVersion == EEbInterfaceVersion.V43)
              {
                aActions.put (new HCSpan ().addChild ("ebInterface " + eVersion.name ()), null);
                bValidEbiVersion = true;
              }
              else
              {
                aActions.put (new HCSpan ().addChild ("ebInterface " + eVersion.name ()),
                              new ErrorList (SingleError.builderError ()
                                                        .setErrorText ("Unsupported version. Only 4.1, 4.2 and 4.3 are supported.")
                                                        .build ()));
              }
            }
          }
        }

        Element aSignatureElement = null;
        if (aSrcDoc != null && bValidEbiVersion)
        {
          final IHCNode aActionKey = new HCTextNode ("Signing XML");
          try
          {
            final XPath aXPath = XPathHelper.createNewXPath (FTHandler.getNSCtx ());
            final NodeList aNodeList = XPathExpressionHelper.evalXPathToNodeList (aXPath, "//ds:Signature", aSrcDoc);
            if (aNodeList != null && aNodeList.getLength () > 0)
            {
              aActions.put (aActionKey,
                            new ErrorList (SingleError.builderError ()
                                                      .setErrorText ("The XML document already contains a signature and cannot be signed again")
                                                      .build ()));
            }
            else
            {
              // Sign document
              aSignatureElement = FTHandler.sign (aSrcDoc,
                                                  FTConfiguration.getPrivateKey (),
                                                  FTConfiguration.getCertificate ());

              // Self-test if signing worked
              XMLDSigValidationResult aResult = XMLDSigValidator.validateSignature (aSrcDoc,
                                                                                    aSignatureElement,
                                                                                    KeySelector.singletonKeySelector (FTConfiguration.getCertificate ()
                                                                                                                                     .getPublicKey ()));
              if (aResult.isInvalid ())
              {
                aActions.put (aActionKey,
                              new ErrorList (SingleError.builderError ()
                                                        .setErrorText ("Failed to validate created signature with constant provided key: " +
                                                                       aResult.toString ())
                                                        .build ()));
              }
              else
              {
                // Validate with key auto-detect
                aResult = XMLDSigValidator.validateSignature (aSrcDoc,
                                                              aSignatureElement,
                                                              new ContainedX509KeySelector ());
                if (aResult.isInvalid ())
                {
                  aActions.put (aActionKey,
                                new ErrorList (SingleError.builderError ()
                                                          .setErrorText ("Failed to validate created signature with contained key: " +
                                                                         aResult.toString ())
                                                          .build ()));
                }
                else
                {
                  // Success
                  aActions.put (aActionKey, null);
                }
              }
            }
          }
          catch (final Exception ex)
          {
            aActions.put (aActionKey, new ErrorList (SingleError.builderError ().setLinkedException (ex).build ()));
          }
        }

        if (aSignatureElement != null)
        {
          if (true)
          {
            // send to test.erechnung
            final String sURL = "https://txm.portal.at/at.gv.bmf.erb.test/FT2";
            final IHCNode aActionKey = new HCSpan ().addChild ("Send to ").addChild (new HCCode ().addChild (sURL));
            LOGGER.info (aActionKey.getPlainText ());

            final MapBasedNamespaceContext aNSCtx = FTHandler.getNSCtx ();
            aNSCtx.addMapping ("eb", aSrcDoc.getDocumentElement ().getNamespaceURI ());

            final WS200Sender aSender = new WS200Sender ("s000j000n466", "2jnrr3kw23u");
            aSender.setTestVersion (true);
            aSender.setURL (URLHelper.getAsURL (sURL));
            aSender.setNamespaceContext (aNSCtx);
            if (false)
              aSender.setDebugMode (true);

            final DeliverySettingsType aSettings = new DeliverySettingsType ();
            aSettings.setTest (Boolean.TRUE);
            aSettings.setLanguage ("en");
            final DeliveryResponseType aResponse = aSender.deliverInvoice (aSignatureElement.getOwnerDocument (),
                                                                           null,
                                                                           aSettings);
            if (aResponse.getSuccess () != null)
              aActions.put (aActionKey, null);
            else
            {
              final ErrorList aEL = new ErrorList ();
              for (final DeliveryErrorDetailType aItem : aResponse.getError ().getErrorDetail ())
                aEL.add (SingleError.builderError ()
                                    .setErrorID (aItem.getErrorCode ())
                                    .setErrorFieldName (aItem.getField ())
                                    .setErrorText (aItem.getMessage ())
                                    .build ());
              aActions.put (aActionKey, aEL);
            }
          }
          else
          {
            // Validate locally
            final IHCNode aActionKey = new HCSpan ().addChild ("Validation at ")
                                                    .addChild (new HCCode ().addChild (FTHandler.getValsURL ()));
            LOGGER.info (aActionKey.getPlainText ());

            try
            {
              final IMicroDocument aVerifyRequestDoc = FTHandler.createVerifyRequest (aSrcDoc.getDocumentElement (),
                                                                                      aSignatureElement);

              final HttpClientFactory aHCFactory = new HttpClientFactory ();
              try (HttpClientManager aMgr = new HttpClientManager (aHCFactory))
              {
                final HttpPost aPost = new HttpPost (FTHandler.getValsURL ());
                final byte [] aSendData = MicroWriter.getNodeAsBytes (aVerifyRequestDoc, FTHandler.getXWS ());

                aPost.setEntity (new ByteArrayEntity (aSendData, ContentType.APPLICATION_XML));

                final ResponseHandlerMicroDom aRH = new ResponseHandlerMicroDom (false);
                final IMicroDocument aDoc = aMgr.execute (aPost, aRH);

                // Dump response
                final IMicroElement aResult = aDoc.getDocumentElement ()
                                                  .getFirstChildElement (FTHandler.NS_DSS2, "Result");
                final boolean bSuccess = aResult != null &&
                                         "urn:oasis:names:tc:dss:1.0:resultmajor:Success".equals (aResult.getTextContentTrimmed ());
                if (LOGGER.isInfoEnabled ())
                  LOGGER.info ("Received response from eRechnung.gv.at - " + (bSuccess ? "success" : "error"));

                if (bSuccess)
                  aActions.put (aActionKey, null);
                else
                  aActions.put (aActionKey,
                                new ErrorList (SingleError.builderError ()
                                                          .setErrorText (MicroWriter.getNodeAsString (aDoc,
                                                                                                      FTHandler.getXWS ()))
                                                          .build ()));
              }
            }
            catch (final IOException ex)
            {
              aActions.put (aActionKey, new ErrorList (SingleError.builderError ().setLinkedException (ex).build ()));
            }
          }
        }

        final BootstrapBox aBox = new BootstrapBox (EBootstrapAlertType.SUCCESS);
        for (final Map.Entry <IHCNode, ErrorList> aEntry : aActions.entrySet ())
        {
          if (true)
          {
            final HCDiv aDiv = aBox.addAndReturnChild (new HCDiv ());
            aDiv.addChild (aEntry.getKey ()).addChild (": ");
            final ErrorList aErrors = aEntry.getValue ();
            if (aErrors == null || aErrors.isEmpty ())
            {
              aDiv.addChild ("success");
            }
            else
            {
              aDiv.addChild (BootstrapFormHelper.createDefaultErrorNode (aErrors, aDisplayLocale, true));
              aBox.setType (EBootstrapAlertType.DANGER);
            }
          }
          else
          {
            final BootstrapRow aRow = aBox.addAndReturnChild (new BootstrapRow ());
            aRow.createColumn (6).addChild (aEntry.getKey ()).addChild (": ");

            final BootstrapCol aResultCol = aRow.createColumn (6);
            final ErrorList aErrors = aEntry.getValue ();
            if (aErrors == null || aErrors.isEmpty ())
            {
              aResultCol.addChild ("success");
            }
            else
            {
              aResultCol.addChild (BootstrapFormHelper.createDefaultErrorNode (aErrors, aDisplayLocale, true));
              aBox.setType (EBootstrapAlertType.DANGER);
            }
          }
        }
        aSW.stop ();
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("Processing took " + aSW.getMillis () + " milliseconds");
        aExecutionResult = aBox;
      }
    }

    final BootstrapRow aRow = aCont.addAndReturnChild (new BootstrapRow ());
    {
      final BootstrapCol aMainCol = aRow.createColumn (6);
      final BootstrapCard aCard = aMainCol.addAndReturnChild (new BootstrapCard ());
      aCard.createAndAddHeader ().addChild ("Sign and send");
      final BootstrapCardBody aCardBody = aCard.createAndAddBody ();

      aCardBody.addChild (aExecutionResult);

      final BootstrapForm aForm = aCardBody.addAndReturnChild (new BootstrapForm (aSWEC));
      aForm.setEncTypeFileUpload ();
      aForm.setLeft (0);
      aForm.addFormGroup (new BootstrapFormGroup ().setCtrl (new BootstrapFileUpload (FIELD_FILE, aDisplayLocale))
                                                   .setHelpText (new HCSpan ().addChild ("Select the ebInterface 4.1/4.2/4.3 file that should be signed and send to ")
                                                                              .addChild (new HCA (new SimpleURL ("https://test.e-rechnung.gv.at")).addChild ("test.e-rechnung.gv.at")
                                                                                                                                                  .setTargetBlank ())
                                                                              .addChild (" for validation."))
                                                   .setErrorList (aFormErrors.getListOfField (FIELD_FILE)));

      final BootstrapButtonToolbar aToolbar = aForm.addAndReturnChild (new BootstrapButtonToolbar (aLEC));
      aToolbar.addHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM);
      aToolbar.addSubmitButton ("Sign and send");
    }

    {
      final BootstrapCol aInfoCol = aRow.createColumn (6);
      final BootstrapCard aCard = aInfoCol.addAndReturnChild (new BootstrapCard ());
      aCard.createAndAddHeader ().addChild ("Configuration information");
      final BootstrapCardBody aCardBody = aCard.createAndAddBody ();
      aCardBody.addChild (new HCDiv ().addChild ("Configuration location: ")
                                      .addChild (new HCCode ().addChild (FTConfiguration.getConfigFile ()
                                                                                        .getReadResource ()
                                                                                        .getPath ())));
      aCardBody.addChild (new HCDiv ().addChild ("Certificate subject: ")
                                      .addChild (new HCCode ().addChild (FTConfiguration.getCertificate ()
                                                                                        .getSubjectX500Principal ()
                                                                                        .getName ())));
      aCardBody.addChild (new HCDiv ().addChild ("Certificate issuer: ")
                                      .addChild (new HCCode ().addChild (FTConfiguration.getCertificate ()
                                                                                        .getIssuerX500Principal ()
                                                                                        .getName ())));
      aCardBody.addChild (new HCDiv ().addChild ("Certificate serial: ")
                                      .addChild (new HCCode ().addChild (FTConfiguration.getCertificate ()
                                                                                        .getSerialNumber ()
                                                                                        .toString (16))));
    }

    aCont.addChild (new HCFooter ().addChild (HCEntityNode.newCopy ())
                                   .addChild (" 2018-2020 BRZ GmbH - Partner von ")
                                   .addChild (new HCA ().addChild ("www.futuretrust.eu")
                                                        .setHref (new SimpleURL ("https://www.futuretrust.eu")))
                                   .addChild (""));
  }
}
