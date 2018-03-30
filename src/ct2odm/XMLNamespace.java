/*
* Copyright 2017 Sam Hume.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ct2odm;

import org.jdom2.Namespace;

/**
 * XMLNamespace creates the XML namespaces used by CDISC ODMv1.3.2
 * @author Sam Hume swhume@gmail.com
 */
public final class XMLNamespace {
    private final Namespace nsNci;
    private final Namespace nsOdm;
    private final Namespace nsXsi;
    private final Namespace nsXml;
    
    public XMLNamespace() {
        this.nsNci = Namespace.getNamespace("nciodm", "http://ncicb.nci.nih.gov/xml/odm/EVS/CDISC");
        this.nsOdm = Namespace.getNamespace("http://www.cdisc.org/ns/odm/v1.3");
        this.nsXml = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
        this.nsXsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    }

    public Namespace getNciNamespace() {
        return this.nsNci;
    } 
    
    public Namespace getOdmNamespace() {
        return this.nsOdm;
    }
    
    public Namespace getXmlNamespace() {
        return this.nsXml;
    }

    public Namespace getXsiNamespace() {
        return this.nsXsi;
    }
}
