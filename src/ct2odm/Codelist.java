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

import java.util.ArrayList;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Codelist represents a CDISC Controlled Terminology Code List
 * @author Sam Hume swhume@gmail.com
 */
class Codelist {
    private final XMLNamespace ns = new XMLNamespace();
    private List<Term> termList = new ArrayList<>();
    private final String clCode;
    private final String clExtensible;
    private final String clName;
    private final String clSubVal;
    private final String clSynonym;
    private final String clDefn;
    private final String clPrefTerm;

    /**
     * Codelist constructor takes a string array of data from the CT delimited text file 
     * @param row a String array containing the row of delimited text file CT
     */
    public Codelist(String[] row) {
        this.clCode = row[0];        // Code
        this.clExtensible = row[2];  // Codelist Extensible (Yes/No)
        this.clName = row[3];        // Codelist Name
        this.clSubVal = row[4];      // CDISC Submission Value
        this.clSynonym = row[5];     // CDISC Synonym(s)
        this.clDefn = row[6];        // CDISC Definition
        this.clPrefTerm = row[7];    // NCI Preferred Term
    }
    
    /**
     * Create an ODM CodeList element
     * @return Element an ODM CodeList element
     */
    public Element generateCodeListElement() {
        Element cl = new Element("CodeList", this.ns.getOdmNamespace());
        cl.setAttribute(new Attribute("OID", "CL." + this.clCode + "." + this.clSubVal));
        cl.setAttribute(new Attribute("Name", this.clName));
        cl.setAttribute(new Attribute("DataType", "text"));
        cl.setAttribute(new Attribute("ExtCodeID", this.clCode, this.ns.getNciNamespace()));
        cl.setAttribute(new Attribute("CodeListExtensible", this.clExtensible, this.ns.getNciNamespace()));
        cl.addContent(generateDescription());
        return cl;
    }
    
    /* Creates the Description element that will be added as a child of CodeList */
    private Element generateDescription() {
        Element desc = new Element("Description", this.ns.getOdmNamespace());
        Element transText = new Element("TranslatedText", this.ns.getOdmNamespace());
        transText.setAttribute(new Attribute("lang", "en", this.ns.getXmlNamespace()));
        transText.addContent(this.clDefn);
        desc.addContent(transText);
        return desc;
    }    

    /** 
     * AddTerm adds a Term object to the list of terms that are part of a CodeList
     * @param t Term object
     */
    public void AddTerm(Term t) {
        if (t == null) 
            throw new IllegalArgumentException("Null terms cannot be added to the code list: " + this.clCode);
        this.termList.add(t);
    }

    /** 
     * Returns the list of term objects for a code list
     * @return termList as Iterable list of code list terms 
     */
    public Iterable<Term> getTerms() {
        return this.termList;
    }

    /**
     * Create the submission value element
     * @return elSubVal returns the submission value element for the code list
     */
    public Element generateSubVal() {
        Element elSubVal = new Element("CDISCSubmissionValue", this.ns.getNciNamespace());    
        elSubVal.addContent(this.clSubVal);
        return elSubVal;           
    }
    
    /**
     * Create the list of synonyms element
     * @return elSynonym as the synonyms element for the code list
     */
    public Element generateSynonym() {
        Element elSynonym = new Element("CDISCSynonym", this.ns.getNciNamespace());    
        elSynonym.addContent(this.clSynonym);
        return elSynonym;           
    }

    /**
     * Create the preferred term element
     * @return elPrefTerm as the preferred term element for the code list
     */
    public Element generatePreferredTerm() {
        Element elPrefTerm = new Element("PreferredTerm", this.ns.getNciNamespace());    
        elPrefTerm.addContent(this.clPrefTerm);
        return elPrefTerm;           
    }
}
