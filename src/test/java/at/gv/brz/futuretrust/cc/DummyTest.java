package at.gv.brz.futuretrust.cc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.xml.crypto.KeySelector;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.security.keystore.EKeyStoreType;
import com.helger.security.keystore.KeyStoreHelper;
import com.helger.security.keystore.LoadedKey;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xmldsig.XMLDSigValidationResult;
import com.helger.xmldsig.XMLDSigValidator;

public class DummyTest
{
  @Test
  public void testA () throws Exception
  {
    final String sXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<?xml-stylesheet type=\"text/xsl\" href=\"invoice.xslt\"?>\n" +
                        "<!--\n" +
                        "\n" +
                        "    Copyright (C) 2006-2018 BRZ GmbH\n" +
                        "    http://www.brz.gv.at\n" +
                        "\n" +
                        "    All rights reserved\n" +
                        "\n" +
                        "-->\n" +
                        "<eb:Invoice xmlns:eb=\"http://www.ebinterface.at/schema/4p3/\" eb:DocumentTitle=\"DEMO-Rechnung\" eb:DocumentType=\"Invoice\" eb:GeneratingSystem=\"ebInterface 4.0 Beta 1 Word PlugIn Vorlage Version 4.0 Beta 1 SVN Rev:242\" eb:InvoiceCurrency=\"EUR\" eb:Language=\"ger\">\n" +
                        "  <ns0:Signature xmlns:ns0=\"http://www.w3.org/2000/09/xmldsig#\" Id=\"signature-1-1\">\n" +
                        "    <ns0:SignedInfo>\n" +
                        "      <ns0:CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\" />\n" +
                        "      <ns0:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256\" />\n" +
                        "      <ns0:Reference Id=\"reference-1-1\" URI=\"\">\n" +
                        "        <ns0:Transforms>\n" +
                        "          <ns0:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\" />\n" +
                        "        </ns0:Transforms>\n" +
                        "        <ns0:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />\n" +
                        "        <ns0:DigestValue>8my/qIp2RKQVIE4UerdnmPIkUtbhYpRUt76PdCqMLfM=</ns0:DigestValue>\n" +
                        "      </ns0:Reference>\n" +
                        "      <ns0:Reference Id=\"etsi-data-reference-1-1\" Type=\"http://uri.etsi.org/01903/v1.1.1#SignedProperties\" URI=\"\">\n" +
                        "        <ns0:Transforms>\n" +
                        "          <ns0:Transform Algorithm=\"http://www.w3.org/2002/06/xmldsig-filter2\">\n" +
                        "            <ns1:XPath xmlns:ns1=\"http://www.w3.org/2002/06/xmldsig-filter2\" Filter=\"intersect\">//*[@Id='etsi-signed-1-1']/etsi:QualifyingProperties/etsi:SignedProperties</ns1:XPath>\n" +
                        "          </ns0:Transform>\n" +
                        "        </ns0:Transforms>\n" +
                        "        <ns0:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />\n" +
                        "        <ns0:DigestValue>ME9JaROX8qIm8BUfxsiaXL+iaTh4tuI2+il/dKUW4uc=</ns0:DigestValue>\n" +
                        "      </ns0:Reference>\n" +
                        "    </ns0:SignedInfo>\n" +
                        "    <ns0:SignatureValue>jKzVMbmJQzU3RWtxNUP9MSxbRt1NRmCbEbGxCUUWgqUuH6QeVF8QSNr2zgNcq/bOa11b1sQAAP8E8RsIJiZ3mQ==</ns0:SignatureValue>\n" +
                        "    <ns0:KeyInfo>\n" +
                        "      <ns0:X509Data>\n" +
                        "        <ns0:X509Certificate>MIIEnjCCA4agAwIBAgIDCFB9MA0GCSqGSIb3DQEBBQUAMIGdMQswCQYDVQQGEwJBVDFIMEYGA1UECgw/QS1UcnVzdCBHZXMuIGYuIFNpY2hlcmhlaXRzc3lzdGVtZSBpbSBlbGVrdHIuIERhdGVudmVya2VociBHbWJIMSEwHwYDVQQLDBhhLXNpZ24tcHJlbWl1bS1tb2JpbGUtMDMxITAfBgNVBAMMGGEtc2lnbi1wcmVtaXVtLW1vYmlsZS0wMzAeFw0xMDEyMjIxMTM3NTNaFw0xNTEyMjIxMTM3NTNaMFoxCzAJBgNVBAYTAkFUMRQwEgYDVQQDDAtKb3NlZiBCb2dhZDEOMAwGA1UEBAwFQm9nYWQxDjAMBgNVBCoMBUpvc2VmMRUwEwYDVQQFEww3MTI0MDUxNjk1MDkwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATMtoznQ8NUL5OApYePUTDhktQcUXxfsHHR/A8Sx6XpdbGKRFLF4nO3KnAssrlN6erTPaSQsc+SfXC8xzWD0RTGo4IB8jCCAe4wEwYDVR0jBAwwCoAIS7hh11/WSGMwJwYIKwYBBQUHAQMBAf8EGDAWMAgGBgQAjkYBATAKBggrBgEFBQcLATB+BggrBgEFBQcBAQRyMHAwRQYIKwYBBQUHMAKGOWh0dHA6Ly93d3cuYS10cnVzdC5hdC9jZXJ0cy9hLXNpZ24tcHJlbWl1bS1tb2JpbGUtMDNhLmNydDAnBggrBgEFBQcwAYYbaHR0cDovL29jc3AuYS10cnVzdC5hdC9vY3NwMGAGA1UdIARZMFcwSwYGKigAEQEUMEEwPwYIKwYBBQUHAgEWM2h0dHA6Ly93d3cuYS10cnVzdC5hdC9kb2NzL2NwL2Etc2lnbi1wcmVtaXVtLW1vYmlsZTAIBgYEAIswAQEwgZ0GA1UdHwSBlTCBkjCBj6CBjKCBiYaBhmxkYXA6Ly9sZGFwLmEtdHJ1c3QuYXQvb3U9YS1zaWduLXByZW1pdW0tbW9iaWxlLTAzLG89QS1UcnVzdCxjPUFUP2NlcnRpZmljYXRlcmV2b2NhdGlvbmxpc3Q/YmFzZT9vYmplY3RjbGFzcz1laWRDZXJ0aWZpY2F0aW9uQXV0aG9yaXR5MBEGA1UdDgQKBAhCauHetOTWqDAOBgNVHQ8BAf8EBAMCBsAwCQYDVR0TBAIwADANBgkqhkiG9w0BAQUFAAOCAQEAm2NYiJMygvQFGwFtzS7/+ch2qv+3smCizJrshiB33ETmjRIdqqRcACDAJ/yizP2P/eIoLclPOqrMjLJmwFBwvkZw3MdBKQ4x07kT5enQvx4zYsTtZA3VUw6+KCnpVSj+mrvw3mEwTEGVfkQTZLAIl0uz8kjtiFTGfUUEKmBTztut71L0GRS8iw1RTxUM6DKeJA3OmAmU+ytvuemCXn1qWQACVn5oMOxprgvOJw4qIU/y+nIp4dzXYjzEG9U5waZgGm68F/KcWnYNNNhq1sYd2NDvtCLgjdLEPeZBwbwJQXo037IGLiXPXu0JPXISXnGLyPaRXEGfFMYJKNGOLnahSw==</ns0:X509Certificate>\n" +
                        "      </ns0:X509Data>\n" +
                        "      <ns0:KeyName>SERIALNUMBER=712405169509, G=Josef, SN=Bogad, CN=Josef Bogad, C=AT</ns0:KeyName>\n" +
                        "    </ns0:KeyInfo>\n" +
                        "    <ns0:Object Id=\"etsi-signed-1-1\">\n" +
                        "      <ns1:QualifyingProperties xmlns:ns1=\"http://uri.etsi.org/01903/v1.1.1#\" Target=\"#signature-1-1\">\n" +
                        "        <ns1:SignedProperties>\n" +
                        "          <ns1:SignedSignatureProperties>\n" +
                        "            <ns1:SigningTime>2012-03-12T11:01:14Z</ns1:SigningTime>\n" +
                        "            <ns1:SigningCertificate>\n" +
                        "              <ns1:Cert>\n" +
                        "                <ns1:CertDigest>\n" +
                        "                  <ns1:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\" />\n" +
                        "                  <ns1:DigestValue>yexQhxCAH1rgoZ0uCc1+d8ylaH4=</ns1:DigestValue>\n" +
                        "                </ns1:CertDigest>\n" +
                        "                <ns1:IssuerSerial>\n" +
                        "                  <ns0:X509IssuerName>CN=a-sign-premium-mobile-03,OU=a-sign-premium-mobile-03,O=A-Trust Ges. f. Sicherheitssysteme im elektr. Datenverkehr GmbH,C=AT</ns0:X509IssuerName>\n" +
                        "                  <ns0:X509SerialNumber>544893</ns0:X509SerialNumber>\n" +
                        "                </ns1:IssuerSerial>\n" +
                        "              </ns1:Cert>\n" +
                        "            </ns1:SigningCertificate>\n" +
                        "            <ns1:SignaturePolicyIdentifier>\n" +
                        "              <ns1:SignaturePolicyImplied />\n" +
                        "            </ns1:SignaturePolicyIdentifier>\n" +
                        "          </ns1:SignedSignatureProperties>\n" +
                        "          <ns1:SignedDataObjectProperties>\n" +
                        "            <ns1:DataObjectFormat ObjectReference=\"#reference-1-1\">\n" +
                        "              <ns1:MimeType>text/html</ns1:MimeType>\n" +
                        "            </ns1:DataObjectFormat>\n" +
                        "          </ns1:SignedDataObjectProperties>\n" +
                        "        </ns1:SignedProperties>\n" +
                        "      </ns1:QualifyingProperties>\n" +
                        "    </ns0:Object>\n" +
                        "  </ns0:Signature>\n" +
                        "  <eb:InvoiceNumber>2012-123</eb:InvoiceNumber>\n" +
                        "  <eb:InvoiceDate>2018-01-01</eb:InvoiceDate>\n" +
                        "  <eb:Delivery>\n" +
                        "    <eb:Date>2018-01-01</eb:Date>\n" +
                        "  </eb:Delivery>\n" +
                        "  <eb:Biller>\n" +
                        "    <eb:VATIdentificationNumber>ATU62698637</eb:VATIdentificationNumber>\n" +
                        "    <eb:Address>\n" +
                        "      <eb:Name>Bogad &amp; Partner Consulting OG</eb:Name>\n" +
                        "      <eb:Street>Steinbachstraße 17</eb:Street>\n" +
                        "      <eb:Town>Mauerbach</eb:Town>\n" +
                        "      <eb:ZIP>3001</eb:ZIP>\n" +
                        "      <eb:Country eb:CountryCode=\"AT\">Österreich</eb:Country>\n" +
                        "      <eb:Phone>+43 (699) 17925908</eb:Phone>\n" +
                        "      <eb:Email>philip.helger@brz.gv.at</eb:Email>\n" +
                        "      <eb:Contact>Josef Bogad</eb:Contact>\n" +
                        "    </eb:Address>\n" +
                        "  </eb:Biller>\n" +
                        "  <eb:InvoiceRecipient>\n" +
                        "    <eb:VATIdentificationNumber>00000000</eb:VATIdentificationNumber>\n" +
                        "\n" +
                        "\n" +
                        "    <eb:SubOrganizationID>Rechnungswesen</eb:SubOrganizationID>\n" +
                        "    <eb:OrderReference>\n" +
                        "      <eb:OrderID>AB-899344</eb:OrderID>\n" +
                        "      <eb:ReferenceDate>2012-01-10</eb:ReferenceDate>\n" +
                        "    </eb:OrderReference>\n" +
                        "    <eb:Address>\n" +
                        "      <eb:Salutation>Firma</eb:Salutation>\n" +
                        "      <eb:Name>Max Mustermann</eb:Name>\n" +
                        "      <eb:Street>Kundenstrasse 1</eb:Street>\n" +
                        "      <eb:Town>Kaufort</eb:Town>\n" +
                        "      <eb:ZIP>1234</eb:ZIP>\n" +
                        "      <eb:Country eb:CountryCode=\"AT\">Österreich</eb:Country>\n" +
                        "      <eb:Phone>\n" +
                        "      </eb:Phone>\n" +
                        "      <eb:Email>\n" +
                        "      </eb:Email>\n" +
                        "      <eb:Contact>Max Mustermann</eb:Contact>\n" +
                        "    </eb:Address>\n" +
                        "    <eb:BillersInvoiceRecipientID>123123</eb:BillersInvoiceRecipientID>\n" +
                        "    <eb:AccountingArea>\n" +
                        "    </eb:AccountingArea>\n" +
                        "  </eb:InvoiceRecipient>\n" +
                        "  <eb:Details>\n" +
                        "    <eb:HeaderDescription>Wir erlauben uns wie folgt zu verrechnen.</eb:HeaderDescription>\n" +
                        "    <eb:ItemList>\n" +
                        "      <eb:ListLineItem>\n" +
                        "        <eb:PositionNumber>1</eb:PositionNumber>\n" +
                        "        <eb:Description>ebInterface Beratung</eb:Description>\n" +
                        "        <eb:ArticleNumber>4711</eb:ArticleNumber>\n" +
                        "        <eb:Quantity eb:Unit=\"Tag\">0.50</eb:Quantity>\n" +
                        "        <eb:UnitPrice>400.00</eb:UnitPrice>\n" +
                        "        <eb:VATRate>20.00</eb:VATRate>\n" +
                        "        <eb:DiscountFlag>false</eb:DiscountFlag>\n" +
                        "        <eb:LineItemAmount>200.00</eb:LineItemAmount>\n" +
                        "      </eb:ListLineItem>\n" +
                        "    </eb:ItemList>\n" +
                        "    <eb:FooterDescription>Wir danken für Ihren Auftrag.</eb:FooterDescription>\n" +
                        "  </eb:Details>\n" +
                        "  <eb:Tax>\n" +
                        "    <eb:VAT>\n" +
                        "      <eb:VATItem>\n" +
                        "        <eb:TaxedAmount>200.00</eb:TaxedAmount>\n" +
                        "        <eb:VATRate>20.00</eb:VATRate>\n" +
                        "        <eb:Amount>40.00</eb:Amount>\n" +
                        "      </eb:VATItem>\n" +
                        "    </eb:VAT>\n" +
                        "  </eb:Tax>\n" +
                        "  <eb:TotalGrossAmount>240.00</eb:TotalGrossAmount>\n" +
                        "  <eb:PayableAmount>240.00</eb:PayableAmount>\n" +
                        "  <eb:PaymentMethod>\n" +
                        "\n" +
                        "  <eb:UniversalBankTransaction eb:ConsolidatorPayable=\"false\">\n" +
                        "      <eb:BeneficiaryAccount>\n" +
                        "      <eb:BankName>Volksbank Wien</eb:BankName>\n" +
                        "      <eb:BankCode eb:BankCodeType=\"AT\">43000</eb:BankCode>\n" +
                        "      <eb:BankAccountNr>40813033004 </eb:BankAccountNr>\n" +
                        "      <eb:BankAccountOwner>Bogad &amp; Partner Consulting OG</eb:BankAccountOwner>\n" +
                        "    </eb:BeneficiaryAccount>\n" +
                        "    </eb:UniversalBankTransaction>\n" +
                        "</eb:PaymentMethod>\n" +
                        "  <eb:PaymentConditions>\n" +
                        "    <eb:DueDate>2018-12-31</eb:DueDate>\n" +
                        "  </eb:PaymentConditions>\n" +
                        "  <eb:PresentationDetails>\n" +
                        "    <eb:URL>www.austriapro.at</eb:URL>\n" +
                        "    <eb:SuppressZero>true</eb:SuppressZero>\n" +
                        "  </eb:PresentationDetails>\n" +
                        "</eb:Invoice>\n";

    final String sSignature = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                              "<ds:Signature Id=\"Signature_1521704672631\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\r\n" +
                              "  <ds:SignedInfo Id=\"Signature_1521704672631-SignedInfo\">\r\n" +
                              "    <ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>\r\n" +
                              "    <ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/>\r\n" +
                              "    <ds:Reference URI=\"#manifest\" Type=\"http://www.w3.org/2000/09/xmldsig#Manifest\" Id=\"Signature_1521704672631-Reference-0\">\r\n" +
                              "      <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "      <ds:DigestValue>nwG34ZhIwk5YzFkXnO7bF1Dikkw3FyrwJ49QRlb+4wgW74/alVmDOAWqhKkbWUprJzBEzilfzTmS/p9Mr/n8OA==</ds:DigestValue>\r\n" +
                              "    </ds:Reference>\r\n" +
                              "    <ds:Reference URI=\"#Signature_1521704672631_SignedProperties\" Type=\"http://uri.etsi.org/01903#SignedProperties\">\r\n" +
                              "      <ds:Transforms>\r\n" +
                              "        <ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>\r\n" +
                              "      </ds:Transforms>\r\n" +
                              "      <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "      <ds:DigestValue>m+Hd2y9PFIp3THWSpfsQRdBwcowhbhAFf325CRbweYb5SCZ8gVsHcun2vA//riQmNdtmRXBne1R37R0rhzBI5Q==</ds:DigestValue>\r\n" +
                              "    </ds:Reference>\r\n" +
                              "  </ds:SignedInfo>\r\n" +
                              "  <ds:SignatureValue Id=\"Signature_1521704672631_Value\">UgHpqCjB4Tejl4OrsdMMU7ozJl6LvKBWxRJKnFmnfMoxtASIc1L57lOAHUpDW3L7tMmVlrNJ04O3fWsY0l5mfL7ddbtDod8zfaZzzuvYOKsSzqJj5P64p660tE3nJU5UksiubBinotuAv8D7mMJ2SI2a0DKYqTk0us/luDj1x5WyjXmin4z8XX67zFE4qPdSsfCvs5BrIyFO5TiXpfQlLTtGoXK1kQVEVEiBqn7yrPZ/w2vR19lCUFYwfLUwcKPcfXnGYJDgAz9n2abZWsLJCO8PFNdbzQa5uCRHY/23nU4SdQF/O75ItXV3w1BaYXuSJWRXRo0I4DNIpjir0wqLnw==</ds:SignatureValue>\r\n" +
                              "  <ds:KeyInfo>\r\n" +
                              "    <ds:X509Data>\r\n" +
                              "      <ds:X509Certificate>MIIFwzCCA6ugAwIBAgIUCn6m30tEntpqJIWe5rgV0xZ/u7EwDQYJKoZIhvcNAQELBQAwRjELMAkGA1UEBhMCTFUxFjAUBgNVBAoMDUx1eFRydXN0IFMuQS4xHzAdBgNVBAMMFkx1eFRydXN0IEdsb2JhbCBSb290IDIwHhcNMTUwMzA1MTMyMTU3WhcNMzUwMzA1MTMyMTU3WjBGMQswCQYDVQQGEwJMVTEWMBQGA1UECgwNTHV4VHJ1c3QgUy5BLjEfMB0GA1UEAwwWTHV4VHJ1c3QgR2xvYmFsIFJvb3QgMjCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBANeFl78RmOnwYoNMPIf5U2o3C/IPPIfOb9wmKb3FibrJgz337spbxm1Jc7TJRqMbNBM/wYlFV/TZsfs2ZUv7COJIcRHIbjuend+JZTemhfY7RBi2xjcwYkSSl2l9QjAk5A0MiWtj3sXh306pFGxT4GHO9hcvHTy95iJMHZP1EMShduxq3sVs35a0VkBCwGKSMKEtFZSg0iAGCW5qbeXrt77U8PEVfIvmTroTzEsnXpk8F12PgX8zPU/TPxvsXD/wPEx1bvKm1Z3aLQdjAsZy6ZS8TEmVT4hSyNvoaYL4zDRbIvCGp4m9SAptZoFtyMhk+wHh9OHe2Z7d21vUKpkmFRseTJIpgp7VkoGSQXAZ96Tlk0u8d2cx3Rz9MXANF5kM+Qw5GSoXtTBxVdUPrljhPS80m8+f9niFwpN6cj5mj5wWEWCPnolvZ77gR1o7DJpni89Gxq44o/KnvObWhWszJHAiS8sIm7vI+AIpHb4gDEa/a4ebsypmQjVGbKq6rfmYe+lQVRQxv7HaLe2ArWgk+2mr2HETMOZns4dA/Yl+8kPREd8vZS9kzl8UubG/Mb2HeFpZZYiq/FkySIbWTLkpS5XTdvN3JW1CHDiDTf2jX5t/Lax5Gw5CMZdjpPuKadUiDTSQMC6otOBttpSsvItO13D8xTiOZCXhTTmQzsmHhFhxAgMBAAGjgagwgaUwDwYDVR0TAQH/BAUwAwEB/zBCBgNVHSAEOzA5MDcGByuBKwEBAQowLDAqBggrBgEFBQcCARYeaHR0cHM6Ly9yZXBvc2l0b3J5Lmx1eHRydXN0Lmx1MA4GA1UdDwEB/wQEAwIBBjAfBgNVHSMEGDAWgBT/GCh2+UgFLKGu8SsbK7JT+Et8szAdBgNVHQ4EFgQU/xgodvlIBSyhrvErGyuyU/hLfLMwDQYJKoZIhvcNAQELBQADggIBAGoZFO1uecEsh9QNcH7X9njJCwROxLHOk3D+sFTAMs2ZMGQXvw/l4jP9BzZAcg4atmpZ1gDlaCDdLnINH2pkMSCEfUmmWjfrRcmF9dTHF5kH5ptV5AzoqbTOjFu1EVzPig4N1qx3gf4ynCSecs5U89BvolbW7MM3LGVYvlcAGvI1+ut7MV3CwRI9loGIlonBWVx65n9wNOeD4rHh4bhY79SV5GCc8JaXcozrhAIuZY+kt9J/Z93I055cqqmkoCUUBpvsT34tC38ddfEz2O3OuHVtPlu5mB0xDVbYQw8wkbIEa91WvpWAVWe+2M2D2RjuLg+GLZKecBPs3lHJQ3gCpU3I+V/EkVhGFndadKpAvAefMLmx9xIX3eP/JEAdemrRTxgKqpAd60Ae36EeRJIQmvKN4dFLRp7oRUKX6kWZ8+xm1QL68qZKJKrezrnK+T+Tb/mjuuqlPpmt/f97mfVl7vBZKGfXkJWkE4SphMHozs51k2MavDzq1WQfLSoSOcbDWjLtR5EWDrw4wVDej8oqkDQc7kGUnF4ZLvhFSZl0kbAEb+MEWrGrKqv+x9CWttrhSmQGbmBNvUJO/3jaJMobtNeWOWyu8Q6qp31IiyBMz2TWuJdGsE7RKlY6oJO9r4Ak4Ap+58rVyuiFVdw2KuGUaJPHZnJED4AhMmwlxyOAgwrr</ds:X509Certificate>\r\n" +
                              "      <ds:X509Certificate>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</ds:X509Certificate>\r\n" +
                              "      <ds:X509Certificate>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</ds:X509Certificate>\r\n" +
                              "    </ds:X509Data>\r\n" +
                              "  </ds:KeyInfo>\r\n" +
                              "  <ds:Object>\r\n" +
                              "    <xades:QualifyingProperties Target=\"#Signature_1521704672631\" xmlns:xades=\"http://uri.etsi.org/01903/v1.3.2#\">\r\n" +
                              "      <xades:SignedProperties Id=\"Signature_1521704672631_SignedProperties\">\r\n" +
                              "        <xades:SignedSignatureProperties>\r\n" +
                              "          <xades:SigningTime>2018-03-22T07:44:32.725Z</xades:SigningTime>\r\n" +
                              "          <xades:SigningCertificate>\r\n" +
                              "            <xades:Cert>\r\n" +
                              "              <xades:CertDigest>\r\n" +
                              "                <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/>\r\n" +
                              "                <ds:DigestValue>sNQLYs3jWtUvACiwBuJRl/fXktC3/InrvMScoNwKkKE=</ds:DigestValue>\r\n" +
                              "              </xades:CertDigest>\r\n" +
                              "              <xades:IssuerSerial>\r\n" +
                              "                <ds:X509IssuerName>CN=LuxTrust Global Qualified CA 3,O=LuxTrust S.A.,C=LU</ds:X509IssuerName>\r\n" +
                              "                <ds:X509SerialNumber>3136253</ds:X509SerialNumber>\r\n" +
                              "              </xades:IssuerSerial>\r\n" +
                              "            </xades:Cert>\r\n" +
                              "          </xades:SigningCertificate>\r\n" +
                              "        </xades:SignedSignatureProperties>\r\n" +
                              "        <xades:SignedDataObjectProperties>\r\n" +
                              "          <xades:DataObjectFormat ObjectReference=\"#Signature_1521704672631-Reference-0\">\r\n" +
                              "            <xades:MimeType>text/xml</xades:MimeType>\r\n" +
                              "          </xades:DataObjectFormat>\r\n" +
                              "        </xades:SignedDataObjectProperties>\r\n" +
                              "      </xades:SignedProperties>\r\n" +
                              "      <xades:UnsignedProperties>\r\n" +
                              "        <xades:UnsignedSignatureProperties>\r\n" +
                              "          <xades:SignatureTimeStamp>\r\n" +
                              "            <ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>\r\n" +
                              "            <xades:EncapsulatedTimeStamp Encoding=\"http://uri.etsi.org/01903/v1.2.2#DER\">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</xades:EncapsulatedTimeStamp>\r\n" +
                              "          </xades:SignatureTimeStamp>\r\n" +
                              "        </xades:UnsignedSignatureProperties>\r\n" +
                              "      </xades:UnsignedProperties>\r\n" +
                              "    </xades:QualifyingProperties>\r\n" +
                              "  </ds:Object>\r\n" +
                              "  <ds:Object MimeType=\"http://www.w3.org/2000/09/xmldsig#Manifest\">\r\n" +
                              "    <ds:Manifest Id=\"manifest\">\r\n" +
                              "      <ds:Reference URI=\"l_07920180322bg.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>iN58JNbdHCQ+0scoboK7Tt+wIor+DIbzjTpDeZe2k3rct/YzGAn48JdmLk9jkOpBzDB2Ln9e8H1CZchMQSz2pQ==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322cs.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>V3E/dQ1Zx1mhoj1Z7LJ7QuG0Z2c2epsjaPWJdKFY/fAVQ+RtQng5PBHS6T1GrMtR6VYg+zlioi3pczHadHwhcg==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322da.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>TWBZW2QDPXwzxrtf8WN4lWjw9zNwThawjdyRLUmkLz1LKLDNV+n2NhJmqIkxRwYcHB1ECcksi/oAgiLy90GhDw==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322de.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>0ewR0uLME9pCaP+g86303esAxmwHMOs6v9u7zxBsLkwBFoPKeiwE195lcLOiZDMWr1firxP8V/kg76AwNP56QQ==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322el.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>vsWagZwtue/68YND7NAHop00nrhBVTWas3JVIohYujAlExrs3Vml4nN/qShEeqLaEU0BG5iu4XryjGddeH8aAw==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322en.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>4oxU+d8DO6SjTnqPQyWFEFjLkMU2E1y1getTc7KwgEneAINiK3PlguGlgzv3hyHo8XDT0NEqIEO0zhB6iYa9Ow==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322et.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>15qQX11CEn029KaxhX7NEpvB5/b315ohDZoTtJ4xLWM1fQ23X3pxpx6gGM7E9kH37Dz181GpVKPnmY0eaSfg5A==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322fi.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>Lolsen8NfDIPxKCa+EuiTN/ZpYWqFFdjJt8mWgfmkECpilOtCyKGmQRmWGsJLqSGBIetscrce+ziIS0g8zuz6A==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322fr.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>BQf6vAlpKyCdSsjsDsMW1IGBS8j4cqm1wncz/BG1zwsPjOztT+tzZzWjZ1aGtbxnK74GPO1nSQEN430jQ2Rtbw==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322hr.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>V68JrYdpydKIc+1HZ5bx0NlQ+QF3vFUjapyyGrdChLJFBh24Xd1YKtQuhA2jxbWgjgXeHyJ18iXXanSCo5dhog==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322hu.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>8z52WdDjtCR4+UEr379VqlJWEFZFGTPJE8FO2ircp/20Sc3Ns7RHOJNZDDQO3BV7r9siGVrBMLtGngFjknkBLA==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322it.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>o5VVCL0YdSVfR47WJj1vYCVCCpbNwRuzyvJXFxrEnKcjyxFJBDCanxnHZPrsEWzKvb+KQmveRxZttobmzCY4Yg==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322lv.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>xt0pO0Vg7lVpEcEBe3lu0/6DNN2RKBcp/GP2X8DqfMvyWMBmErTLe4m2HWzVczi59FpRf+3HC7hKKQiisseEhw==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322lt.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>eXFdChefyeFXsuyiEAzcreHuAhhd4/MNlkoYqYSdOoVRJ5Tubf+sdk0hNUV83IbeYaJbjspGtFaYHgJBtUkwqg==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322mt.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>h6bcBnBJajg/K6K44Pmh1DfS4mfD7HR7Pdl8DO19n8rZXoeeIdh/kSsULO/JkfB4LuL7cGdOmH06wxNDe9pijA==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322nl.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>KtJ02wX0vFZTBqu4z3S5wFC56y7j/6QeOJ3bCBU69y3n8YLaz1sMfIee+eM6acz6auPutv8sP0N/Br3NWjqrvw==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322pl.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>rXEnCkojp5Ko+FWE04/7oKaL9bUudy7gDZaMX0srhOuXYDdlGAsGpL7ThPR3rPT5iUq3WFV4kiuCYIGLJv1HXA==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322pt.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>q++xqA05MIcY8vRp6cuaLFsOqA5xjRrZNIBZzI7V5Bp050pwuBtEt+IMDAd6EvoYWJydkgnzLfL1DTIhqEN6MQ==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322ro.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>siJWvwkXJqXra/WV9zWkHC/XQrQZsE/WyOqre5iK2WbiAPatFoUE+ISHW9H2MUz8dgCSoVVk5euzm6QUdnxg7A==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322sk.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>un85O4h/iSn20JIqpd+u0cZh18iX1YC5VWldA1AkKtRsAHFX5PguoOIl7jgAWXhbRhbj9nb6She7tlF4iXjxzQ==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322sl.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>TF3dUSieUxDDFtZz7WftV5pwnXoVxzab0yld38Lo2bfuW8XOHAvch++cExFI1jPe8dqoMHyWIXGJieVWiVPOhw==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322es.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>k3lEMwFJlFA18e20+1ly6pLglkf2V6KQkIXLtqANL4Idb7QizVowi43bForJ4dM5Kavv/D+SmQLHDeFxyptIqg==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "      <ds:Reference URI=\"l_07920180322sv.pdf\">\r\n" +
                              "        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha512\"/>\r\n" +
                              "        <ds:DigestValue>1j82EmR5Fzy/X0u9nnZqAXNz3lH/ypg1kCGeI8V5k6mAhiBl0X7tPZX2GSF8dOnhKERhfW8Eh/aX++iCGFq5oA==</ds:DigestValue>\r\n" +
                              "      </ds:Reference>\r\n" +
                              "    </ds:Manifest>\r\n" +
                              "  </ds:Object>\r\n" +
                              "</ds:Signature>\r\n";

    final Document aEbiDoc = DOMReader.readXMLDOM (sXML);
    assertNotNull (aEbiDoc);

    // Sign
    final Element aSignatureElement = DOMReader.readXMLDOM (sSignature).getDocumentElement ();

    final String sKSPath = "futureTrust.cz.eRechnung.jks";
    final String sPW = "changeit";
    final KeyStore aKS = KeyStoreHelper.loadKeyStoreDirect (EKeyStoreType.JKS, sKSPath, sPW.toCharArray ());
    assertNotNull (aKS);
    final LoadedKey <PrivateKeyEntry> aLK = KeyStoreHelper.loadPrivateKey (aKS,
                                                                           sKSPath,
                                                                           "futuretrust.cz.erechnung",
                                                                           sPW.toCharArray ());
    assertTrue (aLK.isSuccess ());
    assertNotNull (aLK.getKeyEntry ());
    final X509Certificate aCertificate = (X509Certificate) aLK.getKeyEntry ().getCertificate ();

    // Check if it worked
    final XMLDSigValidationResult aResult = XMLDSigValidator.validateSignature (aEbiDoc,
                                                                                aSignatureElement,
                                                                                KeySelector.singletonKeySelector (aCertificate.getPublicKey ()));
    assertTrue ("Error: " + aResult.toString (), aResult.isValid ());
  }
}
