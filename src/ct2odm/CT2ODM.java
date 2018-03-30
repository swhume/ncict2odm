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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import java.time.LocalDateTime;

/**
 * CT2ODM converts the NCI EVS Controlled Terminology package tab delimited text 
 * file into a CT-XML v1.1 file based on CDISC ODMv1.3.2
 * @author Sam Hume swhume@gmail.com
 */
public class CT2ODM {
    private String odmFileName;
    private String txtFileName;
    private String pkgDate;
    private String standard;
    private String version;
    private Document document;
    private final XMLNamespace ns = new XMLNamespace();    
    
    /**
    * main
    * @param args - the command line arguments are:
    * "odm=odm-file" path and file name of the odm output file
    * "txt=txt-file" path and file name of the tab delimited text input file
    * "std=standard-name" name of the standard for the CT package (e.g. sdtm)
    * "date=package-date" CT package date in ISO 8601 format (YYYY-MM-DD)
     */
    public static void main(String[] args) {
        CT2ODM ct2odm = new CT2ODM();
        ct2odm.setCommandLineOptions(args);
        ct2odm.validateCommandLineOptions();
        ct2odm.buildOdmFile();
    }

    /* template for building ODM XML controlled terminology file */
    private void buildOdmFile() {
        TreeMap<String, Codelist> pkg = ProcessTxtFile();
        Element root = createRootNode();
        this.document = new Document(root);
        generateClXml(pkg, root);
        writeOdmFile();
    }
    
    /* create ODM XML controlled terminology content from TreeMap loaded from text file */
    private void generateClXml(TreeMap<String, Codelist> pkg, Element root) {
        Element study = new Element("Study", this.ns.getOdmNamespace());
        study.setAttribute(new Attribute("OID", "CDISC_CT." + this.standard + "." + this.pkgDate));
        generateGlobalVariables(study);
        Element mdv = generateMDV();
        for (String cCode : pkg.keySet()) {
            Codelist cl = pkg.get(cCode);
            Element clElem = cl.generateCodeListElement();
            for (Term t : cl.getTerms()) {
                Element item = t.generateEnumeratedItem(t);
                clElem.addContent(item);
            }
            clElem.addContent(cl.generateSubVal());
            clElem.addContent(cl.generateSynonym());
            clElem.addContent(cl.generatePreferredTerm());
            mdv.addContent(clElem); 
        }
        study.addContent(mdv);
        root.addContent(study);
    }
    
    /* create the ODM MetaDataVersion element */
    private Element generateMDV() {
        Element mdv = new Element("MetaDataVersion", this.ns.getOdmNamespace());
        mdv.setAttribute(new Attribute("OID", "CDISC_CT_MetaDataVersion." + this.standard + "." + this.pkgDate));
        String mdvInfo = "CDISC " + this.standard + " Controlled Termninology";
        mdv.setAttribute(new Attribute("Name", mdvInfo));
        mdv.setAttribute(new Attribute("Description", mdvInfo + ", " + this.pkgDate));
        return mdv;
    }

    /* set the ODM global variable content */
    private void generateGlobalVariables(Element study) {
        Element global = new Element("GlobalVariables", this.ns.getOdmNamespace());
        Element studyName = new Element("StudyName", this.ns.getOdmNamespace());
        String studyInfo = "CDISC " + this.standard + " Controlled Terminology";
        studyName.setText(studyInfo);
        global.addContent(studyName);
        Element studyDescription = new Element("StudyDescription", this.ns.getOdmNamespace());
        studyDescription.setText(studyInfo + ", " + this.pkgDate);
        global.addContent(studyDescription);
        Element protocol = new Element("ProtocolName", this.ns.getOdmNamespace());
        protocol.setText(studyInfo);
        global.addContent(protocol);
        study.addContent(global);
    }
    
    /* create a TreeMap of Codelist objects from delimited text file */
    private TreeMap<String, Codelist> ProcessTxtFile() {
      TreeMap<String, Codelist> pkg = new TreeMap<>();
      Charset charset = Charset.forName("UTF-8");
      StringBuilder txt;
      try (BufferedReader f = new BufferedReader(new FileReader(this.txtFileName))) {
          String line;
          txt = new StringBuilder();
          f.readLine();
          while((line = f.readLine()) != null) {
              String[] row = line.split("\\t");
              if (row.length < 8) {
                  // assert no unexpected content exists in the CT text input file
                  System.out.println("Invalid row content likely due to invalid characters"
                          + " in the load file in row: " + row[0] + "." + row[1]);
                  System.exit(0);
              } else if (row[1] == null || row[1].isEmpty()) {
                  pkg.put(row[0], new Codelist(row));
              } else {
                  Codelist clTermList = pkg.get(row[1]);
                  Term t = new Term(row);
                  clTermList.AddTerm(t);
              }
          }
      } catch (IOException ex) {
            Logger.getLogger(CT2ODM.class.getName()).log(Level.SEVERE, null, ex);
      }
      return pkg;
    }
        
    /* write the ODM file to the output XML file using pretty print */
    private void writeOdmFile() {
        try {
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new FileWriter(odmFileName));
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }    
    }

    /* create the ODM XML root element node */
    private Element createRootNode() {
        Element root = new Element("ODM", this.ns.getOdmNamespace());
        root.addNamespaceDeclaration(this.ns.getOdmNamespace());
        root.addNamespaceDeclaration(this.ns.getNciNamespace());
        root.addNamespaceDeclaration(this.ns.getXsiNamespace());
        root.setAttribute(new Attribute("FileType", "Snapshot"));
        root.setAttribute(new Attribute("FileOID", "CDISC_CT." + this.standard + "." + this.pkgDate));
        root.setAttribute(new Attribute("Granularity", "Metadata"));
        root.setAttribute(new Attribute("CreationDateTime", LocalDateTime.now().withNano(0).toString()));
        root.setAttribute(new Attribute("AsOfDateTime", this.pkgDate + "T00:00:00"));
        root.setAttribute(new Attribute("ODMVersion", "1.3.2"));
        root.setAttribute(new Attribute("ControlledTerminologyVersion", this.version, this.ns.getNciNamespace()));
        root.setAttribute(new Attribute("Originator", "CDISC Data Exchange Standards Team (CT2ODM converter)"));
        root.setAttribute(new Attribute("SourceSystem", "NCI Thesaurus"));
        root.setAttribute(new Attribute("SourceSystemVersion", this.pkgDate));
        return root;
    }

    /* set properties based on the command-line arguments */
    private void setCommandLineOptions(String[] args) {
        this.version = "1.1.1";
        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
            if (argument.startsWith("odm=")) {
                this.odmFileName = argument.substring(argument.indexOf("=")+1); 
            } else if (argument.startsWith("txt=")) {
                this.txtFileName = argument.substring(argument.indexOf("=")+1); 
            } else if (argument.startsWith("std=")) {
                this.standard = argument.substring(argument.indexOf("=")+1); 
            } else if (argument.startsWith("date=")) {
                this.pkgDate = argument.substring(argument.indexOf("=")+1); 
            } else if (argument.startsWith("ver=")) {
                this.version = argument.substring(argument.indexOf("=")+1); 
            } else if (argument.equals("help")) {
                usage();
                System.exit(0);                
            } else {
                System.out.println("Unknown argument in " + CT2ODM.class.getName() + ": " + argument);
                usage();
                System.exit(0);                
            }
        }
    }
    
    /* ensure the cmd line options needed to proceed exist and are valid */
    private void validateCommandLineOptions() {
        if (this.txtFileName == null || this.txtFileName.isEmpty() || !(new File(this.txtFileName).isFile())) {
            invalidCommandLineExit("The controlled terminology text file is not found: " + this.txtFileName);
        }
        if (this.odmFileName == null || this.odmFileName.isEmpty()) {
            invalidCommandLineExit("Invalid ODM file name. A valid ODM file name is required.");
        }
        if (this.pkgDate == null || !isValidPackageDate()) {
            invalidCommandLineExit("Invalid package date. The package date of the input file is required (YYYY-MM-DD).");
        }
        if (this.standard == null || this.standard.isEmpty()) {
            invalidCommandLineExit("Missing standard name. The standard name (e.g. SDTM, SEND, CDASH, ADaM) is required.");
        }
    }
    
    /* tests for a valid package date */
    private boolean isValidPackageDate() {
        Boolean isValid = Boolean.TRUE;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            format.parse(this.pkgDate);
            if (!this.pkgDate.matches("\\d{4}-[01]\\d-[0-3]\\d")) {
                isValid = Boolean.FALSE;
            }
        } catch(ParseException e){
            return Boolean.FALSE;
        }
        return isValid;
    }    
    
    /* the command line input is invalid; show msg, usage, and exit */
    private void invalidCommandLineExit(String errorMessage) {
        System.out.println(errorMessage);
        usage();
        System.exit(0);                        
    }
    
    /* print the usage directions that include the command-line arguments */
    private void usage() {
        System.out.println("Usage: java -jar ct2odm.jar txt=<text CT file> odm=<ODM CT file> "
                + "std=<standard> date=<package date> ver=<schema version>");        
    }
}
