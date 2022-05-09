
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class transpose {

  public static void main(String argv[]) {
    try {
        //this line prevents a 403 error when trying to open link in MusicXML DTD
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36"); 

        //parses file
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        File myFile = new File("/users/hannahlaws/Capstone.musicxml");
        Document doc = dBuilder.parse(myFile);

        //initializes variables
        String direction = "down";
        int numSteps = 5;
        String step = "";
        String alter = "";
        String octave = "";
        String key = "";
        String newKey = "";

        //gets elements under the tag pitch...this includes step, alter, and octave
        NodeList pitchList = doc.getElementsByTagName("pitch");
        NodeList keyList = doc.getElementsByTagName("key");

            //changes key in both clefs
            for (int i = 0; i < keyList.getLength(); i++) {
                Node keyNode = keyList.item(i);
                
                key = keyNode.getTextContent();
                key = key.trim();
                newKey = getNewFifths(key, direction);

                for(int j = 0; j < numSteps - 1; j++ ){
                    newKey = getNewFifths(newKey, direction);
                    System.out.println("iteration " + j + ": " + newKey);
                }
                
                //sets key node in XML document
                NodeList childNodes = keyNode.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node item = childNodes.item(j);

                    if (item.getNodeType() == Node.ELEMENT_NODE) {

                        if ("fifths".equalsIgnoreCase(item.getNodeName())) {
                            // update xml element `step` text
                            item.setTextContent(newKey);
                        }//if     
                    }//if
                }//for j
            }//for 

            System.out.println(newKey);

            //gets new step, alter, and octave elements
            for (int i = 0; i < pitchList.getLength(); i++) {

                //gets pitch information
                Node pitchNode = pitchList.item(i);

                if (pitchNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) pitchNode;
                    step = eElement.getElementsByTagName("step").item(0).getTextContent();
                    octave = eElement.getElementsByTagName("octave").item(0).getTextContent();

                    //if there is no alter element in the XML, it gets set to zero
                    if(eElement.getElementsByTagName("alter").item(0) == null){
                        alter = "0";
                    }//if

                    else{
                        alter = eElement.getElementsByTagName("alter").item(0).getTextContent();

                    }//else

                    //prints out old step, octave, and alter
                    System.out.println("Step: " + step);
                    System.out.println("Octave: " + octave);
                    System.out.println("Alter: " + alter);

                    //first step transposition
                    String [] newNote = getNewNote(step, alter, octave, direction);
                    String newStep = newNote[0];
                    String newAlter = newNote[1];
                    String newOctave = newNote[2];

                    //remaining step transposition
                    for(int j = 0; j < numSteps - 1; j++)
                    {
                        newNote = getNewNote(newStep, newAlter, newOctave, direction);
                        newStep = newNote[0];
                        newAlter = newNote[1];
                        newOctave = newNote[2];
                      
                    }//for j  


                    //makes sure there are no flats in a sharp key and vice versa
                    if((Integer.parseInt(newKey)) > 0)
                    {
                        if(newAlter.equals("-1")){
                            newStep = getNewStep(newStep, "down");
                            newAlter = "1";
                        }
                    }

                    else if((Integer.parseInt(newKey)) < 0)
                    {
                        if(newAlter.equals("1")){
                            newStep = getNewStep(newStep, "up");
                            newAlter = "-1";
                        }
                    }

                    //changes text in XML nodes to new values
                    NodeList childNodes = pitchNode.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                                Node item = childNodes.item(j);

                                if (item.getNodeType() == Node.ELEMENT_NODE) {

                                    if ("step".equalsIgnoreCase(item.getNodeName())) {
                                        // update xml element `step` text
                                        item.setTextContent(newStep);
                                    }//if

                                    else if ("alter".equalsIgnoreCase(item.getNodeName())) {
                                        if(newAlter.equals("0")){
                                            pitchNode.removeChild(item);
                                        }
                                        // update xml element `alter` text
                                        item.setTextContent(newAlter);
                                    }//else if

                                    else if ("octave".equalsIgnoreCase(item.getNodeName())) {
                                        // update xml element `octave` text
                                        item.setTextContent(newOctave);
                                    }//else if 
                                }//if
                        }//for j

                        //adds alter element if there was not one before
                        if(alter.equals("0") && !newAlter.equals("0")){
                            Element addAlter = doc.createElement("alter");
                            addAlter.appendChild(doc.createTextNode(newAlter));
                                pitchNode.appendChild(addAlter);
                        }//if
                    
                        //prints out new 
                        System.out.println("New Step: " + newStep);
                        System.out.println("New Octave: " + newOctave);
                        System.out.println("New Alter: " + newAlter);

                    }//if


                }//for 

            //writes transposed version to new XML file
            FileOutputStream output = new FileOutputStream("/users/hannahlaws/NewCapstone.musicxml");
            writeXml(doc, output);
      
    }//try

        catch (Exception e) {
            e.printStackTrace();

        }//catch 

  }//main


    //method that ONLY changes pitch name (step)
    public static String getNewStep(String currentNote, String direction)
    {
        String [] notes = {"A", "B", "C", "D", "E","F", "G"};
        int length = notes.length;
        int i = 0;
        int currNoteIndex = -1;
        int newIndex = 0;

        //finds current note in array 'notes'
        while((currNoteIndex != i) && (i < length)){
            
            if (notes[i].equals(currentNote))
                currNoteIndex = i;
            i++;
     
        }

        //gets new note in array depending on transposition direction
        if (direction.equals("up")){
            if(currNoteIndex == (length - 1))
                newIndex = 0;
    
            else 
                newIndex = currNoteIndex + 1;
        }
           
        else if (direction.equals("down")){
            if(currNoteIndex == 0)
                newIndex = length - 1;
    
            else 
                newIndex = currNoteIndex - 1;
        }


        return notes[newIndex];
    }//getNewStep

    //method that changes key
    public static String getNewFifths(String currentFifths, String direction)
    {
        String [] fifths = {"0", "5", "-2", "3", "-4", "1", "-6", "-1", "4", "-3", "2", "-5"};
     
        int length = fifths.length;
        int i = 0;
        int currFifthIndex = -1;
        int newIndex = 0;

        //finds current key in array 'fifths'
        while((currFifthIndex != i) && (i < length)){

            if (fifths[i].equals(currentFifths))
                currFifthIndex = i;
            i++;
        }
        //gets new key in array depending on transposition direction
         if (direction.equals("down")){
            if(currFifthIndex == (length - 1))
                newIndex = 0;
    
            else 
                newIndex = currFifthIndex + 1;
        }
           
        else if (direction.equals("up")){
            if(currFifthIndex == 0)
                newIndex = length - 1;
    
            else 
                newIndex = currFifthIndex - 1;
        }

        return fifths[newIndex];
 
    }//getNewFifths

    //method to get transposed note, octave, and alter
    public static String[] getNewNote(String step, String alter, String octave, String direction)
    {
        int count = 0;
        String [] notes = {"A", "B", "C", "D", "E", "F", "G"};
        int length = notes.length;

        int i = 0;
        int currNoteIndex = -1;
        int newIndex;

        String newStep = "";
        String newOctave = "";
        String [] newNote = new String[3];

        //transposes based on direction
        if(direction.equals("up")){
            //different cases depending on if the note is natural, flat, or sharp
            switch(alter){

                case "0":
                    if (step.equals("E") || step.equals("B")){
                        newStep = getNewStep(step, direction);
                    }

                    else{
                        newStep = step;
                        alter = "1";
                    }

                break;

                case "1":
                    if(step.equals("E")|| step.equals("B")) //aka moving e# (f) up to f#
                        alter = "1";
                    else
                        alter = "0";
                    newStep = getNewStep(step, direction); // do this no matter what 

                break;

                case "-1":
                    alter = "0";
                    newStep = step;

                break;

            }//switch
                        
        }//if

        if(direction.equals("down")){
            switch(alter){
                //different cases depending on if the note is natural, flat, or sharp (alter)
                case "0":
                    if(step.equals("F")|| step.equals("C")){ //aka moving f down to e
                        newStep = getNewStep(step, direction);
                        alter = "0";
                    } 
                            
                    else{
                        newStep = step;
                        alter = "-1";
                    }

                break;

                case "1":
                    alter = "0";
                    newStep = step;
                       
                break;

                case "-1":

                    if(step.equals("F")|| step.equals("C")){ //aka moving f flat (e) down to e flat
                        newStep = getNewStep(step, direction);
                        alter = "-1";
                    } 
                    else{
                        alter = "0";
                        newStep = getNewStep(step, direction);
                    }

                break;

            }//switch
                        
        }//else if
            
        //changes octave if necessary
        int octaveInt = Integer.parseInt(octave);
        if (step.equals("B") && newStep.equals("C")){
            octaveInt++;
        }

        else if (step.equals("C") && newStep.equals("B")){
            octaveInt--;
        }

        newOctave = Integer.toString(octaveInt);
        

        //values of new note returned in an array
        newNote[0] = newStep;
        newNote[1] = alter;
        newNote[2] = newOctave;
        return newNote;

    }//getNewNote

    // write doc to output stream
    private static void writeXml(Document doc, OutputStream output) throws TransformerException, UnsupportedEncodingException {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);

        transformer.transform(source, result);

    }//writeXML

}//Class