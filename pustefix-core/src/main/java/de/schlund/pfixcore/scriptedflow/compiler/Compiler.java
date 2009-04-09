/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.scriptedflow.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.scriptedflow.vm.Instruction;
import de.schlund.pfixcore.scriptedflow.vm.JumpInstruction;
import de.schlund.pfixcore.scriptedflow.vm.NopInstruction;
import de.schlund.pfixcore.scriptedflow.vm.Script;
import de.schlund.pfixcore.scriptedflow.vm.pvo.DynamicObject;
import de.schlund.pfixcore.scriptedflow.vm.pvo.ListObject;
import de.schlund.pfixcore.scriptedflow.vm.pvo.ParamValueObject;
import de.schlund.pfixcore.scriptedflow.vm.pvo.StaticObject;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.util.Xml;

/**
 * Produces a low-level language Script object as it is understood
 * by the scripting VM.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.scriptedflow.vm.ScriptVM
 */
public class Compiler {
    public final static String NS_SCRIPTEDFLOW = "http://pustefix.sourceforge.net/scriptedflow200602";

    public static Script compile(FileResource scriptFile) throws CompilerException {
        Document doc;
        try {
            doc = Xml.parseMutable(scriptFile);
        } catch (SAXException e) {
            throw new CompilerException("XML parser could not parse file " + scriptFile.toString(), e);
        } catch (IOException e) {
            throw new CompilerException("XML parser could not read file " + scriptFile.toString(), e);
        }
        
        Element root = doc.getDocumentElement();
        if (!root.getLocalName().equals("scriptedflow") || !root.getNamespaceURI().equals(NS_SCRIPTEDFLOW)) {
            throw new CompilerException("Input file " + scriptFile.toString() + " is not a scripted flow!");
        }
        
        // Check version
        String version = root.getAttribute("version");

        // Assume default  version 1.0
        if (version == null) {
            version = "1.0";
        }
        
        if (!version.equals("1.0")) {
            throw new CompilerException("Script file \"" + scriptFile.toString() + "\" uses version "
                                        + version + " but compiler only supports version 1.0");
        }
        
        // Check name
        String name = root.getAttribute("name");
        
        if (name == null || name.equals("")) {
            name = "anonymous";
        }
        
        NodeList children = root.getChildNodes();
        
        // root block containing all instructions
        BlockStatement block = blockStatementFromNodeList(null, children);
        // make sure block ends with exit instruction
        block.addStatement(new ExitStatement(block));

        // Optimize code
        Instruction[] temp = block.getInstructions();
        temp = removeNops(temp);

        return new Script(temp, name);
    }
    
    private static Instruction[] removeNops(Instruction[] instructions) {
        List<Instruction> instr = new ArrayList<Instruction>();
        for (int i = 0; i < instructions.length; i++) {
            instr.add(instructions[i]);
        }
        
        for (int i = 0; i < instr.size(); i++) {
            Instruction in = instr.get(i);
            if (in instanceof NopInstruction) {
                // Get following instruction
                // This is save as we know that 
                // last instruction is never a NOP
                Instruction in2 = instr.get(i + 1);
                
                // Update references
                for (Instruction ref : instr) {
                    if (ref instanceof JumpInstruction) {
                        JumpInstruction jump = (JumpInstruction) ref;
                        if (jump.getTargetInstruction() == in) {
                            jump.setTargetInstruction(in2);
                        }
                    }
                }

                // Delete NOP
                instr.remove(i);

                // Decrement to make sure we examine next
                // instruction (which is now i, not i+1)
                i--;
            }
        }

        Instruction[] temp = new Instruction[instr.size()];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = instr.get(i);
        }

        return temp;
    }

    private static Statement statementFromElement(Statement parent,
            Element element) throws CompilerException {
        if (!element.getNamespaceURI().equals(NS_SCRIPTEDFLOW)) {
            throw new CompilerException("Namespace "
                    + element.getNamespaceURI()
                    + " cannot be handled by the compiler");
        }
        String elementName = element.getLocalName();
        if (elementName.equals("if")) {
            return ifStatementFromElement(parent, element);
        } else if (elementName.equals("while")) {
            return whileStatementFromElement(parent, element);
        } else if (elementName.equals("break")) {
            return breakStatementFromElement(parent, element);
        } else if (elementName.equals("choose")) {
            return chooseStatementFromElement(parent, element);
        } else if (elementName.equals("interactive-request")) {
            return interactiveRequestStatementFromElement(parent, element);
        } else if (elementName.equals("virtual-request")) {
            return virtualRequestStatementFromElement(parent, element);
        } else if (elementName.equals("set-variable")) {
            return setVariableStatementFromElement(parent, element);
        } else if (elementName.equals("exit")) {
            return exitStatementFromElement(parent, element);
        } else {
            throw new CompilerException("Found unknown command \""
                    + elementName + "\"");
        }
    }

    private static void addParametersToStatement(Element element, ParameterizedStatement stmt) throws CompilerException {
        NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) childNode;
                if (!child.getNamespaceURI().equals(NS_SCRIPTEDFLOW)) {
                    throw new CompilerException("Namespace " + child.getNamespaceURI() + " cannot be handled by the compiler");
                }
                if (child.getLocalName().equals("param")) {
                    String paramName = child.getAttribute("name");
                    if (paramName == null || paramName.length() == 0) {
                        throw new CompilerException("\"name\" attribute has to be set for \"param\" command");
                    }
                    if (child.getAttributes().getLength() != 1) {
                        throw new CompilerException("\"param\" command has exactly one attribute");
                    }
                    ParamValueObject paramValue = paramValueObjectFromNodeList(child.getChildNodes());
                    stmt.addParam(paramName, paramValue);
                } else {
                    throw new CompilerException("Element \"" + child.getLocalName() + "\" is not allowed below \"" + element.getNodeName() + "\" command");
                }
            } else if (childNode.getNodeType() == Node.TEXT_NODE) {
                if (!childNode.getNodeValue().matches("\\s*")) {
                    throw new CompilerException("Found illegal text data \"" + childNode.getNodeValue() + "\"!");
                }
            }
        }
    }
    
    private static VirtualRequestStatement virtualRequestStatementFromElement(Statement parent, Element element) throws CompilerException {
        String pagename = element.getAttribute("page");
        String interactive = element.getAttribute("dointeractive");

        NamedNodeMap attribs = element.getAttributes();
        for (int i = 0; i < attribs.getLength(); i++) {
            Node attnode = attribs.item(i);
            String name = attnode.getNodeName();
            if (name.equals("page")) {
                pagename = attnode.getNodeValue();
            } else if (name.equals("dointeractive")) {
                interactive = attnode.getNodeValue();
            } else {
                throw new CompilerException("\"virtual-request\" command only allows \"page\" and " + 
                "\"dointeractive\" attribute");                
            }
        }        
                
        VirtualRequestStatement stmt = new VirtualRequestStatement(parent);
        stmt.setPagename(pagename);
        stmt.setDointeractive(interactive);
        addParametersToStatement(element, stmt);
        
        return stmt;
    }

    private static SetVariableStatement setVariableStatementFromElement(Statement parent, Element element) throws CompilerException {
        String varname = element.getAttribute("name");
        if (varname == null || element.getAttributes().getLength() != 1) {
            throw new CompilerException("\"name\" attribute has to be set for \"set-variable\" command");
        }

        SetVariableStatement stmt = new SetVariableStatement(parent);
        stmt.setName(varname);

        NodeList list = element.getChildNodes();
        stmt.setValue(paramValueObjectFromNodeList(list));

        return stmt;
    }

    private static ParamValueObject paramValueObjectFromNodeList(NodeList list) throws CompilerException {
        if (list.getLength() == 0) {
            return new StaticObject("");
        }

        if (list.getLength() == 1) {
            Node node = list.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (!node.getNamespaceURI().equals(NS_SCRIPTEDFLOW)) {
                    throw new CompilerException("Namespace " + node.getNamespaceURI() + " cannot be handled by the compiler");
                }
                if (node.getLocalName().equals("value-of")) {
                    return dynamicParamFromElement((Element) node);
                } else {
                    throw new CompilerException("Element \"" + node.getNodeName() + "\" is not allowed below \"param\"");
                }

            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String value = node.getNodeValue();
                if (isWhitespace(value)) {
                    return new StaticObject("");
                } else {
                    return new StaticObject(value);
                }
            }
        }

        ListObject lo = new ListObject();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (!node.getNamespaceURI().equals(NS_SCRIPTEDFLOW)) {
                    throw new CompilerException("Namespace " + node.getNamespaceURI() + " cannot be handled by the compiler");
                }
                if (node.getLocalName().equals("value-of")) {
                    lo.addObject(dynamicParamFromElement((Element) node));
                } else {
                    throw new CompilerException("Element \"" + node.getNodeName() + "\" is not allowed below \"param\"");
                }
                
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                String value = node.getNodeValue();
                if (!isWhitespace(value)) {
                    lo.addObject(new StaticObject(node.getNodeValue()));
                }
            }
        }
        return lo;
    }

    private static ParamValueObject dynamicParamFromElement(Element element) throws CompilerException {
        String expr = element.getAttribute("select");
        if (expr == null || expr.length() == 0) {
            throw new CompilerException("\"select\" attribute has to be set on \"value-of\" command");
        }
        if (element.getAttributes().getLength() != 1) {
            throw new CompilerException("\"value-of\" command has exactly one attribute");
        }
        return new DynamicObject(expr);
    }

    private static InteractiveRequestStatement interactiveRequestStatementFromElement(Statement parent, Element element) throws CompilerException {
        if (element.getAttributes() != null && element.getAttributes().getLength() != 0) {
            throw new CompilerException("\"interactive-request\" command has no attributes");
        }
        InteractiveRequestStatement stmt = new InteractiveRequestStatement(parent);

        addParametersToStatement(element, stmt);
            
        return stmt;
    }
    
    private static ExitStatement exitStatementFromElement(Statement parent, Element element) throws CompilerException {
        if (element.getAttributes() != null && element.getAttributes().getLength() != 0) {
            throw new CompilerException("\"exit\" command has no attributes");
        }
        ExitStatement stmt = new ExitStatement(parent);

        addParametersToStatement(element, stmt);

        return stmt;
    }

    private static ChooseStatement chooseStatementFromElement(Statement parent, Element element) throws CompilerException {
        if (element.getAttributes() != null && element.getAttributes().getLength() != 0) {
            throw new CompilerException("\"choose\" command has no attributes");
        }
        ChooseStatement stmt = new ChooseStatement(parent);
        
        NodeList list = element.getChildNodes();
        boolean foundOtherwise = false;
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) childNode;
                if (!child.getNamespaceURI().equals(NS_SCRIPTEDFLOW)) {
                    throw new CompilerException("Namespace " + child.getNamespaceURI() + " cannot be handled by the compiler");
                }
                if (child.getLocalName().equals("when")) {
                    if (foundOtherwise) {
                        throw new CompilerException("No \"when\" allowed after \"otherwise\"");
                    }
                    String condition = child.getAttribute("test");
                    if (condition == null || condition.length() == 0) {
                        throw new CompilerException("\"test\" attribute has to be set for \"when\" command");
                    }
                    if (child.getAttributes().getLength() != 1) {
                        throw new CompilerException("\"when\" command has exactly one attribute");
                    }
                    Statement block = blockStatementFromNodeList(stmt, child.getChildNodes());
                    stmt.addBranch(condition, block);
                } else if (child.getLocalName().equals("otherwise")) {
                    foundOtherwise = true;
                    if (child.getAttributes() != null && child.getAttributes().getLength() != 0) {
                        throw new CompilerException("\"otherwise\" command has no attributes");
                    }
                    Statement block = blockStatementFromNodeList(stmt, child.getChildNodes());
                    stmt.addBranch(null, block);
                } else {
                    throw new CompilerException("Element \"" + child.getLocalName() + "\" is not allowed below \"choose\" command");
                }
            } else if (childNode.getNodeType() == Node.TEXT_NODE) {
                if (!childNode.getNodeValue().matches("\\s*")) {
                    throw new CompilerException("Found illegal text data \"" + childNode.getNodeValue() + "\"!");
                }
            }
        }
        return stmt;
    }

    private static BreakStatement breakStatementFromElement(Statement parent, Element element) throws CompilerException {
        if (element.getAttributes() != null && element.getAttributes().getLength() != 0) {
            throw new CompilerException("\"break\" command has no attributes");
        }
        try {
            return new BreakStatement(parent);
        } catch (RuntimeException e) {
            throw new CompilerException("\"break\" is only allowed below a \"while\" block");
        }
    }

    private static WhileStatement whileStatementFromElement(Statement parent, Element element) throws CompilerException {
        String condition = element.getAttribute("test");
        if (condition == null || condition.length() == 0) {
            throw new CompilerException("\"test\" attribute has to be set for \"while\" command");
        }
        if (element.getAttributes().getLength() != 1) {
            throw new CompilerException("\"while\" command has exactly one attribute");
        }
        WhileStatement stmt = new WhileStatement(parent);
        stmt.setCondition(condition);
        stmt.setChild(blockStatementFromNodeList(stmt, element.getChildNodes()));
        return stmt;
    }

    private static IfStatement ifStatementFromElement(Statement parent, Element element) throws CompilerException {
        String condition = element.getAttribute("test");
        if (condition == null || condition.length() == 0) {
            throw new CompilerException("\"test\" attribute has to be set for \"if\" command");
        }
        if (element.getAttributes().getLength() != 1) {
            throw new CompilerException("\"if\" command has exactly one attribute");
        }
        IfStatement stmt = new IfStatement(parent);
        stmt.setCondition(condition);
        stmt.setChild(blockStatementFromNodeList(stmt, element.getChildNodes()));
        return stmt;
    }
    
    private static BlockStatement blockStatementFromNodeList(Statement parent, NodeList list) throws CompilerException {
        BlockStatement block = new BlockStatement(parent);
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                block.addStatement(statementFromElement(block, (Element) child));
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                if (!isWhitespace(child.getNodeValue())) {
                    throw new CompilerException("Found illegal text data \"" + child.getNodeValue() + "\"!");
                }
            }
        }
        return block;
    }

    private static boolean isWhitespace(String teststr) {
        return teststr.matches("\\s*");
    }
}
